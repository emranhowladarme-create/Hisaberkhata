package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.SupportTicket
import com.example.support.SUPPORT_HELPLINE_DIAL_URI
import com.example.support.SUPPORT_HELPLINE_NUMBER
import com.example.ui.components.EmptyStateBox
import com.example.ui.theme.CardBorder
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenDark
import com.example.ui.theme.HisabGreenDeep
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PaonaGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftCanvas
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.HisaberAppData
import com.example.ui.viewmodel.SupportAiUiState
import com.example.ui.viewmodel.SupportAiViewModel
import com.example.ui.viewmodel.SupportAssistantContextSnapshot
import com.example.ui.viewmodel.SupportChatExchange
import com.example.util.Formatters

private val SupportHeaderGreenDark = Color(0xFF0B8A1D)
private val SupportHeaderGreenLight = Color(0xFF15A42A)
private val SupportMintBg = Color(0xFFEFFDF1)
private val SupportMintBorder = Color(0xFF93DF9F)
private val SupportInkDark = Color(0xFF102A1D)

private enum class SupportTab {
    SUPPORT_LINE,
    HELP_LINE,
    HISTORY,
}

private data class QuickQuestionPill(
    val id: String,
    val label: String,
    val prompt: String,
    val icon: ImageVector,
)

private val QuickQuestionPills = listOf(
    QuickQuestionPill(
        id = "add_hisab",
        label = "হিসাব যোগ করা",
        prompt = "হিসাবি খাতা অ্যাপে নতুন কাস্টমার ও হিসাব কীভাবে যোগ করব?",
        icon = Icons.Default.Calculate,
    ),
    QuickQuestionPill(
        id = "view_report",
        label = "রিপোর্ট দেখা",
        prompt = "দৈনিক ও মাসিক হিসাবের রিপোর্ট এবং PDF কীভাবে দেখব?",
        icon = Icons.AutoMirrored.Filled.TrendingUp,
    ),
    QuickQuestionPill(
        id = "data_security",
        label = "ডেটা সুরক্ষা",
        prompt = "আমার ব্যবসার হিসাব ও ডেটা সুরক্ষা ও ব্যাকআপ কীভাবে কাজ করে?",
        icon = Icons.Default.VerifiedUser,
    ),
    QuickQuestionPill(
        id = "premium",
        label = "প্রিমিয়াম ভার্সন",
        prompt = "প্রিমিয়াম বা গোল্ড মেম্বারশিপ ভার্সনে কী কী সুবিধা পাওয়া যায়?",
        icon = Icons.Default.CardGiftcard,
    ),
    QuickQuestionPill(
        id = "how_to_use",
        label = "অ্যাপ ব্যবহারে পদ্ধতি",
        prompt = "হিসাবি খাতা অ্যাপ ব্যবহারের সহজ নিয়ম সংক্ষেপে বলুন।",
        icon = Icons.Default.Help,
    ),
    QuickQuestionPill(
        id = "other_issues",
        label = "অন্যান্য সমস্যা",
        prompt = "অ্যাপ ব্যবহারে কোনো সমস্যা হলে মানব সাপোর্ট টিমের সহায়তা কীভাবে পাব?",
        icon = Icons.Default.Settings,
    ),
)

