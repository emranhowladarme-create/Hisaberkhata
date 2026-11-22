package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GoldBenefitsChecklist
import com.example.ui.components.HisabiGoldCrownHeroBanner
import com.example.ui.theme.CardBorder
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.util.GoldPackageManager
import com.example.util.HishabiPrefsStore

private val HisabiGoldAccent = Color(0xFFF6C230)
private val HisabiGoldSoftBg = Color(0xFFFFFBEB)
private val HisabiGoldBorder = Color(0xFFE5B82E)
private val HisabiGoldGreenSwitch = Color(0xFF0A8F3D)

/**
 * Panel 4: হিসাবি খাতা গোল্ড (Hisabi Khata Gold Overview & Benefits Screen).
 * Uses the same visual language, header, crown artwork, and colors as Panel 5 (গোল্ড প্যাকেজ সমূহ).
 */
@Composable
fun HisabiGoldScreen(
    onBack: () -> Unit,
    onOpenGoldPackages: () -> Unit,
    onOpenMyGoldPackage: () -> Unit = {},
) {
    val context = LocalContext.current
    val prefsState by HishabiPrefsStore.prefsFlow.collectAsState()
    val effectivePrefs = remember(prefsState) { HishabiPrefsStore.load(context) }
    val snapshot = remember(effectivePrefs) { GoldPackageManager.computeSnapshot(effectivePrefs) }

    var autoBackupRestore by rememberSaveable(effectivePrefs.goldAutoRenew) {
        mutableStateOf(effectivePrefs.goldAutoRenew)
    }

    LaunchedEffect(Unit) {
        GoldPackageManager.syncExpiryIfNeeded(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
    ) {
        // Clean White Top Bar matching Panel 4 & Panel 5
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
                    modifier = Modifier.testTag("hisabi_gold_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = InkPrimary,
                    )
                }
                Text(
                    text = "হিসাবি খাতা গোল্ড",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = InkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                    ),
                    modifier = Modifier.weight(1f),
                )
                if (snapshot.hasEverPurchased) {
                    Surface(
                        onClick = onOpenMyGoldPackage,
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, HisabiGoldBorder),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("hisabi_gold_my_package_pill"),
                    ) {
                        Text(
                            text = if (snapshot.isActive) snapshot.remainingDaysBangla else "আমার প্যাকেজ",
                            color = Color(0xFF78350F),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Shared Golden Crown Hero Banner
            HisabiGoldCrownHeroBanner(
                modifier = Modifier.testTag("hisabi_gold_hero_banner"),
            )

            // Package Benefits Checklist
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "প্যাকেজের সুবিধা সমূহ:",
                    color = InkPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                GoldBenefitsChecklist()
            }

            // Auto Backup Restore Card + Gold Info Pill
            Card(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.4.dp, HisabiGoldBorder),
                colors = CardDefaults.cardColors(containerColor = HisabiGoldSoftBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hisabi_gold_auto_restore_card"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "অটো ব্যাকআপ রিস্টোর",
                            color = InkPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                        Switch(
                            checked = autoBackupRestore,
                            onCheckedChange = { enabled ->
                                autoBackupRestore = enabled
                                HishabiPrefsStore.update(context) { it.copy(goldAutoRenew = enabled) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PureWhite,
                                checkedTrackColor = HisabiGoldGreenSwitch,
                                uncheckedThumbColor = PureWhite,
                                uncheckedTrackColor = Color(0xFFCBD5E1),
                            ),
                            modifier = Modifier.testTag("gold_auto_restore_switch"),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFEE57A), HisabiGoldAccent)
                                )
                            )
                            .clickable {
                                if (snapshot.hasEverPurchased) onOpenMyGoldPackage() else onOpenGoldPackages()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B1600)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (snapshot.isActive) {
                                    "গোল্ড মেয়াদ বাকি: ${snapshot.remainingDaysBangla} • বিস্তারিত দেখুন"
                                } else {
                                    "অটো ব্যাকআপ রিস্টোর অন করুন বা নতুন প্যাকেজ কিনুন!"
                                },
                                color = Color(0xFF1F1600),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            if (snapshot.hasEverPurchased) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color(0xFF1F1600),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Current Package Features List ("বর্তমান প্যাকেজের সুবিধা")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "বর্তমান প্যাকেজের সুবিধা",
                    color = InkPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )

                val featureRows = listOf(
                    Icons.Outlined.Sms to "২৫ টি টালি-মেসেজ",
                    Icons.AutoMirrored.Filled.MenuBook to "৫ টি ব্যবসার খাতা",
                    Icons.Outlined.Inventory2 to "স্টক হিসাব",
                    Icons.Outlined.EditNote to "ব্যবসার নোট",
                    Icons.Outlined.NotificationsActive to "গ্রুপ তাগাদা",
                    Icons.AutoMirrored.Outlined.ReceiptLong to "আনলিমিটেড লেনদেন এন্ট্রি",
                    Icons.Outlined.FileDownload to "রিপোর্ট ডাউনলোড",
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    featureRows.forEachIndexed { idx, (icon, label) ->
                        CurrentPackageFeatureRow(icon = icon, label = label)
                        if (idx < featureRows.lastIndex) {
                            HorizontalDivider(
                                color = CardBorder.copy(alpha = 0.55f),
                                modifier = Modifier.padding(start = 34.dp),
                            )
                        }
                    }
                }
            }
        }

        // Bottom Golden CTA Button ("গোল্ড প্যাকেজ দেখি")
        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = onOpenGoldPackages,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF6C230),
                    contentColor = Color(0xFF1F1600),
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp)
                    .testTag("hisabi_gold_view_packages_button"),
            ) {
                Text(
                    text = "গোল্ড প্যাকেজ দেখি",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun CurrentPackageFeatureRow(
    icon: ImageVector,
    label: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HisabGreen,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = InkSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
