package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.HisabGreen
import com.example.ui.theme.HisabGreenDark
import com.example.ui.theme.HisabGreenDeep
import com.example.ui.theme.HisabGreenSoft
import com.example.ui.theme.PureWhite

private val BrightEmerald = Color(0xFF10B981)
private val FreshLimeGreen = Color(0xFF34D399)
private val AccentGold = Color(0xFFFBBF24)
private val AccentGoldDeep = Color(0xFFF59E0B)

/**
 * Custom vector accounting logo featuring an open book, calculator,
 * upward-growing business chart, and yellow curved accent in dark & bright greens.
 */
@Composable
fun AccountingHeroLogo(
    modifier: Modifier = Modifier.size(116.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft light-green halo behind the emblem
        drawCircle(
            color = HisabGreenSoft.copy(alpha = 0.65f),
            radius = w * 0.45f,
            center = Offset(w * 0.50f, h * 0.52f),
        )

        // 1. Calculator card rising behind the left page of the open book
        val calcLeft = w * 0.17f
        val calcTop = h * 0.17f
        val calcW = w * 0.30f
        val calcH = h * 0.36f
        drawRoundRect(
            color = HisabGreenDark,
            topLeft = Offset(calcLeft, calcTop),
            size = Size(calcW, calcH),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        )
        // Calculator screen
        drawRoundRect(
            color = HisabGreenSoft,
            topLeft = Offset(calcLeft + calcW * 0.14f, calcTop + calcH * 0.12f),
            size = Size(calcW * 0.72f, calcH * 0.20f),
            cornerRadius = CornerRadius(w * 0.02f, w * 0.02f),
        )
        // Calculator 3x3 button dots
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val cx = calcLeft + calcW * (0.24f + col * 0.26f)
                val cy = calcTop + calcH * (0.48f + row * 0.16f)
                drawCircle(
                    color = if (row == 2 && col == 2) AccentGold else PureWhite.copy(alpha = 0.9f),
                    radius = w * 0.017f,
                    center = Offset(cx, cy),
                )
            }
        }

        // 2. Upward-growing business chart (bars + growth arrow) on the right side
        val barBaseY = h * 0.57f
        val barWidth = w * 0.068f
        val bars = listOf(
            Triple(w * 0.53f, h * 0.14f, BrightEmerald),
            Triple(w * 0.63f, h * 0.22f, HisabGreen),
            Triple(w * 0.73f, h * 0.31f, HisabGreenDark),
        )
        bars.forEach { (bx, bh, color) ->
            drawRoundRect(
                color = color,
                topLeft = Offset(bx, barBaseY - bh),
                size = Size(barWidth, bh),
                cornerRadius = CornerRadius(w * 0.018f, w * 0.018f),
            )
        }

        // Upward growth trend line + arrowhead
        val trendPath = Path().apply {
            moveTo(w * 0.50f, h * 0.41f)
            lineTo(w * 0.61f, h * 0.32f)
            lineTo(w * 0.68f, h * 0.35f)
            lineTo(w * 0.81f, h * 0.16f)
        }
        drawPath(
            path = trendPath,
            color = BrightEmerald,
            style = Stroke(width = w * 0.036f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        val arrowHead = Path().apply {
            moveTo(w * 0.84f, h * 0.12f)
            lineTo(w * 0.73f, h * 0.15f)
            lineTo(w * 0.82f, h * 0.24f)
            close()
        }
        drawPath(arrowHead, color = BrightEmerald)

        // 3. Open ledger book in front (dark-green outer cover + crisp white/mint pages)
        val bookCover = Path().apply {
            moveTo(w * 0.10f, h * 0.44f)
            quadraticTo(w * 0.30f, h * 0.49f, w * 0.50f, h * 0.56f)
            quadraticTo(w * 0.70f, h * 0.49f, w * 0.90f, h * 0.44f)
            lineTo(w * 0.90f, h * 0.74f)
            quadraticTo(w * 0.70f, h * 0.77f, w * 0.50f, h * 0.84f)
            quadraticTo(w * 0.30f, h * 0.77f, w * 0.10f, h * 0.74f)
            close()
        }
        drawPath(
            path = bookCover,
            brush = Brush.verticalGradient(listOf(HisabGreen, HisabGreenDark)),
        )

        // Left & right open pages
        val leftPage = Path().apply {
            moveTo(w * 0.14f, h * 0.47f)
            quadraticTo(w * 0.32f, h * 0.52f, w * 0.485f, h * 0.58f)
            lineTo(w * 0.485f, h * 0.78f)
            quadraticTo(w * 0.31f, h * 0.72f, w * 0.14f, h * 0.70f)
            close()
        }
        drawPath(leftPage, color = PureWhite)

        val rightPage = Path().apply {
            moveTo(w * 0.86f, h * 0.47f)
            quadraticTo(w * 0.68f, h * 0.52f, w * 0.515f, h * 0.58f)
            lineTo(w * 0.515f, h * 0.78f)
            quadraticTo(w * 0.69f, h * 0.72f, w * 0.86f, h * 0.70f)
            close()
        }
        drawPath(rightPage, color = Color(0xFFECFDF5))

        // Subtle ledger rule lines on left page
        for (i in 0 until 3) {
            val ly = h * (0.56f + i * 0.055f)
            drawLine(
                color = HisabGreenSoft,
                start = Offset(w * 0.19f, ly - h * 0.02f),
                end = Offset(w * 0.43f, ly + h * 0.02f),
                strokeWidth = w * 0.018f,
                cap = StrokeCap.Round,
            )
        }

        // 4. Sweeping golden-yellow curved accent under/around the book
        val yellowSwoosh = Path().apply {
            moveTo(w * 0.08f, h * 0.77f)
            cubicTo(
                w * 0.32f, h * 0.94f,
                w * 0.72f, h * 0.93f,
                w * 0.93f, h * 0.69f,
            )
        }
        drawPath(
            path = yellowSwoosh,
            brush = Brush.horizontalGradient(listOf(AccentGoldDeep, AccentGold)),
            style = Stroke(width = w * 0.052f, cap = StrokeCap.Round),
        )
    }
}

