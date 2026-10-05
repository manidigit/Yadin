package com.manidigit.yadin.domain.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClockAndDayMathTest {

    @Test
    fun testAddDays() {
        val today = "2026-10-05"
        val nextWeek = ClockAndDayMath.addDays(today, 7)
        assertEquals("2026-10-12", nextWeek)

        val yesterday = ClockAndDayMath.addDays(today, -1)
        assertEquals("2026-10-04", yesterday)
    }

    @Test
    fun testIsDue() {
        val today = "2026-10-05"
        assertTrue(ClockAndDayMath.isDue("2026-10-04", today))
        assertTrue(ClockAndDayMath.isDue("2026-10-05", today))
        assertFalse(ClockAndDayMath.isDue("2026-10-06", today))
        assertFalse(ClockAndDayMath.isDue(null, today))
        assertFalse(ClockAndDayMath.isDue("", today))
    }

    @Test
    fun testDaysBetween() {
        val day1 = "2026-10-01"
        val day2 = "2026-10-05"
        assertEquals(4, ClockAndDayMath.daysBetween(day1, day2))
    }

    @Test
    fun testCalculateStreakDays() {
        val today = "2026-10-05"
        val consecutiveDays = listOf("2026-10-05", "2026-10-04", "2026-10-03")
        assertEquals(3, ClockAndDayMath.calculateStreakDays(consecutiveDays, today))

        val brokenStreak = listOf("2026-10-05", "2026-10-03")
        assertEquals(1, ClockAndDayMath.calculateStreakDays(brokenStreak, today))

        val emptyDays = emptyList<String>()
        assertEquals(0, ClockAndDayMath.calculateStreakDays(emptyDays, today))
    }
}
