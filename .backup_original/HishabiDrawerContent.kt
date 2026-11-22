package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GoldCrownIllustration
import com.example.ui.components.ShopStorefrontAvatarIllustration
import com.example.ui.theme.CardBorder
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TallyCrownGold
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.Formatters
import com.example.util.GoldPackageManager
import com.example.util.HishabiPrefsStore

private val DrawerEmerald = Color(0xFF048B44)
private val DrawerEmeraldDeep = Color(0xFF036B33)
private val DrawerEmeraldLight = Color(0xFF10A854)
private val DrawerRowBg = Color(0xFFF3F6F5)

@Composable
fun HishabiDrawerContent(
    data: HisaberAppData,
    onOpenBusinessInfo: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenReports: (ownerMode: Boolean) -> Unit,
    onOpenSalesLedger: () -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenDueLedger: () -> Unit,
    onOpenCashLedger: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPermissionCenter: () -> Unit = {},
    onOpenGoldMembership: () -> Unit,
    onOpenMyGoldPackage: () -> Unit = onOpenGoldMembership,
    onOpenSupportFaq: () -> Unit,
    onShowStaffView: () -> Unit,
    onShowTransactionLimits: () -> Unit,
    onShowServiceCharge: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val prefs by HishabiPrefsStore.prefsFlow.collectAsState()
    val effectivePrefs = remember(prefs) { HishabiPrefsStore.load(context) }
    val goldSnapshot = remember(effectivePrefs) { GoldPackageManager.computeSnapshot(effectivePrefs) }

    val businessName = data.profile.businessName.ifBlank { "ইমরান টি স্টল" }
    val registeredPhone = effectivePrefs.verifiedPhoneNumber
        .ifBlank { data.profile.phone }
        .trim()
        .ifBlank { "মোবাইল নম্বর যুক্ত করুন" }

    val shopPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            HishabiPrefsStore.persistBusinessLogo(context, uri)
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = PureWhite,
        modifier = Modifier.width(304.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(PureWhite),
        ) {
            // Top Emerald Curved Wave Header matching Reference Screen 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_profile_header"),
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize(),
                ) {
                    val w = size.width
                    val h = size.height

                    // Lighter emerald accent wave rim beneath main wave
                    val accentWave = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.90f)
                        cubicTo(
                            w * 0.70f, h * 1.03f,
                            w * 0.34f, h * 0.80f,
                            0f, h * 0.95f,
                        )
                        close()
                    }
                    drawPath(
                        path = accentWave,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1BC25B), DrawerEmeraldLight)
                        ),
                    )

                    // Main deep emerald header wave
                    val mainWave = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.86f)
                        cubicTo(
                            w * 0.72f, h * 0.98f,
                            w * 0.32f, h * 0.74f,
                            0f, h * 0.90f,
                        )
                        close()
                    }
                    drawPath(
                        path = mainWave,
                        brush = Brush.verticalGradient(
                            colors = listOf(DrawerEmeraldDeep, DrawerEmerald, DrawerEmeraldLight)
                        ),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 24.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        // Circular Shop Profile Picture with zero-permission Photo Picker upload option
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clickable {
                                    shopPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("drawer_shop_photo_upload"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECFDF5))
                                    .border(2.5.dp, PureWhite, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (effectivePrefs.businessLogoPath.isNotBlank()) {
                                    AsyncImage(
                                        model = effectivePrefs.businessLogoPath,
                                        contentDescription = "দোকানের প্রোফাইল ছবি",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    ShopStorefrontAvatarIllustration(modifier = Modifier.fillMaxSize())
                                }
                            }

                            // Small camera/upload badge on bottom-right of avatar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PureWhite)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(DrawerEmerald),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "ছবি আপলোড করুন",
                                    tint = PureWhite,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }

                        // "গোল্ড দিন" / "গোল্ড সক্রিয়" Crown Pill Button at Top-Right
                        Surface(
                            onClick = {
                                if (goldSnapshot.isActive) onOpenMyGoldPackage() else onOpenGoldMembership()
                            },
                            color = if (goldSnapshot.isActive) Color(0xFFFEF3C7) else PureWhite.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.2.dp,
                                if (goldSnapshot.isActive) TallyCrownGold else PureWhite.copy(alpha = 0.80f),
                            ),
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .testTag("drawer_gold_badge"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                GoldCrownIllustration(modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (goldSnapshot.isActive) "গোল্ড সক্রিয়" else "গোল্ড দিন",
                                    color = if (goldSnapshot.isActive) Color(0xFF78350F) else PureWhite,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenBusinessInfo),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = businessName,
                                color = PureWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = "ব্যবসার তথ্য",
                                tint = PureWhite,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("drawer_registered_phone"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(17.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF25D366))
                                    .border(1.dp, PureWhite, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "নিবন্ধিত মোবাইল নম্বর",
                                    tint = PureWhite,
                                    modifier = Modifier.size(10.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = registeredPhone,
                                color = PureWhite.copy(alpha = 0.96f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Scrollable Drawer Menu Body preserving every existing item in order
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                DrawerMenuRow(
                    label = "অ্যাকাউন্টসমূহ",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    testTag = "drawer_accounts",
                    onClick = onOpenAccounts,
                )
                DrawerMenuRow(
                    label = "স্টাফ ভিউ",
                    icon = Icons.Outlined.Groups,
                    testTag = "drawer_staff_view",
                    onClick = onShowStaffView,
                )
                DrawerMenuRow(
                    label = "লেনদেন বিবরণী",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    testTag = "drawer_tx_statement",
                    onClick = { onOpenReports(false) },
                )
                DrawerMenuRow(
                    label = "লেনদেন লিমিট",
                    icon = Icons.Outlined.CreditCard,
                    testTag = "drawer_tx_limits",
                    onClick = onShowTransactionLimits,
                )
                DrawerMenuRow(
                    label = "সার্ভিস চার্জ",
                    icon = Icons.Outlined.Percent,
                    testTag = "drawer_service_charge",
                    onClick = onShowServiceCharge,
                )

                HorizontalDivider(
                    color = CardBorder.copy(alpha = 0.85f),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
                )

                DrawerMenuRow(
                    label = "বেচা-কেনা হিসাব",
                    icon = Icons.Outlined.ShoppingCart,
                    testTag = "drawer_sales_ledger",
                    onClick = onOpenSalesLedger,
                )
                DrawerMenuRow(
                    label = "খরচ",
                    icon = Icons.Outlined.Payments,
                    testTag = "drawer_expenses",
                    onClick = onOpenExpenses,
                )
                DrawerMenuRow(
                    label = "বাকি হিসাব",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    testTag = "drawer_due_ledger",
                    onClick = onOpenDueLedger,
                )
                DrawerMenuRow(
                    label = "ক্যাশ হিসাব",
                    icon = Icons.Outlined.AccountBalanceWallet,
                    testTag = "drawer_cash_ledger",
                    onClick = onOpenCashLedger,
                )
                DrawerMenuRow(
                    label = "মালিকের রিপোর্ট",
                    icon = Icons.Outlined.BarChart,
                    testTag = "drawer_owner_report",
                    onClick = { onOpenReports(true) },
                )
                DrawerMenuRow(
                    label = "অন্যান্য সেটিংস",
                    icon = Icons.Outlined.Settings,
                    testTag = "drawer_other_settings",
                    onClick = onOpenSettings,
                )
                DrawerMenuRow(
                    label = "পারমিশন সেন্টার",
                    icon = Icons.Outlined.Shield,
                    testTag = "drawer_permission_center",
                    onClick = onOpenPermissionCenter,
                )
                DrawerMenuRow(
                    label = "সাপোর্ট সেন্টার",
                    icon = Icons.Outlined.HeadsetMic,
                    testTag = "drawer_support_center",
                    onClick = onOpenSupportFaq,
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Red Logout Row matching Panel 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onLogout)
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                        .testTag("drawer_logout"),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "লগ আউট",
                        tint = Color(0xFFD92D20),
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "লগ আউট",
                        color = Color(0xFFD92D20),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Bottom Curved Green Wave Footer matching Panel 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .navigationBarsPadding(),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val lightWave = Path().apply {
                        moveTo(0f, h * 0.45f)
                        cubicTo(w * 0.28f, 0f, w * 0.68f, h * 0.80f, w, h * 0.20f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(lightWave, color = DrawerEmeraldLight)

                    val darkWave = Path().apply {
                        moveTo(0f, h * 0.65f)
                        cubicTo(w * 0.30f, h * 0.24f, w * 0.68f, h * 0.90f, w, h * 0.36f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(darkWave, color = DrawerEmeraldDeep)
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuRow(
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = DrawerRowBg,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DrawerEmerald,
                modifier = Modifier.size(21.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = InkPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun StaffViewDialog(
    businessName: String,
    customerCount: Int,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var readOnlyStaffMode by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HisabGreenSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Groups, contentDescription = null, tint = DrawerEmerald, modifier = Modifier.size(26.dp))
            }
        },
        title = {
            Text("স্টাফ ভিউ ও অনুমতি", fontWeight = FontWeight.Bold, color = InkPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$businessName দোকানের কর্মচারীদের জন্য শুধু দৈনিক এন্ট্রি দেখার ও নতুন বাকি যোগ করার নিরাপদ ভিউ।",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary,
                )
                Surface(
                    color = HisabGreenTint,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HisabGreenBorder),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("স্টাফ সীমিত মোড", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("মোট কাস্টমার: ${Formatters.toBanglaDigits(customerCount.toString())} জন • ডিলিট লক থাকবে", fontSize = 12.sp, color = InkSecondary)
                        }
                        Switch(
                            checked = readOnlyStaffMode,
                            onCheckedChange = { readOnlyStaffMode = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = DrawerEmerald),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DrawerEmerald),
            ) { Text("ঠিক আছে") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(); onOpenSettings() }) {
                Text("PIN সেটিংস", color = DrawerEmerald)
            }
        },
    )
}

@Composable
fun TransactionLimitDialog(
    dailyUsed: Double,
    monthlyUsed: Double,
    isGoldMember: Boolean,
    onDismiss: () -> Unit,
    onUpgradeGold: () -> Unit,
) {
    val dailyLimit = if (isGoldMember) 100000.0 else 50000.0
    val monthlyLimit = if (isGoldMember) 1000000.0 else 300000.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CreditCard, contentDescription = null, tint = DrawerEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text("লেনদেন লিমিট", fontWeight = FontWeight.Bold, color = InkPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LimitProgressBlock(
                    label = "দৈনিক লেনদেন সীমা",
                    used = dailyUsed,
                    max = dailyLimit,
                )
                LimitProgressBlock(
                    label = "মাসিক লেনদেন সীমা",
                    used = monthlyUsed,
                    max = monthlyLimit,
                )
                if (!isGoldMember) {
                    Surface(
                        color = Color(0xFFFEF7DC),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFF3D478)),
                    ) {
                        Text(
                            text = "গোল্ড মেম্বারশিপ নিলে দৈনিক ও মাসিক লিমিট দ্বিগুণ হবে।",
                            fontSize = 12.sp,
                            color = Color(0xFF78350F),
                            modifier = Modifier.padding(10.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DrawerEmerald),
            ) { Text("বুঝেছি") }
        },
        dismissButton = {
            if (!isGoldMember) {
                TextButton(onClick = { onDismiss(); onUpgradeGold() }) {
                    Text("গোল্ড নিন", color = DrawerEmerald, fontWeight = FontWeight.Bold)
                }
            }
        },
    )
}

@Composable
private fun LimitProgressBlock(label: String, used: Double, max: Double) {
    val progress = (used / max).toFloat().coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
            Text(
                text = "${Formatters.formatTaka(used, true)} / ${Formatters.formatTaka(max, true)}",
                fontSize = 12.sp,
                color = InkSecondary,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            color = DrawerEmerald,
            trackColor = HisabGreenSoft,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
fun ServiceChargeDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Verified, contentDescription = null, tint = DrawerEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text("সার্ভিস চার্জ তালিকা", fontWeight = FontWeight.Bold, color = InkPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChargeRow("বাকি-বকেয়া ও ক্যাশবুক এন্ট্রি", "সম্পূর্ণ ফ্রি (৳০)")
                HorizontalDivider(color = CardBorder)
                ChargeRow("হিসাবি বাংলা QR পেমেন্ট গ্রহণ", "০% (ফ্রি)")
                HorizontalDivider(color = CardBorder)
                ChargeRow("বিকাশ / নগদ / রকেট অ্যাড মানি", "১.০% — ১.৫%")
                HorizontalDivider(color = CardBorder)
                ChargeRow("ব্যাংক ট্রান্সফার (BEFTN/NPSB)", "৳১০ প্রতি লেনদেন")
                HorizontalDivider(color = CardBorder)
                ChargeRow("তাগাদা SMS (গোল্ড সদস্য)", "১০০টি মাসিক ফ্রি")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DrawerEmerald),
            ) { Text("ঠিক আছে") }
        },
    )
}

@Composable
private fun ChargeRow(service: String, fee: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(service, fontSize = 13.sp, color = InkPrimary, modifier = Modifier.weight(1f))
        Text(fee, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DrawerEmerald)
    }
}
