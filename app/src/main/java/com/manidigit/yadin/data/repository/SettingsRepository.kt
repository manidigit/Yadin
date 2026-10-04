package com.manidigit.yadin.data.repository

import com.manidigit.yadin.data.local.dao.SettingsDao
import com.manidigit.yadin.data.local.entity.SettingEntity
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

    suspend fun setThemeId(themeId: String) {
        val sanitized = if (themeId == "gemini") "gemini" else "gtp"
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
}
