package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.example.ui.components.GoldBenefitsChecklist
import com.example.ui.components.HisabiGoldCrownHeroBanner
import com.example.ui.components.WhitePaymentBrandLogo
import com.example.ui.theme.BrandBkashPink
import com.example.ui.theme.BrandNagadRedOrange
import com.example.ui.theme.BrandNagadYellow
import com.example.ui.theme.BrandRocketPurple
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.util.Formatters
import com.example.util.GoldPackageManager
import com.example.util.GoldPackagePlan
import com.example.util.HishabiPrefsStore

private val GoldPkgGreenCta = Color(0xFF0A9E3C)
private val GoldPkgBestSellerRed = Color(0xFFE11D2E)
private val GoldPkgBorder = Color(0xFFDDA114)

/**
 * Panel 5: গোল্ড প্যাকেজ সমূহ (Gold Packages Selection Screen).
 * Matches Panel 4 ("হিসাবি খাতা গোল্ড") header, crown graphics, and layout while displaying
 * the 3 side-by-side Gold packages (১ মাস ৳ ৮৯, ২ মাস ৳ ১৫০ বেস্ট সেলার, ৩ মাস ৳ ২১০).
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            effectivePrefs.goldPackageId.ifBlank { GoldPackagePlan.GOLD_2M.id }
        )
    }
    var selectedProvider by rememberSaveable {
        mutableStateOf(
            effectivePrefs.preferredPaymentProvider.takeIf { it in listOf("bkash", "nagad", "rocket") } ?: "bkash"
        )
    }
    var showPaymentSheet by rememberSaveable { mutableStateOf(false) }

    val selectedPlan = GoldPackagePlan.fromIdOrDays(selectedPlanId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureWhite),
    ) {
        // Clean White Top Header matching Panel 4 & Panel 5
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
                    modifier = Modifier.testTag("gold_back_button"),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পিছনে যান",
                        tint = InkPrimary,
                    )
                }

                Text(
                    text = "গোল্ড প্যাকেজ সমূহ",
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
                        border = BorderStroke(1.dp, GoldPkgBorder),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("gold_my_package_button"),
                    ) {
                        Text(
                            text = "আমার প্যাকেজ",
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
            // Shared Golden Crown Hero Banner matching Panel 4
            HisabiGoldCrownHeroBanner(
                modifier = Modifier.testTag("gold_benefits_card"),
            )

            // Shared 5 Green Checkmark Benefits List matching Panel 4 & Panel 5
            GoldBenefitsChecklist()

            // Three Side-by-Side Gold Package Cards with Top Space for "বেস্ট সেলার" Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                GoldPackageSideBySideCard(
                    durationLabelBn = "১ মাস",
                    plan = GoldPackagePlan.GOLD_1M,
                    selected = selectedPlan == GoldPackagePlan.GOLD_1M,
                    isBestSeller = false,
                    onSelect = { selectedPlanId = GoldPackagePlan.GOLD_1M.id },
                    onBuyClick = {
                        selectedPlanId = GoldPackagePlan.GOLD_1M.id
                        showPaymentSheet = true
                    },
                    modifier = Modifier.weight(1f),
                )

                GoldPackageSideBySideCard(
                    durationLabelBn = "২ মাস",
                    plan = GoldPackagePlan.GOLD_2M,
                    selected = selectedPlan == GoldPackagePlan.GOLD_2M,
                    isBestSeller = true,
                    onSelect = { selectedPlanId = GoldPackagePlan.GOLD_2M.id },
                    onBuyClick = {
                        selectedPlanId = GoldPackagePlan.GOLD_2M.id
                        showPaymentSheet = true
                    },
                    modifier = Modifier.weight(1f),
                )

                GoldPackageSideBySideCard(
                    durationLabelBn = "৩ মাস",
                    plan = GoldPackagePlan.GOLD_3M,
                    selected = selectedPlan == GoldPackagePlan.GOLD_3M,
                    isBestSeller = false,
                    onSelect = { selectedPlanId = GoldPackagePlan.GOLD_3M.id },
                    onBuyClick = {
                        selectedPlanId = GoldPackagePlan.GOLD_3M.id
                        showPaymentSheet = true
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            // Shield Security Assurance Line ("নিরাপদ ও নির্ভরযোগ্য সেবা")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF1F2937),
                    modifier = Modifier.size(19.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "নিরাপদ ও নির্ভরযোগ্য সেবা",
                    color = InkPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Bottom Green CTA Button ("এখনই কিনুন")
        Surface(
            color = PureWhite,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = { showPaymentSheet = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPkgGreenCta,
                    contentColor = PureWhite,
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(52.dp)
                    .testTag("gold_subscribe_button"),
            ) {
                Text(
                    text = "এখনই কিনুন",
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    // Payment Provider Selection Sheet (বিকাশ / নগদ / রকেট) -> Authorized External Payment Flow
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            containerColor = PureWhite,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "পেমেন্ট পদ্ধতি নির্বাচন করুন",
                            color = InkPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${selectedPlan.bannerTitleBn} • ${selectedPlan.priceDisplayBn}",
                            color = GoldPkgGreenCta,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }

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

                Surface(
                    color = Color(0xFFECFDF3),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        GoldInfoBulletRow("পেমেন্ট সফল হলে গোল্ড প্যাকেজ চালু হবে।")
                        GoldInfoBulletRow("অফিশিয়াল গেটওয়ে পেজে নিরাপদ পেমেন্ট সম্পন্ন করুন।")
                    }
                }

                Button(
                    onClick = {
                        showPaymentSheet = false
                        onProceedToPayment(selectedPlan, selectedProvider)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPkgGreenCta),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("gold_confirm_payment_provider_button"),
                ) {
                    Text(
                        text = "${selectedPlan.priceDisplayBn} পেমেন্ট করুন",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoldPackageSideBySideCard(
    durationLabelBn: String,
    plan: GoldPackagePlan,
    selected: Boolean,
    isBestSeller: Boolean,
    onSelect: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgBrush = when {
        isBestSeller -> Brush.verticalGradient(listOf(Color(0xFFFFF59D), Color(0xFFF6C230)))
        selected -> Brush.verticalGradient(listOf(Color(0xFFFFFBE6), Color(0xFFFDE68A)))
        else -> Brush.verticalGradient(listOf(Color(0xFFFFFDF4), Color(0xFFFEF3C7)))
    }
    val borderColor = when {
        selected -> GoldPkgGreenCta
        isBestSeller -> Color(0xFFC88A00)
        else -> GoldPkgBorder
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        Card(
            onClick = onSelect,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(if (selected) 2.2.dp else 1.2.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isBestSeller || selected) 4.dp else 1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gold_plan_${plan.id}"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgBrush)
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = if (isBestSeller) 18.dp else 14.dp,
                        bottom = 10.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = durationLabelBn,
                    color = InkPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "৳ ${Formatters.toBanglaDigits(plan.priceTaka.toString())}",
                    color = InkPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBestSeller) Color(0xFFD97706) else Color(0xFFE5B11B),
                        contentColor = if (isBestSeller) PureWhite else Color(0xFF1F1600),
                    ),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .testTag("gold_buy_${plan.id}"),
                ) {
                    Text(
                        text = "কিনুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }

        if (isBestSeller) {
            Surface(
                color = GoldPkgBestSellerRed,
                shape = RoundedCornerShape(50),
                shadowElevation = 2.dp,
                modifier = Modifier.offset(y = (-11).dp),
            ) {
                Text(
                    text = "বেস্ট সেলার",
                    color = PureWhite,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                )
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
            width = if (selected) 2.5.dp else 1.dp,
            color = if (selected) GoldPkgGreenCta else Color.Transparent,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .height(74.dp)
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
                    modifier = Modifier.size(30.dp),
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = labelBn,
                    color = PureWhite,
                    fontSize = 14.5.sp,
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
                        tint = GoldPkgGreenCta,
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
            tint = GoldPkgGreenCta,
            modifier = Modifier.size(17.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = InkPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