/**
 * Authentic 4-color Google 'G' mark drawn on Canvas.
 */
@Composable
fun GoogleGLogo(
    modifier: Modifier = Modifier.size(20.dp),
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.21f
        val pad = strokeWidth * 0.55f
        val arcSize = Size(size.width - pad * 2f, size.height - pad * 2f)
        val topLeft = Offset(pad, pad)

        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -148f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Yellow left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 148f,
            sweepAngle = 68f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 36f,
            sweepAngle = 114f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Blue right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -8f,
            sweepAngle = 52f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
        )
        // Blue horizontal bar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width - pad * 0.45f, size.height * 0.50f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Butt,
        )
    }
}

/**
 * Dark-green curved footer with bright-green wave accents and white Bengali tagline.
 */
@Composable
fun LoginCurvedFooter(
    tagline: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Bright-green accent wave layer
            val brightWave = Path().apply {
                moveTo(0f, h * 0.36f)
                cubicTo(
                    w * 0.28f, h * 0.04f,
                    w * 0.68f, h * 0.52f,
                    w, h * 0.14f,
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = brightWave,
                brush = Brush.horizontalGradient(listOf(BrightEmerald, FreshLimeGreen, HisabGreen)),
            )

            // Secondary translucent emerald wave highlight
            val midWave = Path().apply {
                moveTo(0f, h * 0.48f)
                cubicTo(
                    w * 0.36f, h * 0.18f,
                    w * 0.72f, h * 0.58f,
                    w, h * 0.28f,
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = midWave,
                color = HisabGreen.copy(alpha = 0.82f),
            )

            // Primary dark-green curved footer body
            val darkFooter = Path().apply {
                moveTo(0f, h * 0.56f)
                cubicTo(
                    w * 0.30f, h * 0.26f,
                    w * 0.68f, h * 0.64f,
                    w, h * 0.34f,
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = darkFooter,
                brush = Brush.horizontalGradient(listOf(HisabGreenDeep, HisabGreenDark)),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = tagline,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Top green curved header with bright-green bottom wave ribbon, circular open-book logo,
 * vertical divider, and "হিসাবের খাতা — সহজ হিসাব, নিরাপদ লেনদেন".
 */
@Composable
fun AuthCurvedBrandHeader(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(156.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Bright-green accent wave layer underneath
            val brightWave = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.88f)
                cubicTo(
                    w * 0.68f, h * 0.72f,
                    w * 0.30f, h * 1.04f,
                    0f, h * 0.84f,
                )
                close()
            }
            drawPath(
                path = brightWave,
                brush = Brush.horizontalGradient(listOf(BrightEmerald, FreshLimeGreen, BrightEmerald)),
            )

            // Main rich green curved header
            val headerWave = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.78f)
                cubicTo(
                    w * 0.68f, h * 0.64f,
                    w * 0.30f, h * 0.94f,
                    0f, h * 0.76f,
                )
                close()
            }
            drawPath(
                path = headerWave,
                brush = Brush.verticalGradient(listOf(HisabGreenDark, HisabGreen)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 8.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            // Circular open book badge
            Box(
                modifier = Modifier
                    .size(52.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = PureWhite,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
                OpenBookHeaderIcon(modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Vertical divider line
            Canvas(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(44.dp),
            ) {
                drawLine(
                    color = PureWhite.copy(alpha = 0.75f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = size.width,
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "হিসাবের খাতা",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 25.sp,
                    ),
                )
                Text(
                    text = "সহজ হিসাব, নিরাপদ লেনদেন",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PureWhite.copy(alpha = 0.95f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
fun OpenBookHeaderIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val leftPage = Path().apply {
            moveTo(w * 0.10f, h * 0.22f)
            quadraticTo(w * 0.30f, h * 0.20f, w * 0.50f, h * 0.30f)
            lineTo(w * 0.50f, h * 0.82f)
            quadraticTo(w * 0.30f, h * 0.72f, w * 0.10f, h * 0.74f)
            close()
        }
        val rightPage = Path().apply {
            moveTo(w * 0.90f, h * 0.22f)
            quadraticTo(w * 0.70f, h * 0.20f, w * 0.50f, h * 0.30f)
            lineTo(w * 0.50f, h * 0.82f)
            quadraticTo(w * 0.70f, h * 0.72f, w * 0.90f, h * 0.74f)
            close()
        }
        drawPath(leftPage, color = PureWhite, style = stroke)
        drawPath(rightPage, color = PureWhite, style = stroke)
        // Page ruling lines
        for (i in 0 until 3) {
            val y = h * (0.40f + i * 0.12f)
            drawLine(
                color = PureWhite,
                start = Offset(w * 0.20f, y - h * 0.02f),
                end = Offset(w * 0.40f, y + h * 0.02f),
                strokeWidth = w * 0.055f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = PureWhite,
                start = Offset(w * 0.60f, y + h * 0.02f),
                end = Offset(w * 0.80f, y - h * 0.02f),
                strokeWidth = w * 0.055f,
                cap = StrokeCap.Round,
            )
        }
    }
}

/**
 * Clean bottom wave footer matching the reference login & OTP forms.
 */
@Composable
fun AuthCurvedWaveFooter(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .navigationBarsPadding(),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val brightWave = Path().apply {
                moveTo(0f, h * 0.22f)
                cubicTo(w * 0.30f, 0f, w * 0.68f, h * 0.52f, w, h * 0.14f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = brightWave,
                brush = Brush.horizontalGradient(listOf(BrightEmerald, FreshLimeGreen, BrightEmerald)),
            )

            val deepWave = Path().apply {
                moveTo(0f, h * 0.40f)
                cubicTo(w * 0.32f, h * 0.20f, w * 0.70f, h * 0.64f, w, h * 0.30f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = deepWave,
                brush = Brush.horizontalGradient(listOf(HisabGreen, HisabGreenDark)),
            )
        }
    }
}

/**
 * Authentic classical Bank emblem in deep navy blue (#0E4B7A).
 */
@Composable
fun BankProviderLogo(
    modifier: Modifier = Modifier.size(38.dp),
) {
    val navy = Color(0xFF0E4B7A)
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Roof triangle
        val roof = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.88f, h * 0.36f)
            lineTo(w * 0.12f, h * 0.36f)
            close()
        }
        drawPath(roof, color = navy)
        // Architrave bar
        drawRoundRect(
            color = navy,
            topLeft = Offset(w * 0.14f, h * 0.38f),
            size = Size(w * 0.72f, h * 0.08f),
            cornerRadius = CornerRadius(2f, 2f),
        )
        // Three classical pillars
        val pillarX = listOf(w * 0.22f, w * 0.45f, w * 0.68f)
        pillarX.forEach { px ->
            drawRect(
                color = navy,
                topLeft = Offset(px, h * 0.48f),
                size = Size(w * 0.10f, h * 0.26f),
            )
        }
        // Step base
        drawRoundRect(
            color = navy,
            topLeft = Offset(w * 0.12f, h * 0.76f),
            size = Size(w * 0.76f, h * 0.10f),
            cornerRadius = CornerRadius(2f, 2f),
        )
    }
}

/**
 * Top curved green wave with optional circular back button and circular ledger+pen emblem
 * matching the reference Login and OTP Verification screenshot.
 */
@Composable
fun AuthScreenshotHeaderSection(
    showCircularBookEmblem: Boolean,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Layered top emerald wave + side leaves
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showCircularBookEmblem) 118.dp else 94.dp),
        ) {
            val w = size.width
            val h = size.height

            // Soft mint aura behind header
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE7F9E1), Color(0xFFF6FCF3), Color.Transparent)
                ),
                size = Size(w, h),
            )

            // Back bright-green wave ribbon
            val outerWave = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.62f)
                cubicTo(
                    w * 0.72f, h * 0.88f,
                    w * 0.28f, h * 0.44f,
                    0f, h * 0.68f,
                )
                close()
            }
            drawPath(
                path = outerWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF43C92D), Color(0xFF1BB322), Color(0xFF33BF28))
                ),
            )

            // Deep emerald foreground wave
            val innerWave = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h * 0.50f)
                cubicTo(
                    w * 0.74f, h * 0.78f,
                    w * 0.28f, h * 0.34f,
                    0f, h * 0.56f,
                )
                close()
            }
            drawPath(
                path = innerWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0A8912), Color(0xFF129F1B), Color(0xFF0A8912))
                ),
            )

            // Decorative green leaves on left edge
            drawLeafSprig(
                baseX = w * 0.03f,
                baseY = h * 0.92f,
                scale = w * 0.048f,
                mirrored = false,
            )
            // Decorative green leaves on right edge
            drawLeafSprig(
                baseX = w * 0.97f,
                baseY = h * 0.88f,
                scale = w * 0.048f,
                mirrored = true,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
            ) {
                if (showBackButton) {
                    androidx.compose.material3.Surface(
                        onClick = onBackClick,
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = PureWhite,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 12.dp)
                            .size(48.dp)
                            .testTag("otp_back_to_login"),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            androidx.compose.material3.Text(
                                text = "←",
                                color = Color(0xFF111827),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                if (showCircularBookEmblem) {
                    androidx.compose.material3.Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = PureWhite,
                        shadowElevation = 5.dp,
                        border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFF16A32A)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 8.dp)
                            .size(86.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularOpenLedgerWithPenIcon(modifier = Modifier.size(58.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (showCircularBookEmblem) 4.dp else 10.dp))
            Text(
                text = "হিসাবের খাতা",
                color = Color(0xFF0A8B14),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "আপনার প্রতিদিনের হিসাব, এক জায়গায়",
                color = Color(0xFF111827),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeafSprig(
    baseX: Float,
    baseY: Float,
    scale: Float,
    mirrored: Boolean,
) {
    val dir = if (mirrored) -1f else 1f
    val leaves = listOf(
        Triple(0.0f, -0.35f, 1.0f),
        Triple(0.55f, -0.75f, 0.88f),
        Triple(0.25f, -1.25f, 0.76f),
    )
    leaves.forEach { (dx, dy, s) ->
        val lx = baseX + dx * scale * dir
        val ly = baseY + dy * scale
        val leafPath = Path().apply {
            moveTo(lx, ly)
            quadraticTo(
                lx + 1.1f * scale * s * dir,
                ly - 0.55f * scale * s,
                lx + 1.65f * scale * s * dir,
                ly - 0.05f * scale * s,
            )
            quadraticTo(
                lx + 0.85f * scale * s * dir,
                ly + 0.55f * scale * s,
                lx,
                ly,
            )
            close()
        }
        drawPath(
            path = leafPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF16A32A), Color(0xFF65E02E))
            ),
        )
    }
}

@Composable
fun CircularOpenLedgerWithPenIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val emerald = Color(0xFF0A8B14)

        // Open ledger cover
        val bookBase = Path().apply {
            moveTo(w * 0.14f, h * 0.28f)
            quadraticTo(w * 0.32f, h * 0.24f, w * 0.50f, h * 0.34f)
            quadraticTo(w * 0.68f, h * 0.24f, w * 0.86f, h * 0.28f)
            lineTo(w * 0.86f, h * 0.78f)
            quadraticTo(w * 0.68f, h * 0.72f, w * 0.50f, h * 0.82f)
            quadraticTo(w * 0.32f, h * 0.72f, w * 0.14f, h * 0.78f)
            close()
        }
        drawPath(bookBase, color = emerald, style = stroke)

        // Center spine
        drawLine(
            color = emerald,
            start = Offset(w * 0.50f, h * 0.34f),
            end = Offset(w * 0.50f, h * 0.82f),
            strokeWidth = w * 0.05f,
            cap = StrokeCap.Round,
        )

        // Left page ledger lines
        for (i in 0 until 3) {
            val y = h * (0.43f + i * 0.105f)
            drawLine(
                color = emerald,
                start = Offset(w * 0.22f, y - h * 0.02f),
                end = Offset(w * 0.42f, y + h * 0.015f),
                strokeWidth = w * 0.04f,
                cap = StrokeCap.Round,
            )
        }

        // Mini bar chart on right page
        val barColors = listOf(Color(0xFF16A32A), Color(0xFF0A8B14), Color(0xFF22C55E))
        barColors.forEachIndexed { idx, c ->
            val bx = w * (0.57f + idx * 0.075f)
            val bh = h * (0.11f + idx * 0.05f)
            drawRect(
                color = c,
                topLeft = Offset(bx, h * 0.68f - bh),
                size = Size(w * 0.05f, bh),
            )
        }

        // Angled pen nib writing from top-right onto the book
        val penBody = Path().apply {
            moveTo(w * 0.78f, h * 0.10f)
            lineTo(w * 0.88f, h * 0.19f)
            lineTo(w * 0.62f, h * 0.49f)
            lineTo(w * 0.52f, h * 0.40f)
            close()
        }
        drawPath(penBody, color = Color(0xFF1F2937))
        val penTip = Path().apply {
            moveTo(w * 0.52f, h * 0.40f)
            lineTo(w * 0.62f, h * 0.49f)
            lineTo(w * 0.47f, h * 0.54f)
            close()
        }
        drawPath(penTip, color = emerald)
    }
}

