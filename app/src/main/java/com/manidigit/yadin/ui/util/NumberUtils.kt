package com.manidigit.yadin.ui.util

/**
 * تبدیل تمامی ارقام انگلیسی (0-9) موجود در هر مقدار به ارقام فارسی (۰-۹)
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
