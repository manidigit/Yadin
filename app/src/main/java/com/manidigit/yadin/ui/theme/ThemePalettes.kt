package com.manidigit.yadin.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
