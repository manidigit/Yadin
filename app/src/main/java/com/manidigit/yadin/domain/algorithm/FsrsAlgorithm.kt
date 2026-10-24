package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.time.ClockAndDayMath
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * پیاده‌سازی مستقل و استاندارد الگوریتم مدرن تکرار با فاصله
 * FSRS (Free Spaced Repetition Scheduler - FSRS-4.5 / FSRS-5)
 *
 * مبتنی بر مدل سه‌مؤلفه‌ای حافظه DSR (Difficulty, Stability, Retrievability):
 * - [FsrsState.stability] (S): پایداری حافظه بر حسب روز (مدت زمانی که احتمال یادآوری از ۱۰۰٪ به ۹۰٪ می‌رسد).
 * - [FsrsState.difficulty] (D): دشواری ذاتی کارت در بازه [1.0, 10.0].
 * - Retrievability (R): احتمال بازیابی کارت پس از گذشت t روز از آخرین مرور.
 */
enum class FsrsCardState(val code: Int) {
    NEW(0),
    LEARNING(1),
    REVIEW(2),
    RELEARNING(3);

    companion object {
        fun fromCode(code: Int): FsrsCardState = entries.firstOrNull { it.code == code } ?: NEW
    }
}

enum class FsrsRating(val grade: Int) {
    /** فراموش کردم (Grade = 1) */
    AGAIN(1),

    /** سخت (Grade = 2) */
    HARD(2),

    /** خوب (Grade = 3) */
    GOOD(3),

    /** عالی / آسان (Grade = 4) */
    EASY(4);

    val isCorrect: Boolean
        get() = grade >= 2

    companion object {
        fun fromSm2Quality(quality: Sm2Quality): FsrsRating = when (quality) {
            Sm2Quality.AGAIN -> AGAIN
            Sm2Quality.HARD -> HARD
            Sm2Quality.GOOD -> GOOD
            Sm2Quality.EASY -> EASY
        }

        fun fromBoolean(isCorrect: Boolean): FsrsRating = if (isCorrect) GOOD else AGAIN
    }
}

data class FsrsState(
    val stability: Double = 0.0,
    val difficulty: Double = FsrsAlgorithm.DEFAULT_INITIAL_DIFFICULTY,
    val reps: Int = 0,
    val lapses: Int = 0,
    val state: Int = FsrsCardState.NEW.code
)

data class FsrsTransitionResult(
    val nextState: FsrsState,
    val intervalDays: Int,
    val retrievabilityBefore: Double,
    val mappedStage: Stage,
    val nextReviewDay: String,
    val lastReviewedDay: String
)

object FsrsAlgorithm {

    const val DEFAULT_DESIRED_RETENTION = 0.90
    const val MIN_DESIRED_RETENTION = 0.75
    const val MAX_DESIRED_RETENTION = 0.97

    const val DEFAULT_INITIAL_DIFFICULTY = 5.0
    const val MIN_DIFFICULTY = 1.0
    const val MAX_DIFFICULTY = 10.0
    const val MIN_STABILITY = 0.1

    // ضرایب استاندارد منحنی فراموشی توانی FSRS-4.5 (F = 19/81, C = -0.5)
    private const val FACTOR = 19.0 / 81.0
    private const val DECAY = -0.5

