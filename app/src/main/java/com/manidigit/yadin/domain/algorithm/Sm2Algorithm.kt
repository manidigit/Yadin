package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.time.ClockAndDayMath
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * پیاده‌سازی مستقل و استاندارد الگوریتم تکرار با فاصله SuperMemo 2 (SM-2)
 *
 * مرجع علمی: P.A. Wozniak (1990) - Optimization of repetition spacing in the practice of learning.
 *
 * متغیرهای حالت هر کارت در SM-2:
 * - [Sm2State.repetitions] (n): تعداد پاسخ‌های صحیح متوالی (با کیفیت q >= 3).
 * - [Sm2State.easinessFactor] (EF): ضریب آسانی واژه (مقدار اولیه 2.5، حداقل 1.3).
 * - [Sm2State.intervalDays] (I): فاصله تا مرور بعدی بر حسب روز.
 */
enum class Sm2Quality(val score: Int) {
    /** فراموش کردم (پاسخ کاملاً نادرست - بازنشانی فاصله به ۱ روز) */
    AGAIN(1),

    /** سخت (یادآوری با دشواری زیاد - کاهش ضریب آسانی و رشد محافظه‌کارانه فاصله) */
    HARD(3),

    /** خوب (یادآوری استاندارد پس از کمی فکر - حفظ تعادل ضریب آسانی) */
    GOOD(4),

    /** عالی / آسان (یادآوری فوری و بدون مکث - افزایش ضریب آسانی و رشد سریع‌تر فاصله) */
    EASY(5);

    val isCorrect: Boolean
        get() = score >= 3

    companion object {
        fun fromBoolean(isCorrect: Boolean): Sm2Quality =
            if (isCorrect) GOOD else AGAIN
    }
}

data class Sm2State(
    val repetitions: Int = 0,
    val easinessFactor: Double = Sm2Algorithm.DEFAULT_EASINESS_FACTOR,
    val intervalDays: Int = 0
)

data class Sm2TransitionResult(
    val nextState: Sm2State,
    val mappedStage: Stage,
    val nextReviewDay: String,
    val lastReviewedDay: String
)

data class Sm2IntervalPreview(
    val againDays: Int,
    val hardDays: Int,
    val goodDays: Int,
    val easyDays: Int
)

object Sm2Algorithm {

    const val DEFAULT_EASINESS_FACTOR = 2.5
    const val MIN_EASINESS_FACTOR = 1.3

    /**
     * محاسبه وضعیت بعدی کارت بر اساس فرمول استاندارد SM-2 و نمره کیفیت پاسخ کاربر.
     *
     * @param currentState وضعیت فعلی SM-2 کارت (تکرار، ضریب آسانی، فاصله قبلی)
     * @param quality کیفیت پاسخ کاربر (AGAIN=1, HARD=3, GOOD=4, EASY=5)
     * @param todayDayString تاریخ امروز به فرمت YYYY-MM-DD
     */
    fun calculateNextReview(
        currentState: Sm2State,
        quality: Sm2Quality,
        todayDayString: String = ClockAndDayMath.todayDayString()
    ): Sm2TransitionResult {
        val q = quality.score
        val currentEf = currentState.easinessFactor.coerceAtLeast(MIN_EASINESS_FACTOR)
        val currentReps = currentState.repetitions.coerceAtLeast(0)
        val currentInterval = currentState.intervalDays.coerceAtLeast(0)

        // ۱. محاسبه ضریب آسانی جدید: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val qDiff = (5 - q).toDouble()
        val efDelta = 0.1 - qDiff * (0.08 + qDiff * 0.02)
        val newEf = max(MIN_EASINESS_FACTOR, currentEf + efDelta)

        // ۲. محاسبه تعداد تکرار و فاصله روزانه جدید (Interval)
        val newReps: Int
        val newInterval: Int

        if (q < 3) {
            // پاسخ نادرست: بازنشانی زنجیره تکرار و مرور در روز بعد
            newReps = 0
            newInterval = 1
        } else {
            newReps = currentReps + 1
            newInterval = when (currentReps) {
                0 -> when (quality) {
                    Sm2Quality.HARD -> 1
                    Sm2Quality.GOOD -> 2
                    Sm2Quality.EASY -> 4
                    else -> 1
                }
                1 -> {
                    val hardInt = max(2, (currentInterval * 1.2).roundToInt()).coerceAtMost(5)
                    val goodInt = max(hardInt + 1, 6)
                    val easyInt = max(goodInt + 1, 8)
                    when (quality) {
                        Sm2Quality.HARD -> hardInt
                        Sm2Quality.GOOD -> goodInt
                        Sm2Quality.EASY -> easyInt
                        else -> goodInt
                    }
                }
                else -> {
                    val baseInterval = max(1, currentInterval)
                    val hardInt = max(baseInterval + 1, (baseInterval * max(1.2, currentEf * 0.85)).roundToInt())
                    val goodInt = max(hardInt + 1, (baseInterval * newEf).roundToInt())
                    val easyInt = max(goodInt + 1, (baseInterval * newEf * 1.3).roundToInt())
                    when (quality) {
                        Sm2Quality.HARD -> hardInt
                        Sm2Quality.GOOD -> goodInt
                        Sm2Quality.EASY -> easyInt
                        else -> goodInt
                    }
                }
            }
        }

        val nextReviewDay = ClockAndDayMath.addDays(todayDayString, newInterval)
        val mappedStage = mapIntervalToStage(newInterval, newReps)

        return Sm2TransitionResult(
            nextState = Sm2State(
                repetitions = newReps,
                easinessFactor = (newEf * 100.0).roundToInt() / 100.0,
                intervalDays = newInterval
            ),
            mappedStage = mappedStage,
            nextReviewDay = nextReviewDay,
            lastReviewedDay = todayDayString
        )
    }

