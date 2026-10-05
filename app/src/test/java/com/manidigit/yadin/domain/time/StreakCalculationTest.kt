package com.manidigit.yadin.domain.time

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculationTest {

    private val today = "2026-10-05"

    @Test
    fun emptyList_returnsZero() {
        val streak = ClockAndDayMath.calculateStreakDays(emptyList(), today)
        assertEquals(0, streak)
    }

    @Test
    fun practicedTodayOnly_returnsOne() {
        val streak = ClockAndDayMath.calculateStreakDays(listOf("2026-10-05"), today)
        assertEquals(1, streak)
    }

    @Test
    fun practicedYesterdayOnly_returnsOne() {
        val streak = ClockAndDayMath.calculateStreakDays(listOf("2026-10-04"), today)
        assertEquals(1, streak)
    }

    @Test
    fun threeConsecutiveDaysIncludingToday_returnsThree() {
        val days = listOf("2026-10-03", "2026-10-04", "2026-10-05")
        val streak = ClockAndDayMath.calculateStreakDays(days, today)
        assertEquals(3, streak)
    }

    @Test
    fun threeDistinctDaysWithGaps_returnsCorrectStreakNotTotalDistinct() {
        // Gap between 10-01 and 10-04, 10-05
        val days = listOf("2026-10-01", "2026-10-04", "2026-10-05")
        val streak = ClockAndDayMath.calculateStreakDays(days, today)
        // streak should only be 2 (10-04, 10-05), NOT 3!
        assertEquals(2, streak)
    }

    @Test
    fun lastPracticeTwoDaysAgo_returnsZero() {
        val days = listOf("2026-10-01", "2026-10-02", "2026-10-03")
        val streak = ClockAndDayMath.calculateStreakDays(days, today)
        assertEquals(0, streak)
    }
}
