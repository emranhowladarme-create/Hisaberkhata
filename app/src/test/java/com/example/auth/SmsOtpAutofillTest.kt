package com.example.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsOtpAutofillTest {

    @Test
    fun extractsStandardSmsRetrieverOtpIgnoringTimestamp() {
        val sms = "<#> 01:16 - Hisaber Khata verification code is 4829 FA+9qCX9VSu"
        assertEquals("4829", extractFourDigitOtp(sms))
    }

    @Test
    fun extractsBengaliDigitOtpAndNormalizesToAscii() {
        val sms = "হিসাবের খাতা: আপনার OTP কোড হলো ৪৮২৯। এটি কাউকে বলবেন না।"
        assertEquals("4829", extractFourDigitOtp(sms))
    }

    @Test
    fun prioritizesKeywordAdjacentOtpOverYearOrTime() {
        val sms = "2026-11-17 01:16 সময়ে আপনার পিন: 7391 ব্যবহার করুন।"
        assertEquals("7391", extractFourDigitOtp(sms))
    }

    @Test
    fun returnsNullWhenNoFourDigitCodePresent() {
        assertNull(extractFourDigitOtp("হিসাবের খাতায় স্বাগতম"))
    }

    @Test
    fun extractsSixDigitFirebaseAndSmsRetrieverOtpWithBengaliDigits() {
        val smsAscii = "<#> 482915 আপনার হিসাবের খাতা যাচাইকরণ কোড। FA+9qCX9VSu"
        assertEquals("482915", extractOtpCode(smsAscii, expectedLength = 6))

        val smsBangla = "হিসাবের খাতা: আপনার OTP কোড হলো ৪৮২৯১৫। মেয়াদ ১ মিনিট।"
        assertEquals("482915", extractOtpCode(smsBangla, expectedLength = 6))
    }
}
