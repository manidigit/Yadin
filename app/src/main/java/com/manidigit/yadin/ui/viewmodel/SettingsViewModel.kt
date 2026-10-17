package com.manidigit.yadin.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.manidigit.yadin.R
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.manidigit.yadin.data.repository.BackupExportFormat
import com.manidigit.yadin.data.repository.BackupOptions
import com.manidigit.yadin.data.repository.BackupRepository
import com.manidigit.yadin.data.repository.BackupType
import com.manidigit.yadin.data.repository.SettingsRepository
import com.manidigit.yadin.domain.model.CardDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * مدیریت تنظیمات برنامه، تم، جهت مطالعه و پشتیبان‌گیری/بازیابی - SettingsViewModel
 *
 * مسئولیت‌ها (اصل تک‌مسئولیتی - SRP):
 * - مدیریت اولویت‌ها و ترجیحات کاربر (تم، حالت شب، جهت یادگیری زبان، آستانه سختی).
 * - تهیه خروجی‌های پشتیبان JSON و اکسل (CSV) با گزارش پیشرفت زنده.
 * - بازیابی داده‌های پشتیبان با اعتبارسنجی و مدیریت کامل استثناها.
 *
 * @property application شیء Application اندروید جهت دسترسی به فایل‌ها و ContentResolver
 * @property settingsRepo مخزن تنظیمات و ترجیحات دیتابیس
 * @property backupRepo مخزن تولید و بازیابی پشتیبان
 * @property externalScope دامنه کورتین اختیاری
 */
