package com.manidigit.yadin.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.manidigit.yadin.domain.model.AchievementCategory
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.AchievementTier
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty

/**
 * قرارداد استاندارد و قابل توسعه مجموعه آیکون‌های تم یادین (Themeable Icon Set Contract).
 *
 * هر تم در یادین می‌تواند یک نمونه از [YadinIconSet] را ارائه دهد و هر یک از آیکون‌ها یا
 * توابع نگاشت موضوعی را متناسب با زبان بصری خود بازنویسی (override) کند.
 */
@Immutable
open class YadinIconSet {

    // =========================================================================
    // ۱. نوع محتوا / حالت مطالعه (ReviewMode)
    // =========================================================================
    /** آزمون ۴ گزینه‌ای: برگه آزمون با چک‌باکس و تیک */
    open val modeQuiz: ImageVector = Icons.Default.FactCheck

    /** فلش‌کارت تعاملی: دو کارت روی هم با زاویه ملایم */
    open val modeFlashcard: ImageVector = Icons.Default.Style

    /** چرخش / پشت‌ورو کردن فلش‌کارت */
    open val flipCard: ImageVector = Icons.Default.Flip

    /** راهنمای هوشمند و نکته آموزشی */
    open val hintLightbulb: ImageVector = Icons.Default.Lightbulb

    /** تلفظ صوتی (TTS) */
    open val speakAudio: ImageVector = Icons.AutoMirrored.Filled.VolumeUp

    open fun forReviewMode(mode: ReviewMode): ImageVector = when (mode) {
        ReviewMode.QUIZ -> modeQuiz
        ReviewMode.FLASHCARD -> modeFlashcard
    }

    // =========================================================================
    // ۲. دوره‌های زمانی لایتنر (Stage) و ۳. وضعیت یادگیری / فیلترهای تمرین (ReviewType)
    // =========================================================================
    /** نماد کلی جعبه لایتنر و سیستم مرور فاصله‌دار */
    open val leitnerBox: ImageVector = Icons.Default.School

    /** روزانه: تقویم تک‌برگی روزانه */
    open val stageDaily: ImageVector = Icons.Default.Today

    /** هفتگی: برگه تقویم هفتگی */
    open val stageWeekly: ImageVector = Icons.Default.DateRange

    /** ماهانه: تقویم شبکه‌ای کامل ماهانه */
    open val stageMonthly: ImageVector = Icons.Default.CalendarMonth

    /** یادگرفته: نشان/مدال ستاره‌دار تیک‌دار رسمی */
    open val stageLearned: ImageVector = Icons.Default.Verified

    /** تصادفی (رندوم): دو فلش متقاطع شافل */
    open val reviewRandom: ImageVector = Icons.Default.Shuffle

    /** تعداد کارت‌های هر جلسه */
    open val sessionCardCount: ImageVector = Icons.Default.FormatListNumbered

    open fun forStage(stage: Stage): ImageVector = when (stage) {
        Stage.DAILY -> stageDaily
        Stage.WEEKLY -> stageWeekly
        Stage.MONTHLY -> stageMonthly
        Stage.LEARNED -> stageLearned
    }

    open fun forReviewType(type: ReviewType): ImageVector = when (type) {
        ReviewType.DAILY -> stageDaily
        ReviewType.WEEKLY -> stageWeekly
        ReviewType.MONTHLY -> stageMonthly
        ReviewType.LEARNED -> stageLearned
        ReviewType.RANDOM -> reviewRandom
        ReviewType.SM2 -> retentionBrain
        ReviewType.FSRS -> brandSparkle
    }

    // =========================================================================
    // ۴. سطح دشواری محتوا (VocabularyDifficulty)
    // =========================================================================
    /** نماد کلی طیف و تنظیمات سطح دشواری */
    open val difficultySpectrum: ImageVector = Icons.Default.Tune

    /** آسان: دایره سبز ساده با تیک */
    open val difficultyEasy: ImageVector = Icons.Default.CheckCircle

