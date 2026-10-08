package com.manidigit.yadin.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val GtpLightColors = YadinColors(
    primary = YadinPalette.GtpPurple,
    onPrimary = Color(0xFFFFFFFF),
    secondary = YadinPalette.GtpCyan,
    onSecondary = Color(0xFFFFFFFF),
    background = YadinPalette.GtpBgLight,
    surface = YadinPalette.GtpSurfaceLight,
    surfaceVariant = YadinPalette.GtpSurfaceVarLight,
    onSurface = Color(0xFF171225),
    onSurfaceVariant = Color(0xFF6F6485),
    card = YadinPalette.GtpCardLight,
    outline = YadinPalette.GtpOutlineLight,
    success = YadinPalette.SuccessGreen,
    warning = YadinPalette.WarningAmber,
    error = YadinPalette.ErrorRed,
    info = YadinPalette.InfoBlue,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.GtpPurple, YadinPalette.GtpCyan)),
    cardBorderBrush = null,
    accentGlow = Color(0x337C3AED),
    isGemini = false
)

val GtpDarkColors = YadinColors(
    primary = YadinPalette.GtpPurpleLight,
    onPrimary = Color(0xFF15101F),
    secondary = YadinPalette.GtpCyanLight,
    onSecondary = Color(0xFF090711),
    background = YadinPalette.GtpBgDark,
    surface = YadinPalette.GtpSurfaceDark,
    surfaceVariant = YadinPalette.GtpSurfaceVarDark,
    onSurface = Color(0xFFF7F2FF),
    onSurfaceVariant = Color(0xFFC4B9D6),
    card = YadinPalette.GtpCardDark,
    outline = YadinPalette.GtpOutlineDark,
    success = YadinPalette.SuccessGreenLight,
    warning = YadinPalette.WarningAmberLight,
    error = YadinPalette.ErrorRedLight,
    info = YadinPalette.InfoBlueLight,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.GtpPurpleLight, YadinPalette.GtpCyanLight)),
    cardBorderBrush = null,
    accentGlow = Color(0x44A78BFA),
    isGemini = false
)

val GeminiLightColors = YadinColors(
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
    success = YadinPalette.SuccessGreen,
    warning = YadinPalette.WarningAmber,
    error = YadinPalette.ErrorRed,
    info = YadinPalette.InfoBlue,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.GeminiBlue, YadinPalette.GeminiSky, YadinPalette.GeminiPurple)),
    cardBorderBrush = Brush.linearGradient(
        listOf(
            YadinPalette.GeminiBlue.copy(alpha = 0.5f),
            YadinPalette.GeminiSky.copy(alpha = 0.6f),
            YadinPalette.GeminiPurple.copy(alpha = 0.35f)
        )
    ),
    accentGlow = Color(0x281A73E8),
    isGemini = true
)

val GeminiDarkColors = YadinColors(
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
    success = YadinPalette.SuccessGreenLight,
    warning = YadinPalette.WarningAmberLight,
    error = YadinPalette.ErrorRedLight,
    info = YadinPalette.InfoBlueLight,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.GeminiBlueLight, YadinPalette.GeminiSkyLight, YadinPalette.GeminiPurpleLight)),
    cardBorderBrush = Brush.linearGradient(
        listOf(
            YadinPalette.GeminiBlueLight.copy(alpha = 0.65f),
            YadinPalette.GeminiSkyLight.copy(alpha = 0.7f),
            YadinPalette.GeminiPurpleLight.copy(alpha = 0.45f)
        )
    ),
    accentGlow = Color(0x408AB4F8),
    isGemini = true
)

val ClaudeLightColors = YadinColors(
    primary = YadinPalette.ClaudeTerracotta,
    onPrimary = Color(0xFFFFFFFF),
    secondary = YadinPalette.ClaudeSage,
    onSecondary = Color(0xFFFFFFFF),
    background = YadinPalette.ClaudeBgLight,
    surface = YadinPalette.ClaudeSurfaceLight,
    surfaceVariant = YadinPalette.ClaudeSurfaceVarLight,
    onSurface = Color(0xFF262320),
    onSurfaceVariant = Color(0xFF6B6258),
    card = YadinPalette.ClaudeSurfaceLight,
    outline = YadinPalette.ClaudeOutlineLight,
    success = YadinPalette.ClaudeSage,
    warning = YadinPalette.ClaudeAmber,
    error = YadinPalette.ErrorRed,
    info = YadinPalette.InfoBlue,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.ClaudeTerracotta, YadinPalette.ClaudeAmber, Color(0xFF8B4513))),
    cardBorderBrush = Brush.linearGradient(listOf(YadinPalette.ClaudeOutlineLight, Color(0xFFD5C7B5))),
    accentGlow = Color(0x28C15F3D),
    isGemini = false
)