@Composable
fun HelpCenterScreen(
    appData: HisaberAppData,
    tickets: List<SupportTicket>,
    startOnFaq: Boolean = false,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onNavigateHome: () -> Unit = onBack,
    onNavigateLedger: () -> Unit = onBack,
    onNavigateReports: () -> Unit = onBack,
    onNavigateProfile: () -> Unit = onOpenSettings,
    onCreateTicket: (subject: String, description: String) -> Unit,
    onLogAiSmsReply: (assistantName: String, phone: String, smsBody: String) -> Unit = { _, _, _ -> },
    aiViewModel: SupportAiViewModel = viewModel(),
) {
    val context = LocalContext.current

    var activeTab by rememberSaveable { mutableStateOf(SupportTab.SUPPORT_LINE) }
    var questionDraft by rememberSaveable { mutableStateOf("") }
    var lastSubmittedPrompt by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val aiUiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    val exchanges by aiViewModel.exchanges.collectAsStateWithLifecycle()
    val duplicateNotice by aiViewModel.duplicateBlockedNotice.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        aiViewModel.initializeAssistantIfNeeded()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    if (activeTab != SupportTab.SUPPORT_LINE || exchanges.isNotEmpty()) {
        BackHandler {
            if (activeTab != SupportTab.SUPPORT_LINE) {
                activeTab = SupportTab.SUPPORT_LINE
            } else {
                aiViewModel.clearConversation()
            }
        }
    }

    fun launchHelplineDialer() {
        val dialIntent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse(SUPPORT_HELPLINE_DIAL_URI),
        )
        runCatching { context.startActivity(dialIntent) }
    }

    fun submitSupportQuestion(queryText: String, attachedUri: Uri?, isRetry: Boolean = false) {
        val cleanText = queryText.trim()
        if (cleanText.isEmpty() && attachedUri == null) return

        lastSubmittedPrompt = cleanText
        val snapshot = SupportAssistantContextSnapshot(
            businessName = appData.profile.businessName.ifBlank { "হিসাবের খাতা" },
            ownerName = appData.profile.ownerName.ifBlank { "সম্মানিত ব্যবসায়ী" },
            ownerPhone = appData.profile.phone,
            customerCount = appData.summary.customerCount,
            totalPaona = appData.summary.totalPaona,
            totalDena = appData.summary.totalDena,
            todayIncome = appData.summary.todayIncome,
            todayExpense = appData.summary.todayExpense,
            smsBalance = appData.profile.smsBalance,
        )
        aiViewModel.askSupport(
            context = context,
            rawQuestion = cleanText,
            attachedImageUri = attachedUri,
            businessContext = snapshot,
            onCreateTicket = onCreateTicket,
            isRetry = isRetry,
        )
        questionDraft = ""
        selectedImageUri = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FCF9))
            .imePadding(),
    ) {
        // 1. Top Emerald Header matching Reference ('হিসাবি খাতা' / 'আপনার হিসাব, আমাদের দায়িত্ব')
        SupportReferenceTopHeader(
            onBackClick = onBack,
            onSettingsClick = onOpenSettings,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            // 2. Green Rounded Pill Banner ('Help Support' / 'যেকোনো সমস্যায় আমরা আছি আপনার পাশে')
            Surface(
                color = SupportHeaderGreenLight,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(SupportHeaderGreenDark, SupportHeaderGreenLight, SupportHeaderGreenDark)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = "Help Support",
                            tint = PureWhite,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Help Support",
                            color = PureWhite,
                            fontSize = 18.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "যেকোনো সমস্যায় আমরা আছি আপনার পাশে",
                        color = PureWhite.copy(alpha = 0.96f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // 3. Three Quick Action Cards (Support Line, Help Line, History)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SupportActionTileCard(
                    icon = Icons.Default.HeadsetMic,
                    title = "Support Line",
                    subtitle = "সাপোর্ট লাইনে\nযোগাযোগ করুন",
                    selected = activeTab == SupportTab.SUPPORT_LINE,
                    testTag = "support_tab_line",
                    onClick = { activeTab = SupportTab.SUPPORT_LINE },
                    modifier = Modifier.weight(1f),
                )
                SupportActionTileCard(
                    icon = Icons.Default.SupportAgent,
                    title = "Help Line",
                    subtitle = "হেল্প লাইনে\nযোগাযোগ করুন",
                    selected = activeTab == SupportTab.HELP_LINE,
                    testTag = "support_tab_helpline",
                    onClick = { activeTab = SupportTab.HELP_LINE },
                    modifier = Modifier.weight(1f),
                )
                SupportActionTileCard(
                    icon = Icons.Default.History,
                    title = "History",
                    subtitle = "পূর্বের চ্যাট দেখুন",
                    selected = activeTab == SupportTab.HISTORY,
                    testTag = "support_tab_history",
                    onClick = { activeTab = SupportTab.HISTORY },
                    modifier = Modifier.weight(1f),
                )
            }

            // 4. Body per Active Tab
            when (activeTab) {
                SupportTab.SUPPORT_LINE -> {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            if (exchanges.isEmpty()) {
                                SupportWelcomeHeroAndQuickQuestions(
                                    aiUiState = aiUiState,
                                    onRetryError = {
                                        aiViewModel.dismissError()
                                        if (lastSubmittedPrompt.isNotBlank()) {
                                            submitSupportQuestion(lastSubmittedPrompt, null, isRetry = true)
                                        }
                                    },
                                    onOpenHumanSupport = { activeTab = SupportTab.HELP_LINE },
                                    onQuickQuestionSelected = { pill ->
                                        submitSupportQuestion(pill.prompt, null)
                                    },
                                )
                            } else {
                                SupportTextConversationList(
                                    exchanges = exchanges,
                                    aiUiState = aiUiState,
                                    onRetryError = {
                                        aiViewModel.dismissError()
                                        if (lastSubmittedPrompt.isNotBlank()) {
                                            submitSupportQuestion(lastSubmittedPrompt, null, isRetry = true)
                                        }
                                    },
                                    onDialHelpline = { launchHelplineDialer() },
                                    onOpenHumanSupport = { activeTab = SupportTab.HELP_LINE },
                                    onResetToWelcome = { aiViewModel.clearConversation() },
                                )
                            }
                        }

                        if (!duplicateNotice.isNullOrBlank()) {
                            Surface(
                                color = WarningAmberBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .testTag("support_duplicate_notice"),
                            ) {
                                Text(
                                    text = duplicateNotice!!,
                                    color = InkPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                )
                            }
                        }

                        // 5. Bottom Input Bar ('আপনার প্রশ্ন লিখুন...' + Gallery + 'পাঠান')
                        SupportBottomTextInputBar(
                            questionDraft = questionDraft,
                            onQuestionChange = {
                                questionDraft = it
                                aiViewModel.dismissDuplicateNotice()
                            },
                            selectedImageUri = selectedImageUri,
                            onClearImage = { selectedImageUri = null },
                            isLoading = aiUiState is SupportAiUiState.Loading || aiUiState is SupportAiUiState.Streaming,
                            onPickImage = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onSendClick = {
                                submitSupportQuestion(questionDraft, selectedImageUri)
                            },
                        )
                    }
                }

                SupportTab.HELP_LINE -> {
                    SupportHelplineContactSection(
                        onDialHelpline = { launchHelplineDialer() },
                        modifier = Modifier.weight(1f),
                    )
                }

                SupportTab.HISTORY -> {
                    SupportHistoryTabContent(
                        tickets = tickets,
                        onAskNewQuestion = { activeTab = SupportTab.SUPPORT_LINE },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 6. Bottom 4-Tab Bar matching Reference ('হোম', 'হিসাব', 'রিপোর্ট', 'প্রোফাইল')
        SupportBottomNavigationBar(
            onSelectHome = onNavigateHome,
            onSelectLedger = onNavigateLedger,
            onSelectReports = onNavigateReports,
            onSelectProfile = onNavigateProfile,
        )
    }
}

@Composable
private fun SupportReferenceTopHeader(
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(SupportHeaderGreenDark, SupportHeaderGreenLight, SupportHeaderGreenDark)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("support_back_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "পিছনে যান",
                    tint = PureWhite,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Rounded white square logo icon matching reference
            Surface(
                color = PureWhite,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp),
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(7.dp)) {
                    val w = size.width
                    val h = size.height
                    // Green ledger book frame
                    drawRoundRect(
                        color = SupportHeaderGreenDark,
                        topLeft = Offset(w * 0.12f, h * 0.08f),
                        size = Size(w * 0.72f, h * 0.84f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                    // Chart bars
                    drawRect(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(w * 0.24f, h * 0.52f),
                        size = Size(w * 0.12f, h * 0.28f),
                    )
                    drawRect(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(w * 0.40f, h * 0.38f),
                        size = Size(w * 0.12f, h * 0.42f),
                    )
                    // Rising green trend arrow
                    val path = Path().apply {
                        moveTo(w * 0.22f, h * 0.42f)
                        lineTo(w * 0.44f, h * 0.26f)
                        lineTo(w * 0.62f, h * 0.18f)
                    }
                    drawPath(
                        path = path,
                        color = SupportHeaderGreenLight,
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                    )
                    // Mini calculator badge on bottom-right
                    drawRoundRect(
                        color = SupportHeaderGreenDark,
                        topLeft = Offset(w * 0.56f, h * 0.44f),
                        size = Size(w * 0.36f, h * 0.46f),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "হিসাবি খাতা",
                    color = PureWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "আপনার হিসাব, আমাদের দায়িত্ব",
                    color = PureWhite.copy(alpha = 0.95f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("support_settings_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "সেটিংস",
                    tint = PureWhite,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}

@Composable
private fun SupportActionTileCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = if (selected) PureWhite else SupportMintBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (selected) 1.8.dp else 1.dp,
            color = if (selected) SupportHeaderGreenDark else SupportMintBorder,
        ),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 96.dp)
            .testTag(testTag),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = SupportHeaderGreenDark,
                modifier = Modifier.size(26.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = SupportInkDark,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF334155),
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SupportWelcomeHeroAndQuickQuestions(
    aiUiState: SupportAiUiState,
    onRetryError: () -> Unit,
    onOpenHumanSupport: () -> Unit,
    onQuickQuestionSelected: (QuickQuestionPill) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (aiUiState is SupportAiUiState.Error) {
            SupportErrorBannerCard(
                errorMessage = aiUiState.message,
                onRetry = onRetryError,
                onOpenHumanSupport = onOpenHumanSupport,
            )
        }

        // Illustrated Green Welcome Hero Card matching reference
        Surface(
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("support_welcome_banner"),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFA7F380),
                                Color(0xFF47CE58),
                                Color(0xFF17A42D),
                            )
                        )
                    )
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(width = 112.dp, height = 136.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_support_agent),
                            contentDescription = "হিসাবি খাতা সাপোর্ট প্রতিনিধি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                        )
                        Surface(
                            color = SupportHeaderGreenDark,
                            shape = CircleShape,
                            border = BorderStroke(2.dp, PureWhite),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(30.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Surface(
                            color = PureWhite.copy(alpha = 0.96f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "আসসালামু আলাইকুম!",
                                    color = SupportInkDark,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "হিসাবি খাতার পক্ষ থেকে আপনাকে আন্তরিক স্বাগতম। ❤️",
                                    color = Color(0xFF1E293B),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Surface(
                            color = PureWhite,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SupportMintBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "বলুন, কীভাবে আপনাকে সাহায্য করতে পারি?",
                                color = SupportInkDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        }

        // '⚡ দ্রুত প্রশ্নসমূহ' Badge Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = SupportHeaderGreenDark,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomEnd = 14.dp,
                    bottomStart = 4.dp,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "দ্রুত প্রশ্নসমূহ",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // 2x3 Pill Grid matching reference screenshot
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            QuickQuestionPills.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { pill ->
                        QuickQuestionPillButton(
                            pill = pill,
                            onClick = { onQuickQuestionSelected(pill) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickQuestionPillButton(
    pill: QuickQuestionPill,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = SupportMintBg,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, SupportMintBorder),
        modifier = modifier
            .defaultMinSize(minHeight = 50.dp)
            .testTag("quick_question_${pill.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = pill.icon,
                contentDescription = pill.label,
                tint = SupportHeaderGreenDark,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = pill.label,
                color = SupportInkDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SupportTextConversationList(
    exchanges: List<SupportChatExchange>,
    aiUiState: SupportAiUiState,
    onRetryError: () -> Unit,
    onDialHelpline: () -> Unit,
    onOpenHumanSupport: () -> Unit,
    onResetToWelcome: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(exchanges.size) {
        if (exchanges.isNotEmpty()) {
            listState.animateScrollToItem(exchanges.size * 2)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("support_conversation_list"),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "conversation_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = SupportMintBg,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, SupportMintBorder),
                ) {
                    Text(
                        text = "লিখিত সাপোর্ট চ্যাট",
                        color = SupportHeaderGreenDark,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }

                TextButton(
                    onClick = onResetToWelcome,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "← দ্রুত প্রশ্নসমূহ",
                        color = SupportHeaderGreenDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        exchanges.forEach { exchange ->
            item(key = "user_${exchange.id}") {
                // Customer Message Bubble (Right-aligned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Surface(
                        color = SupportHeaderGreenDark,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 4.dp,
                        ),
                        modifier = Modifier.widthIn(max = 290.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (exchange.imageUri != null) {
                                AsyncImage(
                                    model = exchange.imageUri,
                                    contentDescription = "সংযুক্ত ছবি",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Text(
                                text = exchange.userQuestion,
                                color = PureWhite,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = exchange.timeLabel,
                                color = PureWhite.copy(alpha = 0.78f),
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End),
                            )
                        }
                    }
                }
            }

            item(key = "reply_${exchange.id}") {
                // Representative Text Reply Card
                SupportTextReplyCard(
                    exchange = exchange,
                    onDialHelpline = onDialHelpline,
                )
            }
        }

        if (aiUiState is SupportAiUiState.Error) {
            item(key = "support_error_banner") {
                SupportErrorBannerCard(
                    errorMessage = aiUiState.message,
                    onRetry = onRetryError,
                    onOpenHumanSupport = onOpenHumanSupport,
                )
            }
        }
    }
}

@Composable
private fun SupportBottomTextInputBar(
    questionDraft: String,
    onQuestionChange: (String) -> Unit,
    selectedImageUri: Uri?,
    onClearImage: () -> Unit,
    isLoading: Boolean,
    onPickImage: () -> Unit,
    onSendClick: () -> Unit,
) {
    val canSend = (questionDraft.isNotBlank() || selectedImageUri != null) && !isLoading

    Surface(
        color = Color(0xFFF9FCF9),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (selectedImageUri != null) {
                Row(
                    modifier = Modifier.padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "নির্বাচিত ছবি",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, SupportMintBorder, RoundedCornerShape(10.dp)),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ছবি সংযুক্ত হয়েছে",
                        color = SupportHeaderGreenDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onClearImage, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "ছবি সরান", tint = InkSecondary)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = PureWhite,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.2.dp, SupportMintBorder),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = onPickImage,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("support_attach_image_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = "ছবি সংযুক্ত করুন",
                                tint = SupportHeaderGreenDark,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(SupportMintBorder),
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (questionDraft.isEmpty()) {
                                Text(
                                    text = "আপনার প্রশ্ন লিখুন...",
                                    color = InkMuted,
                                    fontSize = 14.sp,
                                )
                            }
                            BasicTextField(
                                value = questionDraft,
                                onValueChange = onQuestionChange,
                                singleLine = true,
                                cursorBrush = SolidColor(SupportHeaderGreenDark),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = InkPrimary,
                                    fontSize = 14.5.sp,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("support_question_input"),
                            )
                        }
                    }
                }

                Button(
                    onClick = onSendClick,
                    enabled = canSend,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SupportHeaderGreenLight,
                        contentColor = PureWhite,
                        disabledContainerColor = SupportHeaderGreenLight.copy(alpha = 0.45f),
                        disabledContentColor = PureWhite.copy(alpha = 0.85f),
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("support_send_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "পাঠান",
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "পাঠান",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportErrorBannerCard(
    errorMessage: String,
    onRetry: () -> Unit,
    onOpenHumanSupport: () -> Unit,
) {
    Surface(
        color = Color(0xFFFEF2F2),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("support_ai_error_card"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "ত্রুটি",
                    tint = Color(0xFFB91C1C),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সাময়িক সংযোগ সমস্যা",
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                )
            }
            Text(
                text = errorMessage,
                color = InkPrimary,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onRetry,
                    border = BorderStroke(1.dp, Color(0xFFDC2626)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB91C1C)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                ) {
                    Text("আবার চেষ্টা করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpenHumanSupport,
                    colors = ButtonDefaults.buttonColors(containerColor = SupportHeaderGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                ) {
                    Text("হেল্পলাইন দেখুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SupportHelplineContactSection(
    onDialHelpline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = SupportMintBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SupportMintBorder),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("support_helpline_card"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SupportHeaderGreenDark),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "হেল্পলাইন নম্বর",
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "হেল্পলাইন নম্বর",
                        color = InkSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = SUPPORT_HELPLINE_NUMBER,
                        color = SupportInkDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.6.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("helpline_number_text"),
                    )
                }

                Button(
                    onClick = onDialHelpline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SupportHeaderGreenDark,
                        contentColor = PureWhite,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("helpline_call_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "হেল্পলাইনে যোগাযোগ করুন",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportHistoryTabContent(
    tickets: List<SupportTicket>,
    onAskNewQuestion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SoftCanvas)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "পূর্বের সাপোর্ট হিস্টরি (${tickets.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InkPrimary,
            )
            TextButton(onClick = onAskNewQuestion) {
                Text("নতুন প্রশ্ন করুন", color = SupportHeaderGreenDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (tickets.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Default.History,
                title = "কোনো পূর্বের চ্যাট বা টিকেট নেই",
                subtitle = "সাপোর্ট লাইন বা হেল্প লাইন থেকে লিখিত প্রশ্ন বা টিকেট পাঠালে এখানে দেখা যাবে।",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(tickets, key = { it.id }) { ticket ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = ticket.subject,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                val (statusText, statusColor, statusBg) = when (ticket.status) {
                                    "answered" -> Triple("উত্তর দেওয়া হয়েছে", PaonaGreen, HisabGreenSoft)
                                    "closed" -> Triple("সমাধান হয়েছে", InkSecondary, SoftCanvas)
                                    else -> Triple("মানব টিমে প্রেরিত", WarningAmber, WarningAmberBg)
                                }
                                Surface(color = statusBg, shape = CircleShape) {
                                    Text(
                                        text = statusText,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    )
                                }
                            }
                            if (ticket.description.isNotBlank() && ticket.description != ticket.subject) {
                                Text(
                                    text = ticket.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (ticket.reply.isNotBlank()) {
                                Surface(
                                    color = HisabGreenTint,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = "উত্তর: ${ticket.reply}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = HisabGreenDeep,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                            }
                            val millis = ticket.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                            Text(
                                text = Formatters.formatShortDate(millis),
                                style = MaterialTheme.typography.labelSmall,
                                color = InkMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportBottomNavigationBar(
    onSelectHome: () -> Unit,
    onSelectLedger: () -> Unit,
    onSelectReports: () -> Unit,
    onSelectProfile: () -> Unit,
) {
    Surface(
        color = PureWhite,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Column {
            HorizontalDivider(color = CardBorder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SupportBottomNavItem(
                    label = "হোম",
                    icon = Icons.Default.Home,
                    selected = true,
                    onClick = onSelectHome,
                    modifier = Modifier.weight(1f).testTag("support_nav_home"),
                )
                SupportBottomNavItem(
                    label = "হিসাব",
                    icon = Icons.Default.BarChart,
                    selected = false,
                    onClick = onSelectLedger,
                    modifier = Modifier.weight(1f).testTag("support_nav_ledger"),
                )
                SupportBottomNavItem(
                    label = "রিপোর্ট",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    selected = false,
                    onClick = onSelectReports,
                    modifier = Modifier.weight(1f).testTag("support_nav_reports"),
                )
                SupportBottomNavItem(
                    label = "প্রোফাইল",
                    icon = Icons.Default.Person,
                    selected = false,
                    onClick = onSelectProfile,
                    modifier = Modifier.weight(1f).testTag("support_nav_profile"),
                )
            }
        }
    }
}

@Composable
private fun SupportBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) SupportHeaderGreenDark else Color(0xFF475569)
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick)
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(23.dp),
        )
        Text(
            text = label,
            color = tint,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(if (selected) SupportHeaderGreenDark else Color.Transparent),
        )
    }
}