    /**
     * وزن‌های پیش‌فرض بهینه‌شده FSRS-4.5 (w0..w16):
     * w0..w3: پایداری اولیه (S0) برای رتبه‌های Again, Hard, Good, Easy
     * w4, w5: پارامترهای دشواری اولیه (D0)
     * w6, w7: نرخ تغییر دشواری و بازگشت به میانگین (Mean Reversion)
     * w8..w10: ضرایب رشد پایداری در یادآوری موفق (S'_r)
     * w11..w14: ضرایب پایداری پس از فراموشی (S'_f)
     * w15: جریمه پاسخ Hard
     * w16: پاداش پاسخ Easy
     */
    private val W = doubleArrayOf(
        0.48,  // w0: S0(Again)
        1.40,  // w1: S0(Hard)
        3.70,  // w2: S0(Good)
        13.80, // w3: S0(Easy)
        5.00,  // w4: D0 base
        0.85,  // w5: D0 grade slope
        0.90,  // w6: difficulty delta
        0.15,  // w7: mean reversion weight
        1.55,  // w8: recall stability exp(w8)
        0.15,  // w9: stability power decay S^(-w9)
        1.00,  // w10: retrievability gain exp(w10 * (1 - R)) - 1
        2.00,  // w11: forget stability base
        0.20,  // w12: forget difficulty power D^(-w12)
        0.28,  // w13: forget stability power (S+1)^w13 - 1
        0.95,  // w14: forget retrievability exp(w14 * (1 - R))
        0.35,  // w15: Hard penalty
        2.60   // w16: Easy bonus
    )

    /**
     * محاسبه احتمال بازیابی (Retrievability) پس از گذشت [elapsedDays] روز با پایداری [stability].
     */
    fun calculateRetrievability(elapsedDays: Int, stability: Double): Double {
        if (stability <= 0.0) return 0.0
        val t = max(0, elapsedDays).toDouble()
        if (t == 0.0) return 1.0
        return (1.0 + FACTOR * (t / stability)).pow(DECAY).coerceIn(0.01, 1.0)
    }

    /**
     * محاسبه فاصله بهینه تا مرور بعدی بر حسب روز بر اساس پایداری [stability] و نرخ ماندگاری هدف [desiredRetention].
     */
    fun calculateIntervalDays(
        stability: Double,
        desiredRetention: Double = DEFAULT_DESIRED_RETENTION
    ): Int {
        val s = max(MIN_STABILITY, stability)
        val r = desiredRetention.coerceIn(MIN_DESIRED_RETENTION, MAX_DESIRED_RETENTION)
        val rawInterval = (s / FACTOR) * (r.pow(1.0 / DECAY) - 1.0)
        return max(1, rawInterval.roundToInt())
    }

