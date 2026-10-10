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
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Shuffle
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

private data class ReviewTypeItem(val type: ReviewType, val title: String, val icon: ImageVector)
private data class DiffItem(val diff: VocabularyDifficulty, val label: String, val color: Color)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewSetupScreen(
    initialType: ReviewType = ReviewType.RANDOM,
    initialDifficulties: Set<VocabularyDifficulty> = emptySet(),
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

    var selectedType by remember { mutableStateOf(initialType) }
    var selectedMode by remember { mutableStateOf(ReviewMode.QUIZ) }
    var selectedQuizLevel by remember { mutableStateOf(QuizLevel.MEDIUM) }
    var selectedDifficulties by remember {
        mutableStateOf(if (initialDifficulties.isNotEmpty()) initialDifficulties else VocabularyDifficulty.entries.toSet())
    }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedMaxCards by remember { mutableStateOf(20) }

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
                        Text(
                            text = "انتخاب حالت، منبع و تنظیمات جلسه مرور",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
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

                        // Mode selector (Quiz vs Flashcard)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                        // نشانگر جهت مطالعه فعال (تنظیم‌شده در تنظیمات سراسری طبق دستور کارفرما)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "جهت مطالعه فعال:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = if (appLanguageDirection == CardDirection.NORMAL)
                                        "🇪🇸 اسپانیایی ← 🇮🇷 فارسی"
                                    else
                                        "🇮🇷 فارسی ← 🇪🇸 اسپانیایی",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                            Text(
                                text = "تنظیم در منوی تنظیمات",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant.copy(alpha = 0.8f)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "منبع واژگان و تکرار فاصله‌دار",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )

                        // Type selector chips
                        val reviewTypeItems = listOf(
                            ReviewTypeItem(ReviewType.DAILY, "مرور روزانه", Icons.Default.Today),
                            ReviewTypeItem(ReviewType.WEEKLY, "مرور هفتگی", Icons.Default.AutoAwesome),
                            ReviewTypeItem(ReviewType.MONTHLY, "مرور ماهانه", Icons.Default.AutoAwesome),
                            ReviewTypeItem(ReviewType.RANDOM, "تصادفی (آزاد)", Icons.Default.Shuffle),
                            ReviewTypeItem(ReviewType.LEARNED, "یادگرفته‌ها", Icons.Default.DoneAll)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            reviewTypeItems.forEach { item ->
                                val isSelected = (selectedType == item.type)
                                val countBadge = statistics?.let { stats ->
                                    when (item.type) {
                                        ReviewType.DAILY -> "${stats.dueDailyCount}/${stats.dailyStageCount}"
                                        ReviewType.WEEKLY -> "${stats.dueWeeklyCount}/${stats.weeklyStageCount}"
                                        ReviewType.MONTHLY -> "${stats.dueMonthlyCount}/${stats.monthlyStageCount}"
                                        ReviewType.LEARNED -> "${stats.availableLearnedCount}/${stats.learnedStageCount}"
                                        ReviewType.RANDOM -> "${stats.dueTodayCount}/${stats.totalWords}"
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) colors.primary else colors.surfaceVariant)
                                        .clickable { selectedType = item.type }
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
                                            tint = if (isSelected) colors.onPrimary else colors.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) colors.onPrimary else colors.onSurface
                                        )
                                        if (countBadge != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSelected) colors.onPrimary.copy(alpha = 0.2f) else colors.surface)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = countBadge,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
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

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
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
                                    QuizLevel.EASY to "مبتدی",
                                    QuizLevel.MEDIUM to "متوسط",
                                    QuizLevel.HARD to "حرفه‌ای"
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
                                        text = "$limit",
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
                text = if (isReady) "شروع مرور ($candidateCount واژه)" else "واژه‌ای با این فیلتر یافت نشد",
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
fun CompactDiffBadge(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else colors.surfaceVariant)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$label ($count)",
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.onSurface else colors.onSurfaceVariant
            )
        }
    }
}
