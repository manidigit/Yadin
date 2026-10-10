package com.manidigit.yadin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.ReviewCard
import com.manidigit.yadin.ui.components.DifficultyBadge
import com.manidigit.yadin.ui.components.SpeakButton
import com.manidigit.yadin.ui.components.StageBadge
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions

@Composable
fun FlashcardScreen(
    cards: List<ReviewCard>,
    currentIndex: Int,
    isFlipped: Boolean,
    showCategory: Boolean = false,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onExit: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // خالی بودن صف مرور (حل نقص ISS-88: افزودن دکمه خروج درون‌برنامه‌ای)
    if (cards.isEmpty() || currentIndex >= cards.size) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(dimensions.screenPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = "هیچ کارتی برای مرور وجود ندارد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = "همه واژگان واجد شرایط مرور شده‌اند یا کارتی مطابق فیلترهای انتخابی شما یافت نشد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("بازگشت به صفحه اصلی", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    // رهگیری دکمه بازگشت سیستم در حین مرور فعال
    BackHandler {
        showExitConfirmDialog = true
    }

    val card = cards[currentIndex]
    val progress = (currentIndex + 1).toFloat() / cards.size

    val frontScrollState = rememberScrollState()
    val backScrollState = rememberScrollState()

    LaunchedEffect(currentIndex) {
        frontScrollState.scrollTo(0)
        backScrollState.scrollTo(0)
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "cardFlip"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(dimensions.screenPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
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
                        contentDescription = "خروج از مرور",
                        tint = colors.onSurface
                    )
                }

                Text(
                    text = "کارت ${currentIndex + 1} از ${cards.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StageBadge(stage = card.stage)
                    DifficultyBadge(difficulty = card.difficulty)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )
        }

        // Flippable Flashcard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            YadinCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { onFlip() },
                shape = RoundedCornerShape(dimensions.cornerLarge),
                backgroundColor = colors.surface
            ) {
                if (rotation <= 90f) {
                    // Front Face
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (showCategory && card.categoryName != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = card.categoryName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(1.dp))
                            }
                            val frontLang = if (card.direction == CardDirection.NORMAL) "es" else "fa"
                            SpeakButton(text = card.sourceText, languageCode = frontLang)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(frontScrollState)
                        ) {
                            val promptStyle = when {
                                card.sourceText.length > 90 -> MaterialTheme.typography.titleMedium
                                card.sourceText.length > 45 -> MaterialTheme.typography.titleLarge
                                card.sourceText.length > 25 -> MaterialTheme.typography.headlineSmall
                                else -> MaterialTheme.typography.headlineLarge
                            }

                            Text(
                                text = card.sourceText,
                                style = promptStyle,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flip,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "برای مشاهده پاسخ ضربه بزنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Back Face
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = card.sourceText,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(backScrollState)
                        ) {
                            Text(
                                text = if (card.direction == CardDirection.NORMAL) "ترجمه فارسی:" else "واژه اسپانیایی:",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                card.targetTranslations.forEach { trans ->
                                    val translationStyle = if (trans.length > 50) {
                                        MaterialTheme.typography.titleMedium
                                    } else {
                                        MaterialTheme.typography.titleLarge
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors.surfaceVariant)
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = trans,
                                                style = translationStyle,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            if (!card.note.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Text(
                            text = "آیا پاسخ را به یاد آوردید؟",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Bottom Action Buttons
        if (isFlipped) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onAnswer(false) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error.copy(alpha = 0.15f),
                        contentColor = colors.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نادرست", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(
                    onClick = onFlip,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flip,
                        contentDescription = "برگشت به روی کارت",
                        tint = colors.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.success,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("به‌خاطر داشتم", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            Button(
                onClick = onFlip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Flip,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("نمایش پاسخ (چرخش کارت)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // دیالوگ هشدار تأیید خروج از مرور (حل نقص ISS-87 / ISS-113)
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = "خروج از جلسه مرور",
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = "آیا مطمئن هستید که می‌خواهید از جلسه مرور خارج شوید؟ کارت‌های بررسی‌نشده در این نشست ذخیره نخواهند شد.",
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
                    Text("ادامه مرور", fontWeight = FontWeight.Bold, color = colors.primary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
