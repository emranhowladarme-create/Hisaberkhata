package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GoldCrownIllustration
import com.example.ui.theme.DenaRed
import com.example.ui.theme.DenaRedBg
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.util.Formatters
import com.example.util.GoldPackageManager
import com.example.util.HishabiPrefsStore
import kotlinx.coroutines.delay

private val MyGoldEmerald = Color(0xFF048B44)
private val MyGoldPriceRed = Color(0xFFE11D2E)
private val MyGoldCreamBg = Color(0xFFFDF6E4)
private val MyGoldCreamBorder = Color(0xFFF1DDA8)

@Composable
fun MyGoldPackageScreen(
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onBuyOrRenewPackage: () -> Unit,
) {
    val context = LocalContext.current
    val prefsState by HishabiPrefsStore.prefsFlow.collectAsState()
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        GoldPackageManager.syncExpiryIfNeeded(context, System.currentTimeMillis())
        while (true) {
            delay(60_000L)
            val tick = System.currentTimeMillis()
            nowMillis = tick
            GoldPackageManager.syncExpiryIfNeeded(context, tick)
        }
    }

    val effectivePrefs = remember(prefsState) { HishabiPrefsStore.load(context) }
    val snapshot = remember(effectivePrefs, nowMillis) {
        GoldPackageManager.computeSnapshot(effectivePrefs, nowMillis)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
    ) {
        // Emerald Header matching Reference Screen 3
        Surface(color = MyGoldEmerald, shadowElevation = 2.dp) {
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
                    modifier = Modifier.testTag("my_gold_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = PureWhite,
                    )
                }
                Text(
                    text = "আমার গোল্ড প্যাকেজ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 42.dp),
                )
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Golden Crown Summary Banner showing selected/active package name & price in Bengali + টাকা
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFFD49A08)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_gold_summary_banner"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFE57F), Color(0xFFF6C230), Color(0xFFE09F12))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GoldCrownIllustration(modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = snapshot.plan.bannerTitleBn,
                            color = Color(0xFF2B1600),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = snapshot.plan.priceDisplayBn,
                            color = MyGoldPriceRed,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            // Automatic Daily Countdown Card ("অবশিষ্ট দিন" + Large Countdown or Expired State)
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.4.dp,
                    color = when {
                        snapshot.isActive -> Color(0xFF7AD49E)
                        snapshot.isExpired -> DenaRed.copy(alpha = 0.45f)
                        else -> MyGoldCreamBorder
                    },
                ),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        snapshot.isActive -> Color(0xFFF2FBF5)
                        snapshot.isExpired -> DenaRedBg
                        else -> MyGoldCreamBg
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_gold_countdown_card"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (snapshot.isExpired) DenaRed else MyGoldEmerald,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "অবশিষ্ট দিন",
                            color = if (snapshot.isExpired) DenaRed else Color(0xFF065F46),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    when {
                        snapshot.isActive -> {
                            Text(
                                text = snapshot.remainingDaysBangla,
                                color = MyGoldEmerald,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("my_gold_remaining_days_text"),
                            )
                            val progress = (snapshot.remainingDays.toFloat() / snapshot.plan.durationDays.coerceAtLeast(1))
                                .coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                color = MyGoldEmerald,
                                trackColor = HisabGreenSoft,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )
                        }
                        snapshot.isExpired -> {
                            Text(
                                text = "০ দিন",
                                color = DenaRed,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("my_gold_remaining_days_text"),
                            )
                            Text(
                                text = "প্যাকেজের মেয়াদ শেষ।",
                                color = DenaRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("my_gold_expired_message"),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onBuyOrRenewPackage,
                                colors = ButtonDefaults.buttonColors(containerColor = MyGoldEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("my_gold_renew_button"),
                            ) {
                                Text("পুনরায় প্যাকেজ কিনুন", color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Text(
                                text = "কোনো গোল্ড প্যাকেজ চালু নেই",
                                color = InkPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Button(
                                onClick = onBuyOrRenewPackage,
                                colors = ButtonDefaults.buttonColors(containerColor = MyGoldEmerald),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("my_gold_buy_cta"),
                            ) {
                                Text("গোল্ড প্যাকেজ কিনুন", color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Package Details Card ('গোল্ড প্যাকেজের বিবরণ')
            Card(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MyGoldCreamBorder),
                colors = CardDefaults.cardColors(containerColor = MyGoldCreamBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_gold_details_card"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "গোল্ড প্যাকেজের বিবরণ",
                        color = InkPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HorizontalDivider(color = MyGoldCreamBorder)

                    PackageDetailRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "প্যাকেজের মেয়াদ:",
                        value = snapshot.plan.durationDetailBn,
                        valueColor = InkPrimary,
                    )
                    HorizontalDivider(color = MyGoldCreamBorder.copy(alpha = 0.6f))

                    PackageDetailRow(
                        icon = Icons.Default.LocalOffer,
                        label = "প্যাকেজের মূল্য:",
                        value = snapshot.plan.priceDisplayBn,
                        valueColor = InkPrimary,
                    )
                    HorizontalDivider(color = MyGoldCreamBorder.copy(alpha = 0.6f))

                    PackageDetailRow(
                        icon = Icons.Default.Verified,
                        label = "প্যাকেজের অবস্থা:",
                        value = snapshot.statusTextBn,
                        valueColor = when {
                            snapshot.isActive -> MyGoldEmerald
                            snapshot.isExpired -> DenaRed
                            else -> InkSecondary
                        },
                    )

                    if (snapshot.hasEverPurchased) {
                        HorizontalDivider(color = MyGoldCreamBorder.copy(alpha = 0.6f))
                        PackageDetailRow(
                            icon = Icons.Default.EventAvailable,
                            label = "প্যাকেজ শুরুর তারিখ:",
                            value = Formatters.formatBanglaDate(snapshot.startDateMillis),
                            valueColor = InkPrimary,
                        )
                        HorizontalDivider(color = MyGoldCreamBorder.copy(alpha = 0.6f))
                        PackageDetailRow(
                            icon = Icons.Default.EventBusy,
                            label = "মেয়াদ শেষ হওয়ার তারিখ:",
                            value = Formatters.formatBanglaDate(snapshot.expiryDateMillis),
                            valueColor = if (snapshot.isExpired) DenaRed else InkPrimary,
                        )
                    }
                }
            }

            // Green Success Banner when Package is Active
            if (snapshot.isActive) {
                Surface(
                    color = Color(0xFFEAF9EF),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF7AD49E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("my_gold_success_banner"),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MyGoldEmerald),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "সফল",
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "আপনার গোল্ড প্যাকেজ সফলভাবে চালু হয়েছে !",
                            color = Color(0xFF052E16),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBuyOrRenewPackage,
                    border = BorderStroke(1.dp, MyGoldEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("my_gold_change_plan_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = MyGoldEmerald,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("অন্য প্যাকেজ দেখুন / পরিবর্তন করুন", color = MyGoldEmerald, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Full-Width Emerald Home Button ('হোম পেজ যান')
        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = onGoHome,
                colors = ButtonDefaults.buttonColors(containerColor = MyGoldEmerald),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp)
                    .testTag("my_gold_home_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "হোম",
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "হোম পেজ যান",
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun PackageDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MyGoldEmerald,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            color = InkSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
