package com.manidigit.yadin.ui.util

import java.util.Locale

/**
 * ابزار تبدیل ارقام انگلیسی به فارسی جهت یکدست‌سازی نمایش اعداد در رابط کاربری فارسی برنامه یادین.
 */
fun Any?.toPersianDigits(): String {
    if (this == null) return ""
    return this.toString()
        .replace('0', '۰')
        .replace('1', '۱')
        .replace('2', '۲')
        .replace('3', '۳')
        .replace('4', '۴')
        .replace('5', '۵')
        .replace('6', '۶')
        .replace('7', '۷')
        .replace('8', '۸')
        .replace('9', '۹')
}

/**
 * نمایش هوشمند و محلی‌سازی‌شده اعداد متناسب با زبان فعال رابط کاربری (انگلیسی یا فارسی).
 */
fun Any?.formatDigits(): String {
    if (this == null) return ""
    val isEn = try {
        Locale.getDefault().language == "en"
    } catch (_: Exception) {
        false
    }
    return if (isEn) this.toString() else this.toPersianDigits()
}
