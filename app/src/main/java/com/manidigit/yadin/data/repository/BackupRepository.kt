package com.manidigit.yadin.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.manidigit.yadin.data.local.dao.AchievementDao
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.dao.SettingsDao
import com.manidigit.yadin.data.local.database.YadinDatabase
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.local.entity.CategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.data.local.entity.ReviewHistoryEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionEntity
import com.manidigit.yadin.data.local.entity.ReviewSessionItemEntity
import com.manidigit.yadin.data.local.entity.SettingEntity
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.ReviewMode
import com.manidigit.yadin.domain.model.ReviewType
import com.manidigit.yadin.domain.model.SessionItemState
import com.manidigit.yadin.domain.model.SessionStatus
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class BackupType {
    VOCABULARY,
    PROGRESS,
    FULL
}

class RestoreContext {
    private val conceptIdMap = mutableMapOf<String, String>()
    fun remapConceptId(backupId: String): String = conceptIdMap[backupId] ?: backupId
    fun registerMapping(backupId: String, localId: String) {
        conceptIdMap[backupId] = localId
    }
}

class BackupRepository(
    private val context: Context,
    private val database: YadinDatabase,
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val reviewSessionDao: ReviewSessionDao,
    private val settingsDao: SettingsDao,
    private val achievementDao: AchievementDao
) {

    suspend fun createBackupJson(
        type: BackupType = BackupType.FULL,
        onProgress: (Float, String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        onProgress(0.1f, "در حال آماده‌سازی نسخه پشتیبان...")

        val root = JSONObject()
        root.put("format", "yadin-backup")
        root.put("schemaVersion", 2)
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("backupType", type.name)

        val data = JSONObject()

        if (type == BackupType.VOCABULARY || type == BackupType.FULL) {
            onProgress(0.25f, "استخراج واژگان و دسته‌بندی‌ها...")
            val categories = conceptDao.getAllCategories()
            val catArray = JSONArray()
            categories.forEach { cat ->
                val c = JSONObject()
                c.put("id", cat.id)
                c.put("name", cat.name)
                c.put("sortOrder", cat.sortOrder)
                c.put("isDefault", cat.isDefault)
                catArray.put(c)
            }
            data.put("categories", catArray)

            // Concepts and contents
            val conceptsArray = JSONArray()
            val contentsArray = JSONArray()
            val allConcepts = conceptDao.searchConcepts("", 50000)
            allConcepts.forEach { c ->
                val co = JSONObject()
                co.put("id", c.id)
                co.put("entryType", c.entryType.name)
                co.put("categoryId", c.categoryId)
                co.put("favorite", c.favorite)
                co.put("active", c.active)
                co.put("createdAt", c.createdAt)
                co.put("updatedAt", c.updatedAt)
                conceptsArray.put(co)

                val contents = conceptDao.getContentsForConcept(c.id)
                contents.forEach { ct ->
                    val cto = JSONObject()
                    cto.put("id", ct.id)
                    cto.put("conceptId", ct.conceptId)
                    cto.put("languageCode", ct.languageCode)
                    cto.put("text", ct.text)
                    cto.put("canonicalKey", ct.canonicalKey)
                    cto.put("note", ct.note)
                    cto.put("pronunciation", ct.pronunciation)
                    cto.put("translationIndex", ct.translationIndex)
                    contentsArray.put(cto)
                }
            }
            data.put("concepts", conceptsArray)
            data.put("contents", contentsArray)
        }

        if (type == BackupType.PROGRESS || type == BackupType.FULL) {
            onProgress(0.55f, "استخراج مراحل یادگیری لایتنر و دشواری...")
            val learningStates = learningDao.getAllLearningStates()
            val lsArray = JSONArray()
            learningStates.forEach { ls ->
                val o = JSONObject()
                o.put("id", ls.id)
                o.put("conceptId", ls.conceptId)
                o.put("direction", ls.direction.name)
                o.put("stage", ls.stage.name)
                o.put("nextReviewDay", ls.nextReviewDay)
                o.put("lastReviewedDay", ls.lastReviewedDay)
                o.put("updatedAt", ls.updatedAt)
                lsArray.put(o)
            }
            data.put("learningStates", lsArray)

            val diffStates = learningDao.getAllDifficultyStates()
            val dsArray = JSONArray()
            diffStates.forEach { ds ->
                val o = JSONObject()
                o.put("id", ds.id)
                o.put("conceptId", ds.conceptId)
                o.put("direction", ds.direction.name)
                o.put("current", ds.current.name)
                o.put("consecutiveCorrect", ds.consecutiveCorrect)
                o.put("consecutiveWrong", ds.consecutiveWrong)
                o.put("hasReachedVeryHard", ds.hasReachedVeryHard)
                dsArray.put(o)
            }
            data.put("difficultyStates", dsArray)

            onProgress(0.75f, "استخراج تاریخچه جلسات و دستاوردها...")
            val history = reviewSessionDao.getAllHistory()
            val histArray = JSONArray()
            history.forEach { h ->
                val o = JSONObject()
                o.put("id", h.id)
                o.put("sessionId", h.sessionId)
                o.put("conceptId", h.conceptId)
                o.put("direction", h.direction.name)
                o.put("reviewedAt", h.reviewedAt)
                o.put("reviewedDay", h.reviewedDay)
                o.put("isCorrect", h.isCorrect)
                o.put("reviewType", h.reviewType.name)
                o.put("mode", h.mode.name)
                o.put("stageBefore", h.stageBefore.name)
                o.put("quizLevel", h.quizLevel?.name)
                o.put("selectedIndex", h.selectedIndex)
                o.put("correctIndex", h.correctIndex)
                histArray.put(o)
            }
            data.put("reviewHistory", histArray)

            val sessions = reviewSessionDao.getAllSessions()
            val sessArray = JSONArray()
            sessions.forEach { s ->
                val o = JSONObject()
                o.put("id", s.id)
                o.put("startedAt", s.startedAt)
                o.put("endedAt", s.endedAt)
                o.put("reviewType", s.reviewType.name)
                o.put("mode", s.mode.name)
                o.put("direction", s.direction.name)
                o.put("quizLevel", s.quizLevel?.name)
                o.put("status", s.status.name)
                o.put("currentPosition", s.currentPosition)
                o.put("totalItems", s.totalItems)
                sessArray.put(o)
            }
            data.put("reviewSessions", sessArray)

            val sessionItems = reviewSessionDao.getAllSessionItems()
            val sessItemsArray = JSONArray()
            sessionItems.forEach { si ->
                val o = JSONObject()
                o.put("id", si.id)
                o.put("sessionId", si.sessionId)
                o.put("conceptId", si.conceptId)
                o.put("direction", si.direction.name)
                o.put("position", si.position)
                o.put("state", si.state.name)
                sessItemsArray.put(o)
            }
            data.put("reviewSessionItems", sessItemsArray)

            val achievements = achievementDao.getAllAchievements()
            val achArray = JSONArray()
            achievements.forEach { a ->
                val o = JSONObject()
                o.put("id", a.id)
                o.put("unlockedAt", a.unlockedAt)
                o.put("progress", a.progress)
                achArray.put(o)
            }
            data.put("achievements", achArray)

            val settings = settingsDao.getAllSettings()
            val setArray = JSONArray()
            settings.forEach { s ->
                val so = JSONObject()
                so.put("key", s.key)
                so.put("value", s.value)
                setArray.put(so)
            }
            data.put("settings", setArray)
        }

        root.put("data", data)
        onProgress(1.0f, "تهیه پشتیبان با موفقیت انجام شد")

        root.toString(2)
    }

    suspend fun saveBackupToFile(jsonString: String, filename: String): File = withContext(Dispatchers.IO) {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists()) backupsDir.mkdirs()
        val file = File(backupsDir, filename)
        file.writeText(jsonString, Charsets.UTF_8)
        file
    }

    suspend fun restoreFromJson(
        jsonString: String,
        isReplace: Boolean = false,
        onProgress: (Float, String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.1f, "در حال اعتبارسنجی فایل پشتیبان...")
            val root = JSONObject(jsonString)

            val format = root.optString("format", "")
            val isFlashLearn = root.has("payloads")
            if (format != "yadin-backup" && !isFlashLearn) {
                return@withContext Result.failure(IllegalArgumentException("قالب فایل پشتیبان نامعتبر است"))
            }

            val data = root.optJSONObject("data") ?: root.optJSONObject("payloads")?.optJSONObject("VOCABULARY")
            if (data == null) {
                return@withContext Result.failure(IllegalArgumentException("اطلاعات پشتیبان خالی یا مخدوش است"))
            }

            val restoreContext = RestoreContext()

            // Execute all restore database operations inside a single transaction!
            database.withTransaction {
                if (isReplace) {
                    onProgress(0.15f, "پاک‌سازی داده‌های قبلی برای جایگزینی...")
                    val hasVocab = data.has("concepts") || isFlashLearn
                    if (hasVocab) {
                        conceptDao.clearContents()
                        conceptDao.clearConcepts()
                        conceptDao.clearCustomCategories()
                    }
                    learningDao.clearLearningStates()
                    learningDao.clearDifficultyStates()
                    reviewSessionDao.clearHistory()
                    reviewSessionDao.clearSessions()
                    reviewSessionDao.clearSessionItems()
                }

                // 1. Categories
                val categoriesJson = data.optJSONArray("categories")
                if (categoriesJson != null) {
                    onProgress(0.25f, "بازیابی دسته‌بندی‌ها...")
                    val cats = mutableListOf<CategoryEntity>()
                    for (i in 0 until categoriesJson.length()) {
                        val co = categoriesJson.getJSONObject(i)
                        cats.add(
                            CategoryEntity(
                                id = co.getString("id"),
                                name = co.getString("name"),
                                sortOrder = co.optInt("sortOrder", 0),
                                isDefault = co.optBoolean("isDefault", false)
                            )
                        )
                    }
                    conceptDao.insertCategories(cats)
                }

                // 2. Concepts & Contents
                val conceptsJson = data.optJSONArray("concepts")
                val contentsJson = data.optJSONArray("contents")

                if (conceptsJson != null && contentsJson != null) {
                    onProgress(0.40f, "بازیابی ساختار واژگان...")
                    val concepts = mutableListOf<ConceptEntity>()
                    for (i in 0 until conceptsJson.length()) {
                        val co = conceptsJson.getJSONObject(i)
                        val cid = co.getString("id")
                        val entryTypeStr = co.optString("entryType", "WORD")
                        val entryType = runCatching { EntryType.valueOf(entryTypeStr) }.getOrDefault(EntryType.WORD)
                        concepts.add(
                            ConceptEntity(
                                id = cid,
                                entryType = entryType,
                                categoryId = co.optString("categoryId", "").ifEmpty { null },
                                favorite = co.optBoolean("favorite", false),
                                active = co.optBoolean("active", true),
                                createdAt = co.optLong("createdAt", System.currentTimeMillis()),
                                updatedAt = co.optLong("updatedAt", System.currentTimeMillis())
                            )
                        )
                    }
                    conceptDao.insertConcepts(concepts)

                    onProgress(0.55f, "بازیابی معانی و ترجمه‌ها...")
                    val contents = mutableListOf<ContentEntity>()
                    for (i in 0 until contentsJson.length()) {
                        val cto = contentsJson.getJSONObject(i)
                        contents.add(
                            ContentEntity(
                                id = cto.optString("id", UUID.randomUUID().toString()),
                                conceptId = restoreContext.remapConceptId(cto.getString("conceptId")),
                                languageCode = cto.optString("languageCode", "es"),
                                text = cto.getString("text"),
                                canonicalKey = cto.optString("canonicalKey", cto.getString("text").lowercase()),
                                note = cto.optString("note", "").ifEmpty { cto.optString("notes", "").ifEmpty { null } },
                                pronunciation = cto.optString("pronunciation", "").ifEmpty { null },
                                translationIndex = cto.optInt("translationIndex", 0)
                            )
                        )
                    }
                    conceptDao.insertContents(contents)
                }

                // 3. Learning States
                val lsJson = data.optJSONArray("learningStates")
                if (lsJson != null) {
                    onProgress(0.70f, "بازیابی مراحل لایتنر واژگان...")
                    val states = mutableListOf<LearningStateEntity>()
                    for (i in 0 until lsJson.length()) {
                        val o = lsJson.getJSONObject(i)
                        val dirStr = o.optString("direction", "NORMAL")
                        val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                        val stageStr = o.optString("stage", "DAILY")
                        val stage = runCatching { Stage.valueOf(stageStr) }.getOrDefault(Stage.DAILY)
                        states.add(
                            LearningStateEntity(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                conceptId = restoreContext.remapConceptId(o.getString("conceptId")),
                                direction = dir,
                                stage = stage,
                                nextReviewDay = o.optString("nextReviewDay", "").ifEmpty { null },
                                lastReviewedDay = o.optString("lastReviewedDay", "").ifEmpty { null },
                                updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                            )
                        )
                    }
                    learningDao.insertLearningStates(states)
                }

                // 4. Difficulty States
                val dsJson = data.optJSONArray("difficultyStates")
                if (dsJson != null) {
                    onProgress(0.80f, "بازیابی سطوح دشواری...")
                    val states = mutableListOf<DifficultyStateEntity>()
                    for (i in 0 until dsJson.length()) {
                        val o = dsJson.getJSONObject(i)
                        val dirStr = o.optString("direction", "NORMAL")
                        val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                        val curStr = o.optString("current", "MEDIUM")
                        val current = runCatching { VocabularyDifficulty.valueOf(curStr) }.getOrDefault(VocabularyDifficulty.MEDIUM)
                        states.add(
                            DifficultyStateEntity(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                conceptId = restoreContext.remapConceptId(o.getString("conceptId")),
                                direction = dir,
                                current = current,
                                consecutiveCorrect = o.optInt("consecutiveCorrect", 0),
                                consecutiveWrong = o.optInt("consecutiveWrong", 0),
                                hasReachedVeryHard = o.optBoolean("hasReachedVeryHard", false)
                            )
                        )
                    }
                    learningDao.insertDifficultyStates(states)
                }

                // 5. Review History
                val histJson = data.optJSONArray("reviewHistory")
                if (histJson != null) {
                    onProgress(0.88f, "بازیابی تاریخچه مرور...")
                    val history = mutableListOf<ReviewHistoryEntity>()
                    for (i in 0 until histJson.length()) {
                        val o = histJson.getJSONObject(i)
                        val dirStr = o.optString("direction", "NORMAL")
                        val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                        val rtStr = o.optString("reviewType", "DAILY")
                        val rt = runCatching { ReviewType.valueOf(rtStr) }.getOrDefault(ReviewType.DAILY)
                        val mStr = o.optString("mode", "FLASHCARD")
                        val m = runCatching { ReviewMode.valueOf(mStr) }.getOrDefault(ReviewMode.FLASHCARD)
                        val sbStr = o.optString("stageBefore", "DAILY")
                        val sb = runCatching { Stage.valueOf(sbStr) }.getOrDefault(Stage.DAILY)
                        val qlStr = o.optString("quizLevel", "")
                        val ql = if (qlStr.isNotEmpty()) runCatching { QuizLevel.valueOf(qlStr) }.getOrNull() else null

                        history.add(
                            ReviewHistoryEntity(
                                id = o.optString("id", UUID.randomUUID().toString()),
                                sessionId = o.optString("sessionId", UUID.randomUUID().toString()),
                                reviewAttemptId = UUID.randomUUID().toString(),
                                conceptId = restoreContext.remapConceptId(o.getString("conceptId")),
                                direction = dir,
                                reviewedAt = o.optLong("reviewedAt", System.currentTimeMillis()),
                                reviewedDay = o.optString("reviewedDay", "").ifEmpty { 
                                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(o.optLong("reviewedAt", System.currentTimeMillis()))) 
                                },
                                isCorrect = o.optBoolean("isCorrect", true),
                                reviewType = rt,
                                mode = m,
                                stageBefore = sb,
                                quizLevel = ql,
                                optionsJson = null,
                                selectedIndex = if (o.has("selectedIndex")) o.getInt("selectedIndex") else null,
                                correctIndex = if (o.has("correctIndex")) o.getInt("correctIndex") else null
                            )
                        )
                    }
                    reviewSessionDao.insertHistoryItems(history)
                }

                // 6. Review Sessions
                val sessJson = data.optJSONArray("reviewSessions")
                if (sessJson != null) {
                    val sessions = mutableListOf<ReviewSessionEntity>()
                    for (i in 0 until sessJson.length()) {
                        val o = sessJson.getJSONObject(i)
                        val rtStr = o.optString("reviewType", "DAILY")
                        val rt = runCatching { ReviewType.valueOf(rtStr) }.getOrDefault(ReviewType.DAILY)
                        val mStr = o.optString("mode", "FLASHCARD")
                        val m = runCatching { ReviewMode.valueOf(mStr) }.getOrDefault(ReviewMode.FLASHCARD)
                        val dirStr = o.optString("direction", "NORMAL")
                        val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                        val stStr = o.optString("status", "COMPLETED")
                        val st = runCatching { SessionStatus.valueOf(stStr) }.getOrDefault(SessionStatus.COMPLETED)
                        val qlStr = o.optString("quizLevel", "")
                        val ql = if (qlStr.isNotEmpty()) runCatching { QuizLevel.valueOf(qlStr) }.getOrNull() else null

                        sessions.add(
                            ReviewSessionEntity(
                                id = o.getString("id"),
                                startedAt = o.optLong("startedAt", System.currentTimeMillis()),
                                endedAt = if (o.has("endedAt")) o.optLong("endedAt") else null,
                                reviewType = rt,
                                mode = m,
                                direction = dir,
                                quizLevel = ql,
                                status = st,
                                currentPosition = o.optInt("currentPosition", o.optInt("totalItems", 0)),
                                totalItems = o.optInt("totalItems", 0)
                            )
                        )
                    }
                    reviewSessionDao.insertSessions(sessions)

                    val itemsJson = data.optJSONArray("reviewSessionItems")
                    if (itemsJson != null) {
                        val sessionItems = mutableListOf<ReviewSessionItemEntity>()
                        for (i in 0 until itemsJson.length()) {
                            val o = itemsJson.getJSONObject(i)
                            val dirStr = o.optString("direction", "NORMAL")
                            val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                            val stStr = o.optString("state", "PENDING")
                            val state = runCatching { SessionItemState.valueOf(stStr) }.getOrDefault(SessionItemState.PENDING)
                            sessionItems.add(
                                ReviewSessionItemEntity(
                                    id = o.getString("id"),
                                    sessionId = o.getString("sessionId"),
                                    conceptId = restoreContext.remapConceptId(o.getString("conceptId")),
                                    direction = dir,
                                    position = o.optInt("position", i),
                                    state = state
                                )
                            )
                        }
                        reviewSessionDao.insertSessionItems(sessionItems)
                    }
                }

                // 7. Achievements
                val achJson = data.optJSONArray("achievements")
                if (achJson != null) {
                    for (i in 0 until achJson.length()) {
                        val o = achJson.getJSONObject(i)
                        val id = o.getString("id")
                        val unlockedAt = if (o.has("unlockedAt") && !o.isNull("unlockedAt")) o.getLong("unlockedAt") else null
                        val progress = o.optInt("progress", 0)
                        if (unlockedAt != null) {
                            achievementDao.unlock(id, unlockedAt)
                        }
                        if (progress > 0) {
                            achievementDao.updateProgress(id, progress)
                        }
                    }
                }

                // 8. Settings
                val setJson = data.optJSONArray("settings")
                if (setJson != null) {
                    for (i in 0 until setJson.length()) {
                        val o = setJson.getJSONObject(i)
                        settingsDao.setSetting(SettingEntity(o.getString("key"), o.getString("value")))
                    }
                }
            }

            onProgress(1.0f, "بازیابی با موفقیت انجام شد")
            Result.success("تمام داده‌ها با موفقیت بازیابی شدند")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
