package com.manidigit.yadin.data.repository

import com.manidigit.yadin.data.local.dao.AchievementDao
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.database.YadinDatabase
import androidx.room.withTransaction
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.domain.algorithm.DifficultyCalculator
import com.manidigit.yadin.domain.algorithm.FsrsAlgorithm
import com.manidigit.yadin.domain.algorithm.FsrsRating
import com.manidigit.yadin.domain.algorithm.FsrsState
import com.manidigit.yadin.domain.algorithm.LearningTransition
import com.manidigit.yadin.domain.algorithm.QuizDistractorScorer
import com.manidigit.yadin.domain.algorithm.Sm2Algorithm
import com.manidigit.yadin.domain.algorithm.Sm2Quality
import com.manidigit.yadin.domain.algorithm.Sm2State
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.LanguageConstants
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.QuizOption
import com.manidigit.yadin.domain.model.QuizQuestion
import com.manidigit.yadin.domain.model.ReviewCard
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewSession
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SessionItemState
import com.manidigit.yadin.domain.model.SessionStatus
import com.manidigit.yadin.domain.model.SpacedRepetitionAlgorithm
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.time.ClockAndDayMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class SubmitResult(
    val newStage: Stage,
    val nextReviewDay: String?,
    val newDifficulty: VocabularyDifficulty,
    val isCorrect: Boolean,
    val consecutiveCorrect: Int = 0,
    val consecutiveWrong: Int = 0
)

data class ReviewFilters(
    val reviewType: ReviewType = ReviewType.DAILY,
    val mode: ReviewMode = ReviewMode.FLASHCARD,
    val direction: CardDirection = CardDirection.NORMAL,
    val quizLevel: QuizLevel? = null,
    val difficulties: Set<VocabularyDifficulty> = emptySet(),
    val categoryIds: Set<String> = emptySet(),
    val entryTypes: Set<EntryType> = emptySet(),
    val maxCards: Int = 20,
    val algorithm: SpacedRepetitionAlgorithm = SpacedRepetitionAlgorithm.YADIN,
    val fsrsDesiredRetention: Double = FsrsAlgorithm.DEFAULT_DESIRED_RETENTION
)

