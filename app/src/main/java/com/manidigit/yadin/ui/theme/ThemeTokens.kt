package com.manidigit.yadin.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppTheme(val id: String, val titleFa: String, val descriptionFa: String) {
    GEMINI("gemini", "تم Gemini", "طراحی هوش مصنوعی، سرمه‌ای کیهانی، فیروزه‌ای و کارت‌های شیشه‌ای نرم")
}

@Immutable
data class YadinColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val card: Color,
    val outline: Color,
    val outlineVariant: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val error: Color,
    val onError: Color,
    val info: Color,
    val onInfo: Color,
    val accentPurple: Color,
    val heroGradient: Brush,
    val cardBorderBrush: Brush?,
    val accentGlow: Color,
    val onHero: Color = Color(0xFFFFFFFF)
)

@Immutable
data class YadinDimensions(
    val screenPadding: Dp,
    val contentGap: Dp,
    val sectionGap: Dp,
    val itemGapSmall: Dp = 6.dp,
    val itemGapMedium: Dp = 8.dp,
    val itemGapLarge: Dp = 12.dp,
    val statCardHeight: Dp,
    val navHeight: Dp,
    val buttonHeight: Dp = 48.dp,
    val chipHeight: Dp = 36.dp,
    val cardElevation: Dp,
    val cardBorderAlpha: Float,
    val cornerExtraSmall: Dp = 8.dp,
    val cornerSmall: Dp,
    val cornerBadge: Dp = 12.dp,
    val cornerMedium: Dp,
    val cornerLarge: Dp,
    val cornerPill: Dp
)

@Immutable
data class YadinShapes(
    val extraSmall: CornerBasedShape = RoundedCornerShape(8.dp),
    val badge: CornerBasedShape = RoundedCornerShape(12.dp),
    val small: CornerBasedShape = RoundedCornerShape(16.dp),
    val medium: CornerBasedShape = RoundedCornerShape(24.dp),
    val large: CornerBasedShape = RoundedCornerShape(32.dp),
    val pill: CornerBasedShape = RoundedCornerShape(40.dp),
    val circle: Shape = CircleShape
)

@Immutable
data class YadinIconSizes(
    val small: Dp = 16.dp,
    val medium: Dp = 20.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val hero: Dp = 48.dp
)

val LocalYadinColors = staticCompositionLocalOf { GeminiTheme.DarkColors }
val LocalYadinDimensions = staticCompositionLocalOf { GeminiTheme.Dimensions }
val LocalYadinShapes = staticCompositionLocalOf { GeminiTheme.Shapes }
val LocalYadinIconSizes = staticCompositionLocalOf { GeminiTheme.IconSizes }

/**
 * دسترسی مستقیم، تایپ‌شده و سراسری به توکن‌های تم جاری:
 * - `YadinTheme.colors`
 * - `YadinTheme.dimensions`
 * - `YadinTheme.shapes`
 * - `YadinTheme.iconSizes`
 * - `YadinTheme.typography`
 */
object YadinTheme {
    val colors: YadinColors
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinColors.current

    val dimensions: YadinDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinDimensions.current

    val shapes: YadinShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinShapes.current

    val iconSizes: YadinIconSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinIconSizes.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}
