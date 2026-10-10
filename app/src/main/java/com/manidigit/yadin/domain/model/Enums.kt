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
    val titleRes: String,
    val descriptionRes: String,
    val iconName: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val threshold: Int
) {
    // ۱. دایره واژگان (Vocabulary Footprint)
    FIRST_TEN_WORDS("آشنایی با واژگان", "۱۰ واژه را تمرین کنید", "school", AchievementCategory.VOCABULARY, AchievementTier.BRONZE, 10),
    VOCABULARY_BUILDER("سازنده واژگان", "۵۰ واژه را با موفقیت تمرین کنید", "auto_stories", AchievementCategory.VOCABULARY, AchievementTier.SILVER, 50),
    VOCABULARY_MASTER("خزانه‌دار لغات", "۲۰۰ واژه را در چرخه یادگیری تمرین کنید", "library_books", AchievementCategory.VOCABULARY, AchievementTier.GOLD, 200),
    VOCABULARY_LEGEND("فرهنگ لغت متحرک", "۱,۰۰۰ واژه را وارد تمرین‌های خود کنید", "menu_book", AchievementCategory.VOCABULARY, AchievementTier.PLATINUM, 1000),

    // ۲. رگبار و مداومت (Streak Fire)
    STREAK_3_DAYS("جرقه مداومت", "۳ روز متوالی مرور منظم داشته باشید", "local_fire_department", AchievementCategory.STREAK, AchievementTier.BRONZE, 3),
    STREAK_7_DAYS("شعله هفتگی", "۷ روز متوالی (یک هفته کامل) مرور کنید", "whatshot", AchievementCategory.STREAK, AchievementTier.SILVER, 7),
    STREAK_30_DAYS("قهرمان مداومت", "۳۰ روز متوالی مرور بدون وقفه داشته باشید", "military_tech", AchievementCategory.STREAK, AchievementTier.GOLD, 30),
    STREAK_100_DAYS("اراده پولادین", "۱۰۰ روز متوالی پرچم یادگیری را بالا نگه دارید", "diamond", AchievementCategory.STREAK, AchievementTier.PLATINUM, 100),

    // ۳. تثبیت لایتنر و حافظه دائمی (Deep Retention)
    LONG_TERM_MEMORY("اولین جوانه‌های تثبیت", "۵ واژه را به مرحله تثبیت دائمی (یادگرفته) برسانید", "neurology", AchievementCategory.RETENTION, AchievementTier.BRONZE, 5),
    RETENTION_SILVER("ستون‌های حافظه", "۲۵ واژه را به مرحله تثبیت دائمی برسانید", "psychology", AchievementCategory.RETENTION, AchievementTier.SILVER, 25),
    RETENTION_GOLD("گنجینه پایدار", "۱۰۰ واژه را به مرحله یادگرفته‌شده برسانید", "archive", AchievementCategory.RETENTION, AchievementTier.GOLD, 100),
    RETENTION_PLATINUM("استاد ماندگاری", "۵۰۰ واژه را در حافظه بلندمدت حک کنید", "workspace_premium", AchievementCategory.RETENTION, AchievementTier.PLATINUM, 500),

    // ۴. واژگان سخت و چالش‌برانگیز (Hard Words Conqueror)
    HARD_MASTER("شکارچی واژه‌های سخت", "۳ واژه خیلی‌سخت را رام کرده و یاد بگیرید", "fitness_center", AchievementCategory.HARD_WORDS, AchievementTier.BRONZE, 3),
    HARD_SILVER("استاد چالش‌ها", "۱۰ واژه در سطح خیلی‌سخت را تسلیم کنید", "construction", AchievementCategory.HARD_WORDS, AchievementTier.SILVER, 10),
    HARD_GOLD("حریف کلمات دشوار", "۳۰ واژه خیلی‌سخت را مهار کنید", "psychology", AchievementCategory.HARD_WORDS, AchievementTier.GOLD, 30),
    HARD_PLATINUM("شکست‌ناپذیر", "۱۰۰ واژه با بالاترین درجه سختی را فتح کنید", "shield", AchievementCategory.HARD_WORDS, AchievementTier.PLATINUM, 100),

    // ۵. مهارت و تسلط بر کوییز (Quiz Mastery)
    QUIZ_ACE("دقت در آزمون", "در یک آزمون ۵ سؤالی نمره کامل ۱۰۰٪ بگیرید", "grade", AchievementCategory.QUIZ, AchievementTier.BRONZE, 5),
    QUIZ_SILVER("نخبه آزمون", "در یک آزمون ۱۰ سؤالی نمره کامل ۱۰۰٪ بگیرید", "emoji_events", AchievementCategory.QUIZ, AchievementTier.SILVER, 10),
    QUIZ_GOLD("استاد کوییزهای سخت", "در یک آزمون ۲۰ سؤالی نمره کامل ۱۰۰٪ بگیرید", "stars", AchievementCategory.QUIZ, AchievementTier.GOLD, 20),
    QUIZ_PLATINUM("افسانه سنجش", "در آزمون ۳۰ سؤالی یا بالاتر نمره کامل ۱۰۰٪ کسب کنید", "diamond", AchievementCategory.QUIZ, AchievementTier.PLATINUM, 30)
}
