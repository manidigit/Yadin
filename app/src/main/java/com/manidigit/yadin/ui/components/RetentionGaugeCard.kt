package com.manidigit.yadin.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.RetentionMetrics
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.util.formatDigits

/**
 * کارت گیج تحلیلی ماندگاری حافظه و هرم تسلط جعبه لایتنر
 */
@Composable
fun RetentionGaugeCard(
    metrics: RetentionMetrics,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val animatedScore by animateFloatAsState(
        targetValue = metrics.retentionScore.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "retention_score"
    )

    val (statusTextRes, statusColor) = when {
        metrics.retentionScore >= 75 -> R.string.retention_level_high to colors.success
        metrics.retentionScore >= 45 -> R.string.retention_level_medium to colors.info
        else -> R.string.retention_level_growing to colors.warning
    }

    YadinCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            YadinSectionHeader(
                title = stringResource(R.string.retention_gauge_title),
                subtitle = stringResource(R.string.retention_gauge_desc),
                icon = YadinIcons.retentionBrain,
                iconTint = statusColor
            )

            // بخش گیج دایره‌ای و وضعیت
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // گیج دایره‌ای مدرن (۳۶۰ درجه کامل با حلقه پس‌زمینه کمرنگ)
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = statusColor.copy(alpha = 0.22f)
                    val arcColor = statusColor

                    Canvas(modifier = Modifier.size(96.dp)) {
                        val strokeWidth = 10.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                        // حلقه کامل پس‌زمینه (Track Circle - 360 درجه با رنگ کمرنگ جهت مشخص بودن کامل دایره)
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // کمان پرشده پیشرفت (Progress Arc - شروع از بالای دایره در جهت عقربه‌های ساعت)
                        val progressSweep = (animatedScore / 100f).coerceIn(0f, 1f) * 360f
                        if (progressSweep > 0f) {
                            drawArc(
                                color = arcColor,
                                startAngle = -90f,
                                sweepAngle = progressSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${metrics.retentionScore.formatDigits()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.onSurface
                        )
                    }
                }

                // بج وضعیت و توضیح
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = YadinIcons.stageLearned,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(statusTextRes),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Text(
                        text = stringResource(
                            R.string.retention_active_words_fmt,
                            metrics.practicedWords.formatDigits(),
                            metrics.totalWords.formatDigits()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ردیف تفکیک وضعیت کلمات در مراحل لایتنر
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RetentionStageRow(
                    label = stringResource(R.string.retention_stat_mastered),
                    count = metrics.learnedWords,
                    total = metrics.totalWords,
                    color = colors.success
                )
                RetentionStageRow(
                    label = stringResource(R.string.retention_stat_in_progress),
                    count = metrics.monthlyWords + metrics.weeklyWords,
                    total = metrics.totalWords,
                    color = colors.accentPurple
                )
                RetentionStageRow(
                    label = stringResource(R.string.retention_stat_fresh),
                    count = metrics.dailyWords,
                    total = metrics.totalWords,
                    color = colors.primary
                )
                if (metrics.unreviewedWords > 0) {
                    RetentionStageRow(
                        label = stringResource(R.string.retention_stat_unstarted),
                        count = metrics.unreviewedWords,
                        total = metrics.totalWords,
                        color = colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RetentionStageRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val colors = LocalYadinColors.current
    val pct = if (total > 0) ((count.toFloat() / total) * 100).toInt() else 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface,
                fontSize = 12.sp
            )
        }

        Text(
            text = "${count.formatDigits()} (${pct.formatDigits()}%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}
