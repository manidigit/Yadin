package com.manidigit.yadin.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.domain.algorithm.AchievementTierEvaluator
import com.manidigit.yadin.domain.model.AchievementCategory
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.AchievementTier
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.TieredGroupState
import com.manidigit.yadin.domain.time.ClockAndDayMath
import com.manidigit.yadin.ui.components.DailyReviewChart
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.theme.YadinPalette
import com.manidigit.yadin.ui.util.formatDigits

@Composable
fun ProgressScreen(
    progressPercent: Double,
    statistics: StatisticsSummary,
    dailyStats: List<DayCountRaw>,
    practicedCount: Int,
    activeDirection: CardDirection,
    achievements: List<com.manidigit.yadin.data.local.entity.AchievementEntity> = emptyList(),
    totalCorrectCount: Int = 0,
    onRefresh: () -> Unit = {},
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val context = LocalContext.current

    val totalReviewed = statistics.totalReviewsCount
    val totalAccuracy = if (totalReviewed > 0) {
        ((totalCorrectCount.toFloat() / totalReviewed) * 100).toInt().coerceIn(0, 100)
    } else 0

    // Calculations for Review Accuracy breakdown (امروز، این هفته، این ماه، مجموع کل)
    val today = ClockAndDayMath.todayDayString()
    val statMap = dailyStats.associateBy { it.reviewedDay }

    // امروز (Today)
    val todayStat = statMap[today]
    val todayCorrect = todayStat?.correctCount ?: 0
    val todayTotal = todayStat?.totalCount ?: 0
    val todayPercent = if (todayTotal > 0) ((todayCorrect.toFloat() / todayTotal) * 100).toInt().coerceIn(0, 100) else 0

    // این هفته (۷ روز اخیر)
    val weekDays = (0..6).map { ClockAndDayMath.addDays(today, -it) }
    val weekCorrect = weekDays.sumOf { statMap[it]?.correctCount ?: 0 }
    val weekTotal = weekDays.sumOf { statMap[it]?.totalCount ?: 0 }
    val weekPercent = if (weekTotal > 0) ((weekCorrect.toFloat() / weekTotal) * 100).toInt().coerceIn(0, 100) else 0

    // این ماه (۳۰ روز اخیر)
    val monthDays = (0..29).map { ClockAndDayMath.addDays(today, -it) }
    val monthCorrect = monthDays.sumOf { statMap[it]?.correctCount ?: 0 }
    val monthTotal = monthDays.sumOf { statMap[it]?.totalCount ?: 0 }
    val monthPercent = if (monthTotal > 0) ((monthCorrect.toFloat() / monthTotal) * 100).toInt().coerceIn(0, 100) else 0

    // مجموع کل
    val allTimeCorrect = totalCorrectCount
    val allTimeTotal = totalReviewed
    val allTimePercent = totalAccuracy

    // دستاوردها (Achievements count)
    val totalAchievements = AchievementId.values().size
    val unlockedCount = AchievementId.values().count { ach ->
        achievements.any { it.id == ach.name && it.unlockedAt != null }
    }
    val unlockedPercent = if (totalAchievements > 0) ((unlockedCount.toFloat() / totalAchievements) * 100).toInt() else 0

    val tieredGroups = remember(achievements) {
        AchievementTierEvaluator.getTieredGroups(achievements)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ۱. هدر بالا با دکمه بازگشت (Top Header with Back)
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
                        contentDescription = stringResource(R.string.action_back),
                        tint = colors.onSurface
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.progress_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = stringResource(R.string.progress_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // نوار رگبار متوالی (Streak Badge)
        if (statistics.currentStreakDays > 0) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.warning.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.progress_streak_days_fmt, statistics.currentStreakDays.formatDigits()),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.warning
                    )
                }
            }
        }

        // ۱. کارت‌های خلاصه (سه عدد):
        // - دقت کل: درصد
        // - یادگرفته: تعداد واژه‌ها
        // - کل واژه‌ها تمرین شده: تعداد
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // کارت دقت کل
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
                            text = "${totalAccuracy.formatDigits()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.success
                        )
                        Text(
                            text = stringResource(R.string.progress_total_accuracy),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // کارت یادگرفته
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
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = statistics.learnedStageCount.formatDigits(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.onSurface
                        )
                        Text(
                            text = stringResource(R.string.progress_mastered_words),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // کارت کل واژه‌ها تمرین شده
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
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = colors.info,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = practicedCount.formatDigits(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.onSurface
                        )
                        Text(
                            text = stringResource(R.string.progress_total_practiced),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ۲. پیشرفت یادگیری:
        // - درصد پیشرفت کلی
        // - نوار پیشرفت
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.progress_learning_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = stringResource(R.string.progress_learning_formula_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "${progressPercent.toInt().formatDigits()}%",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FormulaScorePill(stringResource(R.string.progress_formula_learned), colors.success, Modifier.weight(1f))
                        FormulaScorePill(stringResource(R.string.progress_formula_monthly), colors.warning, Modifier.weight(1f))
                        FormulaScorePill(stringResource(R.string.progress_formula_weekly), colors.info, Modifier.weight(1f))
                        FormulaScorePill(stringResource(R.string.progress_formula_daily), colors.primary, Modifier.weight(1f))
                    }
                }
            }
        }

        // ۳. حفظ ماندگار:
        // - تعداد پاسخ‌های درست از کل پاسخ‌ها
        // - درصد
        // - نوار پیشرفت
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.progress_deep_retention),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.progress_retention_ratio,
                                    totalCorrectCount.formatDigits(),
                                    totalReviewed.formatDigits()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "${totalAccuracy.formatDigits()}%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = colors.success
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (totalAccuracy / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = colors.success,
                        trackColor = colors.success.copy(alpha = 0.18f)
                    )
                }
            }
        }

        // ۴. فعالیت مرور:
        // - فیلتر بازه زمانی: ماهانه، هفتگی
        // - نمودار میله‌ای تعداد مرور به تفکیک روزهای هفته
        // - برچسب محور: تعداد مرور
        item {
            DailyReviewChart(dailyStats = dailyStats)
        }

        // ۵. مراحل یادگیری: (نوار پیشرفت)
        // - روزانه: تعداد واژه‌ها
        // - هفتگی: تعداد
        // - ماهانه: تعداد
        // - یادگرفته: تعداد
        item {
            Text(
                text = stringResource(R.string.progress_leitner_stages),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))

            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val total = statistics.totalWords.coerceAtLeast(1)

                    ProgressRow(
                        title = stringResource(R.string.stage_daily),
                        count = statistics.dailyStageCount,
                        total = total,
                        color = colors.primary
                    )
                    ProgressRow(
                        title = stringResource(R.string.stage_weekly),
                        count = statistics.weeklyStageCount,
                        total = total,
                        color = colors.info
                    )
                    ProgressRow(
                        title = stringResource(R.string.stage_monthly),
                        count = statistics.monthlyStageCount,
                        total = total,
                        color = colors.warning
                    )
                    ProgressRow(
                        title = stringResource(R.string.stage_learned),
                        count = statistics.learnedStageCount,
                        total = total,
                        color = colors.success
                    )
                }
            }
        }

        // ۶. پروفایل سختی: (نوار پیشرفت)
        // - آسان: تعداد
        // - متوسط: تعداد
        // - سخت: تعداد
        // - خیلی سخت: تعداد
        item {
            Text(
                text = stringResource(R.string.progress_difficulty_profile),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))

            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val diffTotal = (statistics.easyCount + statistics.mediumCount + statistics.hardCount + statistics.veryHardCount).coerceAtLeast(1)

                    ProgressRow(
                        title = stringResource(R.string.diff_easy),
                        count = statistics.easyCount,
                        total = diffTotal,
                        color = colors.success
                    )
                    ProgressRow(
                        title = stringResource(R.string.diff_medium),
                        count = statistics.mediumCount,
                        total = diffTotal,
                        color = colors.info
                    )
                    ProgressRow(
                        title = stringResource(R.string.diff_hard),
                        count = statistics.hardCount,
                        total = diffTotal,
                        color = colors.warning
                    )
                    ProgressRow(
                        title = stringResource(R.string.diff_very_hard),
                        count = statistics.veryHardCount,
                        total = diffTotal,
                        color = colors.error
                    )
                }
            }
        }

        // ۷. دقت مرور:
        // - امروز: تعداد درست از کل و درصد
        // - این هفته: تعداد درست از کل و درصد
        // - این ماه: تعداد درست از کل و درصد
        // - مجموع کل: تعداد درست از کل و درصد
        item {
            Text(
                text = stringResource(R.string.progress_review_accuracy_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))

            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_today),
                        correct = todayCorrect,
                        total = todayTotal,
                        percentage = todayPercent,
                        color = colors.primary
                    )
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_week),
                        correct = weekCorrect,
                        total = weekTotal,
                        percentage = weekPercent,
                        color = colors.info
                    )
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_month),
                        correct = monthCorrect,
                        total = monthTotal,
                        percentage = monthPercent,
                        color = colors.warning
                    )
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_all_time),
                        correct = allTimeCorrect,
                        total = allTimeTotal,
                        percentage = allTimePercent,
                        color = colors.success
                    )
                }
            }
        }

        // ۸. دستاوردهای مرحله‌ای و زنجیره‌ای (Tiered Achievements)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.progress_tiered_achievements),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.progress_achievements_count,
                        unlockedCount.formatDigits(),
                        totalAchievements.formatDigits()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
        }

        // کارت خلاصه پیشرفت دستاوردها
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.progress_all_medals),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }
                        Text(
                            text = "${unlockedPercent.formatDigits()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = colors.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (unlockedPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = colors.primary,
                        trackColor = colors.primary.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // لیست کارت‌های مرحله‌ای (هر دسته یک کارت زنده با جایگزینی خودکار سطح)
        items(
            items = tieredGroups,
            key = { it.category.name }
        ) { group: TieredGroupState ->
            TieredAchievementCard(group = group)
        }

        // ۹. دکمه بزرگ به‌روزرسانی آمار در انتهای صفحه (Refresh Button)
        item {
            Button(
                onClick = {
                    onRefresh()
                    Toast.makeText(context, context.getString(R.string.progress_stats_refreshed_toast), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.progress_btn_refresh),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProgressRow(
    title: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) (count.toFloat() / total).coerceIn(0f, 1f) else 0f
    val percent = (fraction * 100).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = stringResource(
                    R.string.progress_words_count_percent,
                    count.formatDigits(),
                    percent.formatDigits()
                ),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
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

@Composable
private fun AccuracyRow(
    label: String,
    correct: Int,
    total: Int,
    percentage: Int,
    color: Color
) {
    val fraction = (percentage / 100f).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    R.string.progress_accuracy_ratio_fmt,
                    correct.formatDigits(),
                    total.formatDigits(),
                    percentage.formatDigits()
                ),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(3.5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun TieredAchievementCard(group: TieredGroupState) {
    val colors = LocalYadinColors.current
    val ach = group.displayAchievement

    // Tier badge color & title
    val tierColor = when (ach.tier) {
        AchievementTier.BRONZE -> YadinPalette.TierBronze
        AchievementTier.SILVER -> YadinPalette.TierSilver
        AchievementTier.GOLD -> YadinPalette.TierGold
        AchievementTier.PLATINUM -> YadinPalette.TierPlatinum
    }
    val tierLabel = stringResource(ach.tier.badgeLabelRes)

    val progressFraction = if (group.isMaxLevel) 1f else {
        (group.currentProgress.toFloat() / group.targetThreshold.coerceAtLeast(1)).coerceIn(0f, 1f)
    }

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tier Icon Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(tierColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (group.isMaxLevel) Icons.Default.CheckCircle else Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = tierColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(group.category.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tierColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tierLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tierColor
                            )
                        }
                    }

                    Text(
                        text = if (group.isMaxLevel) stringResource(R.string.ach_all_tiers_cleared) else stringResource(ach.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                if (group.isMaxLevel) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.ach_completed),
                        tint = colors.success,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Progress Bar & Ratio
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (group.isMaxLevel) {
                            stringResource(R.string.ach_final_badge, stringResource(ach.titleRes))
                        } else {
                            stringResource(R.string.ach_next_target, stringResource(ach.titleRes))
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (group.isMaxLevel) {
                            stringResource(R.string.ach_100_percent)
                        } else {
                            "${group.currentProgress.formatDigits()} / ${group.targetThreshold.formatDigits()}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (group.isMaxLevel) colors.success else colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (group.isMaxLevel) colors.success else tierColor,
                    trackColor = colors.surfaceVariant
                )
            }
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
