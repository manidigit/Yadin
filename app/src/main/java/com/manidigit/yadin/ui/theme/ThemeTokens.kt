package com.manidigit.yadin.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppTheme(val id: String, val titleFa: String, val descriptionFa: String) {
    GTP("gtp", "تم GTP", "طیف بنفش و ارغوانی نئونی، متراکم و شارپ با خطوط هندسی دقیق"),
    GEMINI("gemini", "تم Gemini", "طراحی هوش مصنوعی آینده‌نگرانه، سرمه‌ای کیهانی، فیروزه‌ای و کارت‌های شیشه‌ای نرم"),
    CLAUDE("claude", "تم Claude (کلاد)", "طراحی مینیمال، متین و آکادمیک با طیف گرم سفالی، پس‌زمینه کاغذی عاجی، تایپوگرافی چشم‌نواز و کنتراست ارگونومیک"),
    GOOGOLI("googoli", "تم گوگولی (Googoli)", "طراحی شاداب، دلنشین و بازی‌وار با رنگ‌های پاستلی جذاب (صورتی پاستلی، هلویی و نعنایی)، گوشه‌های حباب‌گون و استایل بازی‌گونه")
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

val ClaudeLightColors = YadinColors(
    primary = Color(0xFFC15F3D),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF4A7C59),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF7F2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1EBE1),
    onSurface = Color(0xFF262320),
    onSurfaceVariant = Color(0xFF6B6258),
    card = Color(0xFFFFFFFF),
    outline = Color(0xFFE2D9CC),
    success = Color(0xFF2D7A58),
    warning = Color(0xFFD97706),
    error = Color(0xFFC93B2B),
    info = Color(0xFF3F6CB0),
    heroGradient = Brush.linearGradient(listOf(Color(0xFFC15F3D), Color(0xFFD97706), Color(0xFF8B4513))),
    cardBorderBrush = Brush.linearGradient(listOf(Color(0xFFE2D9CC), Color(0xFFD5C7B5))),
    accentGlow = Color(0x28C15F3D),
    isGemini = false
)

val ClaudeDarkColors = YadinColors(
    primary = Color(0xFFE08466),
    onPrimary = Color(0xFF1E140E),
    secondary = Color(0xFF7CB88F),
    onSecondary = Color(0xFF0F1A12),
    background = Color(0xFF161412),
    surface = Color(0xFF221F1C),
    surfaceVariant = Color(0xFF2F2A24),
    onSurface = Color(0xFFF5EFEA),
    onSurfaceVariant = Color(0xFFB8ADA3),
    card = Color(0xFF221F1C),
    outline = Color(0xFF3E362E),
    success = Color(0xFF5AB984),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFE06666),
    info = Color(0xFF7AA2E3),
    heroGradient = Brush.linearGradient(listOf(Color(0xFFE08466), Color(0xFFF59E0B), Color(0xFFB85D38))),
    cardBorderBrush = Brush.linearGradient(listOf(Color(0xFF3E362E), Color(0xFF4C4238))),
    accentGlow = Color(0x35E08466),
    isGemini = false
)

val GoogoliLightColors = YadinColors(
    primary = Color(0xFFFF5E7E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF38B2AC),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFF7F9),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFEDF2),
    onSurface = Color(0xFF2D1F2D),
    onSurfaceVariant = Color(0xFF785E75),
    card = Color(0xFFFFFFFF),
    outline = Color(0xFFFFD4E0),
    success = Color(0xFF38B2AC),
    warning = Color(0xFFFF9F43),
    error = Color(0xFFFF4757),
    info = Color(0xFF54A0FF),
    heroGradient = Brush.linearGradient(listOf(Color(0xFFFF5E7E), Color(0xFFFF9F43), Color(0xFFA55EEA))),
    cardBorderBrush = Brush.linearGradient(listOf(Color(0xFFFFD4E0), Color(0xFFE8D7F9))),
    accentGlow = Color(0x35FF5E7E),
    isGemini = false
)

val GoogoliDarkColors = YadinColors(
    primary = Color(0xFFFF7597),
    onPrimary = Color(0xFF1E1019),
    secondary = Color(0xFF4FD1C5),
    onSecondary = Color(0xFF0C1A17),
    background = Color(0xFF19121E),
    surface = Color(0xFF241A2B),
    surfaceVariant = Color(0xFF33233D),
    onSurface = Color(0xFFFFF0F5),
    onSurfaceVariant = Color(0xFFC7B3CF),
    card = Color(0xFF241A2B),
    outline = Color(0xFF523B61),
    success = Color(0xFF4FD1C5),
    warning = Color(0xFFFFB067),
    error = Color(0xFFFF6B81),
    info = Color(0xFF70A1FF),
    heroGradient = Brush.linearGradient(listOf(Color(0xFFFF7597), Color(0xFFFFB067), Color(0xFFBE82FF))),
    cardBorderBrush = Brush.linearGradient(listOf(Color(0xFF523B61), Color(0xFF6B487A))),
    accentGlow = Color(0x45FF7597),
    isGemini = false
)

val GoogoliDimensions = YadinDimensions(
    screenPadding = 16.dp,
    contentGap = 12.dp,
    sectionGap = 16.dp,
    statCardHeight = 116.dp,
    navHeight = 72.dp,
    cornerSmall = 12.dp,
    cornerMedium = 20.dp,
    cornerLarge = 28.dp,
    cornerPill = 36.dp,
    cardElevation = 3.dp,
    cardBorderAlpha = 0.75f
)

val ClaudeDimensions = YadinDimensions(
    screenPadding = 16.dp,
    contentGap = 12.dp,
    sectionGap = 16.dp,
    statCardHeight = 114.dp,
    navHeight = 72.dp,
    cornerSmall = 10.dp,
    cornerMedium = 16.dp,
    cornerLarge = 22.dp,
    cornerPill = 28.dp,
    cardElevation = 2.dp,
    cardBorderAlpha = 0.80f
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
