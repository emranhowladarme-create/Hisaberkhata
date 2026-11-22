package com.example.auth

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Arrays

enum class SmsAutofillMode {
    SMS_RETRIEVER,
    USER_CONSENT,
}

enum class SmsAutofillStatus {
    IDLE,
    LISTENING,
    AUTO_FILLED,
    TIMED_OUT,
    UNAVAILABLE,
}

private const val TAG = "SmsOtpAutofill"
private const val HASH_TYPE = "SHA-256"
private const val NUM_HASHED_BYTES = 9
private const val NUM_BASE64_CHAR = 11

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Normalizes Bengali digits (০-৯) to ASCII digits (0-9).
 */
fun normalizeDigitsToAscii(input: String): String {
    if (input.isEmpty()) return input
    val sb = StringBuilder(input.length)
    for (ch in input) {
        if (ch in '\u09E6'..'\u09EF') {
            sb.append(('0'.code + (ch.code - '\u09E6'.code)).toChar())
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

/**
 * Extracts a 4-digit OTP code from an incoming SMS message body.
 * Prioritizes digits adjacent to OTP keywords before falling back to standalone 4-digit sequences.
 */
fun extractFourDigitOtp(smsText: String): String? = extractOtpCode(smsText, expectedLength = 4)

/**
 * Extracts a numeric OTP code of [expectedLength] (4 or 6) from an incoming SMS body.
 * Prioritizes digits adjacent to OTP keywords before falling back to standalone sequences.
 */
fun extractOtpCode(smsText: String, expectedLength: Int = 6): String? {
    val normalized = normalizeDigitsToAscii(smsText)
    val len = expectedLength.coerceIn(4, 6)

    val keywordPattern = Regex(
        """(?i)(?:otp|code|pin|verification|verify|passcode|কোড|ওটিপি|পিন)\s*(?:is|হলো|হল|:|-|=)?\s*(\d{$len})(?!\d)"""
    )
    keywordPattern.find(normalized)?.groupValues?.getOrNull(1)?.let { return it }

    val prefixKeywordPattern = Regex(
        """(?i)(?<!\d)(\d{$len})\s*(?:is your|আপনার)\s*(?:otp|code|pin|verification|কোড|ওটিপি)"""
    )
    prefixKeywordPattern.find(normalized)?.groupValues?.getOrNull(1)?.let { return it }

    // Exclude clock times such as 01:16 when choosing fallback candidate
    val standaloneCandidates = Regex("""(?<![\d:])\d{$len}(?![\d:])""")
        .findAll(normalized)
        .map { it.value }
        .toList()
    if (standaloneCandidates.isNotEmpty()) {
        return standaloneCandidates.first()
    }

    return Regex("""(?<!\d)\d{$len}(?!\d)""").find(normalized)?.value
}

/**
 * Computes the official 11-character Google Play Services SMS Retriever app signature hash(es).
 */
fun computeSmsRetrieverAppHashes(context: Context): List<String> {
    val hashes = mutableListOf<String>()
    return runCatching {
        val packageName = context.packageName
        val packageManager = context.packageManager
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signingInfo = info.signingInfo
            if (signingInfo == null) emptyArray()
            else if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners
            else signingInfo.signingCertificateHistory
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
                ?: emptyArray()
        }

        for (signature in signatures) {
            val appInfo = "$packageName ${signature.toCharsString()}"
            val md = MessageDigest.getInstance(HASH_TYPE)
            md.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            val hashSignature = Arrays.copyOfRange(md.digest(), 0, NUM_HASHED_BYTES)
            val base64Hash = Base64.encodeToString(
                hashSignature,
                Base64.NO_PADDING or Base64.NO_WRAP
            ).substring(0, NUM_BASE64_CHAR)
            if (base64Hash !in hashes) {
                hashes.add(base64Hash)
            }
        }
        hashes
    }.onFailure { err ->
        Log.w(TAG, "Failed to compute SMS Retriever app hash: ${err.message}")
    }.getOrDefault(hashes)
}

/**
 * Zero-permission SMS OTP Auto-fill listener powered by Google Play Services
 * SMS Retriever API (or optional SMS User Consent API).
 *
 * Registers the BroadcastReceiver BEFORE starting the SmsRetrieverClient task,
 * guards with SmsRetriever.SEND_PERMISSION + RECEIVER_EXPORTED, and runs a
 * single non-conflicting listener per session.
 */
@Composable
fun SmsOtpAutofillEffect(
    enabled: Boolean,
    mode: SmsAutofillMode,
    sessionKey: Int,
    expectedOtpLength: Int = 6,
    onStatusChanged: (status: SmsAutofillStatus, detail: String?) -> Unit = { _, _ -> },
    onOtpExtracted: (String) -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val currentOnOtpExtracted by rememberUpdatedState(onOtpExtracted)
    val currentOnStatusChanged by rememberUpdatedState(onStatusChanged)

    val consentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE).orEmpty()
            val code = extractOtpCode(message, expectedOtpLength)
            if (code != null) {
                currentOnStatusChanged(SmsAutofillStatus.AUTO_FILLED, null)
                currentOnOtpExtracted(code)
            } else {
                currentOnStatusChanged(
                    SmsAutofillStatus.UNAVAILABLE,
                    "SMS-এ $expectedOtpLength ডিজিটের OTP পাওয়া যায়নি; হাতে কোড লিখুন।"
                )
            }
        }
    }

    DisposableEffect(enabled, mode, sessionKey) {
        if (!enabled) {
            currentOnStatusChanged(SmsAutofillStatus.IDLE, null)
            return@DisposableEffect onDispose {}
        }

        var disposed = false
        var otpCaptured = false

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (disposed || intent?.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
                val extras = intent.extras ?: return
                val status: Status? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    extras.getParcelable(SmsRetriever.EXTRA_STATUS)
                }

                when (status?.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val smsMessage = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE)
                        if (!smsMessage.isNullOrBlank()) {
                            val code = extractOtpCode(smsMessage, expectedOtpLength)
                            if (code != null) {
                                otpCaptured = true
                                currentOnStatusChanged(SmsAutofillStatus.AUTO_FILLED, null)
                                currentOnOtpExtracted(code)
                                return
                            }
                        }

                        if (!otpCaptured) {
                            val consentIntent: Intent? =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    extras.getParcelable(
                                        SmsRetriever.EXTRA_CONSENT_INTENT,
                                        Intent::class.java
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT)
                                }
                            if (consentIntent != null) {
                                runCatching { consentLauncher.launch(consentIntent) }
                                    .onFailure { err ->
                                        Log.w(TAG, "Consent launch failed: ${err.message}")
                                        currentOnStatusChanged(
                                            SmsAutofillStatus.UNAVAILABLE,
                                            err.localizedMessage
                                        )
                                    }
                            }
                        }
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        Log.d(TAG, "SMS Retriever timed out")
                        if (!otpCaptured && !disposed) {
                            currentOnStatusChanged(
                                SmsAutofillStatus.TIMED_OUT,
                                "SMS অটো-ফিলের সময় শেষ হয়েছে; হাতে কোড লিখুন বা আবার OTP পাঠান।"
                            )
                        }
                    }

                    else -> {
                        Log.w(TAG, "SMS Retriever status code: ${status?.statusCode}")
                    }
                }
            }
        }

        // Register BroadcastReceiver BEFORE starting SmsRetrieverClient so no broadcast is missed
        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        val registered = runCatching {
            ContextCompat.registerReceiver(
                appContext,
                receiver,
                filter,
                SmsRetriever.SEND_PERMISSION,
                null,
                ContextCompat.RECEIVER_EXPORTED,
            )
            true
        }.onFailure { err ->
            Log.e(TAG, "Failed to register SMS receiver: ${err.message}", err)
            currentOnStatusChanged(SmsAutofillStatus.UNAVAILABLE, err.localizedMessage)
        }.getOrDefault(false)

        if (registered) {
            val clientContext = context.findActivity() ?: appContext
            val client = SmsRetriever.getClient(clientContext)
            val task = when (mode) {
                SmsAutofillMode.SMS_RETRIEVER -> client.startSmsRetriever()
                SmsAutofillMode.USER_CONSENT -> client.startSmsUserConsent(null)
            }
            task.addOnSuccessListener {
                if (!disposed && !otpCaptured) {
                    currentOnStatusChanged(SmsAutofillStatus.LISTENING, null)
                }
            }.addOnFailureListener { err ->
                Log.w(TAG, "SMS Retriever start failed (${mode.name}): ${err.message}", err)
                if (!disposed) {
                    currentOnStatusChanged(
                        SmsAutofillStatus.UNAVAILABLE,
                        "এই ডিভাইসে SMS অটো-ফিল চালু করা যায়নি; হাতে কোড লিখুন।"
                    )
                }
            }
        }

        onDispose {
            disposed = true
            if (registered) {
                runCatching { appContext.unregisterReceiver(receiver) }
            }
        }
    }
}
