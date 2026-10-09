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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * مدیریت آمار، نمودار فعالیت، پیشرفت یادگیری و نشان‌های دستاورد - StatisticsViewModel
 *
 * مسئولیت‌ها (اصل تک‌مسئولیتی - SRP):
 * - واکنش به تغییر جهت مطالعه و واکشی تفکیکی آمار سررسید و پیشرفت.
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

    // جهت زبان انتخابی برای آمار صفحه پیشرفت (پیش‌فرض NORMAL)
    private val _progressDirection = MutableStateFlow(CardDirection.NORMAL)
    val progressDirection: StateFlow<CardDirection> = _progressDirection.asStateFlow()

    // --- آمار داشبورد متصل به جهت مطالعه فعال ---
    fun getDueCountFlow(directionFlow: StateFlow<CardDirection>): StateFlow<Int> {
        return directionFlow
            .flatMapLatest { dir -> vocabularyRepo.getDueCountFlow(dir) }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)
    }

    fun getStatisticsFlow(directionFlow: StateFlow<CardDirection>): StateFlow<StatisticsSummary> {
        return directionFlow
            .flatMapLatest { dir -> vocabularyRepo.getStatisticsSummary(dir) }
            .stateIn(
                scope,
                SharingStarted.WhileSubscribed(5000),
                StatisticsSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
    }

    fun getDifficultyCountsFlow(directionFlow: StateFlow<CardDirection>): StateFlow<Map<VocabularyDifficulty, Int>> {
        return directionFlow
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

    // --- شاخص‌های تفصیلی صفحه پیشرفت ---
    val progressScorePercent: StateFlow<Double> = _progressDirection
        .flatMapLatest { dir -> vocabularyRepo.getProgressScoreFlow(dir) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailyStats: StateFlow<List<DayCountRaw>> = vocabularyRepo.getRecentDailyStatsFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val practicedWordsCount: StateFlow<Int> = _progressDirection
        .flatMapLatest { dir -> vocabularyRepo.getPracticedWordsCountFlow(dir) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)

    val progressStatistics: StateFlow<StatisticsSummary> = _progressDirection
        .flatMapLatest { dir -> vocabularyRepo.getStatisticsSummary(dir) }
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(5000),
            StatisticsSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        )

    val achievements: StateFlow<List<AchievementEntity>> = reviewRepo.getAllAchievementsFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCorrectReviewsCount: StateFlow<Int> = vocabularyRepo.getTotalCorrectReviewsCountFlow()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * تغییر جهت زبان برای نمایش آمار اختصاصی در صفحه پیشرفت.
     */
    fun setProgressDirection(direction: CardDirection) {
        _progressDirection.value = direction
    }

    /**
     * بازبینی و ارزیابی مجدد شرط‌های دستاوردها و تازه‌سازی مقادیر آماری.
     */
    fun refreshStatistics() {
        scope.launch {
            try {
                reviewRepo.checkAchievements()
                val current = _progressDirection.value
                _progressDirection.value = current
            } catch (e: Exception) {
                // خطای لاگ بدون شکست برنامه
            }
        }
    }
}