    /**
     * پیش‌نمایش فاصله مرور بعدی (بر حسب روز) برای هر ۴ دکمه کیفیت پاسخ در پشت فلش‌کارت.
     */
    fun previewIntervals(
        currentState: Sm2State,
        todayDayString: String = ClockAndDayMath.todayDayString()
    ): Sm2IntervalPreview {
        val again = calculateNextReview(currentState, Sm2Quality.AGAIN, todayDayString).nextState.intervalDays
        val hard = calculateNextReview(currentState, Sm2Quality.HARD, todayDayString).nextState.intervalDays
        val good = calculateNextReview(currentState, Sm2Quality.GOOD, todayDayString).nextState.intervalDays
        val easy = calculateNextReview(currentState, Sm2Quality.EASY, todayDayString).nextState.intervalDays
        return Sm2IntervalPreview(
            againDays = again,
            hardDays = hard,
            goodDays = good,
            easyDays = easy
        )
    }

    /**
     * همگام‌سازی وضعیت اولیه SM-2 هنگام تغییر الگوریتم از یادین یا FSRS به SM-2 برای کارت‌هایی که قبلاً مرور شده‌اند.
     */
    fun resolveEffectiveState(
        state: Sm2State,
        stage: Stage,
        lastReviewedDay: String?,
        fsrsStability: Double = 0.0
    ): Sm2State {
        if (state.repetitions > 0 || state.intervalDays > 0 || lastReviewedDay.isNullOrBlank()) {
            return state
        }
        if (fsrsStability > 0.0) {
            val seededInterval = max(1, fsrsStability.roundToInt())
            val seededReps = when {
                seededInterval >= 30 -> 3
                seededInterval >= 6 -> 2
                else -> 1
            }
            return state.copy(repetitions = seededReps, intervalDays = seededInterval)
        }
        return when (stage) {
            Stage.DAILY -> state.copy(repetitions = 1, intervalDays = 1)
            Stage.WEEKLY -> state.copy(repetitions = 2, intervalDays = 7)
            Stage.MONTHLY -> state.copy(repetitions = 3, intervalDays = 30)
            Stage.LEARNED -> state.copy(repetitions = 4, intervalDays = 60)
        }
    }

    /**
     * نگاشت فاصله زمانی SM-2 به مراحل ۴گانه یادین جهت یکپارچگی آمار، فیلترهای کتابخانه و نمودارها.
     */
    fun mapIntervalToStage(intervalDays: Int, repetitions: Int): Stage {
        return when {
            repetitions == 0 || intervalDays <= 3 -> Stage.DAILY
            intervalDays <= 14 -> Stage.WEEKLY
            intervalDays < 60 -> Stage.MONTHLY
            else -> Stage.LEARNED
        }
    }
}