    /**
     * محاسبه وضعیت بعدی کارت در الگوریتم FSRS.
     *
     * @param currentState وضعیت فعلی FSRS کارت
     * @param rating کیفیت پاسخ کاربر (AGAIN=1, HARD=2, GOOD=3, EASY=4)
     * @param lastReviewedDay تاریخ آخرین مرور کارت (YYYY-MM-DD) برای محاسبه زمان سپری‌شده واقعی
     * @param todayDayString تاریخ امروز (YYYY-MM-DD)
     * @param desiredRetention نرخ هدف ماندگاری حافظه (پیش‌فرض 0.90)
     */
    fun calculateNextReview(
        currentState: FsrsState,
        rating: FsrsRating,
        lastReviewedDay: String? = null,
        todayDayString: String = ClockAndDayMath.todayDayString(),
        desiredRetention: Double = DEFAULT_DESIRED_RETENTION
    ): FsrsTransitionResult {
        val g = rating.grade
        val isFirstReview = currentState.reps <= 0 || currentState.stability <= 0.0 || currentState.state == FsrsCardState.NEW.code

        val elapsedDays = if (!lastReviewedDay.isNullOrBlank()) {
            max(0, ClockAndDayMath.daysBetween(lastReviewedDay, todayDayString))
        } else {
            0
        }

        val newStability: Double
        val newDifficulty: Double
        val newReps: Int
        val newLapses: Int
        val newState: FsrsCardState
        val retrievabilityBefore: Double

        if (isFirstReview) {
            retrievabilityBefore = 0.0
            newStability = initialStability(rating)
            newDifficulty = initialDifficulty(rating)
            if (rating == FsrsRating.AGAIN) {
                newReps = 0
                newLapses = currentState.lapses + 1
                newState = FsrsCardState.LEARNING
            } else {
                newReps = 1
                newLapses = currentState.lapses
                newState = FsrsCardState.REVIEW
            }
        } else {
            val currentS = max(MIN_STABILITY, currentState.stability)
            val currentD = currentState.difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
            val effectiveElapsed = if (elapsedDays > 0) elapsedDays else 1
            val r = calculateRetrievability(effectiveElapsed, currentS)
            retrievabilityBefore = r

            newDifficulty = nextDifficulty(currentD, rating)

            if (rating == FsrsRating.AGAIN) {
                newStability = nextForgetStability(newDifficulty, currentS, r)
                newReps = currentState.reps
                newLapses = currentState.lapses + 1
                newState = FsrsCardState.RELEARNING
            } else {
                newStability = nextRecallStability(newDifficulty, currentS, r, rating)
                newReps = currentState.reps + 1
                newLapses = currentState.lapses
                newState = FsrsCardState.REVIEW
            }
        }

        val intervalDays = if (rating == FsrsRating.AGAIN) {
            1
        } else {
            val computed = calculateIntervalDays(newStability, desiredRetention)
            val hardInterval = if (isFirstReview) {
                max(1, calculateIntervalDays(initialStability(FsrsRating.HARD), desiredRetention))
            } else {
                val currentS = max(MIN_STABILITY, currentState.stability)
                val currentD = currentState.difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
                val dHard = nextDifficulty(currentD, FsrsRating.HARD)
                val sHard = nextRecallStability(dHard, currentS, retrievabilityBefore, FsrsRating.HARD)
                max(1, calculateIntervalDays(sHard, desiredRetention))
            }
            val goodInterval = if (isFirstReview) {
                max(hardInterval, max(2, calculateIntervalDays(initialStability(FsrsRating.GOOD), desiredRetention)))
            } else {
                val currentS = max(MIN_STABILITY, currentState.stability)
                val currentD = currentState.difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
                val dGood = nextDifficulty(currentD, FsrsRating.GOOD)
                val sGood = nextRecallStability(dGood, currentS, retrievabilityBefore, FsrsRating.GOOD)
                max(hardInterval, max(2, calculateIntervalDays(sGood, desiredRetention)))
            }
            when (rating) {
                FsrsRating.HARD -> hardInterval
                FsrsRating.GOOD -> max(hardInterval, max(2, computed))
                FsrsRating.EASY -> {
                    val minEasy = if (isFirstReview) max(4, goodInterval + 1) else goodInterval + 1
                    max(minEasy, computed)
                }
                else -> 1
            }
        }

        val nextReviewDay = ClockAndDayMath.addDays(todayDayString, intervalDays)
        val mappedStage = Sm2Algorithm.mapIntervalToStage(intervalDays, if (rating == FsrsRating.AGAIN) 0 else newReps)

        return FsrsTransitionResult(
            nextState = FsrsState(
                stability = round2(newStability),
                difficulty = round2(newDifficulty),
                reps = newReps,
                lapses = newLapses,
                state = newState.code
            ),
            intervalDays = intervalDays,
            retrievabilityBefore = round2(retrievabilityBefore),
            mappedStage = mappedStage,
            nextReviewDay = nextReviewDay,
            lastReviewedDay = todayDayString
        )
    }

