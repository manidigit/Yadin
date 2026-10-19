package com.manidigit.yadin.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import com.manidigit.yadin.ui.theme.YadinIcons
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
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.AchievementTier
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.TieredGroupState
import com.manidigit.yadin.domain.model.CategoryMasteryItem
import com.manidigit.yadin.domain.model.RetentionMetrics
import com.manidigit.yadin.domain.model.ReviewForecastItem
import com.manidigit.yadin.domain.model.StudyHourBucket
import com.manidigit.yadin.domain.time.ClockAndDayMath
import androidx.compose.ui.graphics.vector.ImageVector
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.components.CategoryMasteryCard
import com.manidigit.yadin.ui.components.DailyReviewChart
import com.manidigit.yadin.ui.components.RetentionGaugeCard
import com.manidigit.yadin.ui.components.StudyHabitsCard
import com.manidigit.yadin.ui.components.UpcomingForecastCard
import com.manidigit.yadin.ui.components.VocabStatType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSectionHeader
import com.manidigit.yadin.ui.components.YadinStatTile
import com.manidigit.yadin.ui.components.getDifficultyColor
import com.manidigit.yadin.ui.components.getDifficultyIcon
import com.manidigit.yadin.ui.components.getReviewModeIcon
import com.manidigit.yadin.ui.components.getStageColor
import com.manidigit.yadin.ui.components.getStageIcon
import com.manidigit.yadin.ui.components.getVocabStatColor
import com.manidigit.yadin.ui.components.getVocabStatIcon
import com.manidigit.yadin.domain.model.ReviewMode
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
    retentionMetrics: RetentionMetrics? = null,
    upcomingForecast: List<ReviewForecastItem> = emptyList(),
    studyHourBuckets: List<StudyHourBucket> = emptyList(),
    categoryMastery: List<CategoryMasteryItem> = emptyList(),
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
        // ۱. هدر استاندارد بالا با دکمه بازگشت (Top Header with Back)
        item {
            val dirText = when (activeDirection) {
                CardDirection.NORMAL -> stringResource(R.string.settings_lang_normal)
                CardDirection.REVERSE -> stringResource(R.string.settings_lang_reverse)
            }
            YadinScreenHeader(
                title = stringResource(R.string.progress_title),
                subtitle = "${stringResource(R.string.progress_subtitle)} ($dirText)",
                onBack = onBack
            )
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
                        imageVector = YadinIcons.streakFire,
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

        // ۱. کارت‌های خلاصه (سه عدد با YadinStatTile و آیکون‌های استاندارد ۶گانه):
        // - دقت کل: درصد
        // - یادگرفته: تعداد واژه‌ها (School)
        // - کل واژه‌ها تمرین شده: تعداد (AssignmentTurnedIn)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    YadinStatTile(
                        title = stringResource(R.string.progress_total_accuracy),
                        value = "${totalAccuracy.formatDigits()}%",
                        icon = getReviewModeIcon(ReviewMode.QUIZ),
                        accentColor = colors.success,
                        modifier = Modifier.weight(1f)
                    )
                    YadinStatTile(
                        title = stringResource(R.string.progress_mastered_words),
                        value = statistics.learnedStageCount.formatDigits(),
                        icon = getVocabStatIcon(VocabStatType.MASTERED),
                        accentColor = getVocabStatColor(VocabStatType.MASTERED, colors),
                        modifier = Modifier.weight(1f)
                    )
                    YadinStatTile(
                        title = stringResource(R.string.progress_total_practiced),
                        value = practicedCount.formatDigits(),
                        icon = getVocabStatIcon(VocabStatType.PRACTICED),
                        accentColor = getVocabStatColor(VocabStatType.PRACTICED, colors),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }


        if (retentionMetrics != null) {
            item {
                RetentionGaugeCard(metrics = retentionMetrics)
            }
        } else {
            item {
                YadinCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        YadinSectionHeader(
                            title = stringResource(R.string.progress_deep_retention),
                            subtitle = stringResource(
                                R.string.progress_retention_ratio,
                                totalCorrectCount.formatDigits(),
                                totalReviewed.formatDigits()
                            ),
                            icon = getStageIcon(Stage.LEARNED),
                            iconTint = colors.success,
                            trailingContent = {
                                Text(
                                    text = "${totalAccuracy.formatDigits()}%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = colors.success
                                )
                            }
                        )

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
        }

        // نمودار تعاملی روند حجم مرورها و منحنی دقت
        item {
            DailyReviewChart(dailyStats = dailyStats)
        }

        // پیش‌بینی موعد مرورهای آینده
        if (upcomingForecast.isNotEmpty()) {
            item {
                UpcomingForecastCard(forecastItems = upcomingForecast)
            }
        }

        // ۵. مراحل یادگیری لایتنر (با YadinSectionHeader یکپارچه درون کارت)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    YadinSectionHeader(
                        title = stringResource(R.string.progress_leitner_stages),
                        icon = getStageIcon(Stage.DAILY),
                        iconTint = colors.primary
                    )

                    val total = statistics.totalWords.coerceAtLeast(1)

                    ProgressRow(
                        title = stringResource(R.string.stage_daily),
                        count = statistics.dailyStageCount,
                        total = total,
                        color = getStageColor(Stage.DAILY, colors),
                        icon = getStageIcon(Stage.DAILY)
                    )
                    ProgressRow(
                        title = stringResource(R.string.stage_weekly),
                        count = statistics.weeklyStageCount,
                        total = total,
                        color = getStageColor(Stage.WEEKLY, colors),
                        icon = getStageIcon(Stage.WEEKLY)
                    )
                    ProgressRow(
                        title = stringResource(R.string.stage_monthly),
                        count = statistics.monthlyStageCount,
                        total = total,
                        color = getStageColor(Stage.MONTHLY, colors),
                        icon = getStageIcon(Stage.MONTHLY)
                    )
                    ProgressRow(
                        title = stringResource(R.string.stage_learned),
                        count = statistics.learnedStageCount,
                        total = total,
                        color = getStageColor(Stage.LEARNED, colors),
                        icon = getStageIcon(Stage.LEARNED)
                    )
                }
            }
        }

        // ۶. پروفایل سختی (با YadinSectionHeader یکپارچه درون کارت)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    YadinSectionHeader(
                        title = stringResource(R.string.progress_difficulty_profile),
                        icon = getDifficultyIcon(VocabularyDifficulty.MEDIUM),
                        iconTint = colors.warning
                    )

                    val diffTotal = (statistics.easyCount + statistics.mediumCount + statistics.hardCount + statistics.veryHardCount).coerceAtLeast(1)

                    ProgressRow(
                        title = stringResource(R.string.diff_easy),
                        count = statistics.easyCount,
                        total = diffTotal,
                        color = getDifficultyColor(VocabularyDifficulty.EASY, colors),
                        icon = getDifficultyIcon(VocabularyDifficulty.EASY)
                    )
                    ProgressRow(
                        title = stringResource(R.string.diff_medium),
                        count = statistics.mediumCount,
                        total = diffTotal,
                        color = getDifficultyColor(VocabularyDifficulty.MEDIUM, colors),
                        icon = getDifficultyIcon(VocabularyDifficulty.MEDIUM)
                    )
                    ProgressRow(
                        title = stringResource(R.string.diff_hard),
                        count = statistics.hardCount,
                        total = diffTotal,
                        color = getDifficultyColor(VocabularyDifficulty.HARD, colors),
                        icon = getDifficultyIcon(VocabularyDifficulty.HARD)
                    )
                    ProgressRow(
                        title = stringResource(R.string.diff_very_hard),
                        count = statistics.veryHardCount,
                        total = diffTotal,
                        color = getDifficultyColor(VocabularyDifficulty.VERY_HARD, colors),
                        icon = getDifficultyIcon(VocabularyDifficulty.VERY_HARD)
                    )
                }
            }
        }

        // تحلیل عادات مطالعه و ساعات طلایی
        if (studyHourBuckets.isNotEmpty()) {
            item {
                StudyHabitsCard(buckets = studyHourBuckets)
            }
        }

        // گزارش تسلط به تفکیک موضوعات
        if (categoryMastery.isNotEmpty()) {
            item {
                CategoryMasteryCard(categoryItems = categoryMastery)
            }
        }

        // ۷. دقت مرور (با YadinSectionHeader یکپارچه درون کارت):
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    YadinSectionHeader(
                        title = stringResource(R.string.progress_review_accuracy_title),
                        icon = getReviewModeIcon(ReviewMode.QUIZ),
                        iconTint = colors.success
                    )
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_today),
                        correct = todayCorrect,
                        total = todayTotal,
                        percentage = todayPercent,
                        color = getStageColor(Stage.DAILY, colors)
                    )
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_week),
                        correct = weekCorrect,
                        total = weekTotal,
                        percentage = weekPercent,
                        color = getStageColor(Stage.WEEKLY, colors)
                    )
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_month),
                        correct = monthCorrect,
                        total = monthTotal,
                        percentage = monthPercent,
                        color = getStageColor(Stage.MONTHLY, colors)
                    )
                    AccuracyRow(
                        label = stringResource(R.string.progress_acc_all_time),
                        correct = allTimeCorrect,
                        total = allTimeTotal,
                        percentage = allTimePercent,
                        color = getStageColor(Stage.LEARNED, colors)
                    )
                }
            }
        }

        // ۸. دستاوردهای مرحله‌ای و زنجیره‌ای (Tiered Achievements)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    YadinSectionHeader(
                        title = stringResource(R.string.progress_tiered_achievements),
                        subtitle = stringResource(
                            R.string.progress_achievements_count,
                            unlockedCount.formatDigits(),
                            totalAchievements.formatDigits()
                        ),
                        icon = YadinIcons.achievementTrophy,
                        iconTint = colors.warning,
                        trailingContent = {
                            Text(
                                text = "${unlockedPercent.formatDigits()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = colors.primary
                            )
                        }
                    )

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
                        imageVector = YadinIcons.refresh,
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
    color: Color,
    icon: ImageVector? = null
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
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
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
                        imageVector = if (group.isMaxLevel) YadinIcons.statusSuccess else YadinIcons.achievementTrophy,
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
                        imageVector = YadinIcons.statusSuccess,
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

                val barColor = if (group.isMaxLevel) colors.success else tierColor
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = barColor,
                    trackColor = barColor.copy(alpha = 0.18f)
                )
            }
        }
    }
}
