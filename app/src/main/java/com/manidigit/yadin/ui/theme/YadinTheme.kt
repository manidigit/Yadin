package com.manidigit.yadin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun YadinTheme(
    themeId: String = "gtp",
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val cleanTheme = themeId.lowercase()
    val colors = when {
        cleanTheme == "googoli" && isDark -> GoogoliDarkColors
        cleanTheme == "googoli" && !isDark -> GoogoliLightColors
        cleanTheme == "claude" && isDark -> ClaudeDarkColors
        cleanTheme == "claude" && !isDark -> ClaudeLightColors
        cleanTheme == "gemini" && isDark -> GeminiDarkColors
        cleanTheme == "gemini" && !isDark -> GeminiLightColors
        cleanTheme == "gtp" && isDark -> GtpDarkColors
        cleanTheme == "gtp" && !isDark -> GtpLightColors
        isDark -> GoogoliDarkColors
        else -> GoogoliLightColors
    }

    val dimensions = when (cleanTheme) {
        "googoli" -> GoogoliDimensions
        "claude" -> ClaudeDimensions
        "gemini" -> GeminiDimensions
        else -> GtpDimensions
    }

    val materialColors = if (isDark) {
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
            error = colors.error
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
            error = colors.error
        )
    }

    val shapes = Shapes(
        small = RoundedCornerShape(dimensions.cornerSmall),
        medium = RoundedCornerShape(dimensions.cornerMedium),
        large = RoundedCornerShape(dimensions.cornerLarge)
    )

    CompositionLocalProvider(
        LocalYadinColors provides colors,
        LocalYadinDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = AppTypography,
            shapes = shapes,
            content = content
        )
    }
}
