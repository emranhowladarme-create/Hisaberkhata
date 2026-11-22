package com.example.gold

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.DashboardSummary
import com.example.ui.screens.GoldMembershipScreen
import com.example.ui.screens.HishabiDrawerContent
import com.example.ui.screens.MyGoldPackageScreen
import com.example.ui.screens.PaymentGatewayScreen
import com.example.ui.theme.HisaberKhataTheme
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.GoldActivationOutcome
import com.example.util.GoldPackageManager
import com.example.util.GoldPackagePlan
import com.example.util.HishabiPrefsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class GoldPackageSystemTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun resetPrefs(context: Context) {
        HishabiPrefsStore.update(context) {
            it.copy(
                goldMembershipActive = false,
                preferredPaymentProvider = "bkash",
                goldPackageId = "",
                goldPackageTitle = "",
                goldPackageDurationDays = 0,
                goldPackagePriceTaka = 0,
                goldStartTimeMillis = 0L,
                goldExpiryTimeMillis = 0L,
                goldLastVerifiedTxId = "",
                goldProcessedTxIds = emptySet(),
                verifiedPhoneNumber = "+8801985401510",
            )
        }
    }

    @Test
    fun goldPackageManagerValidatesActivatesCountsDownExpiresAndBlocksDuplicates() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)
        val t0 = 1_750_000_000_000L

        // Rejects blank transaction ID
        val blankTx = GoldPackageManager.activateVerifiedGoldPackage(
            context = context,
            plan = GoldPackagePlan.GOLD_1M,
            providerId = "bkash",
            verifiedTxId = "   ",
            paidAmountTaka = 79.0,
            nowMillis = t0,
        )
        assertTrue(blankTx is GoldActivationOutcome.InvalidPayment)

        // Rejects under-payment
        val underPaid = GoldPackageManager.activateVerifiedGoldPackage(
            context = context,
            plan = GoldPackagePlan.GOLD_2M,
            providerId = "nagad",
            verifiedTxId = "TX_LOW_01",
            paidAmountTaka = 100.0,
            nowMillis = t0,
        )
        assertTrue(underPaid is GoldActivationOutcome.InvalidPayment)

        // Activates valid 30-day 79 Taka package
        val activated = GoldPackageManager.activateVerifiedGoldPackage(
            context = context,
            plan = GoldPackagePlan.GOLD_1M,
            providerId = "bkash",
            verifiedTxId = "TX_GOLD_OK_100",
            paidAmountTaka = 79.0,
            nowMillis = t0,
        )
        assertTrue(activated is GoldActivationOutcome.Activated)
        val initialSnap = (activated as GoldActivationOutcome.Activated).snapshot
        assertTrue(initialSnap.isActive)
        assertFalse(initialSnap.isExpired)
        assertEquals(30, initialSnap.remainingDays)
        assertEquals("৩০ দিন", initialSnap.remainingDaysBangla)

        // Duplicate transaction ID cannot activate again
        val duplicate = GoldPackageManager.activateVerifiedGoldPackage(
            context = context,
            plan = GoldPackagePlan.GOLD_1M,
            providerId = "bkash",
            verifiedTxId = "TX_GOLD_OK_100",
            paidAmountTaka = 79.0,
            nowMillis = t0 + 1_000L,
        )
        assertTrue(duplicate is GoldActivationOutcome.DuplicateTransaction)

        // Daily countdown: after 1 full day -> 29 days; after 29 days -> 1 day; after 30 days -> expired (0 days)
        val prefs = HishabiPrefsStore.load(context)
        val after1Day = GoldPackageManager.computeSnapshot(prefs, t0 + GoldPackageManager.DAY_MILLIS)
        assertEquals(29, after1Day.remainingDays)
        assertEquals("২৯ দিন", after1Day.remainingDaysBangla)
        assertTrue(after1Day.isActive)

        val after29Days = GoldPackageManager.computeSnapshot(prefs, t0 + 29 * GoldPackageManager.DAY_MILLIS)
        assertEquals(1, after29Days.remainingDays)
        assertEquals("১ দিন", after29Days.remainingDaysBangla)
        assertTrue(after29Days.isActive)

        val expiredSnap = GoldPackageManager.syncExpiryIfNeeded(context, t0 + 30 * GoldPackageManager.DAY_MILLIS)
        assertEquals(0, expiredSnap.remainingDays)
        assertEquals("০ দিন", expiredSnap.remainingDaysBangla)
        assertFalse(expiredSnap.isActive)
        assertTrue(expiredSnap.isExpired)
        assertFalse(HishabiPrefsStore.load(context).goldMembershipActive)
    }

    @Test
    fun drawerScreenDisplaysExactShopInfoUploadButtonGoldBadgeAndAllMenusInOrder() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)

        val appData = HisaberAppData(
            profile = UserProfile(
                businessName = "ইমরান টি স্টল",
                phone = "+8801985401510",
            ),
            summary = DashboardSummary(),
        )

        composeRule.setContent {
            HisaberKhataTheme {
                HishabiDrawerContent(
                    data = appData,
                    onOpenBusinessInfo = {},
                    onOpenAccounts = {},
                    onOpenReports = {},
                    onOpenSalesLedger = {},
                    onOpenExpenses = {},
                    onOpenDueLedger = {},
                    onOpenCashLedger = {},
                    onOpenSettings = {},
                    onOpenPermissionCenter = {},
                    onOpenGoldMembership = {},
                    onOpenMyGoldPackage = {},
                    onOpenSupportFaq = {},
                    onShowStaffView = {},
                    onShowTransactionLimits = {},
                    onShowServiceCharge = {},
                    onLogout = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("drawer_shop_photo_upload").assertIsDisplayed()
        composeRule.onNodeWithText("ইমরান টি স্টল").assertIsDisplayed()
        composeRule.onNodeWithText("+8801985401510").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_gold_badge").assertIsDisplayed()
        composeRule.onNodeWithText("গোল্ড দিন").assertIsDisplayed()

        val expectedMenuTexts = listOf(
            "অ্যাকাউন্টসমূহ",
            "স্টাফ ভিউ",
            "লেনদেন বিবরণী",
            "লেনদেন লিমিট",
            "সার্ভিস চার্জ",
            "বেচা-কেনা হিসাব",
            "খরচ",
            "বাকি হিসাব",
            "ক্যাশ হিসাব",
            "মালিকের রিপোর্ট",
            "অন্যান্য সেটিংস",
            "পারমিশন সেন্টার",
            "সাপোর্ট সেন্টার",
            "লগ আউট",
        )
        expectedMenuTexts.forEach { label ->
            composeRule.onNodeWithText(label).performScrollTo().assertIsDisplayed()
        }
    }

    @Test
    fun goldMembershipScreenRendersThreePlansThreeProvidersAndPassesSelection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)

        var chosenPlan: GoldPackagePlan? = null
        var chosenProvider: String? = null

        composeRule.setContent {
            HisaberKhataTheme {
                GoldMembershipScreen(
                    onBack = {},
                    onOpenMyGoldPackage = {},
                    onProceedToPayment = { plan, provider ->
                        chosenPlan = plan
                        chosenProvider = provider
                    },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("গোল্ড প্যাকেজ").assertIsDisplayed()
        composeRule.onNodeWithText("সোনালী সদস্যতা").assertIsDisplayed()
        composeRule.onNodeWithText("বেশি সুবিধা, বেশি লাভ").assertIsDisplayed()

        composeRule.onNodeWithTag("gold_plan_gold_1m").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("৳৭৯").assertIsDisplayed()
        composeRule.onNodeWithTag("gold_plan_gold_2m").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("৳১৫০").assertIsDisplayed()
        composeRule.onNodeWithTag("gold_plan_gold_3m").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("৳২১০").assertIsDisplayed()

        composeRule.onNodeWithText("পেমেন্ট পদ্ধতি নির্বাচন করুন").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("gold_provider_bkash").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("gold_provider_nagad").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("gold_provider_rocket").performScrollTo().assertIsDisplayed()

        // Select 2-month (150 Taka) + Nagad and click Buy
        composeRule.onNodeWithTag("gold_plan_gold_2m").performScrollTo().performClick()
        composeRule.onNodeWithTag("gold_provider_nagad").performScrollTo().performClick()
        composeRule.onNodeWithTag("gold_subscribe_button").performClick()

        assertEquals(GoldPackagePlan.GOLD_2M, chosenPlan)
        assertEquals("nagad", chosenProvider)
    }

    @Test
    fun paymentGatewayForGoldHasNoInAppPinOrPaymentOtpInputs() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)

        composeRule.setContent {
            HisaberKhataTheme {
                PaymentGatewayScreen(
                    providerId = "bkash",
                    title = "গোল্ড প্যাকেজ • ১ মাস (৩০ দিন) প্যাকেজ",
                    amount = 79.0,
                    smsPackageCount = 100,
                    activateGold = true,
                    goldPackageId = GoldPackagePlan.GOLD_1M.id,
                    userId = "test_user",
                    onBack = {},
                    onGoldActivated = {},
                    onRecordPaymentComplete = { _, _, _, _, _, _ -> },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("গোল্ড প্যাকেজ • ১ মাস (৩০ দিন) প্যাকেজ").assertIsDisplayed()
        composeRule.onNodeWithTag("gateway_submit_button").assertIsDisplayed()
        // Never collect wallet PIN or payment OTP inside the app
        composeRule.onNodeWithText("ওটিপি পাঠান").assertDoesNotExist()
        composeRule.onNodeWithText("০১XXXXXXXXX").assertDoesNotExist()
    }

    @Test
    fun myGoldPackageScreenDisplaysActiveCountdownDetailsAndExpiredState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)
        val now = System.currentTimeMillis()
        val outcome = GoldPackageManager.activateVerifiedGoldPackage(
            context = context,
            plan = GoldPackagePlan.GOLD_1M,
            providerId = "bkash",
            verifiedTxId = "TX_UI_ACTIVE_1",
            paidAmountTaka = 79.0,
            nowMillis = now,
        )
        assertTrue(outcome is GoldActivationOutcome.Activated)

        var homeClicked = false
        composeRule.setContent {
            HisaberKhataTheme {
                MyGoldPackageScreen(
                    onBack = {},
                    onGoHome = { homeClicked = true },
                    onBuyOrRenewPackage = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("আমার গোল্ড প্যাকেজ").assertIsDisplayed()
        composeRule.onNodeWithText("১ মাস (৩০ দিন) প্যাকেজ").assertIsDisplayed()
        composeRule.onNodeWithText("অবশিষ্ট দিন").assertIsDisplayed()
        composeRule.onNodeWithTag("my_gold_remaining_days_text").assertIsDisplayed()
        composeRule.onNodeWithText("৩০ দিন").assertIsDisplayed()
        composeRule.onNodeWithText("গোল্ড প্যাকেজের বিবরণ").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("সক্রিয়").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("আপনার গোল্ড প্যাকেজ সফলভাবে চালু হয়েছে !").performScrollTo().assertIsDisplayed()

        // Ensure no mock OTP input exists on Screen 3
        composeRule.onNodeWithText("ওটিপি পাঠান").assertDoesNotExist()
        composeRule.onNodeWithText("আপনার মোবাইল নাম্বার দিন").assertDoesNotExist()

        composeRule.onNodeWithTag("my_gold_home_button").performClick()
        assertTrue(homeClicked)
    }

    @Test
    fun myGoldPackageScreenShowsZeroDaysAndExpiredMessageAfterExpiry() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)
        val pastStart = System.currentTimeMillis() - 31L * GoldPackageManager.DAY_MILLIS
        GoldPackageManager.activateVerifiedGoldPackage(
            context = context,
            plan = GoldPackagePlan.GOLD_1M,
            providerId = "rocket",
            verifiedTxId = "TX_UI_EXPIRED_1",
            paidAmountTaka = 79.0,
            nowMillis = pastStart,
        )

        composeRule.setContent {
            HisaberKhataTheme {
                MyGoldPackageScreen(
                    onBack = {},
                    onGoHome = {},
                    onBuyOrRenewPackage = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("০ দিন").assertIsDisplayed()
        composeRule.onNodeWithText("প্যাকেজের মেয়াদ শেষ।").assertIsDisplayed()
        composeRule.onNodeWithTag("my_gold_renew_button").assertIsDisplayed()
        composeRule.onNodeWithText("আপনার গোল্ড প্যাকেজ সফলভাবে চালু হয়েছে !").assertDoesNotExist()
    }
}
