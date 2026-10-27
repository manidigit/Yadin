package com.manidigit.yadin.ui.theme

import android.graphics.Picture
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.caverock.androidsvg.SVG

/**
 * Renders a checked-in GPT Glossy SVG asset.
 *
 * Kept separate from the existing icon APIs so GPT assets can be adopted
 * screen-by-screen without changing Gemini visuals.
 */
@Composable
fun GptGlossyIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val context = LocalContext.current
    val safeName = remember(name) {
        name.takeIf { it.matches(Regex("[a-z][a-zA-Z0-9]*")) }
    }
    val svg = remember(context, safeName) {
        safeName?.let {
            runCatching {
                SVG.getFromAsset(context.assets, "icons/gpt-glossy/$it.svg")
            }.getOrNull()
        }
    }

    Canvas(
        modifier = modifier.size(size).drawWithCache {
            val picture: Picture? = svg?.let {
                runCatching {
                    it.renderToPicture(
                        this.size.width.toInt().coerceAtLeast(1),
                        this.size.height.toInt().coerceAtLeast(1)
                    )
                }.getOrNull()
            }
            onDrawBehind {
                if (picture != null) {
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawPicture(picture)
                    }
                }
            }
        }
    ) {}
}