/**
 * Green glowing shield + white padlock + checkmark + leaf sprigs matching the OTP screen reference.
 */
@Composable
fun OtpShieldSecurityBadge(
    modifier: Modifier = Modifier.size(width = 160.dp, height = 74.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f

        drawLeafSprig(baseX = cx - h * 0.56f, baseY = h * 0.90f, scale = h * 0.30f, mirrored = false)
        drawLeafSprig(baseX = cx + h * 0.56f, baseY = h * 0.90f, scale = h * 0.30f, mirrored = true)

        // Emerald shield silhouette
        val shield = Path().apply {
            moveTo(cx, h * 0.04f)
            lineTo(cx + h * 0.44f, h * 0.19f)
            lineTo(cx + h * 0.41f, h * 0.55f)
            quadraticTo(cx + h * 0.34f, h * 0.84f, cx, h * 0.97f)
            quadraticTo(cx - h * 0.34f, h * 0.84f, cx - h * 0.41f, h * 0.55f)
            lineTo(cx - h * 0.44f, h * 0.19f)
            close()
        }
        drawPath(
            path = shield,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF39D22D), Color(0xFF0B8A14))
            ),
        )

        // White padlock inside shield
        val lockW = h * 0.36f
        val lockH = h * 0.30f
        drawArc(
            color = PureWhite,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - lockW * 0.34f, h * 0.25f),
            size = Size(lockW * 0.68f, lockH * 0.85f),
            style = Stroke(width = h * 0.055f, cap = StrokeCap.Round),
        )
        drawRoundRect(
            color = PureWhite,
            topLeft = Offset(cx - lockW * 0.5f, h * 0.43f),
            size = Size(lockW, lockH),
            cornerRadius = CornerRadius(h * 0.06f, h * 0.06f),
        )
        drawCircle(
            color = Color(0xFF0B8A14),
            radius = h * 0.042f,
            center = Offset(cx, h * 0.57f),
        )

        // White-bordered green checkmark circle at bottom-right of shield
        val badgeCenter = Offset(cx + h * 0.34f, h * 0.72f)
        drawCircle(color = PureWhite, radius = h * 0.19f, center = badgeCenter)
        drawCircle(color = Color(0xFF16A32A), radius = h * 0.155f, center = badgeCenter)
        val check = Path().apply {
            moveTo(badgeCenter.x - h * 0.075f, badgeCenter.y)
            lineTo(badgeCenter.x - h * 0.015f, badgeCenter.y + h * 0.06f)
            lineTo(badgeCenter.x + h * 0.08f, badgeCenter.y - h * 0.05f)
        }
        drawPath(
            path = check,
            color = PureWhite,
            style = Stroke(width = h * 0.036f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

/**
 * Compact AD banner shown at the bottom of both Login and OTP screens matching the reference image.
 */
@Composable
fun AuthCompactAdBanner(
    headline: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF42C441)),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFCFF6B8), Color(0xFFEAFCE0), Color(0xFFC2F1A7))
                    )
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    androidx.compose.material3.Surface(
                        color = Color(0xFF0A8012),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = "AD",
                            color = PureWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Canvas(modifier = Modifier.size(22.dp)) {
                        val w = size.width
                        val h = size.height
                        val horn = Path().apply {
                            moveTo(w * 0.14f, h * 0.44f)
                            lineTo(w * 0.74f, h * 0.18f)
                            lineTo(w * 0.74f, h * 0.78f)
                            lineTo(w * 0.14f, h * 0.56f)
                            close()
                        }
                        drawPath(horn, color = Color(0xFF149E21))
                        drawRoundRect(
                            color = Color(0xFF0A7414),
                            topLeft = Offset(w * 0.22f, h * 0.56f),
                            size = Size(w * 0.14f, h * 0.28f),
                            cornerRadius = CornerRadius(2f, 2f),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = headline,
                        color = Color(0xFF0A7D13),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF1F2937),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                }

                // Friendly smartphone user vector avatar on right edge of AD banner
                Canvas(modifier = Modifier.size(width = 46.dp, height = 46.dp)) {
                    val w = size.width
                    val h = size.height
                    // Shirt
                    val shirt = Path().apply {
                        moveTo(w * 0.18f, h * 0.98f)
                        quadraticTo(w * 0.24f, h * 0.62f, w * 0.52f, h * 0.62f)
                        quadraticTo(w * 0.80f, h * 0.62f, w * 0.88f, h * 0.98f)
                        close()
                    }
                    drawPath(shirt, color = Color(0xFF14A426))
                    // Face
                    drawCircle(
                        color = Color(0xFFF3B78A),
                        radius = w * 0.16f,
                        center = Offset(w * 0.52f, h * 0.36f),
                    )
                    // Hair
                    drawArc(
                        color = Color(0xFF1F2937),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.35f, h * 0.16f),
                        size = Size(w * 0.34f, h * 0.24f),
                    )
                    // Phone in hand
                    drawRoundRect(
                        color = Color(0xFF111827),
                        topLeft = Offset(w * 0.14f, h * 0.46f),
                        size = Size(w * 0.18f, h * 0.34f),
                        cornerRadius = CornerRadius(3f, 3f),
                    )
                }
            }
        }
    }
}