    /** متوسط: نماد سنجش نیمه‌پر / دو خط موج‌دار */
    open val difficultyMedium: ImageVector = Icons.Default.Tune

    /** سخت: شعله آتش کوچک */
    open val difficultyHard: ImageVector = Icons.Default.LocalFireDepartment

    /** خیلی سخت: صاعقه پرانرژی */
    open val difficultyVeryHard: ImageVector = Icons.Default.Bolt

    open fun forDifficulty(difficulty: VocabularyDifficulty): ImageVector = when (difficulty) {
        VocabularyDifficulty.EASY -> difficultyEasy
        VocabularyDifficulty.MEDIUM -> difficultyMedium
        VocabularyDifficulty.HARD -> difficultyHard
        VocabularyDifficulty.VERY_HARD -> difficultyVeryHard
    }

    // =========================================================================
    // ۵. سطح مهارت کاربر در آزمون (Proficiency / QuizLevel)
    // =========================================================================
    /** مبتدی: جوانه گیاه دو‌برگی */
    open val proficiencyBeginner: ImageVector = Icons.Default.Eco

    /** نرمال: سپر دفاعی ساده */
    open val proficiencyNormal: ImageVector = Icons.Default.Shield

    /** حرفه‌ای: تاج/نشان ویژه طلایی */
    open val proficiencyProfessional: ImageVector = Icons.Default.WorkspacePremium

    open fun forQuizLevel(level: QuizLevel): ImageVector = when (level) {
        QuizLevel.BEGINNER -> proficiencyBeginner
        QuizLevel.NORMAL -> proficiencyNormal
        QuizLevel.PROFESSIONAL -> proficiencyProfessional
    }

    // =========================================================================
    // ۶. آمار و فیلترهای کلمات (VocabStatType)
    // =========================================================================
    /** کل کلمات: کتابخانه / پشته‌ای از کتاب‌ها */
    open val vocabTotal: ImageVector = Icons.AutoMirrored.Filled.LibraryBooks

    /** تمرین‌شده‌ها: برگه با قلم و تیک تأیید */
    open val vocabPracticed: ImageVector = Icons.Default.AssignmentTurnedIn

    /** تمرین‌نشده‌ها: ساعت شنی دست‌نخورده */
    open val vocabUnpracticed: ImageVector = Icons.Default.HourglassEmpty

    /** یادگرفته‌ها: یکپارچه با مرحله یادگرفته */
    open val vocabLearned: ImageVector
        get() = stageLearned

    // =========================================================================
    // ۷. ناوبری اصلی و جهت‌ها (Navigation & Directional)
    // =========================================================================
    open val navHome: ImageVector = Icons.Default.Home
    open val navReview: ImageVector
        get() = leitnerBox
    open val navLibrary: ImageVector
        get() = vocabTotal
    open val navProgress: ImageVector = Icons.Default.BarChart
    open val navSettings: ImageVector = Icons.Default.Settings

    open val back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    open val forward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    open val dropdown: ImageVector = Icons.Default.ArrowDropDown

    // =========================================================================
    // ۸. آمار پیشرفته، نمودارها و دستاوردها (Analytics, Charts & Gamification)
    // =========================================================================
    /** زنجیره مطالعه روزانه (Streak) */
    open val streakFire: ImageVector = Icons.Default.LocalFireDepartment

    /** شاخص ماندگاری حافظه و تحلیل شناختی */
    open val retentionBrain: ImageVector = Icons.Default.Psychology

    /** نمودار روند مرور روزانه */
    open val chartTrend: ImageVector = Icons.Default.ShowChart

    /** روند رو به رشد */
    open val trendingUp: ImageVector = Icons.AutoMirrored.Filled.TrendingUp

    /** پیش‌بینی مرورهای آینده */
    open val forecastCalendar: ImageVector = Icons.Default.CalendarMonth

    /** کلمه سررسید در انتظار مرور */
    open val dueSchedule: ImageVector = Icons.Default.Schedule

