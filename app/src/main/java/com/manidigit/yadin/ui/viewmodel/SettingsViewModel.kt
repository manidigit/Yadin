package com.manidigit.yadin.ui.viewmodel

import android.app.Application
import android.net.Uri
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
        .stateIn(scope, SharingStarted.Eagerly, "gtp")

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
        .stateIn(scope, SharingStarted.Eagerly, com.manidigit.yadin.domain.model.QuizLevel.MEDIUM)

    val defaultReviewMode: StateFlow<com.manidigit.yadin.domain.model.ReviewMode> = settingsRepo.defaultReviewModeFlow
        .stateIn(scope, SharingStarted.Eagerly, com.manidigit.yadin.domain.model.ReviewMode.FLASHCARD)

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

    /**
     * تغییر چرخشی تم‌های رنگی برنامه (GTP -> Gemini -> Claude -> Googoli -> GTP).
     */
    fun toggleTheme() {
        scope.launch {
            val next = when (themeId.value.lowercase()) {
                "gtp" -> "gemini"
                "gemini" -> "claude"
                "claude" -> "googoli"
                else -> "gtp"
            }
            settingsRepo.setThemeId(next)
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
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (format != BackupExportFormat.JSON)
            _backupProgressMessage.value = if (isExcel) "در حال تولید فایل اکسل..." else "در حال ایجاد فایل پشتیبان..."
            try {
                val content = backupRepo.createBackupByFormat(format, options) { p, msg ->
                    _backupProgress.value = p
                    _backupProgressMessage.value = msg
                }
                val context = getApplication<Application>()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                }
                _backupLastResult.value = if (isExcel) {
                    "فایل اکسل (CSV) با موفقیت در مسیر انتخاب‌شده ذخیره شد."
                } else {
                    "فایل پشتیبان با موفقیت در مسیر انتخاب‌شده ذخیره شد."
                }
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = "خطا در ذخیره فایل: ${e.message}"
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
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (format != BackupExportFormat.JSON)
            _backupProgressMessage.value = if (isExcel) "در حال تولید فایل اکسل..." else "در حال ایجاد فایل پشتیبان..."
            try {
                val content = backupRepo.createBackupByFormat(format, options) { p, msg ->
                    _backupProgress.value = p
                    _backupProgressMessage.value = msg
                }
                val ext = if (isExcel) "csv" else "json"
                val prefix = when (format) {
                    BackupExportFormat.JSON -> if (options.fullBackup) "yadin-full-backup" else "yadin-custom-backup"
                    BackupExportFormat.EXCEL_CSV_VOCABULARY -> "yadin-vocabulary"
                    BackupExportFormat.EXCEL_CSV_PROGRESS -> "yadin-progress-report"
                    BackupExportFormat.EXCEL_CSV_COMPLETE -> "yadin-master-export"
                }
                val fileName = "$prefix-${System.currentTimeMillis()}.$ext"
                val file = backupRepo.saveBackupToFile(content, fileName)
                _backupLastResult.value = "فایل با موفقیت ذخیره شد: ${file.name}"
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = "خطا در تهیه خروجی: ${e.message}"
                _isBackupError.value = true
            } finally {
                _isBackupProcessing.value = false
            }
        }
    }

    /**
     * تهیه نسخه پشتیبان بر اساس نوع عمومی در Uri مشخص‌شده.
     */
    fun exportBackupToUri(type: BackupType, uri: Uri) {
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (type == BackupType.VOCABULARY_EXCEL || type == BackupType.PROGRESS_EXCEL)
            _backupProgressMessage.value = if (isExcel) "در حال تولید فایل اکسل..." else "در حال ایجاد فایل پشتیبان..."
            try {
                val content = backupRepo.createBackupString(type) { p, msg ->
                    _backupProgress.value = p
                    _backupProgressMessage.value = msg
                }
                val context = getApplication<Application>()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                }
                _backupLastResult.value = if (isExcel) {
                    "فایل اکسل (CSV) با موفقیت در مسیر انتخاب‌شده ذخیره شد."
                } else {
                    "فایل پشتیبان با موفقیت در مسیر انتخاب‌شده ذخیره شد."
                }
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = "خطا در ذخیره فایل: ${e.message}"
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
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            val isExcel = (type == BackupType.VOCABULARY_EXCEL || type == BackupType.PROGRESS_EXCEL)
            _backupProgressMessage.value = if (isExcel) "در حال تولید فایل اکسل..." else "در حال ایجاد فایل پشتیبان..."
            try {
                val content = backupRepo.createBackupString(type) { p, msg ->
                    _backupProgress.value = p
                    _backupProgressMessage.value = msg
                }
                val ext = if (isExcel) "csv" else "json"
                val fileName = "yadin-${type.name.lowercase()}-${System.currentTimeMillis()}.$ext"
                val file = backupRepo.saveBackupToFile(content, fileName)
                _backupLastResult.value = "فایل با موفقیت ذخیره شد: ${file.name}"
                _isBackupError.value = false
            } catch (e: Exception) {
                _backupLastResult.value = "خطا در تهیه خروجی: ${e.message}"
                _isBackupError.value = true
            } finally {
                _isBackupProcessing.value = false
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
        scope.launch {
            _isBackupProcessing.value = true
            _isBackupError.value = false
            _backupProgress.value = 0f
            _backupProgressMessage.value = "در حال اعتبارسنجی و بازیابی..."
            val result = backupRepo.restoreFromJson(jsonString, isReplace) { p, msg ->
                _backupProgress.value = p
                _backupProgressMessage.value = msg
            }
            if (result.isSuccess) {
                _backupLastResult.value = result.getOrNull() ?: "با موفقیت انجام شد"
                _isBackupError.value = false
                onRestoreCompleted?.invoke()
            } else {
                _backupLastResult.value = "خطا در بازیابی: ${result.exceptionOrNull()?.message}"
                _isBackupError.value = true
            }
            _isBackupProcessing.value = false
        }
    }
}
