package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

enum class PaymentProvider(
    val id: String,
    val displayNameBn: String,
    val displayNameEn: String,
) {
    BKASH(
        id = "bkash",
        displayNameBn = "বিকাশ",
        displayNameEn = "bKash",
    ),
    NAGAD(
        id = "nagad",
        displayNameBn = "নগদ",
        displayNameEn = "Nagad",
    ),
    ROCKET(
        id = "rocket",
        displayNameBn = "রকেট",
        displayNameEn = "Rocket",
    ),
}

enum class GatewayExecutionMode {
    DEMO,
    BACKEND_SERVER,
}

data class PaymentGatewayResult(
    val transactionId: String,
    val isDemo: Boolean,
    val provider: PaymentProvider,
    val amount: Double,
    val message: String,
    val verified: Boolean = true,
    val checkoutUrl: String = "",
)

object PaymentGatewayConfig {
    private const val PREFS_NAME = "hisaber_gateway_prefs"
    private const val KEY_BACKEND_URL = "backend_server_url"
    private const val KEY_FORCE_DEMO = "force_demo_mode"

    fun getBackendServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BACKEND_URL, "")?.trim().orEmpty()
    }

    fun isForceDemoMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FORCE_DEMO, true)
    }

    fun saveBackendConfig(context: Context, serverUrl: String, forceDemoMode: Boolean) {
        val cleaned = serverUrl.trim().trimEnd('/')
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BACKEND_URL, cleaned)
            .putBoolean(KEY_FORCE_DEMO, forceDemoMode || cleaned.isBlank())
            .apply()
    }

    fun currentMode(context: Context): GatewayExecutionMode {
        val url = getBackendServerUrl(context)
        val forcedDemo = isForceDemoMode(context)
        return if (!forcedDemo && (url.startsWith("https://") || url.startsWith("http://"))) {
            GatewayExecutionMode.BACKEND_SERVER
        } else {
            GatewayExecutionMode.DEMO
        }
    }

    fun isBackendConfigured(context: Context): Boolean {
        return currentMode(context) == GatewayExecutionMode.BACKEND_SERVER
    }
}

