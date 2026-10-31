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
import com.manidigit.yadin.domain.algorithm.FsrsAlgorithm
import com.manidigit.yadin.domain.algorithm.FsrsState
import com.manidigit.yadin.domain.algorithm.Sm2Algorithm
import com.manidigit.yadin.domain.algorithm.Sm2Quality
import com.manidigit.yadin.domain.algorithm.Sm2State
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color

internal data class FlashcardHintInfo(
    val coreAnswer: String,
    val firstLetter: String,
    val wordCount: Int,
    val letterCount: Int,
    val maskedPreview: String,
    val maxRevealableLetters: Int,
    val parentheticalClue: String?
)

internal fun buildFlashcardHintInfo(
    rawTranslation: String?,
    revealCount: Int
): FlashcardHintInfo? {
    if (rawTranslation.isNullOrBlank()) return null
    val trimmed = rawTranslation.trim()
    val parenRegex = Regex("\\(([^()]+)\\)")
    val parenMatches = parenRegex.findAll(trimmed)
        .map { it.groupValues[1].trim() }
        .filter { it.isNotEmpty() }
        .toList()
    val stripped = parenRegex.replace(trimmed, "").replace(Regex("\\s+"), " ").trim()
    val core = if (stripped.isNotEmpty()) stripped else trimmed
    val parentheticalClue = if (stripped.isNotEmpty() && parenMatches.isNotEmpty()) {
        parenMatches.joinToString(" • ")
    } else {
        null
    }

    val words = core.split(Regex("\\s+")).filter { it.isNotBlank() }
    val wordCount = words.size.coerceAtLeast(1)
    val letterCount = core.count { !it.isWhitespace() }
    val firstChar = core.firstOrNull { !it.isWhitespace() }?.toString() ?: ""
    val maxRevealable = (letterCount - 1).coerceAtLeast(1)
    val effectiveReveal = revealCount.coerceIn(1, maxRevealable)

    var revealedSoFar = 0
    val maskedWords = words.map { word ->
        word.map { ch ->
            if (ch.isLetterOrDigit()) {
                if (revealedSoFar < effectiveReveal) {
                    revealedSoFar++
                    ch.toString()
                } else {
                    "•"
                }
            } else {
                ch.toString()
            }
        }.joinToString(" ")
    }
    val maskedPreview = maskedWords.joinToString("   ")

    return FlashcardHintInfo(
        coreAnswer = core,
        firstLetter = firstChar,
        wordCount = wordCount,
        letterCount = letterCount,
        maskedPreview = maskedPreview,
        maxRevealableLetters = maxRevealable,
        parentheticalClue = parentheticalClue
    )
}

