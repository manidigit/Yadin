package com.manidigit.yadin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manidigit.yadin.data.local.dao.DayCountRaw
import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.data.repository.ReviewRepository
import com.manidigit.yadin.data.repository.VocabularyRepository
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.StatisticsSummary
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * مدیریت آمار، نمودار فعالیت، پیشرفت یادگیری و نشان‌های دستاورد - StatisticsViewModel
 *
 * مسئولیت‌ها (اصل تک‌مسئولیتی - SRP):
 * - واکنش به تغییر جهت مطالعه و واکشی تفکیکی آمار سررسید و پیشرفت متصل به جهت سراسری تنظیمات.
 * - ارائه شاخص‌های داشبورد (کارت‌های آماده، وضعیت مراحل روزانه/هفتگی/ماهانه).
 * - پایش تاریخچه مرورهای روزانه جهت رسم نمودار فعالیت (Activity Chart).
 * - ارزیابی و استخراج دستاوردهای کاربر و تعداد کل پاسخ‌های صحیح.
 *
 * @property vocabularyRepo مخزن واژگان و کوئری‌های آماری
 * @property reviewRepo مخزن نشست‌ها و دستاوردها
 * @property externalScope دامنه کورتین اختیاری
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    val vocabularyRepo: VocabularyRepository,
    val reviewRepo: ReviewRepository,
    private val externalScope: CoroutineScope? = null
) : ViewModel() {

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    // جهت زبان محلی (برای سازگاری رو به عقب - ترجیحاً از directionFlow سراسری تنظیمات استفاده می‌شود)
    private val _progressDirection = MutableStateFlow(CardDirection.NORMAL)
    val progressDirection: StateFlow<CardDirection> = _progressDirection.asStateFlow()

    // تریگر تازه‌سازی مجدد داده‌های آماری با فراخوانی refreshStatistics (حل ISS-38)
    private val _refreshTrigger = MutableStateFlow(0L)

    // --- آمار داشبورد متصل به جهت مطالعه فعال ---
    fun getDueCountFlow(directionFlow: StateFlow<CardDirection>): StateFlow<Int> {
        return combine(directionFlow, _refreshTrigger) { dir, _ -> dir }
            .flatMapLatest { dir -> vocabularyRepo.getDueCountFlow(dir) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    }

    fun getStatisticsFlow(directionFlow: StateFlow<CardDirection>): StateFlow<StatisticsSummary> {
        return combine(directionFlow, _refreshTrigger) { dir, _ -> dir }
            .flatMapLatest { dir -> vocabularyRepo.getStatisticsSummary(dir) }
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(5000),
                StatisticsSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
    }

    fun getDifficultyCountsFlow(directionFlow: StateFlow<CardDirection>): StateFlow<Map<VocabularyDifficulty, Int>> {
        return combine(directionFlow, _refreshTrigger) { dir, _ -> dir }
            .flatMapLatest { dir -> vocabularyRepo.getDifficultyBreakdownFlow(dir) }
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(5000),
                mapOf(
                    VocabularyDifficulty.EASY to 0,
                    VocabularyDifficulty.MEDIUM to 0,
                    VocabularyDifficulty.HARD to 0,
                    VocabularyDifficulty.VERY_HARD to 0
                )
            )
    }

    // --- شاخص‌های تفصیلی صفحه پیشرفت متصل به جهت سراسری تنظیمات ---
    fun getProgressScoreFlow(directionFlow: StateFlow<CardDirection>): StateFlow<Double> {
        return combine(directionFlow, _refreshTrigger) { dir, _ -> dir }
            .flatMapLatest { dir -> vocabularyRepo.getProgressScoreFlow(dir) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0.0)
    }

    val dailyStats: StateFlow<List<DayCountRaw>> = vocabularyRepo.getRecentDailyStatsFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getPracticedWordsCountFlow(directionFlow: StateFlow<CardDirection>): StateFlow<Int> {
        return combine(directionFlow, _refreshTrigger) { dir, _ -> dir }
            .flatMapLatest { dir -> vocabularyRepo.getPracticedWordsCountFlow(dir) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    }

    fun getProgressStatisticsFlow(directionFlow: StateFlow<CardDirection>): StateFlow<StatisticsSummary> {
        return combine(directionFlow, _refreshTrigger) { dir, _ -> dir }
            .flatMapLatest { dir -> vocabularyRepo.getStatisticsSummary(dir) }
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(5000),
                StatisticsSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
    }

    val achievements: StateFlow<List<AchievementEntity>> = reviewRepo.getAllAchievementsFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCorrectReviewsCount: StateFlow<Int> = vocabularyRepo.getTotalCorrectReviewsCountFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * همگام‌سازی جهت زبان محلی با جهت سراسری تنظیمات برنامه (ISS-58).
     */
    fun syncDirection(direction: CardDirection) {
        _progressDirection.value = direction
    }

    /**
     * تغییر جهت زبان (جهت سازگاری رو به عقب؛ ترجیحاً از Settings تغییر یابد).
     */
    fun setProgressDirection(direction: CardDirection) {
        _progressDirection.value = direction
    }

    /**
     * بازبینی و ارزیابی مجدد شرط‌های دستاوردها و تازه‌سازی مقادیر آماری (حل ISS-38).
     */
    fun refreshStatistics() {
        scope.launch {
            try {
                reviewRepo.checkAchievements()
                _refreshTrigger.value = System.currentTimeMillis()
            } catch (_: Exception) {
                // نادیده گرفتن خطا
            }
        }
    }
}
