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

        if (options.includeProcessHistory || options.includeStreakAndProgress) {
            onProgress(0.75f, "استخراج تاریخچه جلسات، آزمون‌ها و پروسه پیشرفت...")
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

        if (options.includeSettings) {
            onProgress(0.9f, "استخراج تنظیمات و دستاوردها...")
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
        val learningMap = learningDao.getAllLearningStates().associateBy { it.conceptId }
        val diffMap = learningDao.getAllDifficultyStates().associateBy { it.conceptId }

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
        if (options.includeReviewStats) headers.add("مرحله لایتنر")
        if (options.includeDifficulty) headers.add("سطح دشواری")
        headers.add("توضیحات و یادداشت")
        headers.add("شناسه واژه")

        sb.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

        allConcepts.forEachIndexed { index, concept ->
            val contents = allContentsByConcept[concept.id] ?: emptyList()
            val esText = contents.firstOrNull { it.languageCode == "es" }?.text ?: ""
            val faTranslations = contents.filter { it.languageCode == "fa" }.map { it.text }.joinToString(" ، ")
            val catName = categories[concept.categoryId]?.name ?: "عمومی"
            val stage = learningMap[concept.id]?.stage?.let {
                when (it) {
                    Stage.DAILY -> "روزانه (Daily)"
                    Stage.WEEKLY -> "هفتگی (Weekly)"
                    Stage.MONTHLY -> "ماهانه (Monthly)"
                    Stage.LEARNED -> "تثبیت‌شده (Learned)"
                }
            } ?: "آماده یادگیری"
            val difficulty = diffMap[concept.id]?.current?.let {
                when (it) {
                    VocabularyDifficulty.EASY -> "آسان"
                    VocabularyDifficulty.MEDIUM -> "متوسط"
                    VocabularyDifficulty.HARD -> "سخت"
                    VocabularyDifficulty.VERY_HARD -> "خیلی سخت"
                }
            } ?: "متوسط"
            val note = contents.firstOrNull { it.languageCode == "es" }?.note ?: ""

            val row = mutableListOf<String>()
            row.add((index + 1).toString())
            row.add(esText)
            row.add(faTranslations)
            if (options.includeCategories) row.add(catName)
            if (options.includeReviewStats) row.add(stage)
            if (options.includeDifficulty) row.add(difficulty)
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

        val learnedCount = learningStates.count { it.stage == Stage.LEARNED }
        val monthlyCount = learningStates.count { it.stage == Stage.MONTHLY }
        val weeklyCount = learningStates.count { it.stage == Stage.WEEKLY }
        val dailyCount = learningStates.count { it.stage == Stage.DAILY }

        val easyCount = difficultyStates.count { it.current == VocabularyDifficulty.EASY }
        val mediumCount = difficultyStates.count { it.current == VocabularyDifficulty.MEDIUM }
        val hardCount = difficultyStates.count { it.current == VocabularyDifficulty.HARD }
        val veryHardCount = difficultyStates.count { it.current == VocabularyDifficulty.VERY_HARD }

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
        sb.append(listOf("واژگان تثبیت‌شده (حافظه بلندمدت)", "$learnedCount واژه", "مرحله چهارم جعبه لایتنر (تسلط کامل)").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان مرحله ماهانه", "$monthlyCount واژه", "مرور هر ۳۰ روز یک‌بار").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان مرحله هفتگی", "$weeklyCount واژه", "مرور هر ۷ روز یک‌بار").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان مرحله روزانه", "$dailyCount واژه", "مرورهای جاری و روزانه").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح آسان", "$easyCount واژه", "EASY").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح متوسط", "$mediumCount واژه", "MEDIUM").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح سخت", "$hardCount واژه", "HARD").joinToString(",") { escapeCsv(it) }).append("\n")
        sb.append(listOf("واژگان سطح خیلی سخت", "$veryHardCount واژه", "VERY_HARD").joinToString(",") { escapeCsv(it) }).append("\n")
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

    suspend fun saveBackupToFile(jsonString: String, filename: String): File = withContext(Dispatchers.IO) {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists()) backupsDir.mkdirs()
        val file = File(backupsDir, filename)
        file.writeText(jsonString, Charsets.UTF_8)
        file
    }

    private fun parseIsoDay(isoOrDay: String?): String? {
        if (isoOrDay.isNullOrBlank()) return null
        if (isoOrDay.length >= 10 && isoOrDay.contains("-")) {
            return isoOrDay.take(10)
        }
        return null
    }

    suspend fun restoreFromJson(
        jsonString: String,
        isReplace: Boolean = false,
        onProgress: (Float, String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            onProgress(0.05f, "در حال پردازش و استخراج محتوای پشتیبان...")
            val root = JSONObject(jsonString)

            val format = root.optString("format", "")
            val isFlashLearn = root.has("payloads")
            if (format != "yadin-backup" && !isFlashLearn) {
                return@withContext Result.failure(IllegalArgumentException("قالب فایل پشتیبان نامعتبر است"))
            }

            // Consolidate data from both Yadin format ("data") and FlashLearn bundle format ("payloads")
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

            if (data.length() == 0) {
                return@withContext Result.failure(IllegalArgumentException("اطلاعات پشتیبان خالی یا مخدوش است"))
            }

            val restoreContext = RestoreContext()

            // Step 1: Parse ALL entities in memory BEFORE opening SQLite transaction
            // This prevents holding an exclusive write lock during long JSON iterations, eliminating UI freezes!
            
            // 1. Categories
            val categoriesJson = data.optJSONArray("categories")
            val categoriesList = mutableListOf<CategoryEntity>()
            if (categoriesJson != null) {
                onProgress(0.15f, "پردازش دسته‌بندی‌ها...")
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

            // 2. Concepts & Contents
            val conceptsJson = data.optJSONArray("concepts")
            val contentsJson = data.optJSONArray("contents")
            val conceptsList = mutableListOf<ConceptEntity>()
            val contentsList = mutableListOf<ContentEntity>()

            if (conceptsJson != null) {
                onProgress(0.25f, "پردازش ساختار واژگان...")
                for (i in 0 until conceptsJson.length()) {
                    val co = conceptsJson.getJSONObject(i)
                    val cid = co.getString("id")
                    val entryTypeStr = co.optString("entryType", "WORD")
                    val entryType = runCatching { EntryType.valueOf(entryTypeStr) }.getOrDefault(EntryType.WORD)
                    conceptsList.add(
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
                onProgress(0.35f, "پردازش معانی و ترجمه‌ها...")
                for (i in 0 until contentsJson.length()) {
                    val cto = contentsJson.getJSONObject(i)
                    contentsList.add(
                        ContentEntity(
                            id = cto.optString("id", UUID.randomUUID().toString()),
                            conceptId = restoreContext.remapConceptId(cto.getString("conceptId")),
                            languageCode = cto.optString("languageCode", "es"),
                            text = cto.getString("text"),
                            canonicalKey = cto.optString("canonicalKey", cto.getString("text").lowercase()),
                            note = cto.optString("note", "").ifEmpty { cto.optString("notes", "").ifEmpty { null } },
                            translationIndex = cto.optInt("translationIndex", 0)
                        )
                    )
                }
            }

            // 3. Learning States (Handles both FlashLearn nextReviewAt/lastReviewedAt and Yadin nextReviewDay/lastReviewedDay)
            val lsJson = data.optJSONArray("learningStates")
            val learningStatesList = mutableListOf<LearningStateEntity>()
            if (lsJson != null) {
                onProgress(0.50f, "پردازش مراحل لایتنر واژگان...")
                for (i in 0 until lsJson.length()) {
                    val o = lsJson.getJSONObject(i)
                    val dirStr = o.optString("direction", "NORMAL")
                    val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                    val stageStr = o.optString("stage", "DAILY")
                    val stage = runCatching { Stage.valueOf(stageStr) }.getOrDefault(Stage.DAILY)
                    
                    val nextDay = parseIsoDay(o.optString("nextReviewDay", "").ifEmpty { o.optString("nextReviewAt", "") })
                    val lastDay = parseIsoDay(o.optString("lastReviewedDay", "").ifEmpty { o.optString("lastReviewedAt", "") })

                    learningStatesList.add(
                        LearningStateEntity(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            conceptId = restoreContext.remapConceptId(o.getString("conceptId")),
                            direction = dir,
                            stage = stage,
                            nextReviewDay = nextDay,
                            lastReviewedDay = lastDay,
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // 4. Difficulty States
            val dsJson = data.optJSONArray("difficultyStates")
            val difficultyStatesList = mutableListOf<DifficultyStateEntity>()
            if (dsJson != null) {
                onProgress(0.65f, "پردازش سطوح دشواری...")
                for (i in 0 until dsJson.length()) {
                    val o = dsJson.getJSONObject(i)
                    val dirStr = o.optString("direction", "NORMAL")
                    val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                    val curStr = o.optString("current", "MEDIUM")
                    val current = runCatching { VocabularyDifficulty.valueOf(curStr) }.getOrDefault(VocabularyDifficulty.MEDIUM)
                    difficultyStatesList.add(
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
            }

            // 5. Review History
            val histJson = data.optJSONArray("reviewHistory")
            val historyList = mutableListOf<ReviewHistoryEntity>()
            if (histJson != null) {
                onProgress(0.75f, "پردازش تاریخچه مرور...")
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
                    val revDay = parseIsoDay(o.optString("reviewedDay", "")) ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(revAt))

                    historyList.add(
                        ReviewHistoryEntity(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            sessionId = o.optString("sessionId", UUID.randomUUID().toString()),
                            reviewAttemptId = UUID.randomUUID().toString(),
                            conceptId = restoreContext.remapConceptId(o.getString("conceptId")),
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

            // 6. Review Sessions
            val sessJson = data.optJSONArray("reviewSessions")
            val sessionsList = mutableListOf<ReviewSessionEntity>()
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

                    sessionsList.add(
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
            val sessionItemsList = mutableListOf<ReviewSessionItemEntity>()
            if (itemsJson != null) {
                for (i in 0 until itemsJson.length()) {
                    val o = itemsJson.getJSONObject(i)
                    val dirStr = o.optString("direction", "NORMAL")
                    val dir = runCatching { CardDirection.valueOf(dirStr) }.getOrDefault(CardDirection.NORMAL)
                    val stStr = o.optString("state", "PENDING")
                    val state = runCatching { SessionItemState.valueOf(stStr) }.getOrDefault(SessionItemState.PENDING)
                    sessionItemsList.add(
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
            }

            // 7. Achievements & Settings
            val achJson = data.optJSONArray("achievements")
            val achievementsList = mutableListOf<Pair<String, Pair<Long?, Int>>>()
            if (achJson != null) {
                for (i in 0 until achJson.length()) {
                    val o = achJson.getJSONObject(i)
                    val id = o.getString("id")
                    val unlockedAt = if (o.has("unlockedAt") && !o.isNull("unlockedAt")) o.getLong("unlockedAt") else null
                    val progress = o.optInt("progress", 0)
                    achievementsList.add(id to Pair(unlockedAt, progress))
                }
            }

            val setJson = data.optJSONArray("settings")
            val settingsList = mutableListOf<SettingEntity>()
            if (setJson != null) {
                for (i in 0 until setJson.length()) {
                    val o = setJson.getJSONObject(i)
                    settingsList.add(SettingEntity(o.getString("key"), o.getString("value")))
                }
            }

            // Step 2: High-speed Batch Chunked Database Inserts inside a single transaction
            onProgress(0.85f, "در حال بروزرسانی دیتابیس...")
            database.withTransaction {
                if (isReplace) {
                    val hasVocab = conceptsList.isNotEmpty()
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

                categoriesList.chunked(500).forEach { conceptDao.insertCategories(it) }
                conceptsList.chunked(500).forEach { conceptDao.insertConcepts(it) }
                contentsList.chunked(500).forEach { conceptDao.insertContents(it) }
                learningStatesList.chunked(500).forEach { learningDao.insertLearningStates(it) }
                difficultyStatesList.chunked(500).forEach { learningDao.insertDifficultyStates(it) }
                historyList.chunked(500).forEach { reviewSessionDao.insertHistoryItems(it) }
                sessionsList.chunked(500).forEach { reviewSessionDao.insertSessions(it) }
                sessionItemsList.chunked(500).forEach { reviewSessionDao.insertSessionItems(it) }
                
                achievementsList.forEach { (id, pair) ->
                    val (unlockedAt, progress) = pair
                    if (unlockedAt != null) achievementDao.unlock(id, unlockedAt)
                    if (progress > 0) achievementDao.updateProgress(id, progress)
                }

                settingsList.forEach { settingsDao.setSetting(it) }
            }

            onProgress(1.0f, "بازیابی داده‌ها با موفقیت انجام شد")
            Result.success("تمام داده‌ها با موفقیت بازیابی شدند")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
