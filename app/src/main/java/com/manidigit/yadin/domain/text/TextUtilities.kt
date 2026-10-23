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

    private val SPACE_BEFORE_EXCLAMATION_REGEX = Regex("""\s+!$""")

    /**
     * Checks and normalizes Spanish punctuation corruptions:
     * 1. Replaces leading regular "!" or "?" with Spanish inverted "¡" or "¿" (e.g., "!No respira!" -> "¡No respira!")
     * 2. Fixes mismatched opening/closing marks (e.g., "¿qué nervios!" -> "¡qué nervios!", "¡Cómo estás?" -> "¿Cómo estás?")
     * 3. Replaces corrupted trailing "¿" or ";¿" with correct closing "?" and trailing "¡" or ";¡" with "!"
     * 4. Trims space after opening "¿" or "¡" and before closing "?" or "!"
     * 5. Ensures Spanish sentences starting with "¿" end with "?" and starting with "¡" end with "!"
     * 6. Prepends missing opening "¿" or "¡" for Spanish sentences ending with "?" or "!" (e.g., "Hasta mañana!" -> "¡Hasta mañana!")
     * 7. Strips trailing stray delimiters like stray semicolons or colons
     */
    fun normalizeSpanishPunctuation(input: String): Pair<String, List<String>> {
        var text = input.trim()
        val modifications = mutableListOf<String>()
        val isLatin = !isRtlText(text) && text.any { it in 'A'..'Z' || it in 'a'..'z' || it in '\u00C0'..'\u00FF' }

        // 1. Convert leading regular "!" or "?" on Spanish/Latin text to inverted "¡" or "¿"
        if (isLatin && text.startsWith("!") && text.length > 1) {
            text = "¡" + text.substring(1).trimStart()
            modifications.add("تبدیل علامت تعجب آغازین (!) به علامت تعجب معکوس اسپانیایی (¡)")
        } else if (isLatin && text.startsWith("?") && text.length > 1) {
            text = "¿" + text.substring(1).trimStart()
            modifications.add("تبدیل علامت سؤال آغازین (?) به علامت سؤال معکوس اسپانیایی (¿)")
        }

        // 2. Remove space after opening inverted punctuation
        if (text.startsWith("¿ ") || text.startsWith("¡ ")) {
            val mark = text[0]
            text = mark + text.substring(1).trimStart()
            modifications.add("حذف فاصله بعد از علامت آغازین اسپانیایی ($mark)")
        }

        // 3. Fix corrupted trailing inverted question mark: ";¿" or ",¿" or " ¿" or "¿" at end
        if (TRAILING_INVERTED_QUESTION_REGEX.containsMatchIn(text)) {
            text = TRAILING_INVERTED_QUESTION_REGEX.replace(text, "?").trim()
            modifications.add("اصلاح علامت سؤال اسپانیایی و تبدیل ;¿ پایانی به ?")
        } else if (text.endsWith("¿") && text.length > 1) {
            text = text.substring(0, text.length - 1).trimEnd() + "?"
            modifications.add("اصلاح علامت سؤال معکوس پایانی به ?")
        }

        // 4. Fix corrupted trailing inverted exclamation mark: ";¡" or ",¡" or " ¡" or "¡" at end
        if (TRAILING_INVERTED_EXCLAMATION_REGEX.containsMatchIn(text)) {
            text = TRAILING_INVERTED_EXCLAMATION_REGEX.replace(text, "!").trim()
            modifications.add("اصلاح علامت تعجب اسپانیایی و تبدیل ;¡ پایانی به !")
        } else if (text.endsWith("¡") && text.length > 1) {
            text = text.substring(0, text.length - 1).trimEnd() + "!"
            modifications.add("اصلاح علامت تعجب معکوس پایانی به !")
        }

        // 5. Fix mismatched opening and closing marks (e.g., "¿qué nervios!" -> "¡qué nervios!" or "¡Dónde estás?" -> "¿Dónde estás?")
        if (text.startsWith("¿") && text.endsWith("!") && !text.contains("?")) {
            text = "¡" + text.substring(1)
            modifications.add("اصلاح عدم تطابق علامت آغازین و پایانی (تبدیل ¿ به ¡)")
        } else if (text.startsWith("¡") && text.endsWith("?") && !text.contains("!")) {
            text = "¿" + text.substring(1)
            modifications.add("اصلاح عدم تطابق علامت آغازین و پایانی (تبدیل ¡ به ¿)")
        }

        // 5b. Fix duplicate opening inverted mark before a comma clause (e.g., "¿Por favor, ¿puedes...?" -> "Por favor, ¿puedes...?")
        if (text.startsWith("¿") && text.indexOf('¿', 1) != -1 && text.count { it == '?' } <= 1) {
            text = text.substring(1).trimStart()
            modifications.add("حذف علامت سؤال معکوس تکراری در ابتدای عبارت")
        } else if (text.startsWith("¡") && text.indexOf('¡', 1) != -1 && text.count { it == '!' } <= 1) {
            text = text.substring(1).trimStart()
            modifications.add("حذف علامت تعجب معکوس تکراری در ابتدای عبارت")
        }

        // 6. Handle Spanish questions ("¿...?")
        if (text.startsWith("¿")) {
            if (SPACE_BEFORE_QUESTION_REGEX.containsMatchIn(text)) {
                text = SPACE_BEFORE_QUESTION_REGEX.replace(text, "?")
            }
            if (TRAILING_SEMI_OR_COMMA_REGEX.containsMatchIn(text) && !text.endsWith("?")) {
                text = TRAILING_SEMI_OR_COMMA_REGEX.replace(text, "?").trim()
                modifications.add("اصلاح علامت سؤال در انتهای جمله سؤالی اسپانیایی")
            } else if (!text.endsWith("?") && !text.endsWith(".") && !text.endsWith("!")) {
                text = "$text?"
                modifications.add("افزودن علامت سؤال پایانی برای پرسش اسپانیایی")
            }
        } else if (text.endsWith("?") && isLatin && !text.contains("¿")) {
            if (SPACE_BEFORE_QUESTION_REGEX.containsMatchIn(text)) {
                text = SPACE_BEFORE_QUESTION_REGEX.replace(text, "?")
            }
            val dotIdx = text.lastIndexOf(". ")
            text = if (dotIdx != -1 && dotIdx + 2 < text.length) {
                text.substring(0, dotIdx + 2) + "¿" + text.substring(dotIdx + 2).trimStart()
            } else {
                "¿$text"
            }
            modifications.add("افزودن علامت سؤال معکوس آغازین (¿) برای جمله سؤالی اسپانیایی")
        }

        // 7. Handle Spanish exclamations ("¡...!")
        if (text.startsWith("¡")) {
            if (SPACE_BEFORE_EXCLAMATION_REGEX.containsMatchIn(text)) {
                text = SPACE_BEFORE_EXCLAMATION_REGEX.replace(text, "!")
            }
            if (TRAILING_SEMI_OR_COMMA_REGEX.containsMatchIn(text) && !text.endsWith("!")) {
                text = TRAILING_SEMI_OR_COMMA_REGEX.replace(text, "!").trim()
                modifications.add("اصلاح علامت تعجب در انتهای جمله تعجبی اسپانیایی")
            } else if (!text.endsWith("!") && !text.endsWith("?") && !text.endsWith(".")) {
                text = "$text!"
                modifications.add("افزودن علامت تعجب پایانی (!) برای عبارت تعجبی اسپانیایی")
            }
        } else if (text.endsWith("!") && isLatin && !text.contains("¡")) {
            if (SPACE_BEFORE_EXCLAMATION_REGEX.containsMatchIn(text)) {
                text = SPACE_BEFORE_EXCLAMATION_REGEX.replace(text, "!")
            }
            val dotIdx = text.lastIndexOf(". ")
            text = if (dotIdx != -1 && dotIdx + 2 < text.length) {
                text.substring(0, dotIdx + 2) + "¡" + text.substring(dotIdx + 2).trimStart()
            } else {
                "¡$text"
            }
            modifications.add("افزودن علامت تعجب معکوس آغازین (¡) برای عبارت تعجبی اسپانیایی")
        }

        // 8. Strip trailing stray delimiters like ";" or "؛" or ":" or stray comma/hyphen
        if (TRAILING_DANGLING_DELIMITERS_REGEX.containsMatchIn(text)) {
            val cleaned = TRAILING_DANGLING_DELIMITERS_REGEX.replace(text, "").trim()
            if (cleaned.isNotBlank()) {
                text = cleaned
                modifications.add("حذف علامت یا جداکننده زائد از انتهای متن")
            }
        }

        return Pair(text, modifications)
    }

    enum class SpanishArticleGender {
        NONE,
        MASCULINE,
        FEMININE,
        BOTH
    }

    data class SpanishHeadwordAnalysis(
        val originalText: String,
        val articlePrefix: String?,
        val isDefiniteArticle: Boolean,
        val gender: SpanishArticleGender,
        val coreText: String,
        val dedupeKey: String,
        val accentInsensitiveKey: String,
        val trailingParenNote: String?
    )

    private val SURROUNDING_SPANISH_PUNCT_REGEX = Regex("""^[¿¡"'\s«»]+|[?!."'\s«»]+$""")
    private val INLINE_PUNCT_FOR_KEY_REGEX = Regex("""[¿?¡!.,;:()"'«»]+""")
    private val TRAILING_PAREN_EXTRACT_REGEX = Regex("""^(.*?)\s*[\(\[（]([^\)\]）]+)[\)\]）]\s*$""")
    private val DUAL_ARTICLE_PREFIX_REGEX = Regex(
        """^(el\s*/\s*la|la\s*/\s*el|los\s*/\s*las|las\s*/\s*los|un\s*/\s*una|una\s*/\s*un)\s+(.+)$""",
        RegexOption.IGNORE_CASE
    )
    private val MASC_DEF_PREFIX_REGEX = Regex("""^(el|los)\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val FEM_DEF_PREFIX_REGEX = Regex("""^(la|las)\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val MASC_INDEF_PREFIX_REGEX = Regex("""^(un|unos)\s+(.+)$""", RegexOption.IGNORE_CASE)
    private val FEM_INDEF_PREFIX_REGEX = Regex("""^(una|unas)\s+(.+)$""", RegexOption.IGNORE_CASE)

    private val NON_NOUN_FOLLOWERS = setOf(
        "que", "qué", "cual", "cuál", "cuales", "cuáles", "de", "del", "mismo", "misma", "otro", "otra"
    )
    private val INDEFINITE_FIXED_FOLLOWERS = setOf(
        "poco", "poquito", "vez", "día", "dia", "rato", "momento", "par", "millón", "millon", "ciento", "tanto", "todo", "nada"
    )
    private val SPANISH_ACCENT_CHARS = setOf('á', 'é', 'í', 'ó', 'ú', 'ü', 'ñ', 'Á', 'É', 'Í', 'Ó', 'Ú', 'Ü', 'Ñ')

    /**
     * Analyzes a Spanish headword without altering its stored text:
     * - Identifies leading grammatical articles (El, La, Los, Las, El/La, Un, Una) or trailing parenthetical articles
     * - Extracts core Spanish word/phrase for duplicate detection (ignoring articles, punctuation, and trailing parentheticals)
     * - Computes both accent-preserving `dedupeKey` and `accentInsensitiveKey`
     */
    fun analyzeSpanishHeadword(rawText: String?): SpanishHeadwordAnalysis {
        val cleaned = cleanText(rawText)
        if (cleaned.isEmpty()) {
            return SpanishHeadwordAnalysis(
                originalText = "",
                articlePrefix = null,
                isDefiniteArticle = false,
                gender = SpanishArticleGender.NONE,
                coreText = "",
                dedupeKey = "",
                accentInsensitiveKey = "",
                trailingParenNote = null
            )
        }

        val punctStripped = SURROUNDING_SPANISH_PUNCT_REGEX.replace(cleaned, "").trim()
            .ifEmpty { cleaned }

        var workingText = punctStripped
        var trailingParenNote: String? = null
        var parenArticlePrefix: String? = null
        var parenArticleGender = SpanishArticleGender.NONE
        var parenArticleDefinite = false

        val parenMatch = TRAILING_PAREN_EXTRACT_REGEX.find(workingText)
        if (parenMatch != null) {
            val beforeParen = parenMatch.groupValues[1].trim()
            val insideParen = parenMatch.groupValues[2].trim()
            if (beforeParen.isNotBlank() && insideParen.isNotBlank()) {
                val insideLower = insideParen.lowercase(Locale.ROOT)
                when {
                    insideLower == "el" || insideLower == "los" -> {
                        workingText = beforeParen
                        parenArticlePrefix = insideLower.replaceFirstChar { it.titlecase(Locale.ROOT) }
                        parenArticleGender = SpanishArticleGender.MASCULINE
                        parenArticleDefinite = true
                    }
                    insideLower == "la" || insideLower == "las" -> {
                        workingText = beforeParen
                        parenArticlePrefix = insideLower.replaceFirstChar { it.titlecase(Locale.ROOT) }
                        parenArticleGender = SpanishArticleGender.FEMININE
                        parenArticleDefinite = true
                    }
                    insideLower == "el/la" || insideLower == "el / la" || insideLower == "la/el" -> {
                        workingText = beforeParen
                        parenArticlePrefix = "El/La"
                        parenArticleGender = SpanishArticleGender.BOTH
                        parenArticleDefinite = true
                    }
                    else -> {
                        workingText = beforeParen
                        trailingParenNote = insideParen
                    }
                }
            }
        }

        var articlePrefix: String? = parenArticlePrefix
        var gender: SpanishArticleGender = parenArticleGender
        var isDefinite = parenArticleDefinite
        var coreText = workingText

        fun canStripArticle(remainder: String, isIndefinite: Boolean): Boolean {
            val words = remainder.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
            if (words.isEmpty() || words.size > 4) return false
            val firstFollower = words.first().lowercase(Locale.ROOT).trim('.', ',', '?', '!', '¿', '¡')
            if (firstFollower in NON_NOUN_FOLLOWERS) return false
            if (isIndefinite && firstFollower in INDEFINITE_FIXED_FOLLOWERS) return false
            return true
        }

        val dualMatch = DUAL_ARTICLE_PREFIX_REGEX.find(workingText)
        val mascDefMatch = if (dualMatch == null) MASC_DEF_PREFIX_REGEX.find(workingText) else null
        val femDefMatch = if (dualMatch == null && mascDefMatch == null) FEM_DEF_PREFIX_REGEX.find(workingText) else null
        val mascIndefMatch = if (dualMatch == null && mascDefMatch == null && femDefMatch == null) MASC_INDEF_PREFIX_REGEX.find(workingText) else null
        val femIndefMatch = if (dualMatch == null && mascDefMatch == null && femDefMatch == null && mascIndefMatch == null) FEM_INDEF_PREFIX_REGEX.find(workingText) else null

        when {
            dualMatch != null && canStripArticle(dualMatch.groupValues[2], dualMatch.groupValues[1].startsWith("un", ignoreCase = true)) -> {
                val rawArt = dualMatch.groupValues[1].lowercase(Locale.ROOT).replace(" ", "")
                articlePrefix = when {
                    rawArt.startsWith("los") || rawArt.startsWith("las") -> "Los/Las"
                    rawArt.startsWith("un") -> "Un/Una"
                    else -> "El/La"
                }
                gender = SpanishArticleGender.BOTH
                isDefinite = !rawArt.startsWith("un")
                coreText = dualMatch.groupValues[2].trim()
            }
            mascDefMatch != null && canStripArticle(mascDefMatch.groupValues[2], isIndefinite = false) -> {
                articlePrefix = mascDefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                gender = SpanishArticleGender.MASCULINE
                isDefinite = true
                coreText = mascDefMatch.groupValues[2].trim()
            }
            femDefMatch != null && canStripArticle(femDefMatch.groupValues[2], isIndefinite = false) -> {
                articlePrefix = femDefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                gender = SpanishArticleGender.FEMININE
                isDefinite = true
                coreText = femDefMatch.groupValues[2].trim()
            }
            mascIndefMatch != null && canStripArticle(mascIndefMatch.groupValues[2], isIndefinite = true) -> {
                articlePrefix = mascIndefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                gender = SpanishArticleGender.MASCULINE
                isDefinite = false
                coreText = mascIndefMatch.groupValues[2].trim()
            }
            femIndefMatch != null && canStripArticle(femIndefMatch.groupValues[2], isIndefinite = true) -> {
                articlePrefix = femIndefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                gender = SpanishArticleGender.FEMININE
                isDefinite = false
                coreText = femIndefMatch.groupValues[2].trim()
            }
        }

        val rawDedupe = INLINE_PUNCT_FOR_KEY_REGEX.replace(coreText.lowercase(Locale.ROOT), " ")
        val dedupeKey = WHITESPACE_REGEX.replace(rawDedupe, " ").trim()
            .ifEmpty { toCanonicalKey(cleaned) }

        val nfd = Normalizer.normalize(dedupeKey, Normalizer.Form.NFD)
        val accentInsensitiveKey = WHITESPACE_REGEX.replace(DIACRITICS_REGEX.replace(nfd, ""), " ").trim()

        return SpanishHeadwordAnalysis(
            originalText = cleaned,
            articlePrefix = articlePrefix,
            isDefiniteArticle = isDefinite,
            gender = gender,
            coreText = coreText,
            dedupeKey = dedupeKey,
            accentInsensitiveKey = accentInsensitiveKey,
            trailingParenNote = trailingParenNote
        )
    }

    /**
     * Produces the Spanish duplicate comparison key (ignoring leading articles El/La/Los/Las/Un/Una,
     * surrounding punctuation, and trailing parenthetical notes without altering the actual word text).
     */
    fun spanishDedupeKey(text: String?): String = analyzeSpanishHeadword(text).dedupeKey

    /**
     * Checks whether two lists of Persian translations share at least one normalized meaning or core token.
     */
    fun hasPersianTranslationOverlap(transA: List<String>, transB: List<String>): Boolean {
        val atomicA = splitTranslations(transA.joinToString("، "))
        val atomicB = splitTranslations(transB.joinToString("، "))
        if (atomicA.isEmpty() || atomicB.isEmpty()) return false

        val keysA = atomicA.map { persianDedupeKey(PARENTHETICAL_BLOCK_REGEX.replace(it, "")) }.filter { it.isNotEmpty() }.toSet()
        val keysB = atomicB.map { persianDedupeKey(PARENTHETICAL_BLOCK_REGEX.replace(it, "")) }.filter { it.isNotEmpty() }.toSet()

        if (keysA.intersect(keysB).isNotEmpty()) return true

        for (a in keysA) {
            if (a.length < 3) continue
            for (b in keysB) {
                if (b.length < 3) continue
                if (a.contains(b) || b.contains(a)) return true
            }
        }
        return false
    }

    /**
     * Selects the preferred Spanish display headword when merging duplicate entries:
     * - Prioritizes forms that include the Spanish grammatical article (e.g., "El trampolín" over "Trampolín")
     *   so the learner learns the noun's gender.
     * - Prioritizes properly accented Spanish forms (e.g., "trampolín" over "trampolin").
     * - Combines the best article with the best accented core when one entry has the article and the other has the accent.
     */
    fun selectPreferredSpanishHeadword(candidates: List<String>): String {
        val cleanedCandidates = candidates.map { cleanText(it) }.filter { it.isNotBlank() }
        if (cleanedCandidates.isEmpty()) return ""
        if (cleanedCandidates.size == 1) return cleanedCandidates.first()

        val analyses = cleanedCandidates.map { analyzeSpanishHeadword(it) }

        val hasBothMascAndFem = analyses.any { it.gender == SpanishArticleGender.MASCULINE } &&
            analyses.any { it.gender == SpanishArticleGender.FEMININE }

        val bestArticleAnalysis = analyses
            .filter { it.articlePrefix != null }
            .maxByOrNull { analysis ->
                var score = 0
                if (analysis.isDefiniteArticle) score += 100 else score += 60
                if (hasBothMascAndFem && analysis.gender == SpanishArticleGender.BOTH) score += 30
                if (analysis.coreText.any { ch -> ch in SPANISH_ACCENT_CHARS }) score += 25
                if (analysis.trailingParenNote == null) score += 10
                if (analysis.originalText.firstOrNull()?.isUpperCase() == true) score += 5
                score
            }

        val bestCoreAnalysis = analyses.maxByOrNull { analysis ->
            var score = 0
            score += analysis.coreText.count { ch -> ch in SPANISH_ACCENT_CHARS } * 30
            if (analysis.trailingParenNote == null) score += 10
            if ((analysis.originalText.startsWith("¡") && analysis.originalText.endsWith("!")) ||
                (analysis.originalText.startsWith("¿") && analysis.originalText.endsWith("?"))
            ) {
                score += 8
            }
            val firstLetter = analysis.originalText.firstOrNull { it.isLetter() }
            if (firstLetter?.isUpperCase() == true) score += 5
            if (analysis.articlePrefix != null && analysis.isDefiniteArticle) score += 5
            score
        } ?: analyses.first()

        if (bestArticleAnalysis != null) {
            val chosenPrefix = if (hasBothMascAndFem && bestArticleAnalysis.gender != SpanishArticleGender.BOTH) {
                "El/La"
            } else {
                bestArticleAnalysis.articlePrefix!!
            }

            val bestCore = bestCoreAnalysis.coreText
            val loweredCoreFirst = if (bestCore.isNotEmpty() && bestCore.first().isUpperCase()) {
                bestCore.first().lowercase(Locale.ROOT) + bestCore.substring(1)
            } else {
                bestCore
            }
            return "$chosenPrefix $loweredCoreFirst".trim()
        }

        val winningText = bestCoreAnalysis.originalText
        if (winningText.startsWith("¡") || winningText.startsWith("¿")) {
            return if (winningText.length >= 2 && winningText[1].isLowerCase()) {
                winningText[0] + winningText[1].titlecase(Locale.ROOT) + winningText.substring(2)
            } else {
                winningText
            }
        }
        return if (winningText.isNotEmpty() && winningText.first().isLowerCase()) {
            winningText.first().titlecase(Locale.ROOT) + winningText.substring(1)
        } else {
            winningText
        }
    }
}
