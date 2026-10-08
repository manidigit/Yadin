package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.data.local.entity.AchievementEntity
import com.manidigit.yadin.domain.model.AchievementCategory
import com.manidigit.yadin.domain.model.AchievementId
import com.manidigit.yadin.domain.model.AchievementTier
import com.manidigit.yadin.domain.model.TieredGroupState

object AchievementTierEvaluator {

    fun getTieredGroups(achievements: List<AchievementEntity>): List<TieredGroupState> {
        val achievementMap = achievements.associateBy { it.id }

        return AchievementCategory.values().map { category ->
            val categoryAchievements = AchievementId.values()
                .filter { it.category == category }
                .sortedBy { it.tier.rank }

            // Find highest unlocked tier
            val unlockedTiers = categoryAchievements.filter { ach ->
                val entity = achievementMap[ach.name]
                entity != null && entity.unlockedAt != null
            }

            val highestUnlocked = unlockedTiers.maxByOrNull { it.tier.rank }

            // Find the next target achievement to unlock (first locked tier)
            val nextTarget = categoryAchievements.firstOrNull { ach ->
                val entity = achievementMap[ach.name]
                entity == null || entity.unlockedAt == null
            }

            val isMaxLevel = nextTarget == null && highestUnlocked != null
            val displayAchievement = nextTarget ?: highestUnlocked ?: categoryAchievements.first()

            val progressEntity = achievementMap[displayAchievement.name]
            val progress = progressEntity?.progress ?: 0
            val targetThreshold = displayAchievement.threshold

            TieredGroupState(
                category = category,
                currentTier = highestUnlocked?.tier,
                nextTier = nextTarget?.tier,
                displayAchievement = displayAchievement,
                currentProgress = progress,
                targetThreshold = targetThreshold,
                isMaxLevel = isMaxLevel
            )
        }
    }
}
