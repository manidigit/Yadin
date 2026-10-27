package com.manidigit.yadin.ui.theme

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * قرارداد استاندارد و مستقل هر تم در سامانه یادین (Theme Specification Contract).
 *
 * هر تم جدید برای اضافه شدن به برنامه تنها کافی است این رابط را پیاده‌سازی کرده
 * و در [YadinThemeRegistry] ثبت شود تا بدون تغییر در صفحات، تمامی رنگ‌ها، ابعاد،
 * اشکال هندسی، سایزها، تایپوگرافی و آیکون‌های برنامه به صورت یکپارچه تغییر کنند.
 */
@Immutable
interface YadinThemeSpec {
    /** شناسه یکتای تم (مانند `"gemini"`) */
    val id: String

    /** عنوان فارسی تم جهت نمایش در رابط کاربری */
    val titleFa: String

    /** عنوان انگلیسی تم */
    val titleEn: String
        get() = titleFa

    /** توضیحات فارسی ویژگی‌های بصری تم */
    val descriptionFa: String

    /** توضیحات انگلیسی ویژگی‌های بصری تم */
    val descriptionEn: String
        get() = descriptionFa

    /** کلید اختیاری رشته عنوان در منابع چندزبانه (strings.xml) */
    @get:StringRes
    val titleRes: Int?
        get() = null

    /** کلید اختیاری رشته توضیحات در منابع چندزبانه (strings.xml) */
    @get:StringRes
    val descriptionRes: Int?
        get() = null

    /** پالت رنگی حالت روشن (Light Mode) */
    val LightColors: YadinColors

    /** پالت رنگی حالت تاریک (Dark Mode) */
    val DarkColors: YadinColors

    /** توکن‌های ابعاد، فاصله‌گذاری و ارتفاع المان‌ها */
    val Dimensions: YadinDimensions

    /** توکن‌های انحنای گوشه‌ها و اشکال هندسی */
    val Shapes: YadinShapes

    /** توکن‌های سایز استاندارد آیکون‌ها */
    val IconSizes: YadinIconSizes

    /** مجموعه آیکون‌های اختصاصی تم (پیش‌فرض: [DefaultYadinIconSet]) */
    val Icons: YadinIconSet
        get() = DefaultYadinIconSet

    /** تایپوگرافی اختصاصی تم (پیش‌فرض: [AppTypography]) */
    val Typography: Typography
        get() = AppTypography

    /** بازگرداندن پالت رنگی متناسب با وضعیت شب/روز */
    fun colors(isDark: Boolean): YadinColors = if (isDark) DarkColors else LightColors

    /** بازگرداندن عنوان تم متناسب با زبان رابط کاربری */
    fun localizedTitle(uiLanguage: String = "fa"): String =
        if (uiLanguage.lowercase().startsWith("en")) titleEn else titleFa

    /** بازگرداندن توضیحات تم متناسب با زبان رابط کاربری */
    fun localizedDescription(uiLanguage: String = "fa"): String =
        if (uiLanguage.lowercase().startsWith("en")) descriptionEn else descriptionFa

    /** تولید ColorScheme استاندارد Material 3 از روی توکن‌های رنگی تم */
    fun materialColorScheme(isDark: Boolean): ColorScheme {
        val c = colors(isDark)
        return if (isDark) {
            darkColorScheme(
                primary = c.primary,
                onPrimary = c.onPrimary,
                secondary = c.secondary,
                onSecondary = c.onSecondary,
                background = c.background,
                surface = c.surface,
                surfaceVariant = c.surfaceVariant,
                onSurface = c.onSurface,
                onSurfaceVariant = c.onSurfaceVariant,
                outline = c.outline,
                outlineVariant = c.outlineVariant,
                error = c.error,
                onError = c.onError
            )
        } else {
            lightColorScheme(
                primary = c.primary,
                onPrimary = c.onPrimary,
                secondary = c.secondary,
                onSecondary = c.onSecondary,
                background = c.background,
                surface = c.surface,
                surfaceVariant = c.surfaceVariant,
                onSurface = c.onSurface,
                onSurfaceVariant = c.onSurfaceVariant,
                outline = c.outline,
                outlineVariant = c.outlineVariant,
                error = c.error,
                onError = c.onError
            )
        }
    }

    /** تولید Shapes استاندارد Material 3 از روی اشکال هندسی تم */
    fun materialShapes(): Shapes = Shapes(
        extraSmall = Shapes.extraSmall,
        small = Shapes.small,
        medium = Shapes.medium,
        large = Shapes.large,
        extraLarge = Shapes.pill
    )
}

/**
 * رجیستری مرکزی و پویا جهت مدیریت و بازیابی تم‌های برنامه ([YadinThemeSpec]).
 *
 * هر تم جدید کافی است در این مخزن ثبت شود تا در سراسر برنامه و صفحه تنظیمات در دسترس قرار گیرد.
 */
object YadinThemeRegistry {
    private val registeredThemes = linkedMapOf<String, YadinThemeSpec>(
        GeminiTheme.id.lowercase() to GeminiTheme
    )

    /** تم پیش‌فرض برنامه */
    val defaultTheme: YadinThemeSpec = GeminiTheme

