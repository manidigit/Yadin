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
                    hasReachedVeryHard = hasReachedVeryHard
                )
            } else {
                DifficultyUpdateResult(
                    newDifficulty = current,
                    consecutiveCorrect = newCorrect,
                    consecutiveWrong = 0,
                    hasReachedVeryHard = hasReachedVeryHard
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
                val reachedVeryHard = hasReachedVeryHard || (stepped == VocabularyDifficulty.VERY_HARD)
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
                    hasReachedVeryHard = hasReachedVeryHard
                )
            }
        }
    }
}
