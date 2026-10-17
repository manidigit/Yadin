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
    private val TRAILING_INVERTED_QUESTION_REGEX = Regex("""[;,؛،\s]+¿\s*$""")
    private val TRAILING_INVERTED_EXCLAMATION_REGEX = Regex("""[;,؛،\s]+¡\s*$""")
    private val SPACE_BEFORE_QUESTION_REGEX = Regex("""\s+\?$""")
    private val TRAILING_SEMI_OR_COMMA_REGEX = Regex("""[;,؛،\s]+$""")
    private val SPANISH_QUESTION_STARTER_SPLIT_REGEX = Regex("""\s+""")
    private val TRAILING_DANGLING_DELIMITERS_REGEX = Regex("""[;,؛،:\-—]+\s*$""")
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

    private val EZAFE_HEH_YEH_REGEX = Regex("""ه[\u200C\s]*ی(?:\s|$)""")
    private val PERSIAN_NON_ALNUM_REGEX = Regex("""[\u200C\u200F\u200E\s\-_،,.;؛:!؟?()\[\]«»"'/\\]+""")
    private val PARENTHETICAL_BLOCK_REGEX = Regex("""\([^)]*\)""")
    private val GENDER_PAREN_ONLY_REGEX = Regex("""\(\s*(?:مذکر|مؤنث|مرد|زن|آقا|خانم)(?:\s*[/،,و]\s*(?:مذکر|مؤنث|مرد|زن|آقا|خانم))*\s*\)""")
    private val TRAILING_GENDER_WORD_REGEX = Regex("""\s+(مرد|زن|مذکر|مؤنث)\s*$""")

    /**
     * Splits text by delimiters ONLY when outside parentheses (...), brackets [...], or guillemets «...».
     */
    private fun splitOutsideParens(input: String, delimiters: Set<Char>): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var parenDepth = 0
        for (ch in input) {
            when (ch) {
                '(', '[', '«' -> {
                    parenDepth++
                    sb.append(ch)
                }
                ')', ']', '»' -> {
                    if (parenDepth > 0) parenDepth--
                    sb.append(ch)
                }
                in delimiters -> {
                    if (parenDepth == 0) {
                        result.add(sb.toString())
                        sb.clear()
                    } else {
                        sb.append(ch)
                    }
                }
                else -> sb.append(ch)
            }
        }
        if (sb.isNotEmpty()) {
            result.add(sb.toString())
        }
        return result
    }

    /**
     * Normalized Persian comparison key for detecting duplicate Persian translations
     * (ignores ZWNJ vs space, Arabic/Persian Yeh/Kaf/Alef, Ezafe ه‌ی vs ه, and punctuation).
     */
    fun persianDedupeKey(text: String?): String {
        if (text.isNullOrBlank()) return ""
        var s = cleanText(text).lowercase(Locale.ROOT)
            .replace('ة', 'ه')
            .replace('ؤ', 'و')
            .replace('إ', 'ا')
            .replace('أ', 'ا')
            .replace('آ', 'ا')
            .replace("هٔ", "ه")
            .replace("الآن", "الان")
        s = EZAFE_HEH_YEH_REGEX.replace(s, "ه")
        s = ARABIC_PERSIAN_DIACRITICS_REGEX.replace(s, "")
        return PERSIAN_NON_ALNUM_REGEX.replace(s, "")
    }

    /**
     * Rejoins broken parentheses, strips redundant gender-only tags, splits combined non-sentence
     * translations whose subparts duplicate existing items, and removes duplicate Persian translations.
     */
    fun deduplicatePersianTranslations(translations: List<String>): List<String> {
        if (translations.isEmpty()) return emptyList()

        // 1. Clean and rejoin adjacent items with unbalanced parentheses e.g. ["بیمار (مذکر", "مؤنث)"]
        val rawCleaned = translations.map { cleanText(it) }.filter { it.isNotBlank() }
        if (rawCleaned.isEmpty()) return emptyList()

        val parenMerged = mutableListOf<String>()
        var i = 0
        while (i < rawCleaned.size) {
            var cur = rawCleaned[i]
            while (cur.count { it == '(' } > cur.count { it == ')' } && i + 1 < rawCleaned.size) {
                i++
                cur = "$cur، ${rawCleaned[i]}"
            }
            // Strip redundant gender-only parentheticals e.g. "(مذکر، مؤنث)" or "(مذکر / مؤنث)"
            val strippedGenderParen = cleanText(GENDER_PAREN_ONLY_REGEX.replace(cur, ""))
            if (strippedGenderParen.isNotBlank()) {
                parenMerged.add(strippedGenderParen)
            } else {
                parenMerged.add(cur)
            }
            i++
        }

        // 2. If both "X مرد" and "X زن" exist in the same concept, collapse them to "X"
        val strippedGenderMap = parenMerged.map { item ->
            val match = TRAILING_GENDER_WORD_REGEX.find(item)
            if (match != null) {
                val base = cleanText(item.substring(0, match.range.first))
                val gender = cleanText(match.groupValues[1])
                Triple(item, base, gender)
            } else {
                Triple(item, item, "")
            }
        }
        val baseToGenders = strippedGenderMap.filter { it.third.isNotEmpty() }
            .groupBy({ persianDedupeKey(it.second) }, { it.third })
        val genderCollapsed = strippedGenderMap.map { (orig, base, gender) ->
            val gendersForBase = baseToGenders[persianDedupeKey(base)]?.toSet().orEmpty()
            if (gender.isNotEmpty() && base.isNotBlank() &&
                (("مرد" in gendersForBase && "زن" in gendersForBase) ||
                 ("مذکر" in gendersForBase && "مؤنث" in gendersForBase) ||
                 strippedGenderMap.any { it.third.isEmpty() && persianDedupeKey(it.first) == persianDedupeKey(base) })
            ) {
                base
            } else {
                orig
            }
        }

        // 3. Expand combined items ("A / B" or "A، B" or "A؛ B") when they are word/phrase lists or overlap with other translations
        val atomicCandidates = mutableListOf<String>()
        val allIndividualKeys = genderCollapsed.map { persianDedupeKey(it) }.toSet()
        for (item in genderCollapsed) {
            val hasSpacedSlash = SLASH_SURROUNDED_BY_SPACE.containsMatchIn(item)
            val delimiters = if (hasSpacedSlash) setOf('،', ',', ';', '؛', '/') else setOf('،', ',', ';', '؛')
            val subParts = splitOutsideParens(item, delimiters)
                .map { cleanText(it) }
                .filter { it.isNotBlank() }
            if (subParts.size > 1) {
                val isSentence = item.endsWith(".") || item.endsWith("؟") || item.endsWith("?") || item.endsWith("!") ||
                    item.split(WHITESPACE_REGEX).size >= 6
                val anySubPartAlreadyExists = subParts.any { persianDedupeKey(it) in allIndividualKeys }
                if (!isSentence || anySubPartAlreadyExists) {
                    atomicCandidates.addAll(subParts)
                } else {
                    atomicCandidates.add(item)
                }
            } else {
                atomicCandidates.add(item)
            }
        }

        // 4. Deduplicate by normalized Persian key (preferring proper ZWNJ and standard spelling)
        fun qualityScore(s: String): Int {
            var score = 0
            if (s.contains('\u200C')) score += 5
            if (Regex("""ه\s+ی(?:\s|$)""").containsMatchIn(s)) score -= 5
            if (s.contains("الآن")) score -= 2
            return score
        }

        fun colloquialNormalizedKey(s: String): String {
            val unified = s
                .replace("مگه", "مگر")
                .replace("دیگه", "دیگر")
                .replace("می‌خواد", "می‌خواهد")
                .replace("میخواد", "می‌خواهد")
            return persianDedupeKey(unified)
        }

        val deduped = mutableListOf<String>()
        val keyToIndex = mutableMapOf<String, Int>()
        for (candidate in atomicCandidates) {
            val key = colloquialNormalizedKey(candidate)
            if (key.isEmpty()) continue
            val existingIdx = keyToIndex[key]
            if (existingIdx == null) {
                keyToIndex[key] = deduped.size
                deduped.add(candidate)
            } else if (qualityScore(candidate) > qualityScore(deduped[existingIdx])) {
                deduped[existingIdx] = candidate
            }
        }

        // 5. If both "X" and "X (توضیح)" exist with the exact same base "X", keep only one
        if (deduped.size > 1) {
            val bareKeys = deduped.filter { !it.contains('(') }.map { persianDedupeKey(it) }.toSet()
            if (bareKeys.isNotEmpty()) {
                val filteredParens = deduped.filterNot { item ->
                    item.contains('(') &&
                        persianDedupeKey(PARENTHETICAL_BLOCK_REGEX.replace(item, "")) in bareKeys
                }
                if (filteredParens.isNotEmpty()) {
                    return filteredParens
                }
            }
        }

        return deduped
    }

    fun splitTranslations(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()
        val lines = text.split('\n').map { cleanText(it) }.filter { it.isNotBlank() }
        val rawResult = mutableListOf<String>()

        for (line in lines) {
            val looksLikeSentence = line.endsWith(".") || line.endsWith("؟") || line.endsWith("?") || line.endsWith("!") ||
                line.split(WHITESPACE_REGEX).size >= 6
            val primarySegments = if (looksLikeSentence) {
                splitOutsideParens(line, setOf(';', '؛')).map { cleanText(it) }.filter { it.isNotBlank() }
            } else {
                splitOutsideParens(line, setOf('،', ',', ';', '؛')).map { cleanText(it) }.filter { it.isNotBlank() }
            }

            for (seg in primarySegments) {
                val slashParts = splitOutsideParens(seg, setOf('/')).map { cleanText(it) }.filter { it.isNotBlank() }
                if (slashParts.size > 1) {
                    if (SLASH_SURROUNDED_BY_SPACE.containsMatchIn(seg)) {
                        // Only split on " / " if it is not breaking a sentence at an inline word pair
                        val segIsSentence = seg.endsWith(".") || seg.endsWith("؟") || seg.endsWith("?") || seg.endsWith("!")
                        val firstHasEndPunct = slashParts.first().let { it.endsWith(".") || it.endsWith("؟") || it.endsWith("?") || it.endsWith("!") }
                        if (segIsSentence && !firstHasEndPunct && slashParts.any { it.split(WHITESPACE_REGEX).size <= 2 }) {
                            rawResult.add(seg)
                        } else {
                            rawResult.addAll(slashParts)
                        }
                    } else {
                        val tokens = seg.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
                        if (tokens.size > 2) {
                            rawResult.add(seg)
                        } else {
                            rawResult.addAll(slashParts)
                        }
                    }
                } else {
                    rawResult.add(seg)
                }
            }
        }

        return deduplicatePersianTranslations(rawResult)
    }

    fun formatTranslations(translations: List<String>): String {
        return deduplicatePersianTranslations(translations.filter { it.isNotBlank() }).joinToString(" / ")
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
        if (TRAILING_INVERTED_QUESTION_REGEX.containsMatchIn(text)) {
            text = TRAILING_INVERTED_QUESTION_REGEX.replace(text, "?").trim()
            modifications.add("اصلاح علامت سؤال اسپانیایی و تبدیل ;¿ پایانی به ?")
        } else if (text.endsWith("¿") && text.length > 1) {
            text = text.substring(0, text.length - 1).trimEnd() + "?"
            modifications.add("اصلاح علامت سؤال معکوس پایانی به ?")
        }

        // 3. Fix corrupted trailing inverted exclamation mark: ";¡" or ",¡" or " ¡" or "¡" at end
        if (TRAILING_INVERTED_EXCLAMATION_REGEX.containsMatchIn(text)) {
            text = TRAILING_INVERTED_EXCLAMATION_REGEX.replace(text, "!").trim()
            modifications.add("اصلاح علامت تعجب اسپانیایی و تبدیل ;¡ پایانی به !")
        } else if (text.endsWith("¡") && text.length > 1) {
            text = text.substring(0, text.length - 1).trimEnd() + "!"
            modifications.add("اصلاح علامت تعجب معکوس پایانی به !")
        }

        // 4. If string starts with "¿", ensure it ends with "?"
        if (text.startsWith("¿")) {
            // Remove space before question mark e.g. "carrera ?" -> "carrera?"
            if (SPACE_BEFORE_QUESTION_REGEX.containsMatchIn(text)) {
                text = SPACE_BEFORE_QUESTION_REGEX.replace(text, "?")
            }
            // If ends with semicolon or comma instead of question mark e.g. "¿Cuánto cuesta;"
            if (TRAILING_SEMI_OR_COMMA_REGEX.containsMatchIn(text) && !text.endsWith("?")) {
                text = TRAILING_SEMI_OR_COMMA_REGEX.replace(text, "?").trim()
                modifications.add("اصلاح علامت سؤال در انتهای جمله سؤالی اسپانیایی")
            } else if (!text.endsWith("?") && !text.endsWith(".") && !text.endsWith("!")) {
                text = "$text?"
                modifications.add("افزودن علامت سؤال پایانی برای پرسش اسپانیایی")
            }
        } else if (text.endsWith("?") && !isRtlText(text)) {
            // If it ends with "?" and has Latin/Spanish characters, and is a question clause
            val firstWord = text.split(SPANISH_QUESTION_STARTER_SPLIT_REGEX).firstOrNull()?.lowercase(Locale.ROOT) ?: ""
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
        if (TRAILING_DANGLING_DELIMITERS_REGEX.containsMatchIn(text)) {
            val cleaned = TRAILING_DANGLING_DELIMITERS_REGEX.replace(text, "").trim()
            if (cleaned.isNotBlank()) {
                text = cleaned
                modifications.add("حذف علامت یا جداکننده زائد از انتهای متن")
            }
        }

        return Pair(text, modifications)
    }
}
