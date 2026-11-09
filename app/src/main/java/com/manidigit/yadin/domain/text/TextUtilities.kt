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
    private val GENDER_PAREN_ONLY_REGEX = Regex("""\(\s*(?:(?:در\s+معنای\s+|به\s+عنوان\s+|در\s+بافت\s+|در\s+حالت\s+|بافت\s+|حالت\s+)?(?:صفت|اسم|فعل|قید|فرم)(?:\s+(?:مذکر|مؤنث|مونث|مفرد|جمع))?|(?:مذکر|مؤنث|مونث|مرد|زن|آقا|خانم|پسر|دختر|مفرد|جمع|مؤنث\s+جمع|مونث\s+جمع|مذکر\s+جمع|از\s+زبان\s+آقا|از\s+زبان\s+خانم))(?:\s*[/،,و\-]\s*(?:صفت|اسم|مذکر|مؤنث|مونث|مرد|زن|آقا|خانم|پسر|دختر|مفرد|جمع))*\s*\)""")
    private val TRAILING_GENDER_WORD_REGEX = Regex("""\s+(مرد|زن|مذکر|مؤنث)\s*$""")
    private val PERSIAN_COMPOUND_SPLIT_REGEX = Regex("""\s+(?:و|یا)\s+""")

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

        // 5. Collapse duplicate base headwords before parentheses (e.g., "X" + "X (توضیح)" or multiple "X (توضیح ۱)" + "X (توضیح ۲)")
        val baseCollapsed = mutableListOf<String>()
        val seenBaseKeys = mutableSetOf<String>()
        val bareKeys = deduped.filter { !it.contains('(') }.map { persianDedupeKey(it) }.toSet()
        for (item in deduped) {
            val baseText = PARENTHETICAL_BLOCK_REGEX.replace(item, "").trim()
            val baseKey = persianDedupeKey(baseText)
            if (baseKey.isEmpty()) {
                baseCollapsed.add(item)
                continue
            }
            if (item.contains('(') && baseKey in bareKeys) {
                continue
            }
            if (baseKey in seenBaseKeys) {
                continue
            }
            seenBaseKeys.add(baseKey)
            baseCollapsed.add(item)
        }
        val afterBaseCollapse = if (baseCollapsed.isNotEmpty()) baseCollapsed else deduped

        // 6. Remove redundant compound translations ("A و B" or "A یا B") when A or B already exists as a standalone translation
        if (afterBaseCollapse.size > 1) {
            val standaloneKeys = afterBaseCollapse
                .map { persianDedupeKey(PARENTHETICAL_BLOCK_REGEX.replace(it, "")) }
                .filter { it.isNotEmpty() }
                .toSet()
            val usedCompoundParts = mutableSetOf<String>()
            val prunedCompounds = mutableListOf<String>()
            for ((idx, item) in afterBaseCollapse.withIndex()) {
                val base = PARENTHETICAL_BLOCK_REGEX.replace(item, "").trim()
                val baseKey = persianDedupeKey(base)
                val wordCount = base.split(WHITESPACE_REGEX).count { it.isNotBlank() }
                if ((base.contains(" و ") || base.contains(" یا ")) && wordCount <= 5) {
                    val parts = base.split(PERSIAN_COMPOUND_SPLIT_REGEX).map { it.trim() }.filter { it.isNotEmpty() }
                    if (parts.size >= 2) {
                        val partKeys = parts.map { persianDedupeKey(it) }.filter { it.isNotEmpty() }
                        val matchesStandalone = partKeys.any { it in standaloneKeys && it != baseKey }
                        val matchesPreviousCompound = partKeys.any { it.length >= 3 && it in usedCompoundParts }
                        val allPartsStandalone = partKeys.all { it in standaloneKeys && it != baseKey }
                        val remainingCount = prunedCompounds.size + (afterBaseCollapse.size - idx - 1)
                        if (allPartsStandalone && remainingCount >= 1) {
                            continue
                        }
                        if ((matchesStandalone || matchesPreviousCompound) && remainingCount >= 2) {
                            continue
                        }
                        partKeys.filter { it.length >= 3 }.forEach { usedCompoundParts.add(it) }
                    }
                }
                prunedCompounds.add(item)
            }
            if (prunedCompounds.isNotEmpty()) {
                return prunedCompounds
            }
        }

        return afterBaseCollapse
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

    private val ALL_BRACKETED_BLOCKS_REGEX = Regex("""[\(\[（«][^\)\]）»]*[\)\]）»]""")

    private fun isPersianOrArabicChar(code: Int): Boolean {
        return (code in 0x0600..0x06FF) ||
            (code in 0x0750..0x077F) ||
            (code in 0x08A0..0x08FF) ||
            (code in 0xFB50..0xFDFF) ||
            (code in 0xFE70..0xFEFF)
    }

    private fun isLatinAlphaChar(code: Int): Boolean {
        return (code in 0x0041..0x005A) ||
            (code in 0x0061..0x007A) ||
            (code in 0x00C0..0x024F)
    }

    /**
     * Checks whether the first alphabetic character in [text] is Persian/Arabic script.
     */
    fun startsWithPersianScript(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        for (ch in text) {
            val code = ch.code
            if (isPersianOrArabicChar(code)) return true
            if (isLatinAlphaChar(code)) return false
        }
        return false
    }

    /**
     * Determines whether text is predominantly RTL (Persian/Arabic).
     * Evaluates the primary text outside parentheses/brackets first so that long Latin
     * parenthetical clarifications like "مرکز معاینه فنی خودرو در اسپانیا (Inspección Técnica de Vehículos)"
     * are accurately recognized as RTL Persian text.
     */
    fun isRtlText(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val outsideParens = ALL_BRACKETED_BLOCKS_REGEX.replace(text, " ").trim()
        var primaryRtl = 0
        var primaryLtr = 0
        for (ch in outsideParens) {
            val code = ch.code
            if (isPersianOrArabicChar(code)) {
                primaryRtl++
            } else if (isLatinAlphaChar(code)) {
                primaryLtr++
            }
        }
        if (primaryRtl + primaryLtr > 0) {
            if (primaryRtl != primaryLtr) return primaryRtl > primaryLtr
        }

        var rtlCount = 0
        var ltrCount = 0
        for (ch in text) {
            val code = ch.code
            if (isPersianOrArabicChar(code)) {
                rtlCount++
            } else if (isLatinAlphaChar(code)) {
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

    private val LATIN_COMMA_IN_PERSIAN_REGEX = Regex("""([\u0600-\u06FF])\s*,\s*""")
    private val PERSIAN_MI_SPACE_REGEX = Regex("""(^|[\s(«،])(ن?می)\s+(?=[\u0621-\u06CC]{2,})""")
    private val PERSIAN_MI_ATTACHED_VERB_REGEX = Regex(
        """(^|[\s(«،])(ن?می)(شود|شوند|شوم|شوی|شویم|شوید|کنم|کنی|کند|کنیم|کنید|کنند|دهم|دهی|دهد|دهیم|دهید|دهند|روم|روی|رود|رویم|روید|روند|آیم|آیی|آید|آییم|آیید|آیند|گویم|گویی|گوید|گوییم|گویید|گویند|گیرم|گیری|گیرد|گیریم|گیرید|گیرند|توانم|توانی|تواند|توانیم|توانید|توانند|خواهم|خواهی|خواهد|خواهیم|خواهید|خواهند|خواد|خوان|دانم|دانی|داند|دانیم|دانید|دانند|بینم|بینی|بیند|بینیم|بینید|بینند|گذارم|گذاری|گذارد|گذاریم|گذارید|گذارند|رسد|رسند|زند|زنند|خورم|خوری|خورد|خوریم|خورید|خورند|برم|بری|برد|بریم|برید|برند|دارم|داری|دارد|داریم|دارید|دارند)(?=$|[\s)»!؟.،؛])"""
    )
    private val PERSIAN_HA_SPACE_REGEX = Regex("""(^|[\s(«،])([\u0621-\u06CC]{2,})\s+(ها(?:ی(?:ی|م|ت|ش|مان|تان|شان)?)?)(?=$|[\s)»!؟.،؛])""")
    private val PERSIAN_TAR_SPACE_REGEX = Regex("""(^|[\s(«،])([\u0621-\u06CC]{2,})\s+(تر(?:ین)?)(?=$|[\s)»!؟.،؛])""")
    private val PERSIAN_STOPWORDS_BEFORE_SUFFIX = setOf(
        "از", "به", "در", "با", "تا", "که", "بر", "بی", "هر", "چه", "نه", "آن", "این", "هم", "یا"
    )
    private val TRAILING_PAREN_IN_PERSIAN_REGEX = Regex("""^(.*?)\s*(\([^\(\)]+\))\s*$""")

    // Stems ending in silent 'ه' where stripped ZWNJ turns 'ه‌ای' into 'های' (e.g. 'رایانهای' -> 'رایانه‌ای', 'حرفهای' -> 'حرفه‌ای')
    private val PERSIAN_SILENT_HEH_EI_REGEX = Regex(
        """(^|[\s(«،])(رایانه|جاده|حرفه|بیمه|سابقه|ویژه|روزمره|فاصله|مجموعه|منطقه|ثانیه|دقیقه|وسیله|جریمه|نقره|قهوه|شیشه|هفته|حاشیه|زاویه|لایه|پایه|رشته|صفحه|صحنه|نقطه|حلقه|قطعه|طبقه|درجه|رتبه|مرحله|مسابقه|مقایسه|معامله|محاسبه|مصاحبه|مشاهده|مطالعه|مراجعه|ملاحظه|معاینه|مشاوره|مذاکره|مکاتبه|مبادله|مداخله|مقابله|مواجهه|محاکمه|پروانه|کارخانه|زباله|مجله|مقاله|رساله|حواله|اداره|ستاره|شماره|اجاره|اشاره|قاره|چاره|سواره|پیاده|شبکه|برنامه|سامانه|کارنامه|بودجه|نتیجه|وظیفه|هزینه|زمینه|نمونه|گزینه|کمیته|حادثه|فاجعه|ضایعه|واسطه|رابطه|ضابطه|قاعده|فایده|پدیده|عقیده|نماینده|راننده|خواننده|نویسنده|فروشنده|گیرنده|فرستنده|سازنده|تازه|ساده|آماده|اراده|ماده|[آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی]{2,}نده|[آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی]{2,}انه)ای(?=$|[\s)»!؟.،؛])"""
    )

    // Attached plural suffix 'ها' / 'های' without ZWNJ after a dual-joining Persian letter on stems >= 3 chars (e.g. 'فرکانسها' -> 'فرکانس‌ها')
    private val PERSIAN_ATTACHED_PLURAL_HA_REGEX = Regex(
        """(^|[\s(«،])([آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی\u200C]{2,}[بپتثجچحخسشصضطظعغفقکگلمنهی])(ها(?:یی|یم|یت|یش|یمان|یتان|یشان)?)(?=$|[\s)»!؟.،؛])"""
    )
    private val PERSIAN_NON_PLURAL_HA_WORDS = setOf(
        "تنها", "آنها", "اینها", "منتها", "انتها", "اشتها", "بی\u200Cانتها", "بیانتها", "ناانتها",
        "بها", "گران\u200Cبها", "گرانبها", "اجاره\u200Cبها", "اجارهبها", "کم\u200Cبها", "پربها", "بی\u200Cبها",
        "نهایت", "بی\u200Cنهایت", "بینهایت"
    )

    // Attached passive participle suffix 'شده' / 'نشده' without ZWNJ after a joining letter (e.g. 'سیستمیشده' -> 'سیستمی‌شده', 'تاییدشده' -> 'تاییدشده')
    private val PERSIAN_ATTACHED_SHODEH_REGEX = Regex(
        """(^|[\s(«،])([آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی\u200C]*[بپتثجچحخسشصضطظعغفقکگلمنهی])(ن?شده)(?=$|[\s)»!؟.،؛])"""
    )

    // Attached compound second elements after a dual-joining letter where ZWNJ was stripped during copy-paste
    private val PERSIAN_ATTACHED_COMPOUND_SUFFIX_REGEX = Regex(
        """(^|[\s(«،])([آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی\u200C]*[بپتثجچحخسشصضطظعغفقکگلمنهی])(گذاری|گذار|شناختی|شناسی|کشی)(?=$|[\s)»!؟.،؛])"""
    )

    // Exact common compound words whose ZWNJ is frequently stripped in PDF/web copy-paste
    private val PERSIAN_ZWNJ_COMPOUND_REPLACEMENTS: List<Pair<Regex, String>> = listOf(
        Regex("""(^|[\s(«،])روان(گردان|تنی|پزشک|پزشکی|درمانی)(?=$|[\s)»!؟.،؛])""") to "$1روان\u200C$2",
        Regex("""(^|[\s(«،])(اتومبیل|موتور|کشتی|دوچرخه)(رانی)(?=$|[\s)»!؟.،؛])""") to "$1$2\u200C$3",
        Regex("""(^|[\s(«،])کم(توان|درآمد|خطر|مصرف|نور|فشار|عمق|عرض|سرعت|حجم|ارزش|اهمیت|سابقه|تجربه)(?=$|[\s)»!؟.،؛])""") to "$1کم\u200C$2",
        Regex("""(^|[\s(«،])تمام(خطر|وقت|عیار|قد|صفحه|اتوماتیک)(?=$|[\s)»!؟.،؛])""") to "$1تمام\u200C$2",
        Regex("""(^|[\s(«،])(نیمه|پاره)(وقت)(?=$|[\s)»!؟.،؛])""") to "$1$2\u200C$3",
        Regex("""(^|[\s(«،])صورت(جلسه|حساب|مجلس|وضعیت)(?=$|[\s)»!؟.،؛])""") to "$1صورت\u200C$2",
        Regex("""(^|[\s(«،])(بیمه|آیین|توافق|پرسش|پایان|وصیت|رضایت|شکایت|شهادت|وکالت|ضمانت|امانت|اساس|دعوت|تقدیر|صلح|عهد)(نامه)(?=$|[\s)»!؟.،؛])""") to "$1$2\u200C$3",
        Regex("""(^|[\s(«،])(زمین|آتش|چشم|سخت|گوشه|بهانه|نتیجه|تصمیم|اندازه|فاصله|نفس|موضع)(گیر|گیری)(?=$|[\s)»!؟.،؛])""") to "$1$2\u200C$3",
        Regex("""(^|[\s(«،])(یدک|لوله|سیم|دست|نفس|بار)(کش)(?=$|[\s)»!؟.،؛])""") to "$1$2\u200C$3",
        Regex("""(^|[\s(«،])(سواره|پیاده)(رو)(?=$|[\s)»!؟.،؛])""") to "$1$2\u200C$3",
        Regex("""(^|[\s(«،])زیست(محیطی|بوم|فناوری|کره)(?=$|[\s)»!؟.،؛])""") to "$1زیست\u200C$2",
        Regex("""(^|[\s(«،])پایه(یک|دو|سه)(?=$|[\s)»!؟.،؛])""") to "$1پایه\u200C$2",
        Regex("""(^|[\s(«،])حملونقل(?=$|[\s)»!؟.،؛])""") to "$1حمل\u200Cونقل",
        Regex("""(^|[\s(«،])ضمانتاجرا(?=$|[\s)»!؟.،؛])""") to "$1ضمانت\u200Cاجرا",
        Regex("""(^|[\s(«،])سیسی(?=$|[\s)»!؟.،؛])""") to "$1سی\u200Cسی"
    )

    /**
     * Normalizes Persian translation orthography and aligns punctuation with its Spanish counterpart:
     * 1. Converts Arabic Yeh/Kaf and Latin '?' or ',' in Persian text to standard Persian 'ی', 'ک', '؟', '،'
     * 2. Applies half-space (ZWNJ) to verbal prefixes ('می‌', 'نمی‌'), plural/comparative suffixes ('‌ها', '‌تر', '‌ترین'),
     *    and restores stripped ZWNJ in compound/suffixed words (e.g. 'رایانهای' -> 'رایانه‌ای', 'فرکانسها' -> 'فرکانس‌ها', 'سیستمیشده' -> 'سیستمی‌شده')
     * 3. Strips redundant trailing period '.' from single-word or short non-sentence translations (e.g., "قبوله." -> "قبوله")
     * 4. Aligns question ('؟') and exclamation ('!') marks with the Spanish source sentence
     */
    fun normalizePersianTranslation(
        rawPersian: String,
        spanishSourceText: String? = null
    ): Pair<String, List<String>> {
        var text = cleanText(rawPersian)
        if (text.isBlank()) return Pair("", emptyList())
        val modifications = mutableListOf<String>()

        if (isRtlText(text) || startsWithPersianScript(text)) {
            if (text.contains('?')) {
                text = text.replace('?', '؟')
                modifications.add("تبدیل علامت سؤال لاتین (?) به علامت سؤال فارسی (؟)")
            }
            if (LATIN_COMMA_IN_PERSIAN_REGEX.containsMatchIn(text)) {
                text = LATIN_COMMA_IN_PERSIAN_REGEX.replace(text, "$1، ")
                modifications.add("تبدیل ویرگول لاتین به ویرگول فارسی (،)")
            }

            // 1. Half-space (ZWNJ) for verbal prefix می / نمی
            val afterMiSpace = PERSIAN_MI_SPACE_REGEX.replace(text, "$1$2\u200C")
            val afterMiAttached = PERSIAN_MI_ATTACHED_VERB_REGEX.replace(afterMiSpace, "$1$2\u200C$3")
            if (afterMiAttached != text) {
                text = afterMiAttached
                modifications.add("اصلاح نیم‌فاصله پیشوند فعل (می‌ / نمی‌)")
            }

            // 2. Restore stripped ZWNJ in silent-Heh + 'ای' adjectives/nouns (e.g. 'رایانهای' -> 'رایانه‌ای', 'حرفهای' -> 'حرفه‌ای')
            val afterSilentHehEi = PERSIAN_SILENT_HEH_EI_REGEX.replace(text, "$1$2\u200Cای")
            if (afterSilentHehEi != text) {
                text = afterSilentHehEi
                modifications.add("اصلاح نیم‌فاصله پسوند یای نسبت/نکره (ه‌ای)")
            }

            // 3. Half-space (ZWNJ) for spaced or attached plural suffix ها / های
            val afterHaSpace = PERSIAN_HA_SPACE_REGEX.replace(text) { m ->
                val leading = m.groupValues[1]
                val stem = m.groupValues[2]
                val suffix = m.groupValues[3]
                if (stem in PERSIAN_STOPWORDS_BEFORE_SUFFIX) {
                    m.value
                } else {
                    "$leading$stem\u200C$suffix"
                }
            }
            val afterHaAttached = PERSIAN_ATTACHED_PLURAL_HA_REGEX.replace(afterHaSpace) { m ->
                val leading = m.groupValues[1]
                val stem = m.groupValues[2]
                val suffix = m.groupValues[3]
                val fullWord = stem + suffix
                if (fullWord in PERSIAN_NON_PLURAL_HA_WORDS ||
                    fullWord.endsWith("بها") ||
                    fullWord.endsWith("نهایت") ||
                    stem in PERSIAN_STOPWORDS_BEFORE_SUFFIX
                ) {
                    m.value
                } else {
                    "$leading$stem\u200C$suffix"
                }
            }
            if (afterHaAttached != text) {
                text = afterHaAttached
                modifications.add("اصلاح نیم‌فاصله پسوند جمع (‌ها)")
            }

            // 4. Half-space (ZWNJ) for comparative suffix تر / ترین
            val afterTar = PERSIAN_TAR_SPACE_REGEX.replace(text) { m ->
                val leading = m.groupValues[1]
                val stem = m.groupValues[2]
                val suffix = m.groupValues[3]
                if (stem in PERSIAN_STOPWORDS_BEFORE_SUFFIX) {
                    m.value
                } else {
                    "$leading$stem\u200C$suffix"
                }
            }
            if (afterTar != text) {
                text = afterTar
                modifications.add("اصلاح نیم‌فاصله پسوند صفت برتری (‌تر / ‌ترین)")
            }

            // 5. Restore stripped ZWNJ in attached passive participles ('سیستمیشده' -> 'سیستمی‌شده') and compound suffixes ('پلاکگذاری' -> 'پلاک‌گذاری')
            val afterShodeh = PERSIAN_ATTACHED_SHODEH_REGEX.replace(text) { m ->
                val leading = m.groupValues[1]
                val stem = m.groupValues[2]
                val suffix = m.groupValues[3]
                if (stem.length < 2 || stem == "نا" || stem.endsWith("\u200Cن")) {
                    m.value
                } else {
                    "$leading$stem\u200C$suffix"
                }
            }
            val afterCompoundSuffix = PERSIAN_ATTACHED_COMPOUND_SUFFIX_REGEX.replace(afterShodeh) { m ->
                val leading = m.groupValues[1]
                val stem = m.groupValues[2]
                val suffix = m.groupValues[3]
                if (stem.length < 2 || (stem == "آب" && suffix == "کشی")) {
                    m.value
                } else {
                    "$leading$stem\u200C$suffix"
                }
            }
            var afterCompounds = afterCompoundSuffix
            for ((regex, replacement) in PERSIAN_ZWNJ_COMPOUND_REPLACEMENTS) {
                afterCompounds = regex.replace(afterCompounds, replacement)
            }
            if (afterCompounds != text) {
                text = afterCompounds
                modifications.add("اصلاح نیم‌فاصله ترکیبات و پسوندهای فارسی")
            }

            // 6. Strip redundant trailing period '.' from single-word or short non-sentence translations
            val esClean = spanishSourceText?.trim() ?: ""
            val isEsQuestion = esClean.startsWith("¿") && esClean.endsWith("?")
            val isEsExclamation = esClean.startsWith("¡") && esClean.endsWith("!")
            val isEsDeclarativeWithDot = esClean.endsWith(".") && !esClean.endsWith("..") &&
                esClean.split(WHITESPACE_REGEX).count { it.isNotBlank() } >= 3

            if (text.endsWith(".") && !text.endsWith("..")) {
                val coreWithoutParens = PARENTHETICAL_BLOCK_REGEX.replace(text.removeSuffix("."), "").trim()
                val wordCount = coreWithoutParens.split(WHITESPACE_REGEX).count { it.isNotBlank() }
                if (wordCount <= 2 || (!isEsDeclarativeWithDot && wordCount <= 3 && !isEsQuestion && !isEsExclamation)) {
                    text = text.removeSuffix(".").trimEnd()
                    modifications.add("حذف نقطه زائد پایانی از ترجمه کوتاه فارسی")
                }
            }

            // 7. Align question ('؟') and exclamation ('!') marks between Spanish and Persian
            if (isEsQuestion && !text.contains("؟")) {
                val parenMatch = TRAILING_PAREN_IN_PERSIAN_REGEX.find(text)
                text = if (parenMatch != null) {
                    val mainPart = parenMatch.groupValues[1].trimEnd('.', '!', '?', '؟', ' ')
                    val parenPart = parenMatch.groupValues[2]
                    "$mainPart؟ $parenPart"
                } else {
                    val trimmedBase = text.trimEnd('.', '!', '?', ' ')
                    "$trimmedBase؟"
                }
                modifications.add("هماهنگ‌سازی علامت سؤال فارسی (؟) با جمله پرسشی اسپانیایی")
            } else if (isEsExclamation && !text.contains("!") && !text.contains("؟")) {
                val parenMatch = TRAILING_PAREN_IN_PERSIAN_REGEX.find(text)
                text = if (parenMatch != null) {
                    val mainPart = parenMatch.groupValues[1].trimEnd('.', '!', '?', '؟', ' ')
                    val parenPart = parenMatch.groupValues[2]
                    "$mainPart! $parenPart"
                } else {
                    val trimmedBase = text.trimEnd('.', '!', '?', ' ')
                    "$trimmedBase!"
                }
                modifications.add("هماهنگ‌سازی علامت تعجب (!) با عبارت تعجبی اسپانیایی")
            }
        }

        return Pair(cleanText(text), modifications.distinct())
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
        val trailingParenNote: String?,
        val slashVariantKeys: List<String> = emptyList(),
        val isSlashDualForm: Boolean = false,
        val hasEllipsis: Boolean = false,
        val hasExclamation: Boolean = false,
        val hasQuestion: Boolean = false
    )

    private val SURROUNDING_SPANISH_PUNCT_REGEX = Regex("""^[¿¡"'\s«»]+|[?!."'\s«»…]+$""")
    private val INLINE_PUNCT_FOR_KEY_REGEX = Regex("""[¿?¡!.,;:()"'«»…]+""")
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
        "poco", "poquito", "vez", "día", "dia", "rato", "momento", "par", "millón", "millon", "ciento", "tanto", "todo", "nada",
        "gusto", "placer"
    )
    private val SPANISH_ACCENT_CHARS = setOf('á', 'é', 'í', 'ó', 'ú', 'ü', 'ñ', 'Á', 'É', 'Í', 'Ó', 'Ú', 'Ü', 'Ñ')

    private fun extractArticleAndCore(workingText: String): Triple<String?, SpanishArticleGender, Pair<Boolean, String>> {
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

        return when {
            dualMatch != null && canStripArticle(dualMatch.groupValues[2], dualMatch.groupValues[1].startsWith("un", ignoreCase = true)) -> {
                val rawArt = dualMatch.groupValues[1].lowercase(Locale.ROOT).replace(" ", "")
                val prefix = when {
                    rawArt.startsWith("los") || rawArt.startsWith("las") -> "Los/Las"
                    rawArt.startsWith("un") -> "Un/Una"
                    else -> "El/La"
                }
                Triple(prefix, SpanishArticleGender.BOTH, Pair(!rawArt.startsWith("un"), dualMatch.groupValues[2].trim()))
            }
            mascDefMatch != null && canStripArticle(mascDefMatch.groupValues[2], isIndefinite = false) -> {
                val prefix = mascDefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                Triple(prefix, SpanishArticleGender.MASCULINE, Pair(true, mascDefMatch.groupValues[2].trim()))
            }
            femDefMatch != null && canStripArticle(femDefMatch.groupValues[2], isIndefinite = false) -> {
                val prefix = femDefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                Triple(prefix, SpanishArticleGender.FEMININE, Pair(true, femDefMatch.groupValues[2].trim()))
            }
            mascIndefMatch != null && canStripArticle(mascIndefMatch.groupValues[2], isIndefinite = true) -> {
                val prefix = mascIndefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                Triple(prefix, SpanishArticleGender.MASCULINE, Pair(false, mascIndefMatch.groupValues[2].trim()))
            }
            femIndefMatch != null && canStripArticle(femIndefMatch.groupValues[2], isIndefinite = true) -> {
                val prefix = femIndefMatch.groupValues[1].lowercase(Locale.ROOT).replaceFirstChar { it.titlecase(Locale.ROOT) }
                Triple(prefix, SpanishArticleGender.FEMININE, Pair(false, femIndefMatch.groupValues[2].trim()))
            }
            else -> Triple(null, SpanishArticleGender.NONE, Pair(false, workingText))
        }
    }

    /**
     * Analyzes a Spanish headword without altering its stored text:
     * - Identifies leading grammatical articles (El, La, Los, Las, El/La, Un, Una) or trailing parenthetical articles
     * - Identifies slash-separated dual forms (e.g., "Claro / clara", "el sobrino / la sobrina", "el paciente / la paciente")
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

        val hasEllipsis = cleaned.contains("...") || cleaned.contains("…")
        val hasExclamation = cleaned.startsWith("¡") || cleaned.endsWith("!")
        val hasQuestion = cleaned.startsWith("¿") || cleaned.endsWith("?")

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
        var slashVariantKeys = emptyList<String>()
        var isSlashDualForm = false

        val extracted = extractArticleAndCore(workingText)
        if (extracted.first != null) {
            articlePrefix = extracted.first
            gender = extracted.second
            isDefinite = extracted.third.first
            coreText = extracted.third.second
        }

        // Check for slash-separated dual forms such as "Claro / clara", "el sobrino / la sobrina", "doscientos/as", "magnífico/a"
        if (workingText.contains('/')) {
            val slashParts = workingText.split('/')
                .map { SURROUNDING_SPANISH_PUNCT_REGEX.replace(it.trim(), "").trim() }
                .filter { it.isNotBlank() }
            if (slashParts.size == 2) {
                val firstPart = slashParts[0]
                val secondPartRaw = slashParts[1]
                val expandedSecondPart = when {
                    secondPartRaw.equals("a", ignoreCase = true) && firstPart.endsWith("o", ignoreCase = true) ->
                        firstPart.dropLast(1) + "a"
                    secondPartRaw.equals("as", ignoreCase = true) && firstPart.endsWith("os", ignoreCase = true) ->
                        firstPart.dropLast(2) + "as"
                    secondPartRaw.equals("o", ignoreCase = true) && firstPart.endsWith("a", ignoreCase = true) ->
                        firstPart.dropLast(1) + "o"
                    secondPartRaw.equals("os", ignoreCase = true) && firstPart.endsWith("as", ignoreCase = true) ->
                        firstPart.dropLast(2) + "os"
                    else -> secondPartRaw
                }
                val partAnalyses = listOf(firstPart, expandedSecondPart).map { extractArticleAndCore(it) }
                val variantCores = partAnalyses.map { it.third.second }
                val variantKeys = variantCores.map { c ->
                    val raw = INLINE_PUNCT_FOR_KEY_REGEX.replace(c.lowercase(Locale.ROOT), " ")
                    WHITESPACE_REGEX.replace(raw, " ").trim()
                }.filter { it.isNotBlank() }.distinct()

                if (variantKeys.size == 1 && partAnalyses.any { it.first != null }) {
                    // Same core noun with both masculine and feminine articles: e.g. "el paciente / la paciente"
                    coreText = variantCores.first()
                    articlePrefix = "El/La"
                    gender = SpanishArticleGender.BOTH
                    isDefinite = true
                    isSlashDualForm = true
                    slashVariantKeys = variantKeys
                } else if (variantKeys.size == 2) {
                    isSlashDualForm = true
                    slashVariantKeys = variantKeys
                    if (partAnalyses.all { it.first != null }) {
                        gender = SpanishArticleGender.BOTH
                        isDefinite = partAnalyses.any { it.third.first }
                    }
                }
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
            trailingParenNote = trailingParenNote,
            slashVariantKeys = slashVariantKeys,
            isSlashDualForm = isSlashDualForm,
            hasEllipsis = hasEllipsis,
            hasExclamation = hasExclamation,
            hasQuestion = hasQuestion
        )
    }

    /**
     * Produces the Spanish duplicate comparison key (ignoring leading articles El/La/Los/Las/Un/Una,
     * surrounding punctuation, and trailing parenthetical notes without altering the actual word text).
     */
    fun spanishDedupeKey(text: String?): String = analyzeSpanishHeadword(text).dedupeKey

    /**
     * Computes Levenshtein edit distance between two strings.
     */
    fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in a.indices) {
            curr[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                curr[j + 1] = minOf(
                    curr[j] + 1,
                    prev[j + 1] + 1,
                    prev[j] + cost
                )
            }
            System.arraycopy(curr, 0, prev, 0, prev.size)
        }
        return prev[b.length]
    }

    private val SPANISH_DISTINCT_NEAR_SPELLING_PAIRS = setOf(
        "factura" to "factor",
        "material" to "materia",
        "luz de freno" to "luz de frenado",
        "disculpa" to "disculpe",
        "senal" to "sena",
        "alli" to "alla",
        "bueno" to "buen",
        "diferencial" to "diferencia",
        "empuje" to "empujon",
        "imperio" to "imperial",
        "portal" to "portada",
        "egoista" to "egoismo"
    )

    /**
     * Checks whether two Spanish normalized keys (already stripped of articles/accents) are near-spelling
     * variants (e.g., singular/plural "claro" vs "claros", masculine/feminine "tranquilo" vs "tranquila",
     * or 1-2 character typos/conjugation differences like "tienes" vs "tienen").
     */
    fun areSpanishNearSpellingVariants(keyA: String, keyB: String): Boolean {
        val a = keyA.trim().lowercase(Locale.ROOT)
        val b = keyB.trim().lowercase(Locale.ROOT)
        if (a.isEmpty() || b.isEmpty() || a == b) return false
        if (a.length < 4 || b.length < 4) return false
        if (kotlin.math.abs(a.length - b.length) > 2) return false
        if (isFalseSpanishGenderPair(a, b)) return false
        if ((a to b) in SPANISH_DISTINCT_NEAR_SPELLING_PAIRS || (b to a) in SPANISH_DISTINCT_NEAR_SPELLING_PAIRS) return false
        if (a == "${b}se" || b == "${a}se") return false

        val wordsA = a.split(WHITESPACE_REGEX)
        val wordsB = b.split(WHITESPACE_REGEX)
        if (wordsA.size != wordsB.size) return false

        // Must share the same starting character (avoids unrelated short rhymes like "casa" vs "masa" or "pato" vs "gato")
        if (a.first() != b.first()) return false

        val minLen = minOf(a.length, b.length)
        val commonPrefix = a.commonPrefixWith(b).length
        val minPrefix = if (minLen <= 5) minLen - 1 else minLen - 2
        if (commonPrefix < minPrefix) return false

        val dist = levenshteinDistance(a, b)
        val maxAllowed = if (minLen <= 5) 1 else 2
        return dist in 1..maxAllowed
    }

    private val PERSIAN_STOPWORDS = setOf(
        "در", "به", "از", "با", "تا", "را", "که", "این", "آن", "ان", "یک", "هم", "بر",
        "بی", "هر", "یا", "اما", "اگر", "برای", "چون", "است", "بود", "شد", "شود", "کند",
        "کنم", "کنی", "کنند", "دارد", "دارم", "داری", "خود", "من", "تو", "او", "ما", "شما", "آنها", "انها"
    )

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

        val derivationalSuffixes = listOf("دان", "ستان", "گر", "بان", "چی", "کار", "مند", "ور")
        for (a in keysA) {
            if (a.length < 5) continue
            for (b in keysB) {
                if (b.length < 5) continue
                val (shorter, longer) = if (a.length <= b.length) a to b else b to a
                if (longer.length >= 6 && longer.startsWith(shorter)) {
                    val remainder = longer.removePrefix(shorter)
                    if (remainder == "ی" || remainder in derivationalSuffixes) continue
                    if (remainder in listOf("ها", "های", "ات", "ان")) return true
                }
            }
        }

        // Token-level overlap for multi-word phrase/sentence translations
        val tokensA = atomicA
            .flatMap { PARENTHETICAL_BLOCK_REGEX.replace(it, "").split(PERSIAN_NON_ALNUM_REGEX) }
            .map { persianDedupeKey(it) }
            .filter { it.length >= 3 && it !in PERSIAN_STOPWORDS }
            .toSet()
        val tokensB = atomicB
            .flatMap { PARENTHETICAL_BLOCK_REGEX.replace(it, "").split(PERSIAN_NON_ALNUM_REGEX) }
            .map { persianDedupeKey(it) }
            .filter { it.length >= 3 && it !in PERSIAN_STOPWORDS }
            .toSet()

        if (tokensA.size >= 2 && tokensB.size >= 2) {
            val sharedCount = tokensA.intersect(tokensB).size
            val minTokens = minOf(tokensA.size, tokensB.size)
            if (sharedCount >= 2 || (minTokens >= 2 && sharedCount.toDouble() / minTokens >= 0.5)) {
                return true
            }
        }
        return false
    }

    private val SPANISH_SUBJECT_PRONOUNS = setOf(
        "yo", "tu", "el", "ella", "usted",
        "nosotros", "nosotras", "vosotros", "vosotras",
        "ellos", "ellas", "ustedes"
    )

    private fun stripSpanishAccents(text: String): String =
        DIACRITICS_REGEX.replace(Normalizer.normalize(text, Normalizer.Form.NFD), "")

    private val SPANISH_FALSE_PLURAL_PAIRS = setOf(
        "ante" to "antes",
        "gracia" to "gracias",
        "bien" to "bienes",
        "afuera" to "afueras",
        "coma" to "comas",
        "manual" to "manuales",
        "amarillo" to "amarillas",
        "amarilla" to "amarillas"
    )

    /**
     * Checks whether [pluralToken] is the regular Spanish plural form of [singularToken]
     * (after accent-stripping and lowercase normalization).
     * Covers:
     * - Words ending in 'z' -> '-ces' (e.g., luz -> luces, vez -> veces, voz -> voces)
     * - Words ending in a vowel (a, e, i, o, u) -> '-s' (e.g., testigo -> testigos, silla -> sillas)
     * - Words ending in a consonant (r, l, n, d, j, s, x, y) -> '-es' (e.g., señal -> señales, consumidor -> consumidores)
     */
    fun isSpanishWordPluralOf(pluralToken: String, singularToken: String): Boolean {
        val p = stripSpanishAccents(pluralToken.trim().lowercase(Locale.ROOT))
        val s = stripSpanishAccents(singularToken.trim().lowercase(Locale.ROOT))
        if (s.length < 3 || p.length < 4 || p == s) return false
        if ((s to p) in SPANISH_FALSE_PLURAL_PAIRS) return false
        return when {
            s.endsWith("z") -> p == s.dropLast(1) + "ces"
            s.last() in "aeiou" -> p == s + "s"
            s.last() in "rlndjsxy" -> p == s + "es"
            else -> false
        }
    }

    /**
     * Checks whether [pluralAnalysis] is the plural noun or noun-phrase form of [singularAnalysis].
     * Safely excludes sentences, questions, exclamations, slash-dual forms, pronoun-prefixed verb conjugations,
     * and 2nd-person singular ('tú') verb forms whose Persian translations include 'تو'.
     */
    fun isSpanishPhrasePluralOf(
        pluralAnalysis: SpanishHeadwordAnalysis,
        singularAnalysis: SpanishHeadwordAnalysis,
        pluralPersianTranslations: List<String> = emptyList()
    ): Boolean {
        if (pluralAnalysis.hasExclamation || pluralAnalysis.hasQuestion || pluralAnalysis.hasEllipsis) return false
        if (singularAnalysis.hasExclamation || singularAnalysis.hasQuestion || singularAnalysis.hasEllipsis) return false
        if (pluralAnalysis.isSlashDualForm) return false
        if (pluralAnalysis.coreText.any { it in ".?!¿¡" } || singularAnalysis.coreText.any { it in ".?!¿¡" }) return false

        val pWords = pluralAnalysis.accentInsensitiveKey.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        if (pWords.size !in 1..5 || pWords.first() in SPANISH_SUBJECT_PRONOUNS) return false

        if (pWords.size == 1 && pluralPersianTranslations.isNotEmpty()) {
            val hasSecondPersonPronoun = pluralPersianTranslations.any { tr ->
                val cleanedTr = cleanText(tr)
                cleanedTr.startsWith("تو ") || cleanedTr.contains(" تو ")
            }
            if (hasSecondPersonPronoun) return false
        }

        if (singularAnalysis.isSlashDualForm) {
            if (pWords.size != 1) return false
            return singularAnalysis.slashVariantKeys.any { variantKey ->
                val vClean = stripSpanishAccents(variantKey.trim())
                " " !in vClean && isSpanishWordPluralOf(pWords[0], vClean)
            }
        }

        val sWords = singularAnalysis.accentInsensitiveKey.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        if (pWords.size != sWords.size || sWords.first() in SPANISH_SUBJECT_PRONOUNS) return false

        if (!isSpanishWordPluralOf(pWords[0], sWords[0])) return false
        for (i in 1 until pWords.size) {
            val pw = pWords[i]
            val sw = sWords[i]
            if (pw != sw && !isSpanishWordPluralOf(pw, sw)) return false
        }
        return true
    }

    private fun singularCandidatesForWord(word: String): List<String> {
        val w = stripSpanishAccents(word.trim().lowercase(Locale.ROOT))
        if (w.length < 4) return emptyList()
        val result = mutableListOf<String>()
        if (w.length >= 5 && w.endsWith("ces")) {
            result.add(w.dropLast(3) + "z")
        }
        if (w.length >= 5 && w.endsWith("es") && w[w.length - 3] in "rlndjsxy") {
            result.add(w.dropLast(2))
        }
        if (w.length >= 4 && w.endsWith("s") && w[w.length - 2] in "aeiou") {
            result.add(w.dropLast(1))
        }
        return result.distinct()
    }

    /**
     * Generates candidate singular [SpanishHeadwordAnalysis.accentInsensitiveKey] values for a potentially
     * plural Spanish word or noun phrase so singular/plural pairs can be matched in O(1) bucket lookups.
     */
    fun spanishSingularCandidateKeys(analysis: SpanishHeadwordAnalysis): Set<String> {
        if (analysis.hasExclamation || analysis.hasQuestion || analysis.hasEllipsis || analysis.isSlashDualForm) {
            return emptySet()
        }
        if (analysis.coreText.any { it in ".?!¿¡" }) return emptySet()
        val words = analysis.accentInsensitiveKey.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        if (words.size !in 1..5 || words.first() in SPANISH_SUBJECT_PRONOUNS) return emptySet()

        val firstSingulars = singularCandidatesForWord(words[0])
        if (firstSingulars.isEmpty()) return emptySet()
        if (words.size == 1) return firstSingulars.toSet()

        var partialPhrases = firstSingulars
        for (i in 1 until words.size) {
            val w = words[i]
            val options = (listOf(w) + singularCandidatesForWord(w)).distinct()
            val nextPhrases = mutableListOf<String>()
            for (prefix in partialPhrases) {
                for (opt in options) {
                    nextPhrases.add("$prefix $opt")
                }
            }
            partialPhrases = nextPhrases
        }
        return partialPhrases.toSet()
    }

    /**
     * Converts a plural Spanish article prefix ("Los", "Las", "Los/Las", "Unos", "Unas")
     * to its corresponding singular article prefix ("El", "La", "El/La", "Un", "Una").
     */
    fun toSingularSpanishArticle(articlePrefix: String?): String? = when (articlePrefix) {
        "Los" -> "El"
        "Las" -> "La"
        "Los/Las" -> "El/La"
        "Unos" -> "Un"
        "Unas" -> "Una"
        else -> articlePrefix
    }

    /**
     * Formats a plural Spanish form (and its Persian translations, if any) into a structured note line
     * to attach to the singular concept.
     */
    fun formatPluralFormNote(
        pluralSourceText: String,
        pluralTranslations: List<String> = emptyList()
    ): String {
        val cleanPlural = cleanText(pluralSourceText)
        val cleanTrans = pluralTranslations.map { cleanText(it) }.filter { it.isNotBlank() }.distinct()
        return if (cleanTrans.isNotEmpty()) {
            "فرم جمع: $cleanPlural (${cleanTrans.joinToString("، ")})"
        } else {
            "فرم جمع: $cleanPlural"
        }
    }

    /**
     * Appends [newNote] to [existingNote] without duplicating lines or repeating the same plural form note.
     */
    fun mergeNotesWithPluralSupport(existingNote: String?, newNote: String?): String? {
        val lines = mutableListOf<String>()
        existingNote?.split("\n")?.map { it.trim() }?.filter { it.isNotBlank() }?.let { lines.addAll(it) }
        newNote?.split("\n")?.map { it.trim() }?.filter { it.isNotBlank() }?.forEach { candidate ->
            val pluralMatch = Regex("""^(?:فرم جمع|Plural form):\s*([^(（\n]+)""", RegexOption.IGNORE_CASE).find(candidate)
            if (pluralMatch != null) {
                val pluralHead = cleanText(pluralMatch.groupValues[1]).lowercase(Locale.ROOT)
                val alreadyHasThisPlural = lines.any { line ->
                    val m = Regex("""^(?:فرم جمع|Plural form):\s*([^(（\n]+)""", RegexOption.IGNORE_CASE).find(line)
                    m != null && cleanText(m.groupValues[1]).lowercase(Locale.ROOT) == pluralHead
                }
                if (!alreadyHasThisPlural) {
                    lines.add(candidate)
                }
            } else if (lines.none { it.equals(candidate, ignoreCase = true) }) {
                lines.add(candidate)
            }
        }
        return lines.joinToString("\n").ifBlank { null }
    }

    private val SPANISH_FALSE_GENDER_PAIRS = setOf(
        "libro" to "libra",
        "plazo" to "plaza",
        "puerto" to "puerta",
        "punto" to "punta",
        "modo" to "moda",
        "caso" to "casa",
        "piso" to "pisa",
        "peso" to "pesa",
        "paso" to "pasa",
        "velo" to "vela",
        "suelo" to "suela",
        "ramo" to "rama",
        "bolso" to "bolsa",
        "banco" to "banca",
        "barco" to "barca",
        "cesto" to "cesta",
        "cuadro" to "cuadra",
        "charco" to "charca",
        "fruto" to "fruta",
        "gorro" to "gorra",
        "huerto" to "huerta",
        "jarro" to "jarra",
        "leno" to "lena",
        "manzano" to "manzana",
        "naranjo" to "naranja",
        "cerezo" to "cereza",
        "almendro" to "almendra",
        "olivo" to "oliva",
        "caballo" to "caballa",
        "foco" to "foca",
        "pato" to "pata",
        "rato" to "rata",
        "resto" to "resta",
        "cargo" to "carga",
        "pago" to "paga",
        "politico" to "politica",
        "fisico" to "fisica",
        "quimico" to "quimica",
        "musico" to "musica",
        "tecnico" to "tecnica",
        "critico" to "critica",
        "practico" to "practica",
        "logico" to "logica",
        "etico" to "etica",
        "estetico" to "estetica",
        "informatico" to "informatica",
        "matematico" to "matematica",
        "estadistico" to "estadistica",
        "mecanico" to "mecanica",
        "botanico" to "botanica",
        "gramatico" to "gramatica",
        "derecho" to "derecha",
        "izquierdo" to "izquierda",
        "puesto" to "puesta",
        "presupuesto" to "presupuesta",
        "impuesto" to "impuesta",
        "cuento" to "cuenta",
        "vuelto" to "vuelta",
        "partido" to "partida",
        "herido" to "herida",
        "perdono" to "perdona",
        "recuerdo" to "recuerda",
        "estudio" to "estudia",
        "pino" to "pina",
        "bando" to "banda",
        "dicho" to "dicha",
        "pasado" to "pasada",
        "busco" to "busca",
        "odio" to "odia",
        "uso" to "usa",
        "trato" to "trata",
        "tomo" to "toma",
        "desmayo" to "desmaya",
        "descuido" to "descuida",
        "helado" to "helada",
        "acero" to "acera",
        "trono" to "trona",
        "llanto" to "llanta",
        "tormento" to "tormenta",
        "manto" to "manta",
        "pasto" to "pasta",
        "conducto" to "conducta",
        "calzado" to "calzada",
        "tramo" to "trama",
        "rodillo" to "rodilla",
        "mango" to "manga",
        "haciendo" to "hacienda",
        "lanzo" to "lanza",
        "plato" to "plata",
        "correo" to "correa",
        "capo" to "capa",
        "riendo" to "rienda",
        "rayo" to "raya",
        "oro" to "ora",
        "flor" to "flora",
        "palo" to "pala",
        "reino" to "reina",
        "arco" to "arca",
        "marco" to "marca",
        "bolo" to "bola",
        "bombo" to "bomba",
        "seno" to "sena",
        "cubo" to "cuba",
        "barro" to "barra",
        "indio" to "india",
        "judio" to "judia",
        "moro" to "mora",
        "cuerdo" to "cuerda",
        "parejo" to "pareja",
        "visto" to "vista",
        "carretero" to "carretera",
        "parado" to "parada",
        "entrado" to "entrada",
        "llegado" to "llegada",
        "despedido" to "despedida",
        "llamado" to "llamada",
        "bajo" to "baja",
        "junto" to "junta",
        "caro" to "cara",
        "comodo" to "comoda",
        "minimo" to "minima",
        "recto" to "recta",
        "muneco" to "muneca",
        "chino" to "china",
        "negativo" to "negativa",
        "castano" to "castana",
        "mediano" to "mediana",
        "fiero" to "fiera",
        "perdon" to "perdona"
    )

    fun isFalseSpanishGenderPair(wordA: String, wordB: String): Boolean {
        val a = stripSpanishAccents(wordA.trim().lowercase(Locale.ROOT))
        val b = stripSpanishAccents(wordB.trim().lowercase(Locale.ROOT))
        return (a to b) in SPANISH_FALSE_GENDER_PAIRS || (b to a) in SPANISH_FALSE_GENDER_PAIRS
    }

    private val PERSIAN_GENDER_REPLACEMENTS = listOf(
        "پدربزرگ" to "مادربزرگ",
        "ناپدری" to "نامادری",
        "پدرزن" to "مادرزن",
        "پدرشوهر" to "مادرشوهر",
        "برادرزن" to "خواهرزن",
        "برادرشوهر" to "خواهرشوهر",
        "پسرعمو" to "دخترعمو",
        "پسرعمه" to "دخترعمه",
        "پسردایی" to "دختردایی",
        "پسرخاله" to "دخترخاله",
        "دوست‌پسر" to "دوست‌دختر",
        "دوست پسر" to "دوست دختر",
        "پسر" to "دختر",
        "مرد" to "زن",
        "برادر" to "خواهر",
        "عمو" to "عمه",
        "دایی" to "خاله",
        "پدر" to "مادر",
        "شوهر" to "همسر",
        "داماد" to "عروس",
        "مذکر" to "مؤنث",
        "مونث" to "مؤنث"
    )

    private val PERSIAN_GENDER_QUALIFIER_WORDS_REGEX =
        Regex("""(?:^|\s+)(?:خانم|آقا|مؤنث|مونث|مذکر)(?=\s+|$)""")

    private fun neutralizePersianGenderTerms(text: String): String {
        var normalized = cleanText(text)
        val withoutQualifiers = PERSIAN_GENDER_QUALIFIER_WORDS_REGEX.replace(normalized, " ").trim()
        if (withoutQualifiers.isNotBlank()) {
            normalized = withoutQualifiers
        }
        for ((mascTerm, femTerm) in PERSIAN_GENDER_REPLACEMENTS) {
            normalized = normalized.replace(femTerm, mascTerm)
        }
        return normalized
    }

    private fun isLikelyConjugatedPersianVerbOrImperative(translations: List<String>): Boolean {
        if (translations.isEmpty()) return false
        val verbMarkers = listOf("می‌کند", "می کند", "می‌آورد", "می آورد", "می‌خواهم", "می خواهم", "ببخشید", "بیاور")
        return translations.any { tr ->
            val cleaned = cleanText(tr)
            verbMarkers.any { marker -> cleaned.contains(marker) }
        }
    }

    /**
     * Checks whether [mascToken] and [femToken] are regular Spanish masculine and feminine word counterparts
     * (in that order: masculine first, feminine second), excluding false cognates/homographs.
     */
    fun isSpanishWordMasculineOfFeminine(mascToken: String, femToken: String): Boolean {
        val m = stripSpanishAccents(mascToken.trim().lowercase(Locale.ROOT))
        val f = stripSpanishAccents(femToken.trim().lowercase(Locale.ROOT))
        if (m.length < 3 || f.length < 3 || m == f) return false
        if (" " in m || " " in f || "/" in m || "/" in f) return false
        if ((m to f) in SPANISH_FALSE_GENDER_PAIRS) return false

        return when {
            m.endsWith("o") -> f == m.dropLast(1) + "a"
            m.endsWith("or") -> f == m + "a"
            m.length >= 4 && (m.endsWith("on") || m.endsWith("es") || m.endsWith("an") || m.endsWith("in")) -> f == m + "a"
            else -> false
        }
    }

    private fun isOrderedSpanishMascFemAnalysisPair(
        mascAnalysis: SpanishHeadwordAnalysis,
        femAnalysis: SpanishHeadwordAnalysis,
        mascTranslations: List<String> = emptyList(),
        femTranslations: List<String> = emptyList()
    ): Boolean {
        if (mascAnalysis.hasExclamation || mascAnalysis.hasQuestion || mascAnalysis.hasEllipsis) return false
        if (femAnalysis.hasExclamation || femAnalysis.hasQuestion || femAnalysis.hasEllipsis) return false
        if (mascAnalysis.isSlashDualForm || femAnalysis.isSlashDualForm) return false
        if (mascAnalysis.coreText.any { it in ".?!¿¡" } || femAnalysis.coreText.any { it in ".?!¿¡" }) return false

        if (mascAnalysis.gender == SpanishArticleGender.FEMININE) return false
        if (femAnalysis.gender == SpanishArticleGender.MASCULINE) return false

        val mWords = mascAnalysis.accentInsensitiveKey.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        val fWords = femAnalysis.accentInsensitiveKey.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        if (mWords.size != 1 || fWords.size != 1) return false
        if (!isSpanishWordMasculineOfFeminine(mWords[0], fWords[0])) return false

        if (mascTranslations.isNotEmpty() && femTranslations.isNotEmpty()) {
            val mascHasVerb = isLikelyConjugatedPersianVerbOrImperative(mascTranslations)
            val femHasVerb = isLikelyConjugatedPersianVerbOrImperative(femTranslations)
            if (mascHasVerb != femHasVerb) return false

            if (hasPersianTranslationOverlap(mascTranslations, femTranslations)) return true
            val neutralizedMasc = mascTranslations.map { neutralizePersianGenderTerms(it) }
            val neutralizedFem = femTranslations.map { neutralizePersianGenderTerms(it) }
            return hasPersianTranslationOverlap(neutralizedMasc, neutralizedFem)
        }
        return true
    }

    /**
     * Checks whether [firstAnalysis] and [secondAnalysis] (in either order) form a valid Spanish
     * masculine/feminine word pair that should be unified with a slash headword.
     */
    fun isSpanishMasculineFemininePair(
        firstAnalysis: SpanishHeadwordAnalysis,
        secondAnalysis: SpanishHeadwordAnalysis,
        firstTranslations: List<String> = emptyList(),
        secondTranslations: List<String> = emptyList()
    ): Boolean {
        return isOrderedSpanishMascFemAnalysisPair(firstAnalysis, secondAnalysis, firstTranslations, secondTranslations) ||
            isOrderedSpanishMascFemAnalysisPair(secondAnalysis, firstAnalysis, secondTranslations, firstTranslations)
    }

    /**
     * Generates candidate partner [SpanishHeadwordAnalysis.accentInsensitiveKey] values for a single-word
     * Spanish masculine or feminine headword so gender pairs can be matched in O(1) bucket lookups.
     */
    fun spanishGenderPartnerCandidateKeys(analysis: SpanishHeadwordAnalysis): Set<String> {
        if (analysis.hasExclamation || analysis.hasQuestion || analysis.hasEllipsis || analysis.isSlashDualForm) {
            return emptySet()
        }
        if (analysis.coreText.any { it in ".?!¿¡" }) return emptySet()
        val words = analysis.accentInsensitiveKey.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
        if (words.size != 1) return emptySet()
        val w = words[0]
        if (w.length < 3 || w in SPANISH_SUBJECT_PRONOUNS) return emptySet()

        val result = mutableSetOf<String>()
        if (w.endsWith("o")) {
            val fem = w.dropLast(1) + "a"
            if ((w to fem) !in SPANISH_FALSE_GENDER_PAIRS) result.add(fem)
        }
        if (w.endsWith("or") || (w.length >= 4 && (w.endsWith("on") || w.endsWith("es") || w.endsWith("an") || w.endsWith("in")))) {
            val fem = w + "a"
            if ((w to fem) !in SPANISH_FALSE_GENDER_PAIRS) result.add(fem)
        }
        if (w.endsWith("a") && w.length >= 3) {
            val stem = w.dropLast(1)
            val mascO = stem + "o"
            if ((mascO to w) !in SPANISH_FALSE_GENDER_PAIRS) result.add(mascO)
            if (stem.endsWith("or") || (stem.length >= 4 && (stem.endsWith("on") || stem.endsWith("es") || stem.endsWith("an") || stem.endsWith("in")))) {
                if ((stem to w) !in SPANISH_FALSE_GENDER_PAIRS) result.add(stem)
            }
        }
        return result
    }

    /**
     * Formats a Spanish masculine and feminine pair using a slash ("/"):
     * - With definite articles: "El director / la directora", "El niño / la niña"
     * - With indefinite articles: "Un niño / una niña"
     * - Without articles: "Rojo / roja", "Hijo / hija"
     */
    fun formatMasculineFeminineSlashHeadword(firstText: String, secondText: String): String {
        val a1 = analyzeSpanishHeadword(firstText)
        val a2 = analyzeSpanishHeadword(secondText)
        val (masc, fem) = if (isOrderedSpanishMascFemAnalysisPair(a1, a2)) {
            a1 to a2
        } else {
            a2 to a1
        }
        val mascCore = masc.coreText.trim().lowercase(Locale.ROOT)
        val femCore = fem.coreText.trim().lowercase(Locale.ROOT)
        val hasDefinite = (masc.articlePrefix != null && masc.isDefiniteArticle) ||
            (fem.articlePrefix != null && fem.isDefiniteArticle)
        val hasIndefinite = !hasDefinite && (masc.articlePrefix != null || fem.articlePrefix != null)

        return when {
            hasDefinite -> "El $mascCore / la $femCore"
            hasIndefinite -> "Un $mascCore / una $femCore"
            else -> {
                val mascCap = if (mascCore.isNotEmpty()) {
                    mascCore.first().titlecase(Locale.ROOT) + mascCore.substring(1)
                } else {
                    mascCore
                }
                "$mascCap / $femCore"
            }
        }
    }

    /**
     * Selects the preferred Spanish display headword when merging duplicate entries:
     * - Prioritizes dual slash forms (e.g., "Claro / clara" or "el sobrino / la sobrina") when merging with a single variant.
     * - Synthesizes a dual slash form (e.g., "Rojo / roja" or "El director / la directora") when merging a masculine/feminine pair.
     * - Prioritizes singular forms over plural forms (e.g., "Testigo" over "Testigos", "La luz de emergencia" over "Luces de emergencia"),
     *   converting plural articles ("Los"/"Las") to singular ("El"/"La") when only the plural entry carried an article.
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

        // If one of the candidates is a dual slash form with 2 distinct variants (e.g., "Claro / clara" or "el sobrino / la sobrina"),
        // prefer the dual slash form so both grammatical forms are preserved.
        val bestSlashDual = analyses
            .filter { it.isSlashDualForm && it.slashVariantKeys.size >= 2 }
            .maxByOrNull { analysis ->
                var score = 100
                if (analysis.originalText.contains("el ", ignoreCase = true) || analysis.originalText.contains("la ", ignoreCase = true)) score += 40
                score += analysis.originalText.count { ch -> ch in SPANISH_ACCENT_CHARS } * 15
                score
            }
        if (bestSlashDual != null) {
            val rawSlash = bestSlashDual.originalText
            return if (rawSlash.isNotEmpty() && rawSlash.first().isLowerCase()) {
                rawSlash.first().titlecase(Locale.ROOT) + rawSlash.substring(1)
            } else {
                rawSlash
            }
        }

        // Prefer singular forms over plural forms when both exist in the group
        val nonPluralAnalyses = analyses.filter { candidate ->
            analyses.none { other ->
                other !== candidate && isSpanishPhrasePluralOf(candidate, other)
            }
        }.ifEmpty { analyses }
        val hasSingularPluralMix = nonPluralAnalyses.size < analyses.size

        // Check if non-plural candidates contain a masculine/feminine pair (e.g., "Rojo" and "Roja" or "El director" and "la directora")
        for (i in nonPluralAnalyses.indices) {
            for (j in i + 1 until nonPluralAnalyses.size) {
                val first = nonPluralAnalyses[i]
                val second = nonPluralAnalyses[j]
                if (isSpanishMasculineFemininePair(first, second)) {
                    return formatMasculineFeminineSlashHeadword(first.originalText, second.originalText)
                }
            }
        }

        val hasBothMascAndFem = (analyses.any { it.gender == SpanishArticleGender.MASCULINE } &&
            analyses.any { it.gender == SpanishArticleGender.FEMININE }) ||
            analyses.any { it.gender == SpanishArticleGender.BOTH }

        val bestArticleAnalysis = analyses
            .filter { it.articlePrefix != null }
            .maxByOrNull { analysis ->
                var score = 0
                if (analysis.isDefiniteArticle) score += 100 else score += 60
                if (analysis in nonPluralAnalyses) score += 20
                if (hasBothMascAndFem && analysis.gender == SpanishArticleGender.BOTH) score += 30
                if (analysis.coreText.any { ch -> ch in SPANISH_ACCENT_CHARS }) score += 25
                if (analysis.trailingParenNote == null) score += 10
                if (analysis.originalText.firstOrNull()?.isUpperCase() == true) score += 5
                score
            }

        val bestCoreAnalysis = nonPluralAnalyses.maxByOrNull { analysis ->
            var score = 0
            score += analysis.coreText.count { ch -> ch in SPANISH_ACCENT_CHARS } * 30
            if (analysis.trailingParenNote == null) score += 10
            if ((analysis.originalText.startsWith("¡") && analysis.originalText.endsWith("!")) ||
                (analysis.originalText.startsWith("¿") && analysis.originalText.endsWith("?"))
            ) {
                score += 8
            }
            if (!analysis.hasEllipsis) score += 4
            val firstLetter = analysis.originalText.firstOrNull { it.isLetter() }
            if (firstLetter?.isUpperCase() == true) score += 5
            if (analysis.articlePrefix != null && analysis.isDefiniteArticle) score += 5
            score
        } ?: nonPluralAnalyses.first()

        if (bestArticleAnalysis != null) {
            val rawPrefix = if (hasBothMascAndFem && bestArticleAnalysis.gender != SpanishArticleGender.BOTH) {
                "El/La"
            } else {
                bestArticleAnalysis.articlePrefix!!
            }
            val chosenPrefix = if (hasSingularPluralMix || bestCoreAnalysis in nonPluralAnalyses) {
                toSingularSpanishArticle(rawPrefix) ?: rawPrefix
            } else {
                rawPrefix
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
