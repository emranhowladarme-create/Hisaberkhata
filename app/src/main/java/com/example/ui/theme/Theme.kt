package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val HisaberLightColorScheme = lightColorScheme(
    primary = HisabGreen,
    onPrimary = PureWhite,
    primaryContainer = HisabGreenSoft,
    onPrimaryContainer = HisabGreenDark,
    secondary = HisabGreenDark,
    onSecondary = PureWhite,
    secondaryContainer = HisabGreenTint,
    onSecondaryContainer = HisabGreenDeep,
    tertiary = PaonaGreen,
    onTertiary = PureWhite,
    background = PureWhite,
    onBackground = InkPrimary,
    surface = PureWhite,
    onSurface = InkPrimary,
    surfaceVariant = HisabGreenTint,
    onSurfaceVariant = InkSecondary,
    outline = CardBorder,
    outlineVariant = HisabGreenBorder,
    error = DenaRed,
    onError = PureWhite,
)

val HisaberShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun HisaberKhataTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HisaberLightColorScheme,
        typography = Typography,
        shapes = HisaberShapes,
        content = content,
    )
}
