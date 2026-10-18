package com.manidigit.yadin.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import com.manidigit.yadin.ui.theme.YadinIcons
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import com.manidigit.yadin.ui.theme.YadinColors
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.ReviewCard
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.ui.components.DifficultyBadge
import com.manidigit.yadin.ui.components.SpeakButton
import com.manidigit.yadin.ui.components.StageBadge
import com.manidigit.yadin.ui.components.YadinCard
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.util.formatDigits

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
                        imageVector = YadinIcons.categoryBook,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.flashcard_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(R.string.flashcard_empty_desc),
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
                    Text(stringResource(R.string.summary_back_home), fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    BackHandler {
        showExitConfirmDialog = true
    }

    val card = cards[currentIndex]
    val progress = (currentIndex + 1).toFloat() / cards.size

    val frontScrollState = rememberScrollState()
    val backScrollState = rememberScrollState()

    var dragOffset by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(),
        label = "dragOffset"
    )

    val rotationAnimatable = remember { Animatable(0f) }

    LaunchedEffect(currentIndex) {
        dragOffset = 0f
        frontScrollState.scrollTo(0)
        backScrollState.scrollTo(0)
        rotationAnimatable.snapTo(0f)
    }

    LaunchedEffect(isFlipped) {
        val target = if (isFlipped) 180f else 0f
        if (rotationAnimatable.targetValue != target) {
            rotationAnimatable.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

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
                        imageVector = YadinIcons.close,
                        contentDescription = stringResource(R.string.flashcard_exit_confirm_title),
                        tint = colors.onSurface
                    )
                }

                Text(
                    text = stringResource(R.string.flashcard_card_progress, (currentIndex + 1).formatDigits(), cards.size.formatDigits()),
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
                        rotationY = rotationAnimatable.value
                        translationX = animatedOffsetX
                        rotationZ = (animatedOffsetX / 25f).coerceIn(-12f, 12f)
                        cameraDistance = 16f * density
                    }
                    .pointerInput(currentIndex, isFlipped) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragOffset > 150f) {
                                    dragOffset = 0f
                                    onAnswer(true)
                                } else if (dragOffset < -150f) {
                                    dragOffset = 0f
                                    onAnswer(false)
                                } else {
                                    dragOffset = 0f
                                }
                            },
                            onDragCancel = { dragOffset = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount
                            }
                        )
                    }
                    .clickable(enabled = !rotationAnimatable.isRunning) { onFlip() },
                shape = RoundedCornerShape(dimensions.cornerLarge),
                backgroundColor = colors.surface
            ) {
                val isShowingFront by remember { derivedStateOf { rotationAnimatable.value <= 90f } }
                if (isShowingFront) {
                    FlashcardFrontFace(
                        card = card,
                        showCategory = showCategory,
                        frontScrollState = frontScrollState,
                        colors = colors
                    )
                } else {
                    FlashcardBackFace(
                        card = card,
                        backScrollState = backScrollState,
                        colors = colors
                    )
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
                    Icon(YadinIcons.close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.flashcard_forgot), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                IconButton(
                    onClick = onFlip,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = YadinIcons.flipCard,
                        contentDescription = stringResource(R.string.flashcard_flip_back),
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
                        contentColor = colors.onSuccess
                    )
                ) {
                    Icon(YadinIcons.check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.flashcard_remembered), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    imageVector = YadinIcons.flipCard,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.flashcard_show_answer), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.flashcard_exit_confirm_title),
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.flashcard_exit_confirm_msg),
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
                    Text(stringResource(R.string.action_yes_exit), fontWeight = FontWeight.Bold, color = colors.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text(stringResource(R.string.flashcard_continue_session), fontWeight = FontWeight.Bold, color = colors.primary)
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FlashcardFrontFace(
    card: ReviewCard,
    showCategory: Boolean,
    frontScrollState: ScrollState,
    colors: YadinColors
) {
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

            val isFrontLtr = (card.direction == CardDirection.NORMAL) || !TextUtilities.isRtlText(card.sourceText)
            CompositionLocalProvider(LocalLayoutDirection provides (if (isFrontLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                Text(
                    text = card.sourceText,
                    style = promptStyle.copy(textDirection = if (isFrontLtr) TextDirection.Ltr else TextDirection.Rtl),
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = YadinIcons.flipCard,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(R.string.flashcard_flip_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FlashcardBackFace(
    card: ReviewCard,
    backScrollState: ScrollState,
    colors: YadinColors
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        val isSourceLtr = (card.direction == CardDirection.NORMAL) || !TextUtilities.isRtlText(card.sourceText)
        CompositionLocalProvider(LocalLayoutDirection provides (if (isSourceLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
            Text(
                text = card.sourceText,
                style = MaterialTheme.typography.titleMedium.copy(textDirection = if (isSourceLtr) TextDirection.Ltr else TextDirection.Rtl),
                color = colors.primary,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(backScrollState)
        ) {
            Text(
                text = if (card.direction == CardDirection.NORMAL) stringResource(R.string.flashcard_target_label_fa) else stringResource(R.string.flashcard_target_label_es),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (trans in card.targetTranslations) {
                    val translationStyle = if (trans.length > 50) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    }
                    val isTransLtr = (card.direction == CardDirection.REVERSE) || !TextUtilities.isRtlText(trans)
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
                            CompositionLocalProvider(LocalLayoutDirection provides (if (isTransLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                                Text(
                                    text = trans,
                                    style = translationStyle.copy(textDirection = if (isTransLtr) TextDirection.Ltr else TextDirection.Rtl),
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
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
            text = stringResource(R.string.flashcard_did_you_recall),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )
    }
}

