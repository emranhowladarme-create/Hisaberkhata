package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PureWhite

private val CrownGoldLight = Color(0xFFFFF3A1)
private val CrownGoldMid = Color(0xFFF6B91E)
private val CrownGoldDeep = Color(0xFFB77404)
private val CrownGoldShadow = Color(0xFF7D4B00)

/**
 * 3D-shaded golden crown vector illustration matching the Gold Package & My Gold Package screens.
 */
@Composable
fun GoldCrownIllustration(
    modifier: Modifier = Modifier.size(46.dp),
) {
    Canvas(modifier = modifier) {
        drawDetailedGoldCrown(
            left = size.width * 0.06f,
            top = size.height * 0.12f,
            width = size.width * 0.88f,
            height = size.height * 0.76f,
        )
    }
}

/**
 * Golden Crown & symmetrical Laurel Wreath banner matching Panel 2 ("সোনালী সদস্যতা / বেশি সুবিধা, বেশি লাভ").
 */
@Composable
fun GoldBannerHeaderArtwork(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE57F),
                        Color(0xFFF6C230),
                        Color(0xFFE09F12),
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
        ) {
            val w = size.width
            val h = size.height
            val cx = w * 0.50f

            // Subtle warm radial glow behind the crown
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF8C9).copy(alpha = 0.75f),
                        Color.Transparent,
                    ),
                    center = Offset(cx, h * 0.36f),
                    radius = h * 0.68f,
                ),
                radius = h * 0.68f,
                center = Offset(cx, h * 0.36f),
            )

            // Symmetrical left & right golden laurel wreath branches
            drawLaurelBranch(centerX = cx, height = h, width = w, isRight = false)
            drawLaurelBranch(centerX = cx, height = h, width = w, isRight = true)

            // Central 3D-styled golden crown
            val crownW = minOf(w * 0.30f, 118.dp.toPx())
            val crownH = h * 0.50f
            drawDetailedGoldCrown(
                left = cx - crownW / 2f,
                top = h * 0.02f,
                width = crownW,
                height = crownH,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "সোনালী সদস্যতা",
                color = Color(0xFF2B1600),
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "বেশি সুবিধা, বেশি লাভ",
                color = Color(0xFF4B2B02),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun DrawScope.drawDetailedGoldCrown(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
) {
    val right = left + width
    val bottom = top + height
    val cx = left + width * 0.50f

    // Back crown points shadow
    val backPoints = Path().apply {
        moveTo(left + width * 0.14f, bottom - height * 0.24f)
        lineTo(left + width * 0.28f, top + height * 0.22f)
        lineTo(cx, top + height * 0.48f)
        lineTo(left + width * 0.72f, top + height * 0.22f)
        lineTo(right - width * 0.14f, bottom - height * 0.24f)
        close()
    }
    drawPath(backPoints, color = CrownGoldShadow.copy(alpha = 0.65f))

    // Main front crown silhouette
    val crownBody = Path().apply {
        moveTo(left + width * 0.10f, bottom - height * 0.22f)
        lineTo(left + width * 0.03f, top + height * 0.28f)
        lineTo(left + width * 0.30f, top + height * 0.56f)
        lineTo(cx, top + height * 0.08f)
        lineTo(left + width * 0.70f, top + height * 0.56f)
        lineTo(right - width * 0.03f, top + height * 0.28f)
        lineTo(right - width * 0.10f, bottom - height * 0.22f)
        close()
    }
    drawPath(
        path = crownBody,
        brush = Brush.verticalGradient(
            colors = listOf(CrownGoldLight, CrownGoldMid, CrownGoldDeep),
            startY = top,
            endY = bottom,
        ),
    )
    drawPath(
        path = crownBody,
        color = CrownGoldShadow,
        style = Stroke(width = (width * 0.025f).coerceAtLeast(1.5f), join = StrokeJoin.Round),
    )

    // Crown jeweled base rim
    val rimTop = bottom - height * 0.23f
    val rimH = height * 0.18f
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(CrownGoldDeep, CrownGoldLight, CrownGoldDeep)),
        topLeft = Offset(left + width * 0.09f, rimTop),
        size = Size(width * 0.82f, rimH),
        cornerRadius = CornerRadius(rimH * 0.45f, rimH * 0.45f),
    )
    drawRoundRect(
        color = CrownGoldShadow,
        topLeft = Offset(left + width * 0.09f, rimTop),
        size = Size(width * 0.82f, rimH),
        cornerRadius = CornerRadius(rimH * 0.45f, rimH * 0.45f),
        style = Stroke(width = (width * 0.022f).coerceAtLeast(1.2f)),
    )

    // Crown tip pearls + center ruby accent
    val tipRadius = (width * 0.048f).coerceAtLeast(2.2f)
    val tips = listOf(
        Offset(left + width * 0.03f, top + height * 0.26f),
        Offset(left + width * 0.28f, top + height * 0.20f),
        Offset(cx, top + height * 0.07f),
        Offset(left + width * 0.72f, top + height * 0.20f),
        Offset(right - width * 0.03f, top + height * 0.26f),
    )
    tips.forEachIndexed { idx, offset ->
        drawCircle(color = CrownGoldShadow, radius = tipRadius * 1.18f, center = offset)
        drawCircle(
            color = if (idx == 2) CrownGoldLight else CrownGoldMid,
            radius = tipRadius,
            center = offset,
        )
    }

    // Center diamond jewel in base band
    drawCircle(
        color = Color(0xFFFFF8DC),
        radius = rimH * 0.28f,
        center = Offset(cx, rimTop + rimH * 0.50f),
    )
}

