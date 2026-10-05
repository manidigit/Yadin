package com.manidigit.yadin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manidigit.yadin.ui.screens.AboutScreen
import com.manidigit.yadin.ui.screens.AddEditWordScreen
import com.manidigit.yadin.ui.screens.FlashcardScreen
import com.manidigit.yadin.ui.screens.HelpScreen
import com.manidigit.yadin.ui.screens.HomeScreen
import com.manidigit.yadin.ui.screens.ImportScreen
import com.manidigit.yadin.ui.screens.LibraryScreen
import com.manidigit.yadin.ui.screens.ProgressScreen
import com.manidigit.yadin.ui.screens.QuizScreen
import com.manidigit.yadin.ui.screens.ReviewSetupScreen
import com.manidigit.yadin.ui.screens.SessionSummaryScreen
import com.manidigit.yadin.ui.screens.SettingsScreen
import com.manidigit.yadin.ui.screens.SplashScreen
import com.manidigit.yadin.ui.screens.WordDetailScreen
import com.manidigit.yadin.ui.theme.YadinTheme
import com.manidigit.yadin.ui.viewmodel.MainViewModel
import com.manidigit.yadin.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeId by viewModel.themeId.collectAsStateWithLifecycle()
            val isDark by viewModel.isDark.collectAsStateWithLifecycle()
            val screen by viewModel.currentScreen.collectAsStateWithLifecycle()

            YadinTheme(themeId = themeId, isDark = isDark) {
                // Persian is RTL
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            AppNavigator(viewModel = viewModel, screen = screen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigator(viewModel: MainViewModel, screen: Screen) {
    // BackHandler for sub-screens
    if (screen !is Screen.Home && screen !is Screen.Splash) {
        BackHandler {
            when (screen) {
                is Screen.WordDetailScreen -> viewModel.navigateTo(Screen.Library)
                is Screen.EditWordScreen -> viewModel.navigateTo(Screen.Library)
                is Screen.SessionSummary -> viewModel.navigateTo(Screen.Home)
                is Screen.Flashcard, is Screen.Quiz -> viewModel.navigateTo(Screen.Home)
                else -> viewModel.navigateTo(Screen.Home)
            }
        }
    }

    when (screen) {
        is Screen.Splash -> {
            val progress by viewModel.seedProgress.collectAsStateWithLifecycle()
            val message by viewModel.seedMessage.collectAsStateWithLifecycle()
            SplashScreen(progress = progress, message = message)
        }

        is Screen.Home -> {
            val dueCount by viewModel.dueCount.collectAsStateWithLifecycle()
            val statistics by viewModel.statistics.collectAsStateWithLifecycle()
            val difficultyCounts by viewModel.difficultyCounts.collectAsStateWithLifecycle()
            val themeId by viewModel.themeId.collectAsStateWithLifecycle()
            val isDark by viewModel.isDark.collectAsStateWithLifecycle()
            val activePair by viewModel.activePair.collectAsStateWithLifecycle()

            HomeScreen(
                dueCount = dueCount,
                statistics = statistics,
                difficultyCounts = difficultyCounts,
                themeId = themeId,
                isDark = isDark,
                activePair = activePair,
                onToggleTheme = { viewModel.toggleTheme() },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onOpenReviewSetup = { type ->
                    viewModel.navigateTo(Screen.ReviewSetup(type))
                },
                onStartReview = { type, mode ->
                    viewModel.startSession(type, mode)
                },
                onDifficultyFilterClick = { diff ->
                    viewModel.navigateTo(Screen.ReviewSetup())
                },
                onOpenLibrary = { viewModel.navigateTo(Screen.Library) },
                onOpenAddWord = { viewModel.navigateTo(Screen.EditWordScreen(null)) },
                onOpenImport = { viewModel.navigateTo(Screen.ImportPreview) },
                onOpenProgress = { viewModel.navigateTo(Screen.ProgressStats) },
                onOpenSettings = { viewModel.navigateTo(Screen.Settings) },
                onOpenHelp = { viewModel.navigateTo(Screen.Help) },
                onOpenAbout = { viewModel.navigateTo(Screen.About) }
            )
        }

        is Screen.ReviewSetup -> {
            val categories by viewModel.categories.collectAsStateWithLifecycle()
            val difficultyCounts by viewModel.difficultyCounts.collectAsStateWithLifecycle()
            val candidateCount by viewModel.setupCandidateCount.collectAsStateWithLifecycle()

            ReviewSetupScreen(
                initialType = screen.initialType,
                categories = categories,
                difficultyCounts = difficultyCounts,
                candidateCount = candidateCount,
                onFilterChanged = { filters ->
                    viewModel.updateSetupFilters(filters)
                },
                onStartReview = { filters ->
                    viewModel.startFilteredSession(filters)
                },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Flashcard -> {
            val cards by viewModel.sessionCards.collectAsStateWithLifecycle()
            val currentIndex by viewModel.currentCardIndex.collectAsStateWithLifecycle()
            val isFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()

            FlashcardScreen(
                cards = cards,
                currentIndex = currentIndex,
                isFlipped = isFlipped,
                onFlip = { viewModel.flipCard() },
                onAnswer = { isCorrect -> viewModel.submitFlashcardAnswer(isCorrect) },
                onExit = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Quiz -> {
            val questions by viewModel.quizQuestions.collectAsStateWithLifecycle()
            val currentIndex by viewModel.currentCardIndex.collectAsStateWithLifecycle()
            val selectedOption by viewModel.quizSelectedOption.collectAsStateWithLifecycle()

            QuizScreen(
                questions = questions,
                currentIndex = currentIndex,
                selectedOption = selectedOption,
                onSelectOption = { optIdx -> viewModel.submitQuizAnswer(optIdx) },
                onNextQuestion = { viewModel.nextQuizQuestion() },
                onExit = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.SessionSummary -> {
            val correct by viewModel.sessionCorrectCount.collectAsStateWithLifecycle()
            val wrong by viewModel.sessionWrongCount.collectAsStateWithLifecycle()

            SessionSummaryScreen(
                correctCount = correct,
                wrongCount = wrong,
                onBackHome = { viewModel.navigateTo(Screen.Home) },
                onReviewAgain = { viewModel.navigateTo(Screen.ReviewSetup()) }
            )
        }

        is Screen.Library -> {
            val results by viewModel.searchResults.collectAsStateWithLifecycle()
            val query by viewModel.searchQuery.collectAsStateWithLifecycle()
            val categories by viewModel.categories.collectAsStateWithLifecycle()
            val selectedCat by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
            val selectedStage by viewModel.selectedStageFilter.collectAsStateWithLifecycle()

            LibraryScreen(
                words = results,
                searchQuery = query,
                categories = categories,
                selectedCategory = selectedCat,
                selectedStage = selectedStage,
                onSearchChange = { viewModel.onSearchQueryChanged(it) },
                onCategoryFilterChange = { viewModel.onCategoryFilterChanged(it) },
                onStageFilterChange = { viewModel.onStageFilterChanged(it) },
                onSelectWord = { conceptId -> viewModel.selectWordDetail(conceptId) },
                onToggleFavorite = { cid, fav -> viewModel.toggleFavorite(cid, fav) },
                onAddWord = { viewModel.navigateTo(Screen.EditWordScreen(null)) },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.WordDetailScreen -> {
            val wordDetail by viewModel.selectedWordDetail.collectAsStateWithLifecycle()

            WordDetailScreen(
                word = wordDetail,
                onBack = { viewModel.navigateTo(Screen.Library) },
                onEdit = { viewModel.navigateTo(Screen.EditWordScreen(screen.conceptId)) },
                onDelete = { viewModel.deleteWord(screen.conceptId) },
                onToggleFavorite = { fav -> viewModel.toggleFavorite(screen.conceptId, fav) }
            )
        }

        is Screen.EditWordScreen -> {
            val wordDetail by viewModel.selectedWordDetail.collectAsStateWithLifecycle()
            val categories by viewModel.categories.collectAsStateWithLifecycle()
            val targetWord = if (screen.conceptId != null) wordDetail else null

            AddEditWordScreen(
                initialWord = targetWord,
                categories = categories,
                onSave = { src, trans, cat, note, pron ->
                    viewModel.saveWord(screen.conceptId, src, trans, cat, note, pron) {
                        viewModel.navigateTo(Screen.Library)
                    }
                },
                onBack = {
                    if (screen.conceptId != null) {
                        viewModel.navigateTo(Screen.WordDetailScreen(screen.conceptId))
                    } else {
                        viewModel.navigateTo(Screen.Library)
                    }
                }
            )
        }

        is Screen.ImportPreview -> {
            val parseResult by viewModel.parseResult.collectAsStateWithLifecycle()
            val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
            val importProgress by viewModel.importProgress.collectAsStateWithLifecycle()

            ImportScreen(
                parseResult = parseResult,
                isImporting = isImporting,
                importProgress = importProgress,
                onParseText = { text -> viewModel.parseInputText(text) },
                onConfirmImport = { policy ->
                    viewModel.executeImport(policy) {
                        viewModel.navigateTo(Screen.Library)
                    }
                },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.ProgressStats -> {
            val statistics by viewModel.statistics.collectAsStateWithLifecycle()
            ProgressScreen(
                statistics = statistics,
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Settings -> {
            val themeId by viewModel.themeId.collectAsStateWithLifecycle()
            val isDark by viewModel.isDark.collectAsStateWithLifecycle()

            SettingsScreen(
                currentThemeId = themeId,
                isDark = isDark,
                onSelectTheme = { viewModel.setTheme(it) },
                onToggleDarkMode = { viewModel.setDarkMode(it) },
                onOpenHelp = { viewModel.navigateTo(Screen.Help) },
                onOpenAbout = { viewModel.navigateTo(Screen.About) },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Help -> {
            HelpScreen(onBack = { viewModel.navigateTo(Screen.Home) })
        }

        is Screen.About -> {
            AboutScreen(onBack = { viewModel.navigateTo(Screen.Home) })
        }
    }
}
