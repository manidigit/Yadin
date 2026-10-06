package com.manidigit.yadin.data.repository

import com.manidigit.yadin.data.local.dao.SettingsDao
import com.manidigit.yadin.data.local.entity.SettingEntity
import com.manidigit.yadin.domain.model.CardDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDao: SettingsDao) {

    val themeIdFlow: Flow<String> = settingsDao.getSettingFlow("themeId")
        .map { it ?: "gtp" }

    val isDarkFlow: Flow<Boolean> = settingsDao.getSettingFlow("isDark")
        .map { it?.toBooleanStrictOrNull() ?: true }

    val uiLanguageFlow: Flow<String> = settingsDao.getSettingFlow("uiLanguage")
        .map { it ?: "fa" }

    val activePairFlow: Flow<String> = settingsDao.getSettingFlow("activePair")
        .map { it ?: "es-fa" }

    val appLanguageDirectionFlow: Flow<CardDirection> = settingsDao.getSettingFlow("appLanguageDirection")
        .map { if (it == "REVERSE") CardDirection.REVERSE else CardDirection.NORMAL }

    val difficultyThresholdFlow: Flow<Int> = settingsDao.getSettingFlow("difficultyThreshold")
        .map { it?.toIntOrNull()?.coerceIn(1, 5) ?: 3 }

    val showCategoryInReviewFlow: Flow<Boolean> = settingsDao.getSettingFlow("showCategoryInReview")
        .map { it?.toBooleanStrictOrNull() ?: false }

    suspend fun setThemeId(themeId: String) {
        val sanitized = when (themeId.lowercase()) {
            "googoli" -> "googoli"
            "gemini" -> "gemini"
            "claude" -> "claude"
            else -> "gtp"
        }
        settingsDao.setSetting(SettingEntity("themeId", sanitized))
    }

    suspend fun setDarkMode(isDark: Boolean) {
        settingsDao.setSetting(SettingEntity("isDark", isDark.toString()))
    }

    suspend fun setUiLanguage(language: String) {
        settingsDao.setSetting(SettingEntity("uiLanguage", language))
    }

    suspend fun setActivePair(pair: String) {
        settingsDao.setSetting(SettingEntity("activePair", pair))
    }

    suspend fun setAppLanguageDirection(direction: CardDirection) {
        settingsDao.setSetting(SettingEntity("appLanguageDirection", direction.name))
        val pair = if (direction == CardDirection.NORMAL) "es-fa" else "fa-es"
        settingsDao.setSetting(SettingEntity("activePair", pair))
    }

    suspend fun setDifficultyThreshold(threshold: Int) {
        val clamped = threshold.coerceIn(1, 5)
        settingsDao.setSetting(SettingEntity("difficultyThreshold", clamped.toString()))
    }

    suspend fun setShowCategoryInReview(show: Boolean) {
        settingsDao.setSetting(SettingEntity("showCategoryInReview", show.toString()))
    }
}