private fun DrawScope.drawLaurelBranch(
    centerX: Float,
    width: Float,
    height: Float,
    isRight: Boolean,
) {
    val dir = if (isRight) 1f else -1f
    val branchColor = Color(0xFFB36D00)
    val leafHighlight = Color(0xFFE0990C)
    val startX = centerX + dir * minOf(width * 0.36f, 128.dp.toPx())
    val endX = centerX + dir * minOf(width * 0.26f, 92.dp.toPx())

    val stem = Path().apply {
        moveTo(centerX + dir * minOf(width * 0.14f, 44.dp.toPx()), height * 0.96f)
        quadraticTo(
            startX + dir * 24f,
            height * 0.56f,
            endX,
            height * 0.12f,
        )
    }
    drawPath(
        path = stem,
        color = branchColor,
        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
    )

    for (i in 0 until 6) {
        val t = i / 5f
        val lx = centerX + dir * (minOf(width * 0.22f, 70.dp.toPx()) + kotlin.math.sin(t * Math.PI).toFloat() * minOf(width * 0.16f, 52.dp.toPx()))
        val ly = height * (0.86f - t * 0.68f)
        val leafLen = 14.dp.toPx()
        val leafPath = Path().apply {
            moveTo(lx, ly)
            quadraticTo(lx + dir * leafLen * 0.7f, ly - leafLen * 0.55f, lx + dir * leafLen * 0.9f, ly - leafLen * 0.9f)
            quadraticTo(lx - dir * leafLen * 0.1f, ly - leafLen * 0.55f, lx, ly)
            close()
        }
        drawPath(
            path = leafPath,
            brush = Brush.linearGradient(listOf(leafHighlight, branchColor)),
        )
    }
}

/**
 * Friendly storefront illustration for Screen 1 shop profile circle when no custom photo is set.
 */
