package com.manidigit.yadin.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.domain.time.ClockAndDayMath
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun DailyReviewChart(
    dailyStats: List<DayCountRaw>,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    // Ensure we have at least 7 days represented (today down to today-6)
    val today = ClockAndDayMath.todayDayString()
    val past7Days = (6 downTo 0).map { ClockAndDayMath.addDays(today, -it) }
    val statMap = dailyStats.associateBy { it.reviewedDay }

    val chartItems = past7Days.map { day ->
        val raw = statMap[day]
        DayChartModel(
            dayString = day,
            shortLabel = day.takeLast(5).replace("-", "/"), // e.g. 10/05
            totalCount = raw?.totalCount ?: 0,
            correctCount = raw?.correctCount ?: 0,
            isToday = (day == today)
        )
    }

    val maxCount = (chartItems.maxOfOrNull { it.totalCount } ?: 1).coerceAtLeast(10)
    val totalReviewsInWeek = chartItems.sumOf { it.totalCount }

    YadinCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "نمودار حجم مرور و تمرین واژگان (۷ روز اخیر)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = "مجموع: $totalReviewsInWeek کلمه تمرین‌شده در این هفته",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimensions.cornerPill))
                        .background(colors.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "امروز: ${chartItems.lastOrNull()?.totalCount ?: 0}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
            }

            // Custom Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .padding(top = 10.dp, bottom = 4.dp)
            ) {
                val primaryColor = colors.primary
                val secondaryColor = colors.secondary
                val surfaceVariantColor = colors.surfaceVariant
                val onSurfaceVariantColor = colors.onSurfaceVariant

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomLabelSpace = 24.dp.toPx()
                    val chartHeight = canvasHeight - bottomLabelSpace

                    val barCount = chartItems.size
                    val slotWidth = canvasWidth / barCount
                    val barWidth = slotWidth * 0.48f

                    // Draw baseline
                    drawLine(
                        color = surfaceVariantColor,
                        start = Offset(0f, chartHeight),
                        end = Offset(canvasWidth, chartHeight),
                        strokeWidth = 2f
                    )

                    chartItems.forEachIndexed { index, item ->
                        val centerX = (index * slotWidth) + (slotWidth / 2f)
                        val barHeight = if (item.totalCount > 0) {
                            (item.totalCount.toFloat() / maxCount) * (chartHeight * 0.82f)
                        } else {
                            4.dp.toPx() // Minimum dot for zero
                        }

                        val barTop = chartHeight - barHeight
                        val barLeft = centerX - (barWidth / 2f)

                        val barColor = if (item.isToday) {
                            primaryColor
                        } else if (item.totalCount > 0) {
                            secondaryColor.copy(alpha = 0.85f)
                        } else {
                            surfaceVariantColor
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
                                    color = if (item.isToday) android.graphics.Color.WHITE else android.graphics.Color.GRAY
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                    isFakeBoldText = item.isToday
                                }
                                drawText("${item.totalCount}", centerX, barTop - 6.dp.toPx(), paint)
                            }
                        }

                        // Draw X-axis Day label
                        drawContext.canvas.nativeCanvas.apply {
                            val labelPaint = android.graphics.Paint().apply {
                                color = if (item.isToday) android.graphics.Color.WHITE else android.graphics.Color.GRAY
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = item.isToday
                            }
                            drawText(item.shortLabel, centerX, canvasHeight - 2.dp.toPx(), labelPaint)
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
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.primary))
                    Text("امروز", fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(colors.secondary))
                    Text("روزهای گذشته", fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

data class DayChartModel(
    val dayString: String,
    val shortLabel: String,
    val totalCount: Int,
    val correctCount: Int,
    val isToday: Boolean
)
