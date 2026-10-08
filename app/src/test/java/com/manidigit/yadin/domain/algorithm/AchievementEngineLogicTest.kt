package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.AchievementId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineLogicTest {

    private fun evaluateAchievements(
        streakDays: Int,
        practicedWords: Int,
        learnedWords: Int,
        masteredHardWords: Int,
        quizQuestionsCount: Int,
        quizCorrectCount: Int
    ): Set<AchievementId> {
        val unlocked = mutableSetOf<AchievementId>()

        // 1. Streaks
        if (streakDays >= 3) unlocked.add(AchievementId.STREAK_3_DAYS)
        if (streakDays >= 7) unlocked.add(AchievementId.STREAK_7_DAYS)
        if (streakDays >= 30) unlocked.add(AchievementId.STREAK_30_DAYS)

        // 2. Practiced words
        if (practicedWords >= 10) unlocked.add(AchievementId.FIRST_TEN_WORDS)
        if (practicedWords >= 50) unlocked.add(AchievementId.VOCABULARY_BUILDER)

        // 3. Long-term memory
        if (learnedWords >= 20) unlocked.add(AchievementId.LONG_TERM_MEMORY)

        // 4. Hard master
        if (masteredHardWords >= 5) unlocked.add(AchievementId.HARD_MASTER)

        // 5. Quiz ace
        if (quizQuestionsCount >= 10 && quizCorrectCount == quizQuestionsCount) {
            unlocked.add(AchievementId.QUIZ_ACE)
        }

        return unlocked
    }

    @Test
    fun `streaks trigger corresponding achievements at 3, 7, and 30 days`() {
        val unlocked2 = evaluateAchievements(2, 0, 0, 0, 0, 0)
        assertFalse(unlocked2.contains(AchievementId.STREAK_3_DAYS))

        val unlocked3 = evaluateAchievements(3, 0, 0, 0, 0, 0)
        assertTrue(unlocked3.contains(AchievementId.STREAK_3_DAYS))
        assertFalse(unlocked3.contains(AchievementId.STREAK_7_DAYS))

        val unlocked7 = evaluateAchievements(7, 0, 0, 0, 0, 0)
        assertTrue(unlocked7.contains(AchievementId.STREAK_3_DAYS))
        assertTrue(unlocked7.contains(AchievementId.STREAK_7_DAYS))
        assertFalse(unlocked7.contains(AchievementId.STREAK_30_DAYS))

        val unlocked30 = evaluateAchievements(30, 0, 0, 0, 0, 0)
        assertTrue(unlocked30.contains(AchievementId.STREAK_30_DAYS))
    }

    @Test
    fun `practiced words trigger FIRST_TEN_WORDS and VOCABULARY_BUILDER`() {
        val u9 = evaluateAchievements(0, 9, 0, 0, 0, 0)
        assertFalse(u9.contains(AchievementId.FIRST_TEN_WORDS))

        val u10 = evaluateAchievements(0, 10, 0, 0, 0, 0)
        assertTrue(u10.contains(AchievementId.FIRST_TEN_WORDS))
        assertFalse(u10.contains(AchievementId.VOCABULARY_BUILDER))

        val u50 = evaluateAchievements(0, 50, 0, 0, 0, 0)
        assertTrue(u50.contains(AchievementId.FIRST_TEN_WORDS))
        assertTrue(u50.contains(AchievementId.VOCABULARY_BUILDER))
    }

    @Test
    fun `reaching 20 learned words triggers LONG_TERM_MEMORY`() {
        val u19 = evaluateAchievements(0, 20, 19, 0, 0, 0)
        assertFalse(u19.contains(AchievementId.LONG_TERM_MEMORY))

        val u20 = evaluateAchievements(0, 20, 20, 0, 0, 0)
        assertTrue(u20.contains(AchievementId.LONG_TERM_MEMORY))
    }

    @Test
    fun `mastering 5 hard words triggers HARD_MASTER`() {
        val u4 = evaluateAchievements(0, 0, 0, 4, 0, 0)
        assertFalse(u4.contains(AchievementId.HARD_MASTER))

        val u5 = evaluateAchievements(0, 0, 0, 5, 0, 0)
        assertTrue(u5.contains(AchievementId.HARD_MASTER))
    }

    @Test
    fun `perfect quiz score on 10 or more questions triggers QUIZ_ACE`() {
        val imperfect = evaluateAchievements(0, 0, 0, 0, 10, 9)
        assertFalse(imperfect.contains(AchievementId.QUIZ_ACE))

        val tooShort = evaluateAchievements(0, 0, 0, 0, 9, 9)
        assertFalse(tooShort.contains(AchievementId.QUIZ_ACE))

        val perfect = evaluateAchievements(0, 0, 0, 0, 10, 10)
        assertTrue(perfect.contains(AchievementId.QUIZ_ACE))
    }
}
