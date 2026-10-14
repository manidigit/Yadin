package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.repository.ReviewFilters
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.components.ExposedCategoryDropdown
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.toPersianDigits

private data class ReviewTypeItem(val type: ReviewType, val title: String, val icon: ImageVector)
private data class DiffItem(val diff: VocabularyDifficulty, val label: String, val color: Color)

/**
 * صفحه تنظیم و سفارشی‌سازی جلسه مرور و تمرینات.
 *
 * امکان تعیین حالت تمرین (پیش‌فرض: آزمون ۴ گزینه‌ای)، منبع واژگان (روزانه، هفتگی، آزاد)،
 * دسته‌بندی موضوعی، فیلتر سطوح دشواری و سقف تعداد کلمات جلسه.
 *
 * @param initialType نوع مرور اولیه
 * @param initialDifficulties فیلتر اولیه سطوح دشواری
 * @param initialMode حالت تمرین اولیه (پیش‌فرض [ReviewMode.QUIZ])
 * @param initialQuizLevel سطح اولیه سختی گزینه‌های کوییز
 * @param initialMaxCards سقف اولیه تعداد کارت‌های جلسه
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewSetupScreen(
    initialType: ReviewType = ReviewType.RANDOM,
    initialDifficulties: Set<VocabularyDifficulty> = emptySet(),
    initialMode: ReviewMode = ReviewMode.QUIZ,
    initialQuizLevel: QuizLevel = QuizLevel.MEDIUM,
    initialMaxCards: Int = 20,
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

    // حل نقص ISS-106: بارگذاری مقادیر پیش‌فرض کاربر از تنظیمات ذخیره‌شده
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var selectedMode by remember(initialMode) { mutableStateOf(initialMode) }
    var selectedQuizLevel by remember(initialQuizLevel) { mutableStateOf(initialQuizLevel) }
    var selectedDifficulties by remember(initialDifficulties) {
        mutableStateOf(if (initialDifficulties.isNotEmpty()) initialDifficulties else VocabularyDifficulty.entries.toSet())
    }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedMaxCards by remember(initialMaxCards) { mutableStateOf(initialMaxCards) }

    fun buildFilters() = ReviewFilters(
        reviewType = selectedType,
        mode = selectedMode,
        direction = appLanguageDirection,
        quizLevel = if (selectedMode == ReviewMode.QUIZ) selectedQuizLevel else null,
        difficulties = selectedDifficulties,
        categoryIds = if (selectedCategoryId != null) setOf(selectedCategoryId!!) else emptySet(),
        maxCards = selectedMaxCards
    )

    LaunchedEffect(
        selectedType,
        selectedMode,
        appLanguageDirection,
        selectedQuizLevel,
        selectedDifficulties,
        selectedCategoryId,
        selectedMaxCards
    ) {
        onFilterChanged(buildFilters())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = dimensions.screenPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            contentDescription = "بازگشت",
                            tint = colors.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = "تنظیمات مرور و تمرین",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                }
            }

            // Card 1: حالت تمرین (Mode)
            item {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "حالت تمرین",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        // Mode selector: Segmented Control
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ModeSelectButton(
                                title = "آزمون ۴ گزینه‌ای",
                                icon = Icons.Default.Quiz,
                                isSelected = selectedMode == ReviewMode.QUIZ,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedMode = ReviewMode.QUIZ }
                            )
                            ModeSelectButton(
                                title = "فلش‌کارت تعاملی",
                                icon = Icons.Default.Style,
                                isSelected = selectedMode == ReviewMode.FLASHCARD,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedMode = ReviewMode.FLASHCARD }
                            )
                        }
                    }
                }
            }

            // Card 2: منبع واژگان و دسته‌بندی
            item {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "منبع واژگان و تکرار فاصله‌دار",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        // Category A: چرخه تکرار فاصله‌دار (لایتنر)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "چرخه تکرار فاصله‌دار (لایتنر):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LeitnerSelectableStageBox(
                                    title = "روزانه",
                                    dueCount = dueDaily,
                                    totalCount = totalDaily,
                                    accentColor = colors.primary,
                                    icon = Icons.Default.Today,
                                    isSelected = (selectedType == ReviewType.DAILY),
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedType = ReviewType.DAILY }
                                )

                                LeitnerSelectableStageBox(
                                    title = "هفتگی",
                                    dueCount = dueWeekly,
                                    totalCount = totalWeekly,
                                    accentColor = colors.info,
                                    icon = Icons.Default.AutoAwesome,
                                    isSelected = (selectedType == ReviewType.WEEKLY),
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedType = ReviewType.WEEKLY }
                                )

                                LeitnerSelectableStageBox(
                                    title = "ماهانه",
                                    dueCount = dueMonthly,
                                    totalCount = totalMonthly,
                                    accentColor = colors.warning,
                                    icon = Icons.Default.Star,
                                    isSelected = (selectedType == ReviewType.MONTHLY),
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedType = ReviewType.MONTHLY }
                                )
                            }
                        }

                        // Category B: حالات تکمیلی و مرور آزاد (جداگانه و هم‌سبک با کارت‌ها)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "حالات تکمیلی و مرور آزاد:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant
                            )

                            val stats = statistics
                            val dueRandom = stats?.dueTodayCount ?: 0
                            val totalRandom = stats?.totalWords ?: 0

                            val dueLearned = stats?.availableLearnedCount ?: 0
                            val totalLearned = stats?.learnedStageCount ?: 0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LeitnerSelectableStageBox(
                                    title = "تصادفی (آزاد)",
                                    dueCount = dueRandom,
                                    totalCount = totalRandom,
                                    accentColor = colors.primary,
                                    icon = Icons.Default.Shuffle,
                                    isSelected = (selectedType == ReviewType.RANDOM),
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedType = ReviewType.RANDOM }
                                )

                                LeitnerSelectableStageBox(
                                    title = "تثبیت‌شده (یادگرفته)",
                                    dueCount = dueLearned,
                                    totalCount = totalLearned,
                                    accentColor = colors.success,
                                    icon = Icons.Default.CheckCircle,
                                    isSelected = (selectedType == ReviewType.LEARNED),
                                    modifier = Modifier.weight(1f),
                                    onClick = { selectedType = ReviewType.LEARNED }
                                )
                            }
                        }

                        // Category Dropdown
                        if (categories.isNotEmpty()) {
                            ExposedCategoryDropdown(
                                categories = categories,
                                selectedCategoryId = selectedCategoryId,
                                onSelectCategory = { selectedCategoryId = it },
                                label = "دسته‌بندی موضوعی کلمات"
                            )
                        }
                    }
                }
            }

            // Card 3: فیلتر دشواری و سقف تعداد
            item {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "فیلتر سطح دشواری واژگان",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        val diffItems = listOf(
                            DiffItem(VocabularyDifficulty.EASY, "آسان", colors.success),
                            DiffItem(VocabularyDifficulty.MEDIUM, "متوسط", colors.info),
                            DiffItem(VocabularyDifficulty.HARD, "سخت", colors.warning),
                            DiffItem(VocabularyDifficulty.VERY_HARD, "خیلی سخت", colors.error)
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

                        // Quiz level (if Quiz mode)
                        if (selectedMode == ReviewMode.QUIZ) {
                            Text(
                                text = "سطح سختی گزینه‌های آزمون",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    QuizLevel.EASY to "آسان",
                                    QuizLevel.MEDIUM to "متوسط",
                                    QuizLevel.HARD to "سخت"
                                ).forEach { (lvl, title) ->
                                    val isSelected = (selectedQuizLevel == lvl)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) colors.secondary else colors.surfaceVariant)
                                            .clickable { selectedQuizLevel = lvl }
                                            .padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) colors.onSecondary else colors.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Card limit selector
                        Text(
                            text = "سقف تعداد واژگان جلسه",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10, 20, 30, 50, 100).forEach { limit ->
                                val isSelected = (selectedMaxCards == limit)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.primary else colors.surfaceVariant)
                                        .clickable { selectedMaxCards = limit }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = limit.toPersianDigits(),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // Bottom CTA Bar
        val isReady = candidateCount > 0
        val actualSessionCards = minOf(candidateCount, selectedMaxCards)
        val buttonText = if (isReady) {
            if (candidateCount > selectedMaxCards) {
                "شروع مرور (${actualSessionCards.toPersianDigits()} از ${candidateCount.toPersianDigits()} واژه)"
            } else {
                "شروع مرور (${actualSessionCards.toPersianDigits()} واژه)"
            }
        } else {
            "واژه‌ای با این فیلتر یافت نشد"
        }

        Button(
            onClick = {
                if (isReady) onStartReview(buildFilters())
            },
            enabled = isReady,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(52.dp),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                disabledContainerColor = colors.surfaceVariant,
                disabledContentColor = colors.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = if (isReady) Icons.Default.PlayArrow else Icons.Default.FilterList,
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
fun ModeSelectButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.primary else colors.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.onPrimary else colors.onSurface
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
    val dimensions = LocalYadinDimensions.current
    val colors = LocalYadinColors.current
    val shape = RoundedCornerShape(dimensions.cornerSmall)

    val containerBg = if (isSelected) {
        accentColor.copy(alpha = if (colors.isGemini) 0.22f else 0.16f)
    } else {
        accentColor.copy(alpha = if (colors.isGemini) 0.08f else 0.05f)
    }

    val borderColor = if (isSelected) {
        accentColor
    } else {
        accentColor.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                fontSize = 11.5.sp,
                maxLines = 1
            )
            Text(
                text = if (dueCount > 0) "${dueCount.toPersianDigits()} آماده" else "مرور شده",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (dueCount > 0) accentColor else colors.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.5.sp,
                maxLines = 1
            )
            Text(
                text = "از ${totalCount.toPersianDigits()} واژه",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 9.5.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ReviewSourceChip(
    item: ReviewTypeItem,
    countBadge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val shape = RoundedCornerShape(dimensions.cornerSmall)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isSelected) colors.primary else colors.surfaceVariant.copy(alpha = 0.7f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.3f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isSelected) colors.onPrimary else colors.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.onPrimary else colors.onSurface
            )
            if (!countBadge.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) colors.onPrimary.copy(alpha = 0.22f) else colors.primary.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = countBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) colors.onPrimary else colors.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CompactDiffBadge(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.18f) else colors.surfaceVariant.copy(alpha = 0.7f))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else colors.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = count.toPersianDigits(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else colors.onSurface
            )
            Text(
                text = label,
                fontSize = 10.5.sp,
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else colors.onSurfaceVariant
            )
        }
    }
}
