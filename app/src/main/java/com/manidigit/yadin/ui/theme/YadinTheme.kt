package com.manidigit.yadin.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
