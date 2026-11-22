package com.example.auth

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import com.example.util.PaymentGatewayConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object OtpErrorMessages {
    const val SEND_FAILED = "OTP পাঠানো যায়নি। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।"
    const val INVALID_PHONE = "ভুল মোবাইল নম্বর। সঠিক বাংলাদেশি নম্বর দিন (যেমন: 017XXXXXXXX)।"
    const val INVALID_OTP = "OTP ভুল হয়েছে। সঠিক কোডটি আবার লিখুন।"
    const val OTP_EXPIRED = "OTP-এর সময় শেষ। অনুগ্রহ করে আবার OTP পাঠান।"
    const val SMS_DELAYED = "SMS আসতে দেরি হচ্ছে। নেটওয়ার্ক পরীক্ষা করুন অথবা ৩০ সেকেন্ড পর আবার চেষ্টা করুন।"
    const val NO_INTERNET = "ইন্টারনেট সংযোগ নেই। ইন্টারনেট চালু করে আবার চেষ্টা করুন।"
    const val GOOGLE_SIGNIN_FAILED = "Google Sign-In ব্যর্থ হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।"
    const val TOO_MANY_REQUESTS = "অতিরিক্ত OTP পাঠানোর চেষ্টা হয়েছে। কিছুক্ষণ পর আবার চেষ্টা করুন।"
    const val OTP_SERVICE_UNAVAILABLE =
        "দুঃখিত, এই মুহূর্তে এই নম্বরে OTP পাঠানো যাচ্ছে না। কিছুক্ষণ পর আবার চেষ্টা করুন অথবা Google দিয়ে লগইন করুন।"
}

data class NormalizedBdPhone(
    val localTenDigits: String, // e.g., "1712345678"
    val e164: String,           // e.g., "+8801712345678"
    val displayFormatted: String,
)

object BangladeshPhoneValidator {
    private val BD_LOCAL_10_REGEX = Regex("""^1[3-9]\d{8}$""")

    fun normalize(rawInput: String): NormalizedBdPhone? {
        val ascii = normalizeDigitsToAscii(rawInput).filter { it.isDigit() }
        val local10 = when {
            ascii.length == 13 && ascii.startsWith("880") -> ascii.substring(3)
            ascii.length == 11 && ascii.startsWith("01") -> ascii.substring(1)
            ascii.length == 10 && ascii.startsWith("1") -> ascii
            else -> return null
        }
        if (!BD_LOCAL_10_REGEX.matches(local10)) return null
        return NormalizedBdPhone(
            localTenDigits = local10,
            e164 = "+880$local10",
            displayFormatted = "+880 $local10",
        )
    }

    /**
     * Normalizes partial text entry inside the +880 mobile input box so users can paste
     * "01712345678", "+8801712345678", "8801712345678", or Bengali digits "০১৭১২৩৪৫৬৭৮".
     */
    fun sanitizeInputToTenDigits(rawInput: String): String {
        val digits = normalizeDigitsToAscii(rawInput).filter { it.isDigit() }
        return when {
            digits.startsWith("8801") -> digits.removePrefix("880").take(10)
            digits.startsWith("01") -> digits.removePrefix("0").take(10)
            else -> digits.take(10)
        }
    }
}

enum class OtpBackendProviderMode {
    CUSTOM_SERVER_6_DIGIT,
    FIREBASE_PHONE_6_DIGIT,
}

data class OtpChallengeSession(
    val challengeId: String,
    val phoneE164: String,
    val providerMode: OtpBackendProviderMode,
    val otpLength: Int = 6,
    val expiresInSec: Int = 60,
    val resendAfterSec: Int = 30,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
)

sealed interface OtpSendOutcome {
    data class CodeSent(val session: OtpChallengeSession) : OtpSendOutcome
    data class AutoVerified(val phoneE164: String) : OtpSendOutcome
    data class Error(val message: String) : OtpSendOutcome
}

sealed interface OtpVerifyOutcome {
    data class Verified(
        val phoneE164: String,
        val hasFirebaseUser: Boolean,
    ) : OtpVerifyOutcome
    data class Error(val message: String) : OtpVerifyOutcome
}

interface PhoneOtpAuthGateway {
    fun isNetworkConnected(context: Context): Boolean
    suspend fun sendOtp(
        activity: Activity?,
        context: Context,
        phoneE164: String,
        appHashes: List<String>,
    ): OtpSendOutcome

    suspend fun resendOtp(
        activity: Activity?,
        context: Context,
        previousSession: OtpChallengeSession,
        appHashes: List<String>,
    ): OtpSendOutcome

    suspend fun verifyOtp(
        context: Context,
        session: OtpChallengeSession,
        otpCode: String,
    ): OtpVerifyOutcome
}

