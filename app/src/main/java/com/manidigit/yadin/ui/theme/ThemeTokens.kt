package com.manidigit.yadin.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

enum class AppTheme(val id: String, val titleFa: String, val descriptionFa: String) {
    GTP("gtp", "تم GTP", "طیف بنفش و ارغوانی نئونی، متراکم و شارپ با خطوط هندسی دقیق"),
    GEMINI("gemini", "تم Gemini", "طراحی هوش مصنوعی آینده‌نگرانه، سرمه‌ای کیهانی، فیروزه‌ای و کارت‌های شیشه‌ای نرم"),
    CLAUDE("claude", "تم Claude (کلاد)", "طراحی مینیمال، متین و آکادمیک با طیف گرم سفالی، پس‌زمینه کاغذی عاجی، تایپوگرافی چشم‌نواز و کنتراست ارگونومیک"),
    GOOGOLI("googoli", "تم گوگولی (Googoli)", "طراحی شاداب، دلنشین و بازی‌وار با رنگ آرام نعنایی/فیروزه‌ای پاستلی، رگه‌های هلویی و گوشه‌های حباب‌گون")
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

val LocalYadinColors = staticCompositionLocalOf { GtpDarkColors }
val LocalYadinDimensions = staticCompositionLocalOf { GtpDimensions }
