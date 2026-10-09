package com.manidigit.yadin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manidigit.yadin.data.repository.VocabularyRepository
import com.manidigit.yadin.domain.algorithm.VocabularyParser
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.Category
import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.ImportSummary
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.model.WordDetail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * مدیریت بانک واژگان، کتابخانه، جستجو و ورود گروهی کلمات - LibraryViewModel
 *
 * مسئولیت‌ها (اصل تک‌مسئولیتی - SRP):
 * - مدیریت جستجو و فیلترهای دسته‌بندی و مراحل لایتنر.
 * - عملیات ویرایش، افزودن و حذف واژگان با پایداری شناسه و مدیریت کامل خطا (حل ISS-22).
 * - پارس متن و ورود داده‌های گروهی (Import) با تضمین ریست وضعیت در بلوک finally (حل ISS-22).
 *
 * @property vocabularyRepo مخزن واژگان و دسته‌بندی‌ها
 * @property externalScope دامنه کورتین اختیاری
 */
class LibraryViewModel(
    val vocabularyRepo: VocabularyRepository,
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    // --- وضعیت جستجو و فیلترها ---
    private val _searchQuery = MutableStateFlow("")
    /** عبارت جستجوی فعلی کاربر در کتابخانه */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    /** شناسه دسته‌بندی انتخاب‌شده جهت فیلتر (null یعنی تمام دسته‌ها) */
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedStageFilter = MutableStateFlow<Stage?>(null)
    /** مرحله لایتنر انتخاب‌شده جهت فیلتر (null یعنی تمام مراحل) */
    val selectedStageFilter: StateFlow<Stage?> = _selectedStageFilter.asStateFlow()

    private val _selectedDifficultyFilter = MutableStateFlow<com.manidigit.yadin.domain.model.VocabularyDifficulty?>(null)
    /** فیلتر درجه سختی واژگان (آسان، متوسط، سخت، خیلی سخت) */
    val selectedDifficultyFilter: StateFlow<com.manidigit.yadin.domain.model.VocabularyDifficulty?> = _selectedDifficultyFilter.asStateFlow()

    private val _showOnlyInactiveFilter = MutableStateFlow<Boolean>(false)
    /** فیلتر نمایش فقط واژگان حذف‌شده/غیرفعال */
    val showOnlyInactiveFilter: StateFlow<Boolean> = _showOnlyInactiveFilter.asStateFlow()

    private val _hasMoreResults = MutableStateFlow(true)
    /** نشانگر وجود داده‌های بیشتر برای لود تدریجی */
    val hasMoreResults: StateFlow<Boolean> = _hasMoreResults.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    /** نشانگر در حال لود صفحه بعدی */
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _searchResults = MutableStateFlow<List<WordDetail>>(emptyList())
    /** نتایج جستجو و فیلتر واژگان در کتابخانه */
    val searchResults: StateFlow<List<WordDetail>> = _searchResults.asStateFlow()

    private val _selectedWordDetail = MutableStateFlow<WordDetail?>(null)
    /** جزئیات واژه انتخاب‌شده جهت مشاهده در صفحه جزئیات یا ویرایش */
    val selectedWordDetail: StateFlow<WordDetail?> = _selectedWordDetail.asStateFlow()

    val categories: StateFlow<List<Category>> = vocabularyRepo.getAllCategoriesFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- وضعیت ورود واژگان (Import) ---
    private val _parseResult = MutableStateFlow<ParseResult?>(null)
    /** نتیجه پیش‌نمایش تحلیل ورودی متنی قبل از ثبت */
    val parseResult: StateFlow<ParseResult?> = _parseResult.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    /** وضعیت در حال انجام عملیات ذخیره گروهی */
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importProgress = MutableStateFlow(0f)
    /** درصد پیشرفت فرآیند ورود واژگان (از ۰ تا ۱) */
    val importProgress: StateFlow<Float> = _importProgress.asStateFlow()

    private val _importSummary = MutableStateFlow<ImportSummary?>(null)
    /** خلاصه آماری نتیجه ورود گروهی (کلمات جدید، تکراری، خطاها) */
    val importSummary: StateFlow<ImportSummary?> = _importSummary.asStateFlow()

    private val _libraryErrorMessage = MutableStateFlow<String?>(null)
    /** پیام خطای آخرین عملیات جهت نمایش به کاربر */
    val libraryErrorMessage: StateFlow<String?> = _libraryErrorMessage.asStateFlow()

    /**
     * تغییر عبارت جستجو و به‌روزرسانی نتایج کتابخانه.
     */
    fun onSearchQueryChanged(q: String, direction: CardDirection = CardDirection.NORMAL) {
        _searchQuery.value = q
        performSearch(q, _selectedCategoryFilter.value, _selectedStageFilter.value, direction, _selectedDifficultyFilter.value, _showOnlyInactiveFilter.value)
    }

    /**
     * تغییر فیلتر دسته‌بندی موضوعی.
     */
    fun onCategoryFilterChanged(catId: String?, direction: CardDirection = CardDirection.NORMAL) {
        _selectedCategoryFilter.value = catId
        performSearch(_searchQuery.value, catId, _selectedStageFilter.value, direction, _selectedDifficultyFilter.value, _showOnlyInactiveFilter.value)
    }

    /**
     * تغییر فیلتر مرحله لایتنر.
     */
    fun onStageFilterChanged(stage: Stage?, direction: CardDirection = CardDirection.NORMAL) {
        _selectedStageFilter.value = stage
        performSearch(_searchQuery.value, _selectedCategoryFilter.value, stage, direction, _selectedDifficultyFilter.value, _showOnlyInactiveFilter.value)
    }

    /**
     * تغییر فیلتر سختی واژه.
     */
    fun onDifficultyFilterChanged(difficulty: com.manidigit.yadin.domain.model.VocabularyDifficulty?, direction: CardDirection = CardDirection.NORMAL) {
        _selectedDifficultyFilter.value = difficulty
        performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value, direction, difficulty, _showOnlyInactiveFilter.value)
    }

    /**
     * تغییر فیلتر نمایش واژه‌های غیرفعال/حذف‌شده.
     */
    fun onInactiveFilterToggled(onlyInactive: Boolean, direction: CardDirection = CardDirection.NORMAL) {
        _showOnlyInactiveFilter.value = onlyInactive
        performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value, direction, _selectedDifficultyFilter.value, onlyInactive)
    }

    /**
     * اجرای کوئری فیلترشده در پایگاه داده با صفحه‌بندی نامحدود.
     */
    fun performSearch(
        query: String,
        categoryId: String?,
        stage: Stage?,
        direction: CardDirection = CardDirection.NORMAL,
        difficulty: com.manidigit.yadin.domain.model.VocabularyDifficulty? = _selectedDifficultyFilter.value,
        onlyInactive: Boolean = _showOnlyInactiveFilter.value
    ) {
        scope.launch {
            try {
                _libraryErrorMessage.value = null
                val results = vocabularyRepo.searchFiltered(
                    query = query,
                    categoryId = categoryId,
                    stage = stage,
                    direction = direction,
                    difficulty = difficulty,
                    onlyInactive = onlyInactive,
                    limit = 60,
                    offset = 0
                )
                _searchResults.value = results
                _hasMoreResults.value = (results.size >= 60)
            } catch (e: Exception) {
                _libraryErrorMessage.value = "خطا در جستجوی واژگان: ${e.message}"
            }
        }
    }

    /**
     * لود کردن صفحه بعدی نتایج برای اسکرول نامحدود (Infinite Scroll).
     */
    fun loadNextPage(direction: CardDirection = CardDirection.NORMAL) {
        if (!_hasMoreResults.value || _isLoadingMore.value) return
        scope.launch {
            try {
                _isLoadingMore.value = true
                val currentSize = _searchResults.value.size
                val nextBatch = vocabularyRepo.searchFiltered(
                    query = _searchQuery.value,
                    categoryId = _selectedCategoryFilter.value,
                    stage = _selectedStageFilter.value,
                    direction = direction,
                    difficulty = _selectedDifficultyFilter.value,
                    onlyInactive = _showOnlyInactiveFilter.value,
                    limit = 60,
                    offset = currentSize
                )
                if (nextBatch.isEmpty() || nextBatch.size < 60) {
                    _hasMoreResults.value = false
                }
                _searchResults.value = (_searchResults.value + nextBatch).distinctBy { it.concept.id }
            } catch (e: Exception) {
                _libraryErrorMessage.value = "خطا در بارگذاری موارد بیشتر: ${e.message}"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    /**
     * احیا و فعال‌سازی مجدد واژه غیرفعال‌شده.
     */
    fun reactivateWord(conceptId: String, direction: CardDirection = CardDirection.NORMAL) {
        scope.launch {
            try {
                vocabularyRepo.reactivateWord(conceptId)
                // Refresh current view
                performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value, direction)
            } catch (e: Exception) {
                _libraryErrorMessage.value = "خطا در فعال‌سازی مجدد واژه: ${e.message}"
            }
        }
    }

    /**
     * بارگذاری واژگان اخیر به صورت پیش‌فرض (بدون فیلتر).
     */
    fun loadRecentWords(direction: CardDirection = CardDirection.NORMAL) {
        performSearch("", null, null, direction)
    }

    /**
     * واکشی و انتخاب جزئیات کامل یک واژه بر اساس شناسه مفهوم.
     */
    fun selectWordDetail(conceptId: String, onLoaded: ((WordDetail?) -> Unit)? = null) {
        scope.launch {
            try {
                _libraryErrorMessage.value = null
                val detail = vocabularyRepo.getWordDetail(conceptId)
                _selectedWordDetail.value = detail
                onLoaded?.invoke(detail)
            } catch (e: Exception) {
                _libraryErrorMessage.value = "خطا در واکشی جزئیات واژه: ${e.message}"
            }
        }
    }

    /**
     * حذف نرم واژه (غیرفعال‌سازی مفهوم در دیتابیس).
     */
    fun deleteWord(
        conceptId: String,
        direction: CardDirection = CardDirection.NORMAL,
        onDeleted: (() -> Unit)? = null
    ) {
        scope.launch {
            try {
                _libraryErrorMessage.value = null
                vocabularyRepo.deleteWord(conceptId)
                performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value, direction)
                onDeleted?.invoke()
            } catch (e: Exception) {
                _libraryErrorMessage.value = "خطا در حذف واژه: ${e.message}"
            }
        }
    }

    /**
     * ذخیره یا به‌روزرسانی واژه با ارزیابی دقیق نتیجه (حل ISS-22).
     *
     * به جای نادیده گرفتن Result، وضعیت خروجی بررسی شده و در صورت شکست،
     * استثنا نمایش داده شده و از فراخوانی ناخواسته onSuccess جلوگیری می‌شود.
     */
    fun saveWord(
        conceptId: String?,
        sourceText: String,
        translations: List<String>,
        categoryId: String?,
        note: String?,
        direction: CardDirection = CardDirection.NORMAL,
        onError: ((String) -> Unit)? = null,
        onSuccess: () -> Unit
    ) {
        scope.launch {
            _libraryErrorMessage.value = null
            val result = if (conceptId != null) {
                vocabularyRepo.updateWord(conceptId, sourceText, translations, categoryId, note, direction)
            } else {
                vocabularyRepo.addWord(sourceText, translations, categoryId, note, direction)
            }

            if (result.isSuccess) {
                performSearch(_searchQuery.value, _selectedCategoryFilter.value, _selectedStageFilter.value, direction)
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "خطا در ذخیره واژه"
                _libraryErrorMessage.value = errorMsg
                onError?.invoke(errorMsg)
            }
        }
    }

    /**
     * تحلیل متن ورودی و استخراج ورودی‌های واژگان.
     */
    fun parseInputText(text: String) {
        try {
            _libraryErrorMessage.value = null
            val result = VocabularyParser.parse(text)
            _parseResult.value = result
        } catch (e: Exception) {
            _libraryErrorMessage.value = "خطا در پردازش متن: ${e.message}"
        }
    }

    /**
     * پاکسازی پیش‌نمایش تحلیل‌شده.
     */
    fun clearParseResult() {
        _parseResult.value = null
    }

    /**
     * تنظیم مستقیم نتیجه پارس‌شده از فایل (CSV, JSON, XLSX, Text).
     */
    fun setParseResult(result: ParseResult) {
        _libraryErrorMessage.value = null
        _parseResult.value = result
    }

    /**
     * افزودن دسته‌بندی جدید به دیتابیس.
     */
    fun createCategory(name: String, onCreated: (String) -> Unit = {}) {
        val clean = name.trim()
        if (clean.isBlank()) return
        scope.launch {
            try {
                _libraryErrorMessage.value = null
                val cat = vocabularyRepo.addCategory(clean)
                onCreated(cat.id)
            } catch (e: Exception) {
                _libraryErrorMessage.value = "خطا در ایجاد دسته: ${e.message}"
            }
        }
    }

    /**
     * پاکسازی گزارش خلاصه ورود گروهی.
     */
    fun clearImportSummary() {
        _importSummary.value = null
    }

    /**
     * اجرای عملیات ورود گروهی با مدیریت کامل چرخه حیات و بلوک finally (حل ISS-22).
     *
     * تضمین می‌شود که در صورت بروز هرگونه خطای ناشناخته، پرچم `isImporting` به حالت false
     * بازگشته و رابط کاربری برنامه هرگز قفل نمی‌ماند.
     */
    fun executeImport(
        policy: DuplicatePolicy,
        categoryId: String? = null,
        direction: CardDirection = CardDirection.NORMAL,
        onComplete: () -> Unit = {}
    ) {
        val result = _parseResult.value ?: return
        scope.launch {
            _isImporting.value = true
            _importProgress.value = 0f
            _libraryErrorMessage.value = null
            try {
                val summary = vocabularyRepo.importParsedEntries(result.entries, policy, categoryId) { done, total ->
                    if (total > 0) {
                        _importProgress.value = done.toFloat() / total
                    }
                }
                _parseResult.value = null
                _importSummary.value = summary
                loadRecentWords(direction)
            } catch (e: Exception) {
                _libraryErrorMessage.value = "خطا در ورود واژگان: ${e.message}"
            } finally {
                _isImporting.value = false
                onComplete()
            }
        }
    }

    /**
     * پاکسازی پیام خطا.
     */
    fun clearErrorMessage() {
        _libraryErrorMessage.value = null
    }
}
