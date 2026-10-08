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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.time.ClockAndDayMath
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
    achievements: List<com.manidigit.yadin.data.local.entity.AchievementEntity> = emptyList(),
    totalCorrectCount: Int = 0,
    onDirectionChanged: (CardDirection) -> Unit,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ۱. هدر بالا با دکمه بازگشت و دکمه به‌روزرسانی آمار (Top Header with Back & Refresh)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
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
                            text = "پیشرفت و آمار یادگیری",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "گزارش جامع وضعیت یادگیری و حافظه",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                // دکمه به‌روزرسانی آمار (آیتم ۹)
                IconButton(
                    onClick = {
                        onRefresh()
                        Toast.makeText(context, "آمار با موفقیت به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "به‌روزرسانی آمار",
                        tint = colors.primary
                    )
                }
            }
        }

        // نوار تغییر جهت یادگیری [ عادی | برعکس ]
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
                        text = "رگبار متوالی: ${statistics.currentStreakDays} روز تمرین مستمر و پیوسته",
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
                            text = "$totalAccuracy%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.success
                        )
                        Text(
                            text = "دقت کل",
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
                            text = "${statistics.learnedStageCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.onSurface
                        )
                        Text(
                            text = "یادگرفته",
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
                            text = "$practicedCount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.onSurface
                        )
                        Text(
                            text = "کل تمرین‌شده",
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
                                text = "پیشرفت یادگیری",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "درصد پیشرفت کلی بر اساس فرمول استاندارد لایتنر",
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
                                text = "حفظ ماندگار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "$totalCorrectCount پاسخ درست از مجموع $totalReviewed پاسخ ثبت‌شده",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "$totalAccuracy%",
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
                text = "مراحل یادگیری (لایتنر)",
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
                        title = "روزانه",
                        count = statistics.dailyStageCount,
                        total = total,
                        color = colors.primary
                    )
                    ProgressRow(
                        title = "هفتگی",
                        count = statistics.weeklyStageCount,
                        total = total,
                        color = colors.info
                    )
                    ProgressRow(
                        title = "ماهانه",
                        count = statistics.monthlyStageCount,
                        total = total,
                        color = colors.warning
                    )
                    ProgressRow(
                        title = "یادگرفته",
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
                text = "پروفایل سختی",
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
                        title = "آسان",
                        count = statistics.easyCount,
                        total = diffTotal,
                        color = colors.success
                    )
                    ProgressRow(
                        title = "متوسط",
                        count = statistics.mediumCount,
                        total = diffTotal,
                        color = colors.info
                    )
                    ProgressRow(
                        title = "سخت",
                        count = statistics.hardCount,
                        total = diffTotal,
                        color = colors.warning
                    )
                    ProgressRow(
                        title = "خیلی سخت",
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
                text = "دقت مرور",
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
                        label = "امروز",
                        correct = todayCorrect,
                        total = todayTotal,
                        percentage = todayPercent,
                        color = colors.primary
                    )
                    AccuracyRow(
                        label = "این هفته",
                        correct = weekCorrect,
                        total = weekTotal,
                        percentage = weekPercent,
                        color = colors.info
                    )
                    AccuracyRow(
                        label = "این ماه",
                        correct = monthCorrect,
                        total = monthTotal,
                        percentage = monthPercent,
                        color = colors.warning
                    )
                    AccuracyRow(
                        label = "مجموع کل",
                        correct = allTimeCorrect,
                        total = allTimeTotal,
                        percentage = allTimePercent,
                        color = colors.success
                    )
                }
            }
        }

        // ۸. دستاوردها:
        // - تعداد دستاوردهای کسب‌شده
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "دستاوردها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "تعداد کسب‌شده: $unlockedCount از $totalAchievements",
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
                                text = "تعداد دستاوردهای کسب‌شده",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }
                        Text(
                            text = "$unlockedPercent%",
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

        // لیست دستاوردها
        items(AchievementId.values()) { ach ->
            val isUnlocked = achievements.any { it.id == ach.name && it.unlockedAt != null }

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

        // ۹. دکمه بزرگ به‌روزرسانی آمار در انتهای صفحه (Refresh Button)
        item {
            Button(
                onClick = {
                    onRefresh()
                    Toast.makeText(context, "آمار با موفقیت به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
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
                        text = "به‌روزرسانی آمار",
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
            Text("$count واژه ($percent٪)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
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
                text = "$correct از $total ($percentage٪)",
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
