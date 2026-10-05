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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewSetupScreen(
    initialType: ReviewType = ReviewType.RANDOM,
    categories: List<Category>,
    difficultyCounts: Map<VocabularyDifficulty, Int>,
    candidateCount: Int,
    onFilterChanged: (ReviewFilters) -> Unit,
    onStartReview: (ReviewFilters) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    // 7 Review Setup & Filter Parameters
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedMode by remember { mutableStateOf(ReviewMode.QUIZ) }
    var selectedDirection by remember { mutableStateOf(CardDirection.NORMAL) }
    var selectedQuizLevel by remember { mutableStateOf(QuizLevel.MEDIUM) }
    var selectedDifficulties by remember { mutableStateOf<Set<VocabularyDifficulty>>(emptySet()) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedMaxCards by remember { mutableStateOf(20) }

    fun buildFilters() = ReviewFilters(
        reviewType = selectedType,
        mode = selectedMode,
        direction = selectedDirection,
        quizLevel = if (selectedMode == ReviewMode.QUIZ) selectedQuizLevel else null,
        difficulties = selectedDifficulties,
        categoryIds = if (selectedCategoryId != null) setOf(selectedCategoryId!!) else emptySet(),
        maxCards = selectedMaxCards
    )

    // Notify filter changes to calculate candidate count in background
    LaunchedEffect(
        selectedType,
        selectedMode,
        selectedDirection,
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
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Clean name "مرور واژگان" as requested by user
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = colors.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = "مرور واژگان",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "تنظیم فیلترها و شروع تمرین هوشمند لایتنر",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }

            // بخش ۱: حالت پاسخ (Answer Mode: Quiz vs Flashcard)
            item {
                SectionNumberHeader("۱", "حالت تمرین و پاسخ‌گویی")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReviewOptionCard(
                        title = "آزمون ۴ گزینه‌ای",
                        subtitle = "تست چندگزینه‌ای هوشمند با گزینه‌های هم‌دسته",
                        icon = Icons.Default.Quiz,
                        isSelected = selectedMode == ReviewMode.QUIZ,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMode = ReviewMode.QUIZ }
                    )
                    ReviewOptionCard(
                        title = "فلش‌کارت تعاملی",
                        subtitle = "چرخش کارت ۳D و خودارزیابی آسان/سخت",
                        icon = Icons.Default.Style,
                        isSelected = selectedMode == ReviewMode.FLASHCARD,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMode = ReviewMode.FLASHCARD }
                    )
                }
            }

            // بخش ۲: دسته‌بندی واژگان (منوی کشویی با آیکون مرتبط و شمارنده)
            item {
                SectionNumberHeader("۲", "دسته‌بندی موضوعی کلمات")
                Spacer(modifier = Modifier.height(6.dp))
                ExposedCategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = { selectedCategoryId = it },
                    label = "انتخاب دسته‌بندی موضوعی واژگان"
                )
            }

            // بخش ۳: مرور ویژه (Special Review: تصادفی / یادگرفته‌ها)
            item {
                SectionNumberHeader("۳", "مرور ویژه و آزاد")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReviewOptionCard(
                        title = "مرور تصادفی",
                        subtitle = "انتخاب شانسی از کل بانک واژگان",
                        icon = Icons.Default.Shuffle,
                        isSelected = selectedType == ReviewType.RANDOM,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = ReviewType.RANDOM }
                    )
                    ReviewOptionCard(
                        title = "یادگرفته‌شده‌ها",
                        subtitle = "مرور حافظه دائمی و تسلط یافته",
                        icon = Icons.Default.DoneAll,
                        isSelected = selectedType == ReviewType.LEARNED,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = ReviewType.LEARNED }
                    )
                }
            }

            // بخش ۴: زمان‌بندی تکرار فاصله‌دار (روزانه / هفتگی / ماهانه)
            item {
                SectionNumberHeader("۴", "زمان‌بندی تکرار فاصله‌دار (لایتنر)")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScheduleChoicePill(
                        title = "روزانه",
                        subtitle = "سررسید امروز",
                        icon = Icons.Default.FormatListNumbered,
                        isSelected = selectedType == ReviewType.DAILY,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = ReviewType.DAILY }
                    )
                    ScheduleChoicePill(
                        title = "هفتگی",
                        subtitle = "تثبیت ۷ روزه",
                        icon = Icons.Default.Category,
                        isSelected = selectedType == ReviewType.WEEKLY,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = ReviewType.WEEKLY }
                    )
                    ScheduleChoicePill(
                        title = "ماهانه",
                        subtitle = "حافظه ۳۰ روزه",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = selectedType == ReviewType.MONTHLY,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedType = ReviewType.MONTHLY }
                    )
                }
            }

            // بخش ۵: سطح دشواری کلمات با شمارنده‌های زنده
            item {
                SectionNumberHeader("۵", "سطح دشواری واژگان (شمارنده زنده کلمات)")
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isAllDiff = selectedDifficulties.isEmpty()
                    DifficultySelectBadge(
                        label = "همه سطوح",
                        count = difficultyCounts.values.sum(),
                        isSelected = isAllDiff,
                        color = colors.primary,
                        onClick = { selectedDifficulties = emptySet() }
                    )

                    listOf(
                        VocabularyDifficulty.EASY to ("آسان" to colors.success),
                        VocabularyDifficulty.MEDIUM to ("متوسط" to colors.info),
                        VocabularyDifficulty.HARD to ("سخت" to colors.warning),
                        VocabularyDifficulty.VERY_HARD to ("خیلی سخت" to colors.error)
                    ).forEach { (diff, pair) ->
                        val (label, diffColor) = pair
                        val count = difficultyCounts[diff] ?: 0
                        val isSelected = diff in selectedDifficulties
                        DifficultySelectBadge(
                            label = label,
                            count = count,
                            isSelected = isSelected,
                            color = diffColor,
                            onClick = {
                                selectedDifficulties = if (isSelected) {
                                    selectedDifficulties - diff
                                } else {
                                    selectedDifficulties + diff
                                }
                            }
                        )
                    }
                }
            }

            // بخش ۶: سطح دشواری آزمون (اگر آزمون انتخاب شده باشد)
            if (selectedMode == ReviewMode.QUIZ) {
                item {
                    SectionNumberHeader("۶", "سطح سختی آزمون ۴ گزینه‌ای")
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            QuizLevel.EASY to "مبتدی (گزینه‌های تصادفی)",
                            QuizLevel.MEDIUM to "متوسط (هم‌دسته/هم‌طول)",
                            QuizLevel.HARD to "حرفه‌ای (مشابه‌نما و سخت)"
                        ).forEach { (lvl, title) ->
                            FilterSelectPill(
                                label = title,
                                isSelected = selectedQuizLevel == lvl,
                                modifier = Modifier.weight(1f),
                                onClick = { selectedQuizLevel = lvl }
                            )
                        }
                    }
                }
            }

            // بخش ۷: تعداد واژگان مرور در این جلسه
            item {
                SectionNumberHeader(if (selectedMode == ReviewMode.QUIZ) "۷" else "۶", "سقف تعداد واژگان در این جلسه")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 20, 30, 50, 100).forEach { limit ->
                        FilterSelectPill(
                            label = "$limit واژه",
                            isSelected = selectedMaxCards == limit,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedMaxCards = limit }
                        )
                    }
                }
            }

            // جهت کارت (عادی یا برعکس)
            item {
                SectionNumberHeader("•", "جهت مطالعه کارت‌ها")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterSelectPill(
                        label = "اسپانیایی ← فارسی (عادی)",
                        isSelected = selectedDirection == CardDirection.NORMAL,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDirection = CardDirection.NORMAL }
                    )
                    FilterSelectPill(
                        label = "فارسی ← اسپانیایی (برعکس)",
                        isSelected = selectedDirection == CardDirection.REVERSE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDirection = CardDirection.REVERSE }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Live Candidate Count Summary & Big Start CTA
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isReady = candidateCount > 0

            // Live filter summary card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isReady) "$candidateCount واژه آماده مرور" else "واژه‌ای یافت نشد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.onSurface
                        )
                    }

                    Text(
                        text = if (selectedMode == ReviewMode.QUIZ) "آزمون ۴ گزینه‌ای" else "فلش‌کارت تعاملی",
                        fontSize = 11.sp,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = {
                    if (isReady) onStartReview(buildFilters())
                },
                enabled = isReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
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
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = if (isReady) {
                        "شروع مرور ($candidateCount واژه)"
                    } else {
                        "هیچ کلمه‌ای با این فیلترها یافت نشد ✓"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SectionNumberHeader(number: String, title: String) {
    val colors = LocalYadinColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = colors.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )
    }
}

@Composable
private fun ReviewOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .background(if (isSelected) colors.primary.copy(alpha = 0.14f) else colors.surface)
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) colors.primary else colors.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ScheduleChoicePill(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.secondary.copy(alpha = 0.15f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.secondary else colors.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.secondary else colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) colors.secondary else colors.onSurface,
                fontSize = 12.sp
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterSelectPill(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary.copy(alpha = 0.16f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.primary else colors.onSurface,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DifficultySelectBadge(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(dimensions.cornerPill))
            .background(if (isSelected) color else colors.surface)
            .border(
                width = 1.dp,
                color = if (isSelected) color else colors.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(dimensions.cornerPill)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else colors.onSurface,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.3f) else colors.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    fontSize = 10.sp,
                    color = if (isSelected) Color.White else colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
