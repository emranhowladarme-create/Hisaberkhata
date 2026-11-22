package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.support.KbCoverage
import com.example.support.SUPPORT_HELPLINE_NUMBER
import com.example.support.SupportIssueCategory
import com.example.support.SupportProblemDetector
import com.example.support.SupportTextPolicy
import com.example.support.VerifiedKnowledgeBase
import com.example.util.Formatters
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

sealed interface SupportAiUiState {
    data object Idle : SupportAiUiState
    data object Loading : SupportAiUiState
    data class Streaming(val exchangeId: String) : SupportAiUiState
    data object Success : SupportAiUiState
    data class Error(val message: String) : SupportAiUiState
}

data class SupportChatExchange(
    val id: String,
    val userQuestion: String,
    val imageUri: Uri?,
    val replyText: String,
    val bulletPoints: List<String>,
    val needsHumanSupport: Boolean,
    val forwardedToHumanTeam: Boolean,
    val timeLabel: String,
    val isStreaming: Boolean = false,
    val detectedCategory: SupportIssueCategory = SupportIssueCategory.GENERAL_APP_INFO,
    val kbCoverage: KbCoverage = KbCoverage.VERIFIED_MATCH,
    val kbEntryTitles: List<String> = emptyList(),
    val clarificationPrompt: String? = null,
)

data class SupportAssistantContextSnapshot(
    val businessName: String,
    val ownerName: String,
    val ownerPhone: String,
    val customerCount: Int,
    val totalPaona: Double,
    val totalDena: Double,
    val todayIncome: Double,
    val todayExpense: Double,
    val smsBalance: Long,
)

data class SupportConversationTurn(
    val userQuestion: String,
    val assistantReply: String,
)

interface SupportAiGateway {
    fun preWarmAppCheck()
    fun streamSupportReply(
        systemPrompt: String,
        groundedPrompt: String,
        priorTurns: List<SupportConversationTurn>,
        attachedBitmap: Bitmap?,
    ): Flow<String>
}

class FirebaseSupportAiGateway : SupportAiGateway {

    private fun buildSystemContent(systemPrompt: String): Content = content {
        text(systemPrompt)
    }

    override fun preWarmAppCheck() {
        runCatching {
            FirebaseAppCheck.getInstance().getAppCheckToken(false)
                .addOnSuccessListener { Log.d(TAG, "App Check token pre-warmed.") }
                .addOnFailureListener { err -> Log.w(TAG, "App Check pre-warm warning: ${err.message}") }
        }
    }

    override fun streamSupportReply(
        systemPrompt: String,
        groundedPrompt: String,
        priorTurns: List<SupportConversationTurn>,
        attachedBitmap: Bitmap?,
    ): Flow<String> {
        val systemInstruction = buildSystemContent(systemPrompt)
        val model = if (attachedBitmap != null) {
            Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    temperature = 0.2f
                    maxOutputTokens = 560
                },
                systemInstruction = systemInstruction,
            )
        } else {
            Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = "gemini-3.5-flash-lite",
                generationConfig = generationConfig {
                    temperature = 0.2f
                    maxOutputTokens = 480
                },
                systemInstruction = systemInstruction,
            )
        }

        val responseFlow = if (priorTurns.isNotEmpty() && attachedBitmap == null) {
            val chatHistory = mutableListOf<Content>()
            priorTurns.forEach { turn ->
                chatHistory += content("user") { text(turn.userQuestion) }
                if (turn.assistantReply.isNotBlank()) {
                    chatHistory += content("model") { text(turn.assistantReply) }
                }
            }
            model.startChat(history = chatHistory).sendMessageStream(groundedPrompt)
        } else {
            val promptContents = content {
                if (attachedBitmap != null) {
                    image(attachedBitmap)
                }
                if (priorTurns.isNotEmpty()) {
                    val summary = priorTurns.takeLast(2).joinToString("\n") { prev ->
                        "আগের প্রশ্ন: ${prev.userQuestion} | আগের উত্তর: ${prev.assistantReply}"
                    }
                    text("আগের কথোপকথন:\n$summary\n\n$groundedPrompt")
                } else {
                    text(groundedPrompt)
                }
            }
            model.generateContentStream(promptContents)
        }

        return responseFlow.map { chunk -> chunk.text.orEmpty() }
    }

    private companion object {
        const val TAG = "FirebaseSupportGateway"
    }
}