    /** وضعیت مرتب و بدون عقب‌افتادگی */
    open val dueClear: ImageVector = Icons.Default.CheckCircleOutline

    /** تحلیل عادات مطالعه و ساعات روز */
    open val studyHabitsTime: ImageVector = Icons.Default.AccessTime

    /** ساعت طلایی تمرکز */
    open val goldenHourStar: ImageVector = Icons.Default.Star

    /** تسلط به تفکیک موضوعات و دسته‌بندی‌ها */
    open val categoryMastery: ImageVector = Icons.Default.FolderSpecial

    /** جام دستاوردها و جشن پایان جلسه */
    open val achievementTrophy: ImageVector = Icons.Default.EmojiEvents

    /** نشان‌های رتبه‌ای دستاوردها */
    open val tierBronze: ImageVector = Icons.Default.WorkspacePremium
    open val tierSilver: ImageVector = Icons.Default.MilitaryTech
    open val tierGold: ImageVector = Icons.Default.EmojiEvents
    open val tierPlatinum: ImageVector = Icons.Default.Diamond

    /** نماد غلبه بر کلمات سخت */
    open val achievementHardWords: ImageVector = Icons.Default.FitnessCenter

    open fun forAchievementTier(tier: AchievementTier): ImageVector = when (tier) {
        AchievementTier.BRONZE -> tierBronze
        AchievementTier.SILVER -> tierSilver
        AchievementTier.GOLD -> tierGold
        AchievementTier.PLATINUM -> tierPlatinum
    }

    open fun forAchievementCategory(category: AchievementCategory): ImageVector = when (category) {
        AchievementCategory.VOCABULARY -> vocabTotal
        AchievementCategory.STREAK -> streakFire
        AchievementCategory.RETENTION -> retentionBrain
        AchievementCategory.HARD_WORDS -> achievementHardWords
        AchievementCategory.QUIZ -> achievementTrophy
    }

    open fun forAchievement(achievement: AchievementId): ImageVector =
        forAchievementCategory(achievement.category)

    // =========================================================================
    // ۹. دسته‌بندی‌های موضوعی (Categories)
    // =========================================================================
    open val categoryDefault: ImageVector = Icons.Default.Category
    open val categoryBook: ImageVector = Icons.AutoMirrored.Filled.MenuBook
    open val categoryTravel: ImageVector = Icons.Default.Flight
    open val categoryFood: ImageVector = Icons.Default.Restaurant
    open val categoryConversation: ImageVector = Icons.AutoMirrored.Filled.Chat
    open val categoryIdiom: ImageVector
        get() = hintLightbulb
    open val categoryGrammar: ImageVector = Icons.Default.Description
    open val categoryVerb: ImageVector = Icons.Default.Bolt
    open val categoryWork: ImageVector = Icons.Default.Work
    open val categoryNature: ImageVector = Icons.Default.Eco
    open val categoryHome: ImageVector = Icons.Default.Home
    open val categoryShopping: ImageVector = Icons.Default.ShoppingBag
    open val categoryArt: ImageVector = Icons.Default.Palette
    open val categoryMedical: ImageVector = Icons.Default.Favorite
    open val categoryTransport: ImageVector = Icons.Default.DirectionsBus
    open val categoryFamily: ImageVector = Icons.Default.Groups
    open val categorySports: ImageVector = Icons.Default.SportsSoccer
    open val categoryNumbers: ImageVector = Icons.Default.CalendarMonth