class ReviewRepository(
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val reviewSessionDao: ReviewSessionDao,
    private val achievementDao: AchievementDao,
    private val database: YadinDatabase? = null,
    private val settingsDao: com.manidigit.yadin.data.local.dao.SettingsDao? = database?.settingsDao()
) {

    private suspend fun <T> runInTransaction(block: suspend () -> T): T {
        val db = database
        return if (db != null) {
            db.withTransaction { block() }
        } else {
            block()
        }
    }

    private fun expandEntryTypes(types: Set<EntryType>): List<String> {
        if (types.isEmpty()) return emptyList()
        val expanded = mutableSetOf<String>()
        for (t in types) {
            expanded.add(t.name)
            if (t == EntryType.PHRASE || t == EntryType.IDIOM) {
                expanded.add(EntryType.PHRASE.name)
                expanded.add(EntryType.IDIOM.name)
                expanded.add(EntryType.COLLOCATION.name)
                expanded.add(EntryType.STRUCTURE.name)
            }
        }
        return expanded.toList()
    }

    suspend fun countCandidates(filters: ReviewFilters): Int {
        val today = ClockAndDayMath.todayDayString()
        val expandedTypes = expandEntryTypes(filters.entryTypes)
        val hasDiffFilter = filters.difficulties.isNotEmpty() && filters.difficulties.size < VocabularyDifficulty.entries.size
        val hasCatFilter = filters.categoryIds.isNotEmpty()
        val hasTypeFilter = expandedTypes.isNotEmpty()
        return learningDao.countFilteredCandidates(
            direction = filters.direction,
            reviewType = filters.reviewType.name,
            todayDayString = today,
            hasDifficultyFilter = if (hasDiffFilter) 1 else 0,
            difficulties = if (hasDiffFilter) filters.difficulties.map { it.name } else listOf(""),
            hasCategoryFilter = if (hasCatFilter) 1 else 0,
            categoryIds = if (hasCatFilter) filters.categoryIds.toList() else listOf(""),
            hasEntryTypeFilter = if (hasTypeFilter) 1 else 0,
            entryTypes = if (hasTypeFilter) expandedTypes else listOf("")
        )
    }

    suspend fun createFilteredSession(filters: ReviewFilters): ReviewSession = runInTransaction {
        // ISS-42: Clean up any prior orphan active sessions before starting a new session
        abandonOrphanActiveSessions()

        val today = ClockAndDayMath.todayDayString()
        val expandedTypes = expandEntryTypes(filters.entryTypes)
        val hasDiffFilter = filters.difficulties.isNotEmpty() && filters.difficulties.size < VocabularyDifficulty.entries.size
        val hasCatFilter = filters.categoryIds.isNotEmpty()
        val hasTypeFilter = expandedTypes.isNotEmpty()
        val conceptIds = learningDao.getFilteredCandidateConceptIds(
            direction = filters.direction,
            reviewType = filters.reviewType.name,
            todayDayString = today,
            hasDifficultyFilter = if (hasDiffFilter) 1 else 0,
            difficulties = if (hasDiffFilter) filters.difficulties.map { it.name } else listOf(""),
            hasCategoryFilter = if (hasCatFilter) 1 else 0,
            categoryIds = if (hasCatFilter) filters.categoryIds.toList() else listOf(""),
            limit = filters.maxCards,
            hasEntryTypeFilter = if (hasTypeFilter) 1 else 0,
            entryTypes = if (hasTypeFilter) expandedTypes else listOf("")
        )

        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // ISS-43: If no candidate words found, do not persist an empty ACTIVE session into database
        if (conceptIds.isEmpty()) {
            return@runInTransaction ReviewSession(
                id = sessionId,
                startedAt = now,
                endedAt = now,
                reviewType = filters.reviewType,
                mode = filters.mode,
                direction = filters.direction,
                quizLevel = filters.quizLevel,
                status = SessionStatus.COMPLETED,
                currentPosition = 0,
                totalItems = 0
            )
        }

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

        ReviewSession(
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

    suspend fun abandonOrphanActiveSessions() {
        reviewSessionDao.abandonAllActiveSessions(System.currentTimeMillis())
    }


    suspend fun fetchCardsForSession(sessionId: String): List<ReviewCard> {
        val items = reviewSessionDao.getItemsForSession(sessionId)
        val cards = mutableListOf<ReviewCard>()
        var orphanCount = 0

        val baseTargetLang = LanguageConstants.DEFAULT_TARGET_LANGUAGE

        for (item in items) {
            val concept = conceptDao.getConceptById(item.conceptId)
            if (concept == null) {
                reviewSessionDao.deleteSessionItemById(item.id)
                orphanCount++
                continue
            }
            val contents = conceptDao.getContentsForConcept(item.conceptId)
                .filter { it.text.isNotBlank() }

            val sourceContents = contents.filter { it.languageCode != baseTargetLang }
            val targetContents = contents.filter { it.languageCode == baseTargetLang }

            val primarySource = sourceContents.firstOrNull()
            val targetTranslations = targetContents.map { it.text.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
            if (primarySource == null || targetTranslations.isEmpty()) {
                reviewSessionDao.deleteSessionItemById(item.id)
                orphanCount++
                continue
            }

            val sourceVariants = sourceContents.map { it.text.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .ifEmpty { listOf(primarySource.text.trim()) }

            val learning = learningDao.getLearningState(item.conceptId, item.direction)
            val stage = learning?.stage ?: Stage.DAILY
            val diffState = learningDao.getDifficultyState(item.conceptId, item.direction)
            val diff = diffState?.current ?: VocabularyDifficulty.EASY
            val category = concept.categoryId?.let { conceptDao.getCategoryById(it)?.name }

            val prompt = if (item.direction == CardDirection.NORMAL) {
                sourceVariants.joinToString(" / ")
            } else {
                targetTranslations.joinToString("، ")
            }
            val answers = if (item.direction == CardDirection.NORMAL) {
                targetTranslations
            } else {
                sourceVariants
            }
            val cardNote = primarySource.note
                ?: targetContents.firstOrNull { !it.note.isNullOrBlank() }?.note

            cards.add(
                ReviewCard(
                    conceptId = item.conceptId,
                    direction = item.direction,
                    sourceText = prompt,
                    targetTranslations = answers,
                    note = cardNote,
                    categoryName = category,
                    stage = stage,
                    difficulty = diff,
                    consecutiveCorrect = diffState?.consecutiveCorrect ?: 0,
                    consecutiveWrong = diffState?.consecutiveWrong ?: 0,
                    sm2Repetitions = learning?.sm2Repetitions ?: 0,
                    sm2EasinessFactor = learning?.sm2EasinessFactor ?: Sm2Algorithm.DEFAULT_EASINESS_FACTOR,
                    sm2IntervalDays = learning?.sm2IntervalDays ?: 0,
                    fsrsStability = learning?.fsrsStability ?: 0.0,
                    fsrsDifficulty = learning?.fsrsDifficulty ?: FsrsAlgorithm.DEFAULT_INITIAL_DIFFICULTY,
                    fsrsReps = learning?.fsrsReps ?: 0,
                    fsrsLapses = learning?.fsrsLapses ?: 0,
                    fsrsState = learning?.fsrsState ?: 0,
                    lastReviewedDay = learning?.lastReviewedDay
                )
            )
        }

        if (orphanCount > 0) {
            val currentSession = reviewSessionDao.getSessionById(sessionId)
            if (currentSession != null) {
                reviewSessionDao.updateSession(
                    currentSession.copy(totalItems = cards.size)
                )
            }
        }

        return cards
    }

    suspend fun generateQuizQuestions(sessionId: String): List<QuizQuestion> = withContext(Dispatchers.Default) {
        val session = reviewSessionDao.getSessionById(sessionId)
        val quizLevel = session?.quizLevel ?: QuizLevel.NORMAL
        val cards = fetchCardsForSession(sessionId)
        if (cards.isEmpty()) return@withContext emptyList()

        // Distractor candidate entries (ISS-45: Dynamic target language resolution)
        val targetLang = (session?.direction ?: CardDirection.NORMAL).targetLanguage()
        val sessionConceptIds = cards.map { it.conceptId }.toSet()

        // Fetch session concepts' categories first so same-category distractors are guaranteed in the pool (especially for PROFESSIONAL level)
        val sessionConcepts = sessionConceptIds.chunked(300).flatMap { conceptDao.getConceptsByIds(it) }
        val sessionCategoryIds = sessionConcepts.mapNotNull { it.categoryId }.distinct()
        val categoryCandidateContents = if (sessionCategoryIds.isNotEmpty()) {
            conceptDao.getContentsByCategories(targetLang, sessionCategoryIds, limit = 600)
        } else {
            emptyList()
        }

        // Combine category-targeted candidates with global random sampling (800 items) for wide coverage
        val randomCandidateContents = conceptDao.getRandomContents(targetLang, 800)

        val candidateConceptIds = (
            sessionConceptIds +
            categoryCandidateContents.map { it.conceptId } +
            randomCandidateContents.map { it.conceptId }
        ).distinct()
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

        val sessionCategorySet = sessionCategoryIds.toSet()

        // Precompute semantics ONCE for all unique concept translation entries in pool,
        // prioritizing candidates from session categories before deduplicating by compact key
        val pool = allContents.groupBy { it.conceptId }.mapNotNull { (cId, contentsList) ->
            val combinedText = contentsList.map { it.text.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("، ")
            if (combinedText.isBlank()) null else {
                val semantic = QuizDistractorScorer.precomputeSemantic(combinedText)
                DistractorPoolItem(
                    conceptId = cId,
                    text = combinedText,
                    categoryId = categoryMap[cId],
                    entryType = entryTypeMap[cId] ?: EntryType.WORD,
                    difficulty = diffMap[cId] ?: VocabularyDifficulty.EASY,
                    semantic = semantic
                )
            }
        }
            .sortedByDescending { it.categoryId != null && it.categoryId in sessionCategorySet }
            .distinctBy { it.semantic.compact }

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

            // Prioritize all same-category and same-entryType candidates so PROFESSIONAL level finds the most confusable distractors
            val candidatesToScore = if (nonCollidingPool.size > 240) {
                val cardCat = categoryMap[card.conceptId]
                val cardEntryType = entryTypeMap[card.conceptId] ?: EntryType.WORD
                val sameTypeAndCat = if (cardCat != null) nonCollidingPool.filter { it.categoryId == cardCat && it.entryType == cardEntryType } else emptyList()
                val sameCat = if (cardCat != null) nonCollidingPool.filter { it.categoryId == cardCat } else emptyList()
                val sameType = nonCollidingPool.filter { it.entryType == cardEntryType }
                val otherCat = if (cardCat != null) nonCollidingPool.filter { it.categoryId != cardCat } else nonCollidingPool
                (sameTypeAndCat.shuffled().take(100) + sameCat.shuffled().take(40) + sameType.shuffled().take(60) + otherCat.shuffled().take(40))
                    .distinctBy { it.conceptId }
                    .take(240)
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
                QuizLevel.BEGINNER -> scoredCandidates.sortedBy { it.confusabilityScore }
                QuizLevel.NORMAL -> scoredCandidates.sortedBy { kotlin.math.abs(it.confusabilityScore - 0.5) }
                QuizLevel.PROFESSIONAL -> scoredCandidates.sortedByDescending { it.confusabilityScore }
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

            // If after checking nonCollidingPool we have fewer than 3 distractors,
            // according to Spec 6.6 step 5 and Decision D0.2, random filling is prohibited:
            // this question strictly falls back to flashcard and is omitted from 4-option quiz questions.
            if (chosenDistractors.size < 3) {
                reviewSessionDao.getItemForSessionAndConcept(sessionId, card.conceptId)?.let { item ->
                    reviewSessionDao.updateItemState(item.id, SessionItemState.SKIPPED)
                }
                continue
            }

            val chosenOptions = chosenDistractors.map { QuizOption(it.text, it.conceptId) }
            val optionsList = (chosenOptions + QuizOption(correctAnswer, card.conceptId)).shuffled()
            val distinctNormOptions = optionsList.map { it.text.trim().lowercase() }.distinct()
            
            // Spec 6.6 step 8 final validation: exactly 4 pairwise-distinct options
            if (optionsList.size != 4 || distinctNormOptions.size != 4) {
                reviewSessionDao.getItemForSessionAndConcept(sessionId, card.conceptId)?.let { item ->
                    reviewSessionDao.updateItemState(item.id, SessionItemState.SKIPPED)
                }
                continue
            }

            val correctIdx = optionsList.indexOfFirst { it.conceptId == card.conceptId }
                .takeIf { it >= 0 }
                ?: optionsList.indexOfFirst { it.text == correctAnswer }.coerceAtLeast(0)

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

        if (questions.isEmpty()) {
            // If all questions lacked distractors and session falls back to flashcards, restore items to PENDING
            reviewSessionDao.updateSessionItemsStateWhere(
                sessionId = sessionId,
                currentState = SessionItemState.SKIPPED,
                newState = SessionItemState.PENDING
            )
        } else if (questions.size != cards.size) {
            val currentSession = reviewSessionDao.getSessionById(sessionId)
            if (currentSession != null) {
                reviewSessionDao.updateSession(
                    currentSession.copy(totalItems = questions.size)
                )
            }
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
        difficultyThreshold: Int = 3,
        reviewAttemptId: String = UUID.randomUUID().toString(),
        sm2Quality: Sm2Quality? = null,
        algorithm: SpacedRepetitionAlgorithm = SpacedRepetitionAlgorithm.YADIN,
        fsrsDesiredRetention: Double = FsrsAlgorithm.DEFAULT_DESIRED_RETENTION
    ): SubmitResult {
        val sessionCheck = reviewSessionDao.getSessionById(sessionId)
        val persistedThreshold = settingsDao?.getSetting("difficultyThreshold")?.toIntOrNull()
        val effectiveThreshold = (persistedThreshold ?: difficultyThreshold).coerceIn(1, 5)

        val today = ClockAndDayMath.todayDayString()
        val now = System.currentTimeMillis()
        val useFsrs = algorithm == SpacedRepetitionAlgorithm.FSRS ||
            sessionCheck?.reviewType == ReviewType.FSRS
        val useSm2 = !useFsrs && (
            algorithm == SpacedRepetitionAlgorithm.SM2 ||
                sessionCheck?.reviewType == ReviewType.SM2 ||
                sm2Quality != null
            )

        val submitResult = runInTransaction {
            // Atomic idempotency guard inside transaction: If this exact attempt was already recorded, return latest state with counters
            val existingAttempt = reviewSessionDao.getHistoryByAttemptId(reviewAttemptId)
            if (existingAttempt != null) {
                val curLearning = learningDao.getLearningState(conceptId, direction)
                val curDiff = learningDao.getDifficultyState(conceptId, direction)
                return@runInTransaction SubmitResult(
                    newStage = curLearning?.stage ?: Stage.DAILY,
                    nextReviewDay = curLearning?.nextReviewDay,
                    newDifficulty = curDiff?.current ?: VocabularyDifficulty.EASY,
                    isCorrect = existingAttempt.isCorrect,
                    consecutiveCorrect = curDiff?.consecutiveCorrect ?: 0,
                    consecutiveWrong = curDiff?.consecutiveWrong ?: 0
                )
            }

            val currentLearning = learningDao.getLearningState(conceptId, direction)
            val currentStage = currentLearning?.stage ?: Stage.DAILY

            val finalStage: Stage
            val finalNextReviewDay: String?
            val finalLastReviewedDay: String
            var finalSm2Reps: Int = currentLearning?.sm2Repetitions ?: 0
            var finalSm2Ef: Double = currentLearning?.sm2EasinessFactor ?: Sm2Algorithm.DEFAULT_EASINESS_FACTOR
            var finalSm2Interval: Int = currentLearning?.sm2IntervalDays ?: 0
            var finalFsrsStability: Double = currentLearning?.fsrsStability ?: 0.0
            var finalFsrsDifficulty: Double = currentLearning?.fsrsDifficulty ?: FsrsAlgorithm.DEFAULT_INITIAL_DIFFICULTY
            var finalFsrsReps: Int = currentLearning?.fsrsReps ?: 0
            var finalFsrsLapses: Int = currentLearning?.fsrsLapses ?: 0
            var finalFsrsState: Int = currentLearning?.fsrsState ?: 0

            if (useFsrs) {
                val curFsrs = FsrsAlgorithm.resolveEffectiveState(
                    state = FsrsState(
                        stability = finalFsrsStability,
                        difficulty = finalFsrsDifficulty,
                        reps = finalFsrsReps,
                        lapses = finalFsrsLapses,
                        state = finalFsrsState
                    ),
                    stage = currentStage,
                    lastReviewedDay = currentLearning?.lastReviewedDay,
                    sm2IntervalDays = finalSm2Interval,
                    sm2Repetitions = finalSm2Reps
                )
                val effectiveRating = sm2Quality?.let { FsrsRating.fromSm2Quality(it) }
                    ?: FsrsRating.fromBoolean(isCorrect)
                val fsrsRes = FsrsAlgorithm.calculateNextReview(
                    currentState = curFsrs,
                    rating = effectiveRating,
                    lastReviewedDay = currentLearning?.lastReviewedDay,
                    todayDayString = today,
                    desiredRetention = fsrsDesiredRetention
                )
                finalStage = fsrsRes.mappedStage
                finalNextReviewDay = fsrsRes.nextReviewDay
                finalLastReviewedDay = fsrsRes.lastReviewedDay
                finalFsrsStability = fsrsRes.nextState.stability
                finalFsrsDifficulty = fsrsRes.nextState.difficulty
                finalFsrsReps = fsrsRes.nextState.reps
                finalFsrsLapses = fsrsRes.nextState.lapses
                finalFsrsState = fsrsRes.nextState.state
            } else if (useSm2) {
                val curSm2 = Sm2Algorithm.resolveEffectiveState(
                    state = Sm2State(
                        repetitions = finalSm2Reps,
                        easinessFactor = finalSm2Ef,
                        intervalDays = finalSm2Interval
                    ),
                    stage = currentStage,
                    lastReviewedDay = currentLearning?.lastReviewedDay,
                    fsrsStability = finalFsrsStability
                )
                val effectiveQuality = sm2Quality ?: Sm2Quality.fromBoolean(isCorrect)
                val sm2Res = Sm2Algorithm.calculateNextReview(
                    currentState = curSm2,
                    quality = effectiveQuality,
                    todayDayString = today
                )
                finalStage = sm2Res.mappedStage
                finalNextReviewDay = sm2Res.nextReviewDay
                finalLastReviewedDay = sm2Res.lastReviewedDay
                finalSm2Reps = sm2Res.nextState.repetitions
                finalSm2Ef = sm2Res.nextState.easinessFactor
                finalSm2Interval = sm2Res.nextState.intervalDays
            } else {
                val transition = LearningTransition.calculateNextStage(
                    currentStage = currentStage,
                    isCorrect = isCorrect,
                    todayDayString = today
                )
                finalStage = transition.newStage
                finalNextReviewDay = transition.nextReviewDay
                finalLastReviewedDay = today
            }

            val updatedLearning = (currentLearning ?: com.manidigit.yadin.data.local.entity.LearningStateEntity(
                id = UUID.randomUUID().toString(),
                conceptId = conceptId,
                direction = direction
            )).copy(
                stage = finalStage,
                nextReviewDay = finalNextReviewDay,
                lastReviewedDay = finalLastReviewedDay,
                updatedAt = now,
                sm2Repetitions = finalSm2Reps,
                sm2EasinessFactor = finalSm2Ef,
                sm2IntervalDays = finalSm2Interval,
                fsrsStability = finalFsrsStability,
                fsrsDifficulty = finalFsrsDifficulty,
                fsrsReps = finalFsrsReps,
                fsrsLapses = finalFsrsLapses,
                fsrsState = finalFsrsState
            )
            learningDao.insertLearningState(updatedLearning)

            val currentDiff = learningDao.getDifficultyState(conceptId, direction)
            val curDifficultyEnum = currentDiff?.current ?: VocabularyDifficulty.EASY
            val consecutiveCorrect = currentDiff?.consecutiveCorrect ?: 0
            val consecutiveWrong = currentDiff?.consecutiveWrong ?: 0
            val hasReachedVeryHard = currentDiff?.hasReachedVeryHard ?: false

            val diffResult = if (sm2Quality == Sm2Quality.HARD) {
                DifficultyCalculator.updateDifficultyForHardRating(
                    current = curDifficultyEnum,
                    consecutiveCorrect = consecutiveCorrect,
                    consecutiveWrong = consecutiveWrong,
                    hasReachedVeryHard = hasReachedVeryHard,
                    threshold = effectiveThreshold
                )
            } else {
                val isCorrectForDifficulty = when (sm2Quality) {
                    Sm2Quality.AGAIN -> false
                    Sm2Quality.GOOD, Sm2Quality.EASY -> true
                    else -> isCorrect
                }
                DifficultyCalculator.updateDifficulty(
                    current = curDifficultyEnum,
                    consecutiveCorrect = consecutiveCorrect,
                    consecutiveWrong = consecutiveWrong,
                    hasReachedVeryHard = hasReachedVeryHard,
                    isCorrect = isCorrectForDifficulty,
                    threshold = effectiveThreshold
                )
            }

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
                reviewAttemptId = reviewAttemptId,
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

            // Update item state for this concept in the session
            val matchedItem = reviewSessionDao.getItemForSessionAndConcept(sessionId, conceptId)
            if (matchedItem != null) {
                reviewSessionDao.updateItemState(matchedItem.id, SessionItemState.ANSWERED)
            }

            // Fresh read of session inside transaction to avoid race condition and preserve status
            val currentDbSession = reviewSessionDao.getSessionById(sessionId)
            if (currentDbSession != null) {
                val sessionItems = reviewSessionDao.getItemsForSession(sessionId)
                val answeredCount = sessionItems.count { it.state == SessionItemState.ANSWERED }
                reviewSessionDao.updateSession(
                    currentDbSession.copy(
                        currentPosition = answeredCount.coerceAtMost(currentDbSession.totalItems),
                        status = if (currentDbSession.status == SessionStatus.COMPLETED) SessionStatus.COMPLETED else currentDbSession.status
                    )
                )
            }

            SubmitResult(
                newStage = finalStage,
                nextReviewDay = finalNextReviewDay,
                newDifficulty = diffResult.newDifficulty,
                isCorrect = isCorrect,
                consecutiveCorrect = diffResult.consecutiveCorrect,
                consecutiveWrong = diffResult.consecutiveWrong
            )
        }

        // Check achievements outside transaction
        checkAchievements(sessionId)

        return submitResult
    }

    suspend fun checkAchievements(sessionId: String = "") {
        // اطمینان از وجود تمام انوم‌های دستاورد در پایگاه داده جهت جلوگیری از عدم ثبت
        val defaultAchievements = AchievementId.values().map {
            com.manidigit.yadin.data.local.entity.AchievementEntity(id = it.name, unlockedAt = null, progress = 0)
        }
        achievementDao.insertAchievements(defaultAchievements)

        val today = ClockAndDayMath.todayDayString()
        val distinctDays = reviewSessionDao.getDistinctReviewedDays()
        val streak = ClockAndDayMath.calculateStreakDays(distinctDays, today)

        // ۱. رگبار و مداومت (STREAK: 7, 30, 100, 365 days)
        val streakTiers = listOf(
            AchievementId.STREAK_3_DAYS,
            AchievementId.STREAK_7_DAYS,
            AchievementId.STREAK_30_DAYS,
            AchievementId.STREAK_100_DAYS
        )
        for (ach in streakTiers) {
            val target = ach.threshold
            if (streak >= target) {
                achievementDao.unlock(ach.name)
            } else {
                achievementDao.relock(ach.name)
            }
            achievementDao.updateProgress(ach.name, streak.coerceAtMost(target))
        }

        // استخراج مشخصات نشست و جهت مرور در صورت وجود
        val session = if (sessionId.isNotBlank()) reviewSessionDao.getSessionById(sessionId) else null
        val sessionDirection = session?.direction

        // ۲. دایره واژگان (VOCABULARY: 100, 500, 2000, 5000 words practiced)
        val practicedCount = if (sessionDirection != null) {
            maxOf(learningDao.getTotalPracticedWordsCount(sessionDirection), learningDao.getTotalPracticedWordsCount())
        } else {
            learningDao.getTotalPracticedWordsCount()
        }
        val vocabTiers = listOf(
            AchievementId.FIRST_TEN_WORDS,
            AchievementId.VOCABULARY_BUILDER,
            AchievementId.VOCABULARY_MASTER,
            AchievementId.VOCABULARY_LEGEND
        )
        for (ach in vocabTiers) {
            val target = ach.threshold
            if (practicedCount >= target) {
                achievementDao.unlock(ach.name)
            } else {
                achievementDao.relock(ach.name)
            }
            achievementDao.updateProgress(ach.name, practicedCount.coerceAtMost(target))
        }

        // ۳. تثبیت لایتنر و حافظه دائمی (RETENTION: 100, 500, 2000, 5000 words in LEARNED)
        val learnedCount = if (sessionDirection != null) {
            maxOf(learningDao.getTotalLearnedWordsCount(sessionDirection), learningDao.getTotalLearnedWordsCount())
        } else {
            learningDao.getTotalLearnedWordsCount()
        }
        val retentionTiers = listOf(
            AchievementId.LONG_TERM_MEMORY,
            AchievementId.RETENTION_SILVER,
            AchievementId.RETENTION_GOLD,
            AchievementId.RETENTION_PLATINUM
        )
        for (ach in retentionTiers) {
            val target = ach.threshold
            if (learnedCount >= target) {
                achievementDao.unlock(ach.name)
            } else {
                achievementDao.relock(ach.name)
            }
            achievementDao.updateProgress(ach.name, learnedCount.coerceAtMost(target))
        }

        // ۴. واژگان سخت (HARD_WORDS: 10, 50, 150, 500 words mastered)
        val hardMastered = if (sessionDirection != null) {
            maxOf(learningDao.getMasteredHardWordsCount(sessionDirection), learningDao.getMasteredHardWordsCount())
        } else {
            learningDao.getMasteredHardWordsCount()
        }
        val hardTiers = listOf(
            AchievementId.HARD_MASTER,
            AchievementId.HARD_SILVER,
            AchievementId.HARD_GOLD,
            AchievementId.HARD_PLATINUM
        )
        for (ach in hardTiers) {
            val target = ach.threshold
            if (hardMastered >= target) {
                achievementDao.unlock(ach.name)
            } else {
                achievementDao.relock(ach.name)
            }
            achievementDao.updateProgress(ach.name, hardMastered.coerceAtMost(target))
        }

        // ۵. مهارت کوییز (QUIZ: 5, 20, 50, 100 perfect quiz sessions with >= 5 questions)
        val perfectQuizCount = reviewSessionDao.getPerfectQuizSessionsCount(minQuestions = 5)
        val quizTiers = listOf(
            AchievementId.QUIZ_ACE,
            AchievementId.QUIZ_SILVER,
            AchievementId.QUIZ_GOLD,
            AchievementId.QUIZ_PLATINUM
        )
        for (ach in quizTiers) {
            val target = ach.threshold
            if (perfectQuizCount >= target) {
                achievementDao.unlock(ach.name)
            } else {
                achievementDao.relock(ach.name)
            }
            achievementDao.updateProgress(ach.name, perfectQuizCount.coerceAtMost(target))
        }
    }

    suspend fun completeSession(sessionId: String) {
        val session = runInTransaction {
            val s = reviewSessionDao.getSessionById(sessionId) ?: return@runInTransaction null
            // Mark any remaining PENDING items (e.g., omitted quiz items) as SKIPPED so no orphan PENDING items remain
            reviewSessionDao.updateSessionItemsStateWhere(
                sessionId = sessionId,
                currentState = SessionItemState.PENDING,
                newState = SessionItemState.SKIPPED
            )
            val answeredCount = reviewSessionDao.getItemsForSession(sessionId)
                .count { it.state == SessionItemState.ANSWERED }
            val updated = s.copy(
                status = SessionStatus.COMPLETED,
                endedAt = s.endedAt ?: System.currentTimeMillis(),
                currentPosition = answeredCount,
                totalItems = if (answeredCount > 0) answeredCount else s.totalItems
            )
            reviewSessionDao.updateSession(updated)
            updated
        } ?: return

        // Re-check all achievements on session completion
        checkAchievements(session.id)
    }

    suspend fun abandonSession(sessionId: String) {
        runInTransaction {
            val session = reviewSessionDao.getSessionById(sessionId) ?: return@runInTransaction
            if (session.status == SessionStatus.ACTIVE) {
                reviewSessionDao.updateSession(
                    session.copy(
                        status = SessionStatus.ABANDONED,
                        endedAt = session.endedAt ?: System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun getAllAchievementsFlow() = achievementDao.getAllAchievementsFlow()
}
