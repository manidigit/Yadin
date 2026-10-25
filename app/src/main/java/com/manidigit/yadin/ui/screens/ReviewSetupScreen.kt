package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import com.manidigit.yadin.ui.theme.YadinIcons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.data.repository.ReviewFilters
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SpacedRepetitionAlgorithm
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.model.toStringRes
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.ExposedEntryTypeDropdown
import com.manidigit.yadin.ui.components.VocabStatType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSectionHeader
import com.manidigit.yadin.ui.components.YadinSelectableChip
import com.manidigit.yadin.ui.components.getDifficultyColor
import com.manidigit.yadin.ui.components.getDifficultyIcon
import com.manidigit.yadin.ui.components.getQuizLevelColor
import com.manidigit.yadin.ui.components.getQuizLevelIcon
import com.manidigit.yadin.ui.components.getReviewModeColor
import com.manidigit.yadin.ui.components.getReviewModeIcon
import com.manidigit.yadin.ui.components.getReviewTypeColor
import com.manidigit.yadin.ui.components.getReviewTypeIcon
import com.manidigit.yadin.ui.components.getStageColor
import com.manidigit.yadin.ui.components.getStageIcon
import com.manidigit.yadin.ui.components.getVocabStatIcon
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits

private data class DiffItem(
    val diff: VocabularyDifficulty,
    val label: String,
    val color: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewSetupScreen(
    initialType: ReviewType = ReviewType.RANDOM,
    initialDifficulties: Set<VocabularyDifficulty> = emptySet(),
    initialMode: ReviewMode = ReviewMode.QUIZ,
    initialQuizLevel: QuizLevel = QuizLevel.NORMAL,
    initialMaxCards: Int = 20,
    defaultAlgorithm: SpacedRepetitionAlgorithm = SpacedRepetitionAlgorithm.YADIN,
    categories: List<Category>,
    difficultyCounts: Map<VocabularyDifficulty, Int>,
    candidateCount: Int,
    statistics: com.manidigit.yadin.domain.model.StatisticsSummary? = null,
    appLanguageDirection: CardDirection = CardDirection.NORMAL,
    onFilterChanged: (ReviewFilters) -> Unit,
    onStartReview: (ReviewFilters) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val effectiveInitialType = when {
        initialType == ReviewType.RANDOM && initialDifficulties.isEmpty() && defaultAlgorithm == SpacedRepetitionAlgorithm.SM2 -> ReviewType.SM2
        initialType == ReviewType.RANDOM && initialDifficulties.isEmpty() && defaultAlgorithm == SpacedRepetitionAlgorithm.FSRS -> ReviewType.FSRS
        else -> initialType
    }

    var selectedType by remember(effectiveInitialType) { mutableStateOf(effectiveInitialType) }
    var selectedMode by remember(initialMode) { mutableStateOf(initialMode) }
    var selectedQuizLevel by remember(initialQuizLevel) { mutableStateOf(initialQuizLevel) }
    var selectedDifficulties by remember(initialDifficulties) {
        mutableStateOf(if (initialDifficulties.isNotEmpty()) initialDifficulties else VocabularyDifficulty.entries.toSet())
    }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedEntryType by remember { mutableStateOf<EntryType?>(null) }
    var selectedMaxCards by remember(initialMaxCards) { mutableStateOf(initialMaxCards) }

    fun buildFilters() = ReviewFilters(
        reviewType = selectedType,
        mode = selectedMode,
        direction = appLanguageDirection,
        quizLevel = if (selectedMode == ReviewMode.QUIZ) selectedQuizLevel else null,
        difficulties = selectedDifficulties,
        categoryIds = if (selectedCategoryId != null) setOf(selectedCategoryId!!) else emptySet(),
        entryTypes = if (selectedEntryType != null) setOf(selectedEntryType!!) else emptySet(),
        maxCards = selectedMaxCards,
        algorithm = when (selectedType) {
            ReviewType.SM2 -> SpacedRepetitionAlgorithm.SM2
            ReviewType.FSRS -> SpacedRepetitionAlgorithm.FSRS
            else -> defaultAlgorithm
        }
    )

    LaunchedEffect(
        selectedType,
        appLanguageDirection,
        selectedDifficulties,
        selectedCategoryId,
        selectedEntryType,
        defaultAlgorithm,
        statistics?.reviewedTodayCount
    ) {
        onFilterChanged(buildFilters())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = dimensions.screenPadding, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Standardized Screen Header
            item {
                YadinScreenHeader(
                    title = stringResource(R.string.review_setup_title),
                    onBack = onBack
                )
            }

            // Card 1: Practice Mode (نوع محتوا / حالت مطالعه)
            item {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        YadinSectionHeader(
                            title = stringResource(R.string.review_setup_mode),
                            icon = getReviewModeIcon(selectedMode),
                            iconTint = getReviewModeColor(selectedMode, colors)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            YadinSelectableChip(
                                text = stringResource(R.string.review_mode_quiz),
                                icon = getReviewModeIcon(ReviewMode.QUIZ),
                                isSelected = selectedMode == ReviewMode.QUIZ,
                                accentColor = getReviewModeColor(ReviewMode.QUIZ, colors),
                                preserveAccentWhenUnselected = true,
                                highContrastSelection = true,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedMode = ReviewMode.QUIZ }
                            )
                            YadinSelectableChip(
                                text = stringResource(R.string.review_mode_flashcard),
                                icon = getReviewModeIcon(ReviewMode.FLASHCARD),
                                isSelected = selectedMode == ReviewMode.FLASHCARD,
                                accentColor = getReviewModeColor(ReviewMode.FLASHCARD, colors),
                                preserveAccentWhenUnselected = true,
                                highContrastSelection = true,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedMode = ReviewMode.FLASHCARD }
                            )
                        }
                    }
                }
            }

            // Card 2: Source & Category (دوره‌های زمانی، وضعیت یادگیری و دسته‌بندی)
            item {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        YadinSectionHeader(
                            title = stringResource(R.string.review_setup_source_title),
                            icon = getVocabStatIcon(VocabStatType.TOTAL),
                            iconTint = getReviewTypeColor(selectedType, colors)
                        )

                        val stats = statistics
                        val dueDaily = stats?.dueDailyCount ?: 0
                        val totalDaily = stats?.dailyStageCount ?: 0

                        val dueWeekly = stats?.dueWeeklyCount ?: 0
                        val totalWeekly = stats?.weeklyStageCount ?: 0

                        val dueMonthly = stats?.dueMonthlyCount ?: 0
                        val totalMonthly = stats?.monthlyStageCount ?: 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LeitnerSelectableStageBox(
                                title = stringResource(R.string.stage_daily),
                                dueCount = dueDaily,
                                totalCount = totalDaily,
                                accentColor = getStageColor(Stage.DAILY, colors),
                                icon = getStageIcon(Stage.DAILY),
                                isSelected = (selectedType == ReviewType.DAILY),
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = ReviewType.DAILY }
                            )

                            LeitnerSelectableStageBox(
                                title = stringResource(R.string.stage_weekly),
                                dueCount = dueWeekly,
                                totalCount = totalWeekly,
                                accentColor = getStageColor(Stage.WEEKLY, colors),
                                icon = getStageIcon(Stage.WEEKLY),
                                isSelected = (selectedType == ReviewType.WEEKLY),
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = ReviewType.WEEKLY }
                            )

                            LeitnerSelectableStageBox(
                                title = stringResource(R.string.stage_monthly),
                                dueCount = dueMonthly,
                                totalCount = totalMonthly,
                                accentColor = getStageColor(Stage.MONTHLY, colors),
                                icon = getStageIcon(Stage.MONTHLY),
                                isSelected = (selectedType == ReviewType.MONTHLY),
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = ReviewType.MONTHLY }
                            )
                        }

                        val dueRandom = stats?.dueTodayCount ?: 0
                        val totalRandom = stats?.totalWords ?: 0

                        val dueLearned = stats?.availableLearnedCount ?: 0
                        val totalLearned = stats?.learnedStageCount ?: 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompactHorizontalSourceBox(
                                title = stringResource(R.string.review_setup_mode_random),
                                dueCount = dueRandom,
                                totalCount = totalRandom,
                                accentColor = getReviewTypeColor(ReviewType.RANDOM, colors),
                                icon = getReviewTypeIcon(ReviewType.RANDOM),
                                isSelected = (selectedType == ReviewType.RANDOM),
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = ReviewType.RANDOM }
                            )

                            CompactHorizontalSourceBox(
                                title = stringResource(R.string.stage_learned),
                                dueCount = dueLearned,
                                totalCount = totalLearned,
                                accentColor = getReviewTypeColor(ReviewType.LEARNED, colors),
                                icon = getReviewTypeIcon(ReviewType.LEARNED),
                                isSelected = (selectedType == ReviewType.LEARNED),
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = ReviewType.LEARNED }
                            )
                        }

                        // Dedicated SM-2 & FSRS Smart Review Cards side-by-side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompactHorizontalSourceBox(
                                title = stringResource(R.string.review_setup_sm2_title),
                                dueCount = dueRandom,
                                totalCount = totalRandom,
                                accentColor = getReviewTypeColor(ReviewType.SM2, colors),
                                icon = getReviewTypeIcon(ReviewType.SM2),
                                isSelected = (selectedType == ReviewType.SM2),
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = ReviewType.SM2 }
                            )

                            CompactHorizontalSourceBox(
                                title = stringResource(R.string.review_setup_fsrs_title),
                                dueCount = dueRandom,
                                totalCount = totalRandom,
                                accentColor = getReviewTypeColor(ReviewType.FSRS, colors),
                                icon = getReviewTypeIcon(ReviewType.FSRS),
                                isSelected = (selectedType == ReviewType.FSRS),
                                modifier = Modifier.weight(1f),
                                onClick = { selectedType = ReviewType.FSRS }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExposedCategoryDropdown(
                                categories = categories,
                                selectedCategoryId = selectedCategoryId,
                                onSelectCategory = { selectedCategoryId = it },
                                label = stringResource(R.string.review_setup_category_label),
                                modifier = Modifier.weight(1f)
                            )
                            ExposedEntryTypeDropdown(
                                selectedEntryType = selectedEntryType,
                                onSelectEntryType = { selectedEntryType = it },
                                label = stringResource(R.string.review_setup_entry_type_label),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Card 3: Difficulty, Quiz Option Proficiency & Session Limit
            item {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        YadinSectionHeader(
                            title = stringResource(R.string.review_setup_diff_filter),
                            icon = getDifficultyIcon(VocabularyDifficulty.MEDIUM),
                            iconTint = colors.warning
                        )

                        val diffItems = listOf(
                            DiffItem(
                                VocabularyDifficulty.EASY,
                                stringResource(R.string.difficulty_easy),
                                getDifficultyColor(VocabularyDifficulty.EASY, colors),
                                getDifficultyIcon(VocabularyDifficulty.EASY)
                            ),
                            DiffItem(
                                VocabularyDifficulty.MEDIUM,
                                stringResource(R.string.difficulty_medium),
                                getDifficultyColor(VocabularyDifficulty.MEDIUM, colors),
                                getDifficultyIcon(VocabularyDifficulty.MEDIUM)
                            ),
                            DiffItem(
                                VocabularyDifficulty.HARD,
                                stringResource(R.string.difficulty_hard),
                                getDifficultyColor(VocabularyDifficulty.HARD, colors),
                                getDifficultyIcon(VocabularyDifficulty.HARD)
                            ),
                            DiffItem(
                                VocabularyDifficulty.VERY_HARD,
                                stringResource(R.string.difficulty_very_hard),
                                getDifficultyColor(VocabularyDifficulty.VERY_HARD, colors),
                                getDifficultyIcon(VocabularyDifficulty.VERY_HARD)
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            diffItems.forEach { item ->
                                val count = difficultyCounts[item.diff] ?: 0
                                val isSelected = item.diff in selectedDifficulties
                                CompactDiffBadge(
                                    label = item.label,
                                    count = count,
                                    icon = item.icon,
                                    isSelected = isSelected,
                                    color = item.color,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        selectedDifficulties = if (isSelected) {
                                            val remaining = selectedDifficulties - item.diff
                                            if (remaining.isEmpty()) VocabularyDifficulty.entries.toSet() else remaining
                                        } else {
                                            selectedDifficulties + item.diff
                                        }
                                    }
                                )
                            }
                        }

                        // ۵. سطح مهارت کاربر در آزمون (Proficiency / QuizLevel)
                        if (selectedMode == ReviewMode.QUIZ) {
                            YadinSectionHeader(
                                title = stringResource(R.string.review_setup_quiz_level),
                                icon = getQuizLevelIcon(selectedQuizLevel),
                                iconTint = getQuizLevelColor(selectedQuizLevel, colors)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuizLevel.entries.forEach { lvl ->
                                    val title = stringResource(lvl.toStringRes())
                                    val isSelected = (selectedQuizLevel == lvl)
                                    val lvlIcon = getQuizLevelIcon(lvl)
                                    val lvlColor = getQuizLevelColor(lvl, colors)
                                    YadinSelectableChip(
                                        text = title,
                                        icon = lvlIcon,
                                        isSelected = isSelected,
                                        accentColor = lvlColor,
                                        preserveAccentWhenUnselected = true,
                                        highContrastSelection = true,
                                        verticalLayout = true,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedQuizLevel = lvl }
                                    )
                                }
                            }
                        }

                        // Session Card Limit selector
                        YadinSectionHeader(
                            title = stringResource(R.string.review_setup_count),
                            icon = YadinIcons.sessionCardCount,
                            iconTint = colors.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10, 20, 30, 50, 100).forEach { limit ->
                                val isSelected = (selectedMaxCards == limit)
                                YadinSelectableChip(
                                    text = limit.formatDigits(),
                                    icon = null,
                                    isSelected = isSelected,
                                    accentColor = colors.primary,
                                    preserveAccentWhenUnselected = true,
                                    highContrastSelection = true,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedMaxCards = limit }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom CTA Bar
        val isReady = candidateCount > 0
        val actualSessionCards = minOf(candidateCount, selectedMaxCards)
        val buttonText = if (isReady) {
            if (candidateCount > selectedMaxCards) {
                stringResource(R.string.review_setup_start_partial, actualSessionCards.formatDigits(), candidateCount.formatDigits())
            } else {
                stringResource(R.string.review_setup_start, actualSessionCards.formatDigits())
            }
        } else {
            stringResource(R.string.review_setup_no_cards)
        }

        Button(
            onClick = {
                if (isReady) onStartReview(buildFilters())
            },
            enabled = isReady,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(48.dp),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.surfaceVariant,
                disabledContentColor = colors.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = if (isReady) YadinIcons.play else YadinIcons.filter,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = buttonText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LeitnerSelectableStageBox(
    title: String,
    dueCount: Int,
    totalCount: Int,
    accentColor: Color,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val shape = RoundedCornerShape(10.dp)

    val containerBg = if (isSelected) {
        accentColor.copy(alpha = 0.28f)
    } else {
        colors.surfaceVariant.copy(alpha = 0.22f)
    }

    val borderColor = if (isSelected) {
        accentColor
    } else {
        accentColor.copy(alpha = 0.16f)
    }

    val iconTint = if (isSelected) {
        accentColor
    } else {
        accentColor.copy(alpha = 0.42f)
    }

    val titleColor = if (isSelected) {
        accentColor
    } else {
        colors.onSurfaceVariant.copy(alpha = 0.50f)
    }

    val dueColor = when {
        isSelected && dueCount > 0 -> accentColor
        isSelected -> colors.onSurfaceVariant.copy(alpha = 0.80f)
        dueCount > 0 -> accentColor.copy(alpha = 0.45f)
        else -> colors.onSurfaceVariant.copy(alpha = 0.38f)
    }

    val totalColor = if (isSelected) {
        colors.onSurfaceVariant.copy(alpha = 0.85f)
    } else {
        colors.onSurfaceVariant.copy(alpha = 0.42f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerBg)
            .border(
                width = if (isSelected) 2.dp else 0.8.dp,
                color = borderColor,
                shape = shape
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = titleColor,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
            Text(
                text = if (dueCount > 0) stringResource(R.string.home_due_ready, dueCount.formatDigits()) else stringResource(R.string.home_reviewed_done),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = dueColor,
                fontSize = 10.5.sp,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.home_of_total_words, totalCount.formatDigits()),
                style = MaterialTheme.typography.labelSmall,
                color = totalColor,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactHorizontalSourceBox(
    title: String,
    dueCount: Int,
    totalCount: Int,
    accentColor: Color,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val shape = RoundedCornerShape(10.dp)
    val containerBg = if (isSelected) accentColor.copy(alpha = 0.28f) else colors.surfaceVariant.copy(alpha = 0.22f)
    val borderColor = if (isSelected) accentColor else accentColor.copy(alpha = 0.16f)
    val iconTint = if (isSelected) accentColor else accentColor.copy(alpha = 0.42f)
    val titleColor = if (isSelected) accentColor else colors.onSurfaceVariant.copy(alpha = 0.50f)
    val totalColor = if (isSelected) colors.onSurfaceVariant.copy(alpha = 0.85f) else colors.onSurfaceVariant.copy(alpha = 0.42f)
    val dueColor = if (isSelected) accentColor else accentColor.copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerBg)
            .border(
                width = if (isSelected) 2.dp else 0.8.dp,
                color = borderColor,
                shape = shape
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = titleColor,
                        fontSize = 10.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(R.string.home_of_total_words, totalCount.formatDigits()),
                        color = totalColor,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = dueCount.formatDigits(),
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = dueColor,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CompactDiffBadge(
    label: String,
    count: Int,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    YadinSelectableChip(
        text = label,
        icon = icon,
        badgeText = count.formatDigits(),
        isSelected = isSelected,
        accentColor = color,
        preserveAccentWhenUnselected = true,
        highContrastSelection = true,
        verticalLayout = true,
        fontSize = 10.5.sp,
        modifier = modifier,
        onClick = onClick
    )
}
