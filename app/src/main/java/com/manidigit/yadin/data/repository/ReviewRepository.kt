package com.manidigit.yadin.data.repository

import com.manidigit.yadin.data.local.dao.AchievementDao
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.domain.algorithm.DifficultyCalculator
import com.manidigit.yadin.domain.algorithm.LearningTransition
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.QuizOption
import com.manidigit.yadin.domain.model.QuizQuestion
import com.manidigit.yadin.domain.model.ReviewCard
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewSession
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SessionItemState
import com.manidigit.yadin.domain.model.SessionStatus
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.time.ClockAndDayMath
import java.util.UUID

data class SubmitResult(
    val newStage: Stage,
    val nextReviewDay: String?,
    val newDifficulty: VocabularyDifficulty,
    val isCorrect: Boolean
)

data class ReviewFilters(
    val reviewType: ReviewType = ReviewType.DAILY,
    val mode: ReviewMode = ReviewMode.FLASHCARD,
    val direction: CardDirection = CardDirection.NORMAL,
    val quizLevel: QuizLevel? = null,
    val difficulties: Set<VocabularyDifficulty> = emptySet(),
    val categoryIds: Set<String> = emptySet(),
    val maxCards: Int = 20
)

class ReviewRepository(
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val reviewSessionDao: ReviewSessionDao,
    private val achievementDao: AchievementDao
) {

    suspend fun countCandidates(filters: ReviewFilters): Int {
        val today = ClockAndDayMath.todayDayString()
        val count = learningDao.countFilteredCandidates(
            direction = filters.direction,
            reviewType = filters.reviewType.name,
            todayDayString = today,
            hasDifficultyFilter = if (filters.difficulties.isNotEmpty()) 1 else 0,
            difficulties = filters.difficulties.map { it.name },
            hasCategoryFilter = if (filters.categoryIds.isNotEmpty()) 1 else 0,
            categoryIds = filters.categoryIds.toList()
        )
        return if (count == 0 && filters.reviewType == ReviewType.DAILY && filters.difficulties.isEmpty() && filters.categoryIds.isEmpty()) {
            learningDao.getConceptIdsByStage(Stage.DAILY, filters.direction, 50).size
        } else {
            count
        }
    }

    suspend fun createFilteredSession(filters: ReviewFilters): ReviewSession {
        val today = ClockAndDayMath.todayDayString()
        var conceptIds = learningDao.getFilteredCandidateConceptIds(
            direction = filters.direction,
            reviewType = filters.reviewType.name,
            todayDayString = today,
            hasDifficultyFilter = if (filters.difficulties.isNotEmpty()) 1 else 0,
            difficulties = filters.difficulties.map { it.name },
            hasCategoryFilter = if (filters.categoryIds.isNotEmpty()) 1 else 0,
            categoryIds = filters.categoryIds.toList(),
            limit = filters.maxCards
        )

        if (conceptIds.size < filters.maxCards && filters.reviewType == ReviewType.DAILY && filters.difficulties.isEmpty() && filters.categoryIds.isEmpty()) {
            val fallback = learningDao.getConceptIdsByStage(Stage.DAILY, filters.direction, filters.maxCards - conceptIds.size)
            conceptIds = (conceptIds + fallback).distinct()
        }

        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val sessionEntity = ReviewSessionEntity(
            id = sessionId,
            startedAt = now,
            endedAt = null,
            reviewType = filters.reviewType,
            mode = filters.mode,
            direction = filters.direction,
            quizLevel = filters.quizLevel,
            status = SessionStatus.ACTIVE,
            currentPosition = 0,
            totalItems = conceptIds.size
        )

        val sessionItems = conceptIds.mapIndexed { index, cid ->
            ReviewSessionItemEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                position = index,
                conceptId = cid,
                direction = filters.direction,
                state = SessionItemState.PENDING
            )
        }

        reviewSessionDao.insertSession(sessionEntity)
        reviewSessionDao.insertSessionItems(sessionItems)

        return ReviewSession(
            id = sessionEntity.id,
            startedAt = sessionEntity.startedAt,
            endedAt = sessionEntity.endedAt,
            reviewType = sessionEntity.reviewType,
            mode = sessionEntity.mode,
            direction = sessionEntity.direction,
            quizLevel = sessionEntity.quizLevel,
            status = sessionEntity.status,
            currentPosition = 0,
            totalItems = conceptIds.size
        )
    }

    suspend fun createSession(
        reviewType: ReviewType,
        mode: ReviewMode,
        direction: CardDirection,
        quizLevel: QuizLevel? = null,
        limit: Int = 20
    ): ReviewSession {
        return createFilteredSession(
            ReviewFilters(
                reviewType = reviewType,
                mode = mode,
                direction = direction,
                quizLevel = quizLevel,
                maxCards = limit
            )
        )
    }

    suspend fun fetchCardsForSession(sessionId: String): List<ReviewCard> {
        val items = reviewSessionDao.getItemsForSession(sessionId)
        val cards = mutableListOf<ReviewCard>()

        for (item in items) {
            val concept = conceptDao.getConceptById(item.conceptId) ?: continue
            val contents = conceptDao.getContentsForConcept(item.conceptId)
            val sourceContent = contents.firstOrNull { it.languageCode != "fa" } ?: contents.firstOrNull() ?: continue
            val targetContents = contents.filter { it.languageCode == "fa" || it.id != sourceContent.id }
            val targetTranslations = targetContents.map { it.text }.ifEmpty { listOf(sourceContent.text) }

            val learning = learningDao.getLearningState(item.conceptId, item.direction)
            val stage = learning?.stage ?: Stage.DAILY
            val diff = learningDao.getDifficultyState(item.conceptId, item.direction)?.current ?: VocabularyDifficulty.MEDIUM
            val category = concept.categoryId?.let { conceptDao.getCategoryById(it)?.name }

            val prompt = if (item.direction == CardDirection.NORMAL) sourceContent.text else targetTranslations.first()
            val answers = if (item.direction == CardDirection.NORMAL) targetTranslations else listOf(sourceContent.text)

            cards.add(
                ReviewCard(
                    conceptId = item.conceptId,
                    direction = item.direction,
                    sourceText = prompt,
                    targetTranslations = answers,
                    note = sourceContent.note,
                    pronunciation = sourceContent.pronunciation,
                    categoryName = category,
                    stage = stage,
                    difficulty = diff,
                    isFavorite = concept.favorite
                )
            )
        }
        return cards
    }

    suspend fun generateQuizQuestions(sessionId: String): List<QuizQuestion> {
        val cards = fetchCardsForSession(sessionId)
        if (cards.isEmpty()) return emptyList()

        val allDistractorContents = conceptDao.getRandomContents("fa", 100)
        val allDistractorTexts = allDistractorContents.map { it.text }.distinct()

        val questions = mutableListOf<QuizQuestion>()

        for (card in cards) {
            val correctAnswer = card.targetTranslations.firstOrNull() ?: ""
            // Pick 3 distractors different from the correct answer
            val candidates = allDistractorTexts.filter { it != correctAnswer && it !in card.targetTranslations }
                .shuffled()
                .take(3)

            val optionsList = (candidates.map { QuizOption(it, "") } + QuizOption(correctAnswer, card.conceptId)).shuffled()
            val correctIdx = optionsList.indexOfFirst { it.text == correctAnswer }.coerceAtLeast(0)

            questions.add(
                QuizQuestion(
                    conceptId = card.conceptId,
                    direction = card.direction,
                    promptText = card.sourceText,
                    correctAnswer = correctAnswer,
                    options = optionsList,
                    correctIndex = correctIdx,
                    note = card.note,
                    categoryName = card.categoryName,
                    stage = card.stage,
                    difficulty = card.difficulty
                )
            )
        }
        return questions
    }

    suspend fun submitAnswer(
        sessionId: String,
        conceptId: String,
        direction: CardDirection,
        isCorrect: Boolean,
        mode: ReviewMode,
        selectedIndex: Int? = null,
        correctIndex: Int? = null,
        optionsJson: String? = null
    ): SubmitResult {
        val today = ClockAndDayMath.todayDayString()
        val now = System.currentTimeMillis()

        val currentLearning = learningDao.getLearningState(conceptId, direction)
        val currentStage = currentLearning?.stage ?: Stage.DAILY

        val transition = LearningTransition.calculateNextStage(
            currentStage = currentStage,
            isCorrect = isCorrect,
            todayDayString = today
        )

        val updatedLearning = (currentLearning ?: com.manidigit.yadin.data.local.entity.LearningStateEntity(
            id = UUID.randomUUID().toString(),
            conceptId = conceptId,
            direction = direction
        )).copy(
            stage = transition.newStage,
            nextReviewDay = transition.nextReviewDay,
            lastReviewedDay = today,
            updatedAt = now
        )
        learningDao.insertLearningState(updatedLearning)

        val currentDiff = learningDao.getDifficultyState(conceptId, direction)
        val curDifficultyEnum = currentDiff?.current ?: VocabularyDifficulty.MEDIUM
        val consecutiveCorrect = currentDiff?.consecutiveCorrect ?: 0
        val consecutiveWrong = currentDiff?.consecutiveWrong ?: 0
        val hasReachedVeryHard = currentDiff?.hasReachedVeryHard ?: false

        val diffResult = DifficultyCalculator.updateDifficulty(
            current = curDifficultyEnum,
            consecutiveCorrect = consecutiveCorrect,
            consecutiveWrong = consecutiveWrong,
            hasReachedVeryHard = hasReachedVeryHard,
            isCorrect = isCorrect
        )

        val updatedDiff = (currentDiff ?: com.manidigit.yadin.data.local.entity.DifficultyStateEntity(
            id = UUID.randomUUID().toString(),
            conceptId = conceptId,
            direction = direction
        )).copy(
            current = diffResult.newDifficulty,
            consecutiveCorrect = diffResult.consecutiveCorrect,
            consecutiveWrong = diffResult.consecutiveWrong,
            hasReachedVeryHard = diffResult.hasReachedVeryHard
        )
        learningDao.insertDifficultyState(updatedDiff)

        // Record history
        val session = reviewSessionDao.getSessionById(sessionId)
        val historyItem = ReviewHistoryEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            reviewAttemptId = UUID.randomUUID().toString(),
            conceptId = conceptId,
            direction = direction,
            reviewedAt = now,
            reviewedDay = today,
            isCorrect = isCorrect,
            reviewType = session?.reviewType ?: ReviewType.DAILY,
            mode = mode,
            stageBefore = currentStage,
            quizLevel = session?.quizLevel,
            optionsJson = optionsJson,
            selectedIndex = selectedIndex,
            correctIndex = correctIndex
        )
        reviewSessionDao.insertHistoryItem(historyItem)

        // Update position in session
        if (session != null) {
            reviewSessionDao.updateSession(
                session.copy(currentPosition = session.currentPosition + 1)
            )
        }

        // Check achievements trigger
        checkAchievements(isCorrect)

        return SubmitResult(
            newStage = transition.newStage,
            nextReviewDay = transition.nextReviewDay,
            newDifficulty = diffResult.newDifficulty,
            isCorrect = isCorrect
        )
    }

    private suspend fun checkAchievements(isCorrect: Boolean) {
        val totalReviews = reviewSessionDao.getDistinctReviewedDays().size
        if (totalReviews >= 3) {
            achievementDao.unlock("STREAK_3_DAYS")
        }
        if (totalReviews >= 7) {
            achievementDao.unlock("STREAK_7_DAYS")
        }
        if (totalReviews >= 30) {
            achievementDao.unlock("STREAK_30_DAYS")
        }
    }

    suspend fun completeSession(sessionId: String) {
        val session = reviewSessionDao.getSessionById(sessionId) ?: return
        reviewSessionDao.updateSession(
            session.copy(
                status = SessionStatus.COMPLETED,
                endedAt = System.currentTimeMillis()
            )
        )
    }
}
