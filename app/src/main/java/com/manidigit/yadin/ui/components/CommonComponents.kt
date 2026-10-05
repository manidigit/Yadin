package com.manidigit.yadin.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import java.util.Locale

@Composable
fun YadinCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape? = null,
    backgroundColor: Color? = null,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val resolvedShape = shape ?: RoundedCornerShape(dimensions.cornerMedium)
    val cardColor = backgroundColor ?: colors.card

    val resolvedBorder = borderStroke ?: if (colors.isGemini && colors.cardBorderBrush != null) {
        BorderStroke(1.2.dp, colors.cardBorderBrush)
    } else {
        BorderStroke(1.dp, colors.outline.copy(alpha = dimensions.cardBorderAlpha))
    }

    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(resolvedShape)
                        .clickable { onClick() }
                } else Modifier
            ),
        shape = resolvedShape,
        color = cardColor,
        tonalElevation = dimensions.cardElevation,
        border = resolvedBorder,
        content = content
    )
}

@Composable
fun DifficultyCounterRow(
    counts: Map<VocabularyDifficulty, Int>,
    onSelectDifficulty: ((VocabularyDifficulty) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val items = listOf(
            Triple(VocabularyDifficulty.EASY, "آسان", colors.success),
            Triple(VocabularyDifficulty.MEDIUM, "متوسط", colors.info),
            Triple(VocabularyDifficulty.HARD, "سخت", colors.warning),
            Triple(VocabularyDifficulty.VERY_HARD, "خیلی سخت", colors.error)
        )

        items.forEach { (diff, label, color) ->
            val count = counts[diff] ?: 0
            val shape = RoundedCornerShape(dimensions.cornerSmall)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(color.copy(alpha = if (colors.isGemini) 0.16f else 0.10f))
                    .border(
                        width = 1.dp,
                        color = color.copy(alpha = if (colors.isGemini) 0.50f else 0.30f),
                        shape = shape
                    )
                    .then(
                        if (onSelectDifficulty != null) {
                            Modifier.clickable { onSelectDifficulty(diff) }
                        } else Modifier
                    )
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.sp,
                        color = colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun StageBadge(stage: Stage, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val (title, color) = when (stage) {
        Stage.DAILY -> "روزانه" to colors.primary
        Stage.WEEKLY -> "هفتگی" to colors.info
        Stage.MONTHLY -> "ماهانه" to colors.warning
        Stage.LEARNED -> "یادگرفته" to colors.success
    }

    val shape = RoundedCornerShape(if (colors.isGemini) 12.dp else 6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(0.8.dp, color.copy(alpha = 0.45f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = title,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DifficultyBadge(difficulty: VocabularyDifficulty, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val (title, color) = when (difficulty) {
        VocabularyDifficulty.EASY -> "ساده" to colors.success
        VocabularyDifficulty.MEDIUM -> "متوسط" to colors.info
        VocabularyDifficulty.HARD -> "سخت" to colors.warning
        VocabularyDifficulty.VERY_HARD -> "خیلی سخت" to colors.error
    }

    val shape = RoundedCornerShape(if (colors.isGemini) 12.dp else 6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.15f))
            .border(0.8.dp, color.copy(alpha = 0.40f), shape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = title,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StreakChip(streakDays: Int, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val shape = RoundedCornerShape(dimensions.cornerPill)

    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.warning.copy(alpha = 0.18f))
            .border(1.dp, colors.warning.copy(alpha = 0.45f), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocalFireDepartment,
            contentDescription = "رگبار تمرین",
            tint = colors.warning,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "$streakDays روز",
            color = colors.warning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val dimensions = LocalYadinDimensions.current
    val colors = LocalYadinColors.current

    YadinCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(dimensions.cornerSmall))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = accentColor,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SpeakButton(
    text: String,
    languageCode: String = "es",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalYadinColors.current

    IconButton(
        onClick = {
            var tts: TextToSpeech? = null
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val loc = if (languageCode == "es") Locale("es", "ES") else Locale.US
                    tts?.language = loc
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
                }
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "پخش تلفظ صوتی",
            tint = colors.primary
        )
    }
}
