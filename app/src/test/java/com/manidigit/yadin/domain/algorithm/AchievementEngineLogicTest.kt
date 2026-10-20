package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.domain.model.AchievementCategory
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.AchievementTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineLogicTest {

    private fun evaluateAchievements(
        streakDays: Int,
        practicedWords: Int,
        learnedWords: Int,
        masteredHardWords: Int,
        perfectQuizzesCount: Int
    ): Set<AchievementId> {
        val unlocked = mutableSetOf<AchievementId>()

        // 1. Streaks (7, 30, 100, 365)
        for (ach in AchievementId.values().filter { it.category == AchievementCategory.STREAK }) {
            if (streakDays >= ach.threshold) unlocked.add(ach)
        }

        // 2. Practiced words (100, 500, 2000, 5000)
        for (ach in AchievementId.values().filter { it.category == AchievementCategory.VOCABULARY }) {
            if (practicedWords >= ach.threshold) unlocked.add(ach)
        }

        // 3. Long-term memory (100, 500, 2000, 5000)
        for (ach in AchievementId.values().filter { it.category == AchievementCategory.RETENTION }) {
            if (learnedWords >= ach.threshold) unlocked.add(ach)
        }

        // 4. Hard master (10, 50, 150, 500)
        for (ach in AchievementId.values().filter { it.category == AchievementCategory.HARD_WORDS }) {
            if (masteredHardWords >= ach.threshold) unlocked.add(ach)
        }

        // 5. Quiz mastery (5, 20, 50, 100 perfect quizzes)
        for (ach in AchievementId.values().filter { it.category == AchievementCategory.QUIZ }) {
            if (perfectQuizzesCount >= ach.threshold) unlocked.add(ach)
        }

        return unlocked
    }

    @Test
    fun `streaks trigger corresponding achievements at 7, 30, 100, and 365 days`() {
        val unlocked6 = evaluateAchievements(6, 0, 0, 0, 0)
        assertFalse(unlocked6.contains(AchievementId.STREAK_3_DAYS))

        val unlocked7 = evaluateAchievements(7, 0, 0, 0, 0)
        assertTrue(unlocked7.contains(AchievementId.STREAK_3_DAYS))
        assertFalse(unlocked7.contains(AchievementId.STREAK_7_DAYS))

        val unlocked30 = evaluateAchievements(30, 0, 0, 0, 0)
        assertTrue(unlocked30.contains(AchievementId.STREAK_3_DAYS))
        assertTrue(unlocked30.contains(AchievementId.STREAK_7_DAYS))
        assertFalse(unlocked30.contains(AchievementId.STREAK_30_DAYS))

        val unlocked100 = evaluateAchievements(100, 0, 0, 0, 0)
        assertTrue(unlocked100.contains(AchievementId.STREAK_30_DAYS))
        assertFalse(unlocked100.contains(AchievementId.STREAK_100_DAYS))

        val unlocked365 = evaluateAchievements(365, 0, 0, 0, 0)
        assertTrue(unlocked365.contains(AchievementId.STREAK_100_DAYS))
    }

    @Test
    fun `practiced words trigger tiers progressively at 100, 500, 2000, 5000`() {
        val u99 = evaluateAchievements(0, 99, 0, 0, 0)
        assertFalse(u99.contains(AchievementId.FIRST_TEN_WORDS))

        val u100 = evaluateAchievements(0, 100, 0, 0, 0)
        assertTrue(u100.contains(AchievementId.FIRST_TEN_WORDS))
        assertFalse(u100.contains(AchievementId.VOCABULARY_BUILDER))

        val u500 = evaluateAchievements(0, 500, 0, 0, 0)
        assertTrue(u500.contains(AchievementId.FIRST_TEN_WORDS))
        assertTrue(u500.contains(AchievementId.VOCABULARY_BUILDER))
        assertFalse(u500.contains(AchievementId.VOCABULARY_MASTER))

        val u1000 = evaluateAchievements(0, 1000, 0, 0, 0)
        assertTrue(u1000.contains(AchievementId.VOCABULARY_BUILDER))
        assertFalse(u1000.contains(AchievementId.VOCABULARY_MASTER))
        assertFalse(u1000.contains(AchievementId.VOCABULARY_LEGEND))

        val u2000 = evaluateAchievements(0, 2000, 0, 0, 0)
        assertTrue(u2000.contains(AchievementId.VOCABULARY_MASTER))
        assertFalse(u2000.contains(AchievementId.VOCABULARY_LEGEND))

        val u5000 = evaluateAchievements(0, 5000, 0, 0, 0)
        assertTrue(u5000.contains(AchievementId.VOCABULARY_LEGEND))
    }

    @Test
    fun `learned retention words trigger tiers at 100, 500, 2000, 5000`() {
        val u99 = evaluateAchievements(0, 0, 99, 0, 0)
        assertFalse(u99.contains(AchievementId.LONG_TERM_MEMORY))

        val u100 = evaluateAchievements(0, 0, 100, 0, 0)
        assertTrue(u100.contains(AchievementId.LONG_TERM_MEMORY))
        assertFalse(u100.contains(AchievementId.RETENTION_SILVER))

        val u500 = evaluateAchievements(0, 0, 500, 0, 0)
        assertTrue(u500.contains(AchievementId.RETENTION_SILVER))
        assertFalse(u500.contains(AchievementId.RETENTION_GOLD))

        val u5000 = evaluateAchievements(0, 0, 5000, 0, 0)
        assertTrue(u5000.contains(AchievementId.RETENTION_PLATINUM))
    }

    @Test
    fun `mastering hard words triggers tiers at 10, 50, 150, 500`() {
        val u9 = evaluateAchievements(0, 0, 0, 9, 0)
        assertFalse(u9.contains(AchievementId.HARD_MASTER))

        val u10 = evaluateAchievements(0, 0, 0, 10, 0)
        assertTrue(u10.contains(AchievementId.HARD_MASTER))
        assertFalse(u10.contains(AchievementId.HARD_SILVER))

        val u50 = evaluateAchievements(0, 0, 0, 50, 0)
        assertTrue(u50.contains(AchievementId.HARD_SILVER))

        val u500 = evaluateAchievements(0, 0, 0, 500, 0)
        assertTrue(u500.contains(AchievementId.HARD_PLATINUM))
    }

    @Test
    fun `perfect quiz sessions count triggers tiers at 5, 20, 50, 100`() {
        val q4 = evaluateAchievements(0, 0, 0, 0, 4)
        assertFalse(q4.contains(AchievementId.QUIZ_ACE))

        val q5 = evaluateAchievements(0, 0, 0, 0, 5)
        assertTrue(q5.contains(AchievementId.QUIZ_ACE))
        assertFalse(q5.contains(AchievementId.QUIZ_SILVER))

        val q20 = evaluateAchievements(0, 0, 0, 0, 20)
        assertTrue(q20.contains(AchievementId.QUIZ_ACE))
        assertTrue(q20.contains(AchievementId.QUIZ_SILVER))
        assertFalse(q20.contains(AchievementId.QUIZ_GOLD))

        val q100 = evaluateAchievements(0, 0, 0, 0, 100)
        assertTrue(q100.contains(AchievementId.QUIZ_PLATINUM))
    }

    @Test
    fun `AchievementTierEvaluator replaces completed tier with next tier`() {
        val achievements = listOf(
            AchievementEntity(id = AchievementId.FIRST_TEN_WORDS.name, unlockedAt = 1000L, progress = 100),
            AchievementEntity(id = AchievementId.VOCABULARY_BUILDER.name, unlockedAt = 2000L, progress = 500),
            AchievementEntity(id = AchievementId.VOCABULARY_MASTER.name, unlockedAt = null, progress = 1000),
            AchievementEntity(id = AchievementId.VOCABULARY_LEGEND.name, unlockedAt = null, progress = 1000)
        )

        val groups = AchievementTierEvaluator.getTieredGroups(achievements)
        val vocabGroup = groups.first { it.category == AchievementCategory.VOCABULARY }

        assertEquals(AchievementTier.SILVER, vocabGroup.currentTier)
        assertEquals(AchievementTier.GOLD, vocabGroup.nextTier)
        assertEquals(AchievementId.VOCABULARY_MASTER, vocabGroup.displayAchievement)
        assertEquals(1000, vocabGroup.currentProgress)
        assertEquals(2000, vocabGroup.targetThreshold)
        assertFalse(vocabGroup.isMaxLevel)
    }

    @Test
    fun `AchievementTierEvaluator handles platinum max level completion`() {
        val achievements = listOf(
            AchievementEntity(id = AchievementId.STREAK_3_DAYS.name, unlockedAt = 1000L, progress = 7),
            AchievementEntity(id = AchievementId.STREAK_7_DAYS.name, unlockedAt = 2000L, progress = 30),
            AchievementEntity(id = AchievementId.STREAK_30_DAYS.name, unlockedAt = 3000L, progress = 100),
            AchievementEntity(id = AchievementId.STREAK_100_DAYS.name, unlockedAt = 4000L, progress = 365)
        )

        val groups = AchievementTierEvaluator.getTieredGroups(achievements)
        val streakGroup = groups.first { it.category == AchievementCategory.STREAK }

        assertEquals(AchievementTier.PLATINUM, streakGroup.currentTier)
        assertNull(streakGroup.nextTier)
        assertTrue(streakGroup.isMaxLevel)
        assertEquals(AchievementId.STREAK_100_DAYS, streakGroup.displayAchievement)
    }
}
