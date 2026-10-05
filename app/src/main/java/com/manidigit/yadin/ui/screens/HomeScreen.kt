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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    when (themeId.lowercase()) {
        "claude" -> {
            ClaudeHomeContent(
                dueCount = dueCount,
                statistics = statistics,
                difficultyCounts = difficultyCounts,
                isDark = isDark,
                activePair = activePair,
                onToggleTheme = onToggleTheme,
                onToggleDarkMode = onToggleDarkMode,
                onOpenReviewSetup = onOpenReviewSetup,
                onStartReview = onStartReview,
                onDifficultyFilterClick = onDifficultyFilterClick,
                onOpenLibrary = onOpenLibrary,
                onOpenAddWord = onOpenAddWord,
                onOpenImport = onOpenImport,
                onOpenProgress = onOpenProgress,
                onOpenSettings = onOpenSettings,
                onOpenBackup = onOpenBackup,
                onOpenAbout = onOpenAbout
            )
        }
        "gemini" -> {
            // Refined, clean, non-redundant Gemini layout
            GeminiHomeContent(
                dueCount = dueCount,
                statistics = statistics,
                difficultyCounts = difficultyCounts,
                isDark = isDark,
                activePair = activePair,
                onToggleTheme = onToggleTheme,
                onToggleDarkMode = onToggleDarkMode,
                onOpenReviewSetup = onOpenReviewSetup,
                onStartReview = onStartReview,
                onDifficultyFilterClick = onDifficultyFilterClick,
                onOpenAddWord = onOpenAddWord,
                onOpenImport = onOpenImport,
                onOpenAbout = onOpenAbout
            )
        }
        else -> {
            // Unmodified, comprehensive GTP Cyber layout (original structure)
            GtpHomeContent(
                dueCount = dueCount,
                statistics = statistics,
                difficultyCounts = difficultyCounts,
                isDark = isDark,
                activePair = activePair,
                onToggleTheme = onToggleTheme,
                onToggleDarkMode = onToggleDarkMode,
                onOpenReviewSetup = onOpenReviewSetup,
                onStartReview = onStartReview,
                onDifficultyFilterClick = onDifficultyFilterClick,
                onOpenLibrary = onOpenLibrary,
                onOpenAddWord = onOpenAddWord,
                onOpenImport = onOpenImport,
                onOpenProgress = onOpenProgress,
                onOpenSettings = onOpenSettings,
                onOpenBackup = onOpenBackup,
                onOpenAbout = onOpenAbout
            )
        }
    }
}

/**
 * -------------------------------------------------------------
 * CLAUDE THEME LAYOUT (Editorial, Warm, Structured, Academic)
 * Inspired by humanist typography, paper textures, and clear sections
 * -------------------------------------------------------------
 */
