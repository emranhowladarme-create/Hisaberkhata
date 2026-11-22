package com.example.support

import android.app.Application
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.HelpCenterScreen
import com.example.ui.theme.HisaberKhataTheme
import com.example.ui.viewmodel.HisaberAppData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SupportScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `help center displays exact reference elements and helpline dialer section`() {
        composeRule.setContent {
            HisaberKhataTheme {
                HelpCenterScreen(
                    appData = HisaberAppData(),
                    tickets = emptyList(),
                    onBack = {},
                    onCreateTicket = { _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText("হিসাবি খাতা").assertIsDisplayed()
        composeRule.onNodeWithText("আপনার হিসাব, আমাদের দায়িত্ব").assertIsDisplayed()
        composeRule.onNodeWithText("Help Support").assertIsDisplayed()
        composeRule.onNodeWithTag("support_tab_line").assertIsDisplayed()
        composeRule.onNodeWithTag("support_tab_helpline").assertIsDisplayed()
        composeRule.onNodeWithTag("support_tab_history").assertIsDisplayed()
        composeRule.onNodeWithTag("support_welcome_banner").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("দ্রুত প্রশ্নসমূহ").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_question_add_hisab").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_question_view_report").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_question_data_security").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_question_premium").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_question_how_to_use").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_question_other_issues").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("support_question_input").assertIsDisplayed()
        composeRule.onNodeWithTag("support_send_button").assertIsDisplayed()
        composeRule.onNodeWithTag("support_nav_home").assertIsDisplayed()
        composeRule.onNodeWithTag("support_nav_ledger").assertIsDisplayed()
        composeRule.onNodeWithTag("support_nav_reports").assertIsDisplayed()
        composeRule.onNodeWithTag("support_nav_profile").assertIsDisplayed()

        // Verify Help Line displays helpline number + single button opening phone dialer
        composeRule.onNodeWithTag("support_tab_helpline").performClick()
        composeRule.onNodeWithText("হেল্পলাইন নম্বর").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("+8809638517411").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("হেল্পলাইনে যোগাযোগ করুন").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("ইমেইল সাপোর্ট").assertDoesNotExist()
        composeRule.onNodeWithText("WhatsApp").assertDoesNotExist()

        composeRule.onNodeWithTag("helpline_call_button").performClick()
        val app = ApplicationProvider.getApplicationContext<Application>()
        val startedIntent = Shadows.shadowOf(app).nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(Intent.ACTION_DIAL, startedIntent.action)
        assertEquals("tel:+8809638517411", startedIntent.dataString)
    }
}
