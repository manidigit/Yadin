package com.manidigit.yadin.data.repository

import android.content.Context
import android.util.JsonReader
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.UUID

class SeedImporter(
    private val context: Context,
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val achievementDao: AchievementDao
) {

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        conceptDao.getTotalConceptCount() == 0
    }

    suspend fun importSeedIfNeeded(onProgress: (Float, String) -> Unit) = withContext(Dispatchers.IO) {
        if (!isDatabaseEmpty()) {
            ensureDefaultAchievements()
            return@withContext
        }

        onProgress(0.05f, "در حال خواندن بانک واژگان اولیه...")

        try {
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
                                                contents.add(ContentEntity(id, conceptId, lang, text, canonical, notes, null, transIdx))
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
            if (categories.isNotEmpty()) {
                conceptDao.insertCategories(categories)
            }

            onProgress(0.50f, "در حال ذخیره‌سازی واژگان (${concepts.size} مدخل)...")
            // Batch insert concepts in chunks of 500
            val now = System.currentTimeMillis()
            concepts.chunked(500).forEachIndexed { i, chunk ->
                conceptDao.insertConcepts(chunk)
                val p = 0.50f + (0.20f * (i.toFloat() / (concepts.size / 500 + 1)))
                onProgress(p, "در حال درج واژه‌ها (${(i + 1) * 500}/${concepts.size})...")
            }

            onProgress(0.70f, "در حال ذخیره‌سازی ترجمه‌ها...")
            contents.chunked(1000).forEach { chunk ->
                conceptDao.insertContents(chunk)
            }

            onProgress(0.85f, "در حال تنظیم مراحل یادگیری...")
            // Create initial learning states and difficulty states for all concepts
            val learningBatch = mutableListOf<LearningStateEntity>()
            val diffBatch = mutableListOf<DifficultyStateEntity>()

            concepts.forEach { concept ->
                learningBatch.add(
                    LearningStateEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = concept.id,
                        direction = CardDirection.NORMAL,
                        stage = Stage.DAILY,
                        nextReviewDay = null,
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
                        nextReviewDay = null,
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
                        current = VocabularyDifficulty.MEDIUM
                    )
                )
                diffBatch.add(
                    DifficultyStateEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = concept.id,
                        direction = CardDirection.REVERSE,
                        current = VocabularyDifficulty.MEDIUM
                    )
                )
            }

            learningBatch.chunked(1000).forEach {
                learningDao.insertLearningStates(it)
            }
            diffBatch.chunked(1000).forEach {
                learningDao.insertDifficultyStates(it)
            }

            onProgress(0.95f, "در حال آماده‌سازی دستاوردها...")
            ensureDefaultAchievements()

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
