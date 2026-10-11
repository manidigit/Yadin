package com.manidigit.yadin.ui.util

/**
 * ابزار تبدیل ارقام انگلیسی به فارسی جهت یکدست‌سازی نمایش اعداد در رابط کاربری برنامه یادین.
 *
 * این تابع افزوده (Extension Function) تمامی کاراکترهای عددی انگلیسی (0 تا 9) موجود در هر شیء
 * را به معادل ارقام فارسی (۰ تا ۹) تبدیل می‌کند تا هماهنگی کامل بصری در متون، شمارنده‌ها،
 * درصدها و نمودارها حفظ شود.
 *
 * @return رشته حاصل با ارقام فارسی یا رشته خالی در صورت null بودن شیء
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
