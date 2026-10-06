package com.manidigit.yadin.domain.time

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object ClockAndDayMath {

    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun now(): Long = System.currentTimeMillis()

    fun todayDayString(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }

    fun addDays(dayString: String, daysToAdd: Int): String {
        return try {
            val date = LocalDate.parse(dayString, DATE_FORMATTER)
            date.plusDays(daysToAdd.toLong()).format(DATE_FORMATTER)
        } catch (_: Exception) {
            dayString
        }
    }

    fun isDue(nextReviewDay: String?, today: String = todayDayString()): Boolean {
        if (nextReviewDay.isNullOrBlank()) return false
        return nextReviewDay <= today
    }

    fun daysBetween(day1: String, day2: String): Int {
        return try {
            val d1 = LocalDate.parse(day1, DATE_FORMATTER)
            val d2 = LocalDate.parse(day2, DATE_FORMATTER)
            ChronoUnit.DAYS.between(d1, d2).toInt()
        } catch (_: Exception) {
            0
        }
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

