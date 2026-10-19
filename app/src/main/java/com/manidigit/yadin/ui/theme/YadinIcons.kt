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
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty

/**
 * مخزن استاندارد و متمرکز تمام آیکون‌های تم یادین بر پایه Material Icons.
 * تمامی صفحات و کامپوننت‌ها باید آیکون‌های خود را از این مرجع دریافت کنند تا یکدستی بصری
 * در کل برنامه تضمین شود.
 */
@Immutable
object YadinIcons {

    // =========================================================================
    // ۱. نوع محتوا / حالت مطالعه (ReviewMode)
    // =========================================================================
    /** آزمون ۴ گزینه‌ای: برگه آزمون با چک‌باکس و تیک */
    val modeQuiz: ImageVector = Icons.Default.FactCheck

    /** فلش‌کارت تعاملی: دو کارت روی هم با زاویه ملایم */
    val modeFlashcard: ImageVector = Icons.Default.Style

    /** چرخش / پشت‌ورو کردن فلش‌کارت */
    val flipCard: ImageVector = Icons.Default.Flip

    /** راهنمای هوشمند و نکته آموزشی */
    val hintLightbulb: ImageVector = Icons.Default.Lightbulb

    /** تلفظ صوتی (TTS) */
    val speakAudio: ImageVector = Icons.AutoMirrored.Filled.VolumeUp

    fun forReviewMode(mode: ReviewMode): ImageVector = when (mode) {
        ReviewMode.QUIZ -> modeQuiz
        ReviewMode.FLASHCARD -> modeFlashcard
    }

    // =========================================================================
    // ۲. دوره‌های زمانی لایتنر (Stage) و ۳. وضعیت یادگیری / فیلترهای تمرین (ReviewType)
    // =========================================================================
    /** نماد کلی جعبه لایتنر و سیستم مرور فاصله‌دار */
    val leitnerBox: ImageVector = Icons.Default.School

    /** روزانه: تقویم تک‌برگی روزانه */
    val stageDaily: ImageVector = Icons.Default.Today

    /** هفتگی: برگه تقویم هفتگی */
    val stageWeekly: ImageVector = Icons.Default.DateRange

    /** ماهانه: تقویم شبکه‌ای کامل ماهانه */
    val stageMonthly: ImageVector = Icons.Default.CalendarMonth

    /** یادگرفته: نشان/مدال ستاره‌دار تیک‌دار رسمی */
    val stageLearned: ImageVector = Icons.Default.Verified

    /** تصادفی (رندوم): دو فلش متقاطع شافل */
    val reviewRandom: ImageVector = Icons.Default.Shuffle

    /** تعداد کارت‌های هر جلسه */
    val sessionCardCount: ImageVector = Icons.Default.FormatListNumbered

    fun forStage(stage: Stage): ImageVector = when (stage) {
        Stage.DAILY -> stageDaily
        Stage.WEEKLY -> stageWeekly
        Stage.MONTHLY -> stageMonthly
        Stage.LEARNED -> stageLearned
    }

