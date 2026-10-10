package com.manidigit.yadin.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.data.local.database.YadinDatabase
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.repository.BackupExportFormat
import com.manidigit.yadin.data.repository.BackupOptions
import com.manidigit.yadin.data.repository.BackupRepository
import com.manidigit.yadin.data.repository.BackupType
import com.manidigit.yadin.data.repository.ReviewFilters
import com.manidigit.yadin.data.repository.ReviewRepository
import com.manidigit.yadin.data.repository.SeedImporter
import com.manidigit.yadin.data.repository.SettingsRepository
import com.manidigit.yadin.data.repository.VocabularyRepository
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.ImportSummary
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.QuizQuestion
import com.manidigit.yadin.domain.model.ReviewCard
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewSession
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.model.WordDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * نمایشگرهای مختلف برنامه در ساختار درختی ناوبری
 */
sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    data class ReviewSetup(
        val initialType: ReviewType = ReviewType.RANDOM,
        val initialDifficulties: Set<VocabularyDifficulty> = emptySet()
    ) : Screen()
    object Flashcard : Screen()
    object Quiz : Screen()
    object SessionSummary : Screen()
    object Library : Screen()
    data class WordDetailScreen(val conceptId: String) : Screen()
    data class EditWordScreen(val conceptId: String? = null) : Screen()
    object ImportPreview : Screen()
    object ProgressStats : Screen()
    object Settings : Screen()
    object Backup : Screen()
    object Help : Screen()
    object About : Screen()
}

