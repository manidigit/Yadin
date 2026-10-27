package com.manidigit.yadin.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * GPT Glossy 3D visual theme.
 *
 * This file only defines presentation tokens. It does not change study,
 * review, storage, navigation, or vocabulary behavior.
 */
object GptTheme {
    val LightColors = YadinColors(
        primary = YadinPalette.GptBlue,
        onPrimary = Color.White,
        secondary = YadinPalette.GptPurple,
        onSecondary = Color.White,
        background = YadinPalette.GptBgLight,
        surface = YadinPalette.GptSurfaceLight,
        surfaceVariant = YadinPalette.GptSurfaceVarLight,
        onSurface = Color(0xFF20234A),
        onSurfaceVariant = Color(0xFF656B91),
        card = Color.White,
        outline = YadinPalette.GptOutlineLight,
        outlineVariant = Color(0xFFE5E7FA),
        success = YadinPalette.SuccessGreen,
        onSuccess = Color.White,
        warning = YadinPalette.WarningAmber,
        onWarning = Color(0xFF352600),
        error = YadinPalette.ErrorRed,
        onError = Color.White,
        info = YadinPalette.InfoBlue,
        onInfo = Color.White,
        accentPurple = YadinPalette.GptPurple,
        difficultyEasy = YadinPalette.SuccessGreen,
        difficultyMedium = YadinPalette.WarningAmber,
        difficultyHard = YadinPalette.HardOrange,
        difficultyVeryHard = YadinPalette.ErrorRed,
        heroGradient = Brush.linearGradient(
            listOf(YadinPalette.GptBlue, YadinPalette.GptPurple, YadinPalette.GptCyan)
        ),
        cardBorderBrush = Brush.linearGradient(
            listOf(
                YadinPalette.GptBlue.copy(alpha = 0.35f),
                YadinPalette.GptCyan.copy(alpha = 0.45f),
                YadinPalette.GptPurple.copy(alpha = 0.30f)
            )
        ),
        accentGlow = Color(0x30315BFF),
        onHero = Color.White
    )

    val DarkColors = YadinColors(
        primary = Color(0xFF91A5FF),
        onPrimary = Color(0xFF111329),
        secondary = Color(0xFFD0B8FF),
        onSecondary = Color(0xFF21133E),
        background = YadinPalette.GptBgDark,
        surface = YadinPalette.GptSurfaceDark,
        surfaceVariant = YadinPalette.GptSurfaceVarDark,
        onSurface = Color(0xFFF5F4FF),
        onSurfaceVariant = Color(0xFFB8B9D8),
        card = YadinPalette.GptSurfaceDark,
        outline = YadinPalette.GptOutlineDark,
        outlineVariant = Color(0xFF34365A),
        success = YadinPalette.SuccessGreenLight,
        onSuccess = Color(0xFF082D20),
        warning = YadinPalette.WarningAmberLight,
        onWarning = Color(0xFF332400),
        error = YadinPalette.ErrorRedLight,
        onError = Color(0xFF3D0711),
        info = YadinPalette.InfoBlueLight,
        onInfo = Color(0xFF101747),
        accentPurple = YadinPalette.GptLilac,
        difficultyEasy = YadinPalette.SuccessGreenLight,
        difficultyMedium = YadinPalette.WarningAmberLight,
        difficultyHard = YadinPalette.HardOrangeLight,
        difficultyVeryHard = YadinPalette.ErrorRedLight,
        heroGradient = Brush.linearGradient(
            listOf(Color(0xFF536CFF), Color(0xFF9A69FF), Color(0xFF30C9E9))
        ),
        cardBorderBrush = Brush.linearGradient(
            listOf(
                Color(0x8091A5FF),
                Color(0x8030C9E9),
                Color(0x809A69FF)
            )
        ),
        accentGlow = Color(0x4091A5FF),
        onHero = Color.White
    )

    val Dimensions = YadinDimensions(
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
        cardElevation = 8.dp,
        cardBorderAlpha = 0.55f,
        cornerExtraSmall = 10.dp,
        cornerSmall = 18.dp,
        cornerBadge = 14.dp,
        cornerMedium = 26.dp,
        cornerLarge = 32.dp,
        cornerPill = 40.dp
    )

    val Shapes = YadinShapes(
        extraSmall = RoundedCornerShape(10.dp),
        badge = RoundedCornerShape(14.dp),
        small = RoundedCornerShape(18.dp),
        medium = RoundedCornerShape(26.dp),
        large = RoundedCornerShape(32.dp),
        pill = RoundedCornerShape(40.dp),
        circle = CircleShape
    )

    val IconSizes = YadinIconSizes(
        small = 16.dp,
        medium = 20.dp,
        large = 24.dp,
        extraLarge = 32.dp,
        hero = 48.dp
    )

    fun materialColorScheme(isDark: Boolean): ColorScheme {
        val colors = if (isDark) DarkColors else LightColors
        return if (isDark) {
            darkColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                secondary = colors.secondary,
                onSecondary = colors.onSecondary,
                background = colors.background,
                surface = colors.surface,
                surfaceVariant = colors.surfaceVariant,
                onSurface = colors.onSurface,
                onSurfaceVariant = colors.onSurfaceVariant,
                outline = colors.outline,
                outlineVariant = colors.outlineVariant,
                error = colors.error,
                onError = colors.onError
            )
        } else {
            lightColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                secondary = colors.secondary,
                onSecondary = colors.onSecondary,
                background = colors.background,
                surface = colors.surface,
                surfaceVariant = colors.surfaceVariant,
                onSurface = colors.onSurface,
                onSurfaceVariant = colors.onSurfaceVariant,
                outline = colors.outline,
                outlineVariant = colors.outlineVariant,
                error = colors.error,
                onError = colors.onError
            )
        }
    }

    fun materialShapes() = androidx.compose.material3.Shapes(
        extraSmall = Shapes.extraSmall,
        small = Shapes.small,
        medium = Shapes.medium,
        large = Shapes.large,
        extraLarge = Shapes.pill
    )
}
