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
}
