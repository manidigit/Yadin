package com.manidigit.yadin.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.domain.time.ClockAndDayMath
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.util.formatDigits
import java.time.DayOfWeek
import java.time.LocalDate

enum class ChartTimeframe(@StringRes val titleRes: Int) {
    WEEKLY(R.string.chart_filter_weekly),
    MONTHLY(R.string.chart_filter_monthly),
    QUARTERLY(R.string.chart_filter_quarterly)
}

/**
 * نمودار تحلیلی تعاملی فعالیت مرور با ستون‌های کپسولی دو‌بخشی (حجم کل مرور و سهم پاسخ‌های صحیح)
 * همراه با قابلیت لمس تعاملی برای نمایش جزئیات روز یا بازه انتخابی.
 */
@Composable
fun DailyReviewChart(
    dailyStats: List<DayCountRaw>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalYadinColors.current
    var selectedTimeframe by remember { mutableStateOf(ChartTimeframe.WEEKLY) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val today = ClockAndDayMath.todayDayString()
    val statMap = remember(dailyStats) { dailyStats.associateBy { it.reviewedDay } }

    val recentDaysLabel = stringResource(R.string.chart_recent_days)

    val chartItems = remember(selectedTimeframe, statMap, today) {
        when (selectedTimeframe) {
            ChartTimeframe.WEEKLY -> {
                // 7 روز اخیر
                (6 downTo 0).map { offset ->
                    val day = ClockAndDayMath.addDays(today, -offset)
                    val raw = statMap[day]
                    val dayName = getLocalizedDayOfWeek(day, context)
                    DayChartModel(
                        dayString = day,
                        shortLabel = dayName,
                        totalCount = raw?.totalCount ?: 0,
                        correctCount = raw?.correctCount ?: 0,
                        isToday = (day == today)
                    )
                }
            }
            ChartTimeframe.MONTHLY -> {
                // 30 روز: در 6 بازه 5 روزه
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

                    val label = if (sliceIndex == 0) {
                        recentDaysLabel
                    } else {
                        context.getString(
                            R.string.chart_days_ago,
                            (sliceEndOffset + 1).formatDigits(),
                            (sliceStartOffset + 1).formatDigits()
                        )
                    }
                    DayChartModel(
                        dayString = endDay,
                        shortLabel = label,
                        totalCount = sliceTotal,
                        correctCount = sliceCorrect,
                        isToday = (sliceIndex == 0)
                    )
                }
            }
            ChartTimeframe.QUARTERLY -> {
                // 90 روز: در 6 بازه 15 روزه
                (5 downTo 0).map { sliceIndex ->
                    val sliceEndOffset = sliceIndex * 15
                    val sliceStartOffset = sliceEndOffset + 14
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

                    val label = if (sliceIndex == 0) {
                        recentDaysLabel
                    } else {
                        context.getString(
                            R.string.chart_days_ago,
                            (sliceEndOffset + 1).formatDigits(),
                            (sliceStartOffset + 1).formatDigits()
                        )
                    }
                    DayChartModel(
                        dayString = endDay,
                        shortLabel = label,
                        totalCount = sliceTotal,
                        correctCount = sliceCorrect,
                        isToday = (sliceIndex == 0)
                    )
                }
            }
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // سطر هدر با عنوان و سوئیچ بازه زمانی استاندارد
            YadinSectionHeader(
                title = stringResource(R.string.chart_title),
                subtitle = stringResource(R.string.chart_total_reviews, totalReviews.formatDigits()),
                icon = YadinIcons.chartTrend,
                iconTint = colors.primary,
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ChartTimeframe.entries.forEach { tf ->
                            val isSelected = (selectedTimeframe == tf)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (colors.cardBorderBrush != null) {
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(
                                                    colors.primary.copy(alpha = if (isSelected) 0.24f else 0.10f),
                                                    colors.accentPurple.copy(alpha = if (isSelected) 0.18f else 0.05f),
                                                    colors.surfaceVariant.copy(alpha = if (isSelected) 0.72f else 0.42f)
                                                )
                                            )
                                        } else if (isSelected) colors.primary.copy(alpha = 0.18f)
                                        else colors.primary.copy(alpha = 0.06f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        brush = if (colors.cardBorderBrush != null) {
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(
                                                    colors.primary.copy(alpha = if (isSelected) 0.72f else 0.38f),
                                                    colors.accentPurple.copy(alpha = if (isSelected) 0.58f else 0.24f)
                                                )
                                            )
                                        } else androidx.compose.ui.graphics.SolidColor(
                                            if (isSelected) colors.primary else colors.primary.copy(alpha = 0.28f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedTimeframe = tf
                                        selectedIndex = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(tf.titleRes),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.primary else colors.onSurface
                                )
                            }
                        }
                    }
                }
            )

            // بنر شناور تول‌تیپ تعاملی هنگام لمس هر ستون
            AnimatedVisibility(
                visible = selectedIndex != null && selectedIndex in chartItems.indices,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val item = selectedIndex?.let { chartItems.getOrNull(it) }
                if (item != null) {
                    val acc = if (item.totalCount > 0) ((item.correctCount.toFloat() / item.totalCount) * 100).toInt() else 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = YadinIcons.aboutInfo,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(
                                    R.string.chart_tooltip_fmt,
                                    item.shortLabel,
                                    item.totalCount.formatDigits(),
                                    item.correctCount.formatDigits(),
                                    acc.formatDigits()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // رسم بوم Canvas با ستون‌های کپسولی دو‌بخشی (کل مرورها و سهم پاسخ صحیح)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                val primaryColor = colors.primary
                val secondaryColor = colors.secondary
                val surfaceVariantColor = colors.surfaceVariant
                val textColorPrimary = colors.onSurface.toArgb()
                val textColorVariant = colors.onSurfaceVariant.toArgb()

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(chartItems) {
                            detectTapGestures { tapOffset ->
                                val yAxisWidth = 34.dp.toPx()
                                val chartWidth = size.width - yAxisWidth
                                val barCount = chartItems.size
                                val slotWidth = chartWidth / barCount
                                if (tapOffset.x >= yAxisWidth) {
                                    val idx = ((tapOffset.x - yAxisWidth) / slotWidth).toInt().coerceIn(0, barCount - 1)
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val yAxisWidth = 34.dp.toPx()
                    val bottomLabelSpace = 24.dp.toPx()
                    val chartHeight = canvasHeight - bottomLabelSpace
                    val chartWidth = canvasWidth - yAxisWidth

                    // خطوط شبکه محور Y
                    val steps = 3
                    for (i in 0..steps) {
                        val frac = i.toFloat() / steps
                        val y = chartHeight - (frac * (chartHeight * 0.85f))
                        val value = (frac * maxCount).toInt()

                        drawLine(
                            color = surfaceVariantColor.copy(alpha = if (i == 0) 0.8f else 0.35f),
                            start = Offset(yAxisWidth, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = if (i == 0) 1.5f else 1f
                        )

                        drawContext.canvas.nativeCanvas.apply {
                            val axisPaint = android.graphics.Paint().apply {
                                color = textColorVariant
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.LEFT
                                isAntiAlias = true
                            }
                            drawText(value.formatDigits(), 4.dp.toPx(), y + 3.dp.toPx(), axisPaint)
                        }
                    }

                    val barCount = chartItems.size
                    val slotWidth = chartWidth / barCount
                    val barWidth = (slotWidth * 0.45f).coerceIn(10.dp.toPx(), 26.dp.toPx())
                    val radius = barWidth / 2f

                    // رسم ستون‌های میله‌ای دو‌بخشی
                    chartItems.forEachIndexed { index, item ->
                        val centerX = yAxisWidth + (index * slotWidth) + (slotWidth / 2f)
                        val barLeft = centerX - radius
                        val isHighlighted = selectedIndex == index

                        if (item.totalCount > 0) {
                            val totalBarHeight = ((item.totalCount.toFloat() / maxCount) * (chartHeight * 0.85f))
                                .coerceAtLeast(6.dp.toPx())
                            val barTop = chartHeight - totalBarHeight

                            val baseTone = when {
                                isHighlighted -> primaryColor
                                item.isToday -> primaryColor
                                else -> secondaryColor
                            }

                            val capsulePath = Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        rect = Rect(
                                            left = barLeft,
                                            top = barTop,
                                            right = barLeft + barWidth,
                                            bottom = chartHeight
                                        ),
                                        cornerRadius = CornerRadius(radius, radius)
                                    )
                                )
                            }

                            // ۱. پس‌زمینه کمرنگ ستون (نشان‌دهنده کل مرورها / سهم پاسخ‌های نادرست)
                            drawPath(
                                path = capsulePath,
                                color = baseTone.copy(alpha = if (isHighlighted || item.isToday) 0.24f else 0.20f)
                            )

                            // ۲. بخش پررنگ پایینی ستون (نشان‌دهنده سهم پاسخ‌های صحیح برش‌خورده درون کپسول)
                            val correctRatio = (item.correctCount.toFloat() / item.totalCount).coerceIn(0f, 1f)
                            if (correctRatio > 0f) {
                                val correctHeight = totalBarHeight * correctRatio
                                val correctTop = chartHeight - correctHeight
                                clipPath(capsulePath) {
                                    drawRect(
                                        color = if (isHighlighted || item.isToday) baseTone else baseTone.copy(alpha = 0.90f),
                                        topLeft = Offset(barLeft, correctTop),
                                        size = Size(barWidth, correctHeight)
                                    )
                                }
                            }

                            // ۳. کادر ظریف دور کل کپسول برای تفکیک واضح سقف مرورها
                            drawPath(
                                path = capsulePath,
                                color = baseTone.copy(alpha = if (isHighlighted) 0.85f else 0.40f),
                                style = Stroke(width = if (isHighlighted) 1.5.dp.toPx() else 1.dp.toPx())
                            )

                            // عدد کل مرور روی ستون
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = if (item.isToday || isHighlighted) textColorPrimary else textColorVariant
                                    textSize = 10.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                    isFakeBoldText = (item.isToday || isHighlighted)
                                }
                                drawText(item.totalCount.formatDigits(), centerX, barTop - 5.dp.toPx(), paint)
                            }
                        } else {
                            // روزهای بدون مرور: نشانگر کوتاه خاکستری در کف محور
                            val emptyHeight = 3.dp.toPx()
                            val barTop = chartHeight - emptyHeight
                            drawRoundRect(
                                color = surfaceVariantColor.copy(alpha = 0.5f),
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, emptyHeight),
                                cornerRadius = CornerRadius(radius, radius)
                            )
                        }

                        // برچسب محور افقی (روز / بازه)
                        drawContext.canvas.nativeCanvas.apply {
                            val labelPaint = android.graphics.Paint().apply {
                                color = if (item.isToday || isHighlighted) textColorPrimary else textColorVariant
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                isFakeBoldText = (item.isToday || isHighlighted)
                            }
                            drawText(item.shortLabel, centerX, canvasHeight - 3.dp.toPx(), labelPaint)
                        }
                    }
                }
            }

            // راهنما (Legend): بخش پررنگ (پاسخ‌های صحیح) و بخش کمرنگ کادردار (کل مرورها)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                    Text(
                        text = stringResource(R.string.chart_accuracy_curve_label),
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.22f))
                            .border(1.dp, colors.primary.copy(alpha = 0.5f), CircleShape)
                    )
                    Text(
                        text = stringResource(R.string.chart_volume_bars_label),
                        fontSize = 11.sp,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getLocalizedDayOfWeek(dateString: String, context: Context): String {
    return try {
        val date = LocalDate.parse(dateString)
        when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> context.getString(R.string.dow_sat)
            DayOfWeek.SUNDAY -> context.getString(R.string.dow_sun)
            DayOfWeek.MONDAY -> context.getString(R.string.dow_mon)
            DayOfWeek.TUESDAY -> context.getString(R.string.dow_tue)
            DayOfWeek.WEDNESDAY -> context.getString(R.string.dow_wed)
            DayOfWeek.THURSDAY -> context.getString(R.string.dow_thu)
            DayOfWeek.FRIDAY -> context.getString(R.string.dow_fri)
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
