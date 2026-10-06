package com.manidigit.yadin.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.domain.time.ClockAndDayMath
import com.manidigit.yadin.ui.theme.LocalYadinColors
import java.time.DayOfWeek
import java.time.LocalDate

enum class ChartTimeframe(val title: String) {
    WEEKLY("هفتگی"),
    MONTHLY("ماهانه")
}

@Composable
fun DailyReviewChart(
    dailyStats: List<DayCountRaw>,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current
    var selectedTimeframe by remember { mutableStateOf(ChartTimeframe.WEEKLY) }

    val today = ClockAndDayMath.todayDayString()
    val statMap = dailyStats.associateBy { it.reviewedDay }

    val chartItems = if (selectedTimeframe == ChartTimeframe.WEEKLY) {
        // 7 days: from (today - 6) up to today
        (6 downTo 0).map { offset ->
            val day = ClockAndDayMath.addDays(today, -offset)
            val raw = statMap[day]
            val persianDayName = getPersianDayOfWeek(day)
            DayChartModel(
                dayString = day,
                shortLabel = persianDayName,
                totalCount = raw?.totalCount ?: 0,
                correctCount = raw?.correctCount ?: 0,
                isToday = (day == today)
            )
        }
    } else {
        // 30 days: aggregated into 6 periods of 5 days
        (5 downTo 0).map { sliceIndex ->
            val sliceEndOffset = sliceIndex * 5
            val sliceStartOffset = sliceEndOffset + 4
            val endDay = ClockAndDayMath.addDays(today, -sliceEndOffset)
            
            var sliceTotal = 0
            var sliceCorrect = 0
            for (off in sliceStartOffset downTo sliceEndOffset) {
                val d = ClockAndDayMath.addDays(today, -off)
                statMap[d]?.let {
                    sliceTotal += it.totalCount
                    sliceCorrect += it.correctCount
                }
            }

            val label = if (sliceIndex == 0) "۵ روز اخیر" else "${sliceEndOffset + 1}-${sliceStartOffset + 1} روز پیش"
            DayChartModel(
                dayString = endDay,
                shortLabel = label,
                totalCount = sliceTotal,
                correctCount = sliceCorrect,
                isToday = (sliceIndex == 0)
            )
        }
    }

    val maxVal = chartItems.maxOfOrNull { it.totalCount } ?: 0
    val maxCount = when {
        maxVal <= 5 -> 10
        maxVal <= 10 -> 15
        maxVal <= 20 -> 25
        maxVal <= 50 -> 60
        else -> ((maxVal + 9) / 10) * 10
    }

    val totalReviews = chartItems.sumOf { it.totalCount }

    YadinCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Timeframe Selector (هفتگی / ماهانه)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "حجم مرور و تمرین واژگان",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "مجموع: $totalReviews واژه در دوره انتخاب‌شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                // Segmented Toggle for Weekly vs Monthly
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartTimeframe.values().forEach { tf ->
                        val isSelected = (selectedTimeframe == tf)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) colors.primary else Color.Transparent)
                                .clickable { selectedTimeframe = tf }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.onPrimary else colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Custom Canvas Chart with Y-Axis and X-Axis
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                val primaryColor = colors.primary
                val secondaryColor = colors.secondary
                val surfaceVariantColor = colors.surfaceVariant
                val textColorPrimary = colors.onSurface.toArgb()
                val textColorVariant = colors.onSurfaceVariant.toArgb()

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    val yAxisWidth = 34.dp.toPx()
                    val bottomLabelSpace = 24.dp.toPx()
                    val chartHeight = canvasHeight - bottomLabelSpace
                    val chartWidth = canvasWidth - yAxisWidth

                    // Draw Y-Axis lines and numbers (0, mid, max)
                    val steps = 3
                    for (i in 0..steps) {
                        val frac = i.toFloat() / steps
                        val y = chartHeight - (frac * (chartHeight * 0.85f))
                        val value = (frac * maxCount).toInt()

                        // Grid line
                        drawLine(
                            color = surfaceVariantColor.copy(alpha = if (i == 0) 0.8f else 0.4f),
                            start = Offset(yAxisWidth, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = if (i == 0) 1.5f else 1f
                        )

                        // Y-axis value label
                        drawContext.canvas.nativeCanvas.apply {
                            val axisPaint = android.graphics.Paint().apply {
                                color = textColorVariant
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.LEFT
                                isAntiAlias = true
                            }
                            drawText("$value", 4.dp.toPx(), y + 3.dp.toPx(), axisPaint)
                        }
                    }

                    val barCount = chartItems.size
                    val slotWidth = chartWidth / barCount
                    val barWidth = (slotWidth * 0.45f).coerceIn(12.dp.toPx(), 28.dp.toPx())

                    chartItems.forEachIndexed { index, item ->
                        val centerX = yAxisWidth + (index * slotWidth) + (slotWidth / 2f)
                        val barHeight = if (item.totalCount > 0) {
                            (item.totalCount.toFloat() / maxCount) * (chartHeight * 0.85f)
                        } else {
                            3.dp.toPx() // Minimum indicator
                        }

                        val barTop = chartHeight - barHeight
                        val barLeft = centerX - (barWidth / 2f)

                        val barColor = if (item.isToday) {
                            primaryColor
                        } else if (item.totalCount > 0) {
                            secondaryColor.copy(alpha = 0.85f)
                        } else {
                            surfaceVariantColor.copy(alpha = 0.6f)
                        }

                        // Draw Bar
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Draw value above bar if > 0
                        if (item.totalCount > 0) {
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = if (item.isToday) textColorPrimary else textColorVariant
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                    isFakeBoldText = item.isToday
                                }
                                drawText("${item.totalCount}", centerX, barTop - 5.dp.toPx(), paint)
                            }
                        }

                        // Draw X-axis Day label
                        drawContext.canvas.nativeCanvas.apply {
                            val labelPaint = android.graphics.Paint().apply {
                                color = if (item.isToday) textColorPrimary else textColorVariant
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = item.isToday
                            }
                            drawText(item.shortLabel, centerX, canvasHeight - 3.dp.toPx(), labelPaint)
                        }
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.primary))
                    Text("امروز / دوره جاری", fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.secondary))
                    Text("روزهای قبل", fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

private fun getPersianDayOfWeek(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> "شنبه"
            DayOfWeek.SUNDAY -> "یکشنبه"
            DayOfWeek.MONDAY -> "دوشنبه"
            DayOfWeek.TUESDAY -> "سه‌شنبه"
            DayOfWeek.WEDNESDAY -> "چهارشنبه"
            DayOfWeek.THURSDAY -> "پنج‌شنبه"
            DayOfWeek.FRIDAY -> "جمعه"
            else -> dateString.takeLast(5)
        }
    } catch (_: Exception) {
        dateString.takeLast(5)
    }
}

data class DayChartModel(
    val dayString: String,
    val shortLabel: String,
    val totalCount: Int,
    val correctCount: Int,
    val isToday: Boolean
)

