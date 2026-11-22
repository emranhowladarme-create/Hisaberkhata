package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.DenaRedBg
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenTint
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
    isCloudConnected: Boolean = true,
    onConnectGoogleCloud: () -> Unit = {},
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val initialPrefs = remember { HishabiPrefsStore.load(context) }

    var decimalEnabled by rememberSaveable { mutableStateOf(initialPrefs.decimalEnabled) }
    var notificationSound by rememberSaveable(data.profile.notificationsEnabled) {
        mutableStateOf(data.profile.notificationsEnabled && initialPrefs.notificationSoundEnabled)
    }
    var voiceAlertEnabled by rememberSaveable { mutableStateOf(initialPrefs.voiceAlertEnabled) }
    var pinlessEntryEnabled by rememberSaveable { mutableStateOf(initialPrefs.pinlessEntryEnabled) }

    var showPhoneDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showBackendDialog by remember { mutableStateOf(false) }

    var savedBackendUrl by remember { mutableStateOf(PaymentGatewayConfig.getBackendServerUrl(context)) }
    var forceDemoMode by remember { mutableStateOf(PaymentGatewayConfig.isForceDemoMode(context)) }
    val isServerMode = !forceDemoMode && (savedBackendUrl.startsWith("https://") || savedBackendUrl.startsWith("http://"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9F7)),
    ) {
        // Emerald Top Header matching Panel 2 ('← সেটিংস')
        Surface(color = SettingsEmerald, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(58.dp)
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
                        tint = PureWhite,
                    )
                }
                Text(
                    text = "সেটিংস",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                    ),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Core 6 Settings Rows matching Panel 2 exactly
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    PrimaryToggleSettingRow(
                        title = "টাকার অঙ্কে দশমিক",
                        checked = decimalEnabled,
                        testTag = "settings_decimal_toggle",
                        onCheckedChange = { checked ->
                            decimalEnabled = checked
                            HishabiPrefsStore.update(context) { it.copy(decimalEnabled = checked) }
                        },
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))

                    PrimaryToggleSettingRow(
                        title = "নোটিফিকেশন সাউন্ড",
                        checked = notificationSound,
                        testTag = "settings_notif_sound_toggle",
                        onCheckedChange = { checked ->
                            notificationSound = checked
                            HishabiPrefsStore.update(context) { it.copy(notificationSoundEnabled = checked) }
                            onUpdateSettings(
                                mapOf("notificationsEnabled" to checked),
                                if (checked) "নোটিফিকেশন সাউন্ড চালু হয়েছে" else "নোটিফিকেশন সাউন্ড বন্ধ হয়েছে",
                            )
                        },
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))

                    PrimaryToggleSettingRow(
                        title = "ভয়েস অ্যালার্ট",
                        checked = voiceAlertEnabled,
                        testTag = "settings_voice_alert_toggle",
                        onCheckedChange = { checked ->
                            voiceAlertEnabled = checked
                            HishabiPrefsStore.update(context) { it.copy(voiceAlertEnabled = checked) }
                        },
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))

                    PrimaryToggleSettingRow(
                        title = "মোবাইল নম্বর পরিবর্তন",
                        checked = data.profile.phone.isNotBlank(),
                        testTag = "settings_change_phone_row",
                        onCheckedChange = { showPhoneDialog = true },
                        onRowClick = { showPhoneDialog = true },
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))

                    PrimaryToggleSettingRow(
                        title = "PIN পরিবর্তন",
                        checked = data.profile.pinLockEnabled,
                        testTag = "settings_change_pin_row",
                        onCheckedChange = { showPinDialog = true },
                        onRowClick = { showPinDialog = true },
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))

                    PrimaryToggleSettingRow(
                        title = "পিন ছাড়া এন্ট্রি রেকর্ড",
                        checked = pinlessEntryEnabled,
                        testTag = "settings_pinless_entry_toggle",
                        onCheckedChange = { checked ->
                            pinlessEntryEnabled = checked
                            HishabiPrefsStore.update(context) { it.copy(pinlessEntryEnabled = checked) }
                        },
                    )
                }
            }

            // Secondary Profile, Gold, Backup & Account Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HisabGreenBorder),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    SecondarySettingActionRow(
                        icon = Icons.Outlined.Business,
                        title = "ব্যবসা প্রতিষ্ঠানের তথ্য",
                        subtitle = "${data.profile.businessName.ifBlank { "ইমরান টি স্টল" }} • NID ও নমিনি",
                        onClick = onOpenBusinessInfo,
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    SecondarySettingActionRow(
                        icon = Icons.Outlined.Category,
                        title = "ব্যবসার ধরন নির্বাচন",
                        subtitle = data.profile.businessCategory.ifBlank { "চা স্টল" },
                        onClick = onOpenBusinessCategory,
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    SecondarySettingActionRow(
                        icon = Icons.Outlined.WorkspacePremium,
                        title = "গোল্ড মেম্বারশিপ",
                        subtitle = if (initialPrefs.goldMembershipActive) "সক্রিয় গোল্ড সদস্য" else "১০০ ফ্রি SMS ও অ্যানালিটিক্স পান",
                        onClick = onOpenGoldMembership,
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    SecondarySettingActionRow(
                        icon = Icons.Default.Storefront,
                        title = "অ্যাকাউন্টসমূহ ও মাল্টি-ব্যবসা",
                        subtitle = "একাধিক দোকান পরিচালনা করুন",
                        onClick = onOpenMultiBusiness,
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    SecondarySettingActionRow(
                        icon = Icons.Default.Language,
                        title = "ভাষা (Language)",
                        subtitle = if (data.profile.language == "bn") "বাংলা (নির্বাচিত)" else "English (Selected)",
                        onClick = {
                            val nextLang = if (data.profile.language == "bn") "en" else "bn"
                            onUpdateSettings(mapOf("language" to nextLang), "ভাষা পরিবর্তন করা হয়েছে")
                        },
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    SecondarySettingActionRow(
                        icon = Icons.Outlined.VerifiedUser,
                        title = "অ্যাপ পারমিশন সেন্টার (Permission Center)",
                        subtitle = "Camera, Contacts, Location, Notifications ও Phone অনুমতি",
                        testTag = "settings_permission_center_row",
                        onClick = onOpenPermissionCenter,
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    SecondarySettingActionRow(
                        icon = Icons.Default.CloudQueue,
                        title = "পেমেন্ট ও SMS সার্ভার (Demo / Backend)",
                        subtitle = if (isServerMode) "সার্ভার সক্রিয়: $savedBackendUrl" else "ডেমো মোড সক্রিয়",
                        onClick = { showBackendDialog = true },
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    if (!isCloudConnected) {
                        SecondarySettingActionRow(
                            icon = Icons.Default.CloudQueue,
                            title = "Google ক্লাউড ব্যাকআপ সংযোগ করুন",
                            subtitle = "বর্তমানে লোকাল OTP সেশন চালু • Google দিয়ে সিঙ্ক করুন",
                            onClick = onConnectGoogleCloud,
                        )
                        HorizontalDivider(color = CardBorder.copy(alpha = 0.7f))
                    }
                    SecondarySettingActionRow(
                        icon = Icons.Default.CloudDone,
                        title = "ডাটা ব্যাকআপ ও এক্সপোর্ট",
                        subtitle = if (isCloudConnected) "ক্লাউড সিঙ্ক সক্রিয় • JSON ফাইল শেয়ার" else "অফলাইন স্টোরেজ সক্রিয় • JSON ফাইল শেয়ার",
                        onClick = {
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
                }
            }

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = DenaRedBg, contentColor = DenaRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("settings_logout_button"),
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("লগ আউট", fontWeight = FontWeight.Bold, fontSize = 15.5.sp)
            }
        }
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
private fun PrimaryToggleSettingRow(
    title: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit,
    onRowClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onRowClick != null) Modifier.clickable(onClick = onRowClick) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            color = InkPrimary,
            fontSize = 15.5.sp,
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
private fun SecondarySettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String = "",
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = SettingsEmerald, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = InkPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = InkMuted)
    }
}
