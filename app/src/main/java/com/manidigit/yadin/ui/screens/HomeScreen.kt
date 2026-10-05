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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
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
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.components.DifficultyCounterRow
import com.manidigit.yadin.ui.components.StageBadge
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
    val isGemini = themeId.equals("gemini", ignoreCase = true)

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
                            .size(if (isGemini) 46.dp else 40.dp)
                            .clip(if (isGemini) RoundedCornerShape(16.dp) else CircleShape)
                            .background(colors.heroGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isGemini) Icons.Default.AutoAwesome else Icons.Default.Bolt,
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
                                    text = if (isGemini) "Gemini" else "GTP",
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

                    // Theme Toggle Pill (GTP ⇄ Gemini)
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
                                text = if (isGemini) "Gemini" else "GTP",
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

        // 3. Difficulty Breakdown Counters (شمارنده‌های سطوح سختی کلمات)
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

        // 4. Learning Stages Funnel (مراحل یادگیری لایتنر)
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