/**
 * Curved green wave + scenic skyline silhouettes at the very bottom matching both screenshot screens.
 */
@Composable
fun AuthSilhouetteWaveFooter(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .navigationBarsPadding(),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft green trees & domes skyline silhouette along bottom
            val silhouetteColor = Color(0xFF6FE146).copy(alpha = 0.55f)
            val peaks = listOf(0.08f, 0.22f, 0.38f, 0.50f, 0.66f, 0.82f, 0.92f)
            peaks.forEachIndexed { idx, xFrac ->
                val radius = if (idx == 3) h * 0.32f else h * 0.20f
                drawCircle(
                    color = silhouetteColor,
                    radius = radius,
                    center = Offset(w * xFrac, h * 0.52f),
                )
            }
            // Central spire highlight
            drawLine(
                color = silhouetteColor,
                start = Offset(w * 0.50f, h * 0.08f),
                end = Offset(w * 0.50f, h * 0.42f),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
            )

            // Bright lime green outer hill wave
            val lightWave = Path().apply {
                moveTo(0f, h * 0.42f)
                cubicTo(w * 0.30f, h * 0.18f, w * 0.70f, h * 0.66f, w, h * 0.34f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = lightWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF39C928), Color(0xFF7BE842), Color(0xFF39C928))
                ),
            )

            // Deep emerald foreground wave
            val deepWave = Path().apply {
                moveTo(0f, h * 0.58f)
                cubicTo(w * 0.32f, h * 0.34f, w * 0.68f, h * 0.74f, w, h * 0.48f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = deepWave,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0A8511), Color(0xFF14A426), Color(0xFF0A8511))
                ),
            )
        }
    }
}

