package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.DuplicatePolicy
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.time.ClockAndDayMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainAlgorithmLogicTest {

    @Test
    fun testClockAndDayMath_StreakCalculation() {
        val today = "2026-10-11"
        val days = listOf("2026-10-11", "2026-10-10", "2026-10-09")
        val streak = ClockAndDayMath.calculateStreakDays(days, today)
        assertEquals(3, streak)
    }

    @Test
    fun testClockAndDayMath_EpochMillisConversion() {
        val dayString = "2026-10-11"
        val millis = ClockAndDayMath.dayToEpochMillis(dayString)
        assertTrue(millis > 0L)
        val reconverted = ClockAndDayMath.epochMillisToDayString(millis)
        assertEquals(dayString, reconverted)
    }

    @Test
    fun testVocabularyParser_BasicParsing() {
        val text = "hola - سلام"
        val result = VocabularyParser.parse(text)
        assertEquals(1, result.entries.size)
        assertEquals("hola", result.entries[0].sourceText)
        assertEquals("سلام", result.entries[0].translations.first())
    }

    @Test
    fun testStageTransition_Logic() {
        val initialStage = Stage.DAILY
        val nextStage = Stage.WEEKLY
        assertTrue(nextStage.ordinal > initialStage.ordinal)
    }
}