class SupportAiViewModel(
    private val aiGateway: SupportAiGateway = FirebaseSupportAiGateway(),
) : ViewModel() {

    private val systemPromptText: String =
        """
        তুমি 'হিসাবের খাতা' (Hishaber Khata) অ্যাপের অফিশিয়াল ও বুদ্ধিমান AI কাস্টমার সাপোর্ট সহকারী।
        তোমার মূল দায়িত্ব হলো তিনটি ক্ষেত্রে কাস্টমারকে নির্ভরযোগ্য সাহায্য করা:
        ১) অ্যাপের সম্পূর্ণ ও নির্ভুল তথ্য (আয়, ব্যয়, লেনদেন, কাস্টমার/সাপ্লায়ার অ্যাকাউন্ট, বাকি আদায়, তাগাদা, বাংলা QR, স্টক, নোট, মাল্টি-ব্যবসা, রিপোর্ট ও সেটিংস),
        ২) কারিগরি সমস্যা নির্ণয় ও সমাধান (লগইন, রেজিস্ট্রেশন, OTP, Firebase Authentication, Invalid Token / App Check 403 এরর, ব্যাকআপ/রিস্টোর/সিঙ্ক),
        ৩) আন্তরিক, দ্রুত ও ভদ্র কাস্টমার সাপোর্ট (বাংলা ডিফল্ট ভাষা; ইংরেজিতে প্রশ্ন করলে স্পষ্ট ইংরেজিতে উত্তর)।

        STRICT VERIFIED KNOWLEDGE & HONESTY RULES (কঠোরভাবে মান্য করবে):
        - শুধুমাত্র প্রদত্ত VERIFIED_KNOWLEDGE_BASE_ENTRIES ও অ্যাপে প্রকৃতপক্ষে বিদ্যমান ফিচারগুলোর ভিত্তিতে উত্তর দেবে।
        - কোনো তথ্য, দাম, নীতি বা ফিচার বানিয়ে (fabricate) লিখবে না বা অনুমান করবে না।
        - যদি কোনো তথ্যের নিশ্চিত প্রমাণ নলেজ বেসে না থাকে, স্পষ্টভাবে বলবে যে বিষয়টির নিশ্চিতকরণ প্রয়োজন (needs confirmation) এবং কখনোই ১০০% সঠিকতার মিথ্যা প্রতিশ্রুতি দেবে না।
        - সরাসরি কোনো ডেটাবেজ বা সার্ভার পরীক্ষা করেছো বলে মিথ্যা দাবি করবে না।
        - কোনো কারিগরি সমস্যা ব্যবহারকারী যাচাই করার আগেই "সমাধান হয়ে গেছে" বলে দাবি করবে না; নিরাপদ ধাপগুলো ক্রমানুসারে (১, ২, ৩...) দেবে।
        - প্রাসঙ্গিক অতিরিক্ত তথ্য প্রয়োজন হলে ভদ্রভাবে প্রশ্ন করবে।

        SECURITY & PRIVACY:
        - কখনোই পাসওয়ার্ড, পিন (PIN), OTP, API Key বা Firebase Authentication Token চাইবে না, প্রকাশ করবে না বা লগ করবে না।
        - অন্য কোনো কাস্টমারের আর্থিক তথ্য দেখাবে না বা অনুমতি ছাড়া হিসাবের কোনো এন্ট্রি পরিবর্তন করবে না।

        HUMAN SUPPORT ESCALATION (ONLY OFFICIAL HELPLINE):
        - কোনো সমস্যা অমীমাংসিত থাকলে, যাচাইয়ের বাইরে থাকলে বা মানব সহায়তা প্রয়োজন হলে একমাত্র অফিশিয়াল হেল্পলাইন নম্বর $SUPPORT_HELPLINE_NUMBER-এ যোগাযোগ করতে বলবে এবং উত্তরের একদম শেষ লাইনে `${SupportTextPolicy.HUMAN_ESCALATION_MARKER}` লিখবে।
        - কখনোই WhatsApp, ইমেইল বা কোনো অতিরিক্ত লাইভ চ্যাটের কথা উল্লেখ করবে না।

        COMMUNICATION STYLE:
        - বাংলা হবে স্বাভাবিক, সহজ, ভদ্র ও পেশাদার। জটিল প্রক্রিয়ায় সংখ্যাক্রমে (১, ২, ৩) ধাপ লিখবে।
        - কথোপকথনের আগের প্রসঙ্গ মনে রেখে উত্তর দেবে এবং শুধুমাত্র লিখিত টেক্সট মোডে সহায়তা দেবে।
        """.trimIndent()

    private val _uiState = MutableStateFlow<SupportAiUiState>(SupportAiUiState.Idle)
    val uiState: StateFlow<SupportAiUiState> = _uiState.asStateFlow()

    private val _exchanges = MutableStateFlow<List<SupportChatExchange>>(emptyList())
    val exchanges: StateFlow<List<SupportChatExchange>> = _exchanges.asStateFlow()

    private val _assistantInitialized = MutableStateFlow(false)
    val assistantInitialized: StateFlow<Boolean> = _assistantInitialized.asStateFlow()

    private val _duplicateBlockedNotice = MutableStateFlow<String?>(null)
    val duplicateBlockedNotice: StateFlow<String?> = _duplicateBlockedNotice.asStateFlow()

    private var activeRequestJob: Job? = null
    private var inFlightSignature: String? = null
    private var lastCompletedSignature: String? = null

    init {
        initializeAssistantIfNeeded()
    }

    fun initializeAssistantIfNeeded() {
        if (_assistantInitialized.value) return
        _assistantInitialized.value = true
        viewModelScope.launch(Dispatchers.IO) {
            aiGateway.preWarmAppCheck()
        }
    }

    fun clearConversation() {
        activeRequestJob?.cancel()
        activeRequestJob = null
        inFlightSignature = null
        lastCompletedSignature = null
        _duplicateBlockedNotice.value = null
        _exchanges.value = emptyList()
        _uiState.value = SupportAiUiState.Idle
    }

    fun dismissError() {
        if (_uiState.value is SupportAiUiState.Error) {
            _uiState.value = SupportAiUiState.Idle
        }
    }

    fun dismissDuplicateNotice() {
        _duplicateBlockedNotice.value = null
    }

    fun markForwardedToHumanTeam(exchangeId: String) {
        _exchanges.update { list ->
            list.map { item ->
                if (item.id == exchangeId) item.copy(forwardedToHumanTeam = true) else item
            }
        }
    }

    fun askSupport(
        context: Context,
        rawQuestion: String,
        attachedImageUri: Uri?,
        businessContext: SupportAssistantContextSnapshot,
        onCreateTicket: (subject: String, description: String) -> Unit = { _, _ -> },
        isRetry: Boolean = false,
    ) {
        initializeAssistantIfNeeded()
        val cleanQuestion = rawQuestion.trim()
        if (cleanQuestion.isEmpty() && attachedImageUri == null) return

        val effectiveQuestion = cleanQuestion.ifEmpty {
            "সংযুক্ত ছবি/স্ক্রিনশটটি দেখে সহজভাবে সমাধান জানান।"
        }

        val requestSignature = buildRequestSignature(effectiveQuestion, attachedImageUri)
        if (_uiState.value is SupportAiUiState.Loading || _uiState.value is SupportAiUiState.Streaming) {
            _duplicateBlockedNotice.value = "একটি প্রশ্নের উত্তর তৈরি হচ্ছে, অনুগ্রহ করে একটু অপেক্ষা করুন।"
            return
        }
        if (!isRetry && (requestSignature == inFlightSignature || requestSignature == lastCompletedSignature)) {
            _duplicateBlockedNotice.value = "এই প্রশ্নের উত্তর ইতিমধ্যে দেওয়া হয়েছে।"
            return
        }

        _duplicateBlockedNotice.value = null
        inFlightSignature = requestSignature

        val priorCompleted = _exchanges.value.filter { !it.isStreaming }.takeLast(6)
        val unresolvedCount = priorCompleted.count { it.needsHumanSupport }
        val classification = SupportProblemDetector.analyze(
            rawQuery = effectiveQuestion,
            priorExchangeCount = priorCompleted.size,
            unresolvedHistoryCount = unresolvedCount,
        )
        val kbRetrieval = VerifiedKnowledgeBase.retrieveRelevantEntries(
            query = effectiveQuestion,
            detectedCategory = classification.category,
        )

        val exchangeId = "${System.currentTimeMillis()}_${_exchanges.value.size}"
        val nowTimeLabel = Formatters.formatBanglaClockTime(System.currentTimeMillis())

        // Security guard: immediately block secret/token/unauthorized record requests without sending to model
        if (classification.isSecurityOrPrivacyViolation) {
            val safeRefusal = classification.immediateSafetyWarningBn
                ?: "নিরাপত্তা ও গোপনীয়তা নীতির কারণে পাসওয়ার্ড, OTP, PIN, Firebase টোকেন বা অন্য কারো হিসাবের তথ্য প্রদান ও পরিবর্তন করা সম্ভব নয়।"
            val blockedExchange = SupportChatExchange(
                id = exchangeId,
                userQuestion = SupportProblemDetector.redactSensitiveTokens(effectiveQuestion),
                imageUri = null,
                replyText = safeRefusal,
                bulletPoints = listOf(
                    "১. কখনোই পাসওয়ার্ড, ৪-ডিজিট PIN, OTP বা টোকেন কারো সাথে শেয়ার করবেন না।",
                    "২. হিসাবের খাতায় প্রতিটি ব্যবহারকারীর ডেটা কেবল নিজস্ব যাচাইকৃত অ্যাকাউন্টের মাধ্যমে সুরক্ষিত থাকে।",
                ),
                needsHumanSupport = false,
                forwardedToHumanTeam = false,
                timeLabel = nowTimeLabel,
                isStreaming = false,
                detectedCategory = classification.category,
                kbCoverage = KbCoverage.VERIFIED_MATCH,
                kbEntryTitles = kbRetrieval.matchedEntries.map { it.titleBn },
            )
            appendExchangeIdempotently(blockedExchange)
            inFlightSignature = null
            lastCompletedSignature = requestSignature
            _uiState.value = SupportAiUiState.Success
            return
        }

        val redactedQuestion = SupportProblemDetector.redactSensitiveTokens(effectiveQuestion)
        val initialExchange = SupportChatExchange(
            id = exchangeId,
            userQuestion = redactedQuestion,
            imageUri = attachedImageUri,
            replyText = "",
            bulletPoints = emptyList(),
            needsHumanSupport = false,
            forwardedToHumanTeam = false,
            timeLabel = nowTimeLabel,
            isStreaming = true,
            detectedCategory = classification.category,
            kbCoverage = kbRetrieval.coverage,
            kbEntryTitles = kbRetrieval.matchedEntries.map { it.titleBn },
            clarificationPrompt = classification.missingDetailsQuestionBn,
        )
        appendExchangeIdempotently(initialExchange)
        _uiState.value = SupportAiUiState.Loading

        activeRequestJob = viewModelScope.launch {
            try {
                val decodedBitmap = if (attachedImageUri != null) {
                    withContext(Dispatchers.IO) { decodeScaledBitmap(context, attachedImageUri) }
                } else {
                    null
                }

                val isEnglish = SupportTextPolicy.isLikelyEnglish(redactedQuestion)
                val groundedPrompt = buildGroundedPrompt(
                    redactedQuestion = redactedQuestion,
                    businessContext = businessContext,
                    category = classification.category,
                    kbContext = kbRetrieval.formattedContextForModel,
                    kbCoverage = kbRetrieval.coverage,
                    missingDetailsQuestion = classification.missingDetailsQuestionBn,
                    suggestEscalation = classification.suggestHumanEscalation,
                    isEnglish = isEnglish,
                )

                val priorTurns = priorCompleted.map { prev ->
                    SupportConversationTurn(
                        userQuestion = prev.userQuestion,
                        assistantReply = prev.replyText.ifBlank { prev.bulletPoints.joinToString("\n") },
                    )
                }

                val accumulated = StringBuilder()
                withTimeout(45_000L) {
                    aiGateway.streamSupportReply(
                        systemPrompt = systemPromptText,
                        groundedPrompt = groundedPrompt,
                        priorTurns = priorTurns,
                        attachedBitmap = decodedBitmap,
                    ).collect { piece ->
                        if (piece.isNotEmpty()) {
                            accumulated.append(piece)
                            _uiState.value = SupportAiUiState.Streaming(exchangeId)
                            val parsedPartial = parseSupportReply(
                                rawText = accumulated.toString(),
                                forceEscalation = classification.suggestHumanEscalation ||
                                    kbRetrieval.coverage == KbCoverage.UNVERIFIED_OR_UNKNOWN,
                            )
                            updateExchangeById(exchangeId) { current ->
                                current.copy(
                                    replyText = parsedPartial.mainText,
                                    bulletPoints = parsedPartial.bullets,
                                    needsHumanSupport = parsedPartial.needsHumanSupport,
                                    isStreaming = true,
                                )
                            }
                        }
                    }
                }

                val finalRaw = accumulated.toString().trim()
                if (finalRaw.isBlank()) {
                    throw IllegalStateException("সাপোর্ট সার্ভার থেকে কোনো উত্তর পাওয়া যায়নি।")
                }

                val shouldEscalate = classification.suggestHumanEscalation ||
                    kbRetrieval.coverage == KbCoverage.UNVERIFIED_OR_UNKNOWN
                val parsedFinal = parseSupportReply(
                    rawText = finalRaw,
                    forceEscalation = shouldEscalate,
                )

                val finalReplyText = ensureVerificationHonesty(
                    replyText = parsedFinal.mainText,
                    kbCoverage = kbRetrieval.coverage,
                    missingDetailsQuestion = classification.missingDetailsQuestionBn,
                    isEnglish = isEnglish,
                )

                if (parsedFinal.needsHumanSupport) {
                    onCreateTicket(
                        "${classification.category.labelBn}: ${redactedQuestion.take(45)}",
                        "প্রশ্ন: $redactedQuestion\nহেল্পলাইন: $SUPPORT_HELPLINE_NUMBER",
                    )
                }

                updateExchangeById(exchangeId) { current ->
                    current.copy(
                        replyText = finalReplyText,
                        bulletPoints = parsedFinal.bullets,
                        needsHumanSupport = parsedFinal.needsHumanSupport,
                        forwardedToHumanTeam = false,
                        isStreaming = false,
                    )
                }
                lastCompletedSignature = requestSignature
                _uiState.value = SupportAiUiState.Success
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Support AI request timed out", e)
                removeIncompleteExchange(exchangeId)
                _uiState.value = SupportAiUiState.Error(
                    "উত্তর তৈরি হতে প্রত্যাশার চেয়ে বেশি সময় লেগেছে। অনুগ্রহ করে ইন্টারনেট সংযোগ দেখে আবার চেষ্টা করুন অথবা হেল্পলাইন $SUPPORT_HELPLINE_NUMBER নম্বরে যোগাযোগ করুন।"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Support AI request failed", e)
                removeIncompleteExchange(exchangeId)
                _uiState.value = SupportAiUiState.Error(
                    e.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "সাপোর্ট সেবার সাথে সংযোগ করা যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন অথবা হেল্পলাইন $SUPPORT_HELPLINE_NUMBER নম্বরে যোগাযোগ করুন।"
                )
            } finally {
                inFlightSignature = null
                activeRequestJob = null
            }
        }
    }

    private fun buildRequestSignature(question: String, attachedImageUri: Uri?): String {
        val norm = VerifiedKnowledgeBase.normalizeBengaliAndEnglish(question)
            .replace(Regex("\\s+"), " ")
            .trim()
        return "$norm|img=${attachedImageUri?.toString().orEmpty()}"
    }

    private fun buildGroundedPrompt(
        redactedQuestion: String,
        businessContext: SupportAssistantContextSnapshot,
        category: SupportIssueCategory,
        kbContext: String,
        kbCoverage: KbCoverage,
        missingDetailsQuestion: String?,
        suggestEscalation: Boolean,
        isEnglish: Boolean,
    ): String {
        return buildString {
            appendLine("[DIAGNOSTIC_METADATA]")
            appendLine("সমস্যার ধরন: ${category.labelBn} (${category.name})")
            appendLine("নলেজ বেস কভারেজ: ${kbCoverage.name}")
            appendLine("উত্তরের ভাষা: ${if (isEnglish) "English (concise & polite)" else "সহজ ও ভদ্র বাংলা"}")
            if (suggestEscalation) {
                appendLine("এস্ক্যালেশন নির্দেশ: উত্তরের শেষে অবশ্যই হেল্পলাইন $SUPPORT_HELPLINE_NUMBER উল্লেখ করবে এবং শেষ লাইনে ${SupportTextPolicy.HUMAN_ESCALATION_MARKER} যুক্ত করবে।")
            }
            if (!missingDetailsQuestion.isNullOrBlank()) {
                appendLine("প্রয়োজনীয় বাড়তি তথ্য জিজ্ঞাসা করবে: $missingDetailsQuestion")
            }
            appendLine()
            appendLine("[ACTIVE_APP_SESSION_INFO - Read-only snapshot on screen, NOT a live server check]")
            appendLine(
                "ব্যবসা='${businessContext.businessName}', মালিক='${businessContext.ownerName}', " +
                    "কাস্টমার=${businessContext.customerCount}, পাবো=${businessContext.totalPaona.toLong()}৳, " +
                    "দেবো=${businessContext.totalDena.toLong()}৳, SMS ব্যালেন্স=${businessContext.smsBalance}"
            )
            appendLine()
            appendLine("[$kbContext]")
            appendLine()
            appendLine("[USER_QUESTION]")
            append(redactedQuestion)
        }
    }

    private fun ensureVerificationHonesty(
        replyText: String,
        kbCoverage: KbCoverage,
        missingDetailsQuestion: String?,
        isEnglish: Boolean,
    ): String {
        var result = SupportProblemDetector.redactSensitiveTokens(replyText)
        if (kbCoverage == KbCoverage.UNVERIFIED_OR_UNKNOWN &&
            !result.contains("নিশ্চিত") &&
            !result.contains("যাচাই") &&
            !result.contains("confirm", ignoreCase = true) &&
            !result.contains("verif", ignoreCase = true)
        ) {
            val disclaimer = if (isEnglish) {
                "Note: This specific detail needs confirmation against our verified records. Please contact our official helpline ($SUPPORT_HELPLINE_NUMBER) for accurate assistance."
            } else {
                "বিঃদ্রঃ এই নির্দিষ্ট বিষয়টির নিশ্চিত তথ্য যাচাই করা প্রয়োজন। অনুমানভিত্তিক তথ্য দেওয়া সম্ভব নয়—সঠিক সহায়তার জন্য হেল্পলাইন $SUPPORT_HELPLINE_NUMBER নম্বরে যোগাযোগ করুন।"
            }
            result = if (result.isBlank()) disclaimer else "$result\n\n$disclaimer"
        }
        if (!missingDetailsQuestion.isNullOrBlank() &&
            !result.contains("জানান") &&
            !result.contains("?")
        ) {
            result = "$result\n\n$missingDetailsQuestion"
        }
        return result
    }

    private fun appendExchangeIdempotently(exchange: SupportChatExchange) {
        _exchanges.update { list ->
            if (list.any { it.id == exchange.id }) list else list + exchange
        }
    }

    private fun updateExchangeById(
        exchangeId: String,
        transform: (SupportChatExchange) -> SupportChatExchange,
    ) {
        _exchanges.update { list ->
            list.map { if (it.id == exchangeId) transform(it) else it }
        }
    }

    private fun removeIncompleteExchange(exchangeId: String) {
        _exchanges.update { list ->
            list.filterNot { it.id == exchangeId && it.isStreaming }
        }
    }

    private data class ParsedSupportReply(
        val mainText: String,
        val bullets: List<String>,
        val needsHumanSupport: Boolean,
    )

    private fun parseSupportReply(
        rawText: String,
        forceEscalation: Boolean = false,
    ): ParsedSupportReply {
        val redacted = SupportProblemDetector.redactSensitiveTokens(rawText)
        val needsHuman = forceEscalation ||
            redacted.contains(SupportTextPolicy.HUMAN_ESCALATION_MARKER, ignoreCase = true)
        val cleanedLines = redacted
            .replace(SupportTextPolicy.HUMAN_ESCALATION_MARKER, "")
            .lines()
            .map { line ->
                line.replace("**", "")
                    .replace("__", "")
                    .trim()
            }
            .filter { it.isNotEmpty() }

        val bullets = mutableListOf<String>()
        val introLines = mutableListOf<String>()

        for (line in cleanedLines) {
            if (line.startsWith("• ") || line.startsWith("- ") || line.startsWith("* ")) {
                bullets += line.removePrefix("• ").removePrefix("- ").removePrefix("* ").trim()
            } else {
                introLines += line
            }
        }

        val mainText = if (introLines.isNotEmpty()) {
            introLines.joinToString("\n")
        } else {
            bullets.firstOrNull().orEmpty()
        }
        val finalBullets = if (introLines.isEmpty() && bullets.size <= 1) emptyList() else bullets

        return ParsedSupportReply(
            mainText = mainText,
            bullets = finalBullets,
            needsHumanSupport = needsHuman,
        )
    }

    private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
        return runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)
            val maxDim = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > 1024) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
        }.onFailure {
            Log.e(TAG, "Failed to decode attached image bitmap", it)
        }.getOrNull()
    }

    private companion object {
        const val TAG = "SupportAiVM"
    }
}
