package com.example

import com.example.data.model.Customer
import com.example.support.SupportTextPolicy
import com.example.ui.screens.isSupplierParty
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun supportTextPolicy_enforcesTextOnlyModeAndGreetings() {
        assertTrue(SupportTextPolicy.TEXT_ONLY_MODE_ENABLED)
        assertFalse(SupportTextPolicy.VOICE_FEATURES_ENABLED)
        assertFalse(SupportTextPolicy.AUDIO_FEATURES_ENABLED)
        assertFalse(SupportTextPolicy.VOICE_CALL_ENABLED)
        assertFalse(SupportTextPolicy.MICROPHONE_ENABLED)

        assertEquals(
            "ওয়ালাইকুম আসসালাম। কেমন আছেন? বলুন, কীভাবে আপনাকে সাহায্য করতে পারি?",
            SupportTextPolicy.computeInitialGreeting("আসসালামু আলাইকুম"),
        )
        assertTrue(SupportTextPolicy.isLikelyEnglish("How do I add a customer?"))
        assertFalse(SupportTextPolicy.isLikelyEnglish("কিভাবে হিসাব যোগ করব?"))
    }

    @Test
    fun formatters_banglaDecimalAndDayMonth_matchSpec() {
        assertEquals("৬.০০", Formatters.formatDecimalAmount(6.0, useBanglaDigits = true))
        assertEquals("৳ ৬.০০", Formatters.formatTakaDecimal(6.0, useBanglaDigits = true))

        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 17, 12, 0, 0)
        }
        assertEquals("১৭ নভেম্বর", Formatters.formatBanglaDayMonth(cal.timeInMillis))
    }

    @Test
    fun formatters_customerEntryTimestamp_showsSecondsMinutesHoursAndFullDate() {
        val baseCal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 17, 15, 58, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val entryMillis = baseCal.timeInMillis

        val after12Sec = Formatters.formatCustomerEntryTimestamp(entryMillis, entryMillis + 12_000L)
        assertTrue(after12Sec.startsWith("১২ সেকেন্ড আগে • ১৭ নভেম্বর ২০২৬, ৩:৫৮ PM"))

        val after2Min = Formatters.formatCustomerEntryTimestamp(entryMillis, entryMillis + 2 * 60_000L)
        assertEquals("২ মিনিট আগে • ১৭ নভেম্বর ২০২৬, ৩:৫৮ PM", after2Min)

        val after1Hr = Formatters.formatCustomerEntryTimestamp(entryMillis, entryMillis + 60 * 60_000L)
        assertEquals("১ ঘণ্টা আগে • ১৭ নভেম্বর ২০২৬, ৩:৫৮ PM", after1Hr)
    }

    @Test
    fun homePartyClassification_countsCustomersAndSuppliersDynamically() {
        val list = listOf(
            Customer(id = "1", name = "জান্নাত", balance = 40.0, notes = "Customer"),
            Customer(id = "2", name = "মিলন", balance = 20.0, notes = "Customer"),
            Customer(id = "3", name = "আয়াত", balance = 50.0, notes = "Customer"),
            Customer(id = "4", name = "রহিম ট্রেডার্স", balance = -200.0, notes = "Supplier"),
            Customer(id = "5", name = "করিম স্টোর", balance = 0.0, notes = "Supplier"),
        )
        assertEquals(3, list.count { !isSupplierParty(it) })
        assertEquals(2, list.count { isSupplierParty(it) })
    }
}