@Composable
fun ShopStorefrontAvatarIllustration(
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Sky & sunny mint backdrop
        drawCircle(
            brush = Brush.verticalGradient(listOf(Color(0xFFDCFCE7), Color(0xFFF0FDF4))),
            radius = w * 0.50f,
            center = Offset(w * 0.50f, h * 0.50f),
        )

        // Shop front cream walls
        drawRoundRect(
            color = Color(0xFFFFF7ED),
            topLeft = Offset(w * 0.20f, h * 0.44f),
            size = Size(w * 0.60f, h * 0.38f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        )
        // Shop side & trim
        drawRect(
            color = Color(0xFF92400E),
            topLeft = Offset(w * 0.16f, h * 0.79f),
            size = Size(w * 0.68f, h * 0.06f),
        )
        // Shop window + wooden door
        drawRoundRect(
            color = Color(0xFFBAE6FD),
            topLeft = Offset(w * 0.25f, h * 0.52f),
            size = Size(w * 0.22f, h * 0.16f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
        )
        drawRoundRect(
            color = Color(0xFFD97706),
            topLeft = Offset(w * 0.53f, h * 0.50f),
            size = Size(w * 0.20f, h * 0.29f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
        )

        // Striped emerald & red-orange shop awning
        val stripes = listOf(
            Color(0xFF16A34A),
            Color(0xFFFFEDD5),
            Color(0xFFEF4444),
            Color(0xFFFFEDD5),
            Color(0xFF16A34A),
        )
        val awningLeft = w * 0.16f
        val awningW = w * 0.68f
        val stripeW = awningW / stripes.size
        stripes.forEachIndexed { idx, col ->
            drawRoundRect(
                color = col,
                topLeft = Offset(awningLeft + idx * stripeW, h * 0.26f),
                size = Size(stripeW + 1f, h * 0.20f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
            )
        }
    }
}

/**
 * White-tinted authentic brand emblems for solid pink bKash, orange Nagad, and purple Rocket cards.
 */
@Composable
fun WhitePaymentBrandLogo(
    providerId: String,
    modifier: Modifier = Modifier.size(34.dp),
) {
    when (providerId) {
        "bkash" -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val wingTop = Path().apply {
                    moveTo(w * 0.12f, h * 0.20f)
                    lineTo(w * 0.52f, h * 0.44f)
                    lineTo(w * 0.34f, h * 0.62f)
                    close()
                }
                val wingBody = Path().apply {
                    moveTo(w * 0.36f, h * 0.64f)
                    lineTo(w * 0.66f, h * 0.24f)
                    lineTo(w * 0.88f, h * 0.35f)
                    lineTo(w * 0.56f, h * 0.82f)
                    close()
                }
                val beak = Path().apply {
                    moveTo(w * 0.66f, h * 0.24f)
                    lineTo(w * 0.94f, h * 0.22f)
                    lineTo(w * 0.88f, h * 0.35f)
                    close()
                }
                drawPath(wingTop, color = PureWhite)
                drawPath(wingBody, color = PureWhite.copy(alpha = 0.88f))
                drawPath(beak, color = PureWhite)
            }
        }
        "nagad" -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val outerFlame = Path().apply {
                    moveTo(w * 0.52f, h * 0.10f)
                    cubicTo(w * 0.86f, h * 0.28f, w * 0.90f, h * 0.74f, w * 0.50f, h * 0.90f)
                    cubicTo(w * 0.14f, h * 0.78f, w * 0.14f, h * 0.32f, w * 0.44f, h * 0.18f)
                    cubicTo(w * 0.35f, h * 0.42f, w * 0.46f, h * 0.52f, w * 0.52f, h * 0.10f)
                    close()
                }
                drawPath(outerFlame, color = PureWhite)
                drawCircle(
                    color = Color(0xFFF7821B),
                    radius = w * 0.16f,
                    center = Offset(w * 0.52f, h * 0.60f),
                )
            }
        }
        else -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                drawLine(
                    color = PureWhite,
                    start = Offset(w * 0.08f, h * 0.44f),
                    end = Offset(w * 0.24f, h * 0.36f),
                    strokeWidth = w * 0.07f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = PureWhite.copy(alpha = 0.85f),
                    start = Offset(w * 0.14f, h * 0.64f),
                    end = Offset(w * 0.28f, h * 0.56f),
                    strokeWidth = w * 0.07f,
                    cap = StrokeCap.Round,
                )
                val upperWing = Path().apply {
                    moveTo(w * 0.22f, h * 0.38f)
                    lineTo(w * 0.88f, h * 0.12f)
                    lineTo(w * 0.58f, h * 0.50f)
                    close()
                }
                val lowerWing = Path().apply {
                    moveTo(w * 0.88f, h * 0.12f)
                    lineTo(w * 0.74f, h * 0.84f)
                    lineTo(w * 0.48f, h * 0.58f)
                    close()
                }
                drawPath(upperWing, color = PureWhite)
                drawPath(lowerWing, color = PureWhite.copy(alpha = 0.85f))
            }
        }
    }
}
