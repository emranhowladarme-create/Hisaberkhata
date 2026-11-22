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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GoldBannerHeaderArtwork
import com.example.ui.components.GoldCrownIllustration
import com.example.ui.components.WhitePaymentBrandLogo
import com.example.ui.theme.BrandBkashPink
import com.example.ui.theme.BrandNagadRedOrange
import com.example.ui.theme.BrandNagadYellow
import com.example.ui.theme.BrandRocketPurple
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.PureWhite
import com.example.util.Formatters
import com.example.util.GoldPackageManager
import com.example.util.GoldPackagePlan
import com.example.util.HishabiPrefsStore

private val GoldScreenEmerald = Color(0xFF048B44)
private val GoldScreenEmeraldDark = Color(0xFF036E35)
private val GoldBannerBorder = Color(0xFFD49A08)
private val GoldPriceRed = Color(0xFFE11D2E)

@Composable
fun GoldMembershipScreen(
    onBack: () -> Unit,
    onOpenMyGoldPackage: () -> Unit = {},
    onProceedToPayment: (plan: GoldPackagePlan, providerId: String) -> Unit,
) {
    val context = LocalContext.current
    val prefsState by HishabiPrefsStore.prefsFlow.collectAsState()
    val effectivePrefs = remember(prefsState) { HishabiPrefsStore.load(context) }
    val snapshot = remember(effectivePrefs) { GoldPackageManager.computeSnapshot(effectivePrefs) }

    var selectedPlanId by rememberSaveable {
        mutableStateOf(
            effectivePrefs.goldPackageId.ifBlank { GoldPackagePlan.GOLD_1M.id }
        )
    }
    var selectedProvider by rememberSaveable {
        mutableStateOf(
            effectivePrefs.preferredPaymentProvider.takeIf { it in listOf("bkash", "nagad", "rocket") } ?: "bkash"
        )
    }

    val selectedPlan = GoldPackagePlan.fromIdOrDays(selectedPlanId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
    ) {
        // Emerald Top Header matching Reference Screen 2
        Surface(color = GoldScreenEmerald, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("gold_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = PureWhite,
                    )
                }

                Text(
                    text = "গোল্ড প্যাকেজ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )

                if (snapshot.hasEverPurchased) {
                    Surface(
                        onClick = onOpenMyGoldPackage,
                        color = PureWhite.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("gold_my_package_button"),
                    ) {
                        Text(
                            text = "আমার প্যাকেজ",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Golden Crown + Laurel Wreath Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, GoldBannerBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gold_benefits_card"),
            ) {
                GoldBannerHeaderArtwork(modifier = Modifier.fillMaxWidth())
            }

            // Three Separate Golden Package Cards
            GoldPackagePlan.entries.forEach { plan ->
                val isSelected = selectedPlan == plan
                GoldPackageOptionCard(
                    plan = plan,
                    selected = isSelected,
                    onClick = { selectedPlanId = plan.id },
                )
            }

            // Emerald Pill Section Header: পেমেন্ট পদ্ধতি নির্বাচন করুন
            Surface(
                color = GoldScreenEmerald,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "পেমেন্ট পদ্ধতি নির্বাচন করুন",
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 14.dp),
                )
            }

            // Branded Payment Method Cards (বিকাশ, নগদ, রকেট)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BrandedPaymentMethodCard(
                    providerId = "bkash",
                    labelBn = "বিকাশ",
                    gradientColors = listOf(Color(0xFFEC1D7B), BrandBkashPink),
                    selected = selectedProvider == "bkash",
                    testTag = "gold_provider_bkash",
                    onClick = {
                        selectedProvider = "bkash"
                        HishabiPrefsStore.update(context) { it.copy(preferredPaymentProvider = "bkash") }
                    },
                    modifier = Modifier.weight(1f),
                )
                BrandedPaymentMethodCard(
                    providerId = "nagad",
                    labelBn = "নগদ",
                    gradientColors = listOf(BrandNagadYellow, BrandNagadRedOrange),
                    selected = selectedProvider == "nagad",
                    testTag = "gold_provider_nagad",
                    onClick = {
                        selectedProvider = "nagad"
                        HishabiPrefsStore.update(context) { it.copy(preferredPaymentProvider = "nagad") }
                    },
                    modifier = Modifier.weight(1f),
                )
                BrandedPaymentMethodCard(
                    providerId = "rocket",
                    labelBn = "রকেট",
                    gradientColors = listOf(Color(0xFFA21CAF), BrandRocketPurple),
                    selected = selectedProvider == "rocket",
                    testTag = "gold_provider_rocket",
                    onClick = {
                        selectedProvider = "rocket"
                        HishabiPrefsStore.update(context) { it.copy(preferredPaymentProvider = "rocket") }
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            // Info Box with Two Green Checkmarks
            Surface(
                color = Color(0xFFECFDF3),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    GoldInfoBulletRow("পেমেন্ট সফল হলে গোল্ড প্যাকেজ চালু হবে।")
                    GoldInfoBulletRow("মেয়াদ শেষ হলে পুনরায় কিনতে পারেন।")
                }
            }
        }

        // Bottom Full-Width Emerald CTA Button ('প্যাকেজ কিনুন')
        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = { onProceedToPayment(selectedPlan, selectedProvider) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldScreenEmerald),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .height(52.dp)
                    .testTag("gold_subscribe_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "প্যাকেজ কিনুন",
                    color = PureWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun GoldPackageOptionCard(
    plan: GoldPackagePlan,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bgBrush = if (selected) {
        Brush.verticalGradient(listOf(Color(0xFFFFFBE0), Color(0xFFFDE68A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFDF0), Color(0xFFFEF3C7)))
    }
    val borderColor = if (selected) GoldScreenEmerald else Color(0xFFE5B82E)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (selected) 2.2.dp else 1.2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 3.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gold_plan_${plan.id}"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GoldCrownIllustration(modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.durationTitleBn,
                        color = InkPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = plan.durationSubtitleBn,
                        color = Color(0xFF374151),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.2.dp)
                        .height(44.dp)
                        .background(Color(0xFFD9B24C)),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "৳${Formatters.toBanglaDigits(plan.priceTaka.toString())}",
                        color = GoldPriceRed,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 27.sp,
                    )
                    Text(
                        text = "টাকা",
                        color = InkPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(GoldScreenEmerald),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "নির্বাচিত প্যাকেজ",
                        tint = PureWhite,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandedPaymentMethodCard(
    providerId: String,
    labelBn: String,
    gradientColors: List<Color>,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (selected) 2.6.dp else 1.dp,
            color = if (selected) GoldScreenEmeraldDark else Color.Transparent,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .height(76.dp)
            .testTag(testTag),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                WhitePaymentBrandLogo(
                    providerId = providerId,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = labelBn,
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(PureWhite),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "$labelBn নির্বাচিত",
                        tint = GoldScreenEmeraldDark,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GoldInfoBulletRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GoldScreenEmerald,
            modifier = Modifier.size(17.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = InkPrimary,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
