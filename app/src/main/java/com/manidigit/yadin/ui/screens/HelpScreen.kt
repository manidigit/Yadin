package com.manidigit.yadin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.components.YadinScreenHeader
import com.manidigit.yadin.ui.components.YadinSectionHeader
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.theme.YadinTheme

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val icons = YadinTheme.icons

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Standardized Header
        YadinScreenHeader(
            title = stringResource(R.string.help_title),
            subtitle = stringResource(R.string.help_subtitle),
            onBack = onBack
        )

        // ۱. کارت شروع سریع برای کاربران تازهوارد
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.help_leitner_intro_title),
                    subtitle = stringResource(R.string.help_leitner_intro_desc),
                    icon = icons.hintLightbulb,
                    iconTint = colors.primary
                )

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

                HelpQuickStepRow(
                    icon = icons.navSettings,
                    accentColor = colors.primary,
                    title = stringResource(R.string.help_quick_step1_title),
                    description = stringResource(R.string.help_quick_step1_desc)
                )
                HelpQuickStepRow(
                    icon = icons.navReview,
                    accentColor = colors.accentPurple,
                    title = stringResource(R.string.help_quick_step2_title),
                    description = stringResource(R.string.help_quick_step2_desc)
                )
                HelpQuickStepRow(
                    icon = icons.play,
                    accentColor = colors.success,
                    title = stringResource(R.string.help_quick_step3_title),
                    description = stringResource(R.string.help_quick_step3_desc)
                )
            }
        }

        // ۲. مقایسه تخصصی ۳ الگوریتم زمان‌بندی مرور (یادین، SM-2 و FSRS)
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.help_algos_header_title),
                    subtitle = stringResource(R.string.help_algos_header_subtitle),
                    icon = icons.retentionBrain,
                    iconTint = colors.accentPurple
                )

                HelpAlgorithmDetailBox(
                    icon = icons.leitnerBox,
                    accentColor = colors.primary,
                    badgeText = stringResource(R.string.help_algo_yadin_badge),
                    title = stringResource(R.string.help_algo_yadin_title),
                    description = stringResource(R.string.help_algo_yadin_desc)
                )

                HelpAlgorithmDetailBox(
                    icon = icons.retentionBrain,
                    accentColor = colors.accentPurple,
                    badgeText = stringResource(R.string.help_algo_sm2_badge),
                    title = stringResource(R.string.help_algo_sm2_title),
                    description = stringResource(R.string.help_algo_sm2_desc)
                )

                HelpAlgorithmDetailBox(
                    icon = icons.brandSparkle,
                    accentColor = colors.secondary,
                    badgeText = stringResource(R.string.help_algo_fsrs_badge),
                    title = stringResource(R.string.help_algo_fsrs_title),
                    description = stringResource(R.string.help_algo_fsrs_desc)
                )
            }
        }

        // ۳. نحوه کار صف هوشمند: اولویت کلمات قبلی، ورود کلمات جدید و زمان «یادگرفته شدن»
        YadinCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                YadinSectionHeader(
                    title = stringResource(R.string.help_smart_queue_header_title),
                    subtitle = stringResource(R.string.help_smart_queue_header_subtitle),
                    icon = icons.dueSchedule,
                    iconTint = colors.secondary
                )

                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

                HelpQuickStepRow(
                    icon = icons.dueSchedule,
                    accentColor = colors.warning,
                    title = stringResource(R.string.help_queue_item1_title),
                    description = stringResource(R.string.help_queue_item1_desc)
                )
                HelpQuickStepRow(
                    icon = icons.add,
                    accentColor = colors.primary,
                    title = stringResource(R.string.help_queue_item2_title),
                    description = stringResource(R.string.help_queue_item2_desc)
                )
                HelpQuickStepRow(
                    icon = icons.dueClear,
                    accentColor = colors.accentPurple,
                    title = stringResource(R.string.help_queue_item3_title),
                    description = stringResource(R.string.help_queue_item3_desc)
                )
                HelpQuickStepRow(
                    icon = icons.stageLearned,
                    accentColor = colors.success,
                    title = stringResource(R.string.help_queue_item4_title),
                    description = stringResource(R.string.help_queue_item4_desc)
                )
            }
        }

        // ۴. راهنمای تفصیلی بخش‌ها و امکانات برنامه
        HelpStepCard(
            stepNumber = stringResource(R.string.help_step1_number),
            icon = icons.modeQuiz,
            accentColor = colors.primary,
            title = stringResource(R.string.help_step1_title),
            description = stringResource(R.string.help_step1_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step2_number),
            icon = icons.difficultySpectrum,
            accentColor = colors.warning,
            title = stringResource(R.string.help_step2_title),
            description = stringResource(R.string.help_step2_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step3_number),
            icon = icons.languageDirection,
            accentColor = colors.accentPurple,
            title = stringResource(R.string.help_step3_title),
            description = stringResource(R.string.help_step3_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step4_number),
            icon = icons.navLibrary,
            accentColor = colors.secondary,
            title = stringResource(R.string.help_step4_title),
            description = stringResource(R.string.help_step4_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step5_number),
            icon = icons.navProgress,
            accentColor = colors.success,
            title = stringResource(R.string.help_step5_title),
            description = stringResource(R.string.help_step5_desc)
        )

        HelpStepCard(
            stepNumber = stringResource(R.string.help_step6_number),
            icon = icons.backupExport,
            accentColor = colors.primary,
            title = stringResource(R.string.help_step6_title),
            description = stringResource(R.string.help_step6_desc)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HelpQuickStepRow(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    description: String
) {
    val colors = LocalYadinColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.06f))
            .border(1.dp, accentColor.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun HelpAlgorithmDetailBox(
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    title: String,
    description: String
) {
    val colors = LocalYadinColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (colors.cardBorderBrush != null) {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            accentColor.copy(alpha = 0.15f),
                            colors.surface.copy(alpha = 0.96f),
                            colors.accentPurple.copy(alpha = 0.06f)
                        )
                    )
                } else accentColor.copy(alpha = 0.07f)
            )
            .border(
                if (colors.cardBorderBrush != null) 1.2.dp else 1.dp,
                if (colors.cardBorderBrush != null) {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(accentColor.copy(alpha = 0.4f), colors.outlineVariant, colors.accentPurple.copy(alpha = 0.28f))
                    )
                } else androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.32f)),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            lineHeight = 21.sp
        )
    }
}

@Composable
fun HelpStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector? = null,
    accentColor: Color? = null
) {
    val colors = LocalYadinColors.current
    val resolvedAccent = accentColor ?: colors.primary

    YadinCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(resolvedAccent.copy(alpha = 0.14f))
                    .border(1.dp, resolvedAccent.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = resolvedAccent,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = stepNumber,
                        color = resolvedAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
