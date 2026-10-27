package com.manidigit.yadin.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manidigit.yadin.R

/**
 * مجموعه آیکون‌های اختصاصی تم Gemini (مشتق از [YadinIconSet]).
 */
@Immutable
object GeminiIconSet : YadinIconSet()

/**
 * پرونده و پیکربندی استاندارد، مستقل و یکپارچه تم Gemini (هوش مصنوعی) مبتنی بر قرارداد [YadinThemeSpec].
 *
 * تمامی توکن‌های رنگی (حالت تاریک و روشن)، ابعاد و فواصل چیدمان،
 * اشکال هندسی کارت‌ها و المان‌ها، سایز استاندارد آیکون‌ها، مجموعه آیکون‌ها و اتصال به Material Design 3
 * به طور متمرکز و مستقل در این پیاده‌سازی تعریف و مدیریت می‌شوند.
 */
@Immutable
object GeminiTheme : YadinThemeSpec {
    override val id: String = "gemini"
    override val titleFa: String = "تم Gemini (هوش مصنوعی)"
    override val titleEn: String = "Gemini Theme (AI Aesthetic)"
    override val descriptionFa: String = "طراحی هوش مصنوعی، سرمه‌ای کیهانی، فیروزه‌ای و کارت‌های شیشه‌ای نرم"
    override val descriptionEn: String = "Cosmic navy, cyan accents, and soft glassmorphic cards"
    override val titleRes: Int = R.string.settings_theme_gemini

    override val LightColors = YadinColors(
        primary = YadinPalette.GeminiBlue,
        onPrimary = Color(0xFFFFFFFF),
        secondary = YadinPalette.GeminiSky,
        onSecondary = Color(0xFFFFFFFF),
        background = YadinPalette.GeminiBgLight,
        surface = YadinPalette.GeminiSurfaceLight,
        surfaceVariant = YadinPalette.GeminiSurfaceVarLight,
        onSurface = Color(0xFF1E293B),
        onSurfaceVariant = Color(0xFF5A6F8C),
        card = YadinPalette.GeminiSurfaceLight,
        outline = YadinPalette.GeminiOutlineLight,
        outlineVariant = Color(0xFFE2E8F0),
        success = YadinPalette.SuccessGreen,
        onSuccess = Color(0xFFFFFFFF),
        warning = YadinPalette.WarningAmber,
        onWarning = Color(0xFFFFFFFF),
        error = YadinPalette.ErrorRed,
        onError = Color(0xFFFFFFFF),
        info = YadinPalette.InfoBlue,
        onInfo = Color(0xFFFFFFFF),
        accentPurple = YadinPalette.GeminiPurple,
        difficultyEasy = YadinPalette.SuccessGreen,
        difficultyMedium = YadinPalette.WarningAmber,
        difficultyHard = YadinPalette.HardOrange,
        difficultyVeryHard = YadinPalette.ErrorRed,
        tierBronze = YadinPalette.TierBronze,
        tierSilver = YadinPalette.TierSilver,
        tierGold = YadinPalette.TierGold,
        tierPlatinum = YadinPalette.TierPlatinum,
        heroGradient = Brush.linearGradient(
            listOf(YadinPalette.GeminiBlue, YadinPalette.GeminiSky, YadinPalette.GeminiPurple)
        ),
        cardBorderBrush = Brush.linearGradient(
            listOf(
                YadinPalette.GeminiBlue.copy(alpha = 0.5f),
                YadinPalette.GeminiSky.copy(alpha = 0.6f),
                YadinPalette.GeminiPurple.copy(alpha = 0.35f)
            )
        ),
        accentGlow = Color(0x281A73E8),
        onHero = Color(0xFFFFFFFF)
    )

    override val DarkColors = YadinColors(
        primary = YadinPalette.GeminiBlueLight,
        onPrimary = Color(0xFF060B14),
        secondary = YadinPalette.GeminiSkyLight,
        onSecondary = Color(0xFF060B14),
        background = YadinPalette.GeminiBgDark,
        surface = YadinPalette.GeminiSurfaceDark,
        surfaceVariant = YadinPalette.GeminiSurfaceVarDark,
        onSurface = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF94A3B8),
        card = YadinPalette.GeminiSurfaceDark,
        outline = YadinPalette.GeminiOutlineDark,
        outlineVariant = Color(0xFF1E293B),
        success = YadinPalette.SuccessGreenLight,
        onSuccess = Color(0xFF060B14),
        warning = YadinPalette.WarningAmberLight,
        onWarning = Color(0xFF060B14),
        error = YadinPalette.ErrorRedLight,
        onError = Color(0xFF060B14),
        info = YadinPalette.InfoBlueLight,
        onInfo = Color(0xFF060B14),
        accentPurple = YadinPalette.GeminiPurpleLight,
        difficultyEasy = YadinPalette.SuccessGreenLight,
        difficultyMedium = YadinPalette.WarningAmberLight,
        difficultyHard = YadinPalette.HardOrangeLight,
        difficultyVeryHard = YadinPalette.ErrorRedLight,
        tierBronze = YadinPalette.TierBronze,
        tierSilver = YadinPalette.TierSilver,
        tierGold = YadinPalette.TierGold,
        tierPlatinum = YadinPalette.TierPlatinum,
        heroGradient = Brush.linearGradient(
            listOf(YadinPalette.GeminiBlueLight, YadinPalette.GeminiSkyLight, YadinPalette.GeminiPurpleLight)
        ),
        cardBorderBrush = Brush.linearGradient(
            listOf(
                YadinPalette.GeminiBlueLight.copy(alpha = 0.65f),
                YadinPalette.GeminiSkyLight.copy(alpha = 0.7f),
                YadinPalette.GeminiPurpleLight.copy(alpha = 0.45f)
            )
        ),
        accentGlow = Color(0x408AB4F8),
        onHero = Color(0xFFFFFFFF)
    )

    override val Dimensions = YadinDimensions(
        screenPadding = 18.dp,
        contentGap = 12.dp,
        sectionGap = 16.dp,
        itemGapSmall = 6.dp,
        itemGapMedium = 8.dp,
        itemGapLarge = 12.dp,
        statCardHeight = 124.dp,
        navHeight = 76.dp,
        buttonHeight = 48.dp,
        chipHeight = 36.dp,
        cardElevation = 6.dp,
        cardBorderAlpha = 0.45f,
        cornerExtraSmall = 8.dp,
        cornerSmall = 16.dp,
        cornerBadge = 12.dp,
        cornerMedium = 24.dp,
        cornerLarge = 32.dp,
        cornerPill = 40.dp
    )

    override val Shapes = YadinShapes(
        extraSmall = RoundedCornerShape(8.dp),
        badge = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(24.dp),
        large = RoundedCornerShape(32.dp),
        pill = RoundedCornerShape(40.dp),
        circle = CircleShape
    )

    override val IconSizes = YadinIconSizes(
        small = 16.dp,
        medium = 20.dp,
        large = 24.dp,
        extraLarge = 32.dp,
        hero = 48.dp
    )

    override val Icons: YadinIconSet = GeminiIconSet

    override val Typography: Typography = AppTypography
}

// Aliases for seamless backward compatibility across callers
val GeminiLightColors get() = GeminiTheme.LightColors
val GeminiDarkColors get() = GeminiTheme.DarkColors
val GeminiDimensions get() = GeminiTheme.Dimensions
