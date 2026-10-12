package com.manidigit.yadin.domain.text

import java.text.Normalizer
import java.util.Locale

object TextUtilities {

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val TRANSLATION_DELIMITERS = Regex("[/،,;]")
    private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val PRIMARY_SEGMENTS_REGEX = Regex("[،,;\n]")
    private val SLASH_SURROUNDED_BY_SPACE = Regex("""\s+/\s*|\s*/\s+""")

    fun cleanText(text: String?): String {
        if (text.isNullOrBlank()) return ""
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)
            .replace("\u200B", "") // Remove zero-width space
            .trim()
        return WHITESPACE_REGEX.replace(normalized, " ")
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
     * Lowercase + remove accents (e.g. "adiós" matches "adios").
     */
    fun searchKey(text: String?): String {
        val cleaned = cleanText(text).lowercase(Locale.ROOT)
        val nfd = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
        return DIACRITICS_REGEX.replace(nfd, "")
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
