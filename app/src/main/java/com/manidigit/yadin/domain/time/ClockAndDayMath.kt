package com.manidigit.yadin.domain.time

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ClockAndDayMath {

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    fun now(): Long = System.currentTimeMillis()

    fun todayDayString(): String {
        return DATE_FORMAT.format(Date(now()))
    }

    fun addDays(dayString: String, daysToAdd: Int): String {
        val cal = Calendar.getInstance()
        cal.time = DATE_FORMAT.parse(dayString) ?: Date()
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return DATE_FORMAT.format(cal.time)
    }

    fun isDue(nextReviewDay: String?, today: String = todayDayString()): Boolean {
        if (nextReviewDay.isNullOrBlank()) return false
        return nextReviewDay <= today
    }

    fun daysBetween(day1: String, day2: String): Int {
        val date1 = DATE_FORMAT.parse(day1) ?: return 0
        val date2 = DATE_FORMAT.parse(day2) ?: return 0
        val diffMillis = date2.time - date1.time
        return (diffMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    fun calculateStreakDays(days: List<String>, today: String = todayDayString()): Int {
        if (days.isEmpty()) return 0
        val sortedDays = days.distinct().sortedDescending()
        var streak = 0
        var expectedDay = if (sortedDays.first() == today) today else addDays(today, -1)
        if (sortedDays.first() != today && sortedDays.first() != expectedDay) {
            return 0
        }
        for (day in sortedDays) {
            if (day == expectedDay) {
                streak++
                expectedDay = addDays(expectedDay, -1)
            } else if (day > expectedDay) {
                continue
            } else {
                break
            }
        }
        return streak
    }
}