/**
 * Purple Rocket paper-plane/shuttle mark matching the Login screen provider card.
 */
@Composable
fun RocketProviderEmblem(
    modifier: Modifier = Modifier.size(width = 40.dp, height = 34.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rocketPurple = Color(0xFF8C249C)
        val rocketMagenta = Color(0xFFC026D3)

        // Speed trail dashes on left
        drawLine(
            color = rocketPurple,
            start = Offset(w * 0.08f, h * 0.44f),
            end = Offset(w * 0.24f, h * 0.36f),
            strokeWidth = w * 0.065f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = rocketMagenta,
            start = Offset(w * 0.14f, h * 0.62f),
            end = Offset(w * 0.28f, h * 0.54f),
            strokeWidth = w * 0.065f,
            cap = StrokeCap.Round,
        )

        // Upper wing
        val upperWing = Path().apply {
            moveTo(w * 0.22f, h * 0.38f)
            lineTo(w * 0.88f, h * 0.12f)
            lineTo(w * 0.58f, h * 0.50f)
            close()
        }
        drawPath(upperWing, color = rocketMagenta)

        // Lower wing
        val lowerWing = Path().apply {
            moveTo(w * 0.88f, h * 0.12f)
            lineTo(w * 0.74f, h * 0.84f)
            lineTo(w * 0.48f, h * 0.58f)
            close()
        }
        drawPath(lowerWing, color = rocketPurple)

        // Inner crease shadow
        val crease = Path().apply {
            moveTo(w * 0.88f, h * 0.12f)
            lineTo(w * 0.58f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.74f)
            lineTo(w * 0.48f, h * 0.58f)
            close()
        }
        drawPath(crease, color = Color(0xFF6B177A))
    }
}