    open fun forCategoryName(categoryName: String?): ImageVector {
        if (categoryName == null) return categoryDefault
        val lower = categoryName.lowercase()
        val tokens = lower.split(CATEGORY_TOKEN_SPLIT).filter { it.isNotBlank() }.toSet()
        return when {
            lower.contains("پزشکی") || lower.contains("بدن") || lower.contains("سلامت") || "medical" in tokens || "health" in tokens || "body" in tokens -> categoryMedical
            lower.contains("حمل") || lower.contains("نقل") || "transport" in tokens || "transportation" in tokens -> categoryTransport
            lower.contains("ورزش") || "sport" in tokens || "sports" in tokens -> categorySports
            lower.contains("سفر") || lower.contains("گردش") || "flight" in tokens || "travel" in tokens -> categoryTravel
            lower.contains("غذا") || lower.contains("خوراک") || lower.contains("نوشیدنی") || "food" in tokens -> categoryFood
            lower.contains("مکالمه") || lower.contains("احوال") || lower.contains("گفتگو") || "chat" in tokens -> categoryConversation
            lower.contains("اصطلاح") || lower.contains("ضرب") || "idiom" in tokens || "idioms" in tokens -> categoryIdiom
            lower.contains("گرامر") || lower.contains("قواعد") || lower.contains("دستور") || "grammar" in tokens -> categoryGrammar
            lower.contains("فعل") || lower.contains("افعال") || "verb" in tokens || "verbs" in tokens -> categoryVerb
            "کار" in tokens || lower.contains("شغل") || lower.contains("کسب") || "business" in tokens || "work" in tokens -> categoryWork
            lower.contains("طبیعت") || "آب" in tokens || lower.contains("محیط") || "nature" in tokens -> categoryNature
            lower.contains("خانواده") || "family" in tokens -> categoryFamily
            lower.contains("خانه") || lower.contains("منزل") || "home" in tokens -> categoryHome
            lower.contains("خرید") || lower.contains("پوشاک") || lower.contains("لباس") || "shop" in tokens || "shopping" in tokens -> categoryShopping
            lower.contains("هنر") || lower.contains("فرهنگ") || "art" in tokens || "arts" in tokens -> categoryArt
            lower.contains("اعداد") || lower.contains("تاریخ") || "numbers" in tokens || "date" in tokens -> categoryNumbers
            else -> categoryDefault
        }
    }

    // =========================================================================
    // ۱۰. پشتیبان‌گیری، بازیابی، درون‌ریزی و فرمت فایل‌ها (Backup, Import & Formats)
    // =========================================================================
    open val backupExport: ImageVector = Icons.Default.CloudUpload
    open val backupRestore: ImageVector = Icons.Default.CloudDownload
    open val databaseStorage: ImageVector = Icons.Default.Storage
    open val backupScopeChecklist: ImageVector = Icons.Default.Checklist
    open val processHistory: ImageVector = Icons.Default.History
    open val formatJson: ImageVector = Icons.Default.DataObject
    open val formatCsvExcel: ImageVector = Icons.Default.TableChart
    open val folderOpen: ImageVector = Icons.Default.FolderOpen
    open val fileOpen: ImageVector = Icons.Default.FileOpen
    open val batchImport: ImageVector = Icons.Default.FileUpload
    open val saveFile: ImageVector = Icons.Default.SaveAlt
    open val pasteClipboard: ImageVector = Icons.Default.ContentPaste

    // =========================================================================
    // ۱۱. تنظیمات، زبان، برند و ممیزی هوشمند (Settings, Language & Smart Audit)
    // =========================================================================
    /** نشان برند یادین و تم هوش مصنوعی */
    open val brandSparkle: ImageVector = Icons.Default.AutoAwesome

    /** ممیزی و پالایش هوشمند کلمات */
    open val smartAudit: ImageVector = Icons.Default.AutoFixHigh

    /** بازبینی دستی موارد مشکوک */
    open val manualReview: ImageVector = Icons.Default.RateReview

    open val themePalette: ImageVector = Icons.Default.Palette
    open val lightMode: ImageVector = Icons.Default.LightMode
    open val darkMode: ImageVector = Icons.Default.DarkMode
    open val languageDirection: ImageVector = Icons.Default.Translate
    open val uiLanguage: ImageVector = Icons.Default.Language
    open val purgeTrash: ImageVector = Icons.Default.DeleteForever
    open val aboutInfo: ImageVector = Icons.Default.Info
    open val helpGuide: ImageVector = Icons.AutoMirrored.Filled.HelpOutline
    open val developerCode: ImageVector = Icons.Default.Code
    open val contactSupport: ImageVector = Icons.AutoMirrored.Filled.Chat

