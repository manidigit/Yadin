package com.manidigit.yadin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.manidigit.yadin.data.local.database.YadinDatabase
import com.manidigit.yadin.data.repository.ReviewRepository
import com.manidigit.yadin.data.repository.SeedImporter
import com.manidigit.yadin.data.repository.SettingsRepository
import com.manidigit.yadin.data.repository.VocabularyRepository
import com.manidigit.yadin.domain.algorithm.VocabularyParser
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.QuizQuestion
import com.manidigit.yadin.domain.model.ReviewCard
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewSession
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.WordDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    data class ReviewSetup(val initialType: ReviewType = ReviewType.DAILY) : Screen()
    object Flashcard : Screen()
    object Quiz : Screen()
    object SessionSummary : Screen()
    object Library : Screen()
    data class WordDetailScreen(val conceptId: String) : Screen()
    data class EditWordScreen(val conceptId: String? = null) : Screen()
    object ImportPreview : Screen()
    object ProgressStats : Screen()
    object Settings : Screen()
    object Help : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = YadinDatabase.getInstance(application)
    private val settingsRepo = SettingsRepository(db.settingsDao())
    val vocabularyRepo = VocabularyRepository(db.conceptDao(), db.learningDao(), db.reviewSessionDao())
    val reviewRepo = ReviewRepository(db.conceptDao(), db.learningDao(), db.reviewSessionDao(), db.achievementDao())
    private val seedImporter = SeedImporter(application, db.conceptDao(), db.learningDao(), db.achievementDao())

    // Settings
    val themeId: StateFlow<String> = settingsRepo.themeIdFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "gtp")

    val isDark: StateFlow<Boolean> = settingsRepo.isDarkFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val activePair: StateFlow<String> = settingsRepo.activePairFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "es-fa")

    val uiLanguage: StateFlow<String> = settingsRepo.uiLanguageFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "fa")

    // Navigation & Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Seeding & Initial Loading
    private val _isSeeding = MutableStateFlow(true)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    private val _seedProgress = MutableStateFlow(0f)
    val seedProgress: StateFlow<Float> = _seedProgress.asStateFlow()

    private val _seedMessage = MutableStateFlow("در حال بررسی بانک واژگان...")
    val seedMessage: StateFlow<String> = _seedMessage.asStateFlow()

    // Statistics & Dashboard
    val dueCount: StateFlow<Int> = vocabularyRepo.getDueCountFlow(CardDirection.NORMAL)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val statistics: StateFlow<StatisticsSummary> = vocabularyRepo.getStatisticsSummary(CardDirection.NORMAL)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            StatisticsSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        )

    val categories: StateFlow<List<Category>> = vocabularyRepo.getAllCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Review Session State
    private val _activeSession = MutableStateFlow<ReviewSession?>(null)
    val activeSession: StateFlow<ReviewSession?> = _activeSession.asStateFlow()

    private val _sessionCards = MutableStateFlow<List<ReviewCard>>(emptyList())
    val sessionCards: StateFlow<List<ReviewCard>> = _sessionCards.asStateFlow()

    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _sessionCorrectCount = MutableStateFlow(0)
    val sessionCorrectCount: StateFlow<Int> = _sessionCorrectCount.asStateFlow()

    private val _sessionWrongCount = MutableStateFlow(0)
    val sessionWrongCount: StateFlow<Int> = _sessionWrongCount.asStateFlow()

    // Library & Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedStageFilter = MutableStateFlow<Stage?>(null)
    val selectedStageFilter: StateFlow<Stage?> = _selectedStageFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<WordDetail>>(emptyList())
    val searchResults: StateFlow<List<WordDetail>> = _searchResults.asStateFlow()

    private val _selectedWordDetail = MutableStateFlow<WordDetail?>(null)
    val selectedWordDetail: StateFlow<WordDetail?> = _selectedWordDetail.asStateFlow()

    // Import State
    private val _parseResult = MutableStateFlow<ParseResult?>(null)
    val parseResult: StateFlow<ParseResult?> = _parseResult.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importProgress = MutableStateFlow(0f)
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        viewModelScope.launch {
            if (seedImporter.isDatabaseEmpty()) {
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

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val next = if (themeId.value == "gemini") "gtp" else "gemini"
            settingsRepo.setThemeId(next)
        }
    }

    fun setTheme(id: String) {
        viewModelScope.launch {
            settingsRepo.setThemeId(id)
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            settingsRepo.setDarkMode(!isDark.value)
        }
    }

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch {
            settingsRepo.setDarkMode(dark)
        }
    }

    // Review Session Flow
    fun startSession(
        type: ReviewType,
        mode: ReviewMode,
        direction: CardDirection = CardDirection.NORMAL,
        quizLevel: QuizLevel = QuizLevel.MEDIUM,
        limit: Int = 20
    ) {
        viewModelScope.launch {
            val session = reviewRepo.createSession(type, mode, direction, quizLevel, limit)
            _activeSession.value = session
            _currentCardIndex.value = 0
            _isCardFlipped.value = false
            _quizSelectedOption.value = null
            _sessionCorrectCount.value = 0
            _sessionWrongCount.value = 0

            if (mode == ReviewMode.FLASHCARD) {
                val cards = reviewRepo.fetchCardsForSession(session.id)
                _sessionCards.value = cards
                _currentScreen.value = Screen.Flashcard
            } else {
                val questions = reviewRepo.generateQuizQuestions(session.id)
                _quizQuestions.value = questions
                _currentScreen.value = Screen.Quiz
            }
        }
    }

    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun submitFlashcardAnswer(isCorrect: Boolean) {
        val session = _activeSession.value ?: return
        val cards = _sessionCards.value
        val idx = _currentCardIndex.value
        if (idx >= cards.size) return

        val currentCard = cards[idx]
        viewModelScope.launch {
            reviewRepo.submitAnswer(
                sessionId = session.id,
                conceptId = currentCard.conceptId,
                direction = currentCard.direction,
                isCorrect = isCorrect,
                mode = ReviewMode.FLASHCARD
            )

            if (isCorrect) {
                _sessionCorrectCount.value += 1
            } else {
                _sessionWrongCount.value += 1
            }

            if (idx + 1 < cards.size) {
                _currentCardIndex.value = idx + 1
                _isCardFlipped.value = false
            } else {
                reviewRepo.completeSession(session.id)
                _currentScreen.value = Screen.SessionSummary
            }
        }
    }

    fun submitQuizAnswer(optionIndex: Int) {
        if (_quizSelectedOption.value != null) return // Already answered
        val session = _activeSession.value ?: return
        val questions = _quizQuestions.value
        val idx = _currentCardIndex.value
        if (idx >= questions.size) return

        val q = questions[idx]
        val isCorrect = (optionIndex == q.correctIndex)
        _quizSelectedOption.value = optionIndex

        if (isCorrect) {
            _sessionCorrectCount.value += 1
        } else {
            _sessionWrongCount.value += 1
        }

        viewModelScope.launch {
            reviewRepo.submitAnswer(
                sessionId = session.id,
                conceptId = q.conceptId,
                direction = q.direction,
                isCorrect = isCorrect,
                mode = ReviewMode.QUIZ,
                selectedIndex = optionIndex,
                correctIndex = q.correctIndex
            )
        }
    }

    fun nextQuizQuestion() {
        val questions = _quizQuestions.value
        val idx = _currentCardIndex.value
        val session = _activeSession.value

        if (idx + 1 < questions.size) {
            _currentCardIndex.value = idx + 1
            _quizSelectedOption.value = null
        } else {
            session?.let {
                viewModelScope.launch {
                    reviewRepo.completeSession(it.id)
                }
            }
            _currentScreen.value = Screen.SessionSummary
        }
    }

    // Library & Search
    fun onSearchQueryChanged(q: String) {
        _searchQuery.value = q
        performSearch(q, _selectedCategoryFilter.value, _selectedStageFilter.value)
    }

    fun onCategoryFilterChanged(catId: String?) {
        _selectedCategoryFilter.value = catId
        performSearch(_searchQuery.value, catId, _selectedStageFilter.value)
    }

    fun onStageFilterChanged(stage: Stage?) {
        _selectedStageFilter.value = stage
        performSearch(_searchQuery.value, _selectedCategoryFilter.value, stage)
    }

    private fun performSearch(query: String, categoryId: String?, stage: Stage?) {
        viewModelScope.launch {
            var results = if (query.trim().isEmpty()) {
                vocabularyRepo.getRecentWords(80)
            } else {
                vocabularyRepo.search(query.trim(), 100)
            }

            if (categoryId != null) {
                results = results.filter { it.concept.categoryId == categoryId }
            }
            if (stage != null) {
                results = results.filter { it.normalLearning?.stage == stage }
            }

            _searchResults.value = results
        }
    }

    fun loadRecentWords() {
        performSearch("", null, null)
    }

    fun selectWordDetail(conceptId: String) {
        viewModelScope.launch {
            val detail = vocabularyRepo.getWordDetail(conceptId)
            _selectedWordDetail.value = detail
            _currentScreen.value = Screen.WordDetailScreen(conceptId)
        }
    }

    fun toggleFavorite(conceptId: String, currentFav: Boolean) {
        viewModelScope.launch {
            vocabularyRepo.setFavorite(conceptId, !currentFav)
            _selectedWordDetail.value?.let {
                if (it.concept.id == conceptId) {
                    _selectedWordDetail.value = it.copy(concept = it.concept.copy(favorite = !currentFav))
                }
            }
            performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value)
        }
    }

    fun deleteWord(conceptId: String) {
        viewModelScope.launch {
            vocabularyRepo.deleteWord(conceptId)
            performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value)
            _currentScreen.value = Screen.Library
        }
    }

    fun saveWord(
        conceptId: String?,
        sourceText: String,
        translations: List<String>,
        categoryId: String?,
        note: String?,
        pronunciation: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (conceptId != null) {
                vocabularyRepo.updateWord(conceptId, sourceText, translations, categoryId, note, pronunciation)
            } else {
                vocabularyRepo.addWord(sourceText, translations, categoryId, note, pronunciation)
            }
            performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value)
            onSuccess()
        }
    }

    // Parsing & Import
    fun parseInputText(text: String) {
        val result = VocabularyParser.parse(text)
        _parseResult.value = result
    }

    fun executeImport(policy: DuplicatePolicy, onComplete: () -> Unit) {
        val result = _parseResult.value ?: return
        viewModelScope.launch {
            _isImporting.value = true
            _importProgress.value = 0f
            vocabularyRepo.importParsedEntries(result.entries, policy) { done, total ->
                if (total > 0) {
                    _importProgress.value = done.toFloat() / total
                }
            }
            _isImporting.value = false
            _parseResult.value = null
            loadRecentWords()
            onComplete()
        }
    }
}
