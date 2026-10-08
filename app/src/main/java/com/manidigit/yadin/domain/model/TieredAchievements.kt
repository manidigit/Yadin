package com.manidigit.yadin.domain.model

enum class AchievementCategory(val title: String) {
    VOCABULARY("دایره واژگان"),
    STREAK("رگبار و مداومت"),
    RETENTION("تثبیت لایتنر"),
    HARD_WORDS("واژگان سخت"),
    QUIZ("مهارت آزمون")
}

enum class AchievementTier(
    val title: String,
    val rank: Int,
    val iconName: String
) {
    BRONZE("برنز", 1, "workspace_premium"),
    SILVER("نقره", 2, "military_tech"),
    GOLD("طلا", 3, "emoji_events"),
    PLATINUM("پلاتین", 4, "diamond")
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
