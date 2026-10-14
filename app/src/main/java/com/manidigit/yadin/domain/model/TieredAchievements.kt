package com.manidigit.yadin.domain.model

import androidx.annotation.StringRes
import com.manidigit.yadin.R

enum class AchievementCategory(@StringRes val titleRes: Int) {
    VOCABULARY(R.string.ach_cat_vocabulary),
    STREAK(R.string.ach_cat_streak),
    RETENTION(R.string.ach_cat_retention),
    HARD_WORDS(R.string.ach_cat_hard_words),
    QUIZ(R.string.ach_cat_quiz)
}

enum class AchievementTier(
    @StringRes val titleRes: Int,
    @StringRes val badgeLabelRes: Int,
    val rank: Int,
    val iconName: String
) {
    BRONZE(R.string.tier_bronze_title, R.string.progress_tier_bronze, 1, "workspace_premium"),
    SILVER(R.string.tier_silver_title, R.string.progress_tier_silver, 2, "military_tech"),
    GOLD(R.string.tier_gold_title, R.string.progress_tier_gold, 3, "emoji_events"),
    PLATINUM(R.string.tier_platinum_title, R.string.progress_tier_platinum, 4, "diamond")
}

data class TieredGroupState(
    val category: AchievementCategory,
    val currentTier: AchievementTier?, // null if bronze not yet unlocked
    val nextTier: AchievementTier?,    // null if maxed out (platinum unlocked)
    val displayAchievement: AchievementId, // the achievement to show (current locked or current highest unlocked)
    val currentProgress: Int,
    val targetThreshold: Int,
    val isMaxLevel: Boolean
)
