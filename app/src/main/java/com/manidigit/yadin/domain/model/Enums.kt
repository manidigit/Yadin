package com.manidigit.yadin.domain.model

import androidx.annotation.StringRes
import com.manidigit.yadin.R

enum class EntryType {
    WORD,
    PHRASE,
    SENTENCE,
    IDIOM,
    COLLOCATION,
    STRUCTURE
}

object LanguageConstants {
    const val DEFAULT_SOURCE_LANGUAGE = "es"
    const val DEFAULT_TARGET_LANGUAGE = "fa"
    const val DEFAULT_LANGUAGE_PAIR = "es-fa"

    fun getSourceLanguage(pair: String = DEFAULT_LANGUAGE_PAIR): String {
        val parts = pair.split("-")
        return if (parts.isNotEmpty() && parts[0].isNotBlank()) parts[0] else DEFAULT_SOURCE_LANGUAGE
    }

    fun getTargetLanguage(pair: String = DEFAULT_LANGUAGE_PAIR): String {
        val parts = pair.split("-")
        return if (parts.size > 1 && parts[1].isNotBlank()) parts[1] else DEFAULT_TARGET_LANGUAGE
    }

    fun resolveLanguages(
        direction: CardDirection,
        pair: String = DEFAULT_LANGUAGE_PAIR
    ): Pair<String, String> {
        val source = getSourceLanguage(pair)
        val target = getTargetLanguage(pair)
        return if (direction == CardDirection.REVERSE) {
            Pair(target, source)
        } else {
            Pair(source, target)
        }
    }
}

enum class CardDirection {
    NORMAL,  // Source -> Target (e.g. Spanish -> Persian)
    REVERSE; // Target -> Source (e.g. Persian -> Spanish)

    fun resolveSourceAndTarget(pair: String = LanguageConstants.DEFAULT_LANGUAGE_PAIR): Pair<String, String> =
        LanguageConstants.resolveLanguages(this, pair)

    fun sourceLanguage(pair: String = LanguageConstants.DEFAULT_LANGUAGE_PAIR): String =
        resolveSourceAndTarget(pair).first

    fun targetLanguage(pair: String = LanguageConstants.DEFAULT_LANGUAGE_PAIR): String =
        resolveSourceAndTarget(pair).second
}

enum class Stage {
    DAILY,
    WEEKLY,
    MONTHLY,
    LEARNED
}

@StringRes
fun Stage.toStringRes(): Int = when (this) {
    Stage.DAILY -> R.string.stage_daily
    Stage.WEEKLY -> R.string.stage_weekly
    Stage.MONTHLY -> R.string.stage_monthly
    Stage.LEARNED -> R.string.stage_learned
}

enum class VocabularyDifficulty {
    EASY,
    MEDIUM,
    HARD,
    VERY_HARD
}

@StringRes
fun VocabularyDifficulty.toStringRes(): Int = when (this) {
    VocabularyDifficulty.EASY -> R.string.difficulty_easy
    VocabularyDifficulty.MEDIUM -> R.string.difficulty_medium
    VocabularyDifficulty.HARD -> R.string.difficulty_hard
    VocabularyDifficulty.VERY_HARD -> R.string.difficulty_very_hard
}

enum class SpacedRepetitionAlgorithm {
    YADIN,
    SM2,
    FSRS;

    companion object {
        fun fromString(value: String?): SpacedRepetitionAlgorithm = when (value?.uppercase()) {
            "SM2", "SM-2", "SUPERMEMO2" -> SM2
            "FSRS", "FSRS-4.5", "FSRS-5" -> FSRS
            else -> YADIN
        }
    }
}

@StringRes
fun SpacedRepetitionAlgorithm.toTitleRes(): Int = when (this) {
    SpacedRepetitionAlgorithm.YADIN -> R.string.settings_algo_yadin_title
    SpacedRepetitionAlgorithm.SM2 -> R.string.settings_algo_sm2_title
    SpacedRepetitionAlgorithm.FSRS -> R.string.settings_algo_fsrs_title
}