data class HisaberPromoAdItem(
    val id: Int,
    val headline: String,
    val subtitle: String,
    val bullets: List<String>,
    val ctaLabel: String,
    @param:DrawableRes val imageRes: Int,
    val gradientStart: Color,
    val gradientMid: Color,
    val gradientEnd: Color,
)

val HISABER_PROMO_ADS: List<HisaberPromoAdItem> = listOf(
    HisaberPromoAdItem(
        id = 1,
        headline = "হিসাবের খাতা",
        subtitle = "মোবাইলে রাখুন দৈনিক সব হিসাব!",
        bullets = listOf("দৈনিক আয়-ব্যয় হিসাব", "সহজ ও নিরাপদ ট্র্যাকিং", "সব হিসাব এক অ্যাপে"),
        ctaLabel = "ডাউনলোড করুন  >",
        imageRes = R.drawable.img_promo_ad_1,
        gradientStart = Color(0xFFFCE7F3),
        gradientMid = Color(0xFFF0FDF4),
        gradientEnd = Color(0xFFBBF7D0),
    ),
    HisaberPromoAdItem(
        id = 2,
        headline = "হিসাবের খাতা অ্যাপ",
        subtitle = "ব্যবসা, চাকরি বা ব্যক্তিগত সব হিসাব রাখুন সহজে!",
        bullets = listOf("আয়-ব্যয়ের নির্ভুল হিসাব", "প্রতিদিনের লাভ-ক্ষতি", "দ্রুত রিপোর্ট ও ব্যাকআপ"),
        ctaLabel = "ডাউনলোড করুন  >",
        imageRes = R.drawable.img_promo_ad_2,
        gradientStart = Color(0xFFDCFCE7),
        gradientMid = Color(0xFFFEF9C3),
        gradientEnd = Color(0xFFBEF264),
    ),
    HisaberPromoAdItem(
        id = 3,
        headline = "হিসাবের খাতা",
        subtitle = "একসাথে সহজ হোক সব হিসাব!",
        bullets = listOf("স্মার্ট খাতা ও ক্যালকুলেটর", "বাকি ও জমার সঠিক হিসাব", "১০০% নিরাপদ তথ্য"),
        ctaLabel = "আজই ব্যবহার করুন  >",
        imageRes = R.drawable.img_promo_ad_3,
        gradientStart = Color(0xFFD9F99D),
        gradientMid = Color(0xFFECFDF5),
        gradientEnd = Color(0xFF86EFAC),
    ),
    HisaberPromoAdItem(
        id = 4,
        headline = "দোকানের বাকি খাতা",
        subtitle = "ক্রেতার বাকি ও পেমেন্ট রাখুন এক ট্যাপে!",
        bullets = listOf("কাস্টমারের বাকি ও পেমেন্ট", "তাগাদা SMS ও রিমাইন্ডার", "মাসিক লেনদেন রিপোর্ট"),
        ctaLabel = "শুরু করুন  >",
        imageRes = R.drawable.img_promo_ad_4,
        gradientStart = Color(0xFFD1FAE5),
        gradientMid = Color(0xFFF0FDF4),
        gradientEnd = Color(0xFF6EE7B7),
    ),
    HisaberPromoAdItem(
        id = 5,
        headline = "হিসাবের খাতা",
        subtitle = "সব হিসাব এক অ্যাপে!",
        bullets = listOf("আয়-ব্যয় হিসাব", "খরচের বিশ্লেষণ", "সহজ ও নিরাপদ"),
        ctaLabel = "আজই ব্যবহার করুন  >",
        imageRes = R.drawable.img_promo_ad_5,
        gradientStart = Color(0xFFCCFBF1),
        gradientMid = Color(0xFFECFDF5),
        gradientEnd = Color(0xFF86EFAC),
    ),
)

