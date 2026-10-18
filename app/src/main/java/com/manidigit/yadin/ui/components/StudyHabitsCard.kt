package com.manidigit.yadin.ui.components

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.StudyHourBucket
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.util.formatDigits

/**
 * کارت تحلیل عادات مطالعه و ساعات طلایی تمرکز کاربر
 */
@Composable
fun StudyHabitsCard(
    buckets: List<StudyHourBucket>,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current

    val totalReviews = buckets.sumOf { it.reviewCount }
    val maxBucketCount = buckets.maxOfOrNull { it.reviewCount }?.coerceAtLeast(1) ?: 1

    // پیدا کردن بازه با بالاترین دقت که حداقل ۳ مرور در آن ثبت شده باشد
    val bestSlotBucket = buckets
        .filter { it.reviewCount >= 3 }
        .maxByOrNull { it.accuracyPercent }

    YadinCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            YadinSectionHeader(
                title = stringResource(R.string.habits_title),
                subtitle = stringResource(R.string.habits_subtitle),
                icon = YadinIcons.studyHabitsTime,
                iconTint = colors.warning
            )

            // بنر ساعت طلایی (در صورت وجود داده کافی)
            if (bestSlotBucket != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.warning.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = YadinIcons.goldenHourStar,
                            contentDescription = null,
                            tint = colors.warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(
                                R.string.habits_best_slot_fmt,
                                stringResource(bestSlotBucket.slot.titleRes),
                                bestSlotBucket.accuracyPercent.formatDigits()
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.warning,
                            fontSize = 12.sp
                        )
                    }
                }
            } else if (totalReviews == 0) {
                Text(
                    text = stringResource(R.string.habits_no_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // سطرهای توزیع بازه‌های زمانی
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                buckets.forEach { bucket ->
                    val fraction = if (maxBucketCount > 0) bucket.reviewCount.toFloat() / maxBucketCount else 0f
                    val isPeak = bucket == bestSlotBucket

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(bucket.slot.titleRes),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPeak) colors.primary else colors.onSurface,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "(${stringResource(bucket.slot.timeRangeRes)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bucket.reviewCount.formatDigits()} مرور",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                if (bucket.reviewCount > 0) {
                                    Text(
                                        text = "${bucket.accuracyPercent.formatDigits()}% دقت",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bucket.accuracyPercent >= 80) colors.success else colors.primary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        LinearProgressIndicator(
                            progress = { fraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isPeak) colors.primary else colors.primary.copy(alpha = 0.6f),
                            trackColor = colors.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
