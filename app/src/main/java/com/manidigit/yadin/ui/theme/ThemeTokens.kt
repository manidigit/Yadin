package com.manidigit.yadin.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppTheme(val id: String, val titleFa: String, val descriptionFa: String) {
    GTP("gtp", "تم GTP", "طیف بنفش و ارغوانی نئونی، متراکم و شارپ با کنتراست بالا"),
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
    val accentGlow: Color
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
    accentGlow = Color(0x337C3AED)
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
    accentGlow = Color(0x44A78BFA)
)

val GeminiLightColors = YadinColors(
    primary = Color(0xFF1A73E8),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF00B4D8),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF8FAFD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEDF2F9),
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF64748B),
    card = Color(0xFFFFFFFF),
    outline = Color(0xFFCBD5E1),
    success = Color(0xFF138A5B),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFD92D48),
    info = Color(0xFF536DFE),
    heroGradient = Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF00B4D8), Color(0xFF7B2CBF))),
    accentGlow = Color(0x2E1A73E8)
)

val GeminiDarkColors = YadinColors(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0B0F19),
    secondary = Color(0xFF70D6FF),
    onSecondary = Color(0xFF0B0F19),
    background = Color(0xFF0B0F19),
    surface = Color(0xFF111827),
    surfaceVariant = Color(0xFF1F2937),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF94A3B8),
    card = Color(0xFF161F30),
    outline = Color(0xFF334155),
    success = Color(0xFF52D49A),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFFF8A9A),
    info = Color(0xFF7C8CFF),
    heroGradient = Brush.linearGradient(listOf(Color(0xFF8AB4F8), Color(0xFF70D6FF), Color(0xFFC77DFF))),
    accentGlow = Color(0x408AB4F8)
)

val GtpDimensions = YadinDimensions(
    screenPadding = 16.dp,
    contentGap = 10.dp,
    sectionGap = 14.dp,
    statCardHeight = 118.dp,
    navHeight = 70.dp,
    cornerSmall = 6.dp,
    cornerMedium = 12.dp,
    cornerLarge = 18.dp,
    cardElevation = 5.dp,
    cardBorderAlpha = 0.70f
)

val GeminiDimensions = YadinDimensions(
    screenPadding = 18.dp,
    contentGap = 12.dp,
    sectionGap = 16.dp,
    statCardHeight = 124.dp,
    navHeight = 76.dp,
    cornerSmall = 12.dp,
    cornerMedium = 20.dp,
    cornerLarge = 28.dp,
    cardElevation = 3.dp,
    cardBorderAlpha = 0.35f
)

val LocalYadinColors = staticCompositionLocalOf { GtpDarkColors }
val LocalYadinDimensions = staticCompositionLocalOf { GtpDimensions }