    fun forReviewType(type: ReviewType): ImageVector = when (type) {
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
    val difficultySpectrum: ImageVector = Icons.Default.Tune

    /** آسان: دایره سبز ساده با تیک */
    val difficultyEasy: ImageVector = Icons.Default.CheckCircle

    /** متوسط: نماد سنجش نیمه‌پر / دو خط موج‌دار */
    val difficultyMedium: ImageVector = Icons.Default.Tune

    /** سخت: شعله آتش کوچک */
    val difficultyHard: ImageVector = Icons.Default.LocalFireDepartment

    /** خیلی سخت: صاعقه پرانرژی */
    val difficultyVeryHard: ImageVector = Icons.Default.Bolt

    fun forDifficulty(difficulty: VocabularyDifficulty): ImageVector = when (difficulty) {
        VocabularyDifficulty.EASY -> difficultyEasy
        VocabularyDifficulty.MEDIUM -> difficultyMedium
        VocabularyDifficulty.HARD -> difficultyHard
        VocabularyDifficulty.VERY_HARD -> difficultyVeryHard
    }

    // =========================================================================
    // ۵. سطح مهارت کاربر در آزمون (Proficiency / QuizLevel)
    // =========================================================================
    /** مبتدی: جوانه گیاه دو‌برگی */
    val proficiencyBeginner: ImageVector = Icons.Default.Eco

    /** نرمال: سپر دفاعی ساده */
    val proficiencyNormal: ImageVector = Icons.Default.Shield

    /** حرفه‌ای: تاج/نشان ویژه طلایی */
    val proficiencyProfessional: ImageVector = Icons.Default.WorkspacePremium

    fun forQuizLevel(level: QuizLevel): ImageVector = when (level) {
        QuizLevel.BEGINNER -> proficiencyBeginner
        QuizLevel.NORMAL -> proficiencyNormal
        QuizLevel.PROFESSIONAL -> proficiencyProfessional
    }

    // =========================================================================
    // ۶. آمار و فیلترهای کلمات (VocabStatType)
    // =========================================================================
    /** کل کلمات: کتابخانه / پشته‌ای از کتاب‌ها */
    val vocabTotal: ImageVector = Icons.AutoMirrored.Filled.LibraryBooks

    /** تمرین‌شده‌ها: برگه با قلم و تیک تأیید */
    val vocabPracticed: ImageVector = Icons.Default.AssignmentTurnedIn

    /** تمرین‌نشده‌ها: ساعت شنی دست‌نخورده */
    val vocabUnpracticed: ImageVector = Icons.Default.HourglassEmpty

    /** یادگرفته‌ها: یکپارچه با مرحله یادگرفته */
    val vocabLearned: ImageVector = stageLearned

    // =========================================================================
    // ۷. ناوبری اصلی و جهت‌ها (Navigation & Directional)
    // =========================================================================
    val navHome: ImageVector = Icons.Default.Home
    val navReview: ImageVector = leitnerBox
    val navLibrary: ImageVector = vocabTotal
    val navProgress: ImageVector = Icons.Default.BarChart
    val navSettings: ImageVector = Icons.Default.Settings

    val back: ImageVector = Icons.AutoMirrored.Filled.ArrowBack
    val forward: ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    val dropdown: ImageVector = Icons.Default.ArrowDropDown

    // =========================================================================
    // ۸. آمار پیشرفته، نمودارها و دستاوردها (Analytics, Charts & Gamification)
    // =========================================================================
    /** زنجیره مطالعه روزانه (Streak) */
    val streakFire: ImageVector = Icons.Default.LocalFireDepartment

    /** شاخص ماندگاری حافظه و تحلیل شناختی */
    val retentionBrain: ImageVector = Icons.Default.Psychology

    /** نمودار روند مرور روزانه */
    val chartTrend: ImageVector = Icons.Default.ShowChart

    /** روند رو به رشد */
    val trendingUp: ImageVector = Icons.AutoMirrored.Filled.TrendingUp

    /** پیش‌بینی مرورهای آینده */
    val forecastCalendar: ImageVector = Icons.Default.CalendarMonth

    /** کلمه سررسید در انتظار مرور */
    val dueSchedule: ImageVector = Icons.Default.Schedule

    /** وضعیت مرتب و بدون عقب‌افتادگی */
    val dueClear: ImageVector = Icons.Default.CheckCircleOutline

    /** تحلیل عادات مطالعه و ساعات روز */
    val studyHabitsTime: ImageVector = Icons.Default.AccessTime

    /** ساعت طلایی تمرکز */
    val goldenHourStar: ImageVector = Icons.Default.Star

    /** تسلط به تفکیک موضوعات و دسته‌بندی‌ها */
    val categoryMastery: ImageVector = Icons.Default.FolderSpecial

    /** جام دستاوردها و جشن پایان جلسه */
    val achievementTrophy: ImageVector = Icons.Default.EmojiEvents

    // =========================================================================
    // ۹. دسته‌بندی‌های موضوعی (Categories)
    // =========================================================================
    val categoryDefault: ImageVector = Icons.Default.Category
    val categoryBook: ImageVector = Icons.AutoMirrored.Filled.MenuBook

    fun forCategoryName(categoryName: String?): ImageVector {
        if (categoryName == null) return categoryDefault
        val lower = categoryName.lowercase()
        return when {
            lower.contains("سفر") || lower.contains("گردش") || lower.contains("flight") || lower.contains("travel") -> Icons.Default.Flight
            lower.contains("غذا") || lower.contains("خوراک") || lower.contains("نوشیدنی") || lower.contains("food") -> Icons.Default.Restaurant
            lower.contains("مکالمه") || lower.contains("احوال") || lower.contains("گفتگو") || lower.contains("chat") -> Icons.AutoMirrored.Filled.Chat
            lower.contains("اصطلاح") || lower.contains("ضرب") || lower.contains("idiom") -> Icons.Default.Lightbulb
            lower.contains("گرامر") || lower.contains("قواعد") || lower.contains("دستور") || lower.contains("grammar") -> Icons.Default.Description
            lower.contains("فعل") || lower.contains("افعال") || lower.contains("verb") -> Icons.Default.Bolt
            lower.contains("کار") || lower.contains("شغل") || lower.contains("کسب") || lower.contains("business") -> Icons.Default.Work
            lower.contains("طبیعت") || lower.contains("آب") || lower.contains("محیط") || lower.contains("nature") -> Icons.Default.Eco
            lower.contains("خانه") || lower.contains("منزل") || lower.contains("خانواده") || lower.contains("home") -> Icons.Default.Home
            lower.contains("خرید") || lower.contains("پوشاک") || lower.contains("لباس") || lower.contains("shop") -> Icons.Default.ShoppingBag
            lower.contains("هنر") || lower.contains("فرهنگ") || lower.contains("art") -> Icons.Default.Palette
            else -> categoryDefault
        }
    }

    // =========================================================================
    // ۱۰. پشتیبان‌گیری، بازیابی، درون‌ریزی و فرمت فایل‌ها (Backup, Import & Formats)
    // =========================================================================
    val backupExport: ImageVector = Icons.Default.CloudUpload
    val backupRestore: ImageVector = Icons.Default.CloudDownload
    val databaseStorage: ImageVector = Icons.Default.Storage
    val backupScopeChecklist: ImageVector = Icons.Default.Checklist
    val processHistory: ImageVector = Icons.Default.History
    val formatJson: ImageVector = Icons.Default.DataObject
    val formatCsvExcel: ImageVector = Icons.Default.TableChart
    val folderOpen: ImageVector = Icons.Default.FolderOpen
    val fileOpen: ImageVector = Icons.Default.FileOpen
    val batchImport: ImageVector = Icons.Default.FileUpload
    val saveFile: ImageVector = Icons.Default.SaveAlt
    val pasteClipboard: ImageVector = Icons.Default.ContentPaste

    // =========================================================================
    // ۱۱. تنظیمات، زبان، برند و ممیزی هوشمند (Settings, Language & Smart Audit)
    // =========================================================================
    /** نشان برند یادین و تم هوش مصنوعی Gemini */
    val brandSparkle: ImageVector = Icons.Default.AutoAwesome

    /** ممیزی و پالایش هوشمند کلمات */
    val smartAudit: ImageVector = Icons.Default.AutoFixHigh

    /** بازبینی دستی موارد مشکوک */
    val manualReview: ImageVector = Icons.Default.RateReview

    val themePalette: ImageVector = Icons.Default.Palette
    val lightMode: ImageVector = Icons.Default.LightMode
    val darkMode: ImageVector = Icons.Default.DarkMode
    val languageDirection: ImageVector = Icons.Default.Translate
    val uiLanguage: ImageVector = Icons.Default.Language
    val purgeTrash: ImageVector = Icons.Default.DeleteForever
    val aboutInfo: ImageVector = Icons.Default.Info
    val helpGuide: ImageVector = Icons.AutoMirrored.Filled.HelpOutline
    val developerCode: ImageVector = Icons.Default.Code
    val contactSupport: ImageVector = Icons.AutoMirrored.Filled.Chat

    // =========================================================================
    // ۱۲. عملیات عمومی و وضعیت‌ها (Common Actions & Statuses)
    // =========================================================================
    val add: ImageVector = Icons.Default.Add
    val edit: ImageVector = Icons.Default.Edit
    val delete: ImageVector = Icons.Default.Delete
    val save: ImageVector = Icons.Default.Save
    val search: ImageVector = Icons.Default.Search
    val clear: ImageVector = Icons.Default.Clear
    val close: ImageVector = Icons.Default.Close
    val check: ImageVector = Icons.Default.Check
    val refresh: ImageVector = Icons.Default.Refresh
    val share: ImageVector = Icons.Default.Share
    val play: ImageVector = Icons.Default.PlayArrow
    val filter: ImageVector = Icons.Default.FilterList
    val noteText: ImageVector = Icons.Default.Description

    val statusSuccess: ImageVector = Icons.Default.CheckCircle
    val statusError: ImageVector = Icons.Default.Cancel
    val statusWarning: ImageVector = Icons.Default.Warning
}
