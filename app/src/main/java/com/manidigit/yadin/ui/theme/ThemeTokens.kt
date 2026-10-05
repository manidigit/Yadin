package com.manidigit.yadin.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppTheme(val id: String, val titleFa: String, val descriptionFa: String) {
    GTP("gtp", "تم GTP", "طیف بنفش و ارغوانی نئونی، متراکم و شارپ با خطوط هندسی دقیق"),
    GEMINI("gemini", "تم Gemini", "طراحی هوش مصنوعی آینده‌نگرانه، سرمه‌ای کیهانی، فیروزه‌ای و کارت‌های شیشه‌ای نرم")
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
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val heroGradient: Brush,
    val cardBorderBrush: Brush?,
    val accentGlow: Color,
    val isGemini: Boolean
)

@Immutable
data class YadinDimensions(
    val screenPadding: Dp,
    val contentGap: Dp,
    val sectionGap: Dp,
    val statCardHeight: Dp,
    val navHeight: Dp,
    val cornerSmall: Dp,
    val cornerMedium: Dp,
    val cornerLarge: Dp,
    val cornerPill: Dp,
    val cardElevation: Dp,
    val cardBorderAlpha: Float
)

val GtpLightColors = YadinColors(
    primary = Color(0xFF7C3AED),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF06B6D4),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF6F3FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEDE7FF),
    onSurface = Color(0xFF171225),
    onSurfaceVariant = Color(0xFF6F6485),
    card = Color(0xFFFCFAFF),
    outline = Color(0xFFD8CFF0),
    success = Color(0xFF138A5B),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFD92D48),
    info = Color(0xFF536DFE),
    heroGradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF06B6D4))),
    cardBorderBrush = null,
    accentGlow = Color(0x337C3AED),
    isGemini = false
)

val GtpDarkColors = YadinColors(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF15101F),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF090711),
    background = Color(0xFF090711),
    surface = Color(0xFF15101F),
    surfaceVariant = Color(0xFF21192F),
    onSurface = Color(0xFFF7F2FF),
    onSurfaceVariant = Color(0xFFC4B9D6),
    card = Color(0xFF191222),
    outline = Color(0xFF49375E),
    success = Color(0xFF52D49A),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFFF8A9A),
    info = Color(0xFF7C8CFF),
    heroGradient = Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF22D3EE))),
    cardBorderBrush = null,
    accentGlow = Color(0x44A78BFA),
    isGemini = false
)

val GeminiLightColors = YadinColors(
    primary = Color(0xFF1A73E8),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF00B4D8),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F8FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8F1FC),
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF5A6F8C),
    card = Color(0xFFFFFFFF),
    outline = Color(0xFFCBD5E1),
    success = Color(0xFF0D9488),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFEF4444),
    info = Color(0xFF3B82F6),
    heroGradient = Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF00B4D8), Color(0xFF9333EA))),
    cardBorderBrush = Brush.linearGradient(
        listOf(
            Color(0xFF1A73E8).copy(alpha = 0.5f),
            Color(0xFF00B4D8).copy(alpha = 0.6f),
            Color(0xFF9333EA).copy(alpha = 0.35f)
        )
    ),
    accentGlow = Color(0x281A73E8),
    isGemini = true
)

val GeminiDarkColors = YadinColors(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF060B14),
    secondary = Color(0xFF70D6FF),
    onSecondary = Color(0xFF060B14),
    background = Color(0xFF060B14),
    surface = Color(0xFF0E1726),
    surfaceVariant = Color(0xFF182337),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    card = Color(0xFF0E1726),
    outline = Color(0xFF2E3E5B),
    success = Color(0xFF2DD4BF),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFF87171),
    info = Color(0xFF60A5FA),
    heroGradient = Brush.linearGradient(listOf(Color(0xFF8AB4F8), Color(0xFF70D6FF), Color(0xFFC084FC))),
    cardBorderBrush = Brush.linearGradient(
        listOf(
            Color(0xFF8AB4F8).copy(alpha = 0.65f),
            Color(0xFF70D6FF).copy(alpha = 0.7f),
            Color(0xFFC084FC).copy(alpha = 0.45f)
        )
    ),
    accentGlow = Color(0x408AB4F8),
    isGemini = true
)

val GtpDimensions = YadinDimensions(
    screenPadding = 16.dp,
    contentGap = 10.dp,
    sectionGap = 14.dp,
    statCardHeight = 118.dp,
    navHeight = 70.dp,
    cornerSmall = 6.dp,
    cornerMedium = 10.dp,
    cornerLarge = 14.dp,
    cornerPill = 16.dp,
    cardElevation = 4.dp,
    cardBorderAlpha = 0.85f
)

val GeminiDimensions = YadinDimensions(
    screenPadding = 18.dp,
    contentGap = 12.dp,
    sectionGap = 16.dp,
    statCardHeight = 124.dp,
    navHeight = 76.dp,
    cornerSmall = 16.dp,
    cornerMedium = 24.dp,
    cornerLarge = 32.dp,
    cornerPill = 40.dp,
    cardElevation = 6.dp,
    cardBorderAlpha = 0.45f
)

val LocalYadinColors = staticCompositionLocalOf { GtpDarkColors }
val LocalYadinDimensions = staticCompositionLocalOf { GtpDimensions }
