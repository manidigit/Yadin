package com.manidigit.yadin.data.repository

import android.content.Context
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
import com.manidigit.yadin.data.local.entity.SettingEntity
import com.manidigit.yadin.domain.time.ClockAndDayMath
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
        onProgress(0.1f, "در حال استخراج اطلاعات...")

        val root = JSONObject()
        root.put("format", "yadin-backup")
        root.put("schemaVersion", 1)
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("backupType", type.name)

        val data = JSONObject()

        if (type == BackupType.VOCABULARY || type == BackupType.FULL) {
            onProgress(0.3f, "استخراج واژگان و دسته‌بندی‌ها...")
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
            val allConcepts = conceptDao.searchConcepts("", 10000)
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
            onProgress(0.7f, "استخراج وضعیت پیشرفت و جلسات...")
            // Settings
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
            if (format != "yadin-backup" && !root.has("payloads")) {
                return@withContext Result.failure(IllegalArgumentException("قالب فایل پشتیبان نامعتبر است"))
            }

            val data = root.optJSONObject("data") ?: root.optJSONObject("payloads")?.optJSONObject("VOCABULARY")
            if (data == null) {
                return@withContext Result.failure(IllegalArgumentException("اطلاعات پشتیبان خالی یا مخدوش است"))
            }

            val restoreContext = RestoreContext()

            // 1. Categories
            val categoriesJson = data.optJSONArray("categories")
            if (categoriesJson != null) {
                onProgress(0.3f, "بازیابی دسته‌بندی‌ها...")
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
                onProgress(0.5f, "بازیابی واژگان...")
                val concepts = mutableListOf<ConceptEntity>()
                for (i in 0 until conceptsJson.length()) {
                    val co = conceptsJson.getJSONObject(i)
                    val cid = co.getString("id")
                    concepts.add(
                        ConceptEntity(
                            id = cid,
                            categoryId = co.optString("categoryId", "").ifEmpty { null },
                            favorite = co.optBoolean("favorite", false),
                            active = co.optBoolean("active", true),
                            createdAt = co.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = co.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                conceptDao.insertConcepts(concepts)

                onProgress(0.7f, "بازیابی معانی و ترجمه‌ها...")
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

            onProgress(1.0f, "با موفقیت انجام شد")
            Result.success("بازیابی با موفقیت انجام شد")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
