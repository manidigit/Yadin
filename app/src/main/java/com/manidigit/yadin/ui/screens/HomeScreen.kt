package com.manidigit.yadin.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
    val normalizedTheme = themeId.lowercase()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = dimensions.screenPadding),
        verticalArrangement = Arrangement.spacedBy(
            when (normalizedTheme) {
                "claude", "googoli" -> 14.dp
                "gemini" -> 16.dp
                else -> dimensions.sectionGap
            }
        )
    ) {
        // 1. Unified Theme Header
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
                    when (normalizedTheme) {
                        "googoli" -> {
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
                        }
                        "gemini" -> {
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
                        }
                        "claude" -> {
                            // Claude: minimal, no large icon
                        }
                        else -> {
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
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (normalizedTheme == "claude") "درود و وقت‌بخیر!" else "یادین",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface
                            )
                            val badgeLabel = when (normalizedTheme) {
                                "googoli" -> "گوگولی 🌸"
                                "claude" -> "Claude"
                                "gemini" -> "Gemini"
                                else -> "GTP"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }

                        val pairLabel = if (activePair == "es-fa") "🇪🇸 اسپانیایی ⇄ فارسی 🇮🇷" else activePair
                        Text(
                            text = pairLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                // Controls: Streak + Theme toggle + Dark Mode + Help/About
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
                                imageVector = Icons.Default.Palette,
                                contentDescription = "تغییر پوسته",
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = when (normalizedTheme) {
                                    "googoli" -> "گوگولی"
                                    "claude" -> "Claude"
                                    "gemini" -> "Gemini"
                                    else -> "GTP"
                                },
                                color = colors.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Dark Mode Toggle
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

                    // Help
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
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Focused Leitner Review Card
        item {
            val isDue = dueCount > 0
            val cardShape = when (normalizedTheme) {
                "googoli" -> RoundedCornerShape(20.dp)
                "gemini" -> RoundedCornerShape(16.dp)
                "claude" -> RoundedCornerShape(12.dp)
                else -> RoundedCornerShape(dimensions.cornerMedium)
            }

            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                shape = cardShape
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
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDue) Icons.Default.AutoAwesome else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isDue) "مرور هوشمند سررسید امروز" else "واژه‌های امروز تکمیل شد 🎉",
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

                    // Direct Review Buttons
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
                                shape = RoundedCornerShape(dimensions.cornerSmall),
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
                                    .height(46.dp),
                                shape = RoundedCornerShape(dimensions.cornerSmall),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.secondary,
                                    contentColor = colors.onSecondary
                                )
                            ) {
                                Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("آزمون ۴ گزینه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { onOpenReviewSetup(ReviewType.DAILY) },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(dimensions.cornerSmall))
                                    .background(colors.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "تنظیم و فیلتر پیشرفته مرور",
                                    tint = colors.onSurface
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { onOpenReviewSetup(ReviewType.RANDOM) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(dimensions.cornerSmall),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مرور آزاد / انتخابی واژگان", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Leitner Spaced Memory Funnel
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
                            text = if (normalizedTheme == "googoli") "مراحل جعبه لایتنر 🌸" else "مراحل تثبیت حافظه لایتنر",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "${statistics.totalWords} واژه",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StageMetricPill(
                            title = "روزانه",
                            count = statistics.dailyStageCount,
                            color = colors.primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                        )
                        StageMetricPill(
                            title = "هفتگی",
                            count = statistics.weeklyStageCount,
                            color = colors.info,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.WEEKLY) }
                        )
                        StageMetricPill(
                            title = "ماهانه",
                            count = statistics.monthlyStageCount,
                            color = colors.warning,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.MONTHLY) }
                        )
                        StageMetricPill(
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
                    text = if (normalizedTheme == "googoli") "درجه سختی کلمه‌ها 🍭" else "طیف دشواری واژگان",
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

        // 5. Essential Quick Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "دسترسی‌های سریع و مدیریت واژگان",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "افزودن واژه جدید",
                        subtitle = "کارت تکی با ترجمه و صوت",
                        icon = Icons.Default.Add,
                        accentColor = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAddWord
                    )
                    ActionTile(
                        title = "ورود متنی واژگان",
                        subtitle = "پارسر و درون‌ریزی فایل",
                        icon = Icons.Default.FileUpload,
                        accentColor = colors.warning,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenImport
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "کتابخانه واژگان",
                        subtitle = "جستجو و فیلتر کلمات",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        accentColor = colors.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLibrary
                    )
                    ActionTile(
                        title = "پشتیبان‌گیری",
                        subtitle = "خروجی اکسل و JSON",
                        icon = Icons.Default.CloudDownload,
                        accentColor = colors.info,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenBackup
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
fun StageMetricPill(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.7f))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
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
                color = colors.onSurface,
                fontSize = 14.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                fontSize = 10.sp
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
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
                    fontSize = 13.sp
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
