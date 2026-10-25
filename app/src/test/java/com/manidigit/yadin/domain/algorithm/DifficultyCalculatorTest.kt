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

    @Test
    fun customThreshold_twoConsecutiveCorrect_triggersTransition() {
        val step1 = DifficultyCalculator.updateDifficulty(
            current = VocabularyDifficulty.HARD,
            consecutiveCorrect = 0,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            isCorrect = true,
            threshold = 2
        )
        assertEquals(VocabularyDifficulty.HARD, step1.newDifficulty)
        assertEquals(1, step1.consecutiveCorrect)

        val step2 = DifficultyCalculator.updateDifficulty(
            current = step1.newDifficulty,
            consecutiveCorrect = step1.consecutiveCorrect,
            consecutiveWrong = step1.consecutiveWrong,
            hasReachedVeryHard = false,
            isCorrect = true,
            threshold = 2
        )
        assertEquals(VocabularyDifficulty.MEDIUM, step2.newDifficulty)
        assertEquals(0, step2.consecutiveCorrect)
    }

    @Test
    fun customThreshold_oneConsecutiveCorrect_triggersImmediateTransition() {
        val resultCorrect = DifficultyCalculator.updateDifficulty(
            current = VocabularyDifficulty.MEDIUM,
            consecutiveCorrect = 0,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            isCorrect = true,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.EASY, resultCorrect.newDifficulty)
        assertEquals(0, resultCorrect.consecutiveCorrect)

        val resultWrong = DifficultyCalculator.updateDifficulty(
            current = VocabularyDifficulty.MEDIUM,
            consecutiveCorrect = 0,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            isCorrect = false,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.HARD, resultWrong.newDifficulty)
        assertEquals(0, resultWrong.consecutiveWrong)
    }

    @Test
    fun easy_defaultDifficultyStateEntity_isEasy() {
        val entity = com.manidigit.yadin.data.local.entity.DifficultyStateEntity(
            id = "test-1",
            conceptId = "concept-1"
        )
        assertEquals(VocabularyDifficulty.EASY, entity.current)
    }

    @Test
    fun easy_threeConsecutiveWrong_becomesMedium() {
        var state = DifficultyUpdateResult(VocabularyDifficulty.EASY, 0, 0, false)
        for (i in 1..3) {
            state = DifficultyCalculator.updateDifficulty(
                current = state.newDifficulty,
                consecutiveCorrect = state.consecutiveCorrect,
                consecutiveWrong = state.consecutiveWrong,
                hasReachedVeryHard = state.hasReachedVeryHard,
                isCorrect = false
            )
        }
        assertEquals(VocabularyDifficulty.MEDIUM, state.newDifficulty)
        assertEquals(0, state.consecutiveWrong)
    }

    @Test
    fun easy_consecutiveCorrect_staysEasy() {
        var state = DifficultyUpdateResult(VocabularyDifficulty.EASY, 0, 0, false)
        for (i in 1..5) {
            state = DifficultyCalculator.updateDifficulty(
                current = state.newDifficulty,
                consecutiveCorrect = state.consecutiveCorrect,
                consecutiveWrong = state.consecutiveWrong,
                hasReachedVeryHard = state.hasReachedVeryHard,
                isCorrect = true
            )
        }
        assertEquals(VocabularyDifficulty.EASY, state.newDifficulty)
    }

    @Test
    fun converters_defaultDifficulty_isEasy() {
        val converters = com.manidigit.yadin.data.local.database.Converters()
        assertEquals(VocabularyDifficulty.EASY.name, converters.fromDifficulty(null))
        assertEquals(VocabularyDifficulty.EASY, converters.toDifficulty(null))
        assertEquals(VocabularyDifficulty.EASY, converters.toDifficulty("INVALID_NAME"))
    }

    @Test
    fun thresholdOne_everySingleWrongAnswer_stepsUpDifficultyImmediately() {
        var state = DifficultyUpdateResult(VocabularyDifficulty.EASY, 0, 0, false)

        // 1st wrong with threshold = 1: EASY -> MEDIUM
        state = DifficultyCalculator.updateDifficulty(
            current = state.newDifficulty,
            consecutiveCorrect = state.consecutiveCorrect,
            consecutiveWrong = state.consecutiveWrong,
            hasReachedVeryHard = state.hasReachedVeryHard,
            isCorrect = false,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.MEDIUM, state.newDifficulty)
        assertEquals(0, state.consecutiveWrong)

        // 2nd wrong with threshold = 1: MEDIUM -> HARD
        state = DifficultyCalculator.updateDifficulty(
            current = state.newDifficulty,
            consecutiveCorrect = state.consecutiveCorrect,
            consecutiveWrong = state.consecutiveWrong,
            hasReachedVeryHard = state.hasReachedVeryHard,
            isCorrect = false,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.HARD, state.newDifficulty)
        assertEquals(0, state.consecutiveWrong)

        // 3rd wrong with threshold = 1: HARD -> VERY_HARD
        state = DifficultyCalculator.updateDifficulty(
            current = state.newDifficulty,
            consecutiveCorrect = state.consecutiveCorrect,
            consecutiveWrong = state.consecutiveWrong,
            hasReachedVeryHard = state.hasReachedVeryHard,
            isCorrect = false,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.VERY_HARD, state.newDifficulty)
        assertTrue(state.hasReachedVeryHard)
    }

    @Test
    fun initialVeryHard_whenSteppedDown_preservesHasReachedVeryHardFlag() {
        val result = DifficultyCalculator.updateDifficulty(
            current = VocabularyDifficulty.VERY_HARD,
            consecutiveCorrect = 0,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            isCorrect = true,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.HARD, result.newDifficulty)
        assertTrue(result.hasReachedVeryHard)
    }

    @Test
    fun hardRating_convergesToHardFromEasyAndVeryHardWithoutPushingToVeryHard() {
        // EASY -> MEDIUM -> HARD when rating HARD with threshold = 1
        val fromEasy = DifficultyCalculator.updateDifficultyForHardRating(
            current = VocabularyDifficulty.EASY,
            consecutiveCorrect = 0,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.MEDIUM, fromEasy.newDifficulty)

        val fromMedium = DifficultyCalculator.updateDifficultyForHardRating(
            current = fromEasy.newDifficulty,
            consecutiveCorrect = fromEasy.consecutiveCorrect,
            consecutiveWrong = fromEasy.consecutiveWrong,
            hasReachedVeryHard = fromEasy.hasReachedVeryHard,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.HARD, fromMedium.newDifficulty)

        // HARD stays HARD (never pushed to VERY_HARD by HARD rating) and preserves consecutiveCorrect
        val fromHard = DifficultyCalculator.updateDifficultyForHardRating(
            current = VocabularyDifficulty.HARD,
            consecutiveCorrect = 2,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            threshold = 3
        )
        assertEquals(VocabularyDifficulty.HARD, fromHard.newDifficulty)
        assertEquals(2, fromHard.consecutiveCorrect)
        assertFalse(fromHard.hasReachedVeryHard)

        // VERY_HARD steps down to HARD on HARD recall rating and retains hasReachedVeryHard = true
        val fromVeryHard = DifficultyCalculator.updateDifficultyForHardRating(
            current = VocabularyDifficulty.VERY_HARD,
            consecutiveCorrect = 0,
            consecutiveWrong = 0,
            hasReachedVeryHard = false,
            threshold = 1
        )
        assertEquals(VocabularyDifficulty.HARD, fromVeryHard.newDifficulty)
        assertTrue(fromVeryHard.hasReachedVeryHard)
    }
}
