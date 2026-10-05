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

private val QUIZ_TOKEN_SPLIT = Regex("[^\\p{L}\\p{N}]+")

private fun normalizeQuiz(text: String): String =
    Normalizer.normalize(text.trim(), Normalizer.Form.NFC).lowercase(Locale.ROOT)

private fun levenshteinSimilarity(a: String, b: String): Double {
    if (a == b) return 1.0
    if (a.isEmpty() || b.isEmpty()) return 0.0
    var previous = IntArray(b.length + 1) { it }
    var current = IntArray(b.length + 1)
    for (i in a.indices) {
        current[0] = i + 1
        for (j in b.indices) {
            val sub = previous[j] + if (a[i] == b[j]) 0 else 1
            current[j + 1] = minOf(previous[j + 1] + 1, current[j] + 1, sub)
        }
        val tmp = previous
        previous = current
        current = tmp
    }
    val distance = previous[b.length]
    return 1.0 - (distance.toDouble() / maxOf(a.length, b.length).toDouble()).coerceIn(0.0, 1.0)
}

private fun quizLexicalSimilarity(a: String, b: String): Double {
    val normA = normalizeQuiz(a)
    val normB = normalizeQuiz(b)
    if (normA == normB) return 1.0
    val tokensA = normA.split(QUIZ_TOKEN_SPLIT).filter { it.isNotBlank() }.toSet()
    val tokensB = normB.split(QUIZ_TOKEN_SPLIT).filter { it.isNotBlank() }.toSet()
    val tokenSim = if (tokensA.isEmpty() && tokensB.isEmpty()) 1.0
    else if (tokensA.isEmpty() || tokensB.isEmpty()) 0.0
    else tokensA.intersect(tokensB).size.toDouble() / tokensA.union(tokensB).size.toDouble()

    val levSim = levenshteinSimilarity(normA, normB)
    return (tokenSim * 0.5 + levSim * 0.5).coerceIn(0.0, 1.0)
}

private fun diffDistance(d1: VocabularyDifficulty, d2: VocabularyDifficulty): Int {
    return kotlin.math.abs(d1.ordinal - d2.ordinal)
}

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
        val session = reviewSessionDao.getSessionById(sessionId)
        val quizLevel = session?.quizLevel ?: QuizLevel.MEDIUM
        val cards = fetchCardsForSession(sessionId)
        if (cards.isEmpty()) return emptyList()

        // Distractor candidate entries
        val targetLang = if (session?.direction == CardDirection.REVERSE) "es" else "fa"
        val allConcepts = conceptDao.searchConcepts("", 500)
        val allContents = conceptDao.getAllContents().filter { it.languageCode == targetLang && it.text.isNotBlank() }
        val categoryMap = allConcepts.associate { it.id to it.categoryId }
        val entryTypeMap = allConcepts.associate { it.id to it.entryType }
        val diffMap = learningDao.getAllDifficultyStates()
            .filter { it.direction == (session?.direction ?: CardDirection.NORMAL) }
            .associate { it.conceptId to it.current }

        data class DistractorPoolItem(
            val conceptId: String,
            val text: String,
            val categoryId: String?,
            val entryType: EntryType,
            val difficulty: VocabularyDifficulty
        )

        val pool = allContents.map { content ->
            DistractorPoolItem(
                conceptId = content.conceptId,
                text = content.text.trim(),
                categoryId = categoryMap[content.conceptId],
                entryType = entryTypeMap[content.conceptId] ?: EntryType.WORD,
                difficulty = diffMap[content.conceptId] ?: VocabularyDifficulty.MEDIUM
            )
        }.distinctBy { normalizeQuiz(it.text) }

        val questions = mutableListOf<QuizQuestion>()

        for (card in cards) {
            val correctAnswer = card.targetTranslations.firstOrNull()?.trim() ?: ""
            val allCorrect = card.targetTranslations.map { it.trim() }

            val candidatePool = pool.filter { item ->
                item.conceptId != card.conceptId &&
                !allCorrect.any { correct -> QuizDistractorScorer.areSemanticallyColliding(item.text, correct) }
            }

            data class ScoredCandidate(
                val text: String,
                val confusabilityScore: Double
            )

            val scoredCandidates = candidatePool.map { cand ->
                val lexSim = quizLexicalSimilarity(correctAnswer, cand.text)
                val catMatch = if (cand.categoryId != null && cand.categoryId == categoryMap[card.conceptId]) 1.0 else 0.0
                val diffDist = diffDistance(card.difficulty, cand.difficulty)
                val diffBonus = (3 - diffDist).coerceAtLeast(0) / 3.0

                val confusability = (lexSim * 0.4 + catMatch * 0.35 + diffBonus * 0.25).coerceIn(0.0, 1.0)
                ScoredCandidate(cand.text, confusability)
            }

            // Select 3 distractors based on QuizLevel:
            // EASY: lower confusability, clear differences
            // MEDIUM: moderate confusability, balanced plausibility
            // HARD: highest confusability, closest semantic/lexical similarity
            val sortedCandidates = when (quizLevel) {
                QuizLevel.EASY -> scoredCandidates.sortedBy { it.confusabilityScore }
                QuizLevel.MEDIUM -> scoredCandidates.sortedBy { kotlin.math.abs(it.confusabilityScore - 0.5) }
                QuizLevel.HARD -> scoredCandidates.sortedByDescending { it.confusabilityScore }
            }

            val chosenDistractors = mutableListOf<String>()
            for (cand in sortedCandidates) {
                if (chosenDistractors.size >= 3) break
                val collides = chosenDistractors.any { chosen ->
                    QuizDistractorScorer.areSemanticallyColliding(cand.text, chosen)
                }
                if (!collides) {
                    chosenDistractors.add(cand.text)
                }
            }

            if (chosenDistractors.size < 3) {
                for (cand in candidatePool) {
                    if (chosenDistractors.size >= 3) break
                    val collides = chosenDistractors.any { chosen ->
                        QuizDistractorScorer.areSemanticallyColliding(cand.text, chosen)
                    }
                    if (!collides) {
                        chosenDistractors.add(cand.text)
                    }
                }
            }

            val optionsList = (chosenDistractors.map { QuizOption(it, "") } + QuizOption(correctAnswer, card.conceptId)).shuffled()
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

        // Update position in session
        if (session != null) {
            reviewSessionDao.updateSession(
                session.copy(currentPosition = session.currentPosition + 1)
            )
        }

        // Check achievements trigger
        checkAchievements(sessionId, isCorrect)

        return SubmitResult(
            newStage = transition.newStage,
            nextReviewDay = transition.nextReviewDay,
            newDifficulty = diffResult.newDifficulty,
            isCorrect = isCorrect
        )
    }

    private suspend fun checkAchievements(sessionId: String, isCorrect: Boolean) {
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

        // 5. Quiz Ace (QUIZ_ACE: 100% correct in a quiz session of 5+ questions)
        val session = reviewSessionDao.getSessionById(sessionId)
        if (session != null && session.mode == ReviewMode.QUIZ) {
            val history = reviewSessionDao.getHistoryForSession(sessionId)
            if (history.size >= 5 && history.all { it.isCorrect }) {
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
            if (history.size >= 5 && history.all { it.isCorrect }) {
                achievementDao.unlock("QUIZ_ACE")
                achievementDao.updateProgress("QUIZ_ACE", 1)
            }
        }
    }
}
