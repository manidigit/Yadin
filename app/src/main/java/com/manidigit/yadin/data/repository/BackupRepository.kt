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
    FULL,
    VOCABULARY_EXCEL,
    PROGRESS_EXCEL,
    CUSTOM
}

data class BackupOptions(
    val fullBackup: Boolean = true,
    val includeVocabulary: Boolean = true,
    val includeCategories: Boolean = true,
    val includeDifficulty: Boolean = true,
    val includeStreakAndProgress: Boolean = true,
    val includeReviewStats: Boolean = true,
    val includeProcessHistory: Boolean = true,
    val includeSettings: Boolean = true
)

enum class BackupExportFormat {
    JSON,
    EXCEL_CSV_VOCABULARY,
    EXCEL_CSV_PROGRESS,
    EXCEL_CSV_COMPLETE
}

/**
 * ساختار نگاشت شناسه‌ها (RestoreContext) برای اتصال یکپارچه واژگان و پیشرفت.
 *
 * طبق الگوریتم ۶.۱۴ سند مشخصات یادین، در حالت ادغام (MERGE):
 * هنگامی که یک مفهوم در فایل پشتیبان با یک مفهوم محلی تطبیق داده می‌شود، شناسه نسخه محلی
 * جایگزین می‌گردد. تمام بخش‌های بازیابی پیشرفت (learning_states، difficulty_states، review_history)
 * شناسه‌ها را صرفاً از طریق RestoreContext بازخوانی می‌کنند تا هیچ رکوردی به شناسه نامعتبر متصل نشود.
 */
class RestoreContext {
    private val conceptIdMap = mutableMapOf<String, String>() // backupConceptId -> localConceptId

    /** بازنگاشت شناسه پشتیبان به شناسه معادل محلی */
    fun remapConceptId(backupId: String): String = conceptIdMap[backupId] ?: backupId

    /** ثبت نگاشت شناسه نسخه پشتیبان به شناسه نسخه محلی */
    fun registerMapping(backupId: String, localId: String) {
        conceptIdMap[backupId] = localId
    }

    /** بررسی وجود نگاشت صریح برای شناسه مورد نظر */
    fun hasMapping(backupId: String): Boolean = conceptIdMap.containsKey(backupId)

    /** تعداد نگاشت‌های ثبت‌شده */
    val mappingCount: Int get() = conceptIdMap.size

