package com.manidigit.yadin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manidigit.yadin.R
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
import com.manidigit.yadin.ui.theme.LocalYadinShapes
import com.manidigit.yadin.ui.theme.YadinColors
import com.manidigit.yadin.ui.theme.YadinIcons
import com.manidigit.yadin.ui.theme.GptGlossyIcon
import com.manidigit.yadin.ui.util.TtsManager
import com.manidigit.yadin.ui.util.formatDigits

/**
 * ۱. نوع محتوا / حالت مطالعه (ReviewMode)
 * - آزمون ۴ گزینه‌ای: FactCheck (برگه آزمون با چک‌باکس و تیک)
 * - فلش‌کارت تعاملی: Style (دو کارت روی هم با زاویه ملایم)
 */
fun getReviewModeIcon(mode: ReviewMode): ImageVector = YadinIcons.forReviewMode(mode)

fun getReviewModeColor(mode: ReviewMode, colors: YadinColors): Color = when (mode) {
    ReviewMode.QUIZ -> colors.primary
    ReviewMode.FLASHCARD -> colors.accentPurple
}

/**
 * ۲. دوره‌های زمانی (Leitner Stage) و ۳. وضعیت یادگیری یادگرفته
 * - روزانه: Today (تقویم تک‌برگی روزانه)
 * - هفتگی: DateRange (برگه تقویم هفتگی)
 * - ماهانه: CalendarMonth (تقویم شبکه‌ای کامل ماهانه)
 * - یادگرفته: Verified (نشان/مدال ستاره‌دار تیک‌دار رسمی)
 */
fun getStageColor(stage: Stage, colors: YadinColors): Color = when (stage) {
    Stage.DAILY -> colors.primary
    Stage.WEEKLY -> colors.accentPurple
    Stage.MONTHLY -> colors.warning
    Stage.LEARNED -> colors.success
}

fun getStageIcon(stage: Stage): ImageVector = YadinIcons.forStage(stage)

/**
 * ۳. وضعیت یادگیری / فیلترهای تمرین (ReviewType)
 * - تصادفی (رندوم): Shuffle (دو فلش متقاطع شافل)
 * - یادگرفته: Verified (نشان ستاره‌دار تیک‌دار)
 * - روزانه / هفتگی / ماهانه: مطابق دوره‌های زمانی لایتنر
 */
fun getReviewTypeIcon(type: ReviewType): ImageVector = YadinIcons.forReviewType(type)

fun getReviewTypeColor(type: ReviewType, colors: YadinColors): Color = when (type) {
    ReviewType.DAILY -> getStageColor(Stage.DAILY, colors)
    ReviewType.WEEKLY -> getStageColor(Stage.WEEKLY, colors)
    ReviewType.MONTHLY -> getStageColor(Stage.MONTHLY, colors)
    ReviewType.LEARNED -> getStageColor(Stage.LEARNED, colors)
    ReviewType.RANDOM -> colors.secondary
    ReviewType.SM2 -> colors.info
    ReviewType.FSRS -> colors.primary
}

/**
 * ۴. سطح دشواری محتوا (VocabularyDifficulty)
 * - آسان: CheckCircle (دایره سبز ساده با تیک) - رنگ سبز
 * - متوسط: Tune (نماد سنجش نیمه‌پر / دو خط موج‌دار) - رنگ زرد/کهربایی
 * - سخت: LocalFireDepartment (شعله آتش کوچک) - رنگ نارنجی
 * - خیلی سخت: Bolt (صاعقه پرانرژی) - رنگ قرمز
 */
fun getDifficultyColor(difficulty: VocabularyDifficulty, colors: YadinColors): Color = when (difficulty) {
    VocabularyDifficulty.EASY -> colors.difficultyEasy
    VocabularyDifficulty.MEDIUM -> colors.difficultyMedium
    VocabularyDifficulty.HARD -> colors.difficultyHard
    VocabularyDifficulty.VERY_HARD -> colors.difficultyVeryHard
}

fun getDifficultyIcon(difficulty: VocabularyDifficulty): ImageVector = YadinIcons.forDifficulty(difficulty)

