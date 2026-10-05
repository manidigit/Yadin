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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.ui.components.DailyReviewChart
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun ProgressScreen(
    progressPercent: Double,
    statistics: StatisticsSummary,
    dailyStats: List<DayCountRaw>,
    practicedCount: Int,
    activeDirection: CardDirection,
    onDirectionChanged: (CardDirection) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header
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
                        text = "پیشرفت و آمار یادگیری",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "محاسبه بر اساس فرمول استاندارد ۶.۱۸ سند یادین",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Direction Toggle Bar [ عادی | برعکس ] (spec 7.13)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimensions.cornerPill))
                    .background(colors.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.cornerPill))
                        .background(if (activeDirection == CardDirection.NORMAL) colors.primary else Color.Transparent)
                        .clickable { onDirectionChanged(CardDirection.NORMAL) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جهت عادی (اسپانیایی ← فارسی)",
                        color = if (activeDirection == CardDirection.NORMAL) colors.onPrimary else colors.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(dimensions.cornerPill))
                        .background(if (activeDirection == CardDirection.REVERSE) colors.primary else Color.Transparent)
                        .clickable { onDirectionChanged(CardDirection.REVERSE) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "جهت برعکس (فارسی ← اسپانیایی)",
                        color = if (activeDirection == CardDirection.REVERSE) colors.onPrimary else colors.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 3. Formula Progress Hero Card (Section 6.18)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "درصد پیشرفت کلی (فرمول نهایی)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "مجموع امتیاز کارت‌ها ÷ تعداد کل واژگان فعال",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "$progressPercent%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (progressPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = colors.primary,
                        trackColor = colors.primary.copy(alpha = 0.2f)
                    )

                    // Scoring Formula Badges Explanation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormulaScorePill("یادگرفته: ۱۰۰+", colors.success, Modifier.weight(1f))
                        FormulaScorePill("ماهانه: ۸۰+", colors.warning, Modifier.weight(1f))
                        FormulaScorePill("هفتگی: ۶۰+", colors.info, Modifier.weight(1f))
                        FormulaScorePill("روزانه: ۳۵+", colors.primary, Modifier.weight(1f))
                    }
                }
            }
        }

        // 4. Quick Metrics Summary Cards (Streak, Practiced, Accuracy)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Streak Card
                YadinCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "${statistics.currentStreakDays} روز",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "رگبار متوالی",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // Practiced Words Card
                YadinCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = colors.info,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "$practicedCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "واژه تمرین‌شده",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // Accuracy Card
                val totalReviewed = statistics.totalReviewsCount
                val totalCorrect = dailyStats.sumOf { it.correctCount }
                val accuracy = if (totalReviewed > 0) {
                    ((totalCorrect.toFloat() / totalReviewed) * 100).toInt().coerceIn(0, 100)
                } else 0

                YadinCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = colors.success,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "$accuracy%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.success
                        )
                        Text(
                            text = "دقت آزمون‌ها",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 5. Daily/Weekly Activity Chart (نمودار آمار تعداد کلمات تمرین شده)
        item {
            DailyReviewChart(dailyStats = dailyStats)
        }

        // 6. Spaced Repetition Distribution (توزیع مراحل تکرار فاصله‌دار)
        item {
            Text(
                text = "هرم توزیع مراحل یادگیری (لایتنر)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val total = statistics.totalWords.coerceAtLeast(1)

                    FunnelRow(
                        title = "مرحله روزانه (مرور هر روز)",
                        count = statistics.dailyStageCount,
                        total = total,
                        color = colors.primary
                    )
                    FunnelRow(
                        title = "مرحله هفتگی (تثبیت ۷ روزه)",
                        count = statistics.weeklyStageCount,
                        total = total,
                        color = colors.info
                    )
                    FunnelRow(
                        title = "مرحله ماهانه (حافظه ۳۰ روزه)",
                        count = statistics.monthlyStageCount,
                        total = total,
                        color = colors.warning
                    )
                    FunnelRow(
                        title = "مرحله یادگرفته‌شده (حافظه دائمی)",
                        count = statistics.learnedStageCount,
                        total = total,
                        color = colors.success
                    )
                }
            }
        }

        // 7. Word Difficulty Spectrum Counters (طیف سطح دشواری واژگان)
        item {
            Text(
                text = "شمارنده طیف سختی کلمات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DifficultyMetricBox("آسان", statistics.easyCount, colors.success, Modifier.weight(1f))
                DifficultyMetricBox("متوسط", statistics.mediumCount, colors.info, Modifier.weight(1f))
                DifficultyMetricBox("سخت", statistics.hardCount, colors.warning, Modifier.weight(1f))
                DifficultyMetricBox("خیلی سخت", statistics.veryHardCount, colors.error, Modifier.weight(1f))
            }
        }

        // 8. Gamification Achievements Gallery
        item {
            Text(
                text = "نشان‌ها و دستاوردهای یادگیری",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
        }

        val practicedCount = statistics.dailyStageCount + statistics.weeklyStageCount + statistics.monthlyStageCount + statistics.learnedStageCount

        items(AchievementId.values()) { ach ->
            val isUnlocked = when (ach) {
                AchievementId.FIRST_TEN_WORDS -> practicedCount >= 10
                AchievementId.VOCABULARY_BUILDER -> practicedCount >= 50
                AchievementId.STREAK_3_DAYS -> statistics.currentStreakDays >= 3
                AchievementId.STREAK_7_DAYS -> statistics.currentStreakDays >= 7
                AchievementId.STREAK_30_DAYS -> statistics.currentStreakDays >= 30
                AchievementId.LONG_TERM_MEMORY -> statistics.learnedStageCount >= 20
                AchievementId.HARD_MASTER -> statistics.learnedStageCount >= 5
                AchievementId.QUIZ_ACE -> statistics.totalReviewsCount >= 10
            }

            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isUnlocked) colors.primary.copy(alpha = 0.2f) else colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isUnlocked) colors.primary else colors.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ach.titleRes,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) colors.onSurface else colors.onSurfaceVariant
                        )
                        Text(
                            text = ach.descriptionRes,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "آزاد شده",
                            tint = colors.success,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FormulaScorePill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DifficultyMetricBox(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    YadinCard(
        modifier = modifier,
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = colors.onSurface
            )
        }
    }
}

@Composable
fun FunnelRow(
    title: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = (count.toFloat() / total).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("$count واژه", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
