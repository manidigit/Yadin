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
import androidx.room.withTransaction
import com.manidigit.yadin.data.local.database.YadinDatabase
import java.util.UUID

class VocabularyRepository(
    private val database: YadinDatabase,
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

    suspend fun addCategory(name: String): Category {
        val cleanName = name.trim()
        val all = conceptDao.getAllCategories()
        val existing = all.firstOrNull { it.name.equals(cleanName, ignoreCase = true) }
        if (existing != null) {
            return Category(id = existing.id, name = existing.name, sortOrder = existing.sortOrder, isDefault = existing.isDefault)
        }
        val id = UUID.randomUUID().toString()
        val maxSort = all.maxOfOrNull { it.sortOrder } ?: 0
        val entity = CategoryEntity(id = id, name = cleanName, sortOrder = maxSort + 1, isDefault = false)
        conceptDao.insertCategory(entity)
        return Category(id = id, name = cleanName, sortOrder = maxSort + 1, isDefault = false)
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

    suspend fun searchFiltered(
        query: String = "",
        categoryId: String? = null,
        stage: Stage? = null,
        direction: CardDirection = CardDirection.NORMAL,
        limit: Int = 300
    ): List<WordDetail> {
        val entities = conceptDao.searchConceptsFiltered(
            query = query.trim(),
            categoryId = categoryId,
            stage = stage?.name,
            direction = direction,
            limit = limit
        )
        return entities.mapNotNull { getWordDetail(it.id) }
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
        direction: CardDirection = CardDirection.NORMAL,
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

        val sourceLang = if (direction == CardDirection.REVERSE) "fa" else "es"
        val targetLang = if (direction == CardDirection.REVERSE) "es" else "fa"
        val conceptId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        return database.withTransaction {
            val concept = ConceptEntity(
                id = conceptId,
                entryType = entryType,
                categoryId = categoryId,
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
                        translationIndex = idx
                    )
                )
            }

            val today = ClockAndDayMath.todayDayString()
            // Default learning and difficulty states for both directions
            val normalLearning = LearningStateEntity(
                id = UUID.randomUUID().toString(),
                conceptId = conceptId,
                direction = CardDirection.NORMAL,
                stage = Stage.DAILY,
                nextReviewDay = today,
                lastReviewedDay = null,
                createdAt = now,
                updatedAt = now
            )
            val reverseLearning = LearningStateEntity(
                id = UUID.randomUUID().toString(),
                conceptId = conceptId,
                direction = CardDirection.REVERSE,
                stage = Stage.DAILY,
                nextReviewDay = today,
                lastReviewedDay = null,
                createdAt = now,
                updatedAt = now
            )
            val normalDiff = DifficultyStateEntity(
                id = UUID.randomUUID().toString(),
                conceptId = conceptId,
                direction = CardDirection.NORMAL,
                current = VocabularyDifficulty.EASY
            )
            val reverseDiff = DifficultyStateEntity(
                id = UUID.randomUUID().toString(),
                conceptId = conceptId,
                direction = CardDirection.REVERSE,
                current = VocabularyDifficulty.EASY
            )

            conceptDao.insertConcept(concept)
            conceptDao.insertContents(contents)
            if (categoryId != null) {
                conceptDao.insertConceptCategory(ConceptCategoryEntity(conceptId, categoryId))
            }
            learningDao.insertLearningStates(listOf(normalLearning, reverseLearning))
            learningDao.insertDifficultyStates(listOf(normalDiff, reverseDiff))

            Result.success(conceptId)
        }
    }

    suspend fun updateWord(
        conceptId: String,
        sourceText: String,
        translations: List<String>,
        categoryId: String?,
        note: String?,
        direction: CardDirection = CardDirection.NORMAL
    ): Result<Unit> {
        return database.withTransaction {
            val existing = conceptDao.getConceptById(conceptId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("مفهوم یافت نشد"))
            val now = System.currentTimeMillis()
            conceptDao.updateConcept(existing.copy(categoryId = categoryId, active = true, updatedAt = now))

            val sourceLang = if (direction == CardDirection.REVERSE) "fa" else "es"
            val targetLang = if (direction == CardDirection.REVERSE) "es" else "fa"

            // Recreate contents
            conceptDao.deleteContentsForConcept(conceptId)
            val contents = mutableListOf<ContentEntity>()
            contents.add(
                ContentEntity(
                    id = UUID.randomUUID().toString(),
                    conceptId = conceptId,
                    languageCode = sourceLang,
                    text = sourceText.trim(),
                    canonicalKey = TextUtilities.toCanonicalKey(sourceText.trim()),
                    note = note?.trim()?.ifEmpty { null },
                    translationIndex = 0
                )
            )
            translations.filter { it.isNotBlank() }.forEachIndexed { idx, trans ->
                contents.add(
                    ContentEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = conceptId,
                        languageCode = targetLang,
                        text = trans.trim(),
                        canonicalKey = TextUtilities.toCanonicalKey(trans.trim()),
                        note = null,
                        translationIndex = idx
                    )
                )
            }
            conceptDao.insertContents(contents)
            Result.success(Unit)
        }
    }

    suspend fun deleteWord(conceptId: String) {
        database.withTransaction {
            conceptDao.softDeleteConcept(conceptId)
        }
    }

    fun getTotalCorrectReviewsCountFlow(): Flow<Int> {
        return reviewSessionDao.getTotalCorrectReviewsCountFlow()
    }

    suspend fun getRecentDailyStats(days: Int = 14): List<DayCountRaw> {
        return reviewSessionDao.getRecentDailyStats(days)
    }

    fun getDifficultyBreakdownFlow(direction: CardDirection = CardDirection.NORMAL): Flow<Map<VocabularyDifficulty, Int>> {
        return learningDao.getDifficultyBreakdownFlow(direction).map { list ->
            val map = list.associate { it.current to it.count }.toMutableMap()
            VocabularyDifficulty.values().forEach { diff ->
                if (!map.containsKey(diff)) map[diff] = 0
            }
            map
        }
    }

    fun getStatisticsSummary(direction: CardDirection = CardDirection.NORMAL): Flow<StatisticsSummary> {
        val today = ClockAndDayMath.todayDayString()
        data class StageCountsHolder(val daily: Int, val weekly: Int, val monthly: Int, val learned: Int)
        val stageCountsFlow: Flow<StageCountsHolder> = combine(
            learningDao.getCountByStageFlow(direction, Stage.DAILY),
            learningDao.getCountByStageFlow(direction, Stage.WEEKLY),
            learningDao.getCountByStageFlow(direction, Stage.MONTHLY),
            learningDao.getCountByStageFlow(direction, Stage.LEARNED)
        ) { daily, weekly, monthly, learned ->
            StageCountsHolder(daily, weekly, monthly, learned)
        }

        data class StageDuesHolder(val dueDaily: Int, val dueWeekly: Int, val dueMonthly: Int, val availLearned: Int)
        val stageDueFlow: Flow<StageDuesHolder> = combine(
            learningDao.getDueCountByStageFlow(direction, Stage.DAILY, today),
            learningDao.getDueCountByStageFlow(direction, Stage.WEEKLY, today),
            learningDao.getDueCountByStageFlow(direction, Stage.MONTHLY, today),
            learningDao.getLearnedAvailableCountFlow(direction, today)
        ) { dueDaily, dueWeekly, dueMonthly, availLearned ->
            StageDuesHolder(dueDaily, dueWeekly, dueMonthly, availLearned)
        }

        data class ReviewsHolder(val dueToday: Int, val reviewedToday: Int, val totalReviews: Int)
        val reviewsFlow: Flow<ReviewsHolder> = combine(
            learningDao.getDueCountFlow(direction, today),
            reviewSessionDao.getReviewCountForDayFlow(today),
            reviewSessionDao.getTotalReviewsCountFlow()
        ) { dueToday, reviewedToday, totalReviews ->
            ReviewsHolder(dueToday, reviewedToday, totalReviews)
        }

        return combine(
            stageCountsFlow,
            stageDueFlow,
            reviewsFlow,
            learningDao.getDifficultyBreakdownFlow(direction)
        ) { stageCounts, stageDues, reviews, diffList ->
            val daily = stageCounts.daily
            val weekly = stageCounts.weekly
            val monthly = stageCounts.monthly
            val learned = stageCounts.learned

            val dueDaily = stageDues.dueDaily
            val dueWeekly = stageDues.dueWeekly
            val dueMonthly = stageDues.dueMonthly
            val availLearned = stageDues.availLearned

            val totalWords = daily + weekly + monthly + learned

            val diffBreakdown = diffList.associate { it.current to it.count }

            StatisticsSummary(
                totalWords = totalWords,
                activeWords = totalWords,
                dailyStageCount = daily,
                weeklyStageCount = weekly,
                monthlyStageCount = monthly,
                learnedStageCount = learned,
                dueTodayCount = reviews.dueToday,
                reviewedTodayCount = reviews.reviewedToday,
                currentStreakDays = calculateStreakDays(reviewSessionDao.getDistinctReviewedDays(), today),
                totalReviewsCount = reviews.totalReviews,
                easyCount = diffBreakdown[VocabularyDifficulty.EASY] ?: 0,
                mediumCount = diffBreakdown[VocabularyDifficulty.MEDIUM] ?: 0,
                hardCount = diffBreakdown[VocabularyDifficulty.HARD] ?: 0,
                veryHardCount = diffBreakdown[VocabularyDifficulty.VERY_HARD] ?: 0,
                dueDailyCount = dueDaily,
                dueWeeklyCount = dueWeekly,
                dueMonthlyCount = dueMonthly,
                availableLearnedCount = availLearned
            )
        }
    }

    private fun calculateStreakDays(days: List<String>, today: String): Int {
        return ClockAndDayMath.calculateStreakDays(days, today)
    }

    private fun formatEnrichedNote(entry: ParsedEntry): String? {
        val parts = mutableListOf<String>()
        entry.note?.takeIf { it.isNotBlank() }?.let { parts.add(it.trim()) }
        entry.grammarNotes?.takeIf { it.isNotBlank() }?.let { parts.add("نکات گرامری: ${it.trim()}") }
        if (entry.variants.isNotEmpty()) {
            val vText = entry.variants.joinToString("، ") { 
                if (it.translationText.isNullOrBlank()) it.sourceText else "${it.sourceText} (${it.translationText})" 
            }
            parts.add("اشکال و گونه‌ها: $vText")
        }
        if (entry.breakdowns.isNotEmpty()) {
            val bText = entry.breakdowns.joinToString(" + ") { "${it.sourcePart}: ${it.translationPart}" }
            parts.add("تحلیل اجزاء: $bText")
        }
        if (entry.relations.isNotEmpty()) {
            val rText = entry.relations.joinToString("، ") { "${it.relationType.name}: ${it.relatedSourceText}" }
            parts.add("روابط واژگانی: $rText")
        }
        entry.possibleCorrection?.takeIf { it.isNotBlank() }?.let { parts.add("اصلاح پیشنهادی: ${it.trim()}") }
        return parts.joinToString("\n").ifBlank { null }
    }

    suspend fun importParsedEntries(
        entries: List<ParsedEntry>,
        policy: DuplicatePolicy,
        targetCategoryId: String? = null,
        onProgress: (Int, Int) -> Unit
    ): com.manidigit.yadin.domain.model.ImportSummary {
        val total = entries.size
        var addedCount = 0
        var updatedCount = 0
        var skippedCount = 0

        database.withTransaction {
            entries.forEachIndexed { index, entry ->
                val cleanSource = entry.sourceText.trim()
                val canonical = TextUtilities.toCanonicalKey(cleanSource)
                val existingContent = conceptDao.findContentByCanonicalKey("es", canonical)
                val enrichedNote = formatEnrichedNote(entry)

                if (existingContent != null) {
                    val existingConcept = conceptDao.getConceptById(existingContent.conceptId)
                    val effectiveCat = targetCategoryId ?: existingConcept?.categoryId
                    when (policy) {
                        DuplicatePolicy.SKIP -> {
                            skippedCount++
                        }
                        DuplicatePolicy.REPLACE -> {
                            val res = updateWord(
                                conceptId = existingContent.conceptId,
                                sourceText = cleanSource,
                                translations = entry.translations,
                                categoryId = effectiveCat,
                                note = enrichedNote
                            )
                            if (res.isSuccess) updatedCount++ else skippedCount++
                        }
                        DuplicatePolicy.MERGE -> {
                            val currentContents = conceptDao.getContentsForConcept(existingContent.conceptId)
                            val existingTranslations = currentContents.filter { it.languageCode == "fa" }.map { it.text }
                            val merged = (existingTranslations + entry.translations).distinct()
                            val currentEs = currentContents.firstOrNull { it.languageCode == "es" }
                            val mergedNote = listOfNotNull(currentEs?.note, enrichedNote).distinct().joinToString("\n").ifBlank { null }
                            val res = updateWord(
                                conceptId = existingContent.conceptId,
                                sourceText = cleanSource,
                                translations = merged,
                                categoryId = effectiveCat,
                                note = mergedNote
                            )
                            if (res.isSuccess) updatedCount++ else skippedCount++
                        }
                        DuplicatePolicy.KEEP_SEPARATE -> {
                            val res = addWord(
                                sourceText = cleanSource,
                                translations = entry.translations,
                                categoryId = targetCategoryId,
                                note = enrichedNote
                            )
                            if (res.isSuccess) addedCount++ else skippedCount++
                        }
                    }
                } else {
                    val res = addWord(
                        sourceText = cleanSource,
                        translations = entry.translations,
                        categoryId = targetCategoryId,
                        note = enrichedNote
                    )
                    if (res.isSuccess) addedCount++ else skippedCount++
                }
                onProgress(index + 1, total)
            }
        }
        val catName = targetCategoryId?.let { conceptDao.getCategoryById(it)?.name }
        return com.manidigit.yadin.domain.model.ImportSummary(
            totalProcessed = total,
            addedCount = addedCount,
            updatedCount = updatedCount,
            skippedCount = skippedCount,
            categoryName = catName
        )
    }

    fun getProgressScoreFlow(direction: CardDirection): Flow<Double> {
        return learningDao.getProgressScoreFlow(direction).map { raw ->
            if (raw.totalActive <= 0 || raw.totalScore == null) {
                0.0
            } else {
                val score = raw.totalScore / raw.totalActive.toDouble()
                Math.round(score * 10.0) / 10.0
            }
        }
    }

    fun getRecentDailyStatsFlow(): Flow<List<DayCountRaw>> {
        return reviewSessionDao.getRecentDailyStatsFlow(90)
    }

    fun getPracticedWordsCountFlow(direction: CardDirection): Flow<Int> {
        return learningDao.getPracticedWordsCountFlow(direction)
    }
}