/**
 * ۵. سطح مهارت کاربر در آزمون (Proficiency / QuizLevel)
 * - مبتدی: Eco (جوانه گیاه دو‌برگی)
 * - نرمال: Shield (سپر دفاعی ساده)
 * - حرفه‌ای: WorkspacePremium (نشان/تاج طلایی ویژه)
 */
fun getQuizLevelIcon(level: QuizLevel): ImageVector = YadinIcons.forQuizLevel(level)

fun getQuizLevelColor(level: QuizLevel, colors: YadinColors): Color = when (level) {
    QuizLevel.BEGINNER -> colors.success
    QuizLevel.NORMAL -> colors.primary
    QuizLevel.PROFESSIONAL -> colors.warning
}

/**
 * ۶. آمار و فیلترهای واژگان (VocabStatType)
 * - کل کلمات: LibraryBooks (کتابخانه / پشته‌ای از کتاب‌ها)
 * - تمرین‌شده‌ها: AssignmentTurnedIn (برگه با قلم و تیک تأیید)
 * - تمرین‌نشده: HourglassEmpty (ساعت شنی دست‌نخورده)
 * - یادگرفته: Verified (یکپارچه با مرحله یادگرفته)
 */
enum class VocabStatType {
    TOTAL,
    PRACTICED,
    UNPRACTICED,
    MASTERED
}

fun getVocabStatIcon(type: VocabStatType): ImageVector = when (type) {
    VocabStatType.TOTAL -> YadinIcons.vocabTotal
    VocabStatType.PRACTICED -> YadinIcons.vocabPracticed
    VocabStatType.UNPRACTICED -> YadinIcons.vocabUnpracticed
    VocabStatType.MASTERED -> YadinIcons.vocabLearned
}

fun getVocabStatColor(type: VocabStatType, colors: YadinColors): Color = when (type) {
    VocabStatType.TOTAL -> colors.primary
    VocabStatType.PRACTICED -> colors.accentPurple
    VocabStatType.UNPRACTICED -> colors.warning
    VocabStatType.MASTERED -> getStageColor(Stage.LEARNED, colors)
}

@Composable
fun YadinCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape? = null,
    backgroundColor: Color? = null,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val shapes = LocalYadinShapes.current
    val resolvedShape = shape ?: shapes.medium
    val cardColor = backgroundColor ?: colors.card

    val resolvedBorder = borderStroke ?: if (colors.cardBorderBrush != null) {
        BorderStroke(1.2.dp, colors.cardBorderBrush)
    } else {
        BorderStroke(1.dp, colors.outline.copy(alpha = dimensions.cardBorderAlpha))
    }

    Surface(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(resolvedShape)
                        .clickable { onClick() }
                } else Modifier
            ),
        shape = resolvedShape,
        color = cardColor,
        tonalElevation = dimensions.cardElevation,
        border = resolvedBorder,
        content = content
    )
}

/**
 * هدر استاندارد درون‌کارتی برای تمامی بخش‌های برنامه با آیکون دایره‌ای رنگی، عنوان و زیرعنوان اختیاری
 */
@Composable
fun YadinSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = LocalYadinColors.current.primary,
    subtitle: String? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = LocalYadinColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            GptGlossyIcon(
                image = icon,
                contentDescription = null,
                modifier = Modifier,
                size = 38.dp,
                iconSize = 19.dp,
                tint = iconTint
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (trailingContent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                content = trailingContent
            )
        }
    }
}

/**
 * کاشی آماری استاندارد و یکپارچه برای نمایش آمار واژگان، خلاصه نشست و شاخص‌های کلیدی در تمام صفحات
 */
