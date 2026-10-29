package com.manidigit.yadin.data.repository

import android.content.Context
import android.util.JsonReader
import androidx.room.withTransaction
import com.manidigit.yadin.R
import com.manidigit.yadin.data.local.dao.AchievementDao
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.database.YadinDatabase
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.data.local.entity.SettingEntity
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SessionItemState
import com.manidigit.yadin.domain.model.SessionStatus
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.domain.time.ClockAndDayMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SeedImporter(
    private val context: Context,
    private val database: YadinDatabase,
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val achievementDao: AchievementDao,
    private val settingsDao: com.manidigit.yadin.data.local.dao.SettingsDao = database.settingsDao()
) {

    companion object {
        const val CURRENT_SEED_VERSION = "7875_v1.78.0"
    }

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        conceptDao.getTotalConceptCount() == 0
    }

    suspend fun shouldImportSeed(): Boolean = withContext(Dispatchers.IO) {
        val totalCount = conceptDao.getTotalConceptCount()
        if (totalCount < 7800) {
            return@withContext true
        }
        val seedVersion = settingsDao.getSetting("seedVersion")
        if (seedVersion != CURRENT_SEED_VERSION) {
            return@withContext true
        }
        val alreadyImported = settingsDao.getSetting("seedImported") == "true"
        if (alreadyImported) {
            val learningCount = learningDao.getLearningStateCount()
            val difficultyCount = learningDao.getDifficultyStateCount()
            if (learningCount < totalCount * 2 || difficultyCount < totalCount * 2) {
                repairMissingStatesQuickly()
            }
            return@withContext false
        }
        totalCount == 0
    }

    private suspend fun repairMissingStatesQuickly() {
        try {
            val allConceptIds = conceptDao.getAllConcepts().map { it.id }
            val existingLearningKeys = learningDao.getAllLearningStates()
                .map { it.conceptId to it.direction }
                .toSet()
            val existingDiffKeys = learningDao.getAllDifficultyStates()
                .map { it.conceptId to it.direction }
                .toSet()

            val now = System.currentTimeMillis()
            val today = ClockAndDayMath.todayDayString()
            val missingLearningBatch = mutableListOf<LearningStateEntity>()
            val missingDiffBatch = mutableListOf<DifficultyStateEntity>()

            for (cid in allConceptIds) {
                for (dir in listOf(CardDirection.NORMAL, CardDirection.REVERSE)) {
                    val key = cid to dir
                    if (key !in existingLearningKeys) {
                        missingLearningBatch.add(
                            LearningStateEntity(
                                id = UUID.randomUUID().toString(),
                                conceptId = cid,
                                direction = dir,
                                stage = Stage.DAILY,
                                nextReviewDay = today,
                                lastReviewedDay = null,
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    }
                    if (key !in existingDiffKeys) {
                        missingDiffBatch.add(
                            DifficultyStateEntity(
                                id = UUID.randomUUID().toString(),
                                conceptId = cid,
                                direction = dir,
                                current = VocabularyDifficulty.EASY
                            )
                        )
                    }
                }
            }

            if (missingLearningBatch.isNotEmpty() || missingDiffBatch.isNotEmpty()) {
                database.withTransaction {
                    missingLearningBatch.chunked(1000).forEach {
                        learningDao.insertLearningStatesIgnore(it)
                    }
                    missingDiffBatch.chunked(1000).forEach {
                        learningDao.insertDifficultyStatesIgnore(it)
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    suspend fun importSeedIfNeeded(onProgress: (Float, String) -> Unit) = withContext(Dispatchers.IO) {
        onProgress(0.05f, context.getString(R.string.seed_reading_initial_data))

        try {
            val existingConceptsMap = conceptDao.getAllConcepts().associateBy { it.id }
            val existingConceptIds = existingConceptsMap.keys

            // در صورتی که دیتابیس از قبل دارای واژگان بوده اما به دلیل خطای CASCADE در نسخه قبلی
            // رکوردهای learning_states آسیب دیده باشند، ابتدا از آخرین فایل پشتیبان داخلی (در صورت وجود) بازیابی شود
            if (existingConceptIds.isNotEmpty()) {
                val currentLearningCount = learningDao.getLearningStateCount()
                if (currentLearningCount < existingConceptIds.size * 2) {
                    recoverProgressFromLocalBackupsIfAvailable(existingConceptsMap)
                }
            }

            val assetStream = context.assets.open("seed/vocabulary_seed.json")
            val reader = JsonReader(InputStreamReader(assetStream, "UTF-8"))

            val categories = mutableListOf<CategoryEntity>()
            val concepts = mutableListOf<ConceptEntity>()
            val contents = mutableListOf<ContentEntity>()
            val seedDifficultyStates = mutableListOf<DifficultyStateEntity>()
            val seedConceptCategories = mutableListOf<ConceptCategoryEntity>()
            val obsoleteSeedConceptIds = mutableSetOf<String>()

            fun parseVocabularyObject(reader: JsonReader) {
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "categories" -> {
                            reader.beginArray()
                            var sortOrder = 0
                            while (reader.hasNext()) {
                                reader.beginObject()
                                var id = ""
                                var catName = ""
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "id" -> id = reader.nextString()
                                        "name" -> catName = reader.nextString()
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (id.isNotEmpty() && catName.isNotEmpty()) {
                                    categories.add(CategoryEntity(id, catName, sortOrder++, false))
                                }
                            }
                            reader.endArray()
                        }
                        "concepts" -> {
                            reader.beginArray()
                            val fallbackNow = System.currentTimeMillis()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                var id = ""
                                var categoryId: String? = null
                                var entryTypeStr = "WORD"
                                var act = true
                                var cAt = fallbackNow
                                var uAt = fallbackNow
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "id" -> id = reader.nextString()
                                        "categoryId" -> {
                                            if (reader.peek() == android.util.JsonToken.NULL) {
                                                reader.nextNull()
                                            } else {
                                                categoryId = reader.nextString()
                                            }
                                        }
                                        "entryType" -> entryTypeStr = reader.nextString()
                                        "active" -> act = reader.nextBoolean()
                                        "createdAt" -> cAt = reader.nextLong()
                                        "updatedAt" -> uAt = reader.nextLong()
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (id.isNotEmpty()) {
                                    val eType = try { EntryType.valueOf(entryTypeStr) } catch (_: Exception) { EntryType.WORD }
                                    concepts.add(ConceptEntity(id, eType, categoryId, act, cAt, uAt))
                                }
                            }
                            reader.endArray()
                        }
                        "contents" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                var id = ""
                                var conceptId = ""
                                var lang = "es"
                                var text = ""
                                var canonical = ""
                                var notes: String? = null
                                var transIdx = 0
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "id" -> id = reader.nextString()
                                        "conceptId" -> conceptId = reader.nextString()
                                        "languageCode" -> lang = reader.nextString()
                                        "text" -> text = reader.nextString()
                                        "canonicalKey" -> canonical = reader.nextString()
                                        "notes", "note" -> {
                                            if (reader.peek() == android.util.JsonToken.NULL) {
                                                reader.nextNull()
                                            } else {
                                                notes = reader.nextString()
                                            }
                                        }
                                        "translationIndex" -> transIdx = reader.nextInt()
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (id.isNotEmpty() && conceptId.isNotEmpty()) {
                                    if (canonical.isEmpty()) {
                                        canonical = TextUtilities.toCanonicalKey(text)
                                    }
                                    contents.add(ContentEntity(id, conceptId, lang, text, canonical, notes, transIdx))
                                }
                            }
                            reader.endArray()
                        }
                        "difficultyStates" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                var id = ""
                                var conceptId = ""
                                var dirStr = "NORMAL"
                                var currStr = "EASY"
                                var consCorrect = 0
                                var consWrong = 0
                                var reachedVh = false
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "id" -> id = reader.nextString()
                                        "conceptId" -> conceptId = reader.nextString()
                                        "direction" -> dirStr = reader.nextString()
                                        "current" -> currStr = reader.nextString()
                                        "consecutiveCorrect" -> consCorrect = reader.nextInt()
                                        "consecutiveWrong" -> consWrong = reader.nextInt()
                                        "hasReachedVeryHard" -> reachedVh = reader.nextBoolean()
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (id.isNotEmpty() && conceptId.isNotEmpty()) {
                                    val cardDir = try { CardDirection.valueOf(dirStr) } catch (_: Exception) { CardDirection.NORMAL }
                                    val diffVal = try { VocabularyDifficulty.valueOf(currStr) } catch (_: Exception) { VocabularyDifficulty.EASY }
                                    seedDifficultyStates.add(
                                        DifficultyStateEntity(
                                            id = id,
                                            conceptId = conceptId,
                                            direction = cardDir,
                                            current = diffVal,
                                            consecutiveCorrect = consCorrect,
                                            consecutiveWrong = consWrong,
                                            hasReachedVeryHard = reachedVh || diffVal == VocabularyDifficulty.VERY_HARD
                                        )
                                    )
                                }
                            }
                            reader.endArray()
                        }
                        "conceptCategories" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.beginObject()
                                var cId = ""
                                var catId = ""
                                while (reader.hasNext()) {
                                    when (reader.nextName()) {
                                        "conceptId" -> cId = reader.nextString()
                                        "categoryId" -> catId = reader.nextString()
                                        else -> reader.skipValue()
                                    }
                                }
                                reader.endObject()
                                if (cId.isNotEmpty() && catId.isNotEmpty()) {
                                    seedConceptCategories.add(ConceptCategoryEntity(cId, catId))
                                }
                            }
                            reader.endArray()
                        }
                        "obsoleteConceptIds" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                val obsId = reader.nextString()
                                if (obsId.isNotEmpty()) {
                                    obsoleteSeedConceptIds.add(obsId)
                                }
                            }
                            reader.endArray()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "data" -> parseVocabularyObject(reader)
                    "payloads" -> {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            val payloadName = reader.nextName()
                            if (payloadName == "VOCABULARY") {
                                parseVocabularyObject(reader)
                            } else {
                                reader.skipValue()
                            }
                        }
                        reader.endObject()
                    }
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            reader.close()

            onProgress(0.45f, context.getString(R.string.seed_categorizing_topics))

            val existingLearningStates = if (existingConceptIds.isNotEmpty()) {
                learningDao.getAllLearningStates()
            } else {
                emptyList()
            }
            val practicedConceptIds = existingLearningStates
                .filter { it.lastReviewedDay != null || it.stage != Stage.DAILY || it.sm2Repetitions > 0 || it.fsrsReps > 0 }
                .map { it.conceptId }
                .toSet()
            val practicedLearningKeys = existingLearningStates
                .filter { it.lastReviewedDay != null || it.stage != Stage.DAILY || it.sm2Repetitions > 0 || it.fsrsReps > 0 }
                .map { it.conceptId to it.direction }
                .toSet()

            // حذف مفاهیم تکراری ادغام‌شده از بذر قدیمی که توسط کاربر تمرین نشده‌اند
            val obsoleteUnpracticedToDelete = obsoleteSeedConceptIds
                .filter { it in existingConceptIds && it !in practicedConceptIds }

            val activeExistingConceptIds = existingConceptIds - obsoleteUnpracticedToDelete.toSet()

            // ۱. تفکیک مفاهیم جدید از مفاهیم موجود:
            // هرگز از REPLACE روی جدول concepts استفاده نمی‌کنیم، زیرا در SQLite دستور INSERT OR REPLACE
            // ابتدا سطر والد را DELETE کرده و باعث فعال شدن ON DELETE CASCADE و پاک شدن کل پیشرفت (learning_states) می‌شود!
            val newConcepts = concepts.filter { it.id !in activeExistingConceptIds }
            val conceptsToUpdateCat = concepts.mapNotNull { seedConcept ->
                val existing = existingConceptsMap[seedConcept.id]
                if (existing != null && seedConcept.id in activeExistingConceptIds) {
                    val newCatId = seedConcept.categoryId ?: existing.categoryId
                    if (newCatId != existing.categoryId || existing.entryType != seedConcept.entryType) {
                        existing.copy(
                            categoryId = newCatId,
                            entryType = seedConcept.entryType
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

            // ۲. حفظ یادداشت‌های شخصی معتبر کاربر و پاک‌سازی ترجمه‌های تکراری بدون دست زدن به جدول والد concepts
            val existingContentsByConcept = if (activeExistingConceptIds.isNotEmpty()) {
                conceptDao.getAllContents()
                    .filter { it.conceptId in activeExistingConceptIds }
                    .groupBy { it.conceptId }
            } else {
                emptyMap()
            }

            val legacyNoteRegex = Regex("""^[\d۰-۹\s.،؛مترادفابعادزیادماشینحجمموتور]+$""")
            val mergedSeedContents = contents.map { seedContent ->
                if (seedContent.languageCode != "fa") {
                    val existingSource = existingContentsByConcept[seedContent.conceptId]?.firstOrNull { it.languageCode != "fa" }
                    val existingNote = existingSource?.note?.takeIf { it.isNotBlank() }
                    val isCorruptedLegacyNote = existingNote != null && (
                        existingNote.contains("توضیح جداشده") ||
                        legacyNoteRegex.matches(existingNote.trim())
                    )
                    if (existingNote != null && !isCorruptedLegacyNote && seedContent.note.isNullOrBlank()) {
                        seedContent.copy(note = existingNote)
                    } else {
                        seedContent
                    }
                } else {
                    seedContent
                }
            }.toMutableList()

            val primaryConceptCategories = concepts.mapNotNull { concept ->
                concept.categoryId?.let { catId ->
                    ConceptCategoryEntity(concept.id, catId)
                }
            }
            val allConceptCategories = (primaryConceptCategories + seedConceptCategories).distinct()

            val now = System.currentTimeMillis()
            val today = ClockAndDayMath.todayDayString()
            val seedDiffMap = seedDifficultyStates.associateBy { it.conceptId to it.direction }

            database.withTransaction {
                if (obsoleteUnpracticedToDelete.isNotEmpty()) {
                    obsoleteUnpracticedToDelete.chunked(500).forEach { chunkIds ->
                        conceptDao.deleteConceptsPermanently(chunkIds)
                    }
                }

                if (categories.isNotEmpty()) {
                    conceptDao.insertCategories(categories)
                }

                if (newConcepts.isNotEmpty()) {
                    newConcepts.chunked(500).forEach { chunk ->
                        conceptDao.insertConceptsIgnore(chunk)
                    }
                }

                if (conceptsToUpdateCat.isNotEmpty()) {
                    conceptsToUpdateCat.chunked(500).forEach { chunk ->
                        conceptDao.updateConcepts(chunk)
                    }
                }

                if (activeExistingConceptIds.isNotEmpty()) {
                    // به‌روزرسانی جدول محتوا و دسته‌بندی برای واژگان پایه (بدون دست زدن به جدول والد concepts)
                    concepts.map { it.id }.chunked(500).forEach { chunkIds ->
                        conceptDao.deleteContentsForConcepts(chunkIds)
                        conceptDao.deleteConceptCategoriesForConcepts(chunkIds)
                    }
                }

                onProgress(0.65f, context.getString(R.string.seed_categorizing_topics))

                if (allConceptCategories.isNotEmpty()) {
                    allConceptCategories.chunked(1000).forEach { chunk ->
                        conceptDao.insertConceptCategories(chunk)
                    }
                }

                mergedSeedContents.chunked(1000).forEach { chunk ->
                    conceptDao.insertContents(chunk)
                }

                onProgress(0.85f, context.getString(R.string.seed_categorizing_topics))

                // ۳. تضمین وجود رکوردهای learning_states و difficulty_states برای تمام واژگان (عادی و معکوس)
                // بدون بازنویسی رکوردهای تمرین‌شده کاربر
                val allDbConceptIds = (activeExistingConceptIds + concepts.map { it.id }).distinct()
                val existingLearningKeys = learningDao.getAllLearningStates()
                    .map { it.conceptId to it.direction }
                    .toSet()
                val existingDiffMap = learningDao.getAllDifficultyStates()
                    .associateBy { it.conceptId to it.direction }

                val missingLearningBatch = mutableListOf<LearningStateEntity>()
                val missingDiffBatch = mutableListOf<DifficultyStateEntity>()
                val updatedSeedDiffBatch = mutableListOf<DifficultyStateEntity>()

                for (cid in allDbConceptIds) {
                    for (dir in listOf(CardDirection.NORMAL, CardDirection.REVERSE)) {
                        val key = cid to dir
                        if (key !in existingLearningKeys) {
                            missingLearningBatch.add(
                                LearningStateEntity(
                                    id = UUID.randomUUID().toString(),
                                    conceptId = cid,
                                    direction = dir,
                                    stage = Stage.DAILY,
                                    nextReviewDay = today,
                                    lastReviewedDay = null,
                                    createdAt = now,
                                    updatedAt = now
                                )
                            )
                        }
                        val existingDiff = existingDiffMap[key]
                        val seedDiff = seedDiffMap[key]
                        if (existingDiff == null) {
                            missingDiffBatch.add(
                                seedDiff ?: DifficultyStateEntity(
                                    id = UUID.randomUUID().toString(),
                                    conceptId = cid,
                                    direction = dir,
                                    current = VocabularyDifficulty.EASY
                                )
                            )
                        } else if (seedDiff != null) {
                            val isUnpracticedUpdate = key !in practicedLearningKeys && (
                                existingDiff.current != seedDiff.current ||
                                    existingDiff.hasReachedVeryHard != seedDiff.hasReachedVeryHard ||
                                    existingDiff.consecutiveCorrect != 0 ||
                                    existingDiff.consecutiveWrong != 0
                            )
                            val isPracticedSeedDifficultyUpgrade = key in practicedLearningKeys && (
                                seedDiff.current.ordinal > existingDiff.current.ordinal ||
                                    (seedDiff.current == VocabularyDifficulty.HARD && existingDiff.current != VocabularyDifficulty.HARD)
                            )
                            if (isUnpracticedUpdate || isPracticedSeedDifficultyUpgrade) {
                                updatedSeedDiffBatch.add(
                                    existingDiff.copy(
                                        current = seedDiff.current,
                                        consecutiveCorrect = 0,
                                        consecutiveWrong = 0,
                                        hasReachedVeryHard = existingDiff.hasReachedVeryHard || seedDiff.hasReachedVeryHard || seedDiff.current == VocabularyDifficulty.VERY_HARD
                                    )
                                )
                            }
                        }
                    }
                }

                if (missingLearningBatch.isNotEmpty()) {
                    missingLearningBatch.chunked(1000).forEach {
                        learningDao.insertLearningStatesIgnore(it)
                    }
                }
                if (missingDiffBatch.isNotEmpty()) {
                    missingDiffBatch.chunked(1000).forEach {
                        learningDao.insertDifficultyStatesIgnore(it)
                    }
                }
                if (updatedSeedDiffBatch.isNotEmpty()) {
                    updatedSeedDiffBatch.chunked(1000).forEach {
                        learningDao.insertDifficultyStates(it)
                    }
                }

                ensureDefaultAchievements()
                settingsDao.setSetting(SettingEntity("seedImported", "true"))
                settingsDao.setSetting(SettingEntity("seedVersion", CURRENT_SEED_VERSION))
            }

            onProgress(1.0f, context.getString(R.string.seed_import_completed))
        } catch (e: Exception) {
            e.printStackTrace()
            ensureDefaultAchievements()
        }
    }

    /**
     * بازیابی خودکار پیشرفت مطالعه از فایل‌های پشتیبان داخلی (پوشه safety_backups یا backups)
     * در صورتی که رکوردهای پیشرفت بر اثر خطای CASCADE نسخه قبلی پاک شده باشند.
     */
    private suspend fun recoverProgressFromLocalBackupsIfAvailable(
        existingConceptsMap: Map<String, ConceptEntity>
    ) {
        try {
            val candidateDirs = listOfNotNull(
                File(context.filesDir, "safety_backups"),
                File(context.filesDir, "backups"),
                context.getExternalFilesDir(null)
            )
            val jsonFiles = candidateDirs
                .filter { it.exists() && it.isDirectory }
                .flatMap { dir ->
                    dir.listFiles { f -> f.isFile && f.name.endsWith(".json", ignoreCase = true) }?.toList() ?: emptyList()
                }
                .sortedByDescending { it.lastModified() }

            if (jsonFiles.isEmpty()) return

            val existingContents = conceptDao.getAllContents()
            val existingContentsByKey = existingContents.groupBy { it.languageCode to it.canonicalKey }
            val reviewSessionDao = database.reviewSessionDao()

            for (file in jsonFiles) {
                try {
                    val jsonString = file.readText(Charsets.UTF_8)
                    val root = JSONObject(jsonString)
                    val data = JSONObject()

                    root.optJSONObject("data")?.let { direct ->
                        val keys = direct.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            data.put(k, direct.get(k))
                        }
                    }
                    root.optJSONObject("payloads")?.let { payloads ->
                        val pKeys = payloads.keys()
                        while (pKeys.hasNext()) {
                            val pKey = pKeys.next()
                            payloads.optJSONObject(pKey)?.let { pObj ->
                                val keys = pObj.keys()
                                while (keys.hasNext()) {
                                    val k = keys.next()
                                    data.put(k, pObj.get(k))
                                }
                            }
                        }
                    }

                    val lsJson = data.optJSONArray("learningStates")
                    val histJson = data.optJSONArray("reviewHistory")
                    if ((lsJson == null || lsJson.length() == 0) && (histJson == null || histJson.length() == 0)) {
                        continue
                    }

                    // نگاشت شناسه‌های کانسپت فایل پشتیبان به شناسه‌های موجود در دیتابیس
                    val idMap = mutableMapOf<String, String>()
                    val bContentsByConcept = mutableMapOf<String, MutableList<Pair<String, String>>>()
                    data.optJSONArray("contents")?.let { arr ->
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val cid = o.optString("conceptId", "")
                            val lang = o.optString("languageCode", "es")
                            val text = o.optString("text", "")
                            val canon = o.optString("canonicalKey", TextUtilities.toCanonicalKey(text))
                            if (cid.isNotEmpty()) {
                                bContentsByConcept.getOrPut(cid) { mutableListOf() }.add(lang to canon)
                            }
                        }
                    }

                    data.optJSONArray("concepts")?.let { arr ->
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val bId = o.optString("id", "")
                            if (bId.isNotEmpty()) {
                                if (existingConceptsMap.containsKey(bId)) {
                                    idMap[bId] = bId
                                } else {
                                    val pairs = bContentsByConcept[bId] ?: emptyList()
                                    val esPair = pairs.firstOrNull { it.first == "es" } ?: pairs.firstOrNull()
                                    val matchedId = esPair?.let { pair ->
                                        existingContentsByKey[pair]?.firstOrNull()?.conceptId
                                    }
                                    if (matchedId != null) {
                                        idMap[bId] = matchedId
                                    }
                                }
                            }
                        }
                    }

                    fun resolveConceptId(rawId: String): String? {
                        return idMap[rawId] ?: if (existingConceptsMap.containsKey(rawId)) rawId else null
                    }

                    val recoveredLearning = mutableListOf<LearningStateEntity>()
                    var hasPracticed = false
                    if (lsJson != null) {
                        for (i in 0 until lsJson.length()) {
                            val o = lsJson.getJSONObject(i)
                            val cid = resolveConceptId(o.optString("conceptId", "")) ?: continue
                            val dir = runCatching { CardDirection.valueOf(o.optString("direction", "NORMAL")) }.getOrDefault(CardDirection.NORMAL)
                            val stage = runCatching { Stage.valueOf(o.optString("stage", "DAILY")) }.getOrDefault(Stage.DAILY)
                            val nextDay = o.optString("nextReviewDay", "").ifEmpty { o.optString("nextReviewAt", "") }.takeIf { it.length >= 10 }?.take(10)
                            val lastDay = o.optString("lastReviewedDay", "").ifEmpty { o.optString("lastReviewedAt", "") }.takeIf { it.length >= 10 }?.take(10)
                            val sm2Reps = o.optInt("sm2Repetitions", 0)
                            val sm2Ef = o.optDouble("sm2EasinessFactor", 2.5).let { if (it.isNaN() || it < 1.3) 2.5 else it }
                            val sm2Interval = o.optInt("sm2IntervalDays", 0)
                            val fsrsStab = o.optDouble("fsrsStability", 0.0).let { if (it.isNaN() || it < 0.0) 0.0 else it }
                            val fsrsDiff = o.optDouble("fsrsDifficulty", 5.0).let { if (it.isNaN() || it < 1.0 || it > 10.0) 5.0 else it }
                            val fsrsReps = o.optInt("fsrsReps", 0)
                            val fsrsLapses = o.optInt("fsrsLapses", 0)
                            val fsrsState = o.optInt("fsrsState", 0)
                            if (lastDay != null || stage != Stage.DAILY || sm2Reps > 0 || fsrsReps > 0) {
                                hasPracticed = true
                            }
                            recoveredLearning.add(
                                LearningStateEntity(
                                    id = o.optString("id", UUID.randomUUID().toString()),
                                    conceptId = cid,
                                    direction = dir,
                                    stage = stage,
                                    nextReviewDay = nextDay,
                                    lastReviewedDay = lastDay,
                                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                                    sm2Repetitions = sm2Reps,
                                    sm2EasinessFactor = sm2Ef,
                                    sm2IntervalDays = sm2Interval,
                                    fsrsStability = fsrsStab,
                                    fsrsDifficulty = fsrsDiff,
                                    fsrsReps = fsrsReps,
                                    fsrsLapses = fsrsLapses,
                                    fsrsState = fsrsState
                                )
                            )
                        }
                    }

                    if (!hasPracticed && (histJson == null || histJson.length() == 0)) {
                        continue
                    }

                    val recoveredDiff = mutableListOf<DifficultyStateEntity>()
                    data.optJSONArray("difficultyStates")?.let { dsArr ->
                        for (i in 0 until dsArr.length()) {
                            val o = dsArr.getJSONObject(i)
                            val cid = resolveConceptId(o.optString("conceptId", "")) ?: continue
                            val dir = runCatching { CardDirection.valueOf(o.optString("direction", "NORMAL")) }.getOrDefault(CardDirection.NORMAL)
                            val current = runCatching { VocabularyDifficulty.valueOf(o.optString("current", "EASY")) }.getOrDefault(VocabularyDifficulty.EASY)
                            recoveredDiff.add(
                                DifficultyStateEntity(
                                    id = o.optString("id", UUID.randomUUID().toString()),
                                    conceptId = cid,
                                    direction = dir,
                                    current = current,
                                    consecutiveCorrect = o.optInt("consecutiveCorrect", 0),
                                    consecutiveWrong = o.optInt("consecutiveWrong", 0),
                                    hasReachedVeryHard = o.optBoolean("hasReachedVeryHard", false)
                                )
                            )
                        }
                    }

                    val recoveredSessions = mutableListOf<ReviewSessionEntity>()
                    data.optJSONArray("reviewSessions")?.let { sArr ->
                        for (i in 0 until sArr.length()) {
                            val o = sArr.getJSONObject(i)
                            val sid = o.optString("id", "")
                            if (sid.isNotEmpty()) {
                                val rt = runCatching { ReviewType.valueOf(o.optString("reviewType", "DAILY")) }.getOrDefault(ReviewType.DAILY)
                                val m = runCatching { ReviewMode.valueOf(o.optString("mode", "FLASHCARD")) }.getOrDefault(ReviewMode.FLASHCARD)
                                val dir = runCatching { CardDirection.valueOf(o.optString("direction", "NORMAL")) }.getOrDefault(CardDirection.NORMAL)
                                val st = runCatching { SessionStatus.valueOf(o.optString("status", "COMPLETED")) }.getOrDefault(SessionStatus.COMPLETED)
                                val qlStr = o.optString("quizLevel", "")
                                val ql = if (qlStr.isNotEmpty()) QuizLevel.fromString(qlStr) else null
                                recoveredSessions.add(
                                    ReviewSessionEntity(
                                        id = sid,
                                        startedAt = o.optLong("startedAt", System.currentTimeMillis()),
                                        endedAt = if (o.has("endedAt") && !o.isNull("endedAt")) o.optLong("endedAt") else null,
                                        reviewType = rt,
                                        mode = m,
                                        direction = dir,
                                        quizLevel = ql,
                                        status = st,
                                        currentPosition = o.optInt("currentPosition", 0),
                                        totalItems = o.optInt("totalItems", 0)
                                    )
                                )
                            }
                        }
                    }

                    val recoveredSessionItems = mutableListOf<ReviewSessionItemEntity>()
                    data.optJSONArray("reviewSessionItems")?.let { siArr ->
                        for (i in 0 until siArr.length()) {
                            val o = siArr.getJSONObject(i)
                            val cid = resolveConceptId(o.optString("conceptId", "")) ?: continue
                            val dir = runCatching { CardDirection.valueOf(o.optString("direction", "NORMAL")) }.getOrDefault(CardDirection.NORMAL)
                            val st = runCatching { SessionItemState.valueOf(o.optString("state", "PENDING")) }.getOrDefault(SessionItemState.PENDING)
                            recoveredSessionItems.add(
                                ReviewSessionItemEntity(
                                    id = o.optString("id", UUID.randomUUID().toString()),
                                    sessionId = o.optString("sessionId", ""),
                                    position = o.optInt("position", i),
                                    conceptId = cid,
                                    direction = dir,
                                    state = st
                                )
                            )
                        }
                    }

                    val recoveredHistory = mutableListOf<ReviewHistoryEntity>()
                    if (histJson != null) {
                        for (i in 0 until histJson.length()) {
                            val o = histJson.getJSONObject(i)
                            val cid = resolveConceptId(o.optString("conceptId", "")) ?: continue
                            val dir = runCatching { CardDirection.valueOf(o.optString("direction", "NORMAL")) }.getOrDefault(CardDirection.NORMAL)
                            val rt = runCatching { ReviewType.valueOf(o.optString("reviewType", "DAILY")) }.getOrDefault(ReviewType.DAILY)
                            val m = runCatching { ReviewMode.valueOf(o.optString("mode", "FLASHCARD")) }.getOrDefault(ReviewMode.FLASHCARD)
                            val sb = runCatching { Stage.valueOf(o.optString("stageBefore", "DAILY")) }.getOrDefault(Stage.DAILY)
                            val qlStr = o.optString("quizLevel", "")
                            val ql = if (qlStr.isNotEmpty()) QuizLevel.fromString(qlStr) else null
                            val revAt = o.optLong("reviewedAt", System.currentTimeMillis())
                            val revDay = o.optString("reviewedDay", "").takeIf { it.length >= 10 }?.take(10)
                                ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(revAt))
                            recoveredHistory.add(
                                ReviewHistoryEntity(
                                    id = o.optString("id", UUID.randomUUID().toString()),
                                    sessionId = o.optString("sessionId", UUID.randomUUID().toString()),
                                    reviewAttemptId = o.optString("reviewAttemptId", "").ifEmpty { UUID.randomUUID().toString() },
                                    conceptId = cid,
                                    direction = dir,
                                    reviewedAt = revAt,
                                    reviewedDay = revDay,
                                    isCorrect = o.optBoolean("isCorrect", true),
                                    reviewType = rt,
                                    mode = m,
                                    stageBefore = sb,
                                    quizLevel = ql,
                                    optionsJson = if (o.has("optionsJson") && !o.isNull("optionsJson")) o.optString("optionsJson") else null,
                                    selectedIndex = if (o.has("selectedIndex") && !o.isNull("selectedIndex")) o.optInt("selectedIndex") else null,
                                    correctIndex = if (o.has("correctIndex") && !o.isNull("correctIndex")) o.optInt("correctIndex") else null
                                )
                            )
                        }
                    }

                    database.withTransaction {
                        if (recoveredLearning.isNotEmpty()) {
                            recoveredLearning.chunked(500).forEach { learningDao.insertLearningStates(it) }
                        }
                        if (recoveredDiff.isNotEmpty()) {
                            recoveredDiff.chunked(500).forEach { learningDao.insertDifficultyStates(it) }
                        }
                        if (recoveredSessions.isNotEmpty()) {
                            recoveredSessions.chunked(500).forEach { reviewSessionDao.insertSessions(it) }
                        }
                        val validSessionIds = reviewSessionDao.getAllSessions().map { it.id }.toSet()
                        val validSessionItems = recoveredSessionItems.filter { it.sessionId in validSessionIds }
                        if (validSessionItems.isNotEmpty()) {
                            validSessionItems.chunked(500).forEach { reviewSessionDao.insertSessionItems(it) }
                        }
                        val validHistory = recoveredHistory.filter { it.sessionId in validSessionIds }
                        if (validHistory.isNotEmpty()) {
                            validHistory.chunked(500).forEach { reviewSessionDao.insertHistoryItems(it) }
                        }
                    }
                    break
                } catch (_: Exception) {
                    // اگر یک فایل بکاپ نامعتبر بود، فایل بعدی بررسی شود
                }
            }
        } catch (_: Exception) {
            // نادیده گرفتن خطا در بازیابی خودکار
        }
    }

    private suspend fun ensureDefaultAchievements() {
        val initialAchievements = AchievementId.values().map {
            AchievementEntity(id = it.name, unlockedAt = null, progress = 0)
        }
        achievementDao.insertAchievements(initialAchievements)
    }
}
