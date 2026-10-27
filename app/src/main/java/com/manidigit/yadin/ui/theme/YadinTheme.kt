package com.manidigit.yadin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * نقطه ورود مرکزی و استاندارد سیستم تمینگ یادین.
 *
 * این کامپوزبل بر اساس شناسه [themeId]، مشخصات کامل تم ([YadinThemeSpec]) را از
 * [YadinThemeRegistry] بازیابی کرده و رنگ‌ها ([YadinColors])، ابعاد ([YadinDimensions])،
 * اشکال ([YadinShapes])، سایز آیکون‌ها ([YadinIconSizes])، مجموعه آیکون‌ها ([YadinIconSet])
 * و تایپوگرافی اختصاصی آن تم را به کل درخت رابط کاربری و Material 3 تزریق می‌کند.
 */
@Composable
fun YadinTheme(
    themeId: String = YadinThemeRegistry.defaultTheme.id,
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeSpec = remember(themeId) {
        YadinThemeRegistry.getTheme(themeId)
    }

    val colors = remember(themeSpec, isDark) { themeSpec.colors(isDark) }
    val dimensions = themeSpec.Dimensions
    val shapes = themeSpec.Shapes
    val iconSizes = themeSpec.IconSizes
    val icons = themeSpec.Icons
    val typography = themeSpec.Typography

    val materialColors = remember(themeSpec, isDark) { themeSpec.materialColorScheme(isDark) }
    val materialShapes = remember(themeSpec) { themeSpec.materialShapes() }

    // همگام‌سازی فوری پل استاتیک آیکون‌ها با مجموعه آیکون تم فعال
    YadinIcons.setActiveIconSet(icons)
    SideEffect {
        YadinIcons.setActiveIconSet(icons)
    }

    CompositionLocalProvider(
        LocalYadinThemeSpec provides themeSpec,
        LocalYadinColors provides colors,
        LocalYadinDimensions provides dimensions,
        LocalYadinShapes provides shapes,
        LocalYadinIconSizes provides iconSizes,
        LocalYadinIcons provides icons
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = typography,
            shapes = materialShapes,
            content = content
        )
    }
}