@Composable
fun YadinStatTile(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current
    val shape = RoundedCornerShape(dimensions.cornerSmall)
    val isGpt = colors.cardBorderBrush != null

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isGpt) {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            accentColor.copy(alpha = 0.16f),
                            colors.surface.copy(alpha = 0.94f),
                            colors.accentPurple.copy(alpha = 0.06f)
                        )
                    )
                } else accentColor.copy(alpha = 0.10f)
            )
            .border(
                width = if (isGpt) 1.2.dp else 1.dp,
                brush = if (isGpt) {
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(
                            accentColor.copy(alpha = 0.42f),
                            colors.outlineVariant.copy(alpha = 0.72f),
                            colors.accentPurple.copy(alpha = 0.30f)
                        )
                    )
                } else androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.35f)),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GptGlossyIcon(
                image = icon,
                contentDescription = null,
                size = 30.dp,
                iconSize = 16.dp,
                tint = accentColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 15.sp,
                maxLines = 1
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DifficultyCounterRow(
    counts: Map<VocabularyDifficulty, Int>,
    onSelectDifficulty: ((VocabularyDifficulty) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensions.itemGapMedium)
    ) {
        val items = listOf(
            VocabularyDifficulty.EASY to stringResource(R.string.difficulty_easy),
            VocabularyDifficulty.MEDIUM to stringResource(R.string.difficulty_medium),
            VocabularyDifficulty.HARD to stringResource(R.string.difficulty_hard),
            VocabularyDifficulty.VERY_HARD to stringResource(R.string.difficulty_very_hard)
        )

        items.forEach { (diff, label) ->
            val count = counts[diff] ?: 0
            val color = getDifficultyColor(diff, colors)
            val icon = getDifficultyIcon(diff)

            YadinStatTile(
                title = label,
                value = count.formatDigits(),
                icon = icon,
                accentColor = color,
                modifier = Modifier.weight(1f),
                onClick = if (onSelectDifficulty != null) ({ onSelectDifficulty(diff) }) else null
            )
        }
    }
}

