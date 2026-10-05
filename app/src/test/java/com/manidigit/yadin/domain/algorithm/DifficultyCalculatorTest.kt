package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.VocabularyDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyCalculatorTest {

    @Test
    fun medium_threeConsecutiveCorrect_becomesEasy() {
        val step1 = DifficultyCalculator.updateDifficulty(
            current = VocabularyDifficulty.MEDIUM,
            consecutiveCorrect = 0,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            isCorrect = true
        )
        assertEquals(VocabularyDifficulty.MEDIUM, step1.newDifficulty)
        assertEquals(1, step1.consecutiveCorrect)

        val step2 = DifficultyCalculator.updateDifficulty(
            current = step1.newDifficulty,
            consecutiveCorrect = step1.consecutiveCorrect,
            consecutiveWrong = step1.consecutiveWrong,
            hasReachedVeryHard = false,
            isCorrect = true
        )
        assertEquals(VocabularyDifficulty.MEDIUM, step2.newDifficulty)
        assertEquals(2, step2.consecutiveCorrect)

        val step3 = DifficultyCalculator.updateDifficulty(
            current = step2.newDifficulty,
            consecutiveCorrect = step2.consecutiveCorrect,
            consecutiveWrong = step2.consecutiveWrong,
            hasReachedVeryHard = false,
            isCorrect = true
        )
        assertEquals(VocabularyDifficulty.EASY, step3.newDifficulty)
        assertEquals(0, step3.consecutiveCorrect)
    }

    @Test
    fun medium_threeConsecutiveWrong_becomesHard() {
        var state = DifficultyUpdateResult(VocabularyDifficulty.MEDIUM, 0, 0, false)
        for (i in 1..3) {
            state = DifficultyCalculator.updateDifficulty(
                current = state.newDifficulty,
                consecutiveCorrect = state.consecutiveCorrect,
                consecutiveWrong = state.consecutiveWrong,
                hasReachedVeryHard = state.hasReachedVeryHard,
                isCorrect = false
            )
        }
        assertEquals(VocabularyDifficulty.HARD, state.newDifficulty)
        assertEquals(0, state.consecutiveWrong)
    }

    @Test
    fun hard_threeConsecutiveWrong_becomesVeryHardAndLocksFlag() {
        var state = DifficultyUpdateResult(VocabularyDifficulty.HARD, 0, 0, false)
        for (i in 1..3) {
            state = DifficultyCalculator.updateDifficulty(
                current = state.newDifficulty,
                consecutiveCorrect = state.consecutiveCorrect,
                consecutiveWrong = state.consecutiveWrong,
                hasReachedVeryHard = state.hasReachedVeryHard,
                isCorrect = false
            )
        }
        assertEquals(VocabularyDifficulty.VERY_HARD, state.newDifficulty)
        assertTrue(state.hasReachedVeryHard)
    }
}
