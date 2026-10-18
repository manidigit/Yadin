package com.manidigit.yadin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun YadinTheme(
    @Suppress("UNUSED_PARAMETER") themeId: String = "gemini",
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (isDark) GeminiTheme.DarkColors else GeminiTheme.LightColors
    val dimensions = GeminiTheme.Dimensions
    val shapes = GeminiTheme.Shapes
    val iconSizes = GeminiTheme.IconSizes

    val materialColors = GeminiTheme.materialColorScheme(isDark)
    val materialShapes = GeminiTheme.materialShapes()

    CompositionLocalProvider(
        LocalYadinColors provides colors,
        LocalYadinDimensions provides dimensions,
        LocalYadinShapes provides shapes,
        LocalYadinIconSizes provides iconSizes,
        LocalYadinIcons provides YadinIcons
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = AppTypography,
            shapes = materialShapes,
            content = content
        )
    }
}
