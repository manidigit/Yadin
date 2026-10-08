package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.domain.model.AchievementCategory
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.AchievementTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

        // 1. Streaks (3, 7, 30, 100)
        if (streakDays >= 3) unlocked.add(AchievementId.STREAK_3_DAYS)
        if (streakDays >= 7) unlocked.add(AchievementId.STREAK_7_DAYS)
        if (streakDays >= 30) unlocked.add(AchievementId.STREAK_30_DAYS)
        if (streakDays >= 100) unlocked.add(AchievementId.STREAK_100_DAYS)

        // 2. Practiced words (10, 50, 200, 1000)
        if (practicedWords >= 10) unlocked.add(AchievementId.FIRST_TEN_WORDS)
        if (practicedWords >= 50) unlocked.add(AchievementId.VOCABULARY_BUILDER)
        if (practicedWords >= 200) unlocked.add(AchievementId.VOCABULARY_MASTER)
        if (practicedWords >= 1000) unlocked.add(AchievementId.VOCABULARY_LEGEND)

        // 3. Long-term memory (5, 25, 100, 500)
        if (learnedWords >= 5) unlocked.add(AchievementId.LONG_TERM_MEMORY)
        if (learnedWords >= 25) unlocked.add(AchievementId.RETENTION_SILVER)
        if (learnedWords >= 100) unlocked.add(AchievementId.RETENTION_GOLD)
        if (learnedWords >= 500) unlocked.add(AchievementId.RETENTION_PLATINUM)

        // 4. Hard master (3, 10, 30, 100)
        if (masteredHardWords >= 3) unlocked.add(AchievementId.HARD_MASTER)
        if (masteredHardWords >= 10) unlocked.add(AchievementId.HARD_SILVER)
        if (masteredHardWords >= 30) unlocked.add(AchievementId.HARD_GOLD)
        if (masteredHardWords >= 100) unlocked.add(AchievementId.HARD_PLATINUM)

        // 5. Quiz ace (5, 10, 20, 30)
        if (quizCorrectCount == quizQuestionsCount && quizQuestionsCount > 0) {
            if (quizQuestionsCount >= 5) unlocked.add(AchievementId.QUIZ_ACE)
            if (quizQuestionsCount >= 10) unlocked.add(AchievementId.QUIZ_SILVER)
            if (quizQuestionsCount >= 20) unlocked.add(AchievementId.QUIZ_GOLD)
            if (quizQuestionsCount >= 30) unlocked.add(AchievementId.QUIZ_PLATINUM)
        }

        return unlocked
    }

    @Test
    fun `streaks trigger corresponding achievements at 3, 7, 30, and 100 days`() {
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

        val unlocked100 = evaluateAchievements(100, 0, 0, 0, 0, 0)
        assertTrue(unlocked100.contains(AchievementId.STREAK_100_DAYS))
    }

    @Test
    fun `practiced words trigger tiers progressively up to 1000`() {
        val u9 = evaluateAchievements(0, 9, 0, 0, 0, 0)
        assertFalse(u9.contains(AchievementId.FIRST_TEN_WORDS))

        val u10 = evaluateAchievements(0, 10, 0, 0, 0, 0)
        assertTrue(u10.contains(AchievementId.FIRST_TEN_WORDS))
        assertFalse(u10.contains(AchievementId.VOCABULARY_BUILDER))

        val u50 = evaluateAchievements(0, 50, 0, 0, 0, 0)
        assertTrue(u50.contains(AchievementId.FIRST_TEN_WORDS))
        assertTrue(u50.contains(AchievementId.VOCABULARY_BUILDER))
        assertFalse(u50.contains(AchievementId.VOCABULARY_MASTER))

        val u200 = evaluateAchievements(0, 200, 0, 0, 0, 0)
        assertTrue(u200.contains(AchievementId.VOCABULARY_MASTER))
        assertFalse(u200.contains(AchievementId.VOCABULARY_LEGEND))

        val u1000 = evaluateAchievements(0, 1000, 0, 0, 0, 0)
        assertTrue(u1000.contains(AchievementId.VOCABULARY_LEGEND))
    }

    @Test
    fun `learned retention words trigger tiers at 5, 25, 100, 500`() {
        val u4 = evaluateAchievements(0, 0, 4, 0, 0, 0)
        assertFalse(u4.contains(AchievementId.LONG_TERM_MEMORY))

        val u5 = evaluateAchievements(0, 0, 5, 0, 0, 0)
        assertTrue(u5.contains(AchievementId.LONG_TERM_MEMORY))

        val u25 = evaluateAchievements(0, 0, 25, 0, 0, 0)
        assertTrue(u25.contains(AchievementId.RETENTION_SILVER))
    }

    @Test
    fun `mastering hard words triggers tiers at 3, 10, 30, 100`() {
        val u2 = evaluateAchievements(0, 0, 0, 2, 0, 0)
        assertFalse(u2.contains(AchievementId.HARD_MASTER))

        val u3 = evaluateAchievements(0, 0, 0, 3, 0, 0)
        assertTrue(u3.contains(AchievementId.HARD_MASTER))

        val u10 = evaluateAchievements(0, 0, 0, 10, 0, 0)
        assertTrue(u10.contains(AchievementId.HARD_SILVER))
    }

    @Test
    fun `perfect quiz score triggers tiers at 5, 10, 20, 30`() {
        val imperfect = evaluateAchievements(0, 0, 0, 0, 10, 9)
        assertFalse(imperfect.contains(AchievementId.QUIZ_ACE))

        val perfect5 = evaluateAchievements(0, 0, 0, 0, 5, 5)
        assertTrue(perfect5.contains(AchievementId.QUIZ_ACE))
        assertFalse(perfect5.contains(AchievementId.QUIZ_SILVER))

        val perfect10 = evaluateAchievements(0, 0, 0, 0, 10, 10)
        assertTrue(perfect10.contains(AchievementId.QUIZ_ACE))
        assertTrue(perfect10.contains(AchievementId.QUIZ_SILVER))
    }

    @Test
    fun `AchievementTierEvaluator replaces completed tier with next tier`() {
        val achievements = listOf(
            AchievementEntity(id = AchievementId.FIRST_TEN_WORDS.name, unlockedAt = 1000L, progress = 10),
            AchievementEntity(id = AchievementId.VOCABULARY_BUILDER.name, unlockedAt = null, progress = 25)
        )

        val groups = AchievementTierEvaluator.getTieredGroups(achievements)
        val vocabGroup = groups.first { it.category == AchievementCategory.VOCABULARY }

        assertEquals(AchievementTier.BRONZE, vocabGroup.currentTier)
        assertEquals(AchievementTier.SILVER, vocabGroup.nextTier)
        assertEquals(AchievementId.VOCABULARY_BUILDER, vocabGroup.displayAchievement)
        assertEquals(25, vocabGroup.currentProgress)
        assertEquals(50, vocabGroup.targetThreshold)
        assertFalse(vocabGroup.isMaxLevel)
    }

    @Test
    fun `AchievementTierEvaluator handles platinum max level completion`() {
        val achievements = listOf(
            AchievementEntity(id = AchievementId.STREAK_3_DAYS.name, unlockedAt = 1000L, progress = 3),
            AchievementEntity(id = AchievementId.STREAK_7_DAYS.name, unlockedAt = 2000L, progress = 7),
            AchievementEntity(id = AchievementId.STREAK_30_DAYS.name, unlockedAt = 3000L, progress = 30),
            AchievementEntity(id = AchievementId.STREAK_100_DAYS.name, unlockedAt = 4000L, progress = 100)
        )

        val groups = AchievementTierEvaluator.getTieredGroups(achievements)
        val streakGroup = groups.first { it.category == AchievementCategory.STREAK }

        assertEquals(AchievementTier.PLATINUM, streakGroup.currentTier)
        assertNull(streakGroup.nextTier)
        assertTrue(streakGroup.isMaxLevel)
        assertEquals(AchievementId.STREAK_100_DAYS, streakGroup.displayAchievement)
    }
}
