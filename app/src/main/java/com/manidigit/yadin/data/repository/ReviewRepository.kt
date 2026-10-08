package com.manidigit.yadin.data.repository

import com.manidigit.yadin.data.local.dao.AchievementDao
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.domain.algorithm.DifficultyCalculator
import com.manidigit.yadin.domain.algorithm.LearningTransition
import com.manidigit.yadin.domain.algorithm.QuizDistractorScorer
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.EntryType
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.Locale
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
        return learningDao.countFilteredCandidates(
            direction = filters.direction,
            reviewType = filters.reviewType.name,
            todayDayString = today,
            hasDifficultyFilter = if (filters.difficulties.isNotEmpty()) 1 else 0,
            difficulties = filters.difficulties.map { it.name },
            hasCategoryFilter = if (filters.categoryIds.isNotEmpty()) 1 else 0,
            categoryIds = filters.categoryIds.toList()
        )
    }

    suspend fun createFilteredSession(filters: ReviewFilters): ReviewSession {
        val today = ClockAndDayMath.todayDayString()
        val conceptIds = learningDao.getFilteredCandidateConceptIds(
            direction = filters.direction,
            reviewType = filters.reviewType.name,
            todayDayString = today,
            hasDifficultyFilter = if (filters.difficulties.isNotEmpty()) 1 else 0,
            difficulties = filters.difficulties.map { it.name },
            hasCategoryFilter = if (filters.categoryIds.isNotEmpty()) 1 else 0,
            categoryIds = filters.categoryIds.toList(),
            limit = filters.maxCards
        )

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
            val diffState = learningDao.getDifficultyState(item.conceptId, item.direction)
            val diff = diffState?.current ?: VocabularyDifficulty.MEDIUM
            val category = concept.categoryId?.let { conceptDao.getCategoryById(it)?.name }

            val prompt = if (item.direction == CardDirection.NORMAL) sourceContent.text else targetTranslations.joinToString("، ")
            val answers = if (item.direction == CardDirection.NORMAL) targetTranslations else listOf(sourceContent.text)

            cards.add(
                ReviewCard(
                    conceptId = item.conceptId,
                    direction = item.direction,
                    sourceText = prompt,
                    targetTranslations = answers,
                    note = sourceContent.note,
                    categoryName = category,
                    stage = stage,
                    difficulty = diff,
                    consecutiveCorrect = diffState?.consecutiveCorrect ?: 0,
                    consecutiveWrong = diffState?.consecutiveWrong ?: 0
                )
            )
        }
        return cards
    }

    suspend fun generateQuizQuestions(sessionId: String): List<QuizQuestion> = withContext(Dispatchers.Default) {
        val session = reviewSessionDao.getSessionById(sessionId)
        val quizLevel = session?.quizLevel ?: QuizLevel.MEDIUM
        val cards = fetchCardsForSession(sessionId)
        if (cards.isEmpty()) return@withContext emptyList()

        // Distractor candidate entries
        val targetLang = if (session?.direction == CardDirection.REVERSE) "es" else "fa"
        val sessionConceptIds = cards.map { it.conceptId }.toSet()

        // Fast random candidate sampling (500 items) to ensure wide pool and instant load time
        val randomCandidateContents = conceptDao.getRandomContents(targetLang, 500)
        
        val candidateConceptIds = (randomCandidateContents.map { it.conceptId } + sessionConceptIds).distinct()
        val allContents = conceptDao.getContentsForConcepts(candidateConceptIds).filter { it.languageCode == targetLang && it.text.isNotBlank() }
        val poolConceptIds = allContents.map { it.conceptId }.distinct()

        val categoryMap = mutableMapOf<String, String?>()
        val entryTypeMap = mutableMapOf<String, EntryType>()
        val diffMap = mutableMapOf<String, VocabularyDifficulty>()
        val targetDirection = session?.direction ?: CardDirection.NORMAL

        poolConceptIds.chunked(300).forEach { cIds ->
            val concepts = conceptDao.getConceptsByIds(cIds)
            concepts.forEach {
                categoryMap[it.id] = it.categoryId
                entryTypeMap[it.id] = it.entryType
            }
            val diffStates = learningDao.getDifficultyStatesForConcepts(cIds, targetDirection)
            diffStates.forEach {
                diffMap[it.conceptId] = it.current
            }
        }

        data class DistractorPoolItem(
            val conceptId: String,
            val text: String,
            val categoryId: String?,
            val entryType: EntryType,
            val difficulty: VocabularyDifficulty,
            val semantic: QuizDistractorScorer.PrecomputedSemantic
        )

        // Precompute semantics ONCE for all unique concept translation entries in pool
        val pool = allContents.groupBy { it.conceptId }.mapNotNull { (cId, contentsList) ->
            val combinedText = contentsList.map { it.text.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("، ")
            if (combinedText.isBlank()) null else {
                val semantic = QuizDistractorScorer.precomputeSemantic(combinedText)
                DistractorPoolItem(
                    conceptId = cId,
                    text = combinedText,
                    categoryId = categoryMap[cId],
                    entryType = entryTypeMap[cId] ?: EntryType.WORD,
                    difficulty = diffMap[cId] ?: VocabularyDifficulty.MEDIUM,
                    semantic = semantic
                )
            }
        }.distinctBy { it.semantic.compact }

        val questions = mutableListOf<QuizQuestion>()

        for (card in cards) {
            val correctAnswer = card.targetTranslations.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("، ")
            val correctSemantic = QuizDistractorScorer.precomputeSemantic(correctAnswer)
            val partsSemantics = (card.targetTranslations.map { it.trim() }.filter { it.isNotEmpty() } + QuizDistractorScorer.extractSegments(correctAnswer))
                .distinct()
                .map { QuizDistractorScorer.precomputeSemantic(it) }

            // Ultra-fast filter using precomputed semantics (0 regex/stemming calls inside loop)
            val nonCollidingPool = pool.filter { item ->
                item.conceptId != card.conceptId &&
                !QuizDistractorScorer.arePrecomputedColliding(item.semantic, correctSemantic) &&
                !partsSemantics.any { part -> QuizDistractorScorer.arePrecomputedColliding(item.semantic, part) }
            }

            // If pool is large, take a balanced sample to keep Levenshtein scoring instant (< 0.1ms per card)
            val candidatesToScore = if (nonCollidingPool.size > 40) {
                val cardCat = categoryMap[card.conceptId]
                val sameCat = if (cardCat != null) nonCollidingPool.filter { it.categoryId == cardCat } else emptyList()
                val otherCat = if (cardCat != null) nonCollidingPool.filter { it.categoryId != cardCat } else nonCollidingPool
                (sameCat.shuffled().take(15) + otherCat.shuffled().take(35)).distinctBy { it.conceptId }.take(40)
            } else {
                nonCollidingPool
            }

            data class ScoredCandidate(
                val poolItem: DistractorPoolItem,
                val confusabilityScore: Double
            )

            val scoredCandidates = candidatesToScore.map { cand ->
                val confusability = QuizDistractorScorer.calculateConfusability(
                    correctAnswer = correctAnswer,
                    candidateText = cand.text,
                    correctCategory = categoryMap[card.conceptId],
                    candidateCategory = cand.categoryId,
                    correctDifficulty = card.difficulty,
                    candidateDifficulty = cand.difficulty
                )
                ScoredCandidate(cand, confusability)
            }

            val sortedCandidates = when (quizLevel) {
                QuizLevel.EASY -> scoredCandidates.sortedBy { it.confusabilityScore }
                QuizLevel.MEDIUM -> scoredCandidates.sortedBy { kotlin.math.abs(it.confusabilityScore - 0.5) }
                QuizLevel.HARD -> scoredCandidates.sortedByDescending { it.confusabilityScore }
            }

            val chosenDistractors = mutableListOf<DistractorPoolItem>()
            for (cand in sortedCandidates) {
                if (chosenDistractors.size >= 3) break
                val collidesWithChosen = chosenDistractors.any { chosen ->
                    QuizDistractorScorer.arePrecomputedColliding(cand.poolItem.semantic, chosen.semantic)
                }
                if (!collidesWithChosen) {
                    chosenDistractors.add(cand.poolItem)
                }
            }

            if (chosenDistractors.size < 3) {
                for (cand in nonCollidingPool) {
                    if (chosenDistractors.size >= 3) break
                    val collidesWithChosen = chosenDistractors.any { chosen ->
                        QuizDistractorScorer.arePrecomputedColliding(cand.semantic, chosen.semantic)
                    }
                    if (!collidesWithChosen) {
                        chosenDistractors.add(cand)
                    }
                }
            }

            // Fallback 1: Relax collision check among chosen distractors if candidate does not collide with correct answer
            if (chosenDistractors.size < 3) {
                for (cand in nonCollidingPool) {
                    if (chosenDistractors.size >= 3) break
                    if (cand.text != correctAnswer && chosenDistractors.none { it.text == cand.text }) {
                        chosenDistractors.add(cand)
                    }
                }
            }

            // Fallback 2: If still < 3, fetch extra random items from database
            if (chosenDistractors.size < 3) {
                val extraFromDb = conceptDao.getRandomContents(targetLang, 100)
                for (extra in extraFromDb) {
                    if (chosenDistractors.size >= 3) break
                    val cleanText = extra.text.trim()
                    if (cleanText.isNotBlank() && cleanText != correctAnswer && chosenDistractors.none { it.text == cleanText }) {
                        chosenDistractors.add(
                            DistractorPoolItem(
                                conceptId = extra.conceptId,
                                text = cleanText,
                                categoryId = null,
                                entryType = EntryType.WORD,
                                difficulty = VocabularyDifficulty.MEDIUM,
                                semantic = QuizDistractorScorer.precomputeSemantic(cleanText)
                            )
                        )
                    }
                }
            }

            val chosenTexts = chosenDistractors.map { it.text }
            val optionsList = (chosenTexts.map { QuizOption(it, "") } + QuizOption(correctAnswer, card.conceptId)).shuffled()
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
                    difficulty = card.difficulty,
                    consecutiveCorrect = card.consecutiveCorrect,
                    consecutiveWrong = card.consecutiveWrong
                )
            )
        }
        questions
    }

    suspend fun submitAnswer(
        sessionId: String,
        conceptId: String,
        direction: CardDirection,
        isCorrect: Boolean,
        mode: ReviewMode,
        selectedIndex: Int? = null,
        correctIndex: Int? = null,
        optionsJson: String? = null,
        difficultyThreshold: Int = 3
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
            isCorrect = isCorrect,
            threshold = difficultyThreshold
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

        // Update position and item state in session
        if (session != null) {
            val sessionItems = reviewSessionDao.getItemsForSession(sessionId)
            val currentPos = session.currentPosition
            val curItem = sessionItems.getOrNull(currentPos)
            if (curItem != null) {
                reviewSessionDao.updateItemState(curItem.id, SessionItemState.ANSWERED)
            }
            reviewSessionDao.updateSession(
                session.copy(currentPosition = session.currentPosition + 1)
            )
        }

        // Check achievements trigger
        checkAchievements(sessionId)

        return SubmitResult(
            newStage = transition.newStage,
            nextReviewDay = transition.nextReviewDay,
            newDifficulty = diffResult.newDifficulty,
            isCorrect = isCorrect
        )
    }

    suspend fun checkAchievements(sessionId: String = "") {
        val today = ClockAndDayMath.todayDayString()
        val distinctDays = reviewSessionDao.getDistinctReviewedDays()
        val streak = ClockAndDayMath.calculateStreakDays(distinctDays, today)

        // 1. STREAK achievements
        if (streak >= 3) {
            achievementDao.unlock("STREAK_3_DAYS")
        }
        if (streak >= 7) {
            achievementDao.unlock("STREAK_7_DAYS")
        }
        if (streak >= 30) {
            achievementDao.unlock("STREAK_30_DAYS")
        }
        achievementDao.updateProgress("STREAK_3_DAYS", streak.coerceAtMost(3))
        achievementDao.updateProgress("STREAK_7_DAYS", streak.coerceAtMost(7))
        achievementDao.updateProgress("STREAK_30_DAYS", streak.coerceAtMost(30))

        // 2. Practiced words achievements (FIRST_TEN_WORDS & VOCABULARY_BUILDER)
        val practicedCount = learningDao.getTotalPracticedWordsCount()
        if (practicedCount >= 10) {
            achievementDao.unlock("FIRST_TEN_WORDS")
        }
        achievementDao.updateProgress("FIRST_TEN_WORDS", practicedCount.coerceAtMost(10))

        if (practicedCount >= 50) {
            achievementDao.unlock("VOCABULARY_BUILDER")
        }
        achievementDao.updateProgress("VOCABULARY_BUILDER", practicedCount.coerceAtMost(50))

        // 3. Long-term memory (LONG_TERM_MEMORY: 20 words in LEARNED stage)
        val learnedCount = learningDao.getTotalLearnedWordsCount()
        if (learnedCount >= 20) {
            achievementDao.unlock("LONG_TERM_MEMORY")
        }
        achievementDao.updateProgress("LONG_TERM_MEMORY", learnedCount.coerceAtMost(20))

        // 4. Hard Master (HARD_MASTER: 5 very hard words conquered)
        val hardMastered = learningDao.getMasteredHardWordsCount()
        if (hardMastered >= 5) {
            achievementDao.unlock("HARD_MASTER")
        }
        achievementDao.updateProgress("HARD_MASTER", hardMastered.coerceAtMost(5))

        // 5. Quiz Ace (QUIZ_ACE: 100% correct in a quiz session of 10 questions)
        val session = reviewSessionDao.getSessionById(sessionId)
        if (session != null && session.mode == ReviewMode.QUIZ) {
            val history = reviewSessionDao.getHistoryForSession(sessionId)
            if (history.size >= 10 && history.all { it.isCorrect }) {
                achievementDao.unlock("QUIZ_ACE")
                achievementDao.updateProgress("QUIZ_ACE", 1)
            }
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
        // Re-check quiz ace on session completion
        if (session.mode == ReviewMode.QUIZ) {
            val history = reviewSessionDao.getHistoryForSession(sessionId)
            if (history.size >= 10 && history.all { it.isCorrect }) {
                achievementDao.unlock("QUIZ_ACE")
                achievementDao.updateProgress("QUIZ_ACE", 1)
            }
        }
    }

    suspend fun abandonSession(sessionId: String) {
        val session = reviewSessionDao.getSessionById(sessionId) ?: return
        if (session.status == SessionStatus.ACTIVE) {
            reviewSessionDao.updateSession(
                session.copy(
                    status = SessionStatus.ABANDONED,
                    endedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun getAllAchievementsFlow() = achievementDao.getAllAchievementsFlow()
}