@Composable
fun StageBadge(stage: Stage, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val shapes = LocalYadinShapes.current
    val title = when (stage) {
        Stage.DAILY -> stringResource(R.string.stage_daily)
        Stage.WEEKLY -> stringResource(R.string.stage_weekly)
        Stage.MONTHLY -> stringResource(R.string.stage_monthly)
        Stage.LEARNED -> stringResource(R.string.stage_learned)
    }
    val color = getStageColor(stage, colors)
    val icon = getStageIcon(stage)
    val shape = shapes.badge

    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(0.8.dp, color.copy(alpha = 0.45f), shape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = title,
            color = color,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun DifficultyBadge(difficulty: VocabularyDifficulty, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val shapes = LocalYadinShapes.current
    val title = when (difficulty) {
        VocabularyDifficulty.EASY -> stringResource(R.string.difficulty_easy)
        VocabularyDifficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
        VocabularyDifficulty.HARD -> stringResource(R.string.difficulty_hard)
        VocabularyDifficulty.VERY_HARD -> stringResource(R.string.difficulty_very_hard)
    }
    val color = getDifficultyColor(difficulty, colors)
    val icon = getDifficultyIcon(difficulty)
    val shape = shapes.badge

    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(0.8.dp, color.copy(alpha = 0.40f), shape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = title,
            color = color,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun StreakChip(streakDays: Int, modifier: Modifier = Modifier) {
    val colors = LocalYadinColors.current
    val shapes = LocalYadinShapes.current
    val shape = shapes.pill

    Row(
        modifier = modifier
            .clip(shape)
            .background(colors.warning.copy(alpha = 0.18f))
            .border(1.dp, colors.warning.copy(alpha = 0.45f), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = YadinIcons.streakFire,
            contentDescription = stringResource(R.string.content_desc_streak),
            tint = colors.warning,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "${streakDays.formatDigits()} ${stringResource(R.string.home_streak_days_suffix)}",
            color = colors.warning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SpeakButton(
    text: String,
    languageCode: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (!enabled) return
    val context = LocalContext.current
    val colors = LocalYadinColors.current

    IconButton(
        onClick = {
            TtsManager.speak(context, text, languageCode)
        },
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector = YadinIcons.speakAudio,
            contentDescription = stringResource(R.string.content_desc_speak),
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * هدر استاندارد و یکپارچه تمامی صفحات فرعی برنامه (مجهز به دکمه بازگشت ۳۸dp، عنوان، زیرعنوان اختیاری و اکشن‌های سمت چپ)
 */
@Composable
fun YadinScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = LocalYadinColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = YadinIcons.back,
                    contentDescription = stringResource(R.string.action_back),
                    tint = colors.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (trailingContent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = trailingContent
            )
        }
    }
}

/**
 * دکمه و چیپ انتخابگر استاندارد Material 3 برای تمام بخش‌های برنامه
 * (حالت مرور، سطح مهارت گزینه‌های آزمون، سقف تعداد کلمات، فیلتر سختی، تگ دسته‌بندی، سیاست تکرار و تنظیمات)
 * در هر دو حالت انتخاب‌شده و انتخاب‌نشده دارای تینت و کادر رنگی معنایی ظریف است تا کاملاً با کاشی‌های لایتنر هم‌خانواده باشد.
 */
@Composable
fun YadinSelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeText: String? = null,
    subtitle: String? = null,
    accentColor: Color? = null,
    preserveAccentWhenUnselected: Boolean = true,
    highContrastSelection: Boolean = false,
    verticalLayout: Boolean = false,
    minHeight: Dp = 38.dp,
    fontSize: TextUnit = 11.5.sp
) {
    val colors = LocalYadinColors.current
    val effectiveAccent = accentColor ?: colors.primary
    val shape = com.manidigit.yadin.ui.theme.LocalYadinShapes.current.extraSmall

    val bgColor = when {
        isSelected && highContrastSelection -> effectiveAccent.copy(alpha = 0.28f)
        isSelected -> effectiveAccent.copy(alpha = 0.18f)
        highContrastSelection -> colors.surfaceVariant.copy(alpha = 0.22f)
        preserveAccentWhenUnselected -> effectiveAccent.copy(alpha = 0.07f)
        else -> colors.surfaceVariant.copy(alpha = 0.55f)
    }

    val borderColor = when {
        isSelected -> effectiveAccent
        highContrastSelection -> effectiveAccent.copy(alpha = 0.16f)
        preserveAccentWhenUnselected -> effectiveAccent.copy(alpha = 0.30f)
        else -> colors.outline.copy(alpha = 0.30f)
    }

    val borderWidth = when {
        isSelected && highContrastSelection -> 2.dp
        isSelected -> 1.5.dp
        highContrastSelection -> 0.8.dp
        else -> 1.dp
    }

    val iconTint = when {
        isSelected -> effectiveAccent
        highContrastSelection -> effectiveAccent.copy(alpha = 0.42f)
        preserveAccentWhenUnselected -> effectiveAccent.copy(alpha = 0.85f)
        else -> colors.onSurfaceVariant
    }

    val textColor = when {
        isSelected -> effectiveAccent
        highContrastSelection -> colors.onSurfaceVariant.copy(alpha = 0.50f)
        else -> colors.onSurface
    }

    val badgeColor = when {
        isSelected -> effectiveAccent
        highContrastSelection -> effectiveAccent.copy(alpha = 0.45f)
        preserveAccentWhenUnselected -> effectiveAccent
        else -> colors.onSurface
    }

    val subtitleColor = when {
        isSelected -> colors.onSurfaceVariant.copy(alpha = 0.9f)
        highContrastSelection -> colors.onSurfaceVariant.copy(alpha = 0.42f)
        else -> colors.onSurfaceVariant.copy(alpha = 0.8f)
    }

    val isGptTheme = colors.cardBorderBrush != null
    val chipBackground = if (isGptTheme) {
        androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                bgColor,
                colors.surfaceVariant.copy(alpha = if (isSelected) 0.88f else 0.58f),
                bgColor
            )
        )
    } else androidx.compose.ui.graphics.SolidColor(bgColor)
    val chipBorder = if (isGptTheme) {
        androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                borderColor,
                colors.accentPurple.copy(alpha = if (isSelected) 0.62f else 0.30f),
                borderColor
            )
        )
    } else androidx.compose.ui.graphics.SolidColor(borderColor)

    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(shape)
            .background(chipBackground)
            .border(
                width = if (isGptTheme && isSelected) borderWidth.coerceAtLeast(1.5.dp) else borderWidth,
                brush = chipBorder,
                shape = shape
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (verticalLayout) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (badgeText != null) {
                        Text(
                            text = badgeText,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected || !highContrastSelection) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = badgeColor,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = text,
                    fontSize = fontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = subtitleColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = text,
                    fontSize = fontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                if (badgeText != null) {
                    Text(
                        text = badgeText,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected || !highContrastSelection) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = badgeColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