@StringRes
fun SpacedRepetitionAlgorithm.toSubtitleRes(): Int = when (this) {
    SpacedRepetitionAlgorithm.YADIN -> R.string.settings_algo_yadin_desc
    SpacedRepetitionAlgorithm.SM2 -> R.string.settings_algo_sm2_desc
    SpacedRepetitionAlgorithm.FSRS -> R.string.settings_algo_fsrs_desc
}

enum class ReviewType {
    DAILY,
    WEEKLY,
    MONTHLY,
    LEARNED,
    RANDOM,
    SM2,
    FSRS
}

enum class ReviewMode {
    FLASHCARD,
    QUIZ
}

@StringRes
fun ReviewMode.toStringRes(): Int = when (this) {
    ReviewMode.FLASHCARD -> R.string.review_mode_flashcard
    ReviewMode.QUIZ -> R.string.review_mode_quiz
}

enum class QuizLevel {
    BEGINNER,
    NORMAL,
    PROFESSIONAL;

    companion object {
        fun fromString(value: String?): QuizLevel? = when (value?.uppercase()) {
            "BEGINNER", "EASY" -> BEGINNER
            "NORMAL", "MEDIUM" -> NORMAL
            "PROFESSIONAL", "HARD" -> PROFESSIONAL
            else -> null
        }
    }
}

fun QuizLevel.toPersianLabel(): String = when (this) {
    QuizLevel.BEGINNER -> "مبتدی"
    QuizLevel.NORMAL -> "نرمال"
    QuizLevel.PROFESSIONAL -> "حرفه‌ای"
}

fun QuizLevel.toEnglishLabel(): String = when (this) {
    QuizLevel.BEGINNER -> "Beginner"
    QuizLevel.NORMAL -> "Normal"
    QuizLevel.PROFESSIONAL -> "Professional"
}

@StringRes
fun QuizLevel.toStringRes(): Int = when (this) {
    QuizLevel.BEGINNER -> R.string.quiz_level_beginner
    QuizLevel.NORMAL -> R.string.quiz_level_normal
    QuizLevel.PROFESSIONAL -> R.string.quiz_level_professional
}

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    ABANDONED
}

enum class SessionItemState {
    PENDING,
    ANSWERED,
    SKIPPED
}

enum class VariantType {
    MASCULINE,
    FEMININE,
    ALTERNATIVE
}

enum class RelationType {
    USED_IN,
    INFLECTED_FORM,
    SYNONYM,
    ANTONYM,
    CONTRAST,
    EXAMPLE_OF,
    RELATED_TO,
    DERIVED_FROM
}

fun RelationType.toPersianLabel(): String = when (this) {
    RelationType.SYNONYM -> "مترادف"
    RelationType.ANTONYM -> "متضاد"
    RelationType.USED_IN -> "به‌کاررفته در"
    RelationType.INFLECTED_FORM -> "شکل صرف‌شده"
    RelationType.CONTRAST -> "متمایز با"
    RelationType.EXAMPLE_OF -> "نمونه‌ای از"
    RelationType.RELATED_TO -> "مرتبط با"
    RelationType.DERIVED_FROM -> "مشتق از"
}

enum class DuplicatePolicy {
    SKIP,
    MERGE,
    REPLACE,
    KEEP_SEPARATE
}

enum class ImportItemStatus {
    PENDING,
    APPROVED,
    REJECTED
}

