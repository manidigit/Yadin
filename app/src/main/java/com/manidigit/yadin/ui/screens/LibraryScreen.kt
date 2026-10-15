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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.mutableStateListOf
import com.manidigit.yadin.domain.algorithm.VocabularyParser
import com.manidigit.yadin.domain.model.AuditReviewItem
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
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.ui.components.DifficultyBadge
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.SpeakButton
import com.manidigit.yadin.ui.components.StageBadge
import com.manidigit.yadin.ui.components.YadinCard
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

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (showOnlyInactive) stringResource(R.string.library_trash_title) else stringResource(R.string.library_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = stringResource(R.string.library_loaded_words_count, words.size.formatDigits()),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                if (!showOnlyInactive) {
                    FilledTonalButton(
                        onClick = { showAuditDialog = true },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.library_search_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = colors.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear), tint = colors.onSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Combobox Compact Filter Toolbar (وضعیت، مرحله مرور، سطح سختی)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Combobox 1: Status
                LibraryFilterDropdown(
                    label = stringResource(R.string.filter_status),
                    selectedOption = if (showOnlyInactive) statusTrashLabel else statusActiveLabel,
                    options = listOf(
                        statusActiveLabel to { onInactiveFilterToggle(false) },
                        statusTrashLabel to { onInactiveFilterToggle(true) }
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Combobox 2: Stage
                val currentStageLabel = when (selectedStage) {
                    Stage.DAILY -> stageDailyLabel
                    Stage.WEEKLY -> stageWeeklyLabel
                    Stage.MONTHLY -> stageMonthlyLabel
                    Stage.LEARNED -> stageLearnedLabel
                    null -> stageAllLabel
                }
                LibraryFilterDropdown(
                    label = stringResource(R.string.filter_stage),
                    selectedOption = currentStageLabel,
                    options = listOf(
                        stageAllLabel to { onStageFilterChange(null) },
                        stageDailyLabel to { onStageFilterChange(Stage.DAILY) },
                        stageWeeklyLabel to { onStageFilterChange(Stage.WEEKLY) },
                        stageMonthlyLabel to { onStageFilterChange(Stage.MONTHLY) },
                        stageLearnedLabel to { onStageFilterChange(Stage.LEARNED) }
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Combobox 3: Difficulty
                val currentDiffLabel = when (selectedDifficulty) {
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY -> diffEasyLabel
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM -> diffMedLabel
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD -> diffHardLabel
                    com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD -> diffVeryHardLabel
                    null -> diffAllLabel
                }
                LibraryFilterDropdown(
                    label = stringResource(R.string.filter_difficulty),
                    selectedOption = currentDiffLabel,
                    options = listOf(
                        diffAllLabel to { onDifficultyFilterChange(null) },
                        diffEasyLabel to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY) },
                        diffMedLabel to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.MEDIUM) },
                        diffHardLabel to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.HARD) },
                        diffVeryHardLabel to { onDifficultyFilterChange(com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD) }
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown Menu for Categories
            if (categories.isNotEmpty()) {
                ExposedCategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategory,
                    onSelectCategory = { onCategoryFilterChange(it) },
                    label = stringResource(R.string.category_label),
                    onAddNewCategory = onAddNewCategory,
                    onEditCategory = onEditCategory,
                    onDeleteCategory = onDeleteCategory
                )
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
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.library_add_word))
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
                    imageVector = Icons.Default.Add,
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
                                imageVector = Icons.Default.Edit,
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
                                imageVector = Icons.Default.FileUpload,
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

    if (showManualReviewDialog) {
        AuditManualReviewDialog(
            items = reviewItemsState,
            onDismiss = { showManualReviewDialog = false },
            onSaveItem = { updatedItem ->
                val idx = reviewItemsState.indexOfFirst { it.conceptId == updatedItem.conceptId }
                if (idx != -1) {
                    reviewItemsState[idx] = updatedItem
                }
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
                    imageVector = Icons.Default.AutoAwesome,
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
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = colors.warning, modifier = Modifier.size(18.dp))
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
                                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.success.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.success)
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.success.copy(alpha = 0.12f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.success)
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
                                    .clip(RoundedCornerShape(8.dp))
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
                            onApplyAuditFixes(reviewItemsState)
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
    onSaveItem: (AuditReviewItem) -> Unit
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
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    tint = colors.primary
                )
                Column {
                    Text(
                        text = stringResource(R.string.audit_review_title),
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
                        onUpdate = { onSaveItem(it) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(dimensions.cornerMedium)
            ) {
                Text(stringResource(R.string.action_confirm_close))
            }
        }
    )
}

@Composable
private fun AuditReviewItemCard(
    item: AuditReviewItem,
    onUpdate: (AuditReviewItem) -> Unit
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
                        imageVector = Icons.Default.Warning,
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
                    text = "${(item.confidence * 100).toInt().formatDigits()}٪",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }

            // Original Word
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = "${stringResource(R.string.audit_original_word)} ${item.originalWord}",
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariant,
                    style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
                )
            }

            OutlinedTextField(
                value = word,
                onValueChange = {
                    word = it
                    onUpdate(item.copy(suggestedWord = it, suggestedTranslation = translation, suggestedNote = note.ifBlank { null }))
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
                    onUpdate(item.copy(suggestedWord = word, suggestedTranslation = it, suggestedNote = note.ifBlank { null }))
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
                    onUpdate(item.copy(suggestedWord = word, suggestedTranslation = translation, suggestedNote = it.ifBlank { null }))
                },
                label = { Text(stringResource(R.string.audit_field_note), fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        val pair = VocabularyParser.splitPair(word)
                        if (pair != null) {
                            word = pair.first.trim()
                            val extra = pair.second.trim()
                            if (VocabularyParser.hasPersian(extra)) {
                                translation = if (translation.isBlank()) extra else "$translation، $extra"
                            } else {
                                note = if (note.isBlank()) extra else "$note\n$extra"
                            }
                            onUpdate(item.copy(suggestedWord = word, suggestedTranslation = translation, suggestedNote = note.ifBlank { null }))
                        } else {
                            val paren = Regex("""^(.*?)\s*[\(\[（]([^\)\]）]+)[\)\]）]\s*$""").find(word)
                            if (paren != null) {
                                val main = paren.groupValues[1].trim()
                                val inside = paren.groupValues[2].trim()
                                word = main
                                note = if (note.isBlank()) inside else "$note\n$inside"
                                onUpdate(item.copy(suggestedWord = word, suggestedTranslation = translation, suggestedNote = note.ifBlank { null }))
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.audit_btn_move_to_note), fontSize = 11.sp)
                }

                TextButton(
                    onClick = {
                        word = item.originalWord
                        translation = item.originalTranslation
                        note = ""
                        onUpdate(item.copy(suggestedWord = word, suggestedTranslation = translation, suggestedNote = null))
                    }
                ) {
                    Text(stringResource(R.string.audit_btn_keep_original), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun LibraryFilterDropdown(
    label: String,
    selectedOption: String,
    options: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = LocalYadinColors.current

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surface)
                .border(1.dp, colors.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 9.sp
                )
                Text(
                    text = selectedOption,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surface)
        ) {
            options.forEach { (title, onSelect) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (title == selectedOption) FontWeight.Bold else FontWeight.Normal,
                            color = if (title == selectedOption) colors.primary else colors.onSurface
                        )
                    },
                    onClick = {
                        onSelect()
                        expanded = false
                    }
                )
            }
        }
    }
}