    /** استخراج کپی تمامی نگاشت‌ها جهت ارزیابی و تست */
    fun getAllMappings(): Map<String, String> = conceptIdMap.toMap()
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
        val options = when (type) {
            BackupType.VOCABULARY -> BackupOptions(
                fullBackup = false,
                includeVocabulary = true,
                includeCategories = true,
                includeDifficulty = false,
                includeStreakAndProgress = false,
                includeReviewStats = false,
                includeProcessHistory = false,
                includeSettings = false
            )
            BackupType.PROGRESS -> BackupOptions(
                fullBackup = false,
                includeVocabulary = false,
                includeCategories = false,
                includeDifficulty = true,
                includeStreakAndProgress = true,
                includeReviewStats = true,
                includeProcessHistory = true,
                includeSettings = true
            )
            else -> BackupOptions(fullBackup = true)
        }
        createBackupJsonWithOptions(options, onProgress)
    }

    suspend fun createBackupJsonWithOptions(
        options: BackupOptions,
        onProgress: (Float, String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        onProgress(0.1f, "در حال آماده‌سازی داده‌های پشتیبان...")

        val root = JSONObject()
        root.put("format", "yadin-backup")
        root.put("schemaVersion", 2)
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("backupType", if (options.fullBackup) "FULL" else "CUSTOM")

        val optsJson = JSONObject()
        optsJson.put("includeVocabulary", options.includeVocabulary)
        optsJson.put("includeCategories", options.includeCategories)
        optsJson.put("includeDifficulty", options.includeDifficulty)
        optsJson.put("includeStreakAndProgress", options.includeStreakAndProgress)
        optsJson.put("includeReviewStats", options.includeReviewStats)
        optsJson.put("includeProcessHistory", options.includeProcessHistory)
        optsJson.put("includeSettings", options.includeSettings)
        root.put("options", optsJson)

        val data = JSONObject()

        if (options.includeCategories || options.includeVocabulary) {
            onProgress(0.2f, "استخراج دسته‌بندی‌ها...")
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
        }

        if (options.includeVocabulary) {
            onProgress(0.35f, "استخراج واژگان و ترجمه‌ها...")
            val conceptsArray = JSONArray()
            val contentsArray = JSONArray()
            val allConcepts = conceptDao.getAllConcepts()
            val allContentsByConcept = conceptDao.getAllContents().groupBy { it.conceptId }
            allConcepts.forEach { c ->
                val co = JSONObject()
                co.put("id", c.id)
                co.put("entryType", c.entryType.name)
                co.put("categoryId", c.categoryId)
                co.put("active", c.active)
                co.put("createdAt", c.createdAt)
                co.put("updatedAt", c.updatedAt)
                conceptsArray.put(co)

                val contents = allContentsByConcept[c.id] ?: emptyList()
                contents.forEach { ct ->
                    val cto = JSONObject()
                    cto.put("id", ct.id)
                    cto.put("conceptId", ct.conceptId)
                    cto.put("languageCode", ct.languageCode)
                    cto.put("text", ct.text)
                    cto.put("canonicalKey", ct.canonicalKey)
                    cto.put("note", ct.note)
                    cto.put("translationIndex", ct.translationIndex)
                    contentsArray.put(cto)
                }
            }
            data.put("concepts", conceptsArray)
            data.put("contents", contentsArray)
        }

        if (options.includeReviewStats) {
            onProgress(0.5f, "استخراج مراحل جعبه لایتنر...")
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
        }

        if (options.includeDifficulty) {
            onProgress(0.6f, "استخراج وضعیت درجه سختی کلمات...")
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
        }

        if (options.includeProcessHistory) {
            onProgress(0.75f, "استخراج تاریخچه جلسات و آزمون‌ها...")
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
        }

        if (options.includeStreakAndProgress) {
            onProgress(0.85f, "استخراج نشان‌ها و دستاوردها...")
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
        }

        if (options.includeSettings) {
            onProgress(0.9f, "استخراج تنظیمات برنامه...")
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

    suspend fun createBackupString(
        type: BackupType = BackupType.FULL,
        onProgress: (Float, String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        when (type) {
            BackupType.VOCABULARY_EXCEL -> createVocabularyExcelCsv(onProgress = onProgress)
            BackupType.PROGRESS_EXCEL -> createProgressExcelCsv(onProgress = onProgress)
            else -> createBackupJson(type, onProgress)
        }
    }

    suspend fun createBackupByFormat(
        format: BackupExportFormat,
        options: BackupOptions,
        onProgress: (Float, String) -> Unit
    ): String = withContext(Dispatchers.IO) {
        when (format) {
            BackupExportFormat.JSON -> createBackupJsonWithOptions(options, onProgress)
            BackupExportFormat.EXCEL_CSV_VOCABULARY -> createVocabularyExcelCsv(options, onProgress)
            BackupExportFormat.EXCEL_CSV_PROGRESS -> createProgressExcelCsv(onProgress)
            BackupExportFormat.EXCEL_CSV_COMPLETE -> createCompleteExcelCsv(options, onProgress)
        }
    }

    suspend fun createVocabularyExcelCsv(
        options: BackupOptions = BackupOptions(),
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String = withContext(Dispatchers.IO) {
        onProgress(0.1f, "در حال بارگذاری واژگان و دسته‌بندی‌ها...")
        val categories = conceptDao.getAllCategories().associateBy { it.id }
        val allConcepts = conceptDao.getAllConcepts().filter { it.active }
        val allContentsByConcept = conceptDao.getAllContents().groupBy { it.conceptId }
        val learningMap = learningDao.getAllLearningStates().associateBy { it.conceptId to it.direction }
        val diffMap = learningDao.getAllDifficultyStates().associateBy { it.conceptId to it.direction }

        onProgress(0.5f, "در حال ساخت سطرهای اکسل...")
        val sb = StringBuilder()
        // Prepend UTF-8 BOM so Microsoft Excel seamlessly opens Persian & Spanish characters without corruption
        sb.append("\uFEFF")

        // Dynamic Header based on options
        val headers = mutableListOf<String>()
        headers.add("ردیف")
        headers.add("واژه یا عبارت (اسپانیایی)")
        headers.add("ترجمه‌های فارسی")
        if (options.includeCategories) headers.add("دسته‌بندی")
        if (options.includeReviewStats) {
            headers.add("مرحله عادی (اسپانیایی به فارسی)")
            headers.add("مرحله برعکس (فارسی به اسپانیایی)")
        }
        if (options.includeDifficulty) {
            headers.add("دشواری (عادی)")
            headers.add("دشواری (برعکس)")
        }
        headers.add("توضیحات و یادداشت")
        headers.add("شناسه واژه")

        sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

        allConcepts.forEachIndexed { index, concept ->
            val contents = allContentsByConcept[concept.id] ?: emptyList()
            val esText = contents.firstOrNull { it.languageCode == "es" }?.text ?: ""
            val faTranslations = contents.filter { it.languageCode == "fa" }.map { it.text }.joinToString(" ، ")
            val catName = categories[concept.categoryId]?.name ?: "عمومی"
            
            val stageNormal = learningMap[concept.id to CardDirection.NORMAL]?.stage?.let { formatStageName(it) } ?: "آماده یادگیری"
            val stageReverse = learningMap[concept.id to CardDirection.REVERSE]?.stage?.let { formatStageName(it) } ?: "آماده یادگیری"
            
            val diffNormal = diffMap[concept.id to CardDirection.NORMAL]?.current?.let { formatDiffName(it) } ?: "متوسط"
            val diffReverse = diffMap[concept.id to CardDirection.REVERSE]?.current?.let { formatDiffName(it) } ?: "متوسط"
            
            val note = contents.firstOrNull { it.languageCode == "es" }?.note ?: ""

            val row = mutableListOf<String>()
            row.add((index + 1).toString())
            row.add(esText)
            row.add(faTranslations)
            if (options.includeCategories) row.add(catName)
            if (options.includeReviewStats) {
                row.add(stageNormal)
                row.add(stageReverse)
            }
            if (options.includeDifficulty) {
                row.add(diffNormal)
                row.add(diffReverse)
            }
            row.add(note)
            row.add(concept.id)

            sb.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
        }

        onProgress(1f, "خروجی اکسل واژگان آماده شد.")
        sb.toString()
    }

    suspend fun createCompleteExcelCsv(
        options: BackupOptions = BackupOptions(),
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String = withContext(Dispatchers.IO) {
        onProgress(0.1f, "در حال آماده‌سازی خروجی جامع اکسل...")
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM

        sb.append(escapeCsv("=========================================")).append("\n")
        sb.append(escapeCsv("   خروجی جامع پایگاه داده و آمار یادین (MASTER EXPORT)   ")).append("\n")
        sb.append(escapeCsv("   تاریخ استخراج: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}   ")).append("\n")
        sb.append(escapeCsv("=========================================")).append("\n\n")

        if (options.includeVocabulary) {
            onProgress(0.3f, "افزودن بخش واژگان به اکسل...")
            val vocabCsv = createVocabularyExcelCsv(options) { _, _ -> }
            // Drop BOM from sub-csv if any
            val cleanVocab = vocabCsv.removePrefix("\uFEFF")
            sb.append(escapeCsv("=== بخش ۱: بانک واژگان و دسته‌بندی‌ها ===")).append("\n")
            sb.append(cleanVocab).append("\n\n")
        }

        if (options.includeStreakAndProgress || options.includeProcessHistory) {
            onProgress(0.6f, "افزودن گزارش پیشرفت و رگبار به اکسل...")
            val progressCsv = createProgressExcelCsv { _, _ -> }
            val cleanProgress = progressCsv.removePrefix("\uFEFF")
            sb.append(cleanProgress).append("\n\n")
        }

        if (options.includeSettings) {
            onProgress(0.85f, "افزودن تنظیمات و دستاوردها به اکسل...")
            sb.append(escapeCsv("=== بخش تنظیمات و دستاوردها ===")).append("\n")
            sb.append(listOf("کلید تنظیمات", "مقدار ذخیره‌شده").joinToString(",") { escapeCsv(it) }).append("\n")
            val settings = settingsDao.getAllSettings()
            settings.forEach { s ->
                sb.append(listOf(s.key, s.value).joinToString(",") { escapeCsv(it) }).append("\n")
            }
            sb.append("\n")
            
            sb.append(listOf("شناسه نشان/دستاورد", "میزان پیشرفت", "تاریخ آنلاک").joinToString(",") { escapeCsv(it) }).append("\n")
            val achs = achievementDao.getAllAchievements()
            achs.forEach { a ->
                sb.append(listOf(a.id, a.progress.toString(), a.unlockedAt?.toString() ?: "-").joinToString(",") { escapeCsv(it) }).append("\n")
            }
            sb.append("\n")
        }

        onProgress(1.0f, "خروجی جامع اکسل با موفقیت تولید شد.")
        sb.toString()
    }

    suspend fun createProgressExcelCsv(
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String = withContext(Dispatchers.IO) {
        onProgress(0.1f, "در حال محاسبه رگبار و آمار جلسات...")
        val today = ClockAndDayMath.todayDayString()
        val distinctDays = reviewSessionDao.getDistinctReviewedDays()
        val streak = ClockAndDayMath.calculateStreakDays(distinctDays, today)
        val dailyStats = reviewSessionDao.getRecentDailyStats(365)
        val allHistory = reviewSessionDao.getAllHistory()
        val allSessions = reviewSessionDao.getAllSessions()
        val learningStates = learningDao.getAllLearningStates()
        val difficultyStates = learningDao.getAllDifficultyStates()

        val totalReviews = allHistory.size
        val correctReviews = allHistory.count { it.isCorrect }
        val wrongReviews = totalReviews - correctReviews
        val accuracyPct = if (totalReviews > 0) String.format(Locale.US, "%.1f", (correctReviews.toDouble() / totalReviews) * 100) else "0.0"

        val normalLearned = learningStates.count { it.direction == CardDirection.NORMAL && it.stage == Stage.LEARNED }
        val reverseLearned = learningStates.count { it.direction == CardDirection.REVERSE && it.stage == Stage.LEARNED }
        val normalMonthly = learningStates.count { it.direction == CardDirection.NORMAL && it.stage == Stage.MONTHLY }
        val reverseMonthly = learningStates.count { it.direction == CardDirection.REVERSE && it.stage == Stage.MONTHLY }
        val normalWeekly = learningStates.count { it.direction == CardDirection.NORMAL && it.stage == Stage.WEEKLY }
        val reverseWeekly = learningStates.count { it.direction == CardDirection.REVERSE && it.stage == Stage.WEEKLY }
        val normalDaily = learningStates.count { it.direction == CardDirection.NORMAL && it.stage == Stage.DAILY }
        val reverseDaily = learningStates.count { it.direction == CardDirection.REVERSE && it.stage == Stage.DAILY }

        val normalEasy = difficultyStates.count { it.direction == CardDirection.NORMAL && it.current == VocabularyDifficulty.EASY }
        val reverseEasy = difficultyStates.count { it.direction == CardDirection.REVERSE && it.current == VocabularyDifficulty.EASY }
        val normalMedium = difficultyStates.count { it.direction == CardDirection.NORMAL && it.current == VocabularyDifficulty.MEDIUM }
        val reverseMedium = difficultyStates.count { it.direction == CardDirection.REVERSE && it.current == VocabularyDifficulty.MEDIUM }
        val normalHard = difficultyStates.count { it.direction == CardDirection.NORMAL && it.current == VocabularyDifficulty.HARD }
        val reverseHard = difficultyStates.count { it.direction == CardDirection.REVERSE && it.current == VocabularyDifficulty.HARD }
        val normalVeryHard = difficultyStates.count { it.direction == CardDirection.NORMAL && it.current == VocabularyDifficulty.VERY_HARD }
        val reverseVeryHard = difficultyStates.count { it.direction == CardDirection.REVERSE && it.current == VocabularyDifficulty.VERY_HARD }

        val contentsMap = conceptDao.getAllContents().groupBy { it.conceptId }

        onProgress(0.5f, "در حال آماده‌سازی بخش‌های اکسل...")
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM

        // SECTION 1: خلاصه شاخص‌ها و رگبار متوالی
        sb.append(escapeCsv("=== گزارش پیشرفت و رگبار مطالعه یادین ===")).append("\n")
        sb.append(listOf("شاخص کلیدی", "مقدار", "توضیحات آماری").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("روزهای رگبار متوالی (Streak)", "$streak روز", "تداوم زنجیره مطالعه بدون وقفه روزانه").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("کل مرورهای انجام شده", "$totalReviews بار", "مجموع کل کارت‌ها و سؤالات پاسخ داده شده").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("پاسخ‌های صحیح", "$correctReviews بار", "تعداد پاسخ‌های درست در جلسات").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("پاسخ‌های نادرست", "$wrongReviews بار", "تعداد پاسخ‌های اشتباه در جلسات").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("درصد دقت پاسخ‌ها", "$accuracyPct%", "نسبت پاسخ‌های درست به کل مرورها").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("تعداد کل جلسات تمرین", "${allSessions.size} جلسه", "مجموع جلسات فلش‌کارت و کوییز برگزار شده").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان تثبیت‌شده (عادی: اسپانیایی ← فارسی)", "$normalLearned واژه", "مرحله چهارم جعبه لایتنر").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان تثبیت‌شده (برعکس: فارسی ← اسپانیایی)", "$reverseLearned واژه", "مرحله چهارم جعبه لایتنر").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان مرحله ماهانه (عادی / برعکس)", "$normalMonthly / $reverseMonthly واژه", "مرور هر ۳۰ روز یک‌بار").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان مرحله هفتگی (عادی / برعکس)", "$normalWeekly / $reverseWeekly واژه", "مرور هر ۷ روز یک‌بار").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان مرحله روزانه (عادی / برعکس)", "$normalDaily / $reverseDaily واژه", "مرورهای جاری و روزانه").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح آسان (عادی / برعکس)", "$normalEasy / $reverseEasy واژه", "EASY").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح متوسط (عادی / برعکس)", "$normalMedium / $reverseMedium واژه", "MEDIUM").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح سخت (عادی / برعکس)", "$normalHard / $reverseHard واژه", "HARD").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح خیلی سخت (عادی / برعکس)", "$normalVeryHard / $reverseVeryHard واژه", "VERY_HARD").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append("\n")

        // SECTION 2: آمار حجم مرور تمرین‌های روزانه
        sb.append(escapeCsv("=== آمار حجم مرور تمرین‌های روزانه ===")).append("\n")
        sb.append(listOf("ردیف", "تاریخ", "روز هفته", "حجم مرور (تعداد واژه)", "پاسخ درست", "پاسخ نادرست", "درصد موفقیت روزانه").joinToString(",") { escapeCsv(it) }).append("\n")
        dailyStats.forEachIndexed { idx, dayStat ->
            val dayOfWeek = try {
                val d = java.time.LocalDate.parse(dayStat.reviewedDay)
                when (d.dayOfWeek) {
                    java.time.DayOfWeek.SATURDAY -> "شنبه"
                    java.time.DayOfWeek.SUNDAY -> "یکشنبه"
                    java.time.DayOfWeek.MONDAY -> "دوشنبه"
                    java.time.DayOfWeek.TUESDAY -> "سه‌شنبه"
                    java.time.DayOfWeek.WEDNESDAY -> "چهارشنبه"
                    java.time.DayOfWeek.THURSDAY -> "پنج‌شنبه"
                    java.time.DayOfWeek.FRIDAY -> "جمعه"
                    else -> ""
                }
            } catch (_: Exception) { "" }
            val wrongCount = dayStat.totalCount - dayStat.correctCount
            val dayPct = if (dayStat.totalCount > 0) String.format(Locale.US, "%.1f", (dayStat.correctCount.toDouble() / dayStat.totalCount) * 100) else "0.0"
            sb.append(listOf(
                (idx + 1).toString(),
                dayStat.reviewedDay,
                dayOfWeek,
                dayStat.totalCount.toString(),
                dayStat.correctCount.toString(),
                wrongCount.toString(),
                "$dayPct%"
            ).joinToString(",") { escapeCsv(it) }).append("\n")
        }
        sb.append("\n")

        // SECTION 3: تاریخچه آخرین تمرین‌ها (Recent Item History - last 200 items)
        sb.append(escapeCsv("=== آخرین تمرین‌ها و پاسخ‌ها ===")).append("\n")
        sb.append(listOf("ردیف", "تاریخ و زمان", "واژه (اسپانیایی)", "جهت تمرین", "نوع تمرین", "نتیجه", "مرحله قبل").joinToString(",") { escapeCsv(it) }).append("\n")
        allHistory.takeLast(200).reversed().forEachIndexed { idx, h ->
            val wordText = contentsMap[h.conceptId]?.firstOrNull { it.languageCode == "es" }?.text ?: ""
            val dirText = if (h.direction == CardDirection.NORMAL) "اسپانیایی به فارسی" else "فارسی به اسپانیایی"
            val modeText = if (h.mode == ReviewMode.QUIZ) "آزمون ۴ گزینه‌ای" else "فلش‌کارت"
            val resultText = if (h.isCorrect) "صحیح" else "غلط"
            val stageText = when (h.stageBefore) {
                Stage.DAILY -> "روزانه"
                Stage.WEEKLY -> "هفتگی"
                Stage.MONTHLY -> "ماهانه"
                Stage.LEARNED -> "تثبیت‌شده"
            }
            val formattedDate = try {
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(h.reviewedAt))
            } catch (_: Exception) {
                h.reviewedAt.toString()
            }
            sb.append(listOf(
                (idx + 1).toString(),
                formattedDate,
                wordText,
                dirText,
                modeText,
                resultText,
                stageText
            ).joinToString(",") { escapeCsv(it) }).append("\n")
        }

        onProgress(1f, "خروجی اکسل پیشرفت و حجم مرورها آماده شد.")
        sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    companion object {
        const val MAX_SAFETY_BACKUPS = 3
        const val CURRENT_SUPPORTED_SCHEMA_VERSION = 2
    }

    suspend fun saveBackupToFile(jsonString: String, filename: String): File = withContext(Dispatchers.IO) {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists()) backupsDir.mkdirs()
        val file = File(backupsDir, filename)
        file.writeText(jsonString, Charsets.UTF_8)
        file
    }

    /**
     * ایجاد نسخه پشتیبان اضطراری ایمن (Safety Backup) قبل از آغاز هرگونه بازیابی داده‌ها.
     * طبق الزام بخش ۶.۱۴ و ISS-17 سند مشخصات، پیش از هرگونه دستکاری در دیتابیس باید
     * یک نسخه پشتیبان کامل اضطراری در پوشه `safety_backups` ذخیره شود و حداکثر ۳ نسخه اخیر نگهداری گردند.
     */
    suspend fun createSafetyBackup(): File = withContext(Dispatchers.IO) {
        val safetyDir = File(context.filesDir, "safety_backups")
        if (!safetyDir.exists()) safetyDir.mkdirs()

        // استخراج کلیه داده‌های فعلی بدون ایجاد وقفه در UI
        val backupJson = createBackupJson(BackupType.FULL) { _, _ -> }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val safetyFile = File(safetyDir, "safety_backup_${timestamp}_${System.currentTimeMillis()}.json")
        safetyFile.writeText(backupJson, Charsets.UTF_8)

        // سیاست چرخش فایل‌ها: نگهداری حداکثر ۳ فایل اخیر و حذف قدیمی‌ترها
        rotateSafetyBackups(safetyDir)
        safetyFile
    }

    /**
     * اعمال سیاست چرخش و محدودیت حداکثر ۳ نسخه پشتیبان اضطراری.
     */
    private fun rotateSafetyBackups(safetyDir: File) {
        try {
            val files = safetyDir.listFiles { f ->
                f.isFile && f.name.startsWith("safety_backup_") && f.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()

            if (files.size > MAX_SAFETY_BACKUPS) {
                files.drop(MAX_SAFETY_BACKUPS).forEach { oldFile ->
                    oldFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * دریافت لیست فایل‌های پشتیبان اضطراری موجود.
     */
    suspend fun getSafetyBackups(): List<File> = withContext(Dispatchers.IO) {
        val safetyDir = File(context.filesDir, "safety_backups")
        if (!safetyDir.exists()) return@withContext emptyList()
        safetyDir.listFiles { f ->
            f.isFile && f.name.startsWith("safety_backup_") && f.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }

    /**
     * دریافت لیست فایل‌های پشتیبان داخلی ذخیره‌شده در پوشه backups.
     */
    suspend fun getInternalBackups(): List<File> = withContext(Dispatchers.IO) {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists()) return@withContext emptyList()
        backupsDir.listFiles { f -> f.isFile }?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }

    private fun parseIsoDay(isoOrDay: String?): String? {
        if (isoOrDay.isNullOrBlank()) return null
        if (isoOrDay.length >= 10 && isoOrDay.contains("-")) {
            return isoOrDay.take(10)
        }
        return null
    }

    /**
     * بازیابی بانک اطلاعاتی از رشته JSON بر اساس الگوریتم ۶.۱۴ سند مشخصات و رفع نواقص ISS-17.
     *
     * گام‌های کلیدی:
     * ۰. تهیه خودکار فایل پشتیبان اضطراری ایمن (Safety Backup) با سیاست سقف ۳ نسخه.
     * ۱. اعتبارسنجی ساختار، نسخه (schemaVersion <= 2) و نوع داده‌های پشتیبان.
     * ۲. تجزیه حافظه‌ای کلیه رکوردهای دیتابیس پیش از باز کردن قفل تراکنش SQLite.
     * ۳. ساخت نگاشت RestoreContext برای تطبیق هوشمند شناسه‌های مفاهیم در حالت ادغام (MERGE).
     * ۴. عدم پاک‌سازی جداول پیشرفت در حالت REPLACE چنانچه فایل صرفاً حاوی واژگان باشد (ISS-17).
     * ۵. ادغام هوشمند پیشرفت، سطوح دشواری و حفظ بالاترین رکوردهای مرور بدون رونویسی ناصحیح.
     *
     * @param jsonString رشته محتوای JSON فایل پشتیبان
     * @param isReplace در صورت true جایگزینی کامل، در غیر این صورت ادغام هوشمند
     * @param onProgress گزارش درصد و وضعیت عملیات
     * @return پیام موفقیت یا خطای عملیات
     */
    suspend fun restoreFromJson(
        jsonString: String,
        isReplace: Boolean = false,
        onProgress: (Float, String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // گام ۰: تهیه فایل پشتیبان اضطراری ایمن (Safety Backup) پیش از اعمال هرگونه تغییر
            onProgress(0.02f, "ایجاد فایل پشتیبان اضطراری ایمن (Safety Backup)...")
            try {
                createSafetyBackup()
            } catch (e: Exception) {
                // ثبت لاگ هشدار؛ در صورت دسترسی به دیتابیس ادامه می‌دهیم
                android.util.Log.w("BackupRepository", "Safety backup creation warning: ${e.message}")
            }

            // گام ۱: اعتبارسنجی اولیه ساختار، نسخه و نوع
            onProgress(0.06f, "در حال بررسی و اعتبارسنجی ساختار فایل پشتیبان...")
            val root = JSONObject(jsonString)

            val format = root.optString("format", "")
            val isFlashLearn = root.has("payloads") || root.has("payload") ||
                root.optString("app", "").equals("FlashLearn", ignoreCase = true)

            if (format != "yadin-backup" && !isFlashLearn) {
                return@withContext Result.failure(IllegalArgumentException("فایل پشتیبان نامعتبر است (فرمت شناخته‌شده نیست)"))
            }

            val schemaVersion = root.optInt("schemaVersion", 1)
            if (format == "yadin-backup" && schemaVersion > CURRENT_SUPPORTED_SCHEMA_VERSION) {
                return@withContext Result.failure(
                    IllegalArgumentException("نسخه فایل پشتیبان ($schemaVersion) بالاتر از نسخه پشتیبانی‌شده برنامه ($CURRENT_SUPPORTED_SCHEMA_VERSION) است. لطفاً برنامه را بروزرسانی کنید.")
                )
            }

            // یکپارچه‌سازی محتوای داده از فرمت استاندارد یادین ("data") یا فرمت بسته‌ای فلش‌لرن ("payloads"/"payload")
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

            // پشتیبانی از آرایه تکی payload در فلش‌لرن
            if (data.length() == 0 && root.has("payload")) {
                val payloadArr = root.optJSONArray("payload")
                val pType = root.optString("type", "")
                if (payloadArr != null) {
                    if (pType.equals("PROGRESS", ignoreCase = true)) {
                        data.put("learningStates", payloadArr)
                    } else if (pType.equals("VOCABULARY", ignoreCase = true)) {
                        data.put("concepts", payloadArr)
                    }
                }
            }

            if (data.length() == 0) {
                return@withContext Result.failure(IllegalArgumentException("فایل پشتیبان نامعتبر است (داده‌ای یافت نشد)"))
            }

            // گام ۲: پارس کردن تمامی موجودیت‌ها در حافظه قبل از گشودن تراکنش SQLite
            
            // ۱. دسته‌بندی‌ها (Categories)
            val categoriesJson = data.optJSONArray("categories")
            val categoriesList = mutableListOf<CategoryEntity>()
            if (categoriesJson != null) {
                onProgress(0.12f, "پردازش دسته‌بندی‌ها...")
                for (i in 0 until categoriesJson.length()) {
                    val co = categoriesJson.getJSONObject(i)
                    categoriesList.add(
                        CategoryEntity(
                            id = co.getString("id"),
                            name = co.getString("name"),
                            sortOrder = co.optInt("sortOrder", 0),
                            isDefault = co.optBoolean("isDefault", false)
                        )
                    )
                }
            }

            // ۲. مفاهیم و محتواها (Concepts & Contents)
            val conceptsJson = data.optJSONArray("concepts")
            val contentsJson = data.optJSONArray("contents")
            val incomingConcepts = mutableListOf<ConceptEntity>()
            val incomingContents = mutableListOf<ContentEntity>()

            if (conceptsJson != null) {
                onProgress(0.20f, "پردازش مفاهیم و واژگان...")
                for (i in 0 until conceptsJson.length()) {
                    val co = conceptsJson.getJSONObject(i)
                    val cid = co.getString("id")
                    val entryTypeStr = co.optString("entryType", "WORD")
                    val entryType = runCatching { EntryType.valueOf(entryTypeStr) }.getOrDefault(EntryType.WORD)
                    incomingConcepts.add(
                        ConceptEntity(
                            id = cid,
                            entryType = entryType,
                            categoryId = co.optString("categoryId", "").ifEmpty { null },
                            active = co.optBoolean("active", true),
                            createdAt = co.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = co.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (contentsJson != null) {
                onProgress(0.30f, "پردازش ترجمه‌ها و معانی...")
                for (i in 0 until contentsJson.length()) {
                    val cto = contentsJson.getJSONObject(i)
                    incomingContents.add(
                        ContentEntity(
                            id = cto.optString("id", UUID.randomUUID().toString()),
                            conceptId = cto.getString("conceptId"),
                            languageCode = cto.optString("languageCode", "es"),
                            text = cto.getString("text"),
                            canonicalKey = cto.optString("canonicalKey", cto.getString("text").lowercase()),
                            note = cto.optString("note", "").ifEmpty { cto.optString("notes", "").ifEmpty { null } },
                            translationIndex = cto.optInt("translationIndex", 0)
                        )
                    )
                }
            }

            // ۳. مراحل لایتنر (Learning States)
            val lsJson = data.optJSONArray("learningStates")
            val incomingLearningStates = mutableListOf<LearningStateEntity>()
            if (lsJson != null) {
                onProgress(0.40f, "پردازش مراحل یادگیری لایتنر...")
                for (i in 0 until lsJson.length()) {
                    val o = lsJson.getJSONObject(i)
                    val dirStr = o.optString("direction", "NORMAL")
                    val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                    val stageStr = o.optString("stage", "DAILY")
                    val stage = runCatching { Stage.valueOf(stageStr) }.getOrDefault(Stage.DAILY)
                    val nextDay = parseIsoDay(o.optString("nextReviewDay", "").ifEmpty { o.optString("nextReviewAt", "") })
                    val lastDay = parseIsoDay(o.optString("lastReviewedDay", "").ifEmpty { o.optString("lastReviewedAt", "") })

                    incomingLearningStates.add(
                        LearningStateEntity(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            conceptId = o.getString("conceptId"),
                            direction = dir,
                            stage = stage,
                            nextReviewDay = nextDay,
                            lastReviewedDay = lastDay,
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // ۴. سطوح دشواری (Difficulty States)
            val dsJson = data.optJSONArray("difficultyStates")
            val incomingDifficultyStates = mutableListOf<DifficultyStateEntity>()
            if (dsJson != null) {
                onProgress(0.50f, "پردازش سطوح دشواری کارت‌ها...")
                for (i in 0 until dsJson.length()) {
                    val o = dsJson.getJSONObject(i)
                    val dirStr = o.optString("direction", "NORMAL")
                    val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                    val curStr = o.optString("current", "MEDIUM")
                    val current = runCatching { VocabularyDifficulty.valueOf(curStr) }.getOrDefault(VocabularyDifficulty.MEDIUM)
                    incomingDifficultyStates.add(
                        DifficultyStateEntity(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            conceptId = o.getString("conceptId"),
                            direction = dir,
                            current = current,
                            consecutiveCorrect = o.optInt("consecutiveCorrect", 0),
                            consecutiveWrong = o.optInt("consecutiveWrong", 0),
                            hasReachedVeryHard = o.optBoolean("hasReachedVeryHard", false)
                        )
                    )
                }
            }

            // ۵. تاریخچه مرورها (Review History)
            val histJson = data.optJSONArray("reviewHistory")
            val incomingHistoryList = mutableListOf<ReviewHistoryEntity>()
            if (histJson != null) {
                onProgress(0.60f, "پردازش تاریخچه مرورها و آزمون‌ها...")
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

                    val revAt = o.optLong("reviewedAt", System.currentTimeMillis())
                    val revDay = parseIsoDay(o.optString("reviewedDay", ""))
                        ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(revAt))

                    incomingHistoryList.add(
                        ReviewHistoryEntity(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            sessionId = o.optString("sessionId", UUID.randomUUID().toString()),
                            reviewAttemptId = o.optString("reviewAttemptId", UUID.randomUUID().toString()),
                            conceptId = o.getString("conceptId"),
                            direction = dir,
                            reviewedAt = revAt,
                            reviewedDay = revDay,
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
            }

            // ۶. جلسات مرور (Review Sessions & Items)
            val sessJson = data.optJSONArray("reviewSessions")
            val incomingSessionsList = mutableListOf<ReviewSessionEntity>()
            if (sessJson != null) {
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

                    incomingSessionsList.add(
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
            }

            val itemsJson = data.optJSONArray("reviewSessionItems")
            val incomingSessionItemsList = mutableListOf<ReviewSessionItemEntity>()
            if (itemsJson != null) {
                for (i in 0 until itemsJson.length()) {
                    val o = itemsJson.getJSONObject(i)
                    val dirStr = o.optString("direction", "NORMAL")
                    val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                    val stStr = o.optString("state", "PENDING")
                    val state = runCatching { SessionItemState.valueOf(stStr) }.getOrDefault(SessionItemState.PENDING)
                    incomingSessionItemsList.add(
                        ReviewSessionItemEntity(
                            id = o.getString("id"),
                            sessionId = o.getString("sessionId"),
                            conceptId = o.getString("conceptId"),
                            direction = dir,
                            position = o.optInt("position", i),
                            state = state
                        )
                    )
                }
            }

            // ۷. نشان‌ها و تنظیمات (Achievements & Settings)
            val achJson = data.optJSONArray("achievements")
            val incomingAchievementsList = mutableListOf<Pair<String, Pair<Long?, Int>>>()
            if (achJson != null) {
                for (i in 0 until achJson.length()) {
                    val o = achJson.getJSONObject(i)
                    val id = o.getString("id")
                    val unlockedAt = if (o.has("unlockedAt") && !o.isNull("unlockedAt")) o.getLong("unlockedAt") else null
                    val progress = o.optInt("progress", 0)
                    incomingAchievementsList.add(id to Pair(unlockedAt, progress))
                }
            }

            val setJson = data.optJSONArray("settings")
            val incomingSettingsList = mutableListOf<SettingEntity>()
            if (setJson != null) {
                for (i in 0 until setJson.length()) {
                    val o = setJson.getJSONObject(i)
                    incomingSettingsList.add(SettingEntity(o.getString("key"), o.getString("value")))
                }
            }

            // بررسی ماهیت محتوای فایل پشتیبان
            val hasVocabInData = incomingConcepts.isNotEmpty()
            val hasProgressInData = incomingLearningStates.isNotEmpty() ||
                incomingDifficultyStates.isNotEmpty() ||
                incomingHistoryList.isNotEmpty() ||
                incomingSessionsList.isNotEmpty()

            // گام ۳: ایجاد ساختار RestoreContext و انطباق شناسه‌ها طبق الگوریتم ۶.۱۴
            onProgress(0.70f, "تحلیل شناسه‌ها و اتصال RestoreContext...")
            val restoreContext = RestoreContext()

            // بازخوانی وضعیت فعلی پایگاه داده برای تطبیق
            val existingConcepts = conceptDao.getAllConcepts().associateBy { it.id }
            val existingContents = conceptDao.getAllContents()
            val existingContentsByConcept = existingContents.groupBy { it.conceptId }
            val existingContentsByKey = existingContents.groupBy { it.languageCode to it.canonicalKey }

            val incomingContentsByConcept = incomingContents.groupBy { it.conceptId }

            if (!isReplace) {
                // حالت MERGE: منطبق‌سازی مفاهیم بر اساس UUID یا کلید کانونیکال واژه مبدأ
                for (bConcept in incomingConcepts) {
                    if (existingConcepts.containsKey(bConcept.id)) {
                        // تطبیق با همان شناسه یکتا در دیتابیس
                        restoreContext.registerMapping(bConcept.id, bConcept.id)
                    } else {
                        // جستجو بر اساس کلمه مبدأ (اسپانیایی یا اولین محتوا)
                        val bContents = incomingContentsByConcept[bConcept.id] ?: emptyList()
                        val sourceContent = bContents.firstOrNull { it.languageCode == "es" } ?: bContents.firstOrNull()
                        val matchedLocalConceptId = if (sourceContent != null) {
                            val candidates = existingContentsByKey[sourceContent.languageCode to sourceContent.canonicalKey]
                            candidates?.mapNotNull { existingConcepts[it.conceptId] }
                                ?.sortedBy { it.createdAt }
                                ?.firstOrNull()?.id
                        } else null

                        if (matchedLocalConceptId != null) {
                            // مفهوم با شناسه محلی ادغام می‌شود
                            restoreContext.registerMapping(bConcept.id, matchedLocalConceptId)
                        } else {
                            // مفهوم تازه وارد شده با همان شناسه بکاپ درج می‌شود
                            restoreContext.registerMapping(bConcept.id, bConcept.id)
                        }
                    }
                }
            } else {
                // حالت REPLACE: تمامی شناسه‌های بکاپ مستقیماً استفاده می‌شوند
                for (bConcept in incomingConcepts) {
                    restoreContext.registerMapping(bConcept.id, bConcept.id)
                }
            }

            // آماده‌سازی لیست مفاهیم و محتواهای نهایی برای درج
            val finalConceptsToInsert = mutableListOf<ConceptEntity>()
            val finalContentsToInsert = mutableListOf<ContentEntity>()
            val reactivatedConceptIds = mutableListOf<String>()

            if (isReplace) {
                finalConceptsToInsert.addAll(incomingConcepts)
                for (ct in incomingContents) {
                    finalContentsToInsert.add(
                        ct.copy(conceptId = restoreContext.remapConceptId(ct.conceptId))
                    )
                }
            } else {
                for (bConcept in incomingConcepts) {
                    val localId = restoreContext.remapConceptId(bConcept.id)
                    val existing = existingConcepts[localId]
                    if (existing == null) {
                        // مفهوم کاملاً جدید
                        finalConceptsToInsert.add(bConcept)
                    } else {
                        // اگر مفهوم محلی غیرفعال بود و مفهوم پشتیبان فعال است، فعال شود
                        if (!existing.active && bConcept.active) {
                            reactivatedConceptIds.add(existing.id)
                        }
                    }

                    // الحاق ترجمه‌ها و محتواهای جدید بدون تکرار
                    val bContents = incomingContentsByConcept[bConcept.id] ?: emptyList()
                    val existingLocalContents = existingContentsByConcept[localId] ?: emptyList()
                    val existingKeys = existingLocalContents.map { it.languageCode to it.canonicalKey }.toSet()
                    var nextIndex = (existingLocalContents.maxOfOrNull { it.translationIndex } ?: -1) + 1

                    for (bc in bContents) {
                        val keyPair = bc.languageCode to bc.canonicalKey
                        if (!existingKeys.contains(keyPair)) {
                            finalContentsToInsert.add(
                                bc.copy(
                                    id = UUID.randomUUID().toString(),
                                    conceptId = localId,
                                    translationIndex = nextIndex++
                                )
                            )
                        }
                    }
                }
            }

            // آماده‌سازی پیشرفت (LearningStates & DifficultyStates) با اعمال کامل RestoreContext
            val existingLearningStates = learningDao.getAllLearningStates().associateBy { it.conceptId to it.direction }
            val existingDifficultyStates = learningDao.getAllDifficultyStates().associateBy { it.conceptId to it.direction }

            val finalLearningStatesToInsert = mutableListOf<LearningStateEntity>()
            val finalDifficultyStatesToInsert = mutableListOf<DifficultyStateEntity>()

            // شناسه‌های معتبر مفاهیم (موجود یا در حال اضافه شدن) جهت جلوگیری از درج رکوردهای بی‌سرپرست
            val validConceptIds = if (isReplace) {
                finalConceptsToInsert.map { it.id }.toSet()
            } else {
                existingConcepts.keys + finalConceptsToInsert.map { it.id }.toSet()
            }

            val bDiffMap = incomingDifficultyStates.associateBy { it.conceptId to it.direction }

            for (bLs in incomingLearningStates) {
                val mappedCid = restoreContext.remapConceptId(bLs.conceptId)
                if (!validConceptIds.contains(mappedCid)) {
                    // رکورد بدون مفهوم مرتبط رد می‌شود (طبق بخش ۶.۱۴ سند)
                    continue
                }

                val pairKey = mappedCid to bLs.direction
                val localLs = existingLearningStates[pairKey]
                val bDs = bDiffMap[bLs.conceptId to bLs.direction]

                if (isReplace) {
                    finalLearningStatesToInsert.add(bLs.copy(conceptId = mappedCid))
                    if (bDs != null) {
                        finalDifficultyStatesToInsert.add(bDs.copy(conceptId = mappedCid))
                    }
                } else {
                    // حالت MERGE طبق تصمیمات سند یادین:
                    // اگر کارت محلی هنوز مرور نشده (lastReviewedDay = null): ردیف پشتیبان جایگزین می‌شود
                    // اگر ردیف پشتیبان lastReviewedDay دارد و از محلی جدیدتر است: پشتیبان جایگزین می‌شود
                    val shouldUseBackup = when {
                        localLs == null -> true
                        localLs.lastReviewedDay == null -> true
                        bLs.lastReviewedDay != null && (localLs.lastReviewedDay == null || bLs.lastReviewedDay > localLs.lastReviewedDay) -> true
                        else -> false
                    }

                    if (shouldUseBackup) {
                        finalLearningStatesToInsert.add(bLs.copy(conceptId = mappedCid))
                        if (bDs != null) {
                            finalDifficultyStatesToInsert.add(bDs.copy(conceptId = mappedCid))
                        }
                    }
                }
            }

            // آماده‌سازی تاریخچه و جلسات مرور
            val existingHistory = reviewSessionDao.getAllHistory()
            val existingAttemptIds = existingHistory.map { it.reviewAttemptId }.toSet()
            val existingSessionIds = reviewSessionDao.getAllSessions().map { it.id }.toSet()

            val finalHistoryToInsert = mutableListOf<ReviewHistoryEntity>()
            val finalSessionsToInsert = mutableListOf<ReviewSessionEntity>()
            val finalSessionItemsToInsert = mutableListOf<ReviewSessionItemEntity>()

            for (h in incomingHistoryList) {
                val mappedCid = restoreContext.remapConceptId(h.conceptId)
                if (!validConceptIds.contains(mappedCid)) continue
                if (isReplace || !existingAttemptIds.contains(h.reviewAttemptId)) {
                    finalHistoryToInsert.add(h.copy(conceptId = mappedCid))
                }
            }

            for (s in incomingSessionsList) {
                if (isReplace || !existingSessionIds.contains(s.id)) {
                    finalSessionsToInsert.add(s)
                }
            }

            for (si in incomingSessionItemsList) {
                val mappedCid = restoreContext.remapConceptId(si.conceptId)
                if (!validConceptIds.contains(mappedCid)) continue
                finalSessionItemsToInsert.add(si.copy(conceptId = mappedCid))
            }

            // آماده‌سازی دستاوردها و تنظیمات
            val existingSettings = settingsDao.getAllSettings().associateBy { it.key }
            val finalSettingsToInsert = mutableListOf<SettingEntity>()

            for (st in incomingSettingsList) {
                // کلید seedImported هرگز در بازیابی بازنویسی نمی‌شود
                if (st.key == "seedImported") continue
                if (isReplace || !existingSettings.containsKey(st.key)) {
                    finalSettingsToInsert.add(st)
                }
            }

            // گام ۴: اعمال دسته‌ای و اتمیک تغییرات درون یک تراکنش واحد دیتابیس
            onProgress(0.85f, "در حال ثبت اتمیک اطلاعات در دیتابیس...")
            database.withTransaction {
                if (isReplace) {
                    // پاک‌سازی واژگان صرفاً در صورتی که فایل حاوی واژگان باشد
                    if (hasVocabInData) {
                        conceptDao.clearContents()
                        conceptDao.clearConcepts()
                        conceptDao.clearCustomCategories()
                    }
                    // رفع نقص بحرانی ISS-17: پاک‌سازی جداول پیشرفت صرفاً در صورتی مجاز است که
                    // فایل پشتیبان انتخاب‌شده واقعاً حاوی رکوردهای پیشرفت باشد!
                    if (hasProgressInData) {
                        learningDao.clearLearningStates()
                        learningDao.clearDifficultyStates()
                        reviewSessionDao.clearHistory()
                        reviewSessionDao.clearSessions()
                        reviewSessionDao.clearSessionItems()
                    }
                }

                // فعال‌سازی مجدد مفاهیم غیرفعال
                reactivatedConceptIds.forEach { conceptDao.reactivateConcept(it) }

                // درج واژگان و دسته‌ها به صورت دسته‌ای (Batch 500)
                categoriesList.chunked(500).forEach { conceptDao.insertCategories(it) }
                finalConceptsToInsert.chunked(500).forEach { conceptDao.insertConcepts(it) }
                finalContentsToInsert.chunked(500).forEach { conceptDao.insertContents(it) }

                // درج مراحل لایتنر و دشواری
                finalLearningStatesToInsert.chunked(500).forEach { learningDao.insertLearningStates(it) }
                finalDifficultyStatesToInsert.chunked(500).forEach { learningDao.insertDifficultyStates(it) }

                // درج تاریخچه و جلسات مرور
                finalSessionsToInsert.chunked(500).forEach { reviewSessionDao.insertSessions(it) }
                finalSessionItemsToInsert.chunked(500).forEach { reviewSessionDao.insertSessionItems(it) }
                finalHistoryToInsert.chunked(500).forEach { reviewSessionDao.insertHistoryItems(it) }

                // به‌روزرسانی دستاوردها به صورت اجتماع (Union)
                incomingAchievementsList.forEach { (id, pair) ->
                    val (unlockedAt, progress) = pair
                    if (unlockedAt != null) achievementDao.unlock(id, unlockedAt)
                    if (progress > 0) achievementDao.updateProgress(id, progress)
                }

                // ثبت تنظیمات
                finalSettingsToInsert.forEach { settingsDao.setSetting(it) }
            }

            onProgress(1.0f, "بازیابی داده‌ها با موفقیت کامل انجام شد")
            Result.success("اطلاعات پشتیبان با موفقیت بازیابی شدند")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun formatStageName(stage: Stage): String {
        return when (stage) {
            Stage.DAILY -> "روزانه (Daily)"
            Stage.WEEKLY -> "هفتگی (Weekly)"
            Stage.MONTHLY -> "ماهانه (Monthly)"
            Stage.LEARNED -> "تثبیت‌شده (Learned)"
        }
    }

    private fun formatDiffName(diff: VocabularyDifficulty): String {
        return when (diff) {
            VocabularyDifficulty.EASY -> "آسان"
            VocabularyDifficulty.MEDIUM -> "متوسط"
            VocabularyDifficulty.HARD -> "سخت"
            VocabularyDifficulty.VERY_HARD -> "خیلی سخت"
        }
    }
}