@Composable
fun FlashcardScreen(
    cards: List<ReviewCard>,
    currentIndex: Int,
    isFlipped: Boolean,
    showCategory: Boolean = false,
    ttsEnabled: Boolean = true,
    ttsAutoPlay: Boolean = false,
    isSm2Mode: Boolean = false,
    isFsrsMode: Boolean = false,
    fsrsDesiredRetention: Double = FsrsAlgorithm.DEFAULT_DESIRED_RETENTION,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onSm2Answer: (Sm2Quality) -> Unit = { onAnswer(it.isCorrect) },
    onExit: () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var hintRevealCount by remember { mutableStateOf(0) }

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
        showHelpDialog = false
        hintRevealCount = 0
        frontScrollState.scrollTo(0)
        backScrollState.scrollTo(0)
        rotationAnimatable.snapTo(0f)
    }

    val activeHintInfo = remember(card.conceptId, card.targetTranslations, hintRevealCount) {
        buildFlashcardHintInfo(
            rawTranslation = card.targetTranslations.firstOrNull(),
            revealCount = hintRevealCount.coerceAtLeast(1)
        )
    }

    val openHintDialog = {
        if (hintRevealCount == 0) {
            hintRevealCount = 1
        }
        showHelpDialog = true
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

    LaunchedEffect(card.conceptId, currentIndex, ttsEnabled, ttsAutoPlay) {
        if (ttsEnabled && ttsAutoPlay) {
            val frontLang = if (card.direction == CardDirection.NORMAL) "es" else "fa"
            com.manidigit.yadin.ui.util.TtsManager.speak(context, card.sourceText, frontLang)
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
                                if (!isFlipped) {
                                    dragOffset = 0f
                                } else if (dragOffset > 150f) {
                                    dragOffset = 0f
                                    if (isSm2Mode || isFsrsMode) onSm2Answer(Sm2Quality.GOOD) else onAnswer(true)
                                } else if (dragOffset < -150f) {
                                    dragOffset = 0f
                                    if (isSm2Mode || isFsrsMode) onSm2Answer(Sm2Quality.AGAIN) else onAnswer(false)
                                } else {
                                    dragOffset = 0f
                                }
                            },
                            onDragCancel = { dragOffset = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                if (isFlipped) {
                                    dragOffset += dragAmount
                                }
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
                        ttsEnabled = ttsEnabled,
                        frontScrollState = frontScrollState,
                        colors = colors,
                        inlineHintText = if (hintRevealCount > 0) activeHintInfo?.maskedPreview else null,
                        onShowHint = openHintDialog
                    )
                } else {
                    FlashcardBackFace(
                        card = card,
                        showCategory = showCategory,
                        ttsEnabled = ttsEnabled,
                        backScrollState = backScrollState,
                        colors = colors,
                        onShowHint = openHintDialog
                    )
                }
            }
        }

        // Bottom Action Buttons
        if (isFlipped) {
            if (isSm2Mode || isFsrsMode) {
                val intervalPreview = remember(
                    card.conceptId,
                    card.stage,
                    isFsrsMode,
                    fsrsDesiredRetention,
                    card.sm2Repetitions,
                    card.sm2EasinessFactor,
                    card.sm2IntervalDays,
                    card.fsrsStability,
                    card.fsrsDifficulty,
                    card.fsrsReps,
                    card.fsrsLapses,
                    card.fsrsState,
                    card.lastReviewedDay
                ) {
                    if (isFsrsMode) {
                        val effectiveFsrs = FsrsAlgorithm.resolveEffectiveState(
                            state = FsrsState(
                                stability = card.fsrsStability,
                                difficulty = card.fsrsDifficulty,
                                reps = card.fsrsReps,
                                lapses = card.fsrsLapses,
                                state = card.fsrsState
                            ),
                            stage = card.stage,
                            lastReviewedDay = card.lastReviewedDay,
                            sm2IntervalDays = card.sm2IntervalDays,
                            sm2Repetitions = card.sm2Repetitions
                        )
                        FsrsAlgorithm.previewIntervals(
                            currentState = effectiveFsrs,
                            lastReviewedDay = card.lastReviewedDay,
                            desiredRetention = fsrsDesiredRetention
                        )
                    } else {
                        val effectiveSm2 = Sm2Algorithm.resolveEffectiveState(
                            state = Sm2State(
                                repetitions = card.sm2Repetitions,
                                easinessFactor = card.sm2EasinessFactor,
                                intervalDays = card.sm2IntervalDays
                            ),
                            stage = card.stage,
                            lastReviewedDay = card.lastReviewedDay,
                            fsrsStability = card.fsrsStability
                        )
                        Sm2Algorithm.previewIntervals(effectiveSm2)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Sm2QualityButton(
                        title = stringResource(R.string.flashcard_sm2_again),
                        intervalLabel = stringResource(R.string.flashcard_sm2_days_format, intervalPreview.againDays.formatDigits()),
                        containerColor = colors.error.copy(alpha = 0.16f),
                        contentColor = colors.error,
                        modifier = Modifier.weight(1f),
                        onClick = { onSm2Answer(Sm2Quality.AGAIN) }
                    )
                    Sm2QualityButton(
                        title = stringResource(R.string.flashcard_sm2_hard),
                        intervalLabel = stringResource(R.string.flashcard_sm2_days_format, intervalPreview.hardDays.formatDigits()),
                        containerColor = colors.warning.copy(alpha = 0.16f),
                        contentColor = colors.warning,
                        modifier = Modifier.weight(1f),
                        onClick = { onSm2Answer(Sm2Quality.HARD) }
                    )
                    Sm2QualityButton(
                        title = stringResource(R.string.flashcard_sm2_good),
                        intervalLabel = stringResource(R.string.flashcard_sm2_days_format, intervalPreview.goodDays.formatDigits()),
                        containerColor = colors.primary.copy(alpha = 0.18f),
                        contentColor = colors.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onSm2Answer(Sm2Quality.GOOD) }
                    )
                    Sm2QualityButton(
                        title = stringResource(R.string.flashcard_sm2_easy),
                        intervalLabel = stringResource(R.string.flashcard_sm2_days_format, intervalPreview.easyDays.formatDigits()),
                        containerColor = colors.success.copy(alpha = 0.18f),
                        contentColor = colors.success,
                        modifier = Modifier.weight(1f),
                        onClick = { onSm2Answer(Sm2Quality.EASY) }
                    )
                    IconButton(
                        onClick = onFlip,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = YadinIcons.flipCard,
                            contentDescription = stringResource(R.string.flashcard_flip_back),
                            tint = colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
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
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = openHintDialog,
                    modifier = Modifier
                        .weight(0.36f)
                        .height(48.dp)
                        .testTag("flashcard_hint_button"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.warning.copy(alpha = 0.10f),
                        contentColor = colors.onSurface
                    ),
                    border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = YadinIcons.hintLightbulb,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.flashcard_hint_btn),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onFlip,
                    modifier = Modifier
                        .weight(0.64f)
                        .height(48.dp)
                        .testTag("flashcard_show_answer_button"),
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.flashcard_show_answer),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
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
                        text = stringResource(R.string.action_close),
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
                        imageVector = YadinIcons.hintLightbulb,
                        contentDescription = null,
                        tint = colors.warning
                    )
                    Text(
                        text = stringResource(R.string.flashcard_hint_dialog_title),
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
                    val isHintSourceLtr = (card.direction == CardDirection.NORMAL) || !TextUtilities.isRtlText(card.sourceText)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides (if (isHintSourceLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                            Text(
                                text = card.sourceText,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    textDirection = if (isHintSourceLtr) TextDirection.Ltr else TextDirection.Rtl
                                ),
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        val frontLang = if (card.direction == CardDirection.NORMAL) "es" else "fa"
                        SpeakButton(text = card.sourceText, languageCode = frontLang, enabled = ttsEnabled)
                    }

                    if (activeHintInfo != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.warning.copy(alpha = 0.10f))
                                .border(1.dp, colors.warning.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.flashcard_hint_clue_label),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.warning
                            )
                            Text(
                                text = stringResource(
                                    R.string.flashcard_hint_structure_fmt,
                                    activeHintInfo.firstLetter,
                                    activeHintInfo.wordCount.formatDigits(),
                                    activeHintInfo.letterCount.formatDigits()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant
                            )
                            if (card.targetTranslations.size > 1) {
                                Text(
                                    text = stringResource(
                                        R.string.flashcard_hint_meanings_count_fmt,
                                        card.targetTranslations.size.formatDigits()
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.onSurfaceVariant
                                )
                            }

                            val isTargetLtr = (card.direction == CardDirection.REVERSE) || !TextUtilities.isRtlText(activeHintInfo.coreAnswer)
                            CompositionLocalProvider(LocalLayoutDirection provides (if (isTargetLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                                Text(
                                    text = activeHintInfo.maskedPreview,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        textDirection = if (isTargetLtr) TextDirection.Ltr else TextDirection.Rtl
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                )
                            }

                            if (hintRevealCount < activeHintInfo.maxRevealableLetters) {
                                TextButton(
                                    onClick = { hintRevealCount++ },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("flashcard_reveal_more_hint_button")
                                ) {
                                    Icon(
                                        imageVector = YadinIcons.hintLightbulb,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.flashcard_hint_reveal_more),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                            }
                        }

                        if (!activeHintInfo.parentheticalClue.isNullOrBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(R.string.flashcard_hint_context_clue_label),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = activeHintInfo.parentheticalClue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurface
                                )
                            }
                        }
                    }

                    if (!card.note.isNullOrBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.quiz_grammar_notes),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = card.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.quiz_no_notes),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    if (card.categoryName != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.quiz_category_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = card.categoryName,
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
private fun Sm2QualityButton(
    title: String,
    intervalLabel: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                maxLines = 1
            )
            Text(
                text = intervalLabel,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FlashcardFrontFace(
    card: ReviewCard,
    showCategory: Boolean,
    ttsEnabled: Boolean,
    frontScrollState: ScrollState,
    colors: YadinColors,
    inlineHintText: String? = null,
    onShowHint: () -> Unit = {}
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onShowHint,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant)
                        .testTag("flashcard_front_help_button")
                ) {
                    Icon(
                        imageVector = YadinIcons.helpGuide,
                        contentDescription = stringResource(R.string.flashcard_hint_tooltip),
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                val frontLang = if (card.direction == CardDirection.NORMAL) "es" else "fa"
                SpeakButton(text = card.sourceText, languageCode = frontLang, enabled = ttsEnabled)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
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

            if (!inlineHintText.isNullOrBlank()) {
                val isHintLtr = (card.direction == CardDirection.REVERSE) || !TextUtilities.isRtlText(inlineHintText)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.warning.copy(alpha = 0.12f))
                        .border(1.dp, colors.warning.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable { onShowHint() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = YadinIcons.hintLightbulb,
                        contentDescription = null,
                        tint = colors.warning,
                        modifier = Modifier.size(16.dp)
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides (if (isHintLtr) LayoutDirection.Ltr else LayoutDirection.Rtl)) {
                        Text(
                            text = stringResource(R.string.flashcard_hint_inline_prefix, inlineHintText),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDirection = if (isHintLtr) TextDirection.Ltr else TextDirection.Rtl
                            ),
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                    }
                }
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
    showCategory: Boolean,
    ttsEnabled: Boolean,
    backScrollState: ScrollState,
    colors: YadinColors,
    onShowHint: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onShowHint,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                            .testTag("flashcard_back_help_button")
                    ) {
                        Icon(
                            imageVector = YadinIcons.helpGuide,
                            contentDescription = stringResource(R.string.flashcard_hint_tooltip),
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    val frontLang = if (card.direction == CardDirection.NORMAL) "es" else "fa"
                    SpeakButton(text = card.sourceText, languageCode = frontLang, enabled = ttsEnabled)
                }
            }

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


