package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sm2AlgorithmTest {

    @Test
    fun `first review with GOOD sets interval to 1 day and repetitions to 1`() {
        val initial = Sm2State()
        val result = Sm2Algorithm.calculateNextReview(
            currentState = initial,
            quality = Sm2Quality.GOOD,
            todayDayString = "2026-10-19"
        )

        assertEquals(1, result.nextState.repetitions)
        assertEquals(1, result.nextState.intervalDays)
        assertEquals(2.5, result.nextState.easinessFactor, 0.01)
        assertEquals("2026-10-20", result.nextReviewDay)
        assertEquals(Stage.DAILY, result.mappedStage)
    }

    @Test
    fun `second review with GOOD sets interval to 6 days and maps to WEEKLY`() {
        val stateAfterFirst = Sm2State(repetitions = 1, easinessFactor = 2.5, intervalDays = 1)
        val result = Sm2Algorithm.calculateNextReview(
            currentState = stateAfterFirst,
            quality = Sm2Quality.GOOD,
            todayDayString = "2026-10-20"
        )

        assertEquals(2, result.nextState.repetitions)
        assertEquals(6, result.nextState.intervalDays)
        assertEquals(2.5, result.nextState.easinessFactor, 0.01)
        assertEquals("2026-10-26", result.nextReviewDay)
        assertEquals(Stage.WEEKLY, result.mappedStage)
    }

    @Test
    fun `third review with GOOD multiplies interval by easiness factor`() {
        val stateAfterSecond = Sm2State(repetitions = 2, easinessFactor = 2.5, intervalDays = 6)
        val result = Sm2Algorithm.calculateNextReview(
            currentState = stateAfterSecond,
            quality = Sm2Quality.GOOD,
            todayDayString = "2026-10-26"
        )

        assertEquals(3, result.nextState.repetitions)
        assertEquals(15, result.nextState.intervalDays) // 6 * 2.5 = 15
        assertEquals(Stage.MONTHLY, result.mappedStage)
    }

    @Test
    fun `review with AGAIN resets repetitions to 0 and interval to 1`() {
        val matureState = Sm2State(repetitions = 4, easinessFactor = 2.5, intervalDays = 38)
        val result = Sm2Algorithm.calculateNextReview(
            currentState = matureState,
            quality = Sm2Quality.AGAIN,
            todayDayString = "2026-10-19"
        )

        assertEquals(0, result.nextState.repetitions)
        assertEquals(1, result.nextState.intervalDays)
        assertTrue(result.nextState.easinessFactor < 2.5)
        assertTrue(result.nextState.easinessFactor >= Sm2Algorithm.MIN_EASINESS_FACTOR)
        assertEquals(Stage.DAILY, result.mappedStage)
    }

    @Test
    fun `easiness factor never drops below 1_3`() {
        var state = Sm2State(repetitions = 0, easinessFactor = 1.35, intervalDays = 1)
        repeat(5) {
            state = Sm2Algorithm.calculateNextReview(state, Sm2Quality.AGAIN, "2026-10-19").nextState
        }
        assertEquals(Sm2Algorithm.MIN_EASINESS_FACTOR, state.easinessFactor, 0.001)
    }
}
