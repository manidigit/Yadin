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
    themeId: String = "gemini",
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (isDark) GeminiDarkColors else GeminiLightColors
    val dimensions = GeminiDimensions

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
