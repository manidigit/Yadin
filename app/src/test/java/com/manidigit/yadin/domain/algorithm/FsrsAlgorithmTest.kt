package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FsrsAlgorithmTest {

    @Test
    fun `initial review initializes stability and difficulty and increases interval by rating`() {
        val initial = FsrsState()
        val again = FsrsAlgorithm.calculateNextReview(initial, FsrsRating.AGAIN, null, "2026-10-19", 0.90)
        val hard = FsrsAlgorithm.calculateNextReview(initial, FsrsRating.HARD, null, "2026-10-19", 0.90)
        val good = FsrsAlgorithm.calculateNextReview(initial, FsrsRating.GOOD, null, "2026-10-19", 0.90)
        val easy = FsrsAlgorithm.calculateNextReview(initial, FsrsRating.EASY, null, "2026-10-19", 0.90)

        assertTrue(again.nextState.stability < hard.nextState.stability)
        assertTrue(hard.nextState.stability < good.nextState.stability)
        assertTrue(good.nextState.stability < easy.nextState.stability)

        // Difficulty is strictly decreasing across ratings: AGAIN > HARD > GOOD > EASY (centered at 5.0 for GOOD)
        assertTrue(again.nextState.difficulty > hard.nextState.difficulty)
        assertTrue(hard.nextState.difficulty > good.nextState.difficulty)
        assertTrue(good.nextState.difficulty > easy.nextState.difficulty)
        assertEquals(5.0, good.nextState.difficulty, 0.01)
        assertEquals(1, again.intervalDays)
        assertTrue(easy.intervalDays >= good.intervalDays)
    }

    @Test
    fun `second review after initial AGAIN is treated as learning review rather than brand new card`() {
        val firstAgain = FsrsAlgorithm.calculateNextReview(
            currentState = FsrsState(),
            rating = FsrsRating.AGAIN,
            lastReviewedDay = null,
            todayDayString = "2026-10-19",
            desiredRetention = 0.90
        )
        assertEquals(FsrsCardState.LEARNING.code, firstAgain.nextState.state)
        assertEquals(1, firstAgain.nextState.lapses)
        assertEquals(0, firstAgain.nextState.reps)

        val secondGood = FsrsAlgorithm.calculateNextReview(
            currentState = firstAgain.nextState,
            rating = FsrsRating.GOOD,
            lastReviewedDay = firstAgain.lastReviewedDay,
            todayDayString = firstAgain.nextReviewDay,
            desiredRetention = 0.90
        )
        assertEquals(1, secondGood.nextState.reps)
        assertEquals(1, secondGood.nextState.lapses)
        // Difficulty should retain memory of the initial AGAIN (remain > 5.0) rather than resetting to D0(GOOD)=5.0
        assertTrue(secondGood.nextState.difficulty > 5.0)
    }

    @Test
    fun `subsequent recall review increases stability and maps stage properly`() {
        val firstGood = FsrsAlgorithm.calculateNextReview(
            currentState = FsrsState(),
            rating = FsrsRating.GOOD,
            lastReviewedDay = null,
            todayDayString = "2026-10-19",
            desiredRetention = 0.90
        )

        val secondGood = FsrsAlgorithm.calculateNextReview(
            currentState = firstGood.nextState,
            rating = FsrsRating.GOOD,
            lastReviewedDay = firstGood.lastReviewedDay,
            todayDayString = firstGood.nextReviewDay,
            desiredRetention = 0.90
        )

        assertTrue(secondGood.nextState.stability > firstGood.nextState.stability)
        assertTrue(secondGood.intervalDays > firstGood.intervalDays)
        assertEquals(2, secondGood.nextState.reps)
        assertEquals(0, secondGood.nextState.lapses)
    }

    @Test
    fun `lapse with AGAIN increments lapses and sets state to RELEARNING`() {
        val reviewedState = FsrsState(
            stability = 12.0,
            difficulty = 5.0,
            reps = 3,
            lapses = 0,
            state = FsrsCardState.REVIEW.code
        )

        val afterLapse = FsrsAlgorithm.calculateNextReview(
            currentState = reviewedState,
            rating = FsrsRating.AGAIN,
            lastReviewedDay = "2026-10-07",
            todayDayString = "2026-10-19",
            desiredRetention = 0.90
        )

        assertEquals(1, afterLapse.nextState.lapses)
        assertEquals(FsrsCardState.RELEARNING.code, afterLapse.nextState.state)
        assertEquals(1, afterLapse.intervalDays)
        assertEquals(Stage.DAILY, afterLapse.mappedStage)
        assertTrue(afterLapse.nextState.stability < reviewedState.stability)
    }

    @Test
    fun `higher desired retention produces shorter or equal intervals`() {
        val state = FsrsState(
            stability = 15.0,
            difficulty = 5.0,
            reps = 4,
            lapses = 0,
            state = FsrsCardState.REVIEW.code
        )

        val res85 = FsrsAlgorithm.calculateNextReview(state, FsrsRating.GOOD, "2026-10-04", "2026-10-19", 0.85)
        val res90 = FsrsAlgorithm.calculateNextReview(state, FsrsRating.GOOD, "2026-10-04", "2026-10-19", 0.90)
        val res95 = FsrsAlgorithm.calculateNextReview(state, FsrsRating.GOOD, "2026-10-04", "2026-10-19", 0.95)

        assertTrue(res85.intervalDays >= res90.intervalDays)
        assertTrue(res90.intervalDays >= res95.intervalDays)
    }

    @Test
    fun `previewIntervals returns monotonically non-decreasing intervals across ratings`() {
        val state = FsrsState(
            stability = 8.0,
            difficulty = 5.5,
            reps = 2,
            lapses = 0,
            state = FsrsCardState.REVIEW.code
        )
        val previews = FsrsAlgorithm.previewIntervals(
            currentState = state,
            lastReviewedDay = "2026-10-11",
            todayDayString = "2026-10-19",
            desiredRetention = 0.90
        )

        assertEquals(1, previews.againDays)
        assertTrue(previews.hardDays <= previews.goodDays)
        assertTrue(previews.goodDays < previews.easyDays)

        // Verify previewIntervals matches calculateNextReview 100%
        val actualAgain = FsrsAlgorithm.calculateNextReview(state, FsrsRating.AGAIN, "2026-10-11", "2026-10-19", 0.90).intervalDays
        val actualHard = FsrsAlgorithm.calculateNextReview(state, FsrsRating.HARD, "2026-10-11", "2026-10-19", 0.90).intervalDays
        val actualGood = FsrsAlgorithm.calculateNextReview(state, FsrsRating.GOOD, "2026-10-11", "2026-10-19", 0.90).intervalDays
        val actualEasy = FsrsAlgorithm.calculateNextReview(state, FsrsRating.EASY, "2026-10-11", "2026-10-19", 0.90).intervalDays

        assertEquals(previews.againDays, actualAgain)
        assertEquals(previews.hardDays, actualHard)
        assertEquals(previews.goodDays, actualGood)
        assertEquals(previews.easyDays, actualEasy)
    }

    @Test
    fun `resolveEffectiveState seeds FSRS state from existing stage or SM2 interval when switching algorithms`() {
        val uninitializedFsrs = FsrsState()
        val seededFromMonthly = FsrsAlgorithm.resolveEffectiveState(
            state = uninitializedFsrs,
            stage = Stage.MONTHLY,
            lastReviewedDay = "2026-10-01",
            sm2IntervalDays = 0,
            sm2Repetitions = 0
        )
        assertEquals(30.0, seededFromMonthly.stability, 0.01)
        assertEquals(3, seededFromMonthly.reps)
        assertEquals(FsrsCardState.REVIEW.code, seededFromMonthly.state)
    }
}
