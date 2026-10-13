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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.components.DifficultyCounterRow
import com.manidigit.yadin.ui.components.StreakChip
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.toPersianDigits

/**
 * صفحه اصلی (Home) برنامه یادین.
 *
 * نمایش‌دهنده هدر تمیز و بدون تکرار، خلاصه آمار جامع واژگان (کل، تمرین‌شده، تمرین‌نشده، یادگرفته)،
 * مراحل تثبیت حافظه لایتنر با طراحی گرافیکی خوانا و مدرن، و طیف دشواری واژگان.
 */
@Composable
fun HomeScreen(
    dueCount: Int,
    statistics: StatisticsSummary,
    difficultyCounts: Map<VocabularyDifficulty, Int>,
    themeId: String,
    isDark: Boolean,
    activePair: String,
    onToggleTheme: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenReviewSetup: (ReviewType) -> Unit,
    onStartReview: (ReviewType, ReviewMode) -> Unit,
    onDifficultyFilterClick: (VocabularyDifficulty) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenAddWord: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val practicedCount = (statistics.totalWords - statistics.unpracticedCount).coerceAtLeast(0)
    val unpracticedCount = statistics.unpracticedCount
    val learnedCount = statistics.learnedStageCount
    val totalWords = statistics.totalWords

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Clean, Unified Header (بدون تکرار نام تم و بدون دکمه‌های تم/دارک‌مود)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.heroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "یادین",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        val pairLabel = when (activePair) {
                            "fa-es" -> "🇮🇷 فارسی ← 🇪🇸 اسپانیایی"
                            else -> "🇪🇸 اسپانیایی ← 🇮🇷 فارسی"
                        }
                        Text(
                            text = pairLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                // Controls: Streak + Help/About
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StreakChip(streakDays = statistics.currentStreakDays)

                    IconButton(
                        onClick = onOpenHelp,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "راهنما",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenAbout,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "درباره ما",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Vocabulary Statistics Overview Card (خلاصه آمار کلمات و واژگان)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "خلاصه وضعیت واژگان",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        Text(
                            text = "مجموع ${totalWords.toPersianDigits()} کلمه",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                    }

                    // 4 Metric Tiles: کل کلمات، تمرین‌شده‌ها، تمرین‌نشده‌ها، یادگرفته‌شده‌ها
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VocabStatTile(
                            title = "کل کلمات",
                            count = totalWords,
                            icon = Icons.Default.Layers,
                            accentColor = colors.primary,
                            modifier = Modifier.weight(1f)
                        )
                        VocabStatTile(
                            title = "تمرین‌شده‌ها",
                            count = practicedCount,
                            icon = Icons.Default.Visibility,
                            accentColor = colors.info,
                            modifier = Modifier.weight(1f)
                        )
                        VocabStatTile(
                            title = "تمرین‌نشده",
                            count = unpracticedCount,
                            icon = Icons.Default.VisibilityOff,
                            accentColor = colors.warning,
                            modifier = Modifier.weight(1f)
                        )
                        VocabStatTile(
                            title = "یادگرفته‌ها",
                            count = learnedCount,
                            icon = Icons.Default.CheckCircle,
                            accentColor = colors.success,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Redesigned Leitner Spaced Memory Stages (طراحی بازآرایی‌شده و خوانای مراحل لایتنر)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "مراحل تثبیت حافظه لایتنر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        Text(
                            text = "جهت ورود به جلسه لمس کنید",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeitnerStageBox(
                            title = "روزانه",
                            subtitle = if (statistics.dueDailyCount > 0) "${statistics.dueDailyCount.toPersianDigits()} آماده" else "مرور شده",
                            totalCount = statistics.dailyStageCount,
                            dueCount = statistics.dueDailyCount,
                            accentColor = colors.primary,
                            icon = Icons.Default.Today,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                        )

                        LeitnerStageBox(
                            title = "هفتگی",
                            subtitle = if (statistics.dueWeeklyCount > 0) "${statistics.dueWeeklyCount.toPersianDigits()} آماده" else "مرور شده",
                            totalCount = statistics.weeklyStageCount,
                            dueCount = statistics.dueWeeklyCount,
                            accentColor = colors.info,
                            icon = Icons.Default.AutoAwesome,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.WEEKLY) }
                        )

                        LeitnerStageBox(
                            title = "ماهانه",
                            subtitle = if (statistics.dueMonthlyCount > 0) "${statistics.dueMonthlyCount.toPersianDigits()} آماده" else "مرور شده",
                            totalCount = statistics.monthlyStageCount,
                            dueCount = statistics.dueMonthlyCount,
                            accentColor = colors.warning,
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.MONTHLY) }
                        )

                        LeitnerStageBox(
                            title = "تثبیت‌شده",
                            subtitle = "یادگیری کامل",
                            totalCount = statistics.learnedStageCount,
                            dueCount = statistics.availableLearnedCount,
                            accentColor = colors.success,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.LEARNED) }
                        )
                    }
                }
            }
        }

        // 4. Adaptive Difficulty Matrix (طیف دشواری واژگان)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "طیف دشواری واژگان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                DifficultyCounterRow(
                    counts = difficultyCounts,
                    onSelectDifficulty = onDifficultyFilterClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * ویجت نمایش‌دهنده یک آمار کلیدی از وضعیت واژگان.
 */
@Composable
private fun VocabStatTile(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalYadinDimensions.current
    val shape = RoundedCornerShape(dimensions.cornerSmall)

    Box(
        modifier = modifier
            .clip(shape)
            .background(accentColor.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.25f),
                shape = shape
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = count.toPersianDigits(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 15.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = LocalYadinColors.current.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * باکس استاندارد و شکیل نمایش مرحله تثبیت لایتنر با ابعاد و تایپوگرافی خوانا.
 */
@Composable
private fun LeitnerStageBox(
    title: String,
    subtitle: String,
    totalCount: Int,
    dueCount: Int,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dimensions = LocalYadinDimensions.current
    val colors = LocalYadinColors.current
    val shape = RoundedCornerShape(dimensions.cornerSmall)

    Box(
        modifier = modifier
            .clip(shape)
            .background(accentColor.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.35f),
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
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = totalCount.toPersianDigits(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 15.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                fontSize = 11.sp
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (dueCount > 0) accentColor else colors.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = if (dueCount > 0) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