    // =========================================================================
    // ۱۲. عملیات عمومی و وضعیت‌ها (Common Actions & Statuses)
    // =========================================================================
    open val add: ImageVector = Icons.Default.Add
    open val edit: ImageVector = Icons.Default.Edit
    open val delete: ImageVector = Icons.Default.Delete
    open val save: ImageVector = Icons.Default.Save
    open val search: ImageVector = Icons.Default.Search
    open val clear: ImageVector = Icons.Default.Clear
    open val close: ImageVector = Icons.Default.Close
    open val check: ImageVector = Icons.Default.Check
    open val refresh: ImageVector = Icons.Default.Refresh
    open val share: ImageVector = Icons.Default.Share
    open val play: ImageVector = Icons.Default.PlayArrow
    open val filter: ImageVector = Icons.Default.FilterList
    open val noteText: ImageVector = Icons.Default.Description

    open val statusSuccess: ImageVector = Icons.Default.CheckCircle
    open val statusError: ImageVector = Icons.Default.Cancel
    open val statusWarning: ImageVector = Icons.Default.Warning

    companion object {
        private val CATEGORY_TOKEN_SPLIT = Regex("[\\s,،/()\\[\\]\\-_•|]+")
    }
}

/**
 * پیاده‌سازی پیش‌فرض مجموعه آیکون‌های متریال یادین.
 */
@Immutable
object DefaultYadinIconSet : YadinIconSet()

/**
 * پل دسترسی پویا به مجموعه آیکون‌های تم فعال برنامه ([YadinIconSet]).
 *
 * این شیء به طور خودکار تمامی درخواست‌های آیکون را به مجموعه آیکون تم فعال ([activeIconSet])
 * هدایت می‌کند؛ بنابراین چه کامپوننت‌ها از `YadinTheme.icons` استفاده کنند و چه مستقیماً
 * `YadinIcons` را فراخوانی نمایند، همیشه آیکون‌های اختصاصی تم انتخاب‌شده را دریافت خواهند کرد.
 */
@Immutable
object YadinIcons : YadinIconSet() {

    @Volatile
    private var activeDelegate: YadinIconSet = DefaultYadinIconSet

    /**
     * بروزرسانی مجموعه آیکون فعال هنگام تغییر تم در [YadinTheme].
     */
    fun setActiveIconSet(iconSet: YadinIconSet) {
        activeDelegate = iconSet
    }

    /**
     * دریافت نمونه [YadinIconSet] فعال کنونی.
     */
    fun currentIconSet(): YadinIconSet = activeDelegate

    override val modeQuiz: ImageVector get() = activeDelegate.modeQuiz
    override val modeFlashcard: ImageVector get() = activeDelegate.modeFlashcard
    override val flipCard: ImageVector get() = activeDelegate.flipCard
    override val hintLightbulb: ImageVector get() = activeDelegate.hintLightbulb
    override val speakAudio: ImageVector get() = activeDelegate.speakAudio
    override fun forReviewMode(mode: ReviewMode): ImageVector = activeDelegate.forReviewMode(mode)

    override val leitnerBox: ImageVector get() = activeDelegate.leitnerBox
    override val stageDaily: ImageVector get() = activeDelegate.stageDaily
    override val stageWeekly: ImageVector get() = activeDelegate.stageWeekly
    override val stageMonthly: ImageVector get() = activeDelegate.stageMonthly
    override val stageLearned: ImageVector get() = activeDelegate.stageLearned
    override val reviewRandom: ImageVector get() = activeDelegate.reviewRandom
    override val sessionCardCount: ImageVector get() = activeDelegate.sessionCardCount
    override fun forStage(stage: Stage): ImageVector = activeDelegate.forStage(stage)
    override fun forReviewType(type: ReviewType): ImageVector = activeDelegate.forReviewType(type)