/**
 * Exact reference brand emblem: bright-green spiral-bound ledger notebook,
 * sleek dark calculator at bottom-right, and golden-yellow arc behind it.
 */
@Composable
fun SpiralNotebookCalculatorLogo(
    modifier: Modifier = Modifier.size(width = 110.dp, height = 78.dp),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sweeping golden-yellow circular arc behind the notebook & calculator
        drawArc(
            brush = Brush.horizontalGradient(listOf(AccentGold, AccentGoldDeep)),
            startAngle = 24f,
            sweepAngle = 250f,
            useCenter = false,
            topLeft = Offset(w * 0.24f, h * 0.08f),
            size = Size(w * 0.56f, h * 0.84f),
            style = Stroke(width = w * 0.048f, cap = StrokeCap.Round),
        )

        // Spiral-bound green notebook cover
        val bookLeft = w * 0.28f
        val bookTop = h * 0.12f
        val bookW = w * 0.42f
        val bookH = h * 0.72f
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color(0xFF14B823), Color(0xFF0B8A14))),
            topLeft = Offset(bookLeft, bookTop),
            size = Size(bookW, bookH),
            cornerRadius = CornerRadius(w * 0.045f, w * 0.045f),
        )

        // Subtle curved header line & white ruling stripes on green cover
        drawLine(
            color = PureWhite.copy(alpha = 0.85f),
            start = Offset(bookLeft + bookW * 0.18f, bookTop + bookH * 0.34f),
            end = Offset(bookLeft + bookW * 0.84f, bookTop + bookH * 0.28f),
            strokeWidth = w * 0.018f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = PureWhite.copy(alpha = 0.65f),
            start = Offset(bookLeft + bookW * 0.18f, bookTop + bookH * 0.48f),
            end = Offset(bookLeft + bookW * 0.68f, bookTop + bookH * 0.43f),
            strokeWidth = w * 0.015f,
            cap = StrokeCap.Round,
        )

        // Spiral binding rings along the left spine of the notebook
        for (i in 0 until 5) {
            val ringY = bookTop + bookH * (0.14f + i * 0.17f)
            drawRoundRect(
                color = Color(0xFF065F12),
                topLeft = Offset(bookLeft - w * 0.036f, ringY),
                size = Size(w * 0.068f, h * 0.055f),
                cornerRadius = CornerRadius(4f, 4f),
            )
            drawRoundRect(
                color = PureWhite,
                topLeft = Offset(bookLeft - w * 0.025f, ringY + h * 0.01f),
                size = Size(w * 0.046f, h * 0.030f),
                cornerRadius = CornerRadius(3f, 3f),
            )
        }

        // Sleek dark calculator resting in front at bottom-right
        val calcLeft = w * 0.49f
        val calcTop = h * 0.35f
        val calcW = w * 0.29f
        val calcH = h * 0.47f
        drawRoundRect(
            color = Color(0xFF0F2922),
            topLeft = Offset(calcLeft, calcTop),
            size = Size(calcW, calcH),
            cornerRadius = CornerRadius(w * 0.032f, w * 0.032f),
        )
        // Light mint calculator LCD screen
        drawRoundRect(
            color = Color(0xFFE7F9EC),
            topLeft = Offset(calcLeft + calcW * 0.13f, calcTop + calcH * 0.11f),
            size = Size(calcW * 0.74f, calcH * 0.22f),
            cornerRadius = CornerRadius(w * 0.016f, w * 0.016f),
        )
        // Calculator 3x3 key grid
        val keySizeW = calcW * 0.18f
        val keySizeH = calcH * 0.13f
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val kx = calcLeft + calcW * (0.14f + c * 0.27f)
                val ky = calcTop + calcH * (0.41f + r * 0.18f)
                drawRoundRect(
                    color = if (r == 2 && c == 2) AccentGold else PureWhite,
                    topLeft = Offset(kx, ky),
                    size = Size(keySizeW, keySizeH),
                    cornerRadius = CornerRadius(2f, 2f),
                )
            }
        }
    }
}

/**
 * Exact reference top section: green status bar strip, compact human-photo hero banner strip,
 * spiral notebook+calculator logo, large dark green "হিসাবের খাতা" title, subtitle, and tapered green underline.
 */