object OtpEndpointResolver {
    fun resolveSecureBackendBaseUrl(context: Context): String? {
        val envUrl = BuildConfig.OTP_BACKEND_BASE_URL.trim().trim('"').trimEnd('/')
        val configuredUrl = envUrl.ifBlank {
            PaymentGatewayConfig.getBackendServerUrl(context).trim().trim('"').trimEnd('/')
        }
        if (configuredUrl.isBlank()) return null

        val isHttps = configuredUrl.startsWith("https://", ignoreCase = true)
        val isLocalDev = configuredUrl.startsWith("http://10.0.2.2", ignoreCase = true) ||
            configuredUrl.startsWith("http://127.0.0.1", ignoreCase = true) ||
            configuredUrl.startsWith("http://localhost", ignoreCase = true)

        return if (isHttps || isLocalDev) configuredUrl else null
    }
}

class RealPhoneOtpAuthGateway(
    private val timeProviderMs: () -> Long = { System.currentTimeMillis() },
) : PhoneOtpAuthGateway {

    // Tracks recent send timestamps per phone in-process to prevent spam clicks beyond resend cooldown
    private val clientSendTimestamps = mutableMapOf<String, MutableList<Long>>()

    override fun isNetworkConnected(context: Context): Boolean {
        return isDeviceInternetAvailable(context)
    }

    private fun checkClientSendRateLimit(phoneE164: String): String? {
        val now = timeProviderMs()
        val windowStart = now - 15 * 60 * 1000L
        val history = clientSendTimestamps.getOrPut(phoneE164) { mutableListOf() }
        history.removeAll { it < windowStart }
        if (history.size >= 5) {
            return OtpErrorMessages.TOO_MANY_REQUESTS
        }
        val last = history.lastOrNull()
        if (last != null && now - last < 30_000L) {
            val waitSec = ((30_000L - (now - last) + 999L) / 1000L).coerceIn(1L, 30L)
            return "অনুগ্রহ করে $waitSec সেকেন্ড পর আবার OTP পাঠান।"
        }
        history.add(now)
        return null
    }

    override suspend fun sendOtp(
        activity: Activity?,
        context: Context,
        phoneE164: String,
        appHashes: List<String>,
    ): OtpSendOutcome {
        if (!isNetworkConnected(context)) {
            return OtpSendOutcome.Error(OtpErrorMessages.NO_INTERNET)
        }
        checkClientSendRateLimit(phoneE164)?.let { msg ->
            return OtpSendOutcome.Error(msg)
        }

        val backendUrl = OtpEndpointResolver.resolveSecureBackendBaseUrl(context)
        if (backendUrl != null) {
            return sendViaCustomBackend(
                endpointUrl = "$backendUrl/api/auth/otp/send",
                phoneE164 = phoneE164,
                challengeId = null,
                appHashes = appHashes,
            )
        }

        return sendViaFirebasePhoneAuth(
            context = context,
            activity = activity,
            phoneE164 = phoneE164,
            resendToken = null,
        )
    }

    override suspend fun resendOtp(
        activity: Activity?,
        context: Context,
        previousSession: OtpChallengeSession,
        appHashes: List<String>,
    ): OtpSendOutcome {
        if (!isNetworkConnected(context)) {
            return OtpSendOutcome.Error(OtpErrorMessages.NO_INTERNET)
        }
        checkClientSendRateLimit(previousSession.phoneE164)?.let { msg ->
            return OtpSendOutcome.Error(msg)
        }

        val backendUrl = OtpEndpointResolver.resolveSecureBackendBaseUrl(context)
        if (backendUrl != null && previousSession.providerMode == OtpBackendProviderMode.CUSTOM_SERVER_6_DIGIT) {
            return sendViaCustomBackend(
                endpointUrl = "$backendUrl/api/auth/otp/resend",
                phoneE164 = previousSession.phoneE164,
                challengeId = previousSession.challengeId,
                appHashes = appHashes,
            )
        }

        return sendViaFirebasePhoneAuth(
            context = context,
            activity = activity,
            phoneE164 = previousSession.phoneE164,
            resendToken = previousSession.resendToken,
        )
    }

    override suspend fun verifyOtp(
        context: Context,
        session: OtpChallengeSession,
        otpCode: String,
    ): OtpVerifyOutcome {
        if (!isNetworkConnected(context)) {
            return OtpVerifyOutcome.Error(OtpErrorMessages.NO_INTERNET)
        }
        val elapsedSec = (timeProviderMs() - session.createdAtMillis) / 1000L
        if (elapsedSec >= session.expiresInSec) {
            return OtpVerifyOutcome.Error(OtpErrorMessages.OTP_EXPIRED)
        }

        val cleanedCode = normalizeDigitsToAscii(otpCode).filter { it.isDigit() }
        if (cleanedCode.length != session.otpLength) {
            return OtpVerifyOutcome.Error(OtpErrorMessages.INVALID_OTP)
        }

        return when (session.providerMode) {
            OtpBackendProviderMode.CUSTOM_SERVER_6_DIGIT -> {
                val backendUrl = OtpEndpointResolver.resolveSecureBackendBaseUrl(context)
                    ?: return OtpVerifyOutcome.Error(OtpErrorMessages.SEND_FAILED)
                verifyViaCustomBackend(
                    endpointUrl = "$backendUrl/api/auth/otp/verify",
                    challengeId = session.challengeId,
                    phoneE164 = session.phoneE164,
                    otpCode = cleanedCode,
                )
            }
            OtpBackendProviderMode.FIREBASE_PHONE_6_DIGIT -> {
                verifyViaFirebaseCredential(session, cleanedCode)
            }
        }
    }

    private suspend fun sendViaCustomBackend(
        endpointUrl: String,
        phoneE164: String,
        challengeId: String?,
        appHashes: List<String>,
    ): OtpSendOutcome = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            val payload = JSONObject().apply {
                put("phone", phoneE164)
                put("otpLength", 6)
                if (!challengeId.isNullOrBlank()) put("challengeId", challengeId)
                if (appHashes.isNotEmpty()) put("appHash", appHashes.first())
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            conn.disconnect()

            val json = runCatching { JSONObject(body) }.getOrNull()
            if (code in 200..299) {
                val serverChallengeId = json?.optString("challengeId").orEmpty()
                if (serverChallengeId.isBlank()) {
                    return@withContext OtpSendOutcome.Error(OtpErrorMessages.SEND_FAILED)
                }
                OtpSendOutcome.CodeSent(
                    OtpChallengeSession(
                        challengeId = serverChallengeId,
                        phoneE164 = phoneE164,
                        providerMode = OtpBackendProviderMode.CUSTOM_SERVER_6_DIGIT,
                        otpLength = json?.optInt("otpLength", 6)?.takeIf { it == 4 || it == 6 } ?: 6,
                        expiresInSec = json?.optInt("expiresInSec", 60)?.coerceIn(30, 300) ?: 60,
                        resendAfterSec = json?.optInt("resendAfterSec", 30)?.coerceIn(15, 60) ?: 30,
                        createdAtMillis = timeProviderMs(),
                    )
                )
            } else {
                val errCode = json?.optString("code").orEmpty()
                val msg = when {
                    code == 429 || errCode == "RATE_LIMITED" || errCode == "RESEND_COOLDOWN" ->
                        OtpErrorMessages.TOO_MANY_REQUESTS
                    errCode == "INVALID_PHONE" || code == 400 -> OtpErrorMessages.INVALID_PHONE
                    else -> OtpErrorMessages.SEND_FAILED
                }
                OtpSendOutcome.Error(msg)
            }
        }.getOrElse { err ->
            OtpSendOutcome.Error(mapNetworkOrServerError(err, defaultMessage = OtpErrorMessages.SEND_FAILED))
        }
    }

    private suspend fun verifyViaCustomBackend(
        endpointUrl: String,
        challengeId: String,
        phoneE164: String,
        otpCode: String,
    ): OtpVerifyOutcome = withContext(Dispatchers.IO) {
        val httpResult = runCatching {
            val conn = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
            }

            val payload = JSONObject().apply {
                put("challengeId", challengeId)
                put("phone", phoneE164)
                put("code", otpCode)
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            conn.disconnect()

            val json = runCatching { JSONObject(body) }.getOrNull()
            Triple(code, json, body)
        }.getOrElse { err ->
            return@withContext OtpVerifyOutcome.Error(
                mapNetworkOrServerError(err, defaultMessage = OtpErrorMessages.INVALID_OTP)
            )
        }

        val (statusCode, json, _) = httpResult
        if (statusCode !in 200..299) {
            val errCode = json?.optString("code").orEmpty()
            val mapped = when {
                statusCode == 410 || errCode == "OTP_EXPIRED" -> OtpErrorMessages.OTP_EXPIRED
                statusCode == 429 || errCode == "RATE_LIMITED" -> OtpErrorMessages.TOO_MANY_REQUESTS
                else -> OtpErrorMessages.INVALID_OTP
            }
            return@withContext OtpVerifyOutcome.Error(mapped)
        }

        val verified = json?.optBoolean("verified", false) == true
        if (!verified) {
            return@withContext OtpVerifyOutcome.Error(OtpErrorMessages.INVALID_OTP)
        }

        val customToken = json?.optString("firebaseCustomToken").orEmpty()
        var hasFirebaseUser = Firebase.auth.currentUser != null
        if (customToken.isNotBlank()) {
            val signInResult = runCatching {
                Firebase.auth.signInWithCustomToken(customToken).await()
            }
            if (signInResult.isSuccess) {
                hasFirebaseUser = true
            }
        }
        OtpVerifyOutcome.Verified(phoneE164 = phoneE164, hasFirebaseUser = hasFirebaseUser)
    }

    private suspend fun sendViaFirebasePhoneAuth(
        context: Context,
        activity: Activity?,
        phoneE164: String,
        resendToken: PhoneAuthProvider.ForceResendingToken?,
    ): OtpSendOutcome {
        if (activity == null) {
            return OtpSendOutcome.Error(OtpErrorMessages.SEND_FAILED)
        }
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(context)
        }
        runCatching { Firebase.auth.setLanguageCode("bn") }

        return suspendCancellableCoroutine { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Firebase.auth.signInWithCredential(credential)
                        .addOnSuccessListener {
                            if (cont.isActive) cont.resume(OtpSendOutcome.AutoVerified(phoneE164))
                        }
                        .addOnFailureListener { ex ->
                            if (cont.isActive) {
                                cont.resume(OtpSendOutcome.Error(mapFirebasePhoneException(ex)))
                            }
                        }
                }

                override fun onVerificationFailed(ex: FirebaseException) {
                    android.util.Log.e("PhoneOtpAuth", "Firebase PhoneAuth failed: ${ex.message}", ex)
                    if (cont.isActive) {
                        cont.resume(OtpSendOutcome.Error(mapFirebasePhoneException(ex)))
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    if (cont.isActive) {
                        cont.resume(
                            OtpSendOutcome.CodeSent(
                                OtpChallengeSession(
                                    challengeId = verificationId,
                                    phoneE164 = phoneE164,
                                    providerMode = OtpBackendProviderMode.FIREBASE_PHONE_6_DIGIT,
                                    otpLength = 6,
                                    expiresInSec = 60,
                                    resendAfterSec = 30,
                                    createdAtMillis = timeProviderMs(),
                                    resendToken = token,
                                )
                            )
                        )
                    }
                }
            }

            val builder = PhoneAuthOptions.newBuilder(Firebase.auth)
                .setPhoneNumber(phoneE164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
            if (resendToken != null) {
                builder.setForceResendingToken(resendToken)
            }
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    private suspend fun verifyViaFirebaseCredential(
        session: OtpChallengeSession,
        otpCode: String,
    ): OtpVerifyOutcome {
        return try {
            val credential = PhoneAuthProvider.getCredential(session.challengeId, otpCode)
            Firebase.auth.signInWithCredential(credential).await()
            OtpVerifyOutcome.Verified(phoneE164 = session.phoneE164, hasFirebaseUser = true)
        } catch (ex: Exception) {
            OtpVerifyOutcome.Error(mapFirebasePhoneException(ex))
        }
    }

    private fun mapFirebasePhoneException(ex: Exception): String {
        val code = (ex as? FirebaseAuthException)?.errorCode.orEmpty()
        val msg = ex.message.orEmpty()
        android.util.Log.e("PhoneOtpAuth", "Phone auth error code=$code message=$msg", ex)
        return when {
            ex is FirebaseNetworkException -> OtpErrorMessages.NO_INTERNET
            ex is FirebaseTooManyRequestsException || code == "ERROR_TOO_MANY_REQUESTS" || code == "ERROR_QUOTA_EXCEEDED" ->
                OtpErrorMessages.TOO_MANY_REQUESTS
            code == "ERROR_INVALID_VERIFICATION_CODE" -> OtpErrorMessages.INVALID_OTP
            code == "ERROR_SESSION_EXPIRED" || code == "ERROR_CODE_EXPIRED" -> OtpErrorMessages.OTP_EXPIRED
            ex is FirebaseAuthInvalidCredentialsException && code == "ERROR_INVALID_PHONE_NUMBER" ->
                OtpErrorMessages.INVALID_PHONE
            code == "ERROR_OPERATION_NOT_ALLOWED" ||
                code == "ERROR_UNSUPPORTED_TENANT_OPERATION" ||
                code == "ERROR_APP_NOT_AUTHORIZED" ||
                code == "ERROR_MISSING_CLIENT_IDENTIFIER" ||
                msg.contains("SMS unable to be sent", ignoreCase = true) ||
                msg.contains("region", ignoreCase = true) ||
                msg.contains("Play Integrity", ignoreCase = true) ||
                msg.contains("reCAPTCHA", ignoreCase = true) ->
                OtpErrorMessages.OTP_SERVICE_UNAVAILABLE
            else -> OtpErrorMessages.SEND_FAILED
        }
    }

    private fun mapNetworkOrServerError(err: Throwable, defaultMessage: String): String {
        return when (err) {
            is UnknownHostException, is java.net.ConnectException -> OtpErrorMessages.NO_INTERNET
            is SocketTimeoutException -> OtpErrorMessages.SMS_DELAYED
            else -> defaultMessage
        }
    }
}

fun isDeviceInternetAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
