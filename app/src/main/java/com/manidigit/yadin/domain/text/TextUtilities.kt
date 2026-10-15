package com.manidigit.yadin.domain.text

import java.text.Normalizer
import java.util.Locale

object TextUtilities {

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val TRANSLATION_DELIMITERS = Regex("[/،,;]")
    private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val ARABIC_PERSIAN_DIACRITICS_REGEX = Regex("[\u064B-\u065F\u0670]")
    private val PRIMARY_SEGMENTS_REGEX = Regex("[،,;\n]")
    private val SLASH_SURROUNDED_BY_SPACE = Regex("""\s+/\s*|\s*/\s+""")
    private val MULTI_ZWNJ_REGEX = Regex("\u200C+")

    /**
     * Clean input text:
     * - NFC unicode normalization
     * - Remove zero-width spaces (\u200B)
     * - Normalize Arabic Yeh (\u064A, \u0649) to Persian Yeh (\u06CC)
     * - Normalize Arabic Kaf (\u0643) to Persian Kaf (\u06A9)
     * - Collapse multiple ZWNJs (\u200C) and trim surrounding whitespace/ZWNJs
     * - Collapse multiple whitespace characters into single space
     */
    fun cleanText(text: String?): String {
        if (text.isNullOrBlank()) return ""
        val normalizedNfc = Normalizer.normalize(text, Normalizer.Form.NFC)
            .replace("\u200B", "") // Remove zero-width space
            .replace('\u064A', '\u06CC') // Arabic Yeh -> Persian Yeh
            .replace('\u0649', '\u06CC') // Alef Maksura -> Persian Yeh
            .replace('\u0643', '\u06A9') // Arabic Kaf -> Persian Kaf

        val collapsedZwnj = MULTI_ZWNJ_REGEX.replace(normalizedNfc, "\u200C")
        val trimmed = collapsedZwnj.trim { it <= ' ' || it == '\u200C' || it == '\u200B' }
        return WHITESPACE_REGEX.replace(trimmed, " ")
    }

    /**
     * Canonical key for exact duplicate matching (D18):
     * Lowercase + collapse whitespace + preserve Spanish accents (á, é, í, ó, ú, ü, ñ) and punctuation.
     */
    fun canonicalKey(text: String?): String {
        val cleaned = cleanText(text)
        return cleaned.lowercase(Locale.ROOT)
    }

    fun toCanonicalKey(text: String?): String = canonicalKey(text)

    /**
     * Search key for diacritic-insensitive search:
     * Lowercase + normalize Persian (Yeh, Kaf, Alef variants, remove ZWNJ & Harakat) +
     * remove Latin combining accents (e.g. "adiós" matches "adios", "niño" matches "nino").
     */
    fun searchKey(text: String?): String {
        if (text.isNullOrBlank()) return ""
        val cleaned = cleanText(text).lowercase(Locale.ROOT)
            .replace('\u0622', '\u0627') // Alef with madda above (آ) -> Alef (ا)
            .replace('\u0623', '\u0627') // Alef with hamza above (أ) -> Alef (ا)
            .replace('\u0625', '\u0627') // Alef with hamza below (إ) -> Alef (ا)
            .replace('\u0671', '\u0627') // Alef wasla (ٱ) -> Alef (ا)
            .replace("\u200C", "")       // Remove ZWNJ for unified matching

        val withoutArabicDiacritics = ARABIC_PERSIAN_DIACRITICS_REGEX.replace(cleaned, "")
        val nfd = Normalizer.normalize(withoutArabicDiacritics, Normalizer.Form.NFD)
        val withoutLatinDiacritics = DIACRITICS_REGEX.replace(nfd, "")
        return WHITESPACE_REGEX.replace(withoutLatinDiacritics.trim(), " ")
    }

    fun splitTranslations(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        // First split by commas, semicolons, and newlines
        val primarySegments = text.split(PRIMARY_SEGMENTS_REGEX)
            .map { cleanText(it) }
            .filter { it.isNotBlank() }

        val result = mutableListOf<String>()
        for (seg in primarySegments) {
            if (seg.contains('/')) {
                // If slash is surrounded by whitespace, e.g. "سیب / گلابی"
                if (SLASH_SURROUNDED_BY_SPACE.containsMatchIn(seg)) {
                    val sub = seg.split(SLASH_SURROUNDED_BY_SPACE)
                        .map { cleanText(it) }
                        .filter { it.isNotBlank() }
                    result.addAll(sub)
                } else {
                    // Slash without spaces. Check word count.
                    // If multi-word phrase like "روی کسی/چیزی حساب کردن" (more than 2 tokens), keep intact!
                    val tokens = seg.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
                    if (tokens.size > 2) {
                        result.add(seg)
                    } else {
                        // Isolated single tokens, e.g. "شاد/خوشحال"
                        val sub = seg.split('/')
                            .map { cleanText(it) }
                            .filter { it.isNotBlank() }
                        result.addAll(sub)
                    }
                }
            } else {
                result.add(seg)
            }
        }
        return result.distinct()
    }

    fun formatTranslations(translations: List<String>): String {
        return translations.filter { it.isNotBlank() }.distinct().joinToString(" / ")
    }

