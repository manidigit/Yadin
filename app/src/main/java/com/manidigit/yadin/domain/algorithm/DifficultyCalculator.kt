package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.VocabularyDifficulty

data class DifficultyUpdateResult(
    val newDifficulty: VocabularyDifficulty,
    val consecutiveCorrect: Int,
    val consecutiveWrong: Int,
    val hasReachedVeryHard: Boolean
)

object DifficultyCalculator {

    fun updateDifficulty(
        current: VocabularyDifficulty,
        consecutiveCorrect: Int,
        consecutiveWrong: Int,
        hasReachedVeryHard: Boolean,
        isCorrect: Boolean,
        threshold: Int = 3
    ): DifficultyUpdateResult {
        val safeThreshold = threshold.coerceIn(1, 5)
        val baseReached = hasReachedVeryHard || (current == VocabularyDifficulty.VERY_HARD)

        return if (isCorrect) {
            val newCorrect = consecutiveCorrect + 1
            if (newCorrect >= safeThreshold) {
                // Step down difficulty (make it easier)
                val stepped = when (current) {
                    VocabularyDifficulty.VERY_HARD -> VocabularyDifficulty.HARD
                    VocabularyDifficulty.HARD -> VocabularyDifficulty.MEDIUM
                    VocabularyDifficulty.MEDIUM -> VocabularyDifficulty.EASY
                    VocabularyDifficulty.EASY -> VocabularyDifficulty.EASY
                }
                DifficultyUpdateResult(
                    newDifficulty = stepped,
                    consecutiveCorrect = 0,
                    consecutiveWrong = 0,
                    hasReachedVeryHard = baseReached
                )
            } else {
                DifficultyUpdateResult(
                    newDifficulty = current,
                    consecutiveCorrect = newCorrect,
                    consecutiveWrong = 0,
                    hasReachedVeryHard = baseReached
                )
            }
        } else {
            val newWrong = consecutiveWrong + 1
            if (newWrong >= safeThreshold) {
                // Step up difficulty (make it harder)
                val stepped = when (current) {
                    VocabularyDifficulty.EASY -> VocabularyDifficulty.MEDIUM
                    VocabularyDifficulty.MEDIUM -> VocabularyDifficulty.HARD
                    VocabularyDifficulty.HARD -> VocabularyDifficulty.VERY_HARD
                    VocabularyDifficulty.VERY_HARD -> VocabularyDifficulty.VERY_HARD
                }
                val reachedVeryHard = baseReached || (stepped == VocabularyDifficulty.VERY_HARD)
                DifficultyUpdateResult(
                    newDifficulty = stepped,
                    consecutiveCorrect = 0,
                    consecutiveWrong = 0,
                    hasReachedVeryHard = reachedVeryHard
                )
            } else {
                DifficultyUpdateResult(
                    newDifficulty = current,
                    consecutiveCorrect = 0,
                    consecutiveWrong = newWrong,
                    hasReachedVeryHard = baseReached
                )
            }
        }
    }

    /**
     * بروزرسانی سطح دشواری واژه هنگام انتخاب دکمه «سخت» (HARD) در فلش‌کارت SM-2 / FSRS:
     * در الگوریتم‌های SM-2 و FSRS گزینه HARD یک یادآوری موفق اما دشوار محسوب می‌شود (نه فراموشی کامل).
     * بنابراین:
     * - کلمات EASY و MEDIUM در صورت رسیدن به آستانه به سمت HARD حرکت می‌کنند (اما هرگز به VERY_HARD نمی‌روند).
     * - کلمات HARD در همان سطح HARD تثبیت می‌شوند.
     * - کلمات VERY_HARD به دلیل یادآوری موفق، در صورت رسیدن به آستانه یک پله به HARD کاهش می‌یابند.
     */
    fun updateDifficultyForHardRating(
        current: VocabularyDifficulty,
        consecutiveCorrect: Int,
        consecutiveWrong: Int,
        hasReachedVeryHard: Boolean,
        threshold: Int = 3
    ): DifficultyUpdateResult {
        val safeThreshold = threshold.coerceIn(1, 5)
        val baseReached = hasReachedVeryHard || (current == VocabularyDifficulty.VERY_HARD)

        return when (current) {
            VocabularyDifficulty.EASY, VocabularyDifficulty.MEDIUM -> {
                val newWrong = consecutiveWrong + 1
                if (newWrong >= safeThreshold) {
                    val stepped = if (current == VocabularyDifficulty.EASY) {
                        VocabularyDifficulty.MEDIUM
                    } else {
                        VocabularyDifficulty.HARD
                    }
                    DifficultyUpdateResult(
                        newDifficulty = stepped,
                        consecutiveCorrect = 0,
                        consecutiveWrong = 0,
                        hasReachedVeryHard = baseReached
                    )
                } else {
                    DifficultyUpdateResult(
                        newDifficulty = current,
                        consecutiveCorrect = 0,
                        consecutiveWrong = newWrong,
                        hasReachedVeryHard = baseReached
                    )
                }
            }
            VocabularyDifficulty.HARD -> {
                DifficultyUpdateResult(
                    newDifficulty = VocabularyDifficulty.HARD,
                    consecutiveCorrect = 0,
                    consecutiveWrong = 0,
                    hasReachedVeryHard = baseReached
                )
            }
            VocabularyDifficulty.VERY_HARD -> {
                val newCorrect = consecutiveCorrect + 1
                if (newCorrect >= safeThreshold) {
                    DifficultyUpdateResult(
                        newDifficulty = VocabularyDifficulty.HARD,
                        consecutiveCorrect = 0,
                        consecutiveWrong = 0,
                        hasReachedVeryHard = true
                    )
                } else {
                    DifficultyUpdateResult(
                        newDifficulty = VocabularyDifficulty.VERY_HARD,
                        consecutiveCorrect = newCorrect,
                        consecutiveWrong = 0,
                        hasReachedVeryHard = true
                    )
                }
            }
        }
    }
}
