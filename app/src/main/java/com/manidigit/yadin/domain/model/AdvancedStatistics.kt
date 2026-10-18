package com.manidigit.yadin.domain.model

import androidx.annotation.StringRes
import com.manidigit.yadin.R

/**
 * مدل شاخص‌های ماندگاری حافظه و تسلط کلمات
 */
data class RetentionMetrics(
    val retentionScore: Int, // 0 - 100
    val totalWords: Int,
    val practicedWords: Int,
    val learnedWords: Int,
    val monthlyWords: Int,
    val weeklyWords: Int,
    val dailyWords: Int,
    val unreviewedWords: Int
)

/**
 * مدل پیش‌بینی موعدهای مرور روزهای آینده
 */
data class ReviewForecastItem(
    val dayOffset: Int, // 1 (فردا)، 3 (۳ روز آینده)، 7 (هفته آینده)
    val dayString: String,
    val dueCount: Int
)

/**
 * بازه‌های زمانی شبانه‌روز برای تحلیل عادات مطالعه
 */
enum class StudyHourSlot(
    @StringRes val titleRes: Int,
    @StringRes val timeRangeRes: Int
) {
    MORNING(R.string.study_slot_morning, R.string.study_slot_morning_range),
    AFTERNOON(R.string.study_slot_afternoon, R.string.study_slot_afternoon_range),
    EVENING(R.string.study_slot_evening, R.string.study_slot_evening_range),
    NIGHT(R.string.study_slot_night, R.string.study_slot_night_range)
}

/**
 * آمار عملکرد کاربر در هر بازه زمانی
 */
data class StudyHourBucket(
    val slot: StudyHourSlot,
    val reviewCount: Int,
    val correctCount: Int,
    val accuracyPercent: Int
)

/**
 * میزان تسلط به تفکیک دسته‌بندی‌های موضوعی
 */
data class CategoryMasteryItem(
    val categoryId: String,
    val categoryName: String,
    val totalWords: Int,
    val learnedWords: Int,
    val masteryPercent: Int
)
