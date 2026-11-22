package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.EmojiObjects
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DenaRed
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenBorder
import com.example.ui.theme.HisabGreenDark
import com.example.ui.theme.HisabGreenDeep
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.HisabGreenTint
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.TallyCrownGold
import com.example.ui.theme.TallyPromoYellow
import com.example.ui.viewmodel.HisaberAppData
import com.example.util.Formatters

@Composable
fun HisabiPayScreen(
    data: HisaberAppData,
    onOpenNotifications: () -> Unit,
    onOpenHelpCenter: () -> Unit,
    onOpenMultiBusiness: () -> Unit,
    onOpenPaymentGateway: (providerId: String, title: String) -> Unit,
    onOpenQr: () -> Unit,
) {
    var showBalanceAmount by rememberSaveable { mutableStateOf(false) }

    val businessName = data.profile.businessName
        .takeIf { it.isNotBlank() && it != "আমার ব্যবসা" }
        ?: "ইমরান টি স্টল"

    val netAccountBalance = (data.summary.totalIncomeAll - data.summary.totalExpenseAll)
        .coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HisabGreenTint),
    ) {
        // 1. Top Header: Left 'ইনবক্স' & 'সাপোর্ট', Right Business Pill + White Balance Panel
        HisabiPayTopSection(
            businessName = businessName,
            unreadNotifications = data.summary.unreadNotifications,
            netAccountBalance = netAccountBalance,
            showBalanceAmount = showBalanceAmount,
            onToggleBalance = { showBalanceAmount = !showBalanceAmount },
            onInboxClick = onOpenNotifications,
            onSupportClick = onOpenHelpCenter,
            onBusinessClick = onOpenMultiBusiness,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 2. 8 Service Icons in 2 Rows of 4 Grid
            item {
                HisabiPayServicesGrid(
                    onOpenPaymentGateway = onOpenPaymentGateway,
                    onOpenQr = onOpenQr,
                )
            }

            // 3. Middle Promotional Offer Slider Banner
            item {
                HisabiPayPromoSlider(
                    onOpenHelpCenter = onOpenHelpCenter,
                    onOpenQr = onOpenQr,
                    onOpenPaymentGateway = onOpenPaymentGateway,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }

            // 4. Bottom Bangla QR Showcase Banner
            item {
                HisabiBanglaQrShowcaseCard(
                    onOpenQr = onOpenQr,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun HisabiPayTopSection(
    businessName: String,
    unreadNotifications: Int,
    netAccountBalance: Double,
    showBalanceAmount: Boolean,
    onToggleBalance: () -> Unit,
    onInboxClick: () -> Unit,
    onSupportClick: () -> Unit,
    onBusinessClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HisabGreenSoft, HisabGreenTint)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Top Action Row: Left 'ইনবক্স' & 'সাপোর্ট', Right Business Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TopHeaderIconButton(
                    icon = Icons.Outlined.MoveToInbox,
                    label = "ইনবক্স",
                    badgeCount = unreadNotifications,
                    testTag = "hisabi_pay_inbox",
                    onClick = onInboxClick,
                )
                TopHeaderIconButton(
                    icon = Icons.Outlined.HeadsetMic,
                    label = "সাপোর্ট",
                    badgeCount = 0,
                    testTag = "hisabi_pay_support",
                    onClick = onSupportClick,
                )
            }

            Surface(
                color = PureWhite,
                shape = CircleShape,
                border = BorderStroke(1.dp, HisabGreenBorder),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBusinessClick)
                    .testTag("hisabi_pay_business_pill"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SmallCrownMark(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = businessName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "ব্যবসা পরিবর্তন",
                        tint = InkPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Principal White Panel: Left 'একাউন্ট ব্যালেন্স' & Right 'হিসাবি পে' Logo
        Surface(
            color = PureWhite,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, HisabGreenBorder),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onToggleBalance)
                .testTag("hisabi_pay_balance_panel"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(HisabGreen),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "৳",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (showBalanceAmount) {
                                Formatters.formatTaka(netAccountBalance, useBanglaDigits = true)
                            } else {
                                "একাউন্ট ব্যালেন্স"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = if (showBalanceAmount) 18.sp else 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showBalanceAmount) HisabGreenDark else InkPrimary,
                            ),
                        )
                        if (showBalanceAmount) {
                            Text(
                                text = "একাউন্ট ব্যালেন্স (লুকাতে ট্যাপ করুন)",
                                fontSize = 10.5.sp,
                                color = InkSecondary,
                            )
                        }
                    }
                }

                // Right Brand Title: 'হিসাবি পে'
                Text(
                    text = buildAnnotatedString {
                        append("হিসাবি পে")
                        withStyle(
                            SpanStyle(
                                fontSize = 11.sp,
                                baselineShift = BaselineShift.Superscript,
                            )
                        ) {
                            append("TM")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HisabGreen,
                    ),
                    modifier = Modifier.testTag("hisabi_pay_brand_title"),
                )
            }
        }
    }
}

