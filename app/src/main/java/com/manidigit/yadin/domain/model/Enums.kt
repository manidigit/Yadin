package com.manidigit.yadin.domain.model

enum class EntryType {
    WORD,
    PHRASE,
    SENTENCE,
    IDIOM,
    COLLOCATION,
    STRUCTURE
}

enum class CardDirection {
    NORMAL,  // Source -> Target (e.g. Spanish -> Persian)
    REVERSE  // Target -> Source (e.g. Persian -> Spanish)
}

enum class Stage {
    DAILY,
    WEEKLY,
    MONTHLY,
    LEARNED
}

enum class VocabularyDifficulty {
    EASY,
    MEDIUM,
    HARD,
    VERY_HARD
}

enum class ReviewType {
    DAILY,
    WEEKLY,
    MONTHLY,
    LEARNED,
    RANDOM
}

enum class ReviewMode {
    FLASHCARD,
    QUIZ
}

enum class QuizLevel {
    EASY,
    MEDIUM,
    HARD
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
    val titleRes: String,
    val descriptionRes: String,
    val iconName: String
) {
    FIRST_TEN_WORDS("اولین ده واژه", "۱۰ واژه را به خاطر بسپارید", "school"),
    VOCABULARY_BUILDER("سازنده واژگان", "۵۰ واژه را با موفقیت تمرین کنید", "auto_stories"),
    STREAK_3_DAYS("رگبار ۳ روزه", "۳ روز متوالی تمرین کنید", "local_fire_department"),
    STREAK_7_DAYS("رگبار ۷ روزه", "یک هفته متوالی واژگان را مرور کنید", "whatshot"),
    STREAK_30_DAYS("قهرمان مداومت", "۳۰ روز متوالی مرور منظم داشته باشید", "military_tech"),
    HARD_MASTER("استاد واژگان سخت", "۵ واژه در سطح خیلی‌سخت را یاد بگیرید", "psychology"),
    LONG_TERM_MEMORY("حافظه بلندمدت", "۲۰ واژه را به مرحله یادگرفته‌شده برسانید", "neurology"),
    QUIZ_ACE("نخبه آزمون", "در یک آزمون ۱۰ سؤالی نمره کامل بگیرید", "emoji_events")
}
