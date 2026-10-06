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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
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
        "googoli" -> {
            GoogoliHomeContent(
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
                onOpenHelp = onOpenHelp,
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
                onOpenHelp = onOpenHelp,
                onOpenAbout = onOpenAbout
            )
        }
    }
}

/**
 * -------------------------------------------------------------
 * CLAUDE THEME LAYOUT (Minimal, Editorial, Calm & Non-Redundant)
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
        // 1. Clean Editorial Header
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
                        text = if (activePair == "es-fa") "🇪🇸 اسپانیایی ⇄ فارسی 🇮🇷" else activePair,
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
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
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

                    // Settings
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // About
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
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Focused Hero Review Card (کارت اصلی مرور)
        item {
            val isDue = dueCount > 0

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
                                    imageVector = if (isDue) Icons.Default.AutoAwesome else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isDue) "مرور سررسید امروز" else "واژه‌های امروز تکمیل شد 🎉",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = if (isDue) "$dueCount واژه آماده مرور در صف لایتنر" else "همه واژه‌ها مرور شده‌اند، آماده تمرین آزاد هستید",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        if (isDue) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(dimensions.cornerPill))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$dueCount واژه",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    // Action buttons
                    if (isDue) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStartReview(ReviewType.DAILY, ReviewMode.FLASHCARD) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = colors.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("فلش‌کارت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onStartReview(ReviewType.DAILY, ReviewMode.QUIZ) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surfaceVariant,
                                    contentColor = colors.onSurface
                                )
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("آزمون ۴ گزینه", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onOpenReviewSetup(ReviewType.RANDOM) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("شروع تمرین تصادفی آزاد", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. مراحل یادگیری لایتنر (Learning Stages - Single Clean Card)
        item {
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
                        Text(
                            text = "مراحل تثبیت در لایتنر",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "لمس برای مرور",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClaudeStagePill(
                            title = "روزانه",
                            count = statistics.dailyStageCount,
                            color = colors.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                        )
                        ClaudeStagePill(
                            title = "هفتگی",
                            count = statistics.weeklyStageCount,
                            color = colors.info,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.WEEKLY) }
                        )
                        ClaudeStagePill(
                            title = "ماهانه",
                            count = statistics.monthlyStageCount,
                            color = colors.warning,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.MONTHLY) }
                        )
                        ClaudeStagePill(
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

        // 4. طیف دشواری واژگان
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

        // 5. دسترسی‌های سریع (Quick Actions)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClaudeActionTile(
                        title = "افزودن واژه",
                        subtitle = "کارت جدید با ترجمه",
                        icon = Icons.Default.Add,
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAddWord
                    )
                    ClaudeActionTile(
                        title = "کتابخانه واژگان",
                        subtitle = "جستجو و فیلترها",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        color = colors.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLibrary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ClaudeActionTile(
                        title = "ورود متنی واژگان",
                        subtitle = "درون‌ریزی فایل",
                        icon = Icons.Default.FileUpload,
                        color = colors.warning,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenImport
                    )
                    ClaudeActionTile(
                        title = "پیشرفت و آمار",
                        subtitle = "تحلیل یادگیری و پشتیبان",
                        icon = Icons.Default.BarChart,
                        color = colors.info,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenProgress
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun ClaudeStagePill(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "$count",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClaudeActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
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
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * -------------------------------------------------------------
 * GOOGOLI THEME LAYOUT (Cute, Playful, Pastel, Delightful & Sweet)
 * -------------------------------------------------------------
 */
@Composable
private fun GoogoliHomeContent(
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
        // 1. Cute Playful Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.heroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "سلام قشنگم! ✨",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "گوگولی 🌸",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                        Text(
                            text = if (activePair == "es-fa") "🇪🇸 اسپانیایی ⇄ فارسی 🇮🇷" else activePair,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
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
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "تغییر پوسته",
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "گوگولی",
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

                    // Settings
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات",
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // About
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
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Sweet Hero Review Card (کارت جادویی یادگیری)
        item {
            val isDue = dueCount > 0

            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                shape = RoundedCornerShape(22.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isDue) "🎯" else "🥳",
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = if (isDue) "وقت مرور و یادگیریه! 🎈" else "عالی هستی! امروز تموم شد 🎉",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Text(
                                    text = if (isDue) "$dueCount تا واژه آماده داری که ببری حافظه دائم" else "هیچ واژه سررسیدی نمونده؛ یه تمرین آزاد بزنیم؟",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        }

                        if (isDue) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(dimensions.cornerPill))
                                    .background(colors.primary.copy(alpha = 0.18f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$dueCount کلمه",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    // Action buttons
                    if (isDue) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onStartReview(ReviewType.DAILY, ReviewMode.FLASHCARD) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("🎴 فلش‌کارت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onStartReview(ReviewType.DAILY, ReviewMode.QUIZ) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.surfaceVariant,
                                    contentColor = colors.onSurface
                                )
                            ) {
                                Text("✨ آزمون ۴ گزینه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onOpenReviewSetup(ReviewType.RANDOM) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("🎲 شروع تمرین تصادفی شاداب", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. حباب‌های مراحل یادگیری (Googoli Learning Bubbles)
        item {
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                shape = RoundedCornerShape(20.dp)
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
                        Text(
                            text = "مراحل تثبیت واژه‌ها 🌸",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "لمس برای تمرین",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GoogoliStageBubble(
                            emoji = "🌸",
                            title = "روزانه",
                            count = statistics.dailyStageCount,
                            color = colors.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                        )
                        GoogoliStageBubble(
                            emoji = "🌼",
                            title = "هفتگی",
                            count = statistics.weeklyStageCount,
                            color = colors.info,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.WEEKLY) }
                        )
                        GoogoliStageBubble(
                            emoji = "🌺",
                            title = "ماهانه",
                            count = statistics.monthlyStageCount,
                            color = colors.warning,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.MONTHLY) }
                        )
                        GoogoliStageBubble(
                            emoji = "🏆",
                            title = "تثبیت‌شده",
                            count = statistics.learnedStageCount,
                            color = colors.secondary,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.LEARNED) }
                        )
                    }
                }
            }
        }

        // 4. طیف دشواری واژگان
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "درجه سختی کلمه‌ها 🍭",
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

        // 5. دسترسی‌های سریع گوگولی (Quick Actions)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GoogoliActionTile(
                        emoji = "➕",
                        title = "افزودن واژه جدید",
                        subtitle = "کارت شاداب با تلفظ",
                        color = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAddWord
                    )
                    GoogoliActionTile(
                        emoji = "📚",
                        title = "کتابخانه واژگان",
                        subtitle = "دیدن و جستجوی همه لغات",
                        color = colors.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLibrary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GoogoliActionTile(
                        emoji = "📥",
                        title = "ورود متنی واژگان",
                        subtitle = "درون‌ریزی فایل",
                        color = colors.warning,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenImport
                    )
                    GoogoliActionTile(
                        emoji = "📊",
                        title = "پیشرفت و آمار",
                        subtitle = "تحلیل روند یادگیری",
                        color = colors.info,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenProgress
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun GoogoliStageBubble(
    emoji: String,
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = emoji, fontSize = 14.sp)
            Text(
                text = "$count",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GoogoliActionTile(
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    YadinCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = colors.surface,
        shape = RoundedCornerShape(18.dp)
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Column {
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
                    fontSize = 10.sp
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
    onOpenHelp: () -> Unit,
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

                    // Help Button
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
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // About Us
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
    onOpenHelp: () -> Unit,
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

                    // Help Button
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
                        icon = Icons.AutoMirrored.Filled.MenuBook,
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