enum class AchievementId(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val iconName: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val threshold: Int
) {
    // ۱. دایره واژگان (Vocabulary Footprint: 100, 500, 2000, 5000)
    FIRST_TEN_WORDS(R.string.ach_title_first_ten, R.string.ach_desc_first_ten, "school", AchievementCategory.VOCABULARY, AchievementTier.BRONZE, 100),
    VOCABULARY_BUILDER(R.string.ach_title_builder, R.string.ach_desc_builder, "auto_stories", AchievementCategory.VOCABULARY, AchievementTier.SILVER, 500),
    VOCABULARY_MASTER(R.string.ach_title_master, R.string.ach_desc_master, "library_books", AchievementCategory.VOCABULARY, AchievementTier.GOLD, 2000),
    VOCABULARY_LEGEND(R.string.ach_title_legend, R.string.ach_desc_legend, "menu_book", AchievementCategory.VOCABULARY, AchievementTier.PLATINUM, 5000),

    // ۲. رگبار و مداومت (Streak Fire: 7, 30, 100, 365 days)
    STREAK_3_DAYS(R.string.ach_title_streak_3, R.string.ach_desc_streak_3, "local_fire_department", AchievementCategory.STREAK, AchievementTier.BRONZE, 7),
    STREAK_7_DAYS(R.string.ach_title_streak_7, R.string.ach_desc_streak_7, "whatshot", AchievementCategory.STREAK, AchievementTier.SILVER, 30),
    STREAK_30_DAYS(R.string.ach_title_streak_30, R.string.ach_desc_streak_30, "military_tech", AchievementCategory.STREAK, AchievementTier.GOLD, 100),
    STREAK_100_DAYS(R.string.ach_title_streak_100, R.string.ach_desc_streak_100, "diamond", AchievementCategory.STREAK, AchievementTier.PLATINUM, 365),

    // ۳. تثبیت لایتنر و حافظه دائمی (Deep Retention: 100, 500, 2000, 5000)
    LONG_TERM_MEMORY(R.string.ach_title_retention_bronze, R.string.ach_desc_retention_bronze, "neurology", AchievementCategory.RETENTION, AchievementTier.BRONZE, 100),
    RETENTION_SILVER(R.string.ach_title_retention_silver, R.string.ach_desc_retention_silver, "psychology", AchievementCategory.RETENTION, AchievementTier.SILVER, 500),
    RETENTION_GOLD(R.string.ach_title_retention_gold, R.string.ach_desc_retention_gold, "archive", AchievementCategory.RETENTION, AchievementTier.GOLD, 2000),
    RETENTION_PLATINUM(R.string.ach_title_retention_platinum, R.string.ach_desc_retention_platinum, "workspace_premium", AchievementCategory.RETENTION, AchievementTier.PLATINUM, 5000),

    // ۴. واژگان سخت و چالش‌برانگیز (Hard Words Conqueror: 10, 50, 150, 500)
    HARD_MASTER(R.string.ach_title_hard_bronze, R.string.ach_desc_hard_bronze, "fitness_center", AchievementCategory.HARD_WORDS, AchievementTier.BRONZE, 10),
    HARD_SILVER(R.string.ach_title_hard_silver, R.string.ach_desc_hard_silver, "construction", AchievementCategory.HARD_WORDS, AchievementTier.SILVER, 50),
    HARD_GOLD(R.string.ach_title_hard_gold, R.string.ach_desc_hard_gold, "psychology", AchievementCategory.HARD_WORDS, AchievementTier.GOLD, 150),
    HARD_PLATINUM(R.string.ach_title_hard_platinum, R.string.ach_desc_hard_platinum, "shield", AchievementCategory.HARD_WORDS, AchievementTier.PLATINUM, 500),

    // ۵. مهارت و تسلط بر کوییز (Quiz Mastery: 5, 20, 50, 100 perfect quizzes)
    QUIZ_ACE(R.string.ach_title_quiz_bronze, R.string.ach_desc_quiz_bronze, "grade", AchievementCategory.QUIZ, AchievementTier.BRONZE, 5),
    QUIZ_SILVER(R.string.ach_title_quiz_silver, R.string.ach_desc_quiz_silver, "emoji_events", AchievementCategory.QUIZ, AchievementTier.SILVER, 20),
    QUIZ_GOLD(R.string.ach_title_quiz_gold, R.string.ach_desc_quiz_gold, "stars", AchievementCategory.QUIZ, AchievementTier.GOLD, 50),
    QUIZ_PLATINUM(R.string.ach_title_quiz_platinum, R.string.ach_desc_quiz_platinum, "diamond", AchievementCategory.QUIZ, AchievementTier.PLATINUM, 100)
}
