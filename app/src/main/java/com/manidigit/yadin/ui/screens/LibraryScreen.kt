package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import com.manidigit.yadin.ui.theme.YadinIcons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.manidigit.yadin.domain.algorithm.VocabularyParser
import com.manidigit.yadin.domain.model.AuditReviewItem
import com.manidigit.yadin.domain.model.AuditUserDecision
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.domain.model.AuditActionType
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.LibraryAuditReport
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.ui.components.DifficultyBadge
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.SpeakButton
import com.manidigit.yadin.ui.components.StageBadge
import com.manidigit.yadin.ui.components.VocabStatType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.getDifficultyColor
import com.manidigit.yadin.ui.components.getDifficultyIcon
import com.manidigit.yadin.ui.components.getStageColor
import com.manidigit.yadin.ui.components.getStageIcon
import com.manidigit.yadin.ui.components.getVocabStatIcon
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits

@Composable
fun LibraryScreen(
    words: List<WordDetail>,
    searchQuery: String,
    categories: List<Category>,
    selectedCategory: String?,
    selectedStage: Stage?,
    selectedDifficulty: com.manidigit.yadin.domain.model.VocabularyDifficulty? = null,
    showOnlyInactive: Boolean = false,
    hasMoreResults: Boolean = false,
    isLoadingMore: Boolean = false,
    appLanguageDirection: com.manidigit.yadin.domain.model.CardDirection = com.manidigit.yadin.domain.model.CardDirection.NORMAL,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onStageFilterChange: (Stage?) -> Unit,
    onDifficultyFilterChange: (com.manidigit.yadin.domain.model.VocabularyDifficulty?) -> Unit = {},
    onInactiveFilterToggle: (Boolean) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onReactivateWord: ((String) -> Unit)? = null,
    onAddNewCategory: ((String) -> Unit)? = null,
    onEditCategory: ((String, String) -> Unit)? = null,
    onDeleteCategory: ((String) -> Unit)? = null,
    onSelectWord: (String) -> Unit,
    onAddWord: () -> Unit,
    onBatchImport: () -> Unit = {},
    isAuditing: Boolean = false,
    auditProgress: Float = 0f,
    auditReport: LibraryAuditReport? = null,
    onScanLibrary: () -> Unit = {},
    onApplyAuditFixes: ((List<AuditReviewItem>) -> Unit)? = null,
    onRunAuditAndMerge: () -> Unit = {},
    onClearAuditReport: () -> Unit = {},
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var showAddOptionsDialog by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimensions.screenPadding)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Standardized Header
            YadinScreenHeader(
                title = if (showOnlyInactive) stringResource(R.string.library_trash_title) else stringResource(R.string.library_title),
                subtitle = stringResource(R.string.library_loaded_words_count, words.size.formatDigits()),
                onBack = onBack,
                trailingContent = if (!showOnlyInactive) {
                    {
                        FilledTonalButton(
                            onClick = { showAuditDialog = true },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(dimensions.cornerBadge)
                        ) {
                            Icon(
                                imageVector = YadinIcons.smartAudit,
                                contentDescription = stringResource(R.string.library_audit_btn),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.library_audit_btn_text),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else null
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.library_search_placeholder)) },
                leadingIcon = {
                    Icon(YadinIcons.search, contentDescription = null, tint = colors.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(YadinIcons.clear, contentDescription = stringResource(R.string.action_clear), tint = colors.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Unified 2x2 Filter Grid (وضعیت، مرحله مرور، سطح سختی، دسته‌بندی موضوعی)
            val statusActiveLabel = stringResource(R.string.filter_status_active)
            val statusTrashLabel = stringResource(R.string.filter_status_trash)
            val stageAllLabel = stringResource(R.string.stage_all)
            val stageDailyLabel = stringResource(R.string.stage_daily)
            val stageWeeklyLabel = stringResource(R.string.stage_weekly)
            val stageMonthlyLabel = stringResource(R.string.stage_monthly)
            val stageLearnedLabel = stringResource(R.string.stage_learned)
            val diffAllLabel = stringResource(R.string.difficulty_all)
            val diffEasyLabel = stringResource(R.string.diff_easy)
            val diffMedLabel = stringResource(R.string.diff_medium)
            val diffHardLabel = stringResource(R.string.diff_hard)
            val diffVeryHardLabel = stringResource(R.string.diff_very_hard)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Filter 1: Status (وضعیت)
                    LibraryFilterDropdown(
                        label = stringResource(R.string.filter_status),
                        selectedOption = if (showOnlyInactive) statusTrashLabel else statusActiveLabel,
                        icon = if (showOnlyInactive) YadinIcons.delete else YadinIcons.statusSuccess,
                        isFilterActive = showOnlyInactive,
                        activeAccentColor = colors.error,
                        onClearFilter = if (showOnlyInactive) ({ onInactiveFilterToggle(false) }) else null,
                        options = listOf(
                            LibraryFilterOption(statusActiveLabel, YadinIcons.statusSuccess, colors.primary) { onInactiveFilterToggle(false) },
                            LibraryFilterOption(statusTrashLabel, YadinIcons.delete, colors.error) { onInactiveFilterToggle(true) }
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Filter 2: Stage (مرحله مرور)
                    val currentStageLabel = when (selectedStage) {
                        Stage.DAILY -> stageDailyLabel
                        Stage.WEEKLY -> stageWeeklyLabel
                        Stage.MONTHLY -> stageMonthlyLabel
                        Stage.LEARNED -> stageLearnedLabel
                        null -> stageAllLabel
                    }
                    val stageIcon = if (selectedStage != null) getStageIcon(selectedStage) else getVocabStatIcon(VocabStatType.MASTERED)
                    val stageAccentColor = if (selectedStage != null) getStageColor(selectedStage, colors) else colors.primary

                    LibraryFilterDropdown(
                        label = stringResource(R.string.filter_stage),
                        selectedOption = currentStageLabel,
                        icon = stageIcon,
                        isFilterActive = selectedStage != null,
                        activeAccentColor = stageAccentColor,
                        onClearFilter = if (selectedStage != null) ({ onStageFilterChange(null) }) else null,
                        options = listOf(
                            LibraryFilterOption(stageAllLabel, getVocabStatIcon(VocabStatType.MASTERED), colors.primary) { onStageFilterChange(null) },
                            LibraryFilterOption(stageDailyLabel, getStageIcon(Stage.DAILY), getStageColor(Stage.DAILY, colors)) { onStageFilterChange(Stage.DAILY) },
                            LibraryFilterOption(stageWeeklyLabel, getStageIcon(Stage.WEEKLY), getStageColor(Stage.WEEKLY, colors)) { onStageFilterChange(Stage.WEEKLY) },
                            LibraryFilterOption(stageMonthlyLabel, getStageIcon(Stage.MONTHLY), getStageColor(Stage.MONTHLY, colors)) { onStageFilterChange(Stage.MONTHLY) },
                            LibraryFilterOption(stageLearnedLabel, getStageIcon(Stage.LEARNED), getStageColor(Stage.LEARNED, colors)) { onStageFilterChange(Stage.LEARNED) }
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Filter 3: Difficulty (سطح سختی)
                    val currentDiffLabel = when (selectedDifficulty) {
                        com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY -> diffEasyLabel
                        com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM -> diffMedLabel
                        com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD -> diffHardLabel
                        com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD -> diffVeryHardLabel
                        null -> diffAllLabel
                    }
                    val diffIcon = if (selectedDifficulty != null) getDifficultyIcon(selectedDifficulty) else getDifficultyIcon(VocabularyDifficulty.MEDIUM)
                    val diffAccentColor = if (selectedDifficulty != null) getDifficultyColor(selectedDifficulty, colors) else colors.primary

                    LibraryFilterDropdown(
                        label = stringResource(R.string.filter_difficulty),
                        selectedOption = currentDiffLabel,
                        icon = diffIcon,
                        isFilterActive = selectedDifficulty != null,
                        activeAccentColor = diffAccentColor,
                        onClearFilter = if (selectedDifficulty != null) ({ onDifficultyFilterChange(null) }) else null,
                        options = listOf(
                            LibraryFilterOption(
                                diffAllLabel,
                                getDifficultyIcon(VocabularyDifficulty.MEDIUM),
                                colors.primary
                            ) { onDifficultyFilterChange(null) },
                            LibraryFilterOption(
                                diffEasyLabel,
                                getDifficultyIcon(com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY),
                                getDifficultyColor(com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY, colors)
                            ) { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY) },
                            LibraryFilterOption(
                                diffMedLabel,
                                getDifficultyIcon(com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM),
                                getDifficultyColor(com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM, colors)
                            ) { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM) },
                            LibraryFilterOption(
                                diffHardLabel,
                                getDifficultyIcon(com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD),
                                getDifficultyColor(com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD, colors)
                            ) { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD) },
                            LibraryFilterOption(
                                diffVeryHardLabel,
                                getDifficultyIcon(com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD),
                                getDifficultyColor(com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD, colors)
                            ) { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD) }
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Filter 4: Category (دسته‌بندی موضوعی)
                    ExposedCategoryDropdown(
                        categories = categories,
                        selectedCategoryId = selectedCategory,
                        onSelectCategory = { onCategoryFilterChange(it) },
                        label = stringResource(R.string.category_label),
                        onAddNewCategory = onAddNewCategory,
                        onEditCategory = onEditCategory,
                        onDeleteCategory = onDeleteCategory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Word List
            if (words.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showOnlyInactive) stringResource(R.string.library_empty_trash) else stringResource(R.string.library_empty_search),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(words, key = { it.concept.id }) { word ->
                        WordItemCard(
                            word = word,
                            direction = appLanguageDirection,
                            onClick = { onSelectWord(word.concept.id) },
                            onReactivate = if (!word.concept.active && onReactivateWord != null) {
                                { onReactivateWord(word.concept.id) }
                            } else null
                        )
                    }

                    if (hasMoreResults) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingMore) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = colors.primary,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    androidx.compose.material3.OutlinedButton(
                                        onClick = onLoadMore,
                                        shape = RoundedCornerShape(dimensions.cornerMedium)
                                    ) {
                                        Text(stringResource(R.string.library_load_more))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(76.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddOptionsDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            shape = CircleShape
        ) {
            Icon(YadinIcons.add, contentDescription = stringResource(R.string.library_add_word))
        }

        // Dialog for adding single or batch
        if (showAddOptionsDialog) {
            AddWordChoiceDialog(
                onDismiss = { showAddOptionsDialog = false },
                onAddSingle = {
                    showAddOptionsDialog = false
                    onAddWord()
                },
                onAddBatch = {
                    showAddOptionsDialog = false
                    onBatchImport()
                }
            )
        }

        // Dialog for audit & merge
        if (showAuditDialog) {
            LibraryAuditDialog(
                isAuditing = isAuditing,
                auditProgress = auditProgress,
                auditReport = auditReport,
                onDismiss = {
                    showAuditDialog = false
                    onClearAuditReport()
                },
                onScanLibrary = onScanLibrary,
                onApplyAuditFixes = { items ->
                    if (onApplyAuditFixes != null) {
                        onApplyAuditFixes(items)
                    } else {
                        onRunAuditAndMerge()
                    }
                }
            )
        }
    }
}

@Composable
fun WordItemCard(
    word: WordDetail,
    direction: com.manidigit.yadin.domain.model.CardDirection,
    onClick: () -> Unit,
    onReactivate: (() -> Unit)? = null
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val (sourceFlag, targetFlag) = if (direction == com.manidigit.yadin.domain.model.CardDirection.NORMAL) {
        "🇪🇸" to "🇮🇷"
    } else {
        "🇮🇷" to "🇪🇸"
    }

    val activeLearning = if (direction == com.manidigit.yadin.domain.model.CardDirection.NORMAL) {
        word.normalLearning
    } else {
        word.reverseLearning
    }

    val activeDifficulty = if (direction == com.manidigit.yadin.domain.model.CardDirection.NORMAL) {
        word.normalDifficulty
    } else {
        word.reverseDifficulty
    }

    YadinCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .clickable(onClick = onClick),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = sourceFlag,
                            fontSize = 14.sp
                        )
                        val isSourceLtr = (direction == com.manidigit.yadin.domain.model.CardDirection.NORMAL) || !TextUtilities.isRtlText(word.sourceContent.text)
                        CompositionLocalProvider(LocalLayoutDirection provides (if (isSourceLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                            Text(
                                text = word.sourceContent.text,
                                style = MaterialTheme.typography.titleMedium.copy(textDirection = if (isSourceLtr) TextDirection.Ltr else TextDirection.Rtl),
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!word.concept.active) {
                            Text(
                                text = stringResource(R.string.word_status_deleted),
                                color = colors.error,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.error.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        } else {
                            activeLearning?.stage?.let { StageBadge(stage = it) }
                            activeDifficulty?.current?.let { DifficultyBadge(difficulty = it) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Target Translation
                val trans = word.targetContents.joinToString("، ") { it.text }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = targetFlag,
                        fontSize = 14.sp
                    )
                    Text(
                        text = trans.ifEmpty { stringResource(R.string.word_no_translation) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!word.sourceContent.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = word.sourceContent.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!word.concept.active && onReactivate != null) {
                    TextButton(
                        onClick = onReactivate,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.action_restore), color = colors.success, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                SpeakButton(text = word.sourceContent.text, languageCode = word.sourceContent.languageCode)
            }
        }
    }
}

@Composable
fun AddWordChoiceDialog(
    onDismiss: () -> Unit,
    onAddSingle: () -> Unit,
    onAddBatch: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = YadinIcons.add,
                    contentDescription = null,
                    tint = colors.primary
                )
                Text(
                    text = stringResource(R.string.library_add_choice_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.library_add_choice_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )

                // Single Add Option
                YadinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerMedium))
                        .clickable { onAddSingle() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = YadinIcons.edit,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.library_add_single_title),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Text(
                                text = stringResource(R.string.library_add_single_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Batch Import Option
                YadinCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensions.cornerMedium))
                        .clickable { onAddBatch() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = YadinIcons.batchImport,
                                contentDescription = null,
                                tint = colors.success,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.library_add_batch_title),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Text(
                                text = stringResource(R.string.library_add_batch_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun LibraryAuditDialog(
    isAuditing: Boolean,
    auditProgress: Float,
    auditReport: LibraryAuditReport?,
    onDismiss: () -> Unit,
    onScanLibrary: () -> Unit,
    onApplyAuditFixes: (List<AuditReviewItem>) -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var showManualReviewDialog by remember { mutableStateOf(false) }
    var fixesApplied by remember { mutableStateOf(false) }

    val reviewItemsState = remember(auditReport) {
        mutableStateListOf<AuditReviewItem>().apply {
            if (auditReport != null) {
                addAll(auditReport.reviewItems)
            }
        }
    }
    val resolvedItemsState = remember(auditReport) {
        mutableStateListOf<AuditReviewItem>()
    }

    if (showManualReviewDialog) {
        AuditManualReviewDialog(
            items = reviewItemsState,
            onDismiss = { showManualReviewDialog = false },
            onSaveItem = { updatedItem ->
                val idx = reviewItemsState.indexOfFirst { it.conceptId == updatedItem.conceptId }
                if (idx != -1) {
                    reviewItemsState[idx] = updatedItem
                }
            },
            onResolveItem = { resolvedItem ->
                reviewItemsState.removeAll { it.conceptId == resolvedItem.conceptId }
                val existingIdx = resolvedItemsState.indexOfFirst { it.conceptId == resolvedItem.conceptId }
                if (existingIdx != -1) {
                    resolvedItemsState[existingIdx] = resolvedItem
                } else {
                    resolvedItemsState.add(resolvedItem)
                }
                if (reviewItemsState.isEmpty()) {
                    showManualReviewDialog = false
                    fixesApplied = true
                    onApplyAuditFixes(resolvedItemsState.toList())
                }
            },
            onConfirmAndClose = {
                val allToApply = mutableListOf<AuditReviewItem>()
                allToApply.addAll(resolvedItemsState)
                reviewItemsState.forEach { remaining ->
                    val hasChanges = TextUtilities.cleanText(remaining.suggestedWord) != TextUtilities.cleanText(remaining.originalWord) ||
                        TextUtilities.cleanText(remaining.suggestedTranslation) != TextUtilities.cleanText(remaining.originalTranslation) ||
                        !remaining.suggestedNote.isNullOrBlank() ||
                        remaining.userDecision != AuditUserDecision.PENDING
                    val finalized = if (hasChanges) {
                        if (remaining.userDecision == AuditUserDecision.PENDING) {
                            remaining.copy(userDecision = AuditUserDecision.EDITED)
                        } else {
                            remaining
                        }
                    } else {
                        remaining.copy(userDecision = AuditUserDecision.KEEP_ORIGINAL_DISMISS)
                    }
                    allToApply.add(finalized)
                }
                reviewItemsState.clear()
                showManualReviewDialog = false
                fixesApplied = true
                onApplyAuditFixes(allToApply)
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            if (!isAuditing) onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = YadinIcons.smartAudit,
                    contentDescription = null,
                    tint = colors.primary
                )
                Text(
                    text = stringResource(R.string.audit_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isAuditing) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (auditReport == null)
                                stringResource(R.string.audit_scan_in_progress)
                            else
                                stringResource(R.string.audit_in_progress),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (auditProgress > 0f) {
                            LinearProgressIndicator(
                                progress = { auditProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = colors.primary
                            )
                        }
                    }
                } else if (auditReport != null) {
                    // Result Summary Card
                    YadinCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.audit_result_title),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.audit_total_scanned), fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(stringResource(R.string.library_total_count, auditReport.totalScanned.formatDigits()), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.audit_merged_count), fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(stringResource(R.string.library_total_count, auditReport.mergedCount.formatDigits()), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.audit_cleaned_count), fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(stringResource(R.string.library_total_count, auditReport.cleanedCount.formatDigits()), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.success)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.audit_remaining_count), fontSize = 12.sp, color = colors.onSurfaceVariant)
                                Text(stringResource(R.string.library_total_count, auditReport.totalRemaining.formatDigits()), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
                            }
                        }
                    }

                    // Card for Items Needing Manual Review
                    if (reviewItemsState.isNotEmpty()) {
                        YadinCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.warning.copy(alpha = 0.12f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(YadinIcons.statusWarning, contentDescription = null, tint = colors.warning, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = stringResource(R.string.audit_needs_review_count),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.warning
                                    )
                                    Text(
                                        text = reviewItemsState.size.formatDigits(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.warning
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.audit_review_subtitle),
                                    fontSize = 11.sp,
                                    color = colors.onSurface,
                                    lineHeight = 17.sp
                                )
                                Button(
                                    onClick = { showManualReviewDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.warning, contentColor = colors.surface)
                                ) {
                                    Icon(YadinIcons.manualReview, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.audit_btn_review_items, reviewItemsState.size.formatDigits()),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (fixesApplied) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimensions.cornerExtraSmall))
                                .background(colors.success.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(YadinIcons.statusSuccess, contentDescription = null, tint = colors.success)
                            Text(
                                text = stringResource(R.string.audit_applied_success),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else if (auditReport.actions.isEmpty() && reviewItemsState.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimensions.cornerExtraSmall))
                                .background(colors.success.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(YadinIcons.statusSuccess, contentDescription = null, tint = colors.success)
                            Text(
                                text = stringResource(R.string.audit_no_issues),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurface
                            )
                        }
                    } else if (auditReport.actions.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.audit_actions_detail, auditReport.actions.size.formatDigits()),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        val mergedActionLabel = stringResource(R.string.audit_action_merged)
                        val cleanedActionLabel = stringResource(R.string.audit_action_cleaned)
                        val noteActionLabel = stringResource(R.string.audit_action_note)

                        auditReport.actions.take(30).forEach { act ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(dimensions.cornerExtraSmall))
                                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = when (act.actionType) {
                                            AuditActionType.MERGED -> mergedActionLabel
                                            AuditActionType.CLEANED -> cleanedActionLabel
                                            AuditActionType.NOTE_EXTRACTED -> noteActionLabel
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (act.actionType) {
                                            AuditActionType.MERGED -> colors.primary
                                            else -> colors.success
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(colors.surface)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Text(
                                            text = "${act.originalText} ➔ ${act.cleanedText}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.onSurface,
                                            style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = act.description,
                                    fontSize = 11.sp,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Initial Instructions
                    Text(
                        text = stringResource(R.string.audit_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        },
        confirmButton = {
            if (!isAuditing) {
                if (auditReport == null) {
                    Button(
                        onClick = onScanLibrary,
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Text(stringResource(R.string.audit_btn_scan))
                    }
                } else if (!fixesApplied) {
                    Button(
                        onClick = {
                            fixesApplied = true
                            val allToApply = (resolvedItemsState + reviewItemsState).distinctBy { it.conceptId }
                            reviewItemsState.clear()
                            onApplyAuditFixes(allToApply)
                        },
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Text(stringResource(R.string.audit_btn_apply_fixes))
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(dimensions.cornerMedium)
                    ) {
                        Text(stringResource(R.string.action_confirm_close))
                    }
                }
            }
        },
        dismissButton = {
            if (!isAuditing) {
                if (auditReport != null && !fixesApplied) {
                    TextButton(onClick = onScanLibrary) {
                        Text(stringResource(R.string.audit_btn_retry))
                    }
                } else {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    )
}

@Composable
fun AuditManualReviewDialog(
    items: List<AuditReviewItem>,
    onDismiss: () -> Unit,
    onSaveItem: (AuditReviewItem) -> Unit,
    onResolveItem: (AuditReviewItem) -> Unit = {},
    onConfirmAndClose: () -> Unit = onDismiss
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = YadinIcons.manualReview,
                    contentDescription = null,
                    tint = colors.primary
                )
                Column {
                    Text(
                        text = "${stringResource(R.string.audit_review_title)} (${items.size.formatDigits()})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.audit_review_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.conceptId }) { item ->
                    AuditReviewItemCard(
                        item = item,
                        onUpdate = { onSaveItem(it) },
                        onResolve = { onResolveItem(it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmAndClose,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            ) {
                Text(stringResource(R.string.action_confirm_close))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

private data class NoteExtractionOutcome(
    val newWord: String,
    val newTranslation: String,
    val newNote: String
)

private fun extractSecondTextToNote(
    context: android.content.Context,
    item: AuditReviewItem,
    currentWord: String,
    currentTranslation: String,
    currentNote: String
): NoteExtractionOutcome {
    fun appendToNote(existing: String, extra: String): String {
        val cleanExtra = extra.trim()
        if (cleanExtra.isEmpty()) return existing
        if (existing.contains(cleanExtra)) return existing
        return if (existing.isBlank()) cleanExtra else "$existing\n$cleanExtra"
    }

    val w = currentWord.trim()
    val tr = currentTranslation.trim()

    // ۱. بررسی چندخطی بودن فیلد واژه
    val wLines = w.replace("\r\n", "\n").replace("\r", "\n")
        .split("\n").map { it.trim() }.filter { it.isNotBlank() }
    if (wLines.size >= 2) {
        val first = wLines.first()
        val rest = wLines.drop(1).joinToString("\n")
        return NoteExtractionOutcome(
            newWord = first,
            newTranslation = tr,
            newNote = appendToNote(currentNote, rest)
        )
    }

    // ۲. بررسی جفت با جداکننده (: یا - یا —) در فیلد واژه
    val wPair = VocabularyParser.splitPair(w)
    if (wPair != null && wPair.first.isNotBlank() && wPair.second.isNotBlank()) {
        val left = wPair.first.trim()
        val right = wPair.second.trim()
        return NoteExtractionOutcome(
            newWord = left,
            newTranslation = tr,
            newNote = appendToNote(currentNote, right)
        )
    }

    // ۳. بررسی پرانتز، کروشه یا گیومه در فیلد واژه
    val parenRegex = Regex("""^(.*?)\s*[\(\[（«]([^\)\]）»]+)[\)\]）»]\s*$""")
    val wParen = parenRegex.find(w)
    if (wParen != null) {
        val main = wParen.groupValues[1].trim()
        val inside = wParen.groupValues[2].trim()
        if (main.isNotBlank() && inside.isNotBlank()) {
            return NoteExtractionOutcome(
                newWord = main,
                newTranslation = tr,
                newNote = appendToNote(currentNote, inside)
            )
        }
    }

    // ۴. بررسی فرم دوگانه اسلش‌دار در فیلد واژه (مانند Claro / clara)
    if (w.contains("/")) {
        val slashParts = w.split("/").map { it.trim() }.filter { it.isNotBlank() }
        if (slashParts.size >= 2) {
            return NoteExtractionOutcome(
                newWord = slashParts.first(),
                newTranslation = tr,
                newNote = appendToNote(currentNote, slashParts.drop(1).joinToString(" / "))
            )
        }
    }

    // ۵. بررسی اختلاط فارسی و اسپانیایی در فیلد واژه
    if (VocabularyParser.hasLatin(w) && VocabularyParser.hasPersian(w)) {
        val persianMatch = Regex("""[\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF\u200C\s،,؛;()0-9۰-۹-]+""").findAll(w)
            .map { it.value.trim() }
            .firstOrNull { VocabularyParser.hasPersian(it) }
        if (!persianMatch.isNullOrBlank()) {
            val cleanedLatin = w.replace(persianMatch, "").trim()
            if (cleanedLatin.isNotBlank()) {
                return NoteExtractionOutcome(
                    newWord = cleanedLatin,
                    newTranslation = tr,
                    newNote = appendToNote(currentNote, persianMatch)
                )
            }
        }
    }

    // ۶. اگر واژه اصلی (originalWord) بخشی داشته که قبلاً از واژه پیشنهادی حذف شده است
    val origW = item.originalWord.trim()
    if (origW != w && origW.startsWith(w, ignoreCase = true)) {
        val diff = origW.substring(w.length).trim().trim(':', '-', '—', '(', ')', '[', ']', '،', ',')
        if (diff.isNotBlank()) {
            return NoteExtractionOutcome(
                newWord = w,
                newTranslation = tr,
                newNote = appendToNote(currentNote, diff)
            )
        }
    }

    // ۷. بررسی پرانتز یا بخش دوم در فیلد ترجمه (فارسی)
    val trParen = parenRegex.find(tr)
    if (trParen != null) {
        val mainTr = trParen.groupValues[1].trim()
        val insideTr = trParen.groupValues[2].trim()
        if (mainTr.isNotBlank() && insideTr.isNotBlank()) {
            return NoteExtractionOutcome(
                newWord = w,
                newTranslation = mainTr,
                newNote = appendToNote(currentNote, insideTr)
            )
        }
    }

    val trPair = VocabularyParser.splitPair(tr)
    if (trPair != null && trPair.first.isNotBlank() && trPair.second.isNotBlank()) {
        return NoteExtractionOutcome(
            newWord = w,
            newTranslation = trPair.first.trim(),
            newNote = appendToNote(currentNote, trPair.second.trim())
        )
    }

    val trParts = tr.split(Regex("""[،,;/؛\n]+""")).map { it.trim() }.filter { it.isNotBlank() }
    if (trParts.size >= 2) {
        return NoteExtractionOutcome(
            newWord = w,
            newTranslation = trParts.first(),
            newNote = appendToNote(currentNote, trParts.drop(1).joinToString("، "))
        )
    }

    // ۸. در صورتی که کارت از نوع تشابه مشکوک با واژه دیگر است (mergeTargetWord)
    if (!item.mergeTargetWord.isNullOrBlank()) {
        val extraNote = if (tr.isNotBlank() && tr != item.mergeTargetTranslation) {
            context.getString(R.string.audit_alt_meaning_fmt, w, tr)
        } else {
            context.getString(R.string.audit_alt_form_fmt, w)
        }
        return NoteExtractionOutcome(
            newWord = item.mergeTargetWord,
            newTranslation = tr.ifBlank { item.mergeTargetTranslation ?: "" },
            newNote = appendToNote(currentNote, extraNote)
        )
    }

    // ۹. حالت پیش‌فرض: انتقال متن ترجمه یا واژه اصلی به یادداشت
    val fallbackExtra = tr.ifBlank { origW }
    return NoteExtractionOutcome(
        newWord = w,
        newTranslation = tr,
        newNote = appendToNote(currentNote, fallbackExtra)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuditReviewItemCard(
    item: AuditReviewItem,
    onUpdate: (AuditReviewItem) -> Unit,
    onResolve: (AuditReviewItem) -> Unit = {}
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var word by remember(item.conceptId) { mutableStateOf(item.suggestedWord) }
    var translation by remember(item.conceptId) { mutableStateOf(item.suggestedTranslation) }
    var note by remember(item.conceptId) { mutableStateOf(item.suggestedNote ?: "") }

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(dimensions.cornerMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = YadinIcons.statusWarning,
                        contentDescription = null,
                        tint = if (item.confidence < 0.8) colors.error else colors.warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.reason,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.confidence < 0.8) colors.error else colors.warning,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = stringResource(R.string.percent_fmt, (item.confidence * 100).toInt().formatDigits()),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }

            // Original Word
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.audit_original_word),
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariant
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = item.originalWord,
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant,
                        style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
                    )
                }
            }

            OutlinedTextField(
                value = word,
                onValueChange = {
                    word = it
                    onUpdate(
                        item.copy(
                            suggestedWord = it,
                            suggestedTranslation = translation,
                            suggestedNote = note.ifBlank { null },
                            userDecision = AuditUserDecision.EDITED
                        )
                    )
                },
                label = { Text(stringResource(R.string.audit_field_word), fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                singleLine = true
            )

            OutlinedTextField(
                value = translation,
                onValueChange = {
                    translation = it
                    onUpdate(
                        item.copy(
                            suggestedWord = word,
                            suggestedTranslation = it,
                            suggestedNote = note.ifBlank { null },
                            userDecision = AuditUserDecision.EDITED
                        )
                    )
                },
                label = { Text(stringResource(R.string.audit_field_translation), fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                singleLine = true
            )

            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    onUpdate(
                        item.copy(
                            suggestedWord = word,
                            suggestedTranslation = translation,
                            suggestedNote = it.ifBlank { null },
                            userDecision = AuditUserDecision.EDITED
                        )
                    )
                },
                label = { Text(stringResource(R.string.audit_field_note), fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                maxLines = 3
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!item.mergeTargetWord.isNullOrBlank()) {
                    FilledTonalButton(
                        onClick = {
                            val mergedWord = item.mergeTargetWord
                            val updated = item.copy(
                                suggestedWord = mergedWord,
                                suggestedTranslation = translation,
                                suggestedNote = note.ifBlank { null },
                                userDecision = AuditUserDecision.MERGE_WITH_TARGET
                            )
                            onResolve(updated)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.primary.copy(alpha = 0.14f),
                            contentColor = colors.primary
                        )
                    ) {
                        Icon(YadinIcons.smartAudit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.audit_btn_merge_similar, item.mergeTargetWord),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val localContext = androidx.compose.ui.platform.LocalContext.current
                FilledTonalButton(
                    onClick = {
                        val outcome = extractSecondTextToNote(localContext, item, word, translation, note)
                        word = outcome.newWord
                        translation = outcome.newTranslation
                        note = outcome.newNote
                        val updated = item.copy(
                            suggestedWord = word,
                            suggestedTranslation = translation,
                            suggestedNote = note.ifBlank { null },
                            userDecision = AuditUserDecision.MOVE_TO_NOTE
                        )
                        onUpdate(updated)
                    }
                ) {
                    Icon(YadinIcons.noteText, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.audit_btn_move_to_note), fontSize = 11.sp)
                }

                FilledTonalButton(
                    onClick = {
                        val updated = item.copy(
                            suggestedWord = word,
                            suggestedTranslation = translation,
                            suggestedNote = note.ifBlank { null },
                            userDecision = if (note.isNotBlank()) AuditUserDecision.MOVE_TO_NOTE else AuditUserDecision.EDITED
                        )
                        onResolve(updated)
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = colors.success.copy(alpha = 0.15f),
                        contentColor = colors.success
                    )
                ) {
                    Icon(YadinIcons.statusSuccess, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.audit_btn_confirm_item), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                TextButton(
                    onClick = {
                        val unchanged = item.copy(
                            suggestedWord = item.originalWord,
                            suggestedTranslation = item.originalTranslation,
                            suggestedNote = null,
                            userDecision = AuditUserDecision.KEEP_ORIGINAL_DISMISS
                        )
                        onResolve(unchanged)
                    }
                ) {
                    Text(stringResource(R.string.audit_btn_keep_original), fontSize = 11.sp)
                }
            }
        }
    }
}

private data class LibraryFilterOption(
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val onSelect: () -> Unit
)

@Composable
private fun LibraryFilterDropdown(
    label: String,
    selectedOption: String,
    icon: ImageVector,
    isFilterActive: Boolean,
    activeAccentColor: Color,
    onClearFilter: (() -> Unit)? = null,
    options: List<LibraryFilterOption>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val shape = RoundedCornerShape(dimensions.cornerBadge)
    val bgColor = if (isFilterActive) activeAccentColor.copy(alpha = 0.10f) else colors.surface
    val borderColor = if (isFilterActive) activeAccentColor.copy(alpha = 0.55f) else colors.outline.copy(alpha = 0.25f)
    val accentColor = if (isFilterActive) activeAccentColor else colors.onSurfaceVariant

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(bgColor)
                .border(if (isFilterActive) 1.2.dp else 1.dp, borderColor, shape)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selectedOption,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFilterActive) activeAccentColor else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 11.5.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (isFilterActive && onClearFilter != null) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onClearFilter() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = YadinIcons.clear,
                            contentDescription = stringResource(R.string.action_clear),
                            tint = activeAccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Icon(
                    imageVector = YadinIcons.dropdown,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            options.forEach { option ->
                val isSelected = option.title == selectedOption
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) option.tint else colors.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = option.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) option.tint else colors.onSurface
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = YadinIcons.check,
                                    contentDescription = null,
                                    tint = option.tint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        option.onSelect()
                        expanded = false
                    }
                )
            }
        }
    }
}
