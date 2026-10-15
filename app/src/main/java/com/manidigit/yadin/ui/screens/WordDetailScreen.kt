package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.ui.components.DifficultyBadge
import com.manidigit.yadin.ui.components.SpeakButton
import com.manidigit.yadin.ui.components.StageBadge
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits

@Composable
fun WordDetailScreen(
    word: WordDetail?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReactivate: (() -> Unit)? = null
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.word_delete_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.word_delete_dialog_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.word_btn_delete), color = colors.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (word == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.word_not_found), color = colors.onSurface)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        tint = colors.onSurface
                    )
                }

                Text(
                    text = stringResource(R.string.word_detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!word.concept.active) {
                        Text(
                            text = stringResource(R.string.word_status_inactive),
                            color = colors.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.error.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = colors.error
                        )
                    }
                }
            }

            // Inactive Word Alert Banner
            if (!word.concept.active) {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.error.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.word_trash_banner_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.error
                            )
                            Text(
                                text = stringResource(R.string.word_trash_banner_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        if (onReactivate != null) {
                            Button(
                                onClick = onReactivate,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.success,
                                    contentColor = colors.onSuccess
                                )
                            ) {
                                Text(stringResource(R.string.word_btn_restore_activate), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Word Hero Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isSourceLtr = (word.sourceContent.languageCode != "fa") && !TextUtilities.isRtlText(word.sourceContent.text)
                        CompositionLocalProvider(LocalLayoutDirection provides (if (isSourceLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                            Text(
                                text = word.sourceContent.text,
                                style = MaterialTheme.typography.headlineLarge.copy(textDirection = if (isSourceLtr) TextDirection.Ltr else TextDirection.Rtl),
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }
                        SpeakButton(
                            text = word.sourceContent.text,
                            languageCode = word.sourceContent.languageCode
                        )
                    }

                    if (word.categories.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            word.categories.forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceVariant)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                 ) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Translations Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.word_target_translations_header),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    word.targetContents.forEach { content ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                )
                                Text(
                                    text = content.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface
                                )
                            }
                            SpeakButton(
                                text = content.text,
                                languageCode = content.languageCode.ifBlank { "fa" }
                            )
                        }
                    }

                    if (!word.sourceContent.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.word_notes_header),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                        Text(
                            text = word.sourceContent.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface
                        )
                    }
                }
            }

            // Learning State (Normal Direction: es -> fa)
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.word_learning_normal_header),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_current_stage), color = colors.onSurfaceVariant)
                        word.normalLearning?.stage?.let { StageBadge(stage = it) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_difficulty_level), color = colors.onSurfaceVariant)
                        word.normalDifficulty?.current?.let { DifficultyBadge(difficulty = it) }
                    }
                    val normCorrect = word.normalDifficulty?.consecutiveCorrect ?: 0
                    val normWrong = word.normalDifficulty?.consecutiveWrong ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_streak_counter), color = colors.onSurfaceVariant)
                        val text = when {
                            normCorrect > 0 -> stringResource(R.string.word_streak_correct_fmt, normCorrect.formatDigits())
                            normWrong > 0 -> stringResource(R.string.word_streak_wrong_fmt, normWrong.formatDigits())
                            else -> stringResource(R.string.word_streak_none)
                        }
                        Text(
                            text = text,
                            fontWeight = FontWeight.Bold,
                            color = if (normCorrect > 0) colors.success else if (normWrong > 0) colors.error else colors.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_due_date), color = colors.onSurfaceVariant)
                        val normalDueText = when {
                            word.normalLearning?.stage == com.manidigit.yadin.domain.model.Stage.LEARNED -> stringResource(R.string.word_due_permanent_memory)
                            word.normalLearning?.nextReviewDay != null -> word.normalLearning.nextReviewDay
                            word.normalLearning != null -> stringResource(R.string.word_due_today)
                            else -> stringResource(R.string.word_not_in_review)
                        }
                        Text(
                            text = normalDueText,
                            fontWeight = FontWeight.Bold,
                            color = if (word.normalLearning?.stage == com.manidigit.yadin.domain.model.Stage.LEARNED) colors.success else colors.primary
                        )
                    }
                }
            }

            // Learning State (Reverse Direction: fa -> es)
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.word_learning_reverse_header),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_current_stage), color = colors.onSurfaceVariant)
                        word.reverseLearning?.stage?.let { StageBadge(stage = it) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_difficulty_level), color = colors.onSurfaceVariant)
                        word.reverseDifficulty?.current?.let { DifficultyBadge(difficulty = it) }
                    }
                    val revCorrect = word.reverseDifficulty?.consecutiveCorrect ?: 0
                    val revWrong = word.reverseDifficulty?.consecutiveWrong ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_streak_counter), color = colors.onSurfaceVariant)
                        val text = when {
                            revCorrect > 0 -> stringResource(R.string.word_streak_correct_fmt, revCorrect.formatDigits())
                            revWrong > 0 -> stringResource(R.string.word_streak_wrong_fmt, revWrong.formatDigits())
                            else -> stringResource(R.string.word_streak_none)
                        }
                        Text(
                            text = text,
                            fontWeight = FontWeight.Bold,
                            color = if (revCorrect > 0) colors.success else if (revWrong > 0) colors.error else colors.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.word_stat_due_date), color = colors.onSurfaceVariant)
                        val reverseDueText = when {
                            word.reverseLearning?.stage == com.manidigit.yadin.domain.model.Stage.LEARNED -> stringResource(R.string.word_due_permanent_memory)
                            word.reverseLearning?.nextReviewDay != null -> word.reverseLearning.nextReviewDay
                            word.reverseLearning != null -> stringResource(R.string.word_due_today)
                            else -> stringResource(R.string.word_not_in_review)
                        }
                        Text(
                            text = reverseDueText,
                            fontWeight = FontWeight.Bold,
                            color = if (word.reverseLearning?.stage == com.manidigit.yadin.domain.model.Stage.LEARNED) colors.success else colors.primary
                        )
                    }
                }
            }
        }

        // Edit Button
        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.word_btn_edit), fontWeight = FontWeight.Bold)
        }
    }
}