/**
 * هماهنگ‌کننده اصلی و نمای بیرونی معماری (Composite Facade & Root ViewModel)
 *
 * حل مسئله ساختاری ISS-27 (شکستن God ViewModel به ماژول‌های مجزا):
 * این ویومدل به عنوان هماهنگ‌کننده ارشد (Root Coordinator) عمل کرده و مسئولیت‌های تخصصی را
 * به ۴ ویومدل/نماینده زیر تفویض می‌کند تا اصل مسئولیت واحد (Single Responsibility Principle)
 * و پایداری در تست‌ها حفظ شود:
 * 1. [ReviewViewModel]: چرخه حیات آزمون و فلش‌کارت، نمره‌دهی و محاسبات نشست‌ها.
 * 2. [LibraryViewModel]: جستجو، فیلترها، مدیریت واژگان (CRUD) و فرآیند ورود گروهی (Import).
 * 3. [StatisticsViewModel]: آمار سررسید، مراحل لایتنر، دستاوردها و نمودار فعالیت روزانه.
 * 4. [SettingsViewModel]: مدیریت اولویت‌ها، تم، جهت زبان و فرآیند پشتیبان‌گیری و بازیابی.
 *
 * حل نقص بحرانی ISS-16:
 * ارزیابی پاسخ‌های آزمون ۴گزینه‌ای به طور قطعی بر اساس انطباق شاخص (`optionIndex == q.correctIndex`)
 * انجام شده و شرط برخورد معنایی کاذب که باعث ارتقای اشتباه کارت‌های پاسخ‌داده‌شده می‌شد حذف گردیده است.
 *
 * حل نقص بحرانی ISS-22:
 * تمامی استثناهای کورتین در عملیات حیاتی (مانند ذخیره کلمه، ورود گروهی و ثبت پاسخ) با مدیریت کامل
 * و بلوک‌های try/catch/finally کنترل شده و وضعیت رابط کاربری هرگز قفل نمی‌شود.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- زیرساخت داده و مخازن ---
    private val db = YadinDatabase.getInstance(application)
    private val settingsRepo = SettingsRepository(db.settingsDao())
    val vocabularyRepo = VocabularyRepository(db, db.conceptDao(), db.learningDao(), db.reviewSessionDao())
    val reviewRepo = ReviewRepository(db.conceptDao(), db.learningDao(), db.reviewSessionDao(), db.achievementDao(), db)
    private val seedImporter = SeedImporter(application, db, db.conceptDao(), db.learningDao(), db.achievementDao())
    val backupRepo = BackupRepository(
        application,
        db,
        db.conceptDao(),
        db.learningDao(),
        db.reviewSessionDao(),
        db.settingsDao(),
        db.achievementDao()
    )

    // --- زیر-ویومدل‌های تفکیک‌شده طبق اصول SOLID (ISS-27) ---
    val reviewVm = ReviewViewModel(reviewRepo, viewModelScope)
    val libraryVm = LibraryViewModel(vocabularyRepo, viewModelScope)
    val statisticsVm = StatisticsViewModel(vocabularyRepo, reviewRepo, viewModelScope)
    val settingsVm = SettingsViewModel(application, settingsRepo, backupRepo, viewModelScope)

    // ==========================================
    // ناوبری و وضعیت صفحات
    // ==========================================
    private val _screenStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val screenStack: StateFlow<List<Screen>> = _screenStack.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // ==========================================
    // بارگذاری اولیه و تزریق بذر داده‌ها
    // ==========================================
    private val _isSeeding = MutableStateFlow(true)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    private val _seedProgress = MutableStateFlow(0f)
    val seedProgress: StateFlow<Float> = _seedProgress.asStateFlow()

    private val _seedMessage = MutableStateFlow("در حال بررسی بانک واژگان...")
    val seedMessage: StateFlow<String> = _seedMessage.asStateFlow()

    // ==========================================
    // تفویض وضعیت تنظیمات (SettingsViewModel)
    // ==========================================
    val themeId: StateFlow<String> get() = settingsVm.themeId
    val isDark: StateFlow<Boolean> get() = settingsVm.isDark
    val activePair: StateFlow<String> get() = settingsVm.activePair
    val uiLanguage: StateFlow<String> get() = settingsVm.uiLanguage
    val difficultyThreshold: StateFlow<Int> get() = settingsVm.difficultyThreshold
    val showCategoryInReview: StateFlow<Boolean> get() = settingsVm.showCategoryInReview
    val appLanguageDirection: StateFlow<CardDirection> get() = settingsVm.appLanguageDirection
    val ttsEnabled: StateFlow<Boolean> get() = settingsVm.ttsEnabled
    val ttsAutoPlay: StateFlow<Boolean> get() = settingsVm.ttsAutoPlay
    val ttsSpeechRate: StateFlow<Float> get() = settingsVm.ttsSpeechRate
    val quizAutoAdvanceSeconds: StateFlow<Int> get() = settingsVm.quizAutoAdvanceSeconds
    val defaultQuizLevel: StateFlow<QuizLevel> get() = settingsVm.defaultQuizLevel
    val defaultReviewMode: StateFlow<ReviewMode> get() = settingsVm.defaultReviewMode

    // ==========================================
    // تفویض وضعیت آمار و پیشرفت (StatisticsViewModel)
    // ==========================================
    val dueCount: StateFlow<Int> = statisticsVm.getDueCountFlow(settingsVm.appLanguageDirection)
    val statistics: StateFlow<StatisticsSummary> = statisticsVm.getStatisticsFlow(settingsVm.appLanguageDirection)
    val difficultyCounts: StateFlow<Map<VocabularyDifficulty, Int>> = statisticsVm.getDifficultyCountsFlow(settingsVm.appLanguageDirection)

    val progressDirection: StateFlow<CardDirection> get() = settingsVm.appLanguageDirection
    val progressScorePercent: StateFlow<Double> = statisticsVm.getProgressScoreFlow(settingsVm.appLanguageDirection)
    val dailyStats: StateFlow<List<DayCountRaw>> get() = statisticsVm.dailyStats
    val practicedWordsCount: StateFlow<Int> = statisticsVm.getPracticedWordsCountFlow(settingsVm.appLanguageDirection)
    val progressStatistics: StateFlow<StatisticsSummary> = statisticsVm.getProgressStatisticsFlow(settingsVm.appLanguageDirection)
    val achievements: StateFlow<List<AchievementEntity>> get() = statisticsVm.achievements
    val totalCorrectReviewsCount: StateFlow<Int> = statisticsVm.totalCorrectReviewsCount

    // ==========================================
    // تفویض وضعیت کتابخانه و ورود (LibraryViewModel)
    // ==========================================
    val categories: StateFlow<List<Category>> get() = libraryVm.categories
    val searchQuery: StateFlow<String> get() = libraryVm.searchQuery
    val selectedCategoryFilter: StateFlow<String?> get() = libraryVm.selectedCategoryFilter
    val selectedStageFilter: StateFlow<Stage?> get() = libraryVm.selectedStageFilter
    val selectedDifficultyFilter: StateFlow<VocabularyDifficulty?> get() = libraryVm.selectedDifficultyFilter
    val showOnlyInactiveFilter: StateFlow<Boolean> get() = libraryVm.showOnlyInactiveFilter
    val hasMoreResults: StateFlow<Boolean> get() = libraryVm.hasMoreResults
    val isLoadingMore: StateFlow<Boolean> get() = libraryVm.isLoadingMore
    val searchResults: StateFlow<List<WordDetail>> get() = libraryVm.searchResults
    val selectedWordDetail: StateFlow<WordDetail?> get() = libraryVm.selectedWordDetail
    val parseResult: StateFlow<ParseResult?> get() = libraryVm.parseResult
    val isImporting: StateFlow<Boolean> get() = libraryVm.isImporting
    val importProgress: StateFlow<Float> get() = libraryVm.importProgress
    val importSummary: StateFlow<ImportSummary?> get() = libraryVm.importSummary
    val libraryErrorMessage: StateFlow<String?> get() = libraryVm.libraryErrorMessage

    // ==========================================
    // تفویض وضعیت مرور و نشست‌ها (ReviewViewModel)
    // ==========================================
    val setupCandidateCount: StateFlow<Int> get() = reviewVm.setupCandidateCount
    val activeSession: StateFlow<ReviewSession?> get() = reviewVm.activeSession
    val sessionCards: StateFlow<List<ReviewCard>> get() = reviewVm.sessionCards
    val quizQuestions: StateFlow<List<QuizQuestion>> get() = reviewVm.quizQuestions
    val currentCardIndex: StateFlow<Int> get() = reviewVm.currentCardIndex
    val isCardFlipped: StateFlow<Boolean> get() = reviewVm.isCardFlipped
    val quizSelectedOption: StateFlow<Int?> get() = reviewVm.quizSelectedOption
    val quizUserAnswers: StateFlow<Map<Int, Int>> get() = reviewVm.quizUserAnswers
    val sessionCorrectCount: StateFlow<Int> get() = reviewVm.sessionCorrectCount
    val sessionWrongCount: StateFlow<Int> get() = reviewVm.sessionWrongCount
    val reviewErrorMessage: StateFlow<String?> get() = reviewVm.reviewErrorMessage

    // ==========================================
    // تفویض وضعیت پشتیبان‌گیری (SettingsViewModel)
    // ==========================================
    val backupOptions: StateFlow<BackupOptions> get() = settingsVm.backupOptions
    val isBackupProcessing: StateFlow<Boolean> get() = settingsVm.isBackupProcessing
    val backupProgress: StateFlow<Float> get() = settingsVm.backupProgress
    val backupProgressMessage: StateFlow<String> get() = settingsVm.backupProgressMessage
    val backupLastResult: StateFlow<String?> get() = settingsVm.backupLastResult
    val isBackupError: StateFlow<Boolean> get() = settingsVm.isBackupError

    init {
        initializeDatabase()
        viewModelScope.launch {
            settingsVm.appLanguageDirection.collect { dir ->
                statisticsVm.syncDirection(dir)
                libraryVm.loadRecentWords(dir)
            }
        }
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            if (seedImporter.shouldImportSeed()) {
                _isSeeding.value = true
                seedImporter.importSeedIfNeeded { progress, msg ->
                    _seedProgress.value = progress
                    _seedMessage.value = msg
                }
            }
            _isSeeding.value = false
            _currentScreen.value = Screen.Home
            loadRecentWords()
        }
    }

    // ==========================================
    // متدهای ناوبری (Navigation Stack)
    // ==========================================

    /**
     * ناوبری به صفحه مقصد با مدیریت پشته تاریخچه.
     */
    fun navigateTo(screen: Screen, replaceCurrent: Boolean = false) {
        val currentList = _screenStack.value
        val newList = if (replaceCurrent && currentList.isNotEmpty()) {
            currentList.dropLast(1) + screen
        } else if (screen is Screen.Home) {
            listOf(Screen.Home)
        } else {
            if (currentList.lastOrNull() == screen) currentList else currentList + screen
        }
        _screenStack.value = newList
        _currentScreen.value = screen
    }

    /**
     * بازگشت به صفحه قبلی پشته تاریخچه.
     */
    fun navigateBack(): Boolean {
        val currentList = _screenStack.value
        if (currentList.size > 1) {
            val newList = currentList.dropLast(1)
            _screenStack.value = newList
            _currentScreen.value = newList.last()
            return true
        }
        return false
    }

    /**
     * بررسی امکان بازگشت در پشته فعلی.
     */
    fun canNavigateBack(): Boolean = _screenStack.value.size > 1

    // ==========================================
    // متدهای تنظیمات
    // ==========================================
    fun toggleTheme() = settingsVm.toggleTheme()
    fun setTheme(id: String) = settingsVm.setTheme(id)
    fun toggleDarkMode() = settingsVm.toggleDarkMode()
    fun setDarkMode(dark: Boolean) = settingsVm.setDarkMode(dark)
    fun setDifficultyThreshold(threshold: Int) = settingsVm.setDifficultyThreshold(threshold)
    fun setUiLanguage(lang: String) = settingsVm.setUiLanguage(lang)
    fun setTtsEnabled(enabled: Boolean) = settingsVm.setTtsEnabled(enabled)
    fun setTtsAutoPlay(autoPlay: Boolean) = settingsVm.setTtsAutoPlay(autoPlay)
    fun setTtsSpeechRate(rate: Float) = settingsVm.setTtsSpeechRate(rate)
    fun setQuizAutoAdvanceSeconds(seconds: Int) = settingsVm.setQuizAutoAdvanceSeconds(seconds)
    fun setDefaultQuizLevel(level: QuizLevel) = settingsVm.setDefaultQuizLevel(level)
    fun setDefaultReviewMode(mode: ReviewMode) = settingsVm.setDefaultReviewMode(mode)
    fun purgeInactiveWords(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = vocabularyRepo.purgeInactiveWords()
            loadRecentWords()
            onComplete(count)
        }
    }
    fun setAppLanguageDirection(direction: CardDirection) {
        settingsVm.setAppLanguageDirection(direction) {
            statisticsVm.setProgressDirection(it)
        }
    }
    fun setShowCategoryInReview(show: Boolean) = settingsVm.setShowCategoryInReview(show)

    // ==========================================
    // متدهای نشست مرور و فلش‌کارت / آزمون
    // ==========================================
    fun updateSetupFilters(filters: ReviewFilters) = reviewVm.updateSetupFilters(filters)

    fun startFilteredSession(filters: ReviewFilters) {
        // تضمین همخوانی قطعی با جهت سراسری تنظیمات برنامه (ISS-58)
        val unifiedFilters = filters.copy(direction = appLanguageDirection.value)
        reviewVm.startFilteredSession(unifiedFilters) { mode ->
            val targetScreen = if (mode == ReviewMode.FLASHCARD) Screen.Flashcard else Screen.Quiz
            navigateTo(targetScreen)
        }
    }

    fun startSession(
        type: ReviewType,
        mode: ReviewMode,
        direction: CardDirection = appLanguageDirection.value,
        quizLevel: QuizLevel = QuizLevel.MEDIUM,
        limit: Int = 20
    ) {
        startFilteredSession(
            ReviewFilters(
                reviewType = type,
                mode = mode,
                direction = direction,
                quizLevel = quizLevel,
                maxCards = limit
            )
        )
    }

    fun flipCard() = reviewVm.flipCard()

    fun submitFlashcardAnswer(isCorrect: Boolean) {
        reviewVm.submitFlashcardAnswer(
            isCorrect = isCorrect,
            difficultyThreshold = difficultyThreshold.value,
            onSessionCompleted = {
                // حل نقص ISS-56: انتقال استاندارد به خلاصه نشست و هماهنگ‌سازی پشته ناوبری
                navigateTo(Screen.SessionSummary)
            }
        )
    }

    fun exitSession() {
        reviewVm.exitSession {
            if (!navigateBack()) {
                _currentScreen.value = Screen.Home
            }
        }
    }

    /**
     * ثبت پاسخ آزمون ۴گزینه‌ای (حل نقص ISS-16 با ارزیابی دقیق شاخص انتخابی).
     */
    fun submitQuizAnswer(optionIndex: Int) {
        reviewVm.submitQuizAnswer(optionIndex, difficultyThreshold.value)
    }

    fun nextQuizQuestion() {
        reviewVm.nextQuizQuestion {
            // حل نقص ISS-56: انتقال استاندارد به خلاصه نشست و هماهنگ‌سازی پشته ناوبری
            navigateTo(Screen.SessionSummary)
        }
    }

    fun previousQuizQuestion() = reviewVm.previousQuizQuestion()

    fun goToQuizQuestion(targetIndex: Int) = reviewVm.goToQuizQuestion(targetIndex)

    // ==========================================
    // متدهای کتابخانه و جستجو
    // ==========================================
    fun onSearchQueryChanged(q: String) = libraryVm.onSearchQueryChanged(q, appLanguageDirection.value)
    fun onCategoryFilterChanged(catId: String?) = libraryVm.onCategoryFilterChanged(catId, appLanguageDirection.value)
    fun onStageFilterChanged(stage: Stage?) = libraryVm.onStageFilterChanged(stage, appLanguageDirection.value)
    fun onDifficultyFilterChanged(difficulty: VocabularyDifficulty?) = libraryVm.onDifficultyFilterChanged(difficulty, appLanguageDirection.value)
    fun onInactiveFilterToggled(onlyInactive: Boolean) = libraryVm.onInactiveFilterToggled(onlyInactive, appLanguageDirection.value)
    fun loadNextLibraryPage() = libraryVm.loadNextPage(appLanguageDirection.value)
    fun reactivateWord(conceptId: String) = libraryVm.reactivateWord(conceptId, appLanguageDirection.value)
    fun loadRecentWords() = libraryVm.loadRecentWords(appLanguageDirection.value)

    fun selectWordDetail(conceptId: String) {
        libraryVm.selectWordDetail(conceptId) {
            _currentScreen.value = Screen.WordDetailScreen(conceptId)
        }
    }

    fun deleteWord(conceptId: String) {
        libraryVm.deleteWord(conceptId, appLanguageDirection.value) {
            _currentScreen.value = Screen.Library
        }
    }

    /**
     * ذخیره یا ویرایش واژه با کنترل و اعتبارسنجی خروجی Result (حل ISS-22).
     */
    fun saveWord(
        conceptId: String?,
        sourceText: String,
        translations: List<String>,
        categoryId: String?,
        note: String?,
        direction: CardDirection = appLanguageDirection.value,
        onError: ((String) -> Unit)? = null,
        onSuccess: () -> Unit
    ) {
        libraryVm.saveWord(
            conceptId = conceptId,
            sourceText = sourceText,
            translations = translations,
            categoryId = categoryId,
            note = note,
            direction = direction,
            onError = onError,
            onSuccess = onSuccess
        )
    }

    fun clearLibraryErrorMessage() = libraryVm.clearErrorMessage()
    fun clearReviewErrorMessage() = reviewVm.clearErrorMessage()

    // ==========================================
    // متدهای ورود داده‌ها (Import)
    // ==========================================
    fun parseInputText(text: String) = libraryVm.parseInputText(text)
    fun setParseResult(result: ParseResult) = libraryVm.setParseResult(result)
    fun clearParseResult() = libraryVm.clearParseResult()
    fun createCategory(name: String, onCreated: (String) -> Unit = {}) = libraryVm.createCategory(name, onCreated)
    fun clearImportSummary() = libraryVm.clearImportSummary()

    /**
     * اجرای ورود گروهی با تضمین ریست وضعیت در بلوک finally (حل ISS-22).
     */
    fun executeImport(policy: DuplicatePolicy, categoryId: String? = null, onComplete: () -> Unit = {}) {
        libraryVm.executeImport(policy, categoryId, appLanguageDirection.value, onComplete)
    }

    // ==========================================
    // متدهای آمار و پیشرفت
    // ==========================================
    fun setProgressDirection(direction: CardDirection) {
        settingsVm.setAppLanguageDirection(direction)
    }
    fun refreshStatistics() = statisticsVm.refreshStatistics()

    // ==========================================
    // متدهای پشتیبان‌گیری و بازیابی
    // ==========================================
    fun updateBackupOptions(options: BackupOptions) = settingsVm.updateBackupOptions(options)
    fun setFullBackup(enabled: Boolean) = settingsVm.setFullBackup(enabled)
    fun toggleBackupOption(key: String, value: Boolean) = settingsVm.toggleBackupOption(key, value)
    fun exportCustomBackupToUri(format: BackupExportFormat, uri: Uri, options: BackupOptions = backupOptions.value) =
        settingsVm.exportCustomBackupToUri(format, uri, options)
    fun exportCustomBackup(format: BackupExportFormat, options: BackupOptions = backupOptions.value) =
        settingsVm.exportCustomBackup(format, options)
    fun exportBackupToUri(type: BackupType, uri: Uri) = settingsVm.exportBackupToUri(type, uri)
    fun exportBackup(type: BackupType) = settingsVm.exportBackup(type)
    fun restoreBackup(jsonString: String, isReplace: Boolean) {
        settingsVm.restoreBackup(jsonString, isReplace) {
            loadRecentWords()
        }
    }
}