    /**
     * پیش‌نمایش فاصله مرور بعدی (بر حسب روز) برای هر ۴ دکمه کیفیت پاسخ در پشت فلش‌کارت.
     * از آنجا که یکنواختی (Monotonicity) مستقیماً در [calculateNextReview] اعمال می‌شود،
     * اعداد پیش‌نمایش دقیقاً برابر با نتیجه واقعی پس از انتخاب دکمه هستند.
     */
    fun previewIntervals(
        currentState: FsrsState,
        lastReviewedDay: String? = null,
        todayDayString: String = ClockAndDayMath.todayDayString(),
        desiredRetention: Double = DEFAULT_DESIRED_RETENTION
    ): Sm2IntervalPreview {
        val again = calculateNextReview(currentState, FsrsRating.AGAIN, lastReviewedDay, todayDayString, desiredRetention).intervalDays
        val hard = calculateNextReview(currentState, FsrsRating.HARD, lastReviewedDay, todayDayString, desiredRetention).intervalDays
        val good = calculateNextReview(currentState, FsrsRating.GOOD, lastReviewedDay, todayDayString, desiredRetention).intervalDays
        val easy = calculateNextReview(currentState, FsrsRating.EASY, lastReviewedDay, todayDayString, desiredRetention).intervalDays
        return Sm2IntervalPreview(
            againDays = again,
            hardDays = hard,
            goodDays = good,
            easyDays = easy
        )
    }

    /**
     * همگام‌سازی وضعیت اولیه FSRS هنگام تغییر الگوریتم از یادین یا SM-2 به FSRS برای کارت‌هایی که قبلاً مرور شده‌اند.
     */
    fun resolveEffectiveState(
        state: FsrsState,
        stage: Stage,
        lastReviewedDay: String?,
        sm2IntervalDays: Int = 0,
        sm2Repetitions: Int = 0
    ): FsrsState {
        if (state.reps > 0 || state.stability > 0.0 || lastReviewedDay.isNullOrBlank()) {
            return state
        }
        val seededStability = when {
            sm2IntervalDays > 0 -> sm2IntervalDays.toDouble()
            stage == Stage.LEARNED -> 60.0
            stage == Stage.MONTHLY -> 30.0
            stage == Stage.WEEKLY -> 7.0
            else -> 1.4
        }
        val seededReps = when {
            sm2Repetitions > 0 -> sm2Repetitions
            stage == Stage.LEARNED -> 4
            stage == Stage.MONTHLY -> 3
            stage == Stage.WEEKLY -> 2
            else -> 1
        }
        return state.copy(
            stability = seededStability,
            difficulty = state.difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY),
            reps = seededReps,
            state = FsrsCardState.REVIEW.code
        )
    }

    private fun initialStability(rating: FsrsRating): Double {
        val idx = (rating.grade - 1).coerceIn(0, 3)
        return max(MIN_STABILITY, W[idx])
    }

    private fun initialDifficulty(rating: FsrsRating): Double {
        val d0 = W[4] - exp(W[5] * (rating.grade - 1)) + 1.0
        return d0.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
    }

    private fun nextDifficulty(currentD: Double, rating: FsrsRating): Double {
        val deltaD = -W[6] * (rating.grade - 3.0)
        val dampedD = currentD + deltaD * ((10.0 - currentD) / 9.0)
        val d0Good = initialDifficulty(FsrsRating.GOOD)
        // Mean reversion toward D0(3)
        val reverted = W[7] * d0Good + (1.0 - W[7]) * dampedD
        return reverted.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
    }

    private fun nextRecallStability(
        d: Double,
        s: Double,
        r: Double,
        rating: FsrsRating
    ): Double {
        val hardPenalty = if (rating == FsrsRating.HARD) W[15] else 1.0
        val easyBonus = if (rating == FsrsRating.EASY) W[16] else 1.0
        val gain = exp(W[8]) *
            (11.0 - d) *
            s.pow(-W[9]) *
            (exp(W[10] * (1.0 - r)) - 1.0) *
            hardPenalty *
            easyBonus
        return max(MIN_STABILITY, s * (1.0 + gain))
    }

    private fun nextForgetStability(
        d: Double,
        s: Double,
        r: Double
    ): Double {
        val sf = W[11] *
            d.pow(-W[12]) *
            ((s + 1.0).pow(W[13]) - 1.0) *
            exp(W[14] * (1.0 - r))
        return min(s, max(MIN_STABILITY, sf))
    }

    private fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}