    /**
     * Determines whether text is predominantly RTL (Persian/Arabic).
     */
    fun isRtlText(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        var rtlCount = 0
        var ltrCount = 0
        for (ch in text) {
            val code = ch.code
            if ((code in 0x0600..0x06FF) || (code in 0x0750..0x077F) || (code in 0x08A0..0x08FF) || (code in 0xFB50..0xFDFF) || (code in 0xFE70..0xFEFF)) {
                rtlCount++
            } else if ((code in 0x0041..0x005A) || (code in 0x0061..0x007A) || (code in 0x00C0..0x00FF)) {
                ltrCount++
            }
        }
        return rtlCount > ltrCount
    }

    /**
     * Checks and normalizes Spanish punctuation corruptions (e.g., "¿Cuánto cuesta;¿" -> "¿Cuánto cuesta?"):
     * 1. Replaces corrupted trailing "¿" or ";¿" with correct closing "?"
     * 2. Replaces corrupted trailing "¡" or ";¡" with correct closing "!"
     * 3. Trims space after opening "¿" or "¡" and before closing "?" or "!"
     * 4. Strips trailing stray delimiters like stray semicolons or colons
     * 5. If starts with "¿" and missing closing "?", appends "?"
     */
    fun normalizeSpanishPunctuation(input: String): Pair<String, List<String>> {
        var text = input.trim()
        val modifications = mutableListOf<String>()

        // 1. Remove space after opening inverted punctuation
        if (text.startsWith("¿ ") || text.startsWith("¡ ")) {
            val mark = text[0]
            text = mark + text.substring(1).trimStart()
            modifications.add("حذف فاصله بعد از علامت آغازین اسپانیایی ($mark)")
        }

        // 2. Fix corrupted trailing inverted question mark: ";¿" or ",¿" or " ¿" or "¿" at end
        val trailingInvertedQuestion = Regex("""[;,؛،\s]+¿\s*$""")
        if (trailingInvertedQuestion.containsMatchIn(text)) {
            text = trailingInvertedQuestion.replace(text, "?").trim()
            modifications.add("اصلاح علامت سؤال اسپانیایی و تبدیل ;¿ پایانی به ?")
        } else if (text.endsWith("¿") && text.length > 1) {
            text = text.substring(0, text.length - 1).trimEnd() + "?"
            modifications.add("اصلاح علامت سؤال معکوس پایانی به ?")
        }

        // 3. Fix corrupted trailing inverted exclamation mark: ";¡" or ",¡" or " ¡" or "¡" at end
        val trailingInvertedExclamation = Regex("""[;,؛،\s]+¡\s*$""")
        if (trailingInvertedExclamation.containsMatchIn(text)) {
            text = trailingInvertedExclamation.replace(text, "!").trim()
            modifications.add("اصلاح علامت تعجب اسپانیایی و تبدیل ;¡ پایانی به !")
        } else if (text.endsWith("¡") && text.length > 1) {
            text = text.substring(0, text.length - 1).trimEnd() + "!"
            modifications.add("اصلاح علامت تعجب معکوس پایانی به !")
        }

        // 4. If string starts with "¿", ensure it ends with "?"
        if (text.startsWith("¿")) {
            // Remove space before question mark e.g. "carrera ?" -> "carrera?"
            val spaceBeforeQ = Regex("""\s+\?$""")
            if (spaceBeforeQ.containsMatchIn(text)) {
                text = spaceBeforeQ.replace(text, "?")
            }
            // If ends with semicolon or comma instead of question mark e.g. "¿Cuánto cuesta;"
            val trailingSemiOrComma = Regex("""[;,؛،\s]+$""")
            if (trailingSemiOrComma.containsMatchIn(text) && !text.endsWith("?")) {
                text = trailingSemiOrComma.replace(text, "?").trim()
                modifications.add("اصلاح علامت سؤال در انتهای جمله سؤالی اسپانیایی")
            } else if (!text.endsWith("?") && !text.endsWith(".") && !text.endsWith("!")) {
                text = "$text?"
                modifications.add("افزودن علامت سؤال پایانی برای پرسش اسپانیایی")
            }
        } else if (text.endsWith("?") && !isRtlText(text)) {
            // If it ends with "?" and has Latin/Spanish characters, and is a question clause
            // (e.g. "Cuánto cuesta;¿" after fixing became "Cuánto cuesta?")
            // If it's a full interrogative sentence starting with common Spanish question words, add opening "¿"
            val firstWord = text.split(Regex("""\s+""")).firstOrNull()?.lowercase(Locale.ROOT) ?: ""
            val spanishQuestionStarters = setOf(
                "cuánto", "cuanto", "cuántos", "cuantos", "cuánta", "cuanta", "cuántas", "cuantas",
                "qué", "que", "cómo", "como", "dónde", "donde", "adónde", "adonde",
                "quién", "quien", "quiénes", "quienes", "cuál", "cual", "cuáles", "cuales",
                "por", "para", "es", "son", "hay", "tiene", "tienes", "puede", "puedes", "está", "esta", "están", "estan"
            )
            if (spanishQuestionStarters.contains(firstWord)) {
                text = "¿$text"
                modifications.add("افزودن علامت سؤال معکوس آغازین (¿) برای جمله سؤالی اسپانیایی")
            }
        }

        // 5. Strip trailing stray delimiters like ";" or "؛" or ":" or stray comma/hyphen
        val trailingDanglingDelimiters = Regex("""[;,؛،:\-—]+\s*$""")
        if (trailingDanglingDelimiters.containsMatchIn(text)) {
            val cleaned = trailingDanglingDelimiters.replace(text, "").trim()
            if (cleaned.isNotBlank()) {
                text = cleaned
                modifications.add("حذف علامت یا جداکننده زائد از انتهای متن")
            }
        }

        return Pair(text, modifications)
    }
}