    override val difficultySpectrum: ImageVector get() = activeDelegate.difficultySpectrum
    override val difficultyEasy: ImageVector get() = activeDelegate.difficultyEasy
    override val difficultyMedium: ImageVector get() = activeDelegate.difficultyMedium
    override val difficultyHard: ImageVector get() = activeDelegate.difficultyHard
    override val difficultyVeryHard: ImageVector get() = activeDelegate.difficultyVeryHard
    override fun forDifficulty(difficulty: VocabularyDifficulty): ImageVector = activeDelegate.forDifficulty(difficulty)

    override val proficiencyBeginner: ImageVector get() = activeDelegate.proficiencyBeginner
    override val proficiencyNormal: ImageVector get() = activeDelegate.proficiencyNormal
    override val proficiencyProfessional: ImageVector get() = activeDelegate.proficiencyProfessional
    override fun forQuizLevel(level: QuizLevel): ImageVector = activeDelegate.forQuizLevel(level)

    override val vocabTotal: ImageVector get() = activeDelegate.vocabTotal
    override val vocabPracticed: ImageVector get() = activeDelegate.vocabPracticed
    override val vocabUnpracticed: ImageVector get() = activeDelegate.vocabUnpracticed
    override val vocabLearned: ImageVector get() = activeDelegate.vocabLearned

    override val navHome: ImageVector get() = activeDelegate.navHome
    override val navReview: ImageVector get() = activeDelegate.navReview
    override val navLibrary: ImageVector get() = activeDelegate.navLibrary
    override val navProgress: ImageVector get() = activeDelegate.navProgress
    override val navSettings: ImageVector get() = activeDelegate.navSettings
    override val back: ImageVector get() = activeDelegate.back
    override val forward: ImageVector get() = activeDelegate.forward
    override val dropdown: ImageVector get() = activeDelegate.dropdown

    override val streakFire: ImageVector get() = activeDelegate.streakFire
    override val retentionBrain: ImageVector get() = activeDelegate.retentionBrain
    override val chartTrend: ImageVector get() = activeDelegate.chartTrend
    override val trendingUp: ImageVector get() = activeDelegate.trendingUp
    override val forecastCalendar: ImageVector get() = activeDelegate.forecastCalendar
    override val dueSchedule: ImageVector get() = activeDelegate.dueSchedule
    override val dueClear: ImageVector get() = activeDelegate.dueClear
    override val studyHabitsTime: ImageVector get() = activeDelegate.studyHabitsTime
    override val goldenHourStar: ImageVector get() = activeDelegate.goldenHourStar
    override val categoryMastery: ImageVector get() = activeDelegate.categoryMastery
    override val achievementTrophy: ImageVector get() = activeDelegate.achievementTrophy
    override val tierBronze: ImageVector get() = activeDelegate.tierBronze
    override val tierSilver: ImageVector get() = activeDelegate.tierSilver
    override val tierGold: ImageVector get() = activeDelegate.tierGold
    override val tierPlatinum: ImageVector get() = activeDelegate.tierPlatinum
    override val achievementHardWords: ImageVector get() = activeDelegate.achievementHardWords
    override fun forAchievementTier(tier: AchievementTier): ImageVector = activeDelegate.forAchievementTier(tier)
    override fun forAchievementCategory(category: AchievementCategory): ImageVector = activeDelegate.forAchievementCategory(category)
    override fun forAchievement(achievement: AchievementId): ImageVector = activeDelegate.forAchievement(achievement)

