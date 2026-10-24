package com.manidigit.yadin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.ReviewForecastItem
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.util.formatDigits

/**
 * کارت پیش‌بینی موعد مرورهای آینده در جعبه لایتنر
 */
@Composable
fun UpcomingForecastCard(
    forecastItems: List<ReviewForecastItem>,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current

    val tomorrowCount = forecastItems.firstOrNull { it.dayOffset == 1 }?.dueCount ?: 0
    val next3DaysCount = forecastItems.firstOrNull { it.dayOffset == 3 }?.dueCount ?: 0
    val next7DaysCount = forecastItems.firstOrNull { it.dayOffset == 7 }?.dueCount ?: 0

    YadinCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            YadinSectionHeader(
                title = stringResource(R.string.forecast_title),
                subtitle = stringResource(R.string.forecast_subtitle),
                icon = YadinIcons.forecastCalendar,
                iconTint = colors.primary
            )

            // سه ستون پیش‌بینی
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ForecastColumn(
                    title = stringResource(R.string.forecast_tomorrow),
                    count = tomorrowCount,
                    accentColor = colors.primary,
                    modifier = Modifier.weight(1f)
                )

                ForecastColumn(
                    title = stringResource(R.string.forecast_next_3_days),
                    count = next3DaysCount,
                    accentColor = colors.accentPurple,
                    modifier = Modifier.weight(1f)
                )

                ForecastColumn(
                    title = stringResource(R.string.forecast_next_7_days),
                    count = next7DaysCount,
                    accentColor = colors.warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ForecastColumn(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant.copy(alpha = 0.5f))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = count.formatDigits(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = if (count > 0) accentColor else colors.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (count > 0) YadinIcons.dueSchedule else YadinIcons.dueClear,
                    contentDescription = null,
                    tint = if (count > 0) accentColor else colors.success,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = stringResource(if (count > 0) R.string.forecast_due_words else R.string.forecast_all_clear),
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
