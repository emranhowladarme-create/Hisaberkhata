package com.example.gold

import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Customer
import com.example.data.model.LedgerTransaction
import com.example.data.model.UserProfile
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerEditScreen
import com.example.ui.screens.HisabiGoldScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HisaberKhataTheme
import com.example.ui.viewmodel.DashboardSummary
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.GoldPackageManager
import com.example.util.GoldPackagePlan
import com.example.util.HishabiLocalPrefs
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
class PanelCustomizationUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun resetPrefs(context: Context) {
        HishabiPrefsStore.update(context) {
            HishabiLocalPrefs(
                verifiedPhoneNumber = "+8801985401510",
                goldAutoRenew = true,
            )
        }
    }

    @Test
    fun hisabiGoldScreenRendersChecklistAutoRestoreToggleAndPackagesCta() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)

        var openPackagesClicked = false

        composeRule.setContent {
            HisaberKhataTheme {
                HisabiGoldScreen(
                    onBack = {},
                    onOpenGoldPackages = { openPackagesClicked = true },
                    onOpenMyGoldPackage = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("হিসাবি খাতা গোল্ড").assertCountEquals(2)
        composeRule.onNodeWithText("ব্যবসার হিসাব হবে আরও সহজ").assertIsDisplayed()
        composeRule.onNodeWithText("অটো ব্যাকআপ রিস্টোর").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("বর্তমান প্যাকেজের সুবিধা").performScrollTo().assertIsDisplayed()

        // Toggle auto backup restore switch and verify HishabiPrefsStore updates
        composeRule.onNodeWithTag("gold_auto_restore_switch").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertFalse(HishabiPrefsStore.load(context).goldAutoRenew)

        composeRule.onNodeWithTag("hisabi_gold_view_packages_button").performClick()
        assertTrue(openPackagesClicked)
    }

    @Test
    fun customerEditScreenDisplaysSelectedCustomerAllowsEditingAndValidatesBlankName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)

        val customer = Customer(
            id = "cust_rakib",
            name = "রাকিব গ্যারেজ",
            phone = "+8801400705019",
            address = "ঢাকা",
            notes = "Customer",
            balance = 600.0,
        )

        var savedName = ""
        var savedPhone = ""

        composeRule.setContent {
            HisaberKhataTheme {
                CustomerEditScreen(
                    customer = customer,
                    onBack = {},
                    onSaveCustomer = { name, phone, _, _, _ ->
                        savedName = name
                        savedPhone = phone
                    },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("কাস্টমার এডিট করুন").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_customer_photo_avatar").assertIsDisplayed()
        composeRule.onNodeWithText("ছবি যোগ করুন").assertIsDisplayed()
        composeRule.onNodeWithText("রাকিব গ্যারেজ").assertIsDisplayed()
        composeRule.onNodeWithText("+8801400705019").assertIsDisplayed()

        // Validate blank name rejection
        composeRule.onNodeWithTag("edit_customer_name_input").performTextClearance()
        composeRule.onNodeWithTag("edit_customer_save_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("অনুগ্রহ করে কাস্টমারের নাম লিখুন").assertIsDisplayed()
        assertEquals("", savedName)

        // Enter updated name and save
        composeRule.onNodeWithTag("edit_customer_name_input").performTextInput("রাকিব অটোমোবাইলস")
        composeRule.onNodeWithTag("edit_customer_save_button").performClick()
        composeRule.waitForIdle()

        assertEquals("রাকিব অটোমোবাইলস", savedName)
        assertEquals("+8801400705019", savedPhone)
    }

    @Test
    fun customerDetailScreenDisplaysPanel2GoldExpiryBannerActionsAndSavesTransaction() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)
        GoldPackageManager.activateVerifiedGoldPackage(
            context = context,
            plan = GoldPackagePlan.GOLD_1M,
            providerId = "bkash",
            verifiedTxId = "TX_CUST_DETAIL_1",
            paidAmountTaka = 89.0,
            nowMillis = System.currentTimeMillis(),
        )

        val customer = Customer(
            id = "cust_1",
            name = "বিলন",
            phone = "01711002233",
            balance = 600.0,
        )
        val existingTx = listOf(
            LedgerTransaction(
                id = "tx_1",
                customerId = "cust_1",
                customerName = "বিলন",
                type = "gave",
                amount = 600.0,
                description = "পুরোনো বাকি",
                dateMillis = System.currentTimeMillis() - 600_000L,
            )
        )

        var recordedType = ""
        var recordedAmount = 0.0
        var recordedDesc = ""

        composeRule.setContent {
            HisaberKhataTheme {
                CustomerDetailScreen(
                    customer = customer,
                    businessName = "ইমরান টি স্টল",
                    transactions = existingTx,
                    onBack = {},
                    onSaveTransaction = { type, amt, desc, _, _ ->
                        recordedType = type
                        recordedAmount = amt
                        recordedDesc = desc
                    },
                    onUpdateTransaction = { _, _, _, _ -> },
                    onSendSms = {},
                    onOpenQr = {},
                    onUpdateCustomer = { _, _, _, _ -> },
                    onDeleteCustomer = {},
                    onDeleteTransaction = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("customer_profile_avatar_edit").assertIsDisplayed()
        composeRule.onNodeWithTag("customer_gold_expiry_banner").assertIsDisplayed()
        composeRule.onNodeWithText("হিসাবি খাতা গোল্ড-এর মেয়াদ অবশিষ্ট ৩০ দিন").assertIsDisplayed()
        composeRule.onNodeWithTag("customer_tagada_button").assertIsDisplayed()
        composeRule.onNodeWithTag("customer_report_button").assertIsDisplayed()

        composeRule.onNodeWithTag("profile_gave_input").performTextInput("250")
        composeRule.onNodeWithTag("profile_description_input").performTextInput("চা ও বিস্কুট")
        composeRule.onNodeWithTag("profile_confirm_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals("gave", recordedType)
        assertEquals(250.0, recordedAmount, 0.01)
        assertEquals("চা ও বিস্কুট", recordedDesc)
    }

    @Test
    fun settingsScreenDisplaysBusinessOwnerInfoNotCustomerDataAndConfirmsDeleteAccount() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resetPrefs(context)

        val appData = HisaberAppData(
            profile = UserProfile(
                businessName = "ইমরান টি স্টল",
                ownerName = "Mohammad Emran Hawladar",
                phone = "+8801985401510",
                notificationsEnabled = true,
            ),
            customers = listOf(
                Customer(
                    id = "cust_rakib",
                    name = "রাকিব গ্যারেজ",
                    phone = "+8801400705019",
                    balance = 500.0,
                )
            ),
            summary = DashboardSummary(),
        )

        var deletedAccount = false

        composeRule.setContent {
            HisaberKhataTheme {
                SettingsScreen(
                    data = appData,
                    onBack = {},
                    onUpdateSettings = { _, _ -> },
                    onOpenBusinessInfo = {},
                    onOpenBusinessCategory = {},
                    onOpenGoldMembership = {},
                    onOpenMultiBusiness = {},
                    onOpenPermissionCenter = {},
                    onOpenHelpCenter = {},
                    onLogout = {},
                    onDeleteAccount = { deletedAccount = true },
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("সেটিংস").assertIsDisplayed()
        composeRule.onNodeWithText("ইমরান টি স্টল").assertIsDisplayed()
        composeRule.onNodeWithText("+8801985401510").assertIsDisplayed()

        // Ensure customer data is never mixed into Settings
        composeRule.onNodeWithText("রাকিব গ্যারেজ").assertDoesNotExist()
        composeRule.onNodeWithText("+8801400705019").assertDoesNotExist()

        composeRule.onNodeWithTag("settings_customer_settings_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_notif_sound_toggle").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_language_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_backup_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_support_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_permission_center_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_about_row").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_logout_button").performScrollTo().assertIsDisplayed()

        // Delete account confirmation flow
        composeRule.onNodeWithTag("settings_delete_account_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("অ্যাকাউন্ট মুছবেন?").assertIsDisplayed()
        composeRule.onNodeWithText("হ্যাঁ, মুছে ফেলুন").performClick()
        composeRule.waitForIdle()

        assertTrue(deletedAccount)
    }
}
