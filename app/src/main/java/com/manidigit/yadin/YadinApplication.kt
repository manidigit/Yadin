package com.manidigit.yadin

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

class YadinApplication : Application() {
    override fun attachBaseContext(base: Context) {
        val prefs = base.getSharedPreferences("yadin_settings", Context.MODE_PRIVATE)
        val uiLanguage = prefs.getString("ui_language", "fa") ?: "fa"
        val targetLocale = if (uiLanguage == "en") Locale("en", "US") else Locale("fa", "IR")
        Locale.setDefault(targetLocale)

        val config = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val list = LocaleList(targetLocale)
            LocaleList.setDefault(list)
            config.setLocales(list)
        } else {
            config.setLocale(targetLocale)
        }
        config.setLayoutDirection(targetLocale)
        @Suppress("DEPRECATION")
        base.resources.updateConfiguration(config, base.resources.displayMetrics)
        val localizedContext = base.createConfigurationContext(config)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("yadin_settings", Context.MODE_PRIVATE)
        val uiLanguage = prefs.getString("ui_language", "fa") ?: "fa"
        val targetLocale = if (uiLanguage == "en") Locale("en", "US") else Locale("fa", "IR")
        Locale.setDefault(targetLocale)
    }
}