@Composable
private fun ClaudeHomeContent(
    dueCount: Int,
    statistics: StatisticsSummary,
    difficultyCounts: Map<VocabularyDifficulty, Int>,
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
    onOpenAbout: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Warm Editorial Header with Persian greeting & Theme/Dark controls
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "درود و وقت‌بخیر!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Claude",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }
                    Text(
                        text = "یادگیری آفلاین واژگان با تکرار فاصله‌دار",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                // Controls: Streak + Theme Switcher + Dark Mode
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StreakChip(streakDays = statistics.currentStreakDays)

                    // Theme Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimensions.cornerPill))
                            .background(colors.surfaceVariant)
                            .clickable { onToggleTheme() }
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "تغییر پوسته",
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Claude",
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Dark/Light Mode
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "حالت شب/روز",
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Language Pair & Word Count Card
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activePair == "es-fa") "🇪🇸 اسپانیایی ⇄ فارسی 🇮🇷" else activePair,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimensions.cornerPill))
                            .background(colors.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${statistics.totalWords} واژه در دیتابیس",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary
                        )
                    }
                }
            }
        }

        // 3. Learning Progress Hero Card (پیشرفت یادگیری)
        item {
            val practicedCount = statistics.dailyStageCount + statistics.weeklyStageCount + statistics.monthlyStageCount + statistics.learnedStageCount
            val unpracticedCount = maxOf(0, statistics.totalWords - practicedCount)
            val progressScore = if (statistics.totalWords > 0) {
                (statistics.learnedStageCount.toFloat() / statistics.totalWords * 100).toInt()
            } else 0
            val retentionScore = if (practicedCount > 0) {
                ((statistics.learnedStageCount * 1.0f + statistics.monthlyStageCount * 0.8f + statistics.weeklyStageCount * 0.5f + statistics.dailyStageCount * 0.2f) / practicedCount * 100).toInt().coerceIn(0, 100)
            } else 100

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
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "پیشرفت یادگیری و تسلط",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                        }

                        Text(
                            text = "$progressScore%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primary
                        )
                    }

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (progressScore / 100f).coerceIn(0.03f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.heroGradient)
                        )
                    }

                    // Sub stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تسلط دائم: ${statistics.learnedStageCount} از ${statistics.totalWords} واژه",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "حفظ ماندگار: $retentionScore%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.secondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 4. خلاصه آماری واژگان (4 Cards Grid)
        item {
            val practicedCount = statistics.dailyStageCount + statistics.weeklyStageCount + statistics.monthlyStageCount + statistics.learnedStageCount
            val unpracticedCount = maxOf(0, statistics.totalWords - practicedCount)

            Text(
                text = "خلاصه آماری واژگان",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClaudeStatCard(
                    title = "کل واژگان",
                    count = statistics.totalWords.toString(),
                    subtitle = "بانک جامع واژه‌ها",
                    color = colors.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenLibrary
                )
                ClaudeStatCard(
                    title = "تمرین‌شده",
                    count = practicedCount.toString(),
                    subtitle = "در جریان یادگیری",
                    color = colors.info,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenProgress
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClaudeStatCard(
                    title = "یادگرفته‌شده",
                    count = statistics.learnedStageCount.toString(),
                    subtitle = "تثبیت دائم حافظه",
                    color = colors.success,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenProgress
                )
                ClaudeStatCard(
                    title = "تمرین‌نشده",
                    count = unpracticedCount.toString(),
                    subtitle = "واژگان جدید",
                    color = colors.warning,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenReviewSetup(ReviewType.RANDOM) }
                )
            }
        }

        // 5. مرورهای آماده بر اساس تکرار فاصله‌دار (Scheduled Reviews Queue)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مرورهای زمان‌بندی‌شده (تکرار فاصله‌دار)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                if (dueCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimensions.cornerPill))
                            .background(colors.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$dueCount واژه سررسید",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Daily Leitner Box
            ClaudeReviewStageCard(
                title = "مرور روزانه",
                badgeText = "$dueCount آماده از ${statistics.dailyStageCount} کلمه",
                isDue = dueCount > 0,
                accentColor = colors.primary,
                onFlashcardClick = { onStartReview(ReviewType.DAILY, ReviewMode.FLASHCARD) },
                onQuizClick = { onStartReview(ReviewType.DAILY, ReviewMode.QUIZ) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Weekly Box
            ClaudeReviewStageCard(
                title = "مرور هفتگی",
                badgeText = "۰ آماده از ${statistics.weeklyStageCount} کلمه",
                isDue = false,
                accentColor = colors.info,
                onFlashcardClick = { onStartReview(ReviewType.WEEKLY, ReviewMode.FLASHCARD) },
                onQuizClick = { onStartReview(ReviewType.WEEKLY, ReviewMode.QUIZ) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Box
            ClaudeReviewStageCard(
                title = "مرور ماهانه",
                badgeText = "۰ آماده از ${statistics.monthlyStageCount} کلمه",
                isDue = false,
                accentColor = colors.secondary,
                onFlashcardClick = { onStartReview(ReviewType.MONTHLY, ReviewMode.FLASHCARD) },
                onQuizClick = { onStartReview(ReviewType.MONTHLY, ReviewMode.QUIZ) }
            )
        }

        // 6. Difficulty Profile
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سطوح دشواری واژگان",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "فیلتر بر اساس سختی",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            DifficultyCounterRow(
                counts = difficultyCounts,
                onSelectDifficulty = onDifficultyFilterClick
            )
        }

        // 7. Operations & Tools Hub (لغات تکی, لغات گروهی, ریستور بکاپ, کتابخانه)
        item {
            Text(
                text = "مرکز افزودن و ابزارها",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClaudeActionCard(
                    title = "لغات تکی",
                    subtitle = "افزودن واژه با تلفظ و معنی",
                    icon = Icons.Default.Add,
                    color = colors.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAddWord
                )
                ClaudeActionCard(
                    title = "لغات گروهی",
                    subtitle = "ورود متنی چندتایی با Paste",
                    icon = Icons.Default.FileUpload,
                    color = colors.secondary,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenImport
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ClaudeActionCard(
                    title = "پشتیبان‌گیری",
                    subtitle = "خروجی و بازیابی JSON",
                    icon = Icons.Default.CloudDownload,
                    color = colors.info,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBackup
                )
                ClaudeActionCard(
                    title = "کتابخانه واژگان",
                    subtitle = "جستجو و فیلتر دسته‌بندی‌ها",
                    icon = Icons.Default.MenuBook,
                    color = colors.warning,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenLibrary
                )
            }
        }

        // 8. Custom Review Quick Setup Card
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                onClick = { onOpenReviewSetup(ReviewType.RANDOM) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "آزمون و مرور سفارشی پیشرفته",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "فیلتر بر اساس دسته‌بندی، تعداد و درجه سختی",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = "تنظیم مرور",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 9. Bottom CTA Button (+ افزودن واژه)
        item {
            Button(
                onClick = onOpenAddWord,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(dimensions.cornerMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "افزودن واژه جدید",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Bottom Spacer
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ClaudeStatCard(
    title: String,
    count: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalYadinColors.current

    YadinCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ClaudeReviewStageCard(
    title: String,
    badgeText: String,
    isDue: Boolean,
    accentColor: Color,
    onFlashcardClick: () -> Unit,
    onQuizClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimensions.cornerPill))
                        .background(if (isDue) accentColor.copy(alpha = 0.15f) else colors.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = if (isDue) accentColor else colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (isDue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFlashcardClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فلش‌کارت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onQuizClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(dimensions.cornerSmall),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("آزمون ۴ گزینه", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ClaudeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    YadinCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(dimensions.cornerSmall))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * -------------------------------------------------------------
 * GEMINI THEME LAYOUT (Refined, Modern, Focused on Learning)
 * Eliminates duplicate navigation tiles present in BottomBar
 * -------------------------------------------------------------
 */
@Composable
private fun GeminiHomeContent(
    dueCount: Int,
    statistics: StatisticsSummary,
    difficultyCounts: Map<VocabularyDifficulty, Int>,
    isDark: Boolean,
    activePair: String,
    onToggleTheme: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenReviewSetup: (ReviewType) -> Unit,
    onStartReview: (ReviewType, ReviewMode) -> Unit,
    onDifficultyFilterClick: (VocabularyDifficulty) -> Unit,
    onOpenAddWord: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Futuristic Gemini Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.heroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "یادین",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.14f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Gemini",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                        Text(
                            text = if (activePair == "es-fa") "اسپانیایی ⇄ فارسی" else activePair,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StreakChip(streakDays = statistics.currentStreakDays)

                    // Theme Switcher Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimensions.cornerPill))
                            .background(colors.surfaceVariant)
                            .clickable { onToggleTheme() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "تغییر پوسته",
                                tint = colors.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Gemini",
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Dark/Light Mode
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "حالت شب/روز",
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Focused Gemini Hero Card: Daily Leitner Spaced Review
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مرور هوشمند روزانه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "کارت‌های نیازمند تمرین برای تثبیت حافظه",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(dimensions.cornerPill))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$dueCount واژه سررسید",
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Direct review triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onStartReview(ReviewType.DAILY, ReviewMode.FLASHCARD) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(dimensions.cornerMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فلش‌کارت", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { onStartReview(ReviewType.DAILY, ReviewMode.QUIZ) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(dimensions.cornerMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.secondary,
                                contentColor = colors.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("آزمون ۴ گزینه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        IconButton(
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(dimensions.cornerMedium))
                                .background(colors.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "تنظیم و فیلتر پیشرفته مرور",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Leitner Spaced Memory Funnel (مراحل یادگیری لایتنر)
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
                        Text(
                            text = "مراحل تثبیت حافظه لایتنر",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "${statistics.totalWords} واژه فعال",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GeminiStagePill(
                            title = "روزانه",
                            count = statistics.dailyStageCount,
                            color = colors.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                        )
                        GeminiStagePill(
                            title = "هفتگی",
                            count = statistics.weeklyStageCount,
                            color = colors.info,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.WEEKLY) }
                        )
                        GeminiStagePill(
                            title = "ماهانه",
                            count = statistics.monthlyStageCount,
                            color = colors.warning,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.MONTHLY) }
                        )
                        GeminiStagePill(
                            title = "تثبیت‌شده",
                            count = statistics.learnedStageCount,
                            color = colors.success,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.LEARNED) }
                        )
                    }
                }
            }
        }

        // 4. Adaptive Difficulty Matrix
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "طیف دشواری واژگان",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                DifficultyCounterRow(
                    counts = difficultyCounts,
                    onSelectDifficulty = onDifficultyFilterClick
                )
            }
        }

        // 5. Essential Creation Actions ONLY (افزودن و ورود متن - بدون تکرار دکمه‌های نوار پایین)
        item {
            Text(
                text = "افزودن و مدیریت واژگان",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "افزودن واژه جدید",
                    subtitle = "کارت تکی با ترجمه و تلفظ",
                    icon = Icons.Default.Add,
                    accentColor = colors.success,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAddWord
                )
                ActionTile(
                    title = "ورود متنی هوشمند",
                    subtitle = "پارسر پیشرفته و درون‌ریزی",
                    icon = Icons.Default.FileUpload,
                    accentColor = colors.info,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenImport
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * -------------------------------------------------------------
 * GTP THEME LAYOUT (Original Comprehensive Cyber Dashboard)
 * -------------------------------------------------------------
 */
@Composable
private fun GtpHomeContent(
    dueCount: Int,
    statistics: StatisticsSummary,
    difficultyCounts: Map<VocabularyDifficulty, Int>,
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
    onOpenAbout: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimensions.sectionGap)
    ) {
        // 1. Top Header: App Title, Theme Switcher, Dark mode, About Us icon
        item {
            Spacer(modifier = Modifier.height(12.dp))
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.heroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "یادین",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GTP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                        Text(
                            text = if (activePair == "es-fa") "اسپانیایی ⇄ فارسی" else activePair,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StreakChip(streakDays = statistics.currentStreakDays)

                    // Theme Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimensions.cornerPill))
                            .background(colors.surfaceVariant)
                            .border(1.dp, colors.outline.copy(alpha = 0.45f), RoundedCornerShape(dimensions.cornerPill))
                            .clickable { onToggleTheme() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "تغییر پوسته",
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "GTP",
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Dark/Light Mode Button
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "حالت شب/روز",
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // About Us Button
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

        // 2. Hero Card: Due Cards For Review
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مرور روزانه واژگان",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Text(
                                text = "جعبه لایتنر هوشمند سررسید امروز",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$dueCount واژه سررسید",
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onStartReview(ReviewType.DAILY, ReviewMode.FLASHCARD) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(dimensions.cornerMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("فلش‌کارت", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { onStartReview(ReviewType.DAILY, ReviewMode.QUIZ) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(dimensions.cornerMedium),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.secondary,
                                contentColor = colors.onSecondary
                            )
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("آزمون ۴ گزینه", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        IconButton(
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(dimensions.cornerMedium))
                                .background(colors.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "تنظیم و فیلتر پیشرفته مرور",
                                tint = colors.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Difficulty Breakdown Counters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "شمارنده سختی کلمات (آستانه ۳ پاسخ)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "فیلتر بر اساس سختی",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                    modifier = Modifier.clickable { onOpenReviewSetup(ReviewType.DAILY) }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            DifficultyCounterRow(
                counts = difficultyCounts,
                onSelectDifficulty = onDifficultyFilterClick
            )
        }

        // 4. Learning Stages Funnel
        item {
            Text(
                text = "مراحل یادگیری لایتنر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StageMetricCard(
                    title = "روزانه",
                    count = statistics.dailyStageCount,
                    color = colors.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                )
                StageMetricCard(
                    title = "هفتگی",
                    count = statistics.weeklyStageCount,
                    color = colors.info,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenReviewSetup(ReviewType.WEEKLY) }
                )
                StageMetricCard(
                    title = "ماهانه",
                    count = statistics.monthlyStageCount,
                    color = colors.warning,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenReviewSetup(ReviewType.MONTHLY) }
                )
                StageMetricCard(
                    title = "یادگرفته",
                    count = statistics.learnedStageCount,
                    color = colors.success,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenReviewSetup(ReviewType.LEARNED) }
                )
            }
        }

        // 5. Quick Actions Grid
        item {
            Text(
                text = "دسترسی سریع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "کتابخانه واژگان",
                        subtitle = "${statistics.totalWords} واژه فعال",
                        icon = Icons.Default.MenuBook,
                        accentColor = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLibrary
                    )
                    ActionTile(
                        title = "مرور واژگان",
                        subtitle = "تمرین و آزمون هوشمند",
                        icon = Icons.Default.Tune,
                        accentColor = colors.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "افزودن واژه جدید",
                        subtitle = "ایجاد کارت دستی",
                        icon = Icons.Default.Add,
                        accentColor = colors.success,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAddWord
                    )
                    ActionTile(
                        title = "ورود متنی واژگان",
                        subtitle = "پارسر و درون‌ریزی فایل",
                        icon = Icons.Default.FileUpload,
                        accentColor = colors.info,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenImport
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "پیشرفت و آمار",
                        subtitle = "فرمول ۶.۱۸ و نمودار",
                        icon = Icons.Default.BarChart,
                        accentColor = colors.warning,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenProgress
                    )
                    ActionTile(
                        title = "پشتیبان و بازیابی",
                        subtitle = "صادرات و بازگردانی JSON",
                        icon = Icons.Default.CloudDownload,
                        accentColor = colors.info,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenBackup
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "درباره ما",
                        subtitle = "مشخصات یادین و سازنده",
                        icon = Icons.Default.Info,
                        accentColor = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAbout
                    )
                    ActionTile(
                        title = "تنظیمات برنامه",
                        subtitle = "پوسته GTP و Gemini",
                        icon = Icons.Default.Settings,
                        accentColor = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSettings
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
private fun GeminiStagePill(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun StageMetricCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dimensions = LocalYadinDimensions.current
    val colors = LocalYadinColors.current

    YadinCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dimensions = LocalYadinDimensions.current
    val colors = LocalYadinColors.current

    YadinCard(
        modifier = modifier,
        onClick = onClick
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
                    .clip(RoundedCornerShape(dimensions.cornerSmall))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