@Composable
private fun TopHeaderIconButton(
    icon: ImageVector,
    label: String,
    badgeCount: Int,
    testTag: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = HisabGreenDeep,
                modifier = Modifier.size(22.dp),
            )
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .background(DenaRed, CircleShape)
                        .padding(horizontal = 4.dp, vertical = 0.5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        color = PureWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = HisabGreenDeep,
            maxLines = 1,
        )
    }
}

@Composable
private fun SmallCrownMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.10f, h * 0.78f)
            lineTo(w * 0.08f, h * 0.28f)
            lineTo(w * 0.32f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.68f, h * 0.50f)
            lineTo(w * 0.92f, h * 0.28f)
            lineTo(w * 0.90f, h * 0.78f)
            close()
        }
        drawPath(path, color = TallyCrownGold)
    }
}

private data class PayServiceItem(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color,
    val testTag: String,
    val onClick: () -> Unit,
)

@Composable
private fun HisabiPayServicesGrid(
    onOpenPaymentGateway: (providerId: String, title: String) -> Unit,
    onOpenQr: () -> Unit,
) {
    val services = listOf(
        PayServiceItem("ব্যাংক ট্রান্সফার", Icons.Outlined.AccountBalance, HisabGreen, "pay_bank_transfer") {
            onOpenPaymentGateway("bkash", "ব্যাংক ট্রান্সফার")
        },
        PayServiceItem("ওয়ালেট ট্রান্সফার", Icons.Outlined.AccountBalanceWallet, HisabGreenDark, "pay_wallet_transfer") {
            onOpenPaymentGateway("bkash", "ওয়ালেট ট্রান্সফার")
        },
        PayServiceItem("মোবাইল রিচার্জ", Icons.Outlined.PhoneAndroid, Color(0xFF059669), "pay_mobile_recharge") {
            onOpenPaymentGateway("bkash", "মোবাইল রিচার্জ")
        },
        PayServiceItem("টাকা যোগ করুন", Icons.Outlined.AddCard, HisabGreen, "pay_add_money") {
            onOpenPaymentGateway("bkash", "টাকা যোগ করুন")
        },
        PayServiceItem("হিসাবি ট্রান্সফার", Icons.Outlined.SwapHoriz, HisabGreenDark, "pay_hisabi_transfer") {
            onOpenPaymentGateway("bkash", "হিসাবি ট্রান্সফার")
        },
        PayServiceItem("কালেকশন", Icons.Outlined.Payments, HisabGreen, "pay_collection") {
            onOpenPaymentGateway("bkash", "কালেকশন")
        },
        PayServiceItem("কিউআর কোড", Icons.Outlined.QrCode2, HisabGreenDeep, "pay_qr_code", onOpenQr),
        PayServiceItem("বিল পেমেন্ট", Icons.Outlined.EmojiObjects, Color(0xFF16A34A), "pay_bill_payment") {
            onOpenPaymentGateway("bkash", "বিল পেমেন্ট")
        },
    )

    Surface(
        color = PureWhite,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(0.8.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            services.chunked(4).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    rowItems.forEach { item ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = item.onClick)
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                                .testTag(item.testTag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(HisabGreenSoft.copy(alpha = 0.7f))
                                    .border(1.dp, HisabGreenBorder, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconTint,
                                    modifier = Modifier.size(25.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkPrimary,
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PromoOfferSlide(
    val title: String,
    val ctaLabel: String,
    val onClick: () -> Unit,
)

@Composable
private fun HisabiPayPromoSlider(
    onOpenHelpCenter: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenPaymentGateway: (providerId: String, title: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val slides = listOf(
        PromoOfferSlide("QR পেমেন্টে\nসমস্যা হলেই জানান", "সাবমিট", onOpenHelpCenter),
        PromoOfferSlide("সাশ্রয়ী হিসাবি\nবাংলা QR-এ পেমেন্ট নিন", "QR দেখুন", onOpenQr),
        PromoOfferSlide("বিকাশ, নগদ ও রকেট\nথেকে দ্রুত টাকা যোগ", "টাকা যোগ করুন") {
            onOpenPaymentGateway("bkash", "টাকা যোগ করুন")
        },
        PromoOfferSlide("মোবাইল রিচার্জ ও\nবিল পেমেন্ট মুহূর্তেই", "পেমেন্ট করুন") {
            onOpenPaymentGateway("bkash", "বিল পেমেন্ট")
        },
    )
    val pagerState = rememberPagerState(pageCount = { slides.size })

    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .testTag("hisabi_pay_promo_slider"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(HisabGreen, HisabGreenDark)
                    )
                ),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val slide = slides[page]
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = slide.onClick)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Mini QR + Badge Graphic on the left
                    PromoSlideGraphic(modifier = Modifier.size(78.dp))

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = slide.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.5.sp,
                                lineHeight = 22.sp,
                            ),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = TallyPromoYellow,
                            shape = CircleShape,
                        ) {
                            Text(
                                text = slide.ctaLabel,
                                color = InkPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                            )
                        }
                    }
                }
            }

            // Bottom Pager Dots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(slides.size) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (active) 7.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) PureWhite else PureWhite.copy(alpha = 0.45f))
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoSlideGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // White QR Card on right
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(w * 0.28f, h * 0.12f),
            size = Size(w * 0.66f, h * 0.76f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
        )
        // Mini green QR pattern inside card
        val qLeft = w * 0.36f
        val qTop = h * 0.22f
        val cell = w * 0.14f
        drawRect(
            color = HisabGreen,
            topLeft = Offset(qLeft, qTop),
            size = Size(cell, cell),
            style = Stroke(width = w * 0.025f),
        )
        drawRect(
            color = HisabGreen,
            topLeft = Offset(qLeft + w * 0.34f, qTop),
            size = Size(cell, cell),
            style = Stroke(width = w * 0.025f),
        )
        drawRect(
            color = HisabGreen,
            topLeft = Offset(qLeft, qTop + h * 0.36f),
            size = Size(cell, cell),
            style = Stroke(width = w * 0.025f),
        )
        drawRect(
            color = HisabGreenDark,
            topLeft = Offset(qLeft + w * 0.20f, qTop + h * 0.20f),
            size = Size(cell * 1.1f, cell * 1.1f),
        )

        // Bright Accent Coin / Shield Badge overlapping top-left
        drawCircle(
            color = PureWhite,
            radius = w * 0.23f,
            center = Offset(w * 0.26f, h * 0.34f),
        )
        drawCircle(
            color = TallyPromoYellow,
            radius = w * 0.19f,
            center = Offset(w * 0.26f, h * 0.34f),
        )
        drawCircle(
            color = HisabGreenDeep,
            radius = w * 0.03f,
            center = Offset(w * 0.26f, h * 0.41f),
        )
        drawLine(
            color = HisabGreenDeep,
            start = Offset(w * 0.26f, h * 0.24f),
            end = Offset(w * 0.26f, h * 0.35f),
            strokeWidth = w * 0.04f,
        )
    }
}