    /**
     * ثبت یک تم جدید در سیستم تمینگ برنامه.
     */
    fun register(theme: YadinThemeSpec) {
        registeredThemes[theme.id.trim().lowercase()] = theme
    }

    /**
     * دریافت مشخصات تم بر اساس شناسه (`themeId`).
     * در صورت نامعتبر بودن یا یافت نشدن شناسه، تم پیش‌فرض ([defaultTheme]) بازگردانده می‌شود.
     */
    fun getTheme(themeId: String?): YadinThemeSpec {
        if (themeId.isNullOrBlank()) return defaultTheme
        return registeredThemes[themeId.trim().lowercase()] ?: defaultTheme
    }

    /**
     * فهرست تمامی تم‌های ثبت‌شده در برنامه.
     */
    fun allThemes(): List<YadinThemeSpec> = registeredThemes.values.toList()
}

/**
 * شمارشگر تم‌های استاندارد برنامه (حفظ سازگاری کامل با کدهای موجود).
 */
enum class AppTheme(
    val id: String,
    val titleFa: String,
    val descriptionFa: String,
    val spec: YadinThemeSpec
) {
    GEMINI(
        id = GeminiTheme.id,
        titleFa = GeminiTheme.titleFa,
        descriptionFa = GeminiTheme.descriptionFa,
        spec = GeminiTheme
    );

    companion object {
        fun fromId(id: String?): YadinThemeSpec = YadinThemeRegistry.getTheme(id)
    }
}

@Immutable
data class YadinColors(
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val card: Color,
    val outline: Color,
    val outlineVariant: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val error: Color,
    val onError: Color,
    val info: Color,
    val onInfo: Color,
    val accentPurple: Color,
    val difficultyEasy: Color = success,
    val difficultyMedium: Color = warning,
    val difficultyHard: Color = YadinPalette.HardOrange,
    val difficultyVeryHard: Color = error,
    val tierBronze: Color = YadinPalette.TierBronze,
    val tierSilver: Color = YadinPalette.TierSilver,
    val tierGold: Color = YadinPalette.TierGold,
    val tierPlatinum: Color = YadinPalette.TierPlatinum,
    val heroGradient: Brush,
    val cardBorderBrush: Brush?,
    val accentGlow: Color,
    val onHero: Color = Color(0xFFFFFFFF)
)

@Immutable
data class YadinDimensions(
    val screenPadding: Dp,
    val contentGap: Dp,
    val sectionGap: Dp,
    val itemGapSmall: Dp = 6.dp,
    val itemGapMedium: Dp = 8.dp,
    val itemGapLarge: Dp = 12.dp,
    val statCardHeight: Dp,
    val navHeight: Dp,
    val buttonHeight: Dp = 48.dp,
    val chipHeight: Dp = 36.dp,
    val cardElevation: Dp,
    val cardBorderAlpha: Float,
    val cornerExtraSmall: Dp = 8.dp,
    val cornerSmall: Dp,
    val cornerBadge: Dp = 12.dp,
    val cornerMedium: Dp,
    val cornerLarge: Dp,
    val cornerPill: Dp
)

@Immutable
data class YadinShapes(
    val extraSmall: CornerBasedShape = RoundedCornerShape(8.dp),
    val badge: CornerBasedShape = RoundedCornerShape(12.dp),
    val small: CornerBasedShape = RoundedCornerShape(16.dp),
    val medium: CornerBasedShape = RoundedCornerShape(24.dp),
    val large: CornerBasedShape = RoundedCornerShape(32.dp),
    val pill: CornerBasedShape = RoundedCornerShape(40.dp),
    val circle: Shape = CircleShape
)

@Immutable
data class YadinIconSizes(
    val small: Dp = 16.dp,
    val medium: Dp = 20.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val hero: Dp = 48.dp
)

val LocalYadinThemeSpec = staticCompositionLocalOf<YadinThemeSpec> { YadinThemeRegistry.defaultTheme }
val LocalYadinColors = staticCompositionLocalOf { YadinThemeRegistry.defaultTheme.DarkColors }
val LocalYadinDimensions = staticCompositionLocalOf { YadinThemeRegistry.defaultTheme.Dimensions }
val LocalYadinShapes = staticCompositionLocalOf { YadinThemeRegistry.defaultTheme.Shapes }
val LocalYadinIconSizes = staticCompositionLocalOf { YadinThemeRegistry.defaultTheme.IconSizes }
val LocalYadinIcons = staticCompositionLocalOf<YadinIconSet> { YadinThemeRegistry.defaultTheme.Icons }

/**
 * دسترسی مستقیم، تایپ‌شده و سراسری به توکن‌های تم جاری:
 * - `YadinTheme.spec`
 * - `YadinTheme.colors`
 * - `YadinTheme.dimensions`
 * - `YadinTheme.shapes`
 * - `YadinTheme.iconSizes`
 * - `YadinTheme.icons`
 * - `YadinTheme.typography`
 */
object YadinTheme {
    val spec: YadinThemeSpec
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinThemeSpec.current

    val colors: YadinColors
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinColors.current

    val dimensions: YadinDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinDimensions.current

    val shapes: YadinShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinShapes.current

    val iconSizes: YadinIconSizes
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinIconSizes.current

    val icons: YadinIconSet
        @Composable
        @ReadOnlyComposable
        get() = LocalYadinIcons.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}