    override val categoryDefault: ImageVector get() = activeDelegate.categoryDefault
    override val categoryBook: ImageVector get() = activeDelegate.categoryBook
    override val categoryTravel: ImageVector get() = activeDelegate.categoryTravel
    override val categoryFood: ImageVector get() = activeDelegate.categoryFood
    override val categoryConversation: ImageVector get() = activeDelegate.categoryConversation
    override val categoryIdiom: ImageVector get() = activeDelegate.categoryIdiom
    override val categoryGrammar: ImageVector get() = activeDelegate.categoryGrammar
    override val categoryVerb: ImageVector get() = activeDelegate.categoryVerb
    override val categoryWork: ImageVector get() = activeDelegate.categoryWork
    override val categoryNature: ImageVector get() = activeDelegate.categoryNature
    override val categoryHome: ImageVector get() = activeDelegate.categoryHome
    override val categoryShopping: ImageVector get() = activeDelegate.categoryShopping
    override val categoryArt: ImageVector get() = activeDelegate.categoryArt
    override val categoryMedical: ImageVector get() = activeDelegate.categoryMedical
    override val categoryTransport: ImageVector get() = activeDelegate.categoryTransport
    override val categoryFamily: ImageVector get() = activeDelegate.categoryFamily
    override val categorySports: ImageVector get() = activeDelegate.categorySports
    override val categoryNumbers: ImageVector get() = activeDelegate.categoryNumbers
    override fun forCategoryName(categoryName: String?): ImageVector = activeDelegate.forCategoryName(categoryName)

    override val backupExport: ImageVector get() = activeDelegate.backupExport
    override val backupRestore: ImageVector get() = activeDelegate.backupRestore
    override val databaseStorage: ImageVector get() = activeDelegate.databaseStorage
    override val backupScopeChecklist: ImageVector get() = activeDelegate.backupScopeChecklist
    override val processHistory: ImageVector get() = activeDelegate.processHistory
    override val formatJson: ImageVector get() = activeDelegate.formatJson
    override val formatCsvExcel: ImageVector get() = activeDelegate.formatCsvExcel
    override val folderOpen: ImageVector get() = activeDelegate.folderOpen
    override val fileOpen: ImageVector get() = activeDelegate.fileOpen
    override val batchImport: ImageVector get() = activeDelegate.batchImport
    override val saveFile: ImageVector get() = activeDelegate.saveFile
    override val pasteClipboard: ImageVector get() = activeDelegate.pasteClipboard

    override val brandSparkle: ImageVector get() = activeDelegate.brandSparkle
    override val smartAudit: ImageVector get() = activeDelegate.smartAudit
    override val manualReview: ImageVector get() = activeDelegate.manualReview
    override val themePalette: ImageVector get() = activeDelegate.themePalette
    override val lightMode: ImageVector get() = activeDelegate.lightMode
    override val darkMode: ImageVector get() = activeDelegate.darkMode
    override val languageDirection: ImageVector get() = activeDelegate.languageDirection
    override val uiLanguage: ImageVector get() = activeDelegate.uiLanguage
    override val purgeTrash: ImageVector get() = activeDelegate.purgeTrash
    override val aboutInfo: ImageVector get() = activeDelegate.aboutInfo
    override val helpGuide: ImageVector get() = activeDelegate.helpGuide
    override val developerCode: ImageVector get() = activeDelegate.developerCode
    override val contactSupport: ImageVector get() = activeDelegate.contactSupport

    override val add: ImageVector get() = activeDelegate.add
    override val edit: ImageVector get() = activeDelegate.edit
    override val delete: ImageVector get() = activeDelegate.delete
    override val save: ImageVector get() = activeDelegate.save
    override val search: ImageVector get() = activeDelegate.search
    override val clear: ImageVector get() = activeDelegate.clear
    override val close: ImageVector get() = activeDelegate.close
    override val check: ImageVector get() = activeDelegate.check
    override val refresh: ImageVector get() = activeDelegate.refresh
    override val share: ImageVector get() = activeDelegate.share
    override val play: ImageVector get() = activeDelegate.play
    override val filter: ImageVector get() = activeDelegate.filter
    override val noteText: ImageVector get() = activeDelegate.noteText

    override val statusSuccess: ImageVector get() = activeDelegate.statusSuccess
    override val statusError: ImageVector get() = activeDelegate.statusError
    override val statusWarning: ImageVector get() = activeDelegate.statusWarning
}
