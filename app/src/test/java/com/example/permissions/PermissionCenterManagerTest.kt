package com.example.permissions

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.LocationPermissionScreen
import com.example.ui.screens.PermissionCenterScreen
import com.example.ui.theme.HisaberKhataTheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PermissionCenterManagerTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var app: Application
    private lateinit var context: Context

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        context = app
        PermissionCenterManager.clearLocationHistory(context)
        shadowOf(app).denyPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_PHONE_STATE,
        )
    }

    @Test
    fun `evaluateAllPermissions reflects denied and granted states accurately`() {
        val initial = PermissionCenterManager.evaluateAllPermissions(context, null)
        assertEquals(5, initial.size)
        assertTrue(initial.none { it.isAllowed })

        shadowOf(app).grantPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_PHONE_STATE,
        )

        val updated = PermissionCenterManager.evaluateAllPermissions(context, null)
        assertTrue(updated.all { it.isAllowed })
        val locationState = updated.first { it.type == AppPermissionType.LOCATION }
        assertTrue(locationState.detailNote.contains("Approximate", ignoreCase = true))

        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        val preciseState = PermissionCenterManager.evaluatePermission(context, AppPermissionType.LOCATION, null)
        assertTrue(preciseState.detailNote.contains("Precise", ignoreCase = true))
    }

    @Test
    fun `location history starts empty with no fake locations and fails when denied`() = runTest {
        assertTrue(PermissionCenterManager.loadLocationHistory(context).isEmpty())

        val deniedResult = PermissionCenterManager.fetchAndRecordRealLocation(context)
        assertTrue(deniedResult.isFailure)
        assertTrue(PermissionCenterManager.loadLocationHistory(context).isEmpty())
    }

    @Test
    fun `fetchAndRecordRealLocation stores genuine LocationManager location when granted`() = runTest {
        shadowOf(app).grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val shadowLm = shadowOf(lm)
        shadowLm.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
        val realFix = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = 23.8103
            longitude = 90.4125
            accuracy = 18.0f
            time = System.currentTimeMillis()
        }
        shadowLm.setLastKnownLocation(LocationManager.NETWORK_PROVIDER, realFix)

        val result = PermissionCenterManager.fetchAndRecordRealLocation(context)
        assertTrue(result.isSuccess)
        val recorded = result.getOrNull()
        assertNotNull(recorded)
        assertTrue(recorded!!.label.isNotBlank())
        assertEquals(LocationManager.NETWORK_PROVIDER, recorded.provider)

        val storedHistory = PermissionCenterManager.loadLocationHistory(context)
        assertEquals(1, storedHistory.size)
        assertEquals(recorded.timestampMillis, storedHistory.first().timestampMillis)
    }

    @Test
    fun `permission center UI renders all 5 cards and real status summary`() {
        shadowOf(app).grantPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_PHONE_STATE,
        )

        composeRule.setContent {
            HisaberKhataTheme {
                PermissionCenterScreen(
                    onBack = {},
                    onOpenLocationDetails = {},
                )
            }
        }

        composeRule.onNodeWithText("Permission Center").assertIsDisplayed()
        composeRule.onNodeWithText("Hisaber Khata").assertIsDisplayed()
        composeRule.onNodeWithTag("permission_card_camera").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("permission_card_contacts").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("permission_card_location").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("permission_card_notifications").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("permission_card_phone").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("manage_unused_app_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("All permissions are granted").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `location permission screen displays empty history honestly when unrecorded`() {
        shadowOf(app).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        composeRule.setContent {
            HisaberKhataTheme {
                LocationPermissionScreen(onBack = {})
            }
        }

        composeRule.onNodeWithText("Location Permission").assertIsDisplayed()
        composeRule.onNodeWithText("Permission Granted").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("No location access recorded yet.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("location_open_settings_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("location_fetch_now_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `createManageUnusedAppIntent and createAppSettingsIntent point to valid packages`() {
        val appSettingsIntent = PermissionCenterManager.createAppSettingsIntent(context)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appSettingsIntent.action)
        assertFalse(appSettingsIntent.dataString.isNullOrBlank())

        val unusedIntent = PermissionCenterManager.createManageUnusedAppIntent(context)
        assertNotNull(unusedIntent.action)
    }
}
