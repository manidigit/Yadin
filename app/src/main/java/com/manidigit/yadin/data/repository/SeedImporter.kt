package com.manidigit.yadin.data.repository

import android.content.Context
import android.util.JsonReader
import com.manidigit.yadin.data.local.database.YadinDatabase
import androidx.room.withTransaction
import com.manidigit.yadin.data.local.dao.AchievementDao
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import com.manidigit.yadin.domain.text.TextUtilities
import com.manidigit.yadin.domain.time.ClockAndDayMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.UUID

class SeedImporter(
    private val context: Context,
    private val database: YadinDatabase,
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val achievementDao: AchievementDao
) {

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        conceptDao.getTotalConceptCount() == 0
    }

    suspend fun shouldImportSeed(): Boolean = withContext(Dispatchers.IO) {
        conceptDao.getTotalConceptCount() == 0
    }

    suspend fun importSeedIfNeeded(onProgress: (Float, String) -> Unit) = withContext(Dispatchers.IO) {
        if (!shouldImportSeed()) {
            ensureDefaultAchievements()
            return@withContext
        }

        onProgress(0.05f, "در حال خواندن بانک واژگان اولیه...")

        try {
            val existingConceptIds = conceptDao.getAllConcepts().map { it.id }.toSet()
            val assetStream = context.assets.open("seed/vocabulary_seed.json")
            val reader = JsonReader(InputStreamReader(assetStream, "UTF-8"))

            val categories = mutableListOf<CategoryEntity>()
            val concepts = mutableListOf<ConceptEntity>()
            val contents = mutableListOf<ContentEntity>()

            reader.beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                if (name == "payloads") {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        val payloadName = reader.nextName()
                        if (payloadName == "VOCABULARY") {
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
                                        while (reader.hasNext()) {
                                            reader.beginObject()
                                            var id = ""
                                            var categoryId: String? = null
                                            var entryTypeStr = "WORD"
                                            var act = true
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
                                                    else -> reader.skipValue()
                                                }
                                            }
                                            reader.endObject()
                                            if (id.isNotEmpty()) {
                                                val eType = try { EntryType.valueOf(entryTypeStr) } catch (_: Exception) { EntryType.WORD }
                                                concepts.add(ConceptEntity(id, eType, categoryId, act))
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
                                    else -> reader.skipValue()
                                }
                            }
                            reader.endObject()
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endObject()
                } else {
                    reader.skipValue()
                }
            }
            reader.endObject()
            reader.close()

            onProgress(0.35f, "در حال دسته‌بندی موضوعات...")
            val now = System.currentTimeMillis()
            val conceptsNeedingStates = if (existingConceptIds.isEmpty()) {
                concepts
            } else {
                concepts.filter { it.id !in existingConceptIds }
            }

            val today = ClockAndDayMath.todayDayString()
            val learningBatch = mutableListOf<LearningStateEntity>()
            val diffBatch = mutableListOf<DifficultyStateEntity>()

            conceptsNeedingStates.forEach { concept ->
                learningBatch.add(
                    LearningStateEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = concept.id,
                        direction = CardDirection.NORMAL,
                        stage = Stage.DAILY,
                        nextReviewDay = today,
                        lastReviewedDay = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                learningBatch.add(
                    LearningStateEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = concept.id,
                        direction = CardDirection.REVERSE,
                        stage = Stage.DAILY,
                        nextReviewDay = today,
                        lastReviewedDay = null,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                diffBatch.add(
                    DifficultyStateEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = concept.id,
                        direction = CardDirection.NORMAL,
                        current = VocabularyDifficulty.EASY
                    )
                )
                diffBatch.add(
                    DifficultyStateEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = concept.id,
                        direction = CardDirection.REVERSE,
                        current = VocabularyDifficulty.EASY
                    )
                )
            }

            database.withTransaction {
                if (categories.isNotEmpty()) {
                    conceptDao.insertCategories(categories)
                }

                concepts.chunked(500).forEach { chunk ->
                    conceptDao.insertConcepts(chunk)
                }

                contents.chunked(1000).forEach { chunk ->
                    conceptDao.insertContents(chunk)
                }

                if (learningBatch.isNotEmpty()) {
                    learningBatch.chunked(1000).forEach {
                        learningDao.insertLearningStates(it)
                    }
                }
                if (diffBatch.isNotEmpty()) {
                    diffBatch.chunked(1000).forEach {
                        learningDao.insertDifficultyStates(it)
                    }
                }

                ensureDefaultAchievements()
            }

            onProgress(1.0f, "آماده‌سازی با موفقیت پایان یافت.")
        } catch (e: Exception) {
            e.printStackTrace()
            ensureDefaultAchievements()
        }
    }

    private suspend fun ensureDefaultAchievements() {
        val initialAchievements = AchievementId.values().map {
            AchievementEntity(id = it.name, unlockedAt = null, progress = 0)
        }
        achievementDao.insertAchievements(initialAchievements)
    }
}
