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
import com.manidigit.yadin.domain.model.LanguageConstants
import com.manidigit.yadin.domain.model.LearningState
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.model.WordDetail
import com.manidigit.yadin.domain.model.toPersianLabel
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.domain.time.ClockAndDayMath
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
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

    suspend fun updateCategory(id: String, name: String) {
        val cleanName = name.trim()
        if (cleanName.isNotBlank()) {
            conceptDao.updateCategoryName(id, cleanName)
        }
    }

    suspend fun deleteCategory(id: String) {
        conceptDao.deleteCategory(id)
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
                updatedAt = it.updatedAt,
                sm2Repetitions = it.sm2Repetitions,
                sm2EasinessFactor = it.sm2EasinessFactor,
                sm2IntervalDays = it.sm2IntervalDays,
                fsrsStability = it.fsrsStability,
                fsrsDifficulty = it.fsrsDifficulty,
                fsrsReps = it.fsrsReps,
                fsrsLapses = it.fsrsLapses,
                fsrsState = it.fsrsState
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
                updatedAt = it.updatedAt,
                sm2Repetitions = it.sm2Repetitions,
                sm2EasinessFactor = it.sm2EasinessFactor,
                sm2IntervalDays = it.sm2IntervalDays,
                fsrsStability = it.fsrsStability,
                fsrsDifficulty = it.fsrsDifficulty,
                fsrsReps = it.fsrsReps,
                fsrsLapses = it.fsrsLapses,
                fsrsState = it.fsrsState
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

        val conceptCategories = conceptDao.getCategoriesForConcept(conceptId)
        val categoriesList = if (conceptCategories.isNotEmpty()) {
            conceptCategories.map { Category(id = it.id, name = it.name, sortOrder = it.sortOrder, isDefault = it.isDefault) }
        } else {
            conceptEntity.categoryId?.let { conceptDao.getCategoryById(it) }?.let {
                listOf(Category(id = it.id, name = it.name, sortOrder = it.sortOrder, isDefault = it.isDefault))
            } ?: emptyList()
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
            categories = categoriesList,
            normalLearning = normalLearning,
            reverseLearning = reverseLearning,
            normalDifficulty = normalDiff,
            reverseDifficulty = reverseDiff
        )
    }

    suspend fun searchFilteredPaged(
        query: String = "",
        categoryId: String? = null,
        stage: Stage? = null,
        direction: CardDirection = CardDirection.NORMAL,
        difficulty: com.manidigit.yadin.domain.model.VocabularyDifficulty? = null,
        onlyInactive: Boolean = false,
        limit: Int = 50,
        offset: Int = 0
    ): Pair<List<WordDetail>, Boolean> {
        val trimmed = query.trim()
        val searchQuery = if (trimmed.isEmpty()) "" else TextUtilities.searchKey(trimmed)
        val entities = conceptDao.searchConceptsFiltered(
            query = trimmed,
            searchKey = searchQuery,
            categoryId = categoryId,
            stage = stage?.name,
            direction = direction,
            difficulty = difficulty?.name,
            onlyInactive = onlyInactive,
            limit = limit,
            offset = offset
        )
        if (entities.isEmpty()) return Pair(emptyList(), false)
        val details = batchLoadWordDetails(entities)
        val hasMore = entities.size >= limit
        return Pair(details, hasMore)
    }

    private suspend fun batchLoadWordDetails(entities: List<ConceptEntity>): List<WordDetail> {
        if (entities.isEmpty()) return emptyList()
        val conceptIds = entities.map { it.id }

        // بهینه‌سازی ISS-32: واکشی دسته‌ای (Batch Fetch) جهت حذف کامل مشکل N+1 کوئری
        val contentsMap = conceptDao.getContentsForConcepts(conceptIds).groupBy { it.conceptId }
        val learningStatesMap = learningDao.getLearningStatesForConcepts(conceptIds).groupBy { it.conceptId }
        val difficultyStatesMap = learningDao.getAllDifficultyStatesForConcepts(conceptIds).groupBy { it.conceptId }
        val categoriesJoinMap = conceptDao.getCategoriesForConcepts(conceptIds).groupBy { it.conceptId }

        return entities.mapNotNull { conceptEntity ->
            val contents = contentsMap[conceptEntity.id] ?: emptyList()
            val sourceContentEntity = contents.firstOrNull { it.languageCode != "fa" } ?: contents.firstOrNull() ?: return@mapNotNull null
            val targetContentEntities = contents.filter { it.languageCode == "fa" || it.id != sourceContentEntity.id }

            val lStates = learningStatesMap[conceptEntity.id] ?: emptyList()
            val normalLearning = lStates.firstOrNull { it.direction == CardDirection.NORMAL }?.let {
                LearningState(
                    id = it.id,
                    conceptId = it.conceptId,
                    direction = it.direction,
                    stage = it.stage,
                    nextReviewDay = it.nextReviewDay,
                    lastReviewedDay = it.lastReviewedDay,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    sm2Repetitions = it.sm2Repetitions,
                    sm2EasinessFactor = it.sm2EasinessFactor,
                    sm2IntervalDays = it.sm2IntervalDays,
                    fsrsStability = it.fsrsStability,
                    fsrsDifficulty = it.fsrsDifficulty,
                    fsrsReps = it.fsrsReps,
                    fsrsLapses = it.fsrsLapses,
                    fsrsState = it.fsrsState
                )
            }
            val reverseLearning = lStates.firstOrNull { it.direction == CardDirection.REVERSE }?.let {
                LearningState(
                    id = it.id,
                    conceptId = it.conceptId,
                    direction = it.direction,
                    stage = it.stage,
                    nextReviewDay = it.nextReviewDay,
                    lastReviewedDay = it.lastReviewedDay,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    sm2Repetitions = it.sm2Repetitions,
                    sm2EasinessFactor = it.sm2EasinessFactor,
                    sm2IntervalDays = it.sm2IntervalDays,
                    fsrsStability = it.fsrsStability,
                    fsrsDifficulty = it.fsrsDifficulty,
                    fsrsReps = it.fsrsReps,
                    fsrsLapses = it.fsrsLapses,
                    fsrsState = it.fsrsState
                )
            }

            val dStates = difficultyStatesMap[conceptEntity.id] ?: emptyList()
            val normalDiff = dStates.firstOrNull { it.direction == CardDirection.NORMAL }?.let {
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
            val reverseDiff = dStates.firstOrNull { it.direction == CardDirection.REVERSE }?.let {
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

            val categoriesList = categoriesJoinMap[conceptEntity.id]?.map {
                Category(id = it.id, name = it.name, sortOrder = it.sortOrder, isDefault = it.isDefault)
            } ?: emptyList()

            val domainConcept = Concept(
                id = conceptEntity.id,
                entryType = conceptEntity.entryType,
                categoryId = conceptEntity.categoryId,
                active = conceptEntity.active,
                createdAt = conceptEntity.createdAt,
                updatedAt = conceptEntity.updatedAt
            )

            WordDetail(
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
                categories = categoriesList,
                normalLearning = normalLearning,
                reverseLearning = reverseLearning,
                normalDifficulty = normalDiff,
                reverseDifficulty = reverseDiff
            )
        }
    }

    suspend fun searchFiltered(
        query: String = "",
        categoryId: String? = null,
        stage: Stage? = null,
        direction: CardDirection = CardDirection.NORMAL,
        difficulty: com.manidigit.yadin.domain.model.VocabularyDifficulty? = null,
        onlyInactive: Boolean = false,
        limit: Int = 50,
        offset: Int = 0
    ): List<WordDetail> {
        return searchFilteredPaged(
            query = query,
            categoryId = categoryId,
            stage = stage,
            direction = direction,
            difficulty = difficulty,
            onlyInactive = onlyInactive,
            limit = limit,
            offset = offset
        ).first
    }

    suspend fun reactivateWord(conceptId: String): Result<Unit> {
        return try {
            conceptDao.reactivateConcept(conceptId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun purgeInactiveWords(): Int = database.withTransaction {
        val inactives = conceptDao.getInactiveConcepts()
        if (inactives.isEmpty()) return@withTransaction 0
        val ids = inactives.map { it.id }
        conceptDao.deleteContentsForConcepts(ids)
        conceptDao.deleteConceptCategoriesForConcepts(ids)
        learningDao.deleteLearningStatesForConcepts(ids)
        learningDao.deleteDifficultyStatesForConcepts(ids)
        reviewSessionDao.deleteSessionItemsForConcepts(ids)
        reviewSessionDao.deleteHistoryForConcepts(ids)
        reviewSessionDao.deleteEmptySessions()
        conceptDao.deleteConceptsPermanently(ids)
        inactives.size
    }

    suspend fun search(query: String, limit: Int = 100): List<WordDetail> {
        val trimmed = query.trim()
        val entities = if (trimmed.isEmpty()) {
            emptyList()
        } else {
            val searchKey = TextUtilities.searchKey(trimmed)
            conceptDao.searchConcepts(query = trimmed, searchKey = searchKey, limit = limit)
        }
        return batchLoadWordDetails(entities)
    }

    suspend fun getRecentWords(limit: Int = 50): List<WordDetail> {
        val entities = conceptDao.searchConcepts(query = "", searchKey = "", limit = limit)
        return batchLoadWordDetails(entities)
    }

    suspend fun addWord(
        sourceText: String,
        translations: List<String>,
        categoryId: String? = null,
        note: String? = null,
        direction: CardDirection = CardDirection.NORMAL,
        entryType: EntryType = EntryType.WORD,
        categoryIds: List<String> = emptyList()
    ): Result<String> {
        val cleanSource = sourceText.trim()
        if (cleanSource.isEmpty()) {
            return Result.failure(IllegalArgumentException("واژه ورودی نمی‌تواند خالی باشد"))
        }
        val cleanTranslations = translations.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleanTranslations.isEmpty()) {
            return Result.failure(IllegalArgumentException("حداقل یک ترجمه باید وارد شود"))
        }

        // حفظ نقش اصیل زبان‌ها در پایگاه داده (اسپانیایی: مبدأ، فارسی: مقصد)
        val canonicalSourceLang = LanguageConstants.DEFAULT_SOURCE_LANGUAGE
        val canonicalTargetLang = LanguageConstants.DEFAULT_TARGET_LANGUAGE
        val (rawSourceText, rawFinalTranslations) = if (direction == CardDirection.REVERSE) {
            val spanishWord = cleanTranslations.firstOrNull { it.isNotBlank() } ?: cleanSource
            val sourcePersianList = cleanSource.split("،", ",", "\n").map { it.trim() }.filter { it.isNotEmpty() }
            val extraPersianList = cleanTranslations.drop(1).filter { it.isNotBlank() }
            val persianTranslations = TextUtilities.deduplicatePersianTranslations(sourcePersianList + extraPersianList)
            Pair(spanishWord, persianTranslations.ifEmpty { (sourcePersianList + extraPersianList).distinct() })
        } else {
            val deduped = TextUtilities.deduplicatePersianTranslations(cleanTranslations)
            Pair(cleanSource, deduped.ifEmpty { cleanTranslations.distinct() })
        }
        val finalSourceText = TextUtilities.normalizeSpanishPunctuation(rawSourceText).first
        val finalTranslations = rawFinalTranslations.map {
            TextUtilities.normalizePersianTranslation(it, finalSourceText).first
        }.distinct()

        val sourceCanonicalKey = TextUtilities.toCanonicalKey(finalSourceText)
        val now = System.currentTimeMillis()
        val effectivePrimaryCategory = categoryIds.firstOrNull() ?: categoryId

        return database.withTransaction {
            // بررسی عدم وجود واژه فعال تکراری با همین کلید قانونی (Issue 1.1)
            val existingActive = conceptDao.findActiveContentByCanonicalKey(canonicalSourceLang, sourceCanonicalKey)
            if (existingActive != null) {
                return@withTransaction Result.failure(
                    IllegalStateException("این واژه («$finalSourceText») قبلاً در واژه‌نامه ثبت شده است.")
                )
            }

            // در صورت وجود واژه غیرفعال، فعال‌سازی مجدد و به‌روزرسانی آن
            val existingAny = conceptDao.findContentByCanonicalKey(canonicalSourceLang, sourceCanonicalKey)
            if (existingAny != null) {
                val existingConcept = conceptDao.getConceptById(existingAny.conceptId)
                if (existingConcept != null && !existingConcept.active) {
                    val updateRes = updateWord(
                        conceptId = existingConcept.id,
                        sourceText = finalSourceText,
                        translations = finalTranslations,
                        categoryId = effectivePrimaryCategory,
                        note = note,
                        direction = CardDirection.NORMAL,
                        entryType = entryType,
                        categoryIds = categoryIds
                    )
                    return@withTransaction if (updateRes.isSuccess) {
                        Result.success(existingConcept.id)
                    } else {
                        Result.failure(updateRes.exceptionOrNull() ?: Exception("خطا در فعال‌سازی مجدد واژه"))
                    }
                }
            }

            val conceptId = UUID.randomUUID().toString()
            val concept = ConceptEntity(
                id = conceptId,
                entryType = entryType,
                categoryId = effectivePrimaryCategory,
                active = true,
                createdAt = now,
                updatedAt = now
            )

            val contents = mutableListOf<ContentEntity>()
            contents.add(
                ContentEntity(
                    id = UUID.randomUUID().toString(),
                    conceptId = conceptId,
                    languageCode = canonicalSourceLang,
                    text = finalSourceText,
                    canonicalKey = TextUtilities.toCanonicalKey(finalSourceText),
                    note = note?.trim()?.ifEmpty { null },
                    translationIndex = 0
                )
            )

            finalTranslations.forEachIndexed { idx, trans ->
                contents.add(
                    ContentEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = conceptId,
                        languageCode = canonicalTargetLang,
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

            val allCatsToInsert = (categoryIds + listOfNotNull(categoryId)).distinct()
            allCatsToInsert.forEach { catId ->
                conceptDao.insertConceptCategory(ConceptCategoryEntity(conceptId, catId))
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
        direction: CardDirection = CardDirection.NORMAL,
        entryType: EntryType? = null,
        categoryIds: List<String> = emptyList()
    ): Result<Unit> {
        return database.withTransaction {
            val existing = conceptDao.getConceptById(conceptId)
                ?: return@withTransaction Result.failure(IllegalArgumentException("مفهوم یافت نشد"))
            val now = System.currentTimeMillis()
            val effectivePrimaryCategory = categoryIds.firstOrNull() ?: categoryId
            conceptDao.updateConcept(
                existing.copy(
                    categoryId = effectivePrimaryCategory,
                    entryType = entryType ?: existing.entryType,
                    active = true,
                    updatedAt = now
                )
            )

            // ISS-21 & ISS-10: Synchronize concept_categories join table
            conceptDao.deleteConceptCategories(conceptId)
            val allCatsToInsert = (categoryIds + listOfNotNull(categoryId)).distinct()
            allCatsToInsert.forEach { catId ->
                conceptDao.insertConceptCategory(ConceptCategoryEntity(conceptId, catId))
            }

            val canonicalSourceLang = LanguageConstants.DEFAULT_SOURCE_LANGUAGE
            val canonicalTargetLang = LanguageConstants.DEFAULT_TARGET_LANGUAGE

            val cleanSource = sourceText.trim()
            val cleanTranslations = translations.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

            // اصلاح جابه‌جایی ناصحیح زبان مبدأ و مقصد در ویرایش از جهت برعکس (REVERSE)
            val (rawSourceText, rawFinalTranslations) = if (direction == CardDirection.REVERSE) {
                val spanishWord = cleanTranslations.firstOrNull { it.isNotBlank() } ?: cleanSource
                val sourcePersianList = cleanSource.split("،", ",", "\n").map { it.trim() }.filter { it.isNotEmpty() }
                val extraPersianList = cleanTranslations.drop(1).filter { it.isNotBlank() }
                val persianTranslations = TextUtilities.deduplicatePersianTranslations(sourcePersianList + extraPersianList)
                Pair(spanishWord, persianTranslations.ifEmpty { (sourcePersianList + extraPersianList).distinct() })
            } else {
                val deduped = TextUtilities.deduplicatePersianTranslations(cleanTranslations)
                Pair(cleanSource, deduped.ifEmpty { cleanTranslations })
            }
            val finalSourceText = TextUtilities.normalizeSpanishPunctuation(rawSourceText).first
            val finalTranslations = rawFinalTranslations.map {
                TextUtilities.normalizePersianTranslation(it, finalSourceText).first
            }.distinct()

            // بررسی عدم تداخل کلید قانونی با واژه‌های فعال دیگر (Issue 1.1)
            val newCanonicalKey = TextUtilities.toCanonicalKey(finalSourceText)
            val conflict = conceptDao.findActiveContentByCanonicalKey(canonicalSourceLang, newCanonicalKey)
            if (conflict != null && conflict.conceptId != conceptId) {
                return@withTransaction Result.failure(
                    IllegalStateException("این واژه («$finalSourceText») قبلاً در واژه‌نامه ثبت شده است.")
                )
            }

            // ISS-21: Preserve stable IDs instead of deleting all contents and generating random UUIDs
            val existingContents = conceptDao.getContentsForConcept(conceptId)
            val existingSource = existingContents.firstOrNull { it.languageCode != canonicalTargetLang }
                ?: existingContents.firstOrNull { it.languageCode == canonicalSourceLang }
            val existingTargets = existingContents.filter { it.languageCode == canonicalTargetLang }.sortedBy { it.translationIndex }

            val sourceId = existingSource?.id ?: UUID.randomUUID().toString()
            val updatedSource = ContentEntity(
                id = sourceId,
                conceptId = conceptId,
                languageCode = existingSource?.languageCode?.takeIf { it != canonicalTargetLang } ?: canonicalSourceLang,
                text = finalSourceText,
                canonicalKey = TextUtilities.toCanonicalKey(finalSourceText),
                note = note?.trim()?.ifEmpty { null },
                translationIndex = 0
            )

            val updatedTargets = finalTranslations.mapIndexed { idx, trans ->
                val targetId = if (idx < existingTargets.size) existingTargets[idx].id else UUID.randomUUID().toString()
                ContentEntity(
                    id = targetId,
                    conceptId = conceptId,
                    languageCode = canonicalTargetLang,
                    text = trans,
                    canonicalKey = TextUtilities.toCanonicalKey(trans),
                    note = null,
                    translationIndex = idx
                )
            }

            // Remove excess obsolete target translations if count decreased
            if (existingTargets.size > finalTranslations.size) {
                val toDelete = existingTargets.drop(finalTranslations.size).map { it.id }
                conceptDao.deleteContents(toDelete)
            }

            conceptDao.insertContents(listOf(updatedSource) + updatedTargets)
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

    // ISS-23: Dynamic current date ticker flow so day transition across midnight updates due and streak queries reactively
    private val currentDateFlow: Flow<String> = flow {
        while (true) {
            emit(ClockAndDayMath.todayDayString())
            delay(30_000)
        }
    }.distinctUntilChanged()

    fun getStatisticsSummary(direction: CardDirection = CardDirection.NORMAL): Flow<StatisticsSummary> {
        return currentDateFlow.flatMapLatest { today ->
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

            combine(
                stageCountsFlow,
                stageDueFlow,
                reviewsFlow,
                learningDao.getDifficultyBreakdownFlow(direction),
                learningDao.getUnstartedCountFlow(direction)
            ) { stageCounts, stageDues, reviews, diffList, unstarted ->
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
                    availableLearnedCount = availLearned,
                    unpracticedCount = unstarted
                )
            }
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
            val rText = entry.relations.joinToString("، ") { "${it.relationType.toPersianLabel()}: ${it.relatedSourceText}" }
            parts.add("روابط واژگانی: $rText")
        }
        entry.possibleCorrection?.takeIf { it.isNotBlank() }?.let { parts.add("اصلاح پیشنهادی: ${it.trim()}") }
        return parts.joinToString("\n").ifBlank { null }
    }

    suspend fun importParsedEntries(
        entries: List<ParsedEntry>,
        policy: DuplicatePolicy,
        targetCategoryId: String? = null,
        direction: CardDirection = CardDirection.NORMAL,
        onProgress: (Int, Int) -> Unit
    ): com.manidigit.yadin.domain.model.ImportSummary {
        val total = entries.size
        var addedCount = 0
        var updatedCount = 0
        var skippedCount = 0

        database.withTransaction {
            val (sourceLang, targetLang) = direction.resolveSourceAndTarget()
            val allCategories = conceptDao.getAllCategories().toMutableList()

            entries.forEachIndexed { index, entry ->
                val cleanSource = entry.sourceText.trim()
                val canonical = TextUtilities.toCanonicalKey(cleanSource)
                val existingContent = conceptDao.findActiveContentByCanonicalKey(sourceLang, canonical)
                val enrichedNote = formatEnrichedNote(entry)

                // ISS-49: Resolve categories from entry or fallback to targetCategoryId
                val entryCategoryIds = mutableListOf<String>()
                entry.categoryNames.forEach { catName ->
                    val clean = catName.trim()
                    if (clean.isNotBlank()) {
                        val matched = allCategories.find { it.name.equals(clean, ignoreCase = true) }
                        val catId = matched?.id ?: run {
                            val newCatId = UUID.randomUUID().toString()
                            val newCat = CategoryEntity(id = newCatId, name = clean, sortOrder = allCategories.size, isDefault = false)
                            conceptDao.insertCategory(newCat)
                            allCategories.add(newCat)
                            newCatId
                        }
                        entryCategoryIds.add(catId)
                    }
                }
                val primaryCatId = entryCategoryIds.firstOrNull() ?: targetCategoryId

                if (existingContent != null) {
                    val existingConcept = conceptDao.getConceptById(existingContent.conceptId)
                    val effectiveCat = primaryCatId ?: existingConcept?.categoryId
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
                                note = enrichedNote,
                                direction = direction,
                                entryType = entry.entryType
                            )
                            if (res.isSuccess) {
                                updatedCount++
                                if (entryCategoryIds.size > 1) {
                                    entryCategoryIds.drop(1).forEach { extraCat ->
                                        conceptDao.insertConceptCategory(ConceptCategoryEntity(existingContent.conceptId, extraCat))
                                    }
                                }
                            } else skippedCount++
                        }
                        DuplicatePolicy.MERGE -> {
                            val currentContents = conceptDao.getContentsForConcept(existingContent.conceptId)
                            val existingTranslations = currentContents.filter { it.languageCode == targetLang }.map { it.text }
                            val merged = TextUtilities.deduplicatePersianTranslations(existingTranslations + entry.translations)
                                .ifEmpty { (existingTranslations + entry.translations).distinct() }
                            val currentSourceContent = currentContents.firstOrNull { it.languageCode == sourceLang }
                            val mergedNote = listOfNotNull(currentSourceContent?.note, enrichedNote).distinct().joinToString("\n").ifBlank { null }
                            val res = updateWord(
                                conceptId = existingContent.conceptId,
                                sourceText = cleanSource,
                                translations = merged,
                                categoryId = effectiveCat,
                                note = mergedNote,
                                direction = direction,
                                entryType = entry.entryType
                            )
                            if (res.isSuccess) {
                                updatedCount++
                                if (entryCategoryIds.size > 1) {
                                    entryCategoryIds.drop(1).forEach { extraCat ->
                                        conceptDao.insertConceptCategory(ConceptCategoryEntity(existingContent.conceptId, extraCat))
                                    }
                                }
                            } else skippedCount++
                        }
                        DuplicatePolicy.KEEP_SEPARATE -> {
                            val res = addWord(
                                sourceText = cleanSource,
                                translations = entry.translations,
                                categoryId = primaryCatId,
                                note = enrichedNote,
                                entryType = entry.entryType
                            )
                            if (res.isSuccess) {
                                addedCount++
                                res.getOrNull()?.let { newConceptId ->
                                    if (entryCategoryIds.size > 1) {
                                        entryCategoryIds.drop(1).forEach { extraCat ->
                                            conceptDao.insertConceptCategory(ConceptCategoryEntity(newConceptId, extraCat))
                                        }
                                    }
                                }
                            } else skippedCount++
                        }
                    }
                } else {
                    val res = addWord(
                        sourceText = cleanSource,
                        translations = entry.translations,
                        categoryId = primaryCatId,
                        note = enrichedNote,
                        entryType = entry.entryType
                    )
                    if (res.isSuccess) {
                        addedCount++
                        res.getOrNull()?.let { newConceptId ->
                            if (entryCategoryIds.size > 1) {
                                entryCategoryIds.drop(1).forEach { extraCat ->
                                    conceptDao.insertConceptCategory(ConceptCategoryEntity(newConceptId, extraCat))
                                }
                            }
                        }
                    } else skippedCount++
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

    private val auditService by lazy {
        com.manidigit.yadin.domain.algorithm.VocabularyAuditService(database, conceptDao, learningDao, reviewSessionDao)
    }

    suspend fun scanLibraryForAudit(
        direction: CardDirection = CardDirection.NORMAL,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): com.manidigit.yadin.domain.model.LibraryAuditReport {
        return auditService.scanLibraryForAudit(direction, onProgress)
    }

    suspend fun applyAuditFixes(
        direction: CardDirection = CardDirection.NORMAL,
        approvedReviewItems: List<com.manidigit.yadin.domain.model.AuditReviewItem> = emptyList(),
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): com.manidigit.yadin.domain.model.LibraryAuditReport {
        return auditService.applyAuditFixes(direction, approvedReviewItems, onProgress)
    }

    suspend fun auditAndMergeLibrary(
        direction: CardDirection = CardDirection.NORMAL,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): com.manidigit.yadin.domain.model.LibraryAuditReport {
        return auditService.auditAndMergeLibrary(direction, onProgress)
    }

    suspend fun getRetentionMetrics(direction: CardDirection): com.manidigit.yadin.domain.model.RetentionMetrics {
        val totalActive = conceptDao.getActiveConceptCount()
        val learned = learningDao.getCountByStage(direction, Stage.LEARNED)
        val monthly = learningDao.getCountByStage(direction, Stage.MONTHLY)
        val weekly = learningDao.getCountByStage(direction, Stage.WEEKLY)
        val daily = learningDao.getCountByStage(direction, Stage.DAILY)
        val practiced = learningDao.getTotalPracticedWordsCount(direction)
        val unreviewed = (totalActive - practiced).coerceAtLeast(0)

        val weightedScore = if (totalActive > 0) {
            val raw = (learned * 100.0 + monthly * 80.0 + weekly * 60.0 + (practiced - learned - monthly - weekly).coerceAtLeast(0) * 35.0) / totalActive
            raw.toInt().coerceIn(0, 100)
        } else 0

        return com.manidigit.yadin.domain.model.RetentionMetrics(
            retentionScore = weightedScore,
            totalWords = totalActive,
            practicedWords = practiced,
            learnedWords = learned,
            monthlyWords = monthly,
            weeklyWords = weekly,
            dailyWords = daily,
            unreviewedWords = unreviewed
        )
    }

    suspend fun getUpcomingDueForecast(direction: CardDirection): List<com.manidigit.yadin.domain.model.ReviewForecastItem> {
        val today = ClockAndDayMath.todayDayString()
        val day1 = ClockAndDayMath.addDays(today, 1)
        val day3 = ClockAndDayMath.addDays(today, 3)
        val day7 = ClockAndDayMath.addDays(today, 7)

        val scheduledList = learningDao.getScheduledReviewsByDay(direction, day1, day7)
        val scheduledMap = scheduledList.associateBy { it.nextReviewDay }

        val tomorrowCount = scheduledMap[day1]?.count ?: 0

        var next3DaysCount = 0
        for (i in 1..3) {
            val d = ClockAndDayMath.addDays(today, i)
            next3DaysCount += scheduledMap[d]?.count ?: 0
        }

        var next7DaysCount = 0
        for (i in 1..7) {
            val d = ClockAndDayMath.addDays(today, i)
            next7DaysCount += scheduledMap[d]?.count ?: 0
        }

        return listOf(
            com.manidigit.yadin.domain.model.ReviewForecastItem(dayOffset = 1, dayString = day1, dueCount = tomorrowCount),
            com.manidigit.yadin.domain.model.ReviewForecastItem(dayOffset = 3, dayString = day3, dueCount = next3DaysCount),
            com.manidigit.yadin.domain.model.ReviewForecastItem(dayOffset = 7, dayString = day7, dueCount = next7DaysCount)
        )
    }

    suspend fun getStudyHourBuckets(): List<com.manidigit.yadin.domain.model.StudyHourBucket> {
        val history = reviewSessionDao.getRecentReviewTimestamps(2000)
        val zone = java.time.ZoneId.systemDefault()

        val buckets = com.manidigit.yadin.domain.model.StudyHourSlot.entries.associateWith {
            mutableListOf<Boolean>()
        }

        for (item in history) {
            val hour = try {
                java.time.Instant.ofEpochMilli(item.reviewedAt).atZone(zone).hour
            } catch (_: Exception) {
                12
            }
            val slot = when (hour) {
                in 6..11 -> com.manidigit.yadin.domain.model.StudyHourSlot.MORNING
                in 12..16 -> com.manidigit.yadin.domain.model.StudyHourSlot.AFTERNOON
                in 17..20 -> com.manidigit.yadin.domain.model.StudyHourSlot.EVENING
                else -> com.manidigit.yadin.domain.model.StudyHourSlot.NIGHT
            }
            buckets[slot]?.add(item.isCorrect)
        }

        return com.manidigit.yadin.domain.model.StudyHourSlot.entries.map { slot ->
            val list = buckets[slot] ?: emptyList()
            val total = list.size
            val correct = list.count { it }
            val acc = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0
            com.manidigit.yadin.domain.model.StudyHourBucket(
                slot = slot,
                reviewCount = total,
                correctCount = correct,
                accuracyPercent = acc
            )
        }
    }

    suspend fun getCategoryMasteryBreakdown(direction: CardDirection): List<com.manidigit.yadin.domain.model.CategoryMasteryItem> {
        val raw = conceptDao.getCategoryMasteryRaw(direction)
        return raw.map { r ->
            val pct = if (r.totalWords > 0) ((r.learnedWords.toFloat() / r.totalWords) * 100).toInt() else 0
            com.manidigit.yadin.domain.model.CategoryMasteryItem(
                categoryId = r.categoryId,
                categoryName = r.categoryName,
                totalWords = r.totalWords,
                learnedWords = r.learnedWords,
                masteryPercent = pct
            )
        }
    }
}