class SettingsViewModel(
    application: Application,
    val settingsRepo: SettingsRepository,
    val backupRepo: BackupRepository,
    private val externalScope: CoroutineScope? = null
) : AndroidViewModel(application) {

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    // --- تنظیمات رابط کاربری و یادگیری ---
    val themeId: StateFlow<String> = settingsRepo.themeIdFlow
        .stateIn(scope, SharingStarted.Eagerly, "gemini")

    val isDark: StateFlow<Boolean> = settingsRepo.isDarkFlow
        .stateIn(scope, SharingStarted.Eagerly, true)

    val activePair: StateFlow<String> = settingsRepo.activePairFlow
        .stateIn(scope, SharingStarted.Eagerly, "es-fa")

    val uiLanguage: StateFlow<String> = settingsRepo.uiLanguageFlow
        .stateIn(scope, SharingStarted.Eagerly, "fa")

    val difficultyThreshold: StateFlow<Int> = settingsRepo.difficultyThresholdFlow
        .stateIn(scope, SharingStarted.Eagerly, 3)

    val showCategoryInReview: StateFlow<Boolean> = settingsRepo.showCategoryInReviewFlow
        .stateIn(scope, SharingStarted.Eagerly, false)

    val ttsEnabled: StateFlow<Boolean> = settingsRepo.ttsEnabledFlow
        .stateIn(scope, SharingStarted.Eagerly, true)

    val ttsAutoPlay: StateFlow<Boolean> = settingsRepo.ttsAutoPlayFlow
        .stateIn(scope, SharingStarted.Eagerly, false)

    val ttsSpeechRate: StateFlow<Float> = settingsRepo.ttsSpeechRateFlow
        .stateIn(scope, SharingStarted.Eagerly, 1.0f)

    val quizAutoAdvanceSeconds: StateFlow<Int> = settingsRepo.quizAutoAdvanceSecondsFlow
        .stateIn(scope, SharingStarted.Eagerly, 2)

    val defaultQuizLevel: StateFlow<com.manidigit.yadin.domain.model.QuizLevel> = settingsRepo.defaultQuizLevelFlow
        .stateIn(scope, SharingStarted.Eagerly, com.manidigit.yadin.domain.model.QuizLevel.NORMAL)

    val defaultReviewMode: StateFlow<com.manidigit.yadin.domain.model.ReviewMode> = settingsRepo.defaultReviewModeFlow
        .stateIn(scope, SharingStarted.Eagerly, com.manidigit.yadin.domain.model.ReviewMode.QUIZ)

    val appLanguageDirection: StateFlow<CardDirection> = settingsRepo.appLanguageDirectionFlow
        .stateIn(scope, SharingStarted.Eagerly, CardDirection.NORMAL)

    // --- وضعیت پشتیبان‌گیری و بازیابی ---
    private val _backupOptions = MutableStateFlow(BackupOptions())
    /** گزینه‌های انتخابی کاربر برای تهیه فایل پشتیبان */
    val backupOptions: StateFlow<BackupOptions> = _backupOptions.asStateFlow()

    private val _isBackupProcessing = MutableStateFlow(false)
    /** وضعیت در حال انجام عملیات پردازش فایل بکاپ */
    val isBackupProcessing: StateFlow<Boolean> = _isBackupProcessing.asStateFlow()

    private val _backupProgress = MutableStateFlow(0f)
    /** درصد پیشرفت عملیات پشتیبان‌گیری یا بازیابی (از ۰ تا ۱) */
    val backupProgress: StateFlow<Float> = _backupProgress.asStateFlow()

    private val _backupProgressMessage = MutableStateFlow("")
    /** پیام گام جاری عملیات پشتیبان‌گیری */
    val backupProgressMessage: StateFlow<String> = _backupProgressMessage.asStateFlow()

    private val _backupLastResult = MutableStateFlow<String?>(null)
    /** نتیجه آخرین عملیات پشتیبان‌گیری یا بازیابی */
    val backupLastResult: StateFlow<String?> = _backupLastResult.asStateFlow()

    private val _isBackupError = MutableStateFlow(false)
    /** آیا آخرین عملیات با خطا متوقف شده است */
    val isBackupError: StateFlow<Boolean> = _isBackupError.asStateFlow()

    private val _internalBackups = MutableStateFlow<List<File>>(emptyList())
    /** لیست فایل‌های پشتیبان و خروجی ذخیره‌شده در پوشه backups برنامه */
    val internalBackups: StateFlow<List<File>> = _internalBackups.asStateFlow()

    init {
        loadInternalBackups()
    }

    /** بارگذاری و بروزرسانی لیست فایل‌های پشتیبان داخلی */
    fun loadInternalBackups() {
        scope.launch {
            val list = backupRepo.getInternalBackups()
            _internalBackups.value = list
        }
    }

    /** حذف یک فایل پشتیبان داخلی */
    fun deleteInternalBackup(file: File) {
        scope.launch(Dispatchers.IO) {
            try {
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {}
            loadInternalBackups()
        }
    }

    /** اشتراک‌گذاری عمومی فایل خروجی/بکاپ با استفاده از FileProvider */
    fun shareBackupFile(file: File, context: Context) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = if (file.name.endsWith(".csv", ignoreCase = true)) "text/csv" else "application/json"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.backup_share_title)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            _backupLastResult.value = context.getString(R.string.backup_share_error, e.message ?: "")
            _isBackupError.value = true
        }
    }

    /**
     * تنظیم تم رنگی اختصاصی Gemini برنامه.
     */
    fun toggleTheme() {
        scope.launch {
            settingsRepo.setThemeId("gemini")
        }
    }

    /**
     * تعیین شناسه تم دلخواه.
     */
    fun setTheme(id: String) {
        scope.launch {
            settingsRepo.setThemeId(id)
        }
    }

    /**
     * سوئیچ وضعیت حالت شب/روز.
     */
    fun toggleDarkMode() {
        scope.launch {
            settingsRepo.setDarkMode(!isDark.value)
        }
    }

    /**
     * تنظیم مستقیم حالت شب/روز.
     */
    fun setDarkMode(dark: Boolean) {
        scope.launch {
            settingsRepo.setDarkMode(dark)
        }
    }

    /**
     * تعیین آستانه سختی کلمات (تعداد دفعات فراموشی پیش از برچسب سخت).
     */
    fun setDifficultyThreshold(threshold: Int) {
        scope.launch {
            settingsRepo.setDifficultyThreshold(threshold)
        }
    }

    /**
     * تغییر جهت پیش‌فرض زبان مطالعه در سراسر برنامه (اسپانیایی-فارسی یا برعکس).
     */
    fun setAppLanguageDirection(direction: CardDirection, onDirectionChanged: ((CardDirection) -> Unit)? = null) {
        scope.launch {
            settingsRepo.setAppLanguageDirection(direction)
            onDirectionChanged?.invoke(direction)
        }
    }

    /**
     * فعال/غیرفعال‌سازی نمایش نشان دسته در کارت‌های مرور.
     */
    fun setShowCategoryInReview(show: Boolean) {
        scope.launch {
            settingsRepo.setShowCategoryInReview(show)
        }
    }

    fun setUiLanguage(language: String) {
        scope.launch {
            settingsRepo.setUiLanguage(language)
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepo.setTtsEnabled(enabled)
        }
    }

    fun setTtsAutoPlay(autoPlay: Boolean) {
        scope.launch {
            settingsRepo.setTtsAutoPlay(autoPlay)
        }
    }

    fun setTtsSpeechRate(rate: Float) {
        scope.launch {
            settingsRepo.setTtsSpeechRate(rate)
        }
    }

    fun setQuizAutoAdvanceSeconds(seconds: Int) {
        scope.launch {
            settingsRepo.setQuizAutoAdvanceSeconds(seconds)
        }
    }

    fun setDefaultQuizLevel(level: com.manidigit.yadin.domain.model.QuizLevel) {
        scope.launch {
            settingsRepo.setDefaultQuizLevel(level)
        }
    }

    fun setDefaultReviewMode(mode: com.manidigit.yadin.domain.model.ReviewMode) {
        scope.launch {
            settingsRepo.setDefaultReviewMode(mode)
        }
    }

    // --- متدهای مدیریت پشتیبان‌گیری ---
    fun updateBackupOptions(options: BackupOptions) {
        _backupOptions.value = options
    }

    fun setFullBackup(enabled: Boolean) {
        _backupOptions.value = if (enabled) {
            BackupOptions(
                fullBackup = true,
                includeVocabulary = true,
                includeCategories = true,
                includeDifficulty = true,
                includeStreakAndProgress = true,
                includeReviewStats = true,
                includeProcessHistory = true,
                includeSettings = true
            )
        } else {
            _backupOptions.value.copy(fullBackup = false)
        }
    }

    fun toggleBackupOption(key: String, value: Boolean) {
        val cur = _backupOptions.value
        val updated = when (key) {
            "vocabulary" -> cur.copy(includeVocabulary = value)
            "categories" -> cur.copy(includeCategories = value)
            "difficulty" -> cur.copy(includeDifficulty = value)
            "streak" -> cur.copy(includeStreakAndProgress = value)
            "reviewStats" -> cur.copy(includeReviewStats = value)
            "process" -> cur.copy(includeProcessHistory = value)
            "settings" -> cur.copy(includeSettings = value)
            else -> cur
        }
        val isAll = updated.includeVocabulary && updated.includeCategories &&
                updated.includeDifficulty && updated.includeStreakAndProgress &&
                updated.includeReviewStats && updated.includeProcessHistory &&
                updated.includeSettings
        _backupOptions.value = updated.copy(fullBackup = isAll)
    }

    /**
     * ذخیره خروجی پشتیبان سفارشی در مسیر انتخابی سیستم فایل (SAF Uri).
     */
    fun exportCustomBackupToUri(
        format: BackupExportFormat,
        uri: Uri,
        options: BackupOptions = _backupOptions.value
    ) {
        val context = getApplication<Application>()
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (format != BackupExportFormat.JSON)
            _backupProgressMessage.value = if (isExcel) {
                context.getString(R.string.backup_generating_excel)
            } else {
                context.getString(R.string.backup_creating_backup)
            }
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        backupRepo.writeBackupByFormat(format, options, outputStream) { p, msg ->
                            _backupProgress.value = p
                            _backupProgressMessage.value = msg
                        }
                    }
                }
                _backupLastResult.value = if (isExcel) {
                    context.getString(R.string.backup_excel_saved_path)
                } else {
                    context.getString(R.string.backup_saved_path)
                }
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = context.getString(R.string.backup_save_error, e.message ?: "")
                _isBackupError.value = true
            } finally {
                _isBackupProcessing.value = false
            }
        }
    }

    /**
     * ذخیره فایل خروجی پشتیبان در حافظه محلی برنامه.
     */
    fun exportCustomBackup(
        format: BackupExportFormat,
        options: BackupOptions = _backupOptions.value
    ) {
        val context = getApplication<Application>()
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (format != BackupExportFormat.JSON)
            _backupProgressMessage.value = if (isExcel) {
                context.getString(R.string.backup_generating_excel)
            } else {
                context.getString(R.string.backup_creating_backup)
            }
            try {
                val ext = if (isExcel) "csv" else "json"
                val prefix = when (format) {
                    BackupExportFormat.JSON -> if (options.fullBackup) "yadin-full-backup" else "yadin-custom-backup"
                    BackupExportFormat.EXCEL_CSV_VOCABULARY -> "yadin-vocabulary"
                    BackupExportFormat.EXCEL_CSV_PROGRESS -> "yadin-progress-report"
                    BackupExportFormat.EXCEL_CSV_COMPLETE -> "yadin-master-export"
                }
                val fileName = "$prefix-${System.currentTimeMillis()}.$ext"
                val file = withContext(Dispatchers.IO) {
                    val backupsDir = File(context.filesDir, "backups")
                    if (!backupsDir.exists()) backupsDir.mkdirs()
                    val targetFile = File(backupsDir, fileName)
                    targetFile.outputStream().use { os ->
                        backupRepo.writeBackupByFormat(format, options, os) { p, msg ->
                            _backupProgress.value = p
                            _backupProgressMessage.value = msg
                        }
                    }
                    targetFile
                }
                _backupLastResult.value = context.getString(R.string.backup_file_saved_fmt, file.name)
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = context.getString(R.string.backup_export_error, e.message ?: "")
                _isBackupError.value = true
            } finally {
                _isBackupProcessing.value = false
                loadInternalBackups()
            }
        }
    }

    /**
     * تهیه نسخه پشتیبان بر اساس نوع عمومی در Uri مشخص‌شده.
     */
    fun exportBackupToUri(type: BackupType, uri: Uri) {
        val context = getApplication<Application>()
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (type == BackupType.VOCABULARY_EXCEL || type == BackupType.PROGRESS_EXCEL)
            _backupProgressMessage.value = if (isExcel) {
                context.getString(R.string.backup_generating_excel)
            } else {
                context.getString(R.string.backup_creating_backup)
            }
            try {
                val opt = when (type) {
                    BackupType.VOCABULARY -> BackupOptions(fullBackup = false, includeVocabulary = true, includeCategories = true, includeDifficulty = false, includeStreakAndProgress = false, includeReviewStats = false, includeProcessHistory = false, includeSettings = false)
                    BackupType.PROGRESS -> BackupOptions(fullBackup = false, includeVocabulary = false, includeCategories = false, includeDifficulty = true, includeStreakAndProgress = true, includeReviewStats = true, includeProcessHistory = true, includeSettings = true)
                    else -> BackupOptions(fullBackup = true)
                }
                val fmt = when (type) {
                    BackupType.VOCABULARY_EXCEL -> BackupExportFormat.EXCEL_CSV_VOCABULARY
                    BackupType.PROGRESS_EXCEL -> BackupExportFormat.EXCEL_CSV_PROGRESS
                    else -> BackupExportFormat.JSON
                }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        backupRepo.writeBackupByFormat(fmt, opt, outputStream) { p, msg ->
                            _backupProgress.value = p
                            _backupProgressMessage.value = msg
                        }
                    }
                }
                _backupLastResult.value = if (isExcel) {
                    context.getString(R.string.backup_excel_saved_path)
                } else {
                    context.getString(R.string.backup_saved_path)
                }
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = context.getString(R.string.backup_save_error, e.message ?: "")
                _isBackupError.value = true
            } finally {
                _isBackupProcessing.value = false
            }
        }
    }

    /**
     * ذخیره فایل پشتیبان بر اساس نوع عمومی در حافظه برنامه.
     */
    fun exportBackup(type: BackupType) {
        val context = getApplication<Application>()
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (type == BackupType.VOCABULARY_EXCEL || type == BackupType.PROGRESS_EXCEL)
            _backupProgressMessage.value = if (isExcel) {
                context.getString(R.string.backup_generating_excel)
            } else {
                context.getString(R.string.backup_creating_backup)
            }
            try {
                val opt = when (type) {
                    BackupType.VOCABULARY -> BackupOptions(fullBackup = false, includeVocabulary = true, includeCategories = true, includeDifficulty = false, includeStreakAndProgress = false, includeReviewStats = false, includeProcessHistory = false, includeSettings = false)
                    BackupType.PROGRESS -> BackupOptions(fullBackup = false, includeVocabulary = false, includeCategories = false, includeDifficulty = true, includeStreakAndProgress = true, includeReviewStats = true, includeProcessHistory = true, includeSettings = true)
                    else -> BackupOptions(fullBackup = true)
                }
                val fmt = when (type) {
                    BackupType.VOCABULARY_EXCEL -> BackupExportFormat.EXCEL_CSV_VOCABULARY
                    BackupType.PROGRESS_EXCEL -> BackupExportFormat.EXCEL_CSV_PROGRESS
                    else -> BackupExportFormat.JSON
                }
                val ext = if (isExcel) "csv" else "json"
                val fileName = "yadin-${type.name.lowercase()}-${System.currentTimeMillis()}.$ext"
                val file = withContext(Dispatchers.IO) {
                    val backupsDir = File(context.filesDir, "backups")
                    if (!backupsDir.exists()) backupsDir.mkdirs()
                    val targetFile = File(backupsDir, fileName)
                    targetFile.outputStream().use { os ->
                        backupRepo.writeBackupByFormat(fmt, opt, os) { p, msg ->
                            _backupProgress.value = p
                            _backupProgressMessage.value = msg
                        }
                    }
                    targetFile
                }
                _backupLastResult.value = context.getString(R.string.backup_file_saved_fmt, file.name)
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = context.getString(R.string.backup_export_error, e.message ?: "")
                _isBackupError.value = true
            } finally {
                _isBackupProcessing.value = false
                loadInternalBackups()
            }
        }
    }

    /**
     * بازیابی نسخه پشتیبان از رشته JSON.
     *
     * @param jsonString محتوای متنی فایل پشتیبان
     * @param isReplace جایگزینی کامل یا ادغام هوشمند
     * @param onRestoreCompleted کالبک اعلام موفقیت جهت بارگذاری مجدد کتابخانه
     */
    fun restoreBackup(
        jsonString: String,
        isReplace: Boolean,
        onRestoreCompleted: (() -> Unit)? = null
    ) {
        val context = getApplication<Application>()
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            _backupProgressMessage.value = context.getString(R.string.backup_validating_restoring)
            val result = backupRepo.restoreFromJson(jsonString, isReplace) { p, msg ->
                _backupProgress.value = p
                _backupProgressMessage.value = msg
            }
            if (result.isSuccess) {
                _backupLastResult.value = result.getOrNull() ?: context.getString(R.string.backup_action_success)
                _isBackupError.value = false
                onRestoreCompleted?.invoke()
            } else {
                _backupLastResult.value = context.getString(R.string.backup_restore_error_fmt, result.exceptionOrNull()?.message ?: "")
                _isBackupError.value = true
            }
            _isBackupProcessing.value = false
        }
    }
}
