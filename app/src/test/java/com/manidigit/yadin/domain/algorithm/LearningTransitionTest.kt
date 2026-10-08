package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LearningTransitionTest {

    private val today = "2026-10-05"

    @Test
    fun daily_correct_transitionsToWeekly() {
        val result = LearningTransition.calculateNextStage(Stage.DAILY, isCorrect = true, todayDayString = today)
        assertEquals(Stage.WEEKLY, result.newStage)
        assertEquals("2026-10-12", result.nextReviewDay) // +7 days
    }

    @Test
    fun daily_wrong_staysInDailyNextDay() {
        val result = LearningTransition.calculateNextStage(Stage.DAILY, isCorrect = false, todayDayString = today)
        assertEquals(Stage.DAILY, result.newStage)
        assertEquals("2026-10-06", result.nextReviewDay) // +1 day
    }

    @Test
    fun weekly_correct_transitionsToMonthly() {
        val result = LearningTransition.calculateNextStage(Stage.WEEKLY, isCorrect = true, todayDayString = today)
        assertEquals(Stage.MONTHLY, result.newStage)
        assertEquals("2026-11-04", result.nextReviewDay) // +30 days
    }

    @Test
    fun weekly_wrong_fallsBackToDaily() {
        val result = LearningTransition.calculateNextStage(Stage.WEEKLY, isCorrect = false, todayDayString = today)
        assertEquals(Stage.DAILY, result.newStage)
        assertEquals("2026-10-06", result.nextReviewDay) // +1 day
    }

    @Test
    fun monthly_correct_transitionsToLearned() {
        val result = LearningTransition.calculateNextStage(Stage.MONTHLY, isCorrect = true, todayDayString = today)
        assertEquals(Stage.LEARNED, result.newStage)
        assertNull(result.nextReviewDay)
    }

    @Test
    fun monthly_wrong_fallsBackToDaily() {
        val result = LearningTransition.calculateNextStage(Stage.MONTHLY, isCorrect = false, todayDayString = today)
        assertEquals(Stage.DAILY, result.newStage)
        assertEquals("2026-10-06", result.nextReviewDay) // +1 day
    }

    @Test
    fun learned_correct_remainsLearned() {
        val result = LearningTransition.calculateNextStage(Stage.LEARNED, isCorrect = true, todayDayString = today)
        assertEquals(Stage.LEARNED, result.newStage)
        assertNull(result.nextReviewDay)
    }

    @Test
    fun learned_wrong_fallsBackToDaily() {
        val result = LearningTransition.calculateNextStage(Stage.LEARNED, isCorrect = false, todayDayString = today)
        assertEquals(Stage.DAILY, result.newStage)
        assertEquals("2026-10-06", result.nextReviewDay) // +1 day
    }
}