object BackendGatewayClient {
    suspend fun initiatePayment(
        context: Context,
        provider: PaymentProvider,
        amount: Double,
        accountNumber: String,
        reference: String,
        userId: String,
    ): Result<PaymentGatewayResult> = withContext(Dispatchers.IO) {
        val mode = PaymentGatewayConfig.currentMode(context)
        if (mode == GatewayExecutionMode.DEMO) {
            val demoRef = "DEMO-${provider.id.uppercase()}-${UUID.randomUUID().toString().take(8).uppercase()}"
            return@withContext Result.success(
                PaymentGatewayResult(
                    transactionId = demoRef,
                    isDemo = true,
                    provider = provider,
                    amount = amount,
                    message = "ডেমো মোডে ${provider.displayNameBn} পেমেন্ট যাচাই সম্পন্ন হয়েছে (কোনো আসল লেনদেন হয়নি)।",
                    verified = true,
                )
            )
        }

        runCatching {
            val baseUrl = PaymentGatewayConfig.getBackendServerUrl(context)
            val endpoint = URL("$baseUrl/api/payments/initiate")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val payload = JSONObject().apply {
                put("provider", provider.id)
                put("amount", amount)
                put("accountNumber", accountNumber)
                put("reference", reference)
                put("userId", userId)
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            conn.disconnect()

            if (code !in 200..299) {
                throw IllegalStateException("ব্যাকএন্ড সার্ভার ত্রুটি (HTTP $code): ${body.ifBlank { "সংযোগ ব্যর্থ" }}")
            }

            val json = runCatching { JSONObject(body) }.getOrNull()
            val txId = json?.optString("transactionId").orEmpty()
                .ifBlank { "SRV-${provider.id.uppercase()}-${UUID.randomUUID().toString().take(8).uppercase()}" }
            val checkoutUrl = json?.optString("checkoutUrl").orEmpty()
            val verifiedFromServer = json?.optBoolean("verified", checkoutUrl.isBlank()) ?: checkoutUrl.isBlank()
            PaymentGatewayResult(
                transactionId = txId,
                isDemo = false,
                provider = provider,
                amount = amount,
                message = json?.optString("message").orEmpty().ifBlank {
                    "${provider.displayNameBn} ব্যাকএন্ড পেমেন্ট অনুরোধ সফল হয়েছে।"
                },
                verified = verifiedFromServer,
                checkoutUrl = checkoutUrl,
            )
        }
    }

    suspend fun verifyPaymentStatus(
        context: Context,
        provider: PaymentProvider,
        transactionId: String,
        expectedAmount: Double,
    ): Result<PaymentGatewayResult> = withContext(Dispatchers.IO) {
        if (transactionId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("অবৈধ ট্রানজেকশন আইডি"))
        }
        val mode = PaymentGatewayConfig.currentMode(context)
        if (mode == GatewayExecutionMode.DEMO) {
            return@withContext Result.success(
                PaymentGatewayResult(
                    transactionId = transactionId,
                    isDemo = true,
                    provider = provider,
                    amount = expectedAmount,
                    message = "ডেমো মোডে ${provider.displayNameBn} পেমেন্ট স্ট্যাটাস যাচাই সফল হয়েছে।",
                    verified = true,
                )
            )
        }

        runCatching {
            val baseUrl = PaymentGatewayConfig.getBackendServerUrl(context)
            val endpoint = URL("$baseUrl/api/payments/verify")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val payload = JSONObject().apply {
                put("provider", provider.id)
                put("transactionId", transactionId)
                put("expectedAmount", expectedAmount)
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            conn.disconnect()

            if (code !in 200..299) {
                throw IllegalStateException("পেমেন্ট যাচাই ব্যর্থ (HTTP $code): ${body.ifBlank { "সার্ভার ত্রুটি" }}")
            }

            val json = JSONObject(body)
            val isVerified = json.optBoolean("verified", false)
            val paidAmount = json.optDouble("amount", 0.0)
            if (!isVerified || paidAmount + 0.01 < expectedAmount) {
                throw IllegalStateException("পেমেন্ট সম্পন্ন হয়নি বা পরিমাণ মিলছে না")
            }
            PaymentGatewayResult(
                transactionId = transactionId,
                isDemo = false,
                provider = provider,
                amount = paidAmount,
                message = json.optString("message").ifBlank {
                    "${provider.displayNameBn} পেমেন্ট সফলভাবে যাচাই হয়েছে।"
                },
                verified = true,
            )
        }
    }

    suspend fun sendSmsViaBackendOrDemo(
        context: Context,
        recipients: List<String>,
        message: String,
        messageType: String,
        userId: String,
    ): Result<Pair<Boolean, String>> = withContext(Dispatchers.IO) {
        if (!PaymentGatewayConfig.isBackendConfigured(context)) {
            return@withContext Result.success(true to "ডেমো মোড: ${recipients.size}টি নম্বরে SMS টেস্ট লগ সম্পন্ন হয়েছে।")
        }

        runCatching {
            val baseUrl = PaymentGatewayConfig.getBackendServerUrl(context)
            val endpoint = URL("$baseUrl/api/sms/send")
            val conn = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }

            val payload = JSONObject().apply {
                put("recipients", org.json.JSONArray(recipients))
                put("message", message)
                put("messageType", messageType)
                put("userId", userId)
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }
            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            conn.disconnect()

            if (code !in 200..299) {
                throw IllegalStateException("SMS সার্ভার ত্রুটি (HTTP $code): $body")
            }
            false to "ব্যাকএন্ড SMS সার্ভারের মাধ্যমে পাঠানো হয়েছে।"
        }
    }
}
