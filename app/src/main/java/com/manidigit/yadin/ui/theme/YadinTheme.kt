package com.manidigit.yadin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun YadinTheme(
    themeId: String = "gpt",
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val useGpt = themeId.equals("gpt", ignoreCase = true)
    val colors = when {
        useGpt && isDark -> GptTheme.DarkColors
        useGpt -> GptTheme.LightColors
        isDark -> GeminiTheme.DarkColors
        else -> GeminiTheme.LightColors
    }
    val dimensions = if (useGpt) GptTheme.Dimensions else GeminiTheme.Dimensions
    val shapes = if (useGpt) GptTheme.Shapes else GeminiTheme.Shapes
    val iconSizes = if (useGpt) GptTheme.IconSizes else GeminiTheme.IconSizes
    val materialColors = if (useGpt) GptTheme.materialColorScheme(isDark) else GeminiTheme.materialColorScheme(isDark)
    val materialShapes = if (useGpt) GptTheme.materialShapes() else GeminiTheme.materialShapes()

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
