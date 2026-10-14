package com.manidigit.yadin.ui.components

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import com.manidigit.yadin.ui.util.TtsManager
import com.manidigit.yadin.ui.util.formatDigits
import com.manidigit.yadin.ui.util.toPersianDigits
import com.manidigit.yadin.R
import androidx.compose.ui.res.stringResource
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
import com.manidigit.yadin.ui.theme.LocalYadinShapes
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
    val shapes = LocalYadinShapes.current
    val resolvedShape = shape ?: shapes.medium
    val cardColor = backgroundColor ?: colors.card

    val resolvedBorder = borderStroke ?: if (colors.cardBorderBrush != null) {
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
    val shapes = LocalYadinShapes.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensions.itemGapMedium)
    ) {
        val items = listOf(
            Triple(VocabularyDifficulty.EASY, stringResource(R.string.difficulty_easy), colors.success),
            Triple(VocabularyDifficulty.MEDIUM, stringResource(R.string.difficulty_medium), colors.info),
            Triple(VocabularyDifficulty.HARD, stringResource(R.string.difficulty_hard), colors.warning),
            Triple(VocabularyDifficulty.VERY_HARD, stringResource(R.string.difficulty_very_hard), colors.error)
        )

        items.forEach { (diff, label, color) ->
            val count = counts[diff] ?: 0
            val shape = shapes.small

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(color.copy(alpha = 0.16f))
                    .border(
                        width = 1.dp,
                        color = color.copy(alpha = 0.50f),
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
                        text = count.formatDigits(),
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
    val shapes = LocalYadinShapes.current
    val (title, color) = when (stage) {
        Stage.DAILY -> stringResource(R.string.stage_daily) to colors.primary
        Stage.WEEKLY -> stringResource(R.string.stage_weekly) to colors.info
        Stage.MONTHLY -> stringResource(R.string.stage_monthly) to colors.warning
        Stage.LEARNED -> stringResource(R.string.stage_learned) to colors.success
    }

    val shape = shapes.badge

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
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun DifficultyBadge(difficulty: VocabularyDifficulty, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val shapes = LocalYadinShapes.current
    val (title, color) = when (difficulty) {
        VocabularyDifficulty.EASY -> stringResource(R.string.difficulty_easy) to colors.success
        VocabularyDifficulty.MEDIUM -> stringResource(R.string.difficulty_medium) to colors.info
        VocabularyDifficulty.HARD -> stringResource(R.string.difficulty_hard) to colors.warning
        VocabularyDifficulty.VERY_HARD -> stringResource(R.string.difficulty_very_hard) to colors.error
    }

    val shape = shapes.badge

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
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun StreakChip(streakDays: Int, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val shapes = LocalYadinShapes.current
    val shape = shapes.pill

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
            contentDescription = stringResource(R.string.content_desc_streak),
            tint = colors.warning,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "${streakDays.formatDigits()} ${stringResource(R.string.home_streak_days_suffix)}",
            color = colors.warning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}



@Composable
fun SpeakButton(
    text: String,
    languageCode: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalYadinColors.current

    IconButton(
        onClick = {
            TtsManager.speak(context, text, languageCode)
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = stringResource(R.string.content_desc_speak),
            tint = colors.primary
        )
    }
}
