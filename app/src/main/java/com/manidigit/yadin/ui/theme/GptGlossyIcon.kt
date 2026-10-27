package com.manidigit.yadin.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glossy 3D icon treatment for the GPT theme.
 *
 * This is a presentation-only wrapper: callers pass an existing vector icon and
 * retain their current click handlers and content descriptions. It does not
 * alter navigation or business logic.
 */
@Composable
fun GptGlossyIcon(
    image: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    tint: Color? = null,
    enabled: Boolean = true,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    val colors = LocalYadinColors.current
    val isGpt = colors.primary == YadinPalette.GptBlue || colors.primary == Color(0xFF91A5FF)
    val shape = RoundedCornerShape(15.dp)
    val surfaceBrush = if (isGpt) {
        Brush.linearGradient(
            listOf(
                colors.primary.copy(alpha = if (enabled) 0.24f else 0.10f),
                colors.accentPurple.copy(alpha = if (enabled) 0.18f else 0.08f),
                colors.surface
            )
        )
    } else {
        Brush.linearGradient(listOf(colors.surfaceVariant, colors.surface))
    }
    val iconTint = tint ?: colors.primary

    Box(
        modifier = modifier
            .shadow(if (isGpt && enabled) 7.dp else 2.dp, shape)
            .background(surfaceBrush, shape)
            .border(
                width = 1.dp,
                brush = if (isGpt) {
                    Brush.linearGradient(
                        listOf(
                            colors.primary.copy(alpha = 0.55f),
                            colors.accentPurple.copy(alpha = 0.35f),
                            colors.surface.copy(alpha = 0.75f)
                        )
                    )
                } else {
                    Brush.linearGradient(listOf(colors.outline, colors.outlineVariant))
                },
                shape = shape
            )
            .padding(2.dp)
            .then(Modifier)
            .background(Color.Transparent, shape),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Icon(
                imageVector = image,
                contentDescription = contentDescription,
                tint = if (enabled) iconTint else colors.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.padding(1.dp)
            )
        }
    }
}