@Composable
private fun HisabiBanglaQrShowcaseCard(
    onOpenQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onOpenQr,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, HisabGreenBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hisabi_pay_bangla_qr_banner"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Merchant & Phone QR Scan Vector Artwork in Emerald & White
            MerchantBanglaQrIllustration(
                modifier = Modifier
                    .width(200.dp)
                    .height(132.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HisabGreenSoft, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "বাজারে এলো সাশ্রয়ী হিসাবই বাংলা কিউআর",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    tint = HisabGreen,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "হিসাবি কিউআর-এ পেমেন্ট নিন বিকাশ, নগদ, রকেটসহ সকল ব্যাংক অ্যাপ থেকে",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = InkSecondary,
                        lineHeight = 20.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun MerchantBanglaQrIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft Green Arch Backdrop
        drawArc(
            color = HisabGreenSoft.copy(alpha = 0.7f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.14f, h * 0.04f),
            size = Size(w * 0.72f, h * 1.4f),
        )

        // Store Shelf Grid Lines
        drawLine(
            color = HisabGreenBorder.copy(alpha = 0.55f),
            start = Offset(w * 0.18f, h * 0.40f),
            end = Offset(w * 0.82f, h * 0.40f),
            strokeWidth = 2f,
        )
        drawLine(
            color = HisabGreenBorder.copy(alpha = 0.55f),
            start = Offset(w * 0.14f, h * 0.68f),
            end = Offset(w * 0.86f, h * 0.68f),
            strokeWidth = 3f,
        )

        // Merchant Figure (Left)
        // Head
        drawCircle(
            color = Color(0xFFF6C8B6),
            radius = h * 0.09f,
            center = Offset(w * 0.34f, h * 0.28f),
        )
        // Hair
        drawArc(
            color = Color(0xFF1F2937),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.28f, h * 0.16f),
            size = Size(w * 0.12f, h * 0.12f),
        )
        // Green Shirt Torso
        drawRoundRect(
            color = HisabGreen,
            topLeft = Offset(w * 0.25f, h * 0.39f),
            size = Size(w * 0.18f, h * 0.32f),
            cornerRadius = CornerRadius(14f, 14f),
        )

        // Countertop QR Standee (Center)
        drawRoundRect(
            color = HisabGreenDark,
            topLeft = Offset(w * 0.44f, h * 0.50f),
            size = Size(w * 0.14f, h * 0.26f),
            cornerRadius = CornerRadius(8f, 8f),
        )
        drawRect(
            color = PureWhite,
            topLeft = Offset(w * 0.46f, h * 0.54f),
            size = Size(w * 0.10f, h * 0.16f),
        )

        // Customer Smartphone Scanning QR (Right)
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.63f, h * 0.22f),
            size = Size(w * 0.20f, h * 0.58f),
            cornerRadius = CornerRadius(16f, 16f),
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(w * 0.65f, h * 0.27f),
            size = Size(w * 0.16f, h * 0.46f),
            cornerRadius = CornerRadius(8f, 8f),
        )
        // QR Code squares inside smartphone screen
        drawRect(
            color = HisabGreen,
            topLeft = Offset(w * 0.68f, h * 0.36f),
            size = Size(w * 0.04f, h * 0.06f),
        )
        drawRect(
            color = HisabGreen,
            topLeft = Offset(w * 0.74f, h * 0.36f),
            size = Size(w * 0.04f, h * 0.06f),
        )
        drawRect(
            color = HisabGreenDark,
            topLeft = Offset(w * 0.68f, h * 0.46f),
            size = Size(w * 0.10f, h * 0.14f),
            style = Stroke(width = 3f),
        )
    }
}
