package com.manidigit.yadin.data.repository

import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.Concept
import com.manidigit.yadin.domain.model.Content
import com.manidigit.yadin.domain.model.DifficultyState
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.LearningState
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.domain.time.ClockAndDayMath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class VocabularyRepository(
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val reviewSessionDao: ReviewSessionDao
) {

    fun getDueCountFlow(direction: CardDirection = CardDirection.NORMAL): Flow<Int> {
        val today = ClockAndDayMath.todayDayString()
        return learningDao.getDueCountFlow(direction, today)
    }

    fun getAllCategoriesFlow(): Flow<List<Category>> {
        return conceptDao.getAllCategoriesFlow().map { list ->
            list.map { Category(id = it.id, name = it.name, sortOrder = it.sortOrder, isDefault = it.isDefault) }
        }
    }

    suspend fun getAllCategories(): List<Category> {
        return conceptDao.getAllCategories().map {
            Category(id = it.id, name = it.name, sortOrder = it.sortOrder, isDefault = it.isDefault)
        }
    }

    fun getAllActiveConceptsFlow(): Flow<List<ConceptEntity>> {
        return conceptDao.getAllActiveConceptsFlow()
    }

    suspend fun getWordDetail(conceptId: String): WordDetail? {
        val conceptEntity = conceptDao.getConceptById(conceptId) ?: return null
        val contents = conceptDao.getContentsForConcept(conceptId)
        val sourceContentEntity = contents.firstOrNull { it.languageCode != "fa" } ?: contents.firstOrNull() ?: return null
        val targetContentEntities = contents.filter { it.languageCode == "fa" || it.id != sourceContentEntity.id }

        val normalLearning = learningDao.getLearningState(conceptId, CardDirection.NORMAL)?.let {
            LearningState(
                id = it.id,
                conceptId = it.conceptId,
                direction = it.direction,
                stage = it.stage,
                nextReviewDay = it.nextReviewDay,
                lastReviewedDay = it.lastReviewedDay,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
        val reverseLearning = learningDao.getLearningState(conceptId, CardDirection.REVERSE)?.let {
            LearningState(
                id = it.id,
                conceptId = it.conceptId,
                direction = it.direction,
                stage = it.stage,
                nextReviewDay = it.nextReviewDay,
                lastReviewedDay = it.lastReviewedDay,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
        val normalDiff = learningDao.getDifficultyState(conceptId, CardDirection.NORMAL)?.let {
            DifficultyState(
                id = it.id,
                conceptId = it.conceptId,
                direction = it.direction,
                current = it.current,
                consecutiveCorrect = it.consecutiveCorrect,
                consecutiveWrong = it.consecutiveWrong,
                hasReachedVeryHard = it.hasReachedVeryHard
            )
        }
        val reverseDiff = learningDao.getDifficultyState(conceptId, CardDirection.REVERSE)?.let {
            DifficultyState(
                id = it.id,
                conceptId = it.conceptId,
                direction = it.direction,
                current = it.current,
                consecutiveCorrect = it.consecutiveCorrect,
                consecutiveWrong = it.consecutiveWrong,
                hasReachedVeryHard = it.hasReachedVeryHard
            )
        }

        val category = conceptEntity.categoryId?.let { conceptDao.getCategoryById(it) }?.let {
            Category(id = it.id, name = it.name, sortOrder = it.sortOrder, isDefault = it.isDefault)
        }

        val domainConcept = Concept(
            id = conceptEntity.id,
            entryType = conceptEntity.entryType,
            categoryId = conceptEntity.categoryId,
            favorite = conceptEntity.favorite,
            active = conceptEntity.active,
            createdAt = conceptEntity.createdAt,
            updatedAt = conceptEntity.updatedAt
        )

        return WordDetail(
            concept = domainConcept,
            sourceContent = Content(
                id = sourceContentEntity.id,
                conceptId = sourceContentEntity.conceptId,
                languageCode = sourceContentEntity.languageCode,
                text = sourceContentEntity.text,
                canonicalKey = sourceContentEntity.canonicalKey,
                note = sourceContentEntity.note,
                pronunciation = sourceContentEntity.pronunciation,
                translationIndex = sourceContentEntity.translationIndex
            ),
            targetContents = targetContentEntities.map {
                Content(
                    id = it.id,
                    conceptId = it.conceptId,
                    languageCode = it.languageCode,
                    text = it.text,
                    canonicalKey = it.canonicalKey,
                    note = it.note,
                    pronunciation = it.pronunciation,
                    translationIndex = it.translationIndex
                )
            },
            categories = listOfNotNull(category),
            tags = emptyList(),
            normalLearning = normalLearning,
            reverseLearning = reverseLearning,
            normalDifficulty = normalDiff,
            reverseDifficulty = reverseDiff
        )
    }

    suspend fun search(query: String, limit: Int = 100): List<WordDetail> {
        val normalized = query.trim()
        val entities = if (normalized.isEmpty()) {
            emptyList()
        } else {
            conceptDao.searchConcepts(normalized, limit)
        }
        return entities.mapNotNull { getWordDetail(it.id) }
    }

    suspend fun getRecentWords(limit: Int = 50): List<WordDetail> {
        val entities = conceptDao.searchConcepts("", limit)
        return entities.mapNotNull { getWordDetail(it.id) }
    }

    suspend fun addWord(
        sourceText: String,
        translations: List<String>,
        categoryId: String? = null,
        note: String? = null,
        pronunciation: String? = null,
        sourceLang: String = "es",
        targetLang: String = "fa",
        entryType: EntryType = EntryType.WORD
    ): Result<String> {
        val cleanSource = sourceText.trim()
        if (cleanSource.isEmpty()) {
            return Result.failure(IllegalArgumentException("واژه ورودی نمی‌تواند خالی باشد"))
        }
        val cleanTranslations = translations.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleanTranslations.isEmpty()) {
            return Result.failure(IllegalArgumentException("حداقل یک ترجمه باید وارد شود"))
        }

        val conceptId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val concept = ConceptEntity(
            id = conceptId,
            entryType = entryType,
            categoryId = categoryId,
            favorite = false,
            active = true,
            createdAt = now,
            updatedAt = now
        )

        val contents = mutableListOf<ContentEntity>()
        contents.add(
            ContentEntity(
                id = UUID.randomUUID().toString(),
                conceptId = conceptId,
                languageCode = sourceLang,
                text = cleanSource,
                canonicalKey = TextUtilities.toCanonicalKey(cleanSource),
                note = note?.trim()?.ifEmpty { null },
                pronunciation = pronunciation?.trim()?.ifEmpty { null },
                translationIndex = 0
            )
        )

        cleanTranslations.forEachIndexed { idx, trans ->
            contents.add(
                ContentEntity(
                    id = UUID.randomUUID().toString(),
                    conceptId = conceptId,
                    languageCode = targetLang,
                    text = trans,
                    canonicalKey = TextUtilities.toCanonicalKey(trans),
                    note = null,
                    pronunciation = null,
                    translationIndex = idx
                )
            )
        }

        // Default learning and difficulty states for both directions
        val normalLearning = LearningStateEntity(
            id = UUID.randomUUID().toString(),
            conceptId = conceptId,
            direction = CardDirection.NORMAL,
            stage = Stage.DAILY,
            nextReviewDay = null,
            lastReviewedDay = null,
            createdAt = now,
            updatedAt = now
        )
        val reverseLearning = LearningStateEntity(
            id = UUID.randomUUID().toString(),
            conceptId = conceptId,
            direction = CardDirection.REVERSE,
            stage = Stage.DAILY,
            nextReviewDay = null,
            lastReviewedDay = null,
            createdAt = now,
            updatedAt = now
        )
        val normalDiff = DifficultyStateEntity(
            id = UUID.randomUUID().toString(),
            conceptId = conceptId,
            direction = CardDirection.NORMAL,
            current = VocabularyDifficulty.MEDIUM
        )
        val reverseDiff = DifficultyStateEntity(
            id = UUID.randomUUID().toString(),
            conceptId = conceptId,
            direction = CardDirection.REVERSE,
            current = VocabularyDifficulty.MEDIUM
        )

        conceptDao.insertConcept(concept)
        conceptDao.insertContents(contents)
        if (categoryId != null) {
            conceptDao.insertConceptCategory(ConceptCategoryEntity(conceptId, categoryId))
        }
        learningDao.insertLearningStates(listOf(normalLearning, reverseLearning))
        learningDao.insertDifficultyStates(listOf(normalDiff, reverseDiff))

        return Result.success(conceptId)
    }

    suspend fun updateWord(
        conceptId: String,
        sourceText: String,
        translations: List<String>,
        categoryId: String?,
        note: String?,
        pronunciation: String?
    ): Result<Unit> {
        val existing = conceptDao.getConceptById(conceptId)
            ?: return Result.failure(IllegalArgumentException("مفهوم یافت نشد"))
        val now = System.currentTimeMillis()
        conceptDao.updateConcept(existing.copy(categoryId = categoryId, updatedAt = now))

        // Recreate contents
        conceptDao.deleteContentsForConcept(conceptId)
        val contents = mutableListOf<ContentEntity>()
        contents.add(
            ContentEntity(
                id = UUID.randomUUID().toString(),
                conceptId = conceptId,
                languageCode = "es",
                text = sourceText.trim(),
                canonicalKey = TextUtilities.toCanonicalKey(sourceText.trim()),
                note = note?.trim()?.ifEmpty { null },
                pronunciation = pronunciation?.trim()?.ifEmpty { null },
                translationIndex = 0
            )
        )
        translations.filter { it.isNotBlank() }.forEachIndexed { idx, trans ->
            contents.add(
                ContentEntity(
                    id = UUID.randomUUID().toString(),
                    conceptId = conceptId,
                    languageCode = "fa",
                    text = trans.trim(),
                    canonicalKey = TextUtilities.toCanonicalKey(trans.trim()),
                    note = null,
                    pronunciation = null,
                    translationIndex = idx
                )
            )
        }
        conceptDao.insertContents(contents)
        return Result.success(Unit)
    }

    suspend fun setFavorite(conceptId: String, favorite: Boolean) {
        conceptDao.setFavorite(conceptId, favorite)
    }

    suspend fun deleteWord(conceptId: String) {
        conceptDao.softDeleteConcept(conceptId)
    }

    suspend fun getRecentDailyStats(days: Int = 14): List<DayCountRaw> {
        return reviewSessionDao.getRecentDailyStats(days)
    }

    fun getStatisticsSummary(direction: CardDirection = CardDirection.NORMAL): Flow<StatisticsSummary> {
        val today = ClockAndDayMath.todayDayString()
        return combine(
            learningDao.getCountByStageFlow(direction, Stage.DAILY),
            learningDao.getCountByStageFlow(direction, Stage.WEEKLY),
            learningDao.getCountByStageFlow(direction, Stage.MONTHLY),
            learningDao.getCountByStageFlow(direction, Stage.LEARNED),
            learningDao.getDueCountFlow(direction, today),
            reviewSessionDao.getReviewCountForDayFlow(today),
            reviewSessionDao.getTotalReviewsCountFlow()
        ) { args: Array<Int> ->
            val daily = args[0]
            val weekly = args[1]
            val monthly = args[2]
            val learned = args[3]
            val dueToday = args[4]
            val reviewedToday = args[5]
            val totalReviews = args[6]
            val totalWords = daily + weekly + monthly + learned
            StatisticsSummary(
                totalWords = totalWords,
                activeWords = totalWords,
                dailyStageCount = daily,
                weeklyStageCount = weekly,
                monthlyStageCount = monthly,
                learnedStageCount = learned,
                dueTodayCount = dueToday,
                reviewedTodayCount = reviewedToday,
                currentStreakDays = calculateStreakDays(reviewSessionDao.getDistinctReviewedDays(), today),
                totalReviewsCount = totalReviews,
                easyCount = 0,
                mediumCount = 0,
                hardCount = 0,
                veryHardCount = 0
            )
        }
    }

    private fun calculateStreakDays(days: List<String>, today: String): Int {
        if (days.isEmpty()) return 0
        val sortedDays = days.distinct().sortedDescending()
        var streak = 0
        var expectedDay = if (sortedDays.first() == today) today else ClockAndDayMath.addDays(today, -1)
        if (sortedDays.first() != today && sortedDays.first() != expectedDay) {
            return 0
        }
        for (day in sortedDays) {
            if (day == expectedDay) {
                streak++
                expectedDay = ClockAndDayMath.addDays(expectedDay, -1)
            } else if (day > expectedDay) {
                continue
            } else {
                break
            }
        }
        return streak
    }

    suspend fun importParsedEntries(
        entries: List<ParsedEntry>,
        policy: DuplicatePolicy,
        onProgress: (Int, Int) -> Unit
    ) {
        val total = entries.size
        entries.forEachIndexed { index, entry ->
            val cleanSource = entry.sourceText.trim()
            val canonical = TextUtilities.toCanonicalKey(cleanSource)
            val existingContent = conceptDao.findContentByCanonicalKey("es", canonical)

            if (existingContent != null) {
                when (policy) {
                    DuplicatePolicy.SKIP -> { /* Skip */ }
                    DuplicatePolicy.REPLACE -> {
                        updateWord(
                            conceptId = existingContent.conceptId,
                            sourceText = cleanSource,
                            translations = entry.translations,
                            categoryId = null,
                            note = entry.note,
                            pronunciation = null
                        )
                    }
                    DuplicatePolicy.MERGE -> {
                        val currentContents = conceptDao.getContentsForConcept(existingContent.conceptId)
                        val existingTranslations = currentContents.filter { it.languageCode == "fa" }.map { it.text }
                        val merged = (existingTranslations + entry.translations).distinct()
                        updateWord(
                            conceptId = existingContent.conceptId,
                            sourceText = cleanSource,
                            translations = merged,
                            categoryId = null,
                            note = entry.note ?: currentContents.firstOrNull { it.languageCode == "es" }?.note,
                            pronunciation = currentContents.firstOrNull { it.languageCode == "es" }?.pronunciation
                        )
                    }
                    DuplicatePolicy.KEEP_SEPARATE -> {
                        addWord(
                            sourceText = cleanSource,
                            translations = entry.translations,
                            categoryId = null,
                            note = entry.note,
                            pronunciation = null
                        )
                    }
                }
            } else {
                addWord(
                    sourceText = cleanSource,
                    translations = entry.translations,
                    categoryId = null,
                    note = entry.note,
                    pronunciation = null
                )
            }
            onProgress(index + 1, total)
        }
    }
}