val ClaudeDarkColors = YadinColors(
    primary = YadinPalette.ClaudeTerracottaLight,
    onPrimary = Color(0xFF1E140E),
    secondary = YadinPalette.ClaudeSageLight,
    onSecondary = Color(0xFF0F1A12),
    background = YadinPalette.ClaudeBgDark,
    surface = YadinPalette.ClaudeSurfaceDark,
    surfaceVariant = YadinPalette.ClaudeSurfaceVarDark,
    onSurface = Color(0xFFF5EFEA),
    onSurfaceVariant = Color(0xFFB8ADA3),
    card = YadinPalette.ClaudeSurfaceDark,
    outline = YadinPalette.ClaudeOutlineDark,
    success = YadinPalette.ClaudeSageLight,
    warning = YadinPalette.WarningAmberLight,
    error = YadinPalette.ErrorRedLight,
    info = YadinPalette.InfoBlueLight,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.ClaudeTerracottaLight, YadinPalette.WarningAmberLight, YadinPalette.ClaudeTerracotta)),
    cardBorderBrush = Brush.linearGradient(listOf(YadinPalette.ClaudeOutlineDark, Color(0xFF4C4238))),
    accentGlow = Color(0x35E08466),
    isGemini = false
)

val GoogoliLightColors = YadinColors(
    primary = YadinPalette.GoogoliTeal,
    onPrimary = Color(0xFFFFFFFF),
    secondary = YadinPalette.GoogoliPeach,
    onSecondary = Color(0xFFFFFFFF),
    background = YadinPalette.GoogoliBgLight,
    surface = YadinPalette.GoogoliSurfaceLight,
    surfaceVariant = YadinPalette.GoogoliSurfaceVarLight,
    onSurface = Color(0xFF132A24),
    onSurfaceVariant = Color(0xFF4A6B62),
    card = YadinPalette.GoogoliSurfaceLight,
    outline = YadinPalette.GoogoliOutlineLight,
    success = YadinPalette.GoogoliTeal,
    warning = YadinPalette.GoogoliPeach,
    error = YadinPalette.ErrorRed,
    info = YadinPalette.InfoBlue,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.GoogoliTeal, YadinPalette.GoogoliPeach, YadinPalette.GoogoliLavender)),
    cardBorderBrush = Brush.linearGradient(listOf(YadinPalette.GoogoliOutlineLight, Color(0xFFD6F5EC))),
    accentGlow = Color(0x3014B8A6),
    isGemini = false
)

val GoogoliDarkColors = YadinColors(
    primary = YadinPalette.GoogoliTealLight,
    onPrimary = Color(0xFF06231C),
    secondary = YadinPalette.GoogoliPeachLight,
    onSecondary = Color(0xFF261008),
    background = YadinPalette.GoogoliBgDark,
    surface = YadinPalette.GoogoliSurfaceDark,
    surfaceVariant = YadinPalette.GoogoliSurfaceVarDark,
    onSurface = Color(0xFFE6FAF4),
    onSurfaceVariant = Color(0xFF98BEB4),
    card = YadinPalette.GoogoliSurfaceDark,
    outline = YadinPalette.GoogoliOutlineDark,
    success = YadinPalette.GoogoliTealLight,
    warning = YadinPalette.GoogoliPeachLight,
    error = YadinPalette.ErrorRedLight,
    info = YadinPalette.InfoBlueLight,
    heroGradient = Brush.linearGradient(listOf(YadinPalette.GoogoliTealLight, YadinPalette.GoogoliPeachLight, YadinPalette.GoogoliLavender)),
    cardBorderBrush = Brush.linearGradient(listOf(YadinPalette.GoogoliOutlineDark, Color(0xFF3B665A))),
    accentGlow = Color(0x3D2DD4BF),
    isGemini = false
)
