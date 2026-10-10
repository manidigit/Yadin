package com.manidigit.yadin.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.QuizQuestion
import com.manidigit.yadin.ui.components.DifficultyBadge
import com.manidigit.yadin.ui.components.SpeakButton
import com.manidigit.yadin.ui.components.StageBadge
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun QuizScreen(
    questions: List<QuizQuestion>,
    currentIndex: Int,
    selectedOption: Int?,
    userAnswers: Map<Int, Int> = emptyMap(),
    showCategory: Boolean = false,
    onSelectOption: (Int) -> Unit,
    onPreviousQuestion: () -> Unit = {},
    onNextQuestion: () -> Unit,
    onGoToQuestion: (Int) -> Unit = {},
    onExit: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    var showHelpDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Intercept hardware back button to show exit confirmation
    androidx.activity.compose.BackHandler {
        showExitConfirmDialog = true
    }

    if (questions.isEmpty() || currentIndex >= questions.size) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text("سؤالی برای نمایش وجود ندارد", color = colors.onSurface)
        }
        return
    }

    val q = questions[currentIndex]
    val progress = (currentIndex + 1).toFloat() / questions.size
    val hasAnswered = (selectedOption != null)

    val scrollState = rememberScrollState()

    // Auto reset scroll to top on question change, and auto-scroll down on answer selection
    LaunchedEffect(currentIndex) {
        scrollState.scrollTo(0)
    }

    LaunchedEffect(selectedOption) {
        if (selectedOption != null) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding)
    ) {
        // Top Header (Fixed at top)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showExitConfirmDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "خروج از آزمون",
                        tint = colors.onSurface
                    )
                }

                Text(
                    text = "سؤال ${currentIndex + 1} از ${questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StageBadge(stage = q.stage)
                    DifficultyBadge(difficulty = q.difficulty)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.secondary,
                trackColor = colors.surfaceVariant
            )

            // Question review & navigation strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(questions.size) { idx ->
                    val isCurrent = (idx == currentIndex)
                    val answeredOption = userAnswers[idx]
                    val isAnswered = (answeredOption != null)
                    val isCorrect = if (isAnswered) answeredOption == questions[idx].correctIndex else null

                    val bgColor = when {
                        isCurrent -> colors.primary
                        isCorrect == true -> colors.success.copy(alpha = 0.2f)
                        isCorrect == false -> colors.error.copy(alpha = 0.2f)
                        else -> colors.surfaceVariant
                    }
                    val textColor = when {
                        isCurrent -> colors.onPrimary
                        isCorrect == true -> colors.success
                        isCorrect == false -> colors.error
                        else -> colors.onSurfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(
                                width = if (isCurrent) 2.dp else if (isAnswered) 1.dp else 0.dp,
                                color = if (isCurrent) colors.primary else if (isAnswered) textColor.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onGoToQuestion(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollable Question & Options Content (occupies available middle space)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Question Prompt Card
            YadinCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensions.cornerLarge),
                backgroundColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Optional Category Badge
                        if (showCategory && q.categoryName != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = q.categoryName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.onSurfaceVariant
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(1.dp))
                        }

                        // Action Controls: Speech + Help/Note Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Help / Note Button
                            IconButton(
                                onClick = { showHelpDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "راهنما و یادداشت واژه",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            val promptLang = if (q.direction == CardDirection.NORMAL) "es" else "fa"
                            SpeakButton(text = q.promptText, languageCode = promptLang)
                        }
                    }

                    // Responsive typography based on text length
                    val promptStyle = when {
                        q.promptText.length > 90 -> MaterialTheme.typography.titleMedium
                        q.promptText.length > 45 -> MaterialTheme.typography.titleLarge
                        q.promptText.length > 25 -> MaterialTheme.typography.headlineSmall
                        else -> MaterialTheme.typography.headlineMedium
                    }

                    Text(
                        text = q.promptText,
                        style = promptStyle,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "ترجمه مناسب کدام است؟",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }

            // 4 Options Grid/List
            val correctOptionText = q.options.getOrNull(q.correctIndex)?.text ?: q.correctAnswer
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                q.options.forEachIndexed { index, option ->
                    val isSelected = (selectedOption == index)
                    val isCorrect = (index == q.correctIndex)

                    val optionBorderColor = when {
                        !hasAnswered -> colors.outline.copy(alpha = dimensions.cardBorderAlpha)
                        isCorrect -> colors.success
                        isSelected && !isCorrect -> colors.error
                        else -> colors.outline.copy(alpha = 0.2f)
                    }

                    val optionBgColor = when {
                        !hasAnswered -> colors.surface
                        isCorrect -> colors.success.copy(alpha = 0.15f)
                        isSelected && !isCorrect -> colors.error.copy(alpha = 0.15f)
                        else -> colors.surface.copy(alpha = 0.5f)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(dimensions.cornerMedium))
                            .clickable(enabled = !hasAnswered) {
                                onSelectOption(index)
                            },
                        shape = RoundedCornerShape(dimensions.cornerMedium),
                        color = optionBgColor,
                        border = BorderStroke(1.5.dp, optionBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = if (option.text.length > 60) 10.dp else 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val optionTextStyle = when {
                                option.text.length > 70 -> MaterialTheme.typography.bodySmall
                                option.text.length > 40 -> MaterialTheme.typography.bodyMedium
                                else -> MaterialTheme.typography.titleMedium
                            }

                            Text(
                                text = option.text,
                                style = optionTextStyle,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurface,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            if (hasAnswered) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "صحیح",
                                        tint = colors.success,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "غلط",
                                        tint = colors.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Buttons (Always Visible & Pinned at Bottom - حل نقص ISS-68)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPreviousQuestion,
                enabled = currentIndex > 0,
                modifier = Modifier
                    .weight(0.35f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.onSurface,
                    disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (currentIndex > 0) colors.outline.copy(alpha = 0.4f) else colors.outline.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "سؤال قبلی",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "قبلی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onNextQuestion,
                enabled = hasAnswered,
                modifier = Modifier
                    .weight(0.65f)
                    .height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary,
                    disabledContainerColor = colors.surfaceVariant,
                    disabledContentColor = colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when {
                            !hasAnswered -> "انتخاب گزینه"
                            currentIndex + 1 < questions.size -> "سؤال بعدی"
                            else -> "مشاهده نتیجه آزمون"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (hasAnswered) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(
                        text = "بستن",
                        color = colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = colors.warning
                    )
                    Text(
                        text = "راهنما و توضیحات واژه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = q.promptText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )

                    if (!q.note.isNullOrBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "یادداشت و نکات گرامری:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = q.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                        }
                    } else {
                        Text(
                            text = "یادداشت اختصاصی برای این واژه ثبت نشده است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    if (q.categoryName != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "دسته‌بندی موضوعی:",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = q.categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.secondary
                            )
                        }
                    }
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // دیالوگ هشدار تأیید خروج از آزمون (حل نقص ISS-113)
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "خروج از آزمون",
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = "آیا مطمئن هستید که می‌خواهید از آزمون خارج شوید؟ پاسخ‌های ثبت‌نشده در این نشست ذخیره نخواهند شد.",
                    color = colors.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onExit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text("بله، خروج", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("ادامه آزمون", fontWeight = FontWeight.Bold, color = colors.primary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
