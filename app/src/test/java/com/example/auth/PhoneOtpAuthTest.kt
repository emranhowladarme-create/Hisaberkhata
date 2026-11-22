package com.example.auth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.AuthFlowScreen
import com.example.ui.theme.HisaberKhataTheme
import com.example.util.HishabiPrefsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakePhoneOtpGateway(
    var online: Boolean = true,
    var forceSendFailure: String? = null,
    val expectedCode: String = "482915",
) : PhoneOtpAuthGateway {
    val sentPhones = mutableListOf<String>()
    val verifiedAttempts = mutableListOf<Pair<String, String>>()

    override fun isNetworkConnected(context: Context): Boolean = online

    override suspend fun sendOtp(
        activity: Activity?,
        context: Context,
        phoneE164: String,
        appHashes: List<String>,
    ): OtpSendOutcome {
        if (!online) return OtpSendOutcome.Error(OtpErrorMessages.NO_INTERNET)
        forceSendFailure?.let { return OtpSendOutcome.Error(it) }
        sentPhones += phoneE164
        return OtpSendOutcome.CodeSent(
            OtpChallengeSession(
                challengeId = "ch_${sentPhones.size}",
                phoneE164 = phoneE164,
                providerMode = OtpBackendProviderMode.FIREBASE_PHONE_6_DIGIT,
                otpLength = 6,
                expiresInSec = 60,
                resendAfterSec = 30,
            )
        )
    }

    override suspend fun resendOtp(
        activity: Activity?,
        context: Context,
        previousSession: OtpChallengeSession,
        appHashes: List<String>,
    ): OtpSendOutcome {
        return sendOtp(activity, context, previousSession.phoneE164, appHashes)
    }

    override suspend fun verifyOtp(
        context: Context,
        session: OtpChallengeSession,
        otpCode: String,
    ): OtpVerifyOutcome {
        if (!online) return OtpVerifyOutcome.Error(OtpErrorMessages.NO_INTERNET)
        verifiedAttempts += session.phoneE164 to otpCode
        return if (otpCode == expectedCode) {
            OtpVerifyOutcome.Verified(phoneE164 = session.phoneE164, hasFirebaseUser = false)
        } else {
            OtpVerifyOutcome.Error(OtpErrorMessages.INVALID_OTP)
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class PhoneOtpAuthTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bangladeshPhoneValidatorHandlesFormatsAndRejectsInvalidNumbers() {
        assertEquals("+8801712345678", BangladeshPhoneValidator.normalize("+8801712345678")?.e164)
        assertEquals("+8801712345678", BangladeshPhoneValidator.normalize("8801712345678")?.e164)
        assertEquals("+8801712345678", BangladeshPhoneValidator.normalize("01712345678")?.e164)
        assertEquals("+8801712345678", BangladeshPhoneValidator.normalize("1712345678")?.e164)
        assertEquals("+8801712345678", BangladeshPhoneValidator.normalize("০১৭১২৩৪৫৬৭৮")?.e164)
        assertEquals("+8801912345678", BangladeshPhoneValidator.normalize("01912345678")?.e164)

        assertNull(BangladeshPhoneValidator.normalize("01212345678"))
        assertNull(BangladeshPhoneValidator.normalize("12345"))
        assertNull(BangladeshPhoneValidator.normalize("0171234"))
    }

    @Test
    fun appManifestDoesNotRequestProhibitedSmsPermissions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        val requested = pkgInfo.requestedPermissions?.toSet().orEmpty()
        assertFalse(Manifest.permission.READ_SMS in requested)
        assertFalse(Manifest.permission.RECEIVE_SMS in requested)
    }

    @Test
    fun authFlowMatchesReferenceScreensHandlesFiveSecondAdRotationAndSixDigitOtp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        HishabiPrefsStore.update(context) { it.copy(isPhoneSessionVerified = false, verifiedPhoneNumber = "") }

        val gateway = FakePhoneOtpGateway(online = true, expectedCode = "482915")
        var verifiedCallbackPhone: String? = null

        composeRule.setContent {
            HisaberKhataTheme {
                AuthFlowScreen(
                    onOtpVerifiedOnly = { _, _, phone -> verifiedCallbackPhone = phone },
                    onGoogleSignInSuccess = { _, _, _ -> },
                    otpGateway = gateway,
                )
            }
        }
        composeRule.waitForIdle()

        // 1. Login screen matches exact reference layout with ad in place of old payment icons
        composeRule.onNodeWithText("আপনার হিসাব, এখন আরও সহজ!").assertIsDisplayed()
        composeRule.onNodeWithText("NID দিয়ে নিবন্ধিত মোবাইল নম্বর লিখুন").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("login_phone_input").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("login_ad_banner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("send_otp_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("google_signin_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Google দিয়ে লগইন করুন").performScrollTo().assertIsDisplayed()

        // Old payment cards must NOT appear on login page
        composeRule.onNodeWithText("বিকাশ").assertDoesNotExist()
        composeRule.onNodeWithText("নগদ").assertDoesNotExist()
        composeRule.onNodeWithText("রকেট").assertDoesNotExist()
        composeRule.onNodeWithText("ব্যাংক").assertDoesNotExist()

        // Only 1 promo ad visible at start (#1: woman using app) & cycles through all 5 every 5 seconds
        composeRule.onNodeWithText("মোবাইলে রাখুন দৈনিক সব হিসাব!").assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(5_100L)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ব্যবসা, চাকরি বা ব্যক্তিগত সব হিসাব রাখুন সহজে!").assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(5_100L)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("একসাথে সহজ হোক সব হিসাব!").assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(5_100L)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ক্রেতার বাকি ও পেমেন্ট রাখুন এক ট্যাপে!").assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(5_100L)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("সব হিসাব এক অ্যাপে!").assertIsDisplayed()

        // Wraps cleanly back to ad #1 after 5th ad
        composeRule.mainClock.advanceTimeBy(5_100L)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("মোবাইলে রাখুন দৈনিক সব হিসাব!").assertIsDisplayed()

        // 2. Invalid number is rejected cleanly in Bengali
        composeRule.onNodeWithTag("login_phone_input").performTextInput("12345")
        composeRule.onNodeWithTag("send_otp_button").performScrollTo().performClick()
        composeRule.onNodeWithText(OtpErrorMessages.INVALID_PHONE).assertIsDisplayed()
        assertTrue(gateway.sentPhones.isEmpty())

        // 3. Offline state shows Bengali internet error without faking OTP screen
        gateway.online = false
        composeRule.onNodeWithTag("login_phone_input").performTextReplacement("01712345678")
        composeRule.onNodeWithTag("send_otp_button").performScrollTo().performClick()
        composeRule.onNodeWithText(OtpErrorMessages.NO_INTERNET).assertIsDisplayed()
        assertTrue(gateway.sentPhones.isEmpty())

        // Verify no user-facing error message exposes technical Firebase/Console/API jargon
        val allUserErrors = listOf(
            OtpErrorMessages.SEND_FAILED,
            OtpErrorMessages.INVALID_PHONE,
            OtpErrorMessages.INVALID_OTP,
            OtpErrorMessages.OTP_EXPIRED,
            OtpErrorMessages.SMS_DELAYED,
            OtpErrorMessages.NO_INTERNET,
            OtpErrorMessages.GOOGLE_SIGNIN_FAILED,
            OtpErrorMessages.TOO_MANY_REQUESTS,
            OtpErrorMessages.OTP_SERVICE_UNAVAILABLE,
        )
        allUserErrors.forEach { msg ->
            assertFalse(msg.contains("Firebase", ignoreCase = true))
            assertFalse(msg.contains("Console", ignoreCase = true))
            assertFalse(msg.contains("API Key", ignoreCase = true))
            assertFalse(msg.contains("SHA-1", ignoreCase = true))
        }

        // 4. Valid online Bangladeshi number sends real OTP and enters 6-box OTP screen
        gateway.online = true
        composeRule.onNodeWithTag("send_otp_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) { gateway.sentPhones.size == 1 }
        assertEquals("+8801712345678", gateway.sentPhones.first())

        composeRule.onNodeWithText("আপনার মোবাইলে ৬ সংখ্যার\nOTP পাঠানো হয়েছে").assertIsDisplayed()
        composeRule.onNodeWithText("+880 1712345678").assertIsDisplayed()
        composeRule.onNodeWithTag("otp_boxes_row").performScrollTo().assertIsDisplayed()
        repeat(6) { idx ->
            composeRule.onNodeWithTag("otp_box_$idx").assertIsDisplayed()
        }
        composeRule.onNodeWithText("যাচাই করুন").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("otp_countdown_text").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("পুনরায় OTP পাঠান").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("resend_otp_button").assertIsNotEnabled()
        composeRule.onNodeWithTag("otp_ad_banner").performScrollTo().assertIsDisplayed()

        // 5. Wrong 6-digit OTP (multi-digit input into first box) is rejected cleanly
        composeRule.onNodeWithTag("otp_box_0").performScrollTo().performTextInput("000000")
        composeRule.onNodeWithTag("verify_otp_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) { gateway.verifiedAttempts.size == 1 }
        composeRule.onNodeWithText(OtpErrorMessages.INVALID_OTP).performScrollTo().assertIsDisplayed()
        assertNull(verifiedCallbackPhone)

        // 6. After 60 seconds expire, "পুনরায় OTP পাঠান" button automatically enables and resends OTP
        composeRule.mainClock.advanceTimeBy(61_000L)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("resend_otp_button").performScrollTo().assertIsEnabled()
        composeRule.onNodeWithTag("resend_otp_button").performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) { gateway.sentPhones.size == 2 }

        // 7. Correct 6-digit OTP typed one digit per separate box succeeds and enters app
        val targetDigits = listOf("4", "8", "2", "9", "1", "5")
        targetDigits.forEachIndexed { idx, digit ->
            composeRule.onNodeWithTag("otp_box_$idx").performScrollTo().performTextInput(digit)
        }
        composeRule.onNodeWithTag("verify_otp_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) { verifiedCallbackPhone != null }
        assertEquals("+8801712345678", verifiedCallbackPhone)
        assertTrue(HishabiPrefsStore.load(context).isPhoneSessionVerified)
    }
}
