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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.components.DifficultyCounterRow
import com.manidigit.yadin.ui.components.StreakChip
import com.manidigit.yadin.ui.components.VocabStatType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinSectionHeader
import com.manidigit.yadin.ui.components.YadinStatTile
import com.manidigit.yadin.ui.components.getStageColor
import com.manidigit.yadin.ui.components.getStageIcon
import com.manidigit.yadin.ui.components.getVocabStatColor
import com.manidigit.yadin.ui.components.getVocabStatIcon
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits

/**
 * صفحه اصلی (Home) برنامه یادین.
 *
 * نمایش‌دهنده هدر تمیز و بدون تکرار، خلاصه آمار جامع کلمات (کل، تمرین‌شده، تمرین‌نشده، تثبیت‌شده)،
 * مراحل تثبیت حافظه لایتنر با رنگ‌بندی و آیکون‌های متمایز، و طیف دشواری کلمات با آیکون‌های اختصاصی.
 */
@Composable
fun HomeScreen(
    statistics: StatisticsSummary,
    difficultyCounts: Map<VocabularyDifficulty, Int>,
    activePair: String,
    onOpenReviewSetup: (ReviewType) -> Unit,
    onDifficultyFilterClick: (VocabularyDifficulty) -> Unit
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
        // 1. Clean, Unified Header
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
                            tint = colors.onHero,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        val pairLabel = when (activePair) {
                            "fa-es" -> stringResource(R.string.home_lang_pair_fa_es)
                            else -> stringResource(R.string.home_lang_pair_es_fa)
                        }
                        Text(
                            text = pairLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                // Controls: Streak Chip
                StreakChip(streakDays = statistics.currentStreakDays)
            }
        }

        // 2. Vocabulary Statistics Overview Card (خلاصه وضعیت کلمات با آیکون‌های استاندارد دسته ۶)
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
                        title = stringResource(R.string.home_vocab_summary),
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconTint = colors.primary
                    )

                    // 4 Metric Tiles: کل کلمات، تمرین‌شده‌ها، تمرین‌نشده‌ها، تثبیت‌شده‌ها
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        YadinStatTile(
                            title = stringResource(R.string.home_total_words),
                            value = totalWords.formatDigits(),
                            icon = getVocabStatIcon(VocabStatType.TOTAL),
                            accentColor = getVocabStatColor(VocabStatType.TOTAL, colors),
                            modifier = Modifier.weight(1f)
                        )
                        YadinStatTile(
                            title = stringResource(R.string.home_practiced_words),
                            value = practicedCount.formatDigits(),
                            icon = getVocabStatIcon(VocabStatType.PRACTICED),
                            accentColor = getVocabStatColor(VocabStatType.PRACTICED, colors),
                            modifier = Modifier.weight(1f)
                        )
                        YadinStatTile(
                            title = stringResource(R.string.home_unpracticed_words),
                            value = unpracticedCount.formatDigits(),
                            icon = getVocabStatIcon(VocabStatType.UNPRACTICED),
                            accentColor = getVocabStatColor(VocabStatType.UNPRACTICED, colors),
                            modifier = Modifier.weight(1f)
                        )
                        YadinStatTile(
                            title = stringResource(R.string.home_mastered_words),
                            value = learnedCount.formatDigits(),
                            icon = getVocabStatIcon(VocabStatType.MASTERED),
                            accentColor = getVocabStatColor(VocabStatType.MASTERED, colors),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Leitner Spaced Memory Stages (مراحل تثبیت حافظه لایتنر با رنگ و آیکون استاندارد دسته ۲ و ۳)
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
                        title = stringResource(R.string.home_leitner_box),
                        icon = Icons.Default.School,
                        iconTint = colors.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeitnerStageBox(
                            title = stringResource(R.string.stage_daily),
                            totalCount = statistics.dailyStageCount,
                            dueCount = statistics.dueDailyCount,
                            accentColor = getStageColor(Stage.DAILY, colors),
                            icon = getStageIcon(Stage.DAILY),
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.DAILY) }
                        )

                        LeitnerStageBox(
                            title = stringResource(R.string.stage_weekly),
                            totalCount = statistics.weeklyStageCount,
                            dueCount = statistics.dueWeeklyCount,
                            accentColor = getStageColor(Stage.WEEKLY, colors),
                            icon = getStageIcon(Stage.WEEKLY),
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.WEEKLY) }
                        )

                        LeitnerStageBox(
                            title = stringResource(R.string.stage_monthly),
                            totalCount = statistics.monthlyStageCount,
                            dueCount = statistics.dueMonthlyCount,
                            accentColor = getStageColor(Stage.MONTHLY, colors),
                            icon = getStageIcon(Stage.MONTHLY),
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.MONTHLY) }
                        )

                        LeitnerStageBox(
                            title = stringResource(R.string.stage_learned),
                            totalCount = statistics.learnedStageCount,
                            dueCount = statistics.availableLearnedCount,
                            accentColor = getStageColor(Stage.LEARNED, colors),
                            icon = getStageIcon(Stage.LEARNED),
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenReviewSetup(ReviewType.LEARNED) }
                        )
                    }
                }
            }
        }

        // 4. Adaptive Difficulty Matrix in YadinCard (طیف دشواری کلمات با آیکون و رنگ استاندارد دسته ۴)
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
                        title = stringResource(R.string.home_difficulty_spectrum),
                        icon = Icons.Default.Tune,
                        iconTint = colors.warning
                    )

                    DifficultyCounterRow(
                        counts = difficultyCounts,
                        onSelectDifficulty = onDifficultyFilterClick
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * باکس استاندارد، خوانا و شکیل نمایش مرحله تثبیت لایتنر در صفحه اصلی.
 */
@Composable
private fun LeitnerStageBox(
    title: String,
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
            .background(accentColor.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.35f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 2.dp),
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
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                fontSize = 11.5.sp,
                maxLines = 1
            )
            Text(
                text = if (dueCount > 0) stringResource(R.string.home_due_ready, dueCount.formatDigits()) else stringResource(R.string.home_reviewed_done),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (dueCount > 0) accentColor else colors.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.5.sp,
                maxLines = 1
            )
            Text(
                text = stringResource(R.string.home_of_total_words, totalCount.formatDigits()),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 9.5.sp,
                maxLines = 1
            )
        }
    }
}
