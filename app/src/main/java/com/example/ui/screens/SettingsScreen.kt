package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.components.ShopStorefrontAvatarIllustration
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.DenaRedBg
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.Formatters
import com.example.util.HishabiPrefsStore
import com.example.util.PaymentGatewayConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val SettingsEmerald = Color(0xFF057A4B)
private val SettingsSwitchBlue = Color(0xFF1D74F5)

/**
 * Panel 6: সেটিংস পেজ (Settings Screen).
 * Displays the logged-in business owner's profile (never mixing customer data), clean icon rows,
 * customer/app settings sub-controls, notification switch, language selector, backup options,
 * support, permission center, about dialog, logout, and account deletion confirmation.
 */
@Composable
fun SettingsScreen(
    data: HisaberAppData,
    onBack: () -> Unit,
    onUpdateSettings: (Map<String, Any>, String) -> Unit,
    onOpenBusinessInfo: () -> Unit,
    onOpenBusinessCategory: () -> Unit,
    onOpenGoldMembership: () -> Unit,
    onOpenMultiBusiness: () -> Unit,
    onOpenPermissionCenter: () -> Unit = {},
    onOpenHelpCenter: () -> Unit = {},
    isCloudConnected: Boolean = true,
    onConnectGoogleCloud: () -> Unit = {},
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit = onLogout,
) {
    val context = LocalContext.current
    val prefsState by HishabiPrefsStore.prefsFlow.collectAsState()
    val initialPrefs = remember(prefsState) { HishabiPrefsStore.load(context) }

    var decimalEnabled by rememberSaveable { mutableStateOf(initialPrefs.decimalEnabled) }
    var notificationSound by rememberSaveable(data.profile.notificationsEnabled) {
        mutableStateOf(data.profile.notificationsEnabled && initialPrefs.notificationSoundEnabled)
    }
    var voiceAlertEnabled by rememberSaveable { mutableStateOf(initialPrefs.voiceAlertEnabled) }
    var pinlessEntryEnabled by rememberSaveable { mutableStateOf(initialPrefs.pinlessEntryEnabled) }

    var showCustomerSettingsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showBackendDialog by remember { mutableStateOf(false) }

    var savedBackendUrl by remember { mutableStateOf(PaymentGatewayConfig.getBackendServerUrl(context)) }
    var forceDemoMode by remember { mutableStateOf(PaymentGatewayConfig.isForceDemoMode(context)) }
    val isServerMode = !forceDemoMode && (savedBackendUrl.startsWith("https://") || savedBackendUrl.startsWith("http://"))

    val ownerBusinessName = data.profile.businessName.ifBlank { "ইমরান টি স্টল" }
    val ownerPhone = initialPrefs.verifiedPhoneNumber
        .ifBlank { data.profile.phone }
        .trim()
        .ifBlank { "মোবাইল নম্বর যোগ নেই" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
    ) {
        // Clean White Top Bar matching Panel 6 ('← সেটিংস')
        Surface(color = PureWhite, shadowElevation = 1.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = InkPrimary,
                    )
                }
                Text(
                    text = "সেটিংস",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState()),
        ) {
            // Top Business Profile Header Row (Strictly Business Owner's Data, NEVER Customer Data)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenBusinessInfo)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("settings_profile_header"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5))
                        .border(1.5.dp, SettingsEmerald.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (initialPrefs.businessLogoPath.isNotBlank()) {
                        AsyncImage(
                            model = initialPrefs.businessLogoPath,
                            contentDescription = ownerBusinessName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        ShopStorefrontAvatarIllustration(modifier = Modifier.fillMaxSize())
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ownerBusinessName,
                        color = InkPrimary,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ownerPhone,
                        color = InkSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "ব্যবসার তথ্য",
                    tint = InkSecondary,
                )
            }

            HorizontalDivider(color = CardBorder)

            // 1. কাস্টমার সেটিংস >
            SettingsMenuRow(
                icon = Icons.Outlined.AccountCircle,
                title = "কাস্টমার সেটিংস",
                testTag = "settings_customer_settings_row",
                onClick = { showCustomerSettingsDialog = true },
            )
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(start = 54.dp))

            // 2. নোটিফিকেশন [Switch]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("settings_notif_sound_toggle"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "নোটিফিকেশন",
                    color = InkPrimary,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = notificationSound,
                    onCheckedChange = { checked ->
                        notificationSound = checked
                        HishabiPrefsStore.update(context) { it.copy(notificationSoundEnabled = checked) }
                        onUpdateSettings(
                            mapOf("notificationsEnabled" to checked),
                            if (checked) "নোটিফিকেশন চালু হয়েছে" else "নোটিফিকেশন বন্ধ হয়েছে",
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureWhite,
                        checkedTrackColor = SettingsSwitchBlue,
                        uncheckedThumbColor = PureWhite,
                        uncheckedTrackColor = Color(0xFFCBD5E1),
                    ),
                )
            }
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(start = 54.dp))

            // 3. ভাষা পরিবর্তন (বাংলা ⌄)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLanguageDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("settings_language_row"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = InkPrimary,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "ভাষা পরিবর্তন",
                    color = InkPrimary,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (data.profile.language == "en") "English" else "বাংলা",
                        color = InkSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = InkSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(start = 54.dp))

            // 4. ডাটা ব্যাকআপ >
            SettingsMenuRow(
                icon = Icons.Outlined.CloudUpload,
                title = "ডাটা ব্যাকআপ",
                testTag = "settings_backup_row",
                onClick = { showBackupDialog = true },
            )
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(start = 54.dp))

            // 5. সাহায্য ও সাপোর্ট >
            SettingsMenuRow(
                icon = Icons.AutoMirrored.Outlined.HelpOutline,
                title = "সাহায্য ও সাপোর্ট",
                testTag = "settings_support_row",
                onClick = onOpenHelpCenter,
            )
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(start = 54.dp))

            // 6. পারমিশন সেন্টার >
            SettingsMenuRow(
                icon = Icons.Outlined.VerifiedUser,
                title = "পারমিশন সেন্টার",
                testTag = "settings_permission_center_row",
                onClick = onOpenPermissionCenter,
            )
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(start = 54.dp))

            // 7. অ্যাপ সম্পর্কে >
            SettingsMenuRow(
                icon = Icons.Outlined.Info,
                title = "অ্যাপ সম্পর্কে",
                testTag = "settings_about_row",
                onClick = { showAboutDialog = true },
            )
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f), modifier = Modifier.padding(start = 54.dp))

            // 8. লগ আউট >
            SettingsMenuRow(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "লগ আউট",
                testTag = "settings_logout_button",
                onClick = onLogout,
            )
            HorizontalDivider(color = CardBorder.copy(alpha = 0.6f))
        }

        // Bottom Fixed Soft-Red CTA Button: অ্যাকাউন্ট মুছুন
        Surface(
            color = PureWhite,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = { showDeleteAccountDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DenaRedBg,
                    contentColor = Color(0xFFD92D20),
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(50.dp)
                    .testTag("settings_delete_account_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "অ্যাকাউন্ট মুছুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.5.sp,
                )
            }
        }
    }

    // Sub-dialog 1: কাস্টমার ও হিসাব সেটিংস (preserves all existing controls!)
    if (showCustomerSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showCustomerSettingsDialog = false },
            containerColor = PureWhite,
            title = { Text("কাস্টমার ও হিসাব সেটিংস", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CompactToggleRow(
                        title = "টাকার অঙ্কে দশমিক",
                        checked = decimalEnabled,
                        testTag = "settings_decimal_toggle",
                        onCheckedChange = { checked ->
                            decimalEnabled = checked
                            HishabiPrefsStore.update(context) { it.copy(decimalEnabled = checked) }
                        },
                    )
                    CompactToggleRow(
                        title = "ভয়েস অ্যালার্ট",
                        checked = voiceAlertEnabled,
                        testTag = "settings_voice_alert_toggle",
                        onCheckedChange = { checked ->
                            voiceAlertEnabled = checked
                            HishabiPrefsStore.update(context) { it.copy(voiceAlertEnabled = checked) }
                        },
                    )
                    CompactToggleRow(
                        title = "পিন ছাড়া এন্ট্রি রেকর্ড",
                        checked = pinlessEntryEnabled,
                        testTag = "settings_pinless_entry_toggle",
                        onCheckedChange = { checked ->
                            pinlessEntryEnabled = checked
                            HishabiPrefsStore.update(context) { it.copy(pinlessEntryEnabled = checked) }
                        },
                    )
                    HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 6.dp))
                    CompactLinkRow(
                        icon = Icons.Outlined.VerifiedUser,
                        title = "PIN পরিবর্তন ও নিরাপত্তা",
                        subtitle = if (data.profile.pinLockEnabled) "পিন লক সক্রিয়" else "পিন লক বন্ধ",
                        onClick = {
                            showCustomerSettingsDialog = false
                            showPinDialog = true
                        },
                    )
                    CompactLinkRow(
                        icon = Icons.Outlined.Business,
                        title = "মোবাইল নম্বর পরিবর্তন",
                        subtitle = ownerPhone,
                        onClick = {
                            showCustomerSettingsDialog = false
                            showPhoneDialog = true
                        },
                    )
                    CompactLinkRow(
                        icon = Icons.Outlined.Category,
                        title = "ব্যবসার ধরন নির্বাচন",
                        subtitle = data.profile.businessCategory.ifBlank { "চা স্টল" },
                        onClick = {
                            showCustomerSettingsDialog = false
                            onOpenBusinessCategory()
                        },
                    )
                    CompactLinkRow(
                        icon = Icons.Outlined.Business,
                        title = "অ্যাকাউন্টসমূহ ও মাল্টি-ব্যবসা",
                        subtitle = "একাধিক দোকান পরিচালনা",
                        onClick = {
                            showCustomerSettingsDialog = false
                            onOpenMultiBusiness()
                        },
                    )
                    CompactLinkRow(
                        icon = Icons.Outlined.WorkspacePremium,
                        title = "হিসাবি খাতা গোল্ড",
                        subtitle = if (initialPrefs.goldMembershipActive) "সক্রিয় গোল্ড সদস্য" else "প্যাকেজ ও সুবিধা দেখুন",
                        onClick = {
                            showCustomerSettingsDialog = false
                            onOpenGoldMembership()
                        },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCustomerSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsEmerald),
                ) { Text("সম্পন্ন") }
            },
        )
    }

    // Sub-dialog 2: ভাষা পরিবর্তন
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = PureWhite,
            title = { Text("ভাষা নির্বাচন করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("bn" to "বাংলা (ডিফল্ট)", "en" to "English").forEach { (code, label) ->
                        val isSelected = (data.profile.language.ifBlank { "bn" }) == code
                        Surface(
                            onClick = {
                                showLanguageDialog = false
                                onUpdateSettings(mapOf("language" to code), "ভাষা পরিবর্তন করা হয়েছে")
                            },
                            color = if (isSelected) Color(0xFFECFDF5) else PureWhite,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) HisabGreen else CardBorder),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) SettingsEmerald else InkPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("বন্ধ করুন") }
            },
        )
    }

    // Sub-dialog 3: ডাটা ব্যাকআপ
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            containerColor = PureWhite,
            title = { Text("ডাটা ব্যাকআপ ও সিঙ্ক", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CompactLinkRow(
                        icon = Icons.Default.CloudDone,
                        title = "JSON ফাইল এক্সপোর্ট / শেয়ার",
                        subtitle = if (isCloudConnected) "ক্লাউড সিঙ্ক সক্রিয়" else "অফলাইন লোকাল স্টোরেজ সক্রিয়",
                        onClick = {
                            showBackupDialog = false
                            val backup = JSONObject().apply {
                                put("businessName", data.profile.businessName)
                                put("ownerName", data.profile.ownerName)
                                put("exportedAt", System.currentTimeMillis())
                                put("customers", JSONArray(data.customers.map { c ->
                                    JSONObject().put("name", c.name).put("phone", c.phone).put("balance", c.balance)
                                }))
                                put("transactionsCount", data.transactions.size)
                            }
                            val file = File(context.cacheDir, "hisaber_khata_backup.json").apply {
                                writeText(backup.toString(2))
                            }
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "ডাটা ব্যাকআপ এক্সপোর্ট করুন"))
                        },
                    )
                    if (!isCloudConnected) {
                        CompactLinkRow(
                            icon = Icons.Default.CloudQueue,
                            title = "Google ক্লাউড সংযোগ করুন",
                            subtitle = "ক্লাউড ব্যাকআপ চালু করতে ট্যাপ করুন",
                            onClick = {
                                showBackupDialog = false
                                onConnectGoogleCloud()
                            },
                        )
                    }
                    CompactLinkRow(
                        icon = Icons.Default.CloudQueue,
                        title = "পেমেন্ট ও SMS সার্ভার (Demo / Backend)",
                        subtitle = if (isServerMode) "সক্রিয়: $savedBackendUrl" else "ডেমো মোড সক্রিয়",
                        onClick = {
                            showBackupDialog = false
                            showBackendDialog = true
                        },
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBackupDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsEmerald),
                ) { Text("ঠিক আছে") }
            },
        )
    }

    // Sub-dialog 4: অ্যাপ সম্পর্কে
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = PureWhite,
            title = { Text("হিসাবের খাতা সম্পর্কে", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "হিসাবের খাতা — আপনার দোকানের দৈনিক বাকি, পেমেন্ট, ক্যাশবাক্স, স্টক এবং কাস্টমার হিসাবের সহজ ডিজিটাল খাতা।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkPrimary,
                    )
                    Text(
                        text = "দোকান: $ownerBusinessName",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                    )
                    Text(
                        text = "নিরাপদ তথ্য সংরক্ষণ ও স্বচ্ছ পেমেন্ট গেটওয়ে নীতিমালা অনুসরণ করে পরিচালিত।",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsEmerald),
                ) { Text("ঠিক আছে") }
            },
        )
    }

    // Sub-dialog 5: অ্যাকাউন্ট মুছুন Confirmation
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = PureWhite,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = DenaRed,
                    modifier = Modifier.size(30.dp),
                )
            },
            title = { Text("অ্যাকাউন্ট মুছবেন?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "আপনি কি নিশ্চিতভাবে লোকাল সেশন ও সংরক্ষিত প্রোফাইল তথ্য মুছে লগ আউট করতে চান?",
                    color = InkSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DenaRed),
                ) { Text("হ্যাঁ, মুছে ফেলুন", color = PureWhite, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("বাতিল") }
            },
        )
    }

    if (showPhoneDialog) {
        var phone by remember { mutableStateOf(data.profile.phone) }
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            containerColor = PureWhite,
            title = { Text("মোবাইল নম্বর পরিবর্তন", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { raw -> phone = raw.filter { it.isDigit() || it == '+' }.take(15) },
                    label = { Text("নতুন মোবাইল নম্বর") },
                    placeholder = { Text("01XXXXXXXXX") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phone.trim().length >= 10) {
                            onUpdateSettings(
                                mapOf("phone" to phone.trim().take(20)),
                                "মোবাইল নম্বর আপডেট হয়েছে",
                            )
                            showPhoneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsEmerald),
                ) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false }) { Text("বাতিল") }
            },
        )
    }

    if (showPinDialog) {
        var pin by remember { mutableStateOf("") }
        var enablePin by remember { mutableStateOf(data.profile.pinLockEnabled) }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            containerColor = PureWhite,
            title = { Text("PIN পরিবর্তন ও নিরাপত্তা", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("পিন লক চালু রাখুন", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = enablePin,
                            onCheckedChange = { enablePin = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = SettingsEmerald),
                        )
                    }
                    if (enablePin) {
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("নতুন ৪ ডিজিট পিন") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!enablePin) {
                            onUpdateSettings(mapOf("pinLockEnabled" to false, "pinHash" to ""), "পিন লক বন্ধ করা হয়েছে")
                            showPinDialog = false
                        } else if (pin.length == 4) {
                            onUpdateSettings(
                                mapOf("pinLockEnabled" to true, "pinHash" to Formatters.sha256Hex(pin)),
                                "৪ ডিজিট পিন সেট হয়েছে",
                            )
                            showPinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SettingsEmerald),
                ) { Text("সংরক্ষণ") }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("বাতিল") }
            },
        )
    }

    if (showBackendDialog) {
        BackendServerConfigDialog(
            initialUrl = savedBackendUrl,
            initialForceDemo = forceDemoMode,
            onDismiss = { showBackendDialog = false },
            onSave = { url, demo ->
                PaymentGatewayConfig.saveBackendConfig(context, url, demo)
                savedBackendUrl = PaymentGatewayConfig.getBackendServerUrl(context)
                forceDemoMode = PaymentGatewayConfig.isForceDemoMode(context)
                showBackendDialog = false
            },
        )
    }
}

@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkPrimary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = InkPrimary,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = InkMuted,
        )
    }
}

@Composable
private fun CompactToggleRow(
    title: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = InkPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = SettingsEmerald,
                uncheckedThumbColor = PureWhite,
                uncheckedTrackColor = Color(0xFFCBD5E1),
            ),
        )
    }
}

@Composable
private fun CompactLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SettingsEmerald, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = InkPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = InkSecondary, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
    }
}