@Composable
fun ReferenceTopStatusAndHeaderSection(
    isOtpStep: Boolean,
    onTopBannerClick: () -> Unit,
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Top Green Status Bar Area (with unobtrusive back button only on OTP screen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF097E12), Color(0xFF139E1D), Color(0xFF097E12))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = if (isOtpStep) 2.dp else 4.dp),
        ) {
            if (isOtpStep) {
                Surface(
                    onClick = onBackClick,
                    shape = CircleShape,
                    color = PureWhite.copy(alpha = 0.22f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(34.dp)
                        .testTag("otp_back_to_login"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "←",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Soft curved mint background behind top banner & central logo matching reference screenshot
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .matchParentSize(),
            ) {
                val w = size.width
                val h = size.height
                drawRect(color = PureWhite, size = Size(w, h))
                val mintWash = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, h * 0.74f)
                    quadraticTo(w * 0.50f, h * 1.06f, 0f, h * 0.72f)
                    close()
                }
                drawPath(
                    path = mintWash,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFECFBF0), Color(0xFFF5FDF6), PureWhite)
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 2. Top Human-Photo Promo Strip matching top of both phones in the reference screenshot
                Surface(
                    onClick = onTopBannerClick,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 2.dp,
                    border = BorderStroke(1.dp, if (isOtpStep) Color(0xFF139E1D) else Color(0xFFE5D8EC)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isOtpStep) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0A8013), Color(0xFF109E1C), Color(0xFF076D0F))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFE7F9EC), Color(0xFFFCEEF6), Color(0xFFF3E4FA))
                                    )
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isOtpStep) R.drawable.img_promo_ad_1 else R.drawable.img_promo_ad_1
                            ),
                            contentDescription = "হিসাবের খাতা অ্যাপ প্রচার ব্যানার",
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                            modifier = Modifier
                                .width(84.dp)
                                .fillMaxHeight(),
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            if (isOtpStep) {
                                Text(
                                    text = "খরচের হিসাব রাখুন হিসাবের খাতা অ্যাপে!",
                                    color = Color(0xFFFDE047),
                                    fontSize = 11.5.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFACC15),
                                ) {
                                    Text(
                                        text = "এখনই শুরু করুন  >",
                                        color = Color(0xFF052E16),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
                                    )
                                }
                            } else {
                                Text(
                                    text = "হিসাবের খাতা অ্যাপ",
                                    color = Color(0xFFD81B60),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "ঘরে বসেই রাখুন\nসব হিসাব!",
                                        color = Color(0xFF111827),
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF0A7D13),
                                    ) {
                                        Text(
                                            text = "ডাউনলোড করুন  >",
                                            color = PureWhite,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Center Green Spiral Notebook & Calculator Logo
                SpiralNotebookCalculatorLogo(
                    modifier = Modifier.size(width = 96.dp, height = 66.dp),
                )

                // 4. Large Bold "হিসাবের খাতা" Title + Subtitle + Sleek Curved Green Underline
                Text(
                    text = "হিসাবের খাতা",
                    color = Color(0xFF0B6E14),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "আপনার হিসাব, এখন আরও সহজ!",
                    color = Color(0xFF111827),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Canvas(
                    modifier = Modifier
                        .padding(top = 3.dp, bottom = 2.dp)
                        .width(196.dp)
                        .height(6.dp),
                ) {
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.85f)
                        quadraticTo(
                            size.width * 0.50f,
                            0f,
                            size.width,
                            size.height * 0.85f,
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF0B8A14),
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
        }
    }
}

/**
 * Large single rotating advertisement box (displaying ONLY ONE of the 5 distinct scenarios at a time,
 * replacing the old bKash/Nagad/Rocket/Bank cards on Login and positioned at the bottom of OTP page).
 */
@Composable
fun HisaberPromoAdCarouselBanner(
    currentAdIndex: Int,
    onDownloadClick: (HisaberPromoAdItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeIndex = ((currentAdIndex % HISABER_PROMO_ADS.size) + HISABER_PROMO_ADS.size) % HISABER_PROMO_ADS.size
    val activeAd = HISABER_PROMO_ADS[safeIndex]

    Surface(
        onClick = { onDownloadClick(activeAd) },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, Color(0xFF7BE082)),
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp),
    ) {
        AnimatedContent(
            targetState = activeAd,
            transitionSpec = {
                (fadeIn(animationSpec = tween(420)) +
                    slideInHorizontally(animationSpec = tween(420)) { width -> width / 6 })
                    .togetherWith(
                        fadeOut(animationSpec = tween(360)) +
                            slideOutHorizontally(animationSpec = tween(360)) { width -> -width / 6 }
                    )
            },
            label = "promo_ad_carousel",
        ) { ad ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .heightIn(min = 132.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(ad.gradientStart, ad.gradientMid, ad.gradientEnd)
                        )
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Realistic human portrait + visible phone & Hishaber Khata app screen
                Box(
                    modifier = Modifier
                        .width(126.dp)
                        .fillMaxHeight(),
                ) {
                    Image(
                        painter = painterResource(id = ad.imageRes),
                        contentDescription = "${ad.headline} - ${ad.subtitle}",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    0.0f to Color.Transparent,
                                    0.72f to Color.Transparent,
                                    1.0f to ad.gradientMid,
                                )
                            ),
                    )
                }

                // Right promo content matching reference bottom ad cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = ad.headline,
                        color = Color(0xFF065F12),
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = ad.subtitle,
                        color = Color(0xFF111827),
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    ad.bullets.forEach { bullet ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B8A14)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "✓",
                                    color = PureWhite,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                            Text(
                                text = bullet,
                                color = Color(0xFF1F2937),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Surface(
                            onClick = { onDownloadClick(ad) },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0B6E14),
                            shadowElevation = 2.dp,
                            modifier = Modifier.testTag("promo_ad_download_button"),
                        ) {
                            Text(
                                text = ad.ctaLabel,
                                color = PureWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            repeat(HISABER_PROMO_ADS.size) { dotIdx ->
                                Box(
                                    modifier = Modifier
                                        .size(if (dotIdx == safeIndex) 6.dp else 4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (dotIdx == safeIndex) Color(0xFF0A8B14)
                                            else Color(0xFF0A8B14).copy(alpha = 0.28f)
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



