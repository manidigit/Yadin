package com.manidigit.yadin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manidigit.yadin.ui.screens.AboutScreen
import com.manidigit.yadin.ui.screens.AddEditWordScreen
import com.manidigit.yadin.ui.screens.BackupScreen
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
import com.manidigit.yadin.ui.theme.LocalYadinColors
import com.manidigit.yadin.ui.theme.LocalYadinDimensions
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
                    val shouldShowBottomBar = screen is Screen.Home ||
                            screen is Screen.ReviewSetup ||
                            screen is Screen.Library ||
                            screen is Screen.ProgressStats ||
                            screen is Screen.Settings

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding(),
                        bottomBar = {
                            if (shouldShowBottomBar) {
                                YadinBottomBar(
                                    currentScreen = screen,
                                    onNavigate = { target -> viewModel.navigateTo(target) }
                                )
                            }
                        }
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
fun YadinBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val colors = LocalYadinColors.current
    val dimensions = LocalYadinDimensions.current

    val items = listOf(
        Triple(Screen.Home, "خانه", Icons.Default.Home),
        Triple(Screen.ReviewSetup(), "مرور", Icons.Default.History),
        Triple(Screen.Library, "کتابخانه", Icons.Default.MenuBook),
        Triple(Screen.ProgressStats, "پیشرفت", Icons.Default.BarChart),
        Triple(Screen.Settings, "تنظیمات", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = colors.surface,
        tonalElevation = dimensions.cardElevation,
        modifier = Modifier.height(64.dp)
    ) {
        items.forEach { (targetScreen, label, icon) ->
            val isSelected = when (targetScreen) {
                is Screen.Home -> currentScreen is Screen.Home
                is Screen.ReviewSetup -> currentScreen is Screen.ReviewSetup
                is Screen.Library -> currentScreen is Screen.Library
                is Screen.ProgressStats -> currentScreen is Screen.ProgressStats
                is Screen.Settings -> currentScreen is Screen.Settings
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(targetScreen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.primary,
                    selectedTextColor = colors.primary,
                    indicatorColor = colors.primary.copy(alpha = 0.15f),
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant
                )
            )
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
                is Screen.Backup -> viewModel.navigateTo(Screen.Settings)
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
                onOpenBackup = { viewModel.navigateTo(Screen.Backup) },
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
            val progressPercent by viewModel.progressScorePercent.collectAsStateWithLifecycle()
            val statistics by viewModel.statistics.collectAsStateWithLifecycle()
            val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
            val practicedCount by viewModel.practicedWordsCount.collectAsStateWithLifecycle()
            val activeDirection by viewModel.progressDirection.collectAsStateWithLifecycle()

            ProgressScreen(
                progressPercent = progressPercent,
                statistics = statistics,
                dailyStats = dailyStats,
                practicedCount = practicedCount,
                activeDirection = activeDirection,
                onDirectionChanged = { viewModel.setProgressDirection(it) },
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
                onOpenBackup = { viewModel.navigateTo(Screen.Backup) },
                onOpenHelp = { viewModel.navigateTo(Screen.Help) },
                onOpenAbout = { viewModel.navigateTo(Screen.About) },
                onBack = { viewModel.navigateTo(Screen.Home) }
            )
        }

        is Screen.Backup -> {
            val statistics by viewModel.statistics.collectAsStateWithLifecycle()
            val categories by viewModel.categories.collectAsStateWithLifecycle()
            val isProcessing by viewModel.isBackupProcessing.collectAsStateWithLifecycle()
            val progress by viewModel.backupProgress.collectAsStateWithLifecycle()
            val message by viewModel.backupProgressMessage.collectAsStateWithLifecycle()
            val lastResult by viewModel.backupLastResult.collectAsStateWithLifecycle()
            val isError by viewModel.isBackupError.collectAsStateWithLifecycle()

            BackupScreen(
                totalConcepts = statistics.totalWords,
                totalCategories = categories.size,
                isProcessing = isProcessing,
                progress = progress,
                progressMessage = message,
                lastResult = lastResult,
                isError = isError,
                onExportBackup = { type -> viewModel.exportBackup(type) },
                onRestoreBackup = { json, replace -> viewModel.restoreBackup(json, replace) },
                onBack = { viewModel.navigateTo(Screen.Settings) }
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
