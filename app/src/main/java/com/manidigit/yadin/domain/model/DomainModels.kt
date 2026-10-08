package com.manidigit.yadin.domain.model

data class Concept(
    val id: String,
    val entryType: EntryType,
    val categoryId: String?,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class Content(
    val id: String,
    val conceptId: String,
    val languageCode: String,
    val text: String,
    val canonicalKey: String,
    val note: String?,
    val translationIndex: Int
)

data class Category(
    val id: String,
    val name: String,
    val sortOrder: Int,
    val isDefault: Boolean
)

data class Tag(
    val id: String,
    val name: String
)

data class LearningState(
    val id: String,
    val conceptId: String,
    val direction: CardDirection,
    val stage: Stage,
    val nextReviewDay: String?, // YYYY-MM-DD
    val lastReviewedDay: String?, // YYYY-MM-DD
    val createdAt: Long,
    val updatedAt: Long
)

data class DifficultyState(
    val id: String,
    val conceptId: String,
    val direction: CardDirection,
    val current: VocabularyDifficulty,
    val consecutiveCorrect: Int,
    val consecutiveWrong: Int,
    val hasReachedVeryHard: Boolean
)

data class ReviewSession(
    val id: String,
    val startedAt: Long,
    val endedAt: Long?,
    val reviewType: ReviewType,
    val mode: ReviewMode,
    val direction: CardDirection,
    val quizLevel: QuizLevel?,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val currentPosition: Int = 0,
    val totalItems: Int = 0
)

data class ReviewSessionItem(
    val id: String,
    val sessionId: String,
    val position: Int,
    val conceptId: String,
    val direction: CardDirection,
    val state: SessionItemState = SessionItemState.PENDING
)

data class ReviewHistoryItem(
    val id: String,
    val sessionId: String,
    val reviewAttemptId: String,
    val conceptId: String,
    val direction: CardDirection,
    val reviewedAt: Long,
    val reviewedDay: String, // YYYY-MM-DD
    val isCorrect: Boolean,
    val reviewType: ReviewType,
    val mode: ReviewMode,
    val attemptMode: ReviewMode,
    val stageBefore: Stage,
    val quizLevel: QuizLevel?,
    val optionsJson: String?,
    val selectedIndex: Int?,
    val correctIndex: Int?
)

data class WordDetail(
    val concept: Concept,
    val sourceContent: Content,
    val targetContents: List<Content>,
    val categories: List<Category>,
    val tags: List<Tag>,
    val normalLearning: LearningState?,
    val reverseLearning: LearningState?,
    val normalDifficulty: DifficultyState?,
    val reverseDifficulty: DifficultyState?,
    val variants: List<VocabularyVariantEntry> = emptyList(),
    val relations: List<VocabularyRelationEntry> = emptyList()
)

data class ReviewCard(
    val conceptId: String,
    val direction: CardDirection,
    val sourceText: String,
    val targetTranslations: List<String>,
    val note: String?,
    val categoryName: String?,
    val stage: Stage,
    val difficulty: VocabularyDifficulty,
    val consecutiveCorrect: Int = 0,
    val consecutiveWrong: Int = 0
)

data class QuizQuestion(
    val conceptId: String,
    val direction: CardDirection,
    val promptText: String,
    val correctAnswer: String,
    val options: List<QuizOption>,
    val correctIndex: Int,
    val note: String?,
    val categoryName: String?,
    val stage: Stage,
    val difficulty: VocabularyDifficulty,
    val consecutiveCorrect: Int = 0,
    val consecutiveWrong: Int = 0
)

data class QuizOption(
    val text: String,
    val conceptId: String
)

data class ParsedEntry(
    val sourceText: String,
    val translations: List<String>,
    val note: String?,
    val grammarNotes: String? = null,
    val categoryNames: List<String> = emptyList(),
    val entryType: EntryType = EntryType.WORD,
    val confidence: Double = 1.0,
    val lineNumber: Int = 1,
    val rawLines: List<String> = emptyList(),
    val evidence: List<String> = emptyList(),
    val variants: List<VocabularyVariantEntry> = emptyList(),
    val breakdowns: List<VocabularyBreakdownEntry> = emptyList(),
    val relations: List<VocabularyRelationEntry> = emptyList(),
    val possibleCorrection: String? = null
)

data class VocabularyVariantEntry(
    val sourceText: String,
    val translationText: String?,
    val variantType: VariantType
)

data class VocabularyBreakdownEntry(
    val sourcePart: String,
    val translationPart: String,
    val orderIndex: Int
)

data class VocabularyRelationEntry(
    val relatedSourceText: String,
    val relationType: RelationType,
    val confidence: Double = 1.0
)

data class ParseWarning(
    val type: ParseWarningType,
    val lineNumber: Int,
    val rawText: String,
    val message: String
)

enum class ParseWarningType {
    ORPHAN_SOURCE,
    ORPHAN_TRANSLATION,
    ORPHAN_LINE,
    UNKNOWN_FORMAT,
    NOTE_TOO_LONG,
    TOO_MANY_TRANSLATIONS,
    POSSIBLE_TYPO,
    CONFLICT
}

data class ParseResult(
    val entries: List<ParsedEntry>,
    val warnings: List<ParseWarning>
)

data class ImportSummary(
    val totalProcessed: Int,
    val addedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val categoryName: String? = null
)

data class ImportReviewItem(
    val id: String,
    val sessionTag: String,
    val sourceText: String,
    val translationsText: String?,
    val note: String?,
    val categoryNames: String?,
    val entryType: String?,
    val confidence: Double,
    val reason: String,
    val status: ImportItemStatus,
    val lineNumber: Int?,
    val rawText: String,
    val policy: DuplicatePolicy,
    val targetConceptId: String? = null,
    val createdAt: Long
)

data class StatisticsSummary(
    val totalWords: Int,
    val activeWords: Int,
    val dailyStageCount: Int,
    val weeklyStageCount: Int,
    val monthlyStageCount: Int,
    val learnedStageCount: Int,
    val dueTodayCount: Int,
    val reviewedTodayCount: Int,
    val currentStreakDays: Int,
    val totalReviewsCount: Int,
    val easyCount: Int,
    val mediumCount: Int,
    val hardCount: Int,
    val veryHardCount: Int,
    val dueDailyCount: Int = 0,
    val dueWeeklyCount: Int = 0,
    val dueMonthlyCount: Int = 0,
    val availableLearnedCount: Int = 0
)

data class DailyReviewCount(
    val dayString: String, // YYYY-MM-DD
    val totalCount: Int,
    val correctCount: Int
)
