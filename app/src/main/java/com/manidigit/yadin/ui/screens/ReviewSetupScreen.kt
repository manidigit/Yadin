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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun ReviewSetupScreen(
    initialType: ReviewType = ReviewType.DAILY,
    onStart: (ReviewType, ReviewMode, CardDirection, QuizLevel, Int) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var selectedType by remember { mutableStateOf(initialType) }
    var selectedMode by remember { mutableStateOf(ReviewMode.FLASHCARD) }
    var selectedDirection by remember { mutableStateOf(CardDirection.NORMAL) }
    var selectedQuizLevel by remember { mutableStateOf(QuizLevel.MEDIUM) }
    var selectedLimit by remember { mutableStateOf(20) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = colors.onSurface
                        )
                    }
                    Text(
                        text = "تنظیمات جلسه مرور",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
            }

            // Review Type
            item {
                Text(
                    text = "نوع مرور و صف کلمات:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val types = listOf(
                        ReviewType.DAILY to "واژگان سررسید روزانه",
                        ReviewType.WEEKLY to "کلمات مرحله هفتگی",
                        ReviewType.MONTHLY to "کلمات مرحله ماهانه",
                        ReviewType.LEARNED to "کلمات یادگرفته‌شده (تثبیت)",
                        ReviewType.RANDOM to "مرور تصادفی از تمام بانک"
                    )
                    types.forEach { (type, label) ->
                        val isSelected = selectedType == type
                        OptionRow(
                            title = label,
                            isSelected = isSelected,
                            onClick = { selectedType = type }
                        )
                    }
                }
            }

            // Mode: Flashcard vs Quiz
            item {
                Text(
                    text = "حالت تمرین:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeCard(
                        title = "فلش‌کارت",
                        subtitle = "مرور تعاملی با چرخش کارت",
                        icon = Icons.Default.Style,
                        isSelected = selectedMode == ReviewMode.FLASHCARD,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMode = ReviewMode.FLASHCARD }
                    )
                    ModeCard(
                        title = "آزمون",
                        subtitle = "انتخاب از ۴ گزینه هوشمند",
                        icon = Icons.Default.Quiz,
                        isSelected = selectedMode == ReviewMode.QUIZ,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMode = ReviewMode.QUIZ }
                    )
                }
            }

            // Direction
            item {
                Text(
                    text = "جهت کارت:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OptionPill(
                        label = "اسپانیایی → فارسی",
                        isSelected = selectedDirection == CardDirection.NORMAL,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDirection = CardDirection.NORMAL }
                    )
                    OptionPill(
                        label = "فارسی → اسپانیایی (معکوس)",
                        isSelected = selectedDirection == CardDirection.REVERSE,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedDirection = CardDirection.REVERSE }
                    )
                }
            }

            // Number of cards limit
            item {
                Text(
                    text = "تعداد واژگان در این جلسه:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 20, 30, 50).forEach { limit ->
                        OptionPill(
                            label = "$limit واژه",
                            isSelected = selectedLimit == limit,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedLimit = limit }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                onStart(selectedType, selectedMode, selectedDirection, selectedQuizLevel, selectedLimit)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(dimensions.cornerMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text("شروع جلسه مرور", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun OptionRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.primary else colors.onSurface
        )
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerMedium))
            .background(if (isSelected) colors.secondary.copy(alpha = 0.12f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.secondary else colors.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(dimensions.cornerMedium)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.secondary else colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) colors.secondary else colors.onSurface
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

@Composable
fun OptionPill(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(dimensions.cornerSmall))
            .background(if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(dimensions.cornerSmall)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colors.primary else colors.onSurface,
            fontSize = 12.sp
        )
    }
}
