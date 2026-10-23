package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.FieldValidationResult
import com.manidigit.yadin.domain.model.ReviewAction
import com.manidigit.yadin.domain.text.TextUtilities

object FieldValidationEngine {

    private val REGEX_PAREN_BRACKET = Regex("""^(.*?)\s*[\(\[（]([^\)\]）]+)[\)\]）]\s*$""")
    private val REGEX_TECHNICAL_CODE = Regex("""^[A-Za-z0-9\-_./\s]{1,10}$""")

    /**
     * بررسی، اعتبارسنجی و تعیین تکلیف محافظه‌کارانه فیلدهای واژه و ترجمه بر اساس سند اجرایی:
     * - سالم (KEEP): متن اسپانیایی در فیلد کلمه و متن فارسی در فیلد ترجمه معتبر است.
     * - قابل انتقال به یادداشت (MOVE_TO_NOTE): متن اضافی یا توضیحی با اطمینان بالای ۹۸٪ که تفکیک‌پذیر است.
     * - پیشنهاد اصلاح (SUGGEST_CORRECTION): اطمینان ۸۰٪ تا ۹۸٪ برای تأیید کاربر.
     * - نیازمند بازبینی دستی (MANUAL_REVIEW): متن مخلوط، مبهم، چندخطی با چند مفهوم مجزا، ناقص یا اطمینان کمتر از ۸۰٪.
     */
    fun validateRecord(
        word: String,
        translations: List<String>,
        existingNote: String? = null
    ): FieldValidationResult {
        val trimmedWord = word.trim()
        val translationCombined = translations.joinToString("، ").trim()

        if (trimmedWord.isBlank()) {
            return FieldValidationResult(
                action = ReviewAction.MANUAL_REVIEW,
                confidence = 0.1,
                originalWord = trimmedWord,
                originalTranslation = translationCombined,
                suggestedWord = null,
                suggestedTranslation = translationCombined,
                suggestedNote = existingNote,
                reason = "فیلد واژه خالی است"
            )
        }

        // ۱. بررسی خطوط چندگانه (مانند Dispositivo V-16 چراغ هشدار V-16\nPresión de los neumáticos)
        val lines = trimmedWord.replace("\r\n", "\n").replace("\r", "\n")
            .split("\n").map { it.trim() }.filter { it.isNotBlank() }

        if (lines.size >= 2) {
            val firstLine = lines[0]
            val otherLines = lines.drop(1)
            val otherLinesLatin = otherLines.filter { VocabularyParser.hasLatin(it) }

            // اگر سطرهای بعدی خود شامل واژگان لاتین مستقل باشند (مانند Presión de los neumáticos)
            // طبق قاعده: "متن ناقص است یا انتقال آن باعث حذف اطلاعات می‌شود -> بازبینی"
            if (otherLinesLatin.isNotEmpty()) {
                val suggestedWord = firstLine
                val noteFromOtherLines = otherLines.joinToString("\n")
                val combinedNote = listOfNotNull(existingNote, "مفاهیم الحاقی سطر بعدی: $noteFromOtherLines")
                    .joinToString("\n")

                return FieldValidationResult(
                    action = ReviewAction.MANUAL_REVIEW,
                    confidence = 0.60,
                    originalWord = trimmedWord,
                    originalTranslation = translationCombined,
                    suggestedWord = suggestedWord,
                    suggestedTranslation = translationCombined,
                    suggestedNote = combinedNote,
                    reason = "متن چندخطی شامل چندین واژه و مفهوم مجزا است؛ نیازمند بازبینی دستی جهت جلوگیری از حذف اطلاعات"
                )
            } else {
                // سطرهای بعدی فقط ترجمه فارسی یا توضیحات هستند
                val persianLines = otherLines.filter { VocabularyParser.hasPersian(it) }
                val nonPersianLines = otherLines.filter { !VocabularyParser.hasPersian(it) }
                val suggestedWord = firstLine
                val newTrans = (translations + persianLines).distinct().joinToString("، ")
                val newNote = listOfNotNull(existingNote, nonPersianLines.joinToString("\n").ifBlank { null }).joinToString("\n")

                return FieldValidationResult(
                    action = ReviewAction.MOVE_TO_NOTE,
                    confidence = 0.92,
                    originalWord = trimmedWord,
                    originalTranslation = translationCombined,
                    suggestedWord = suggestedWord,
                    suggestedTranslation = newTrans,
                    suggestedNote = newNote.ifBlank { null },
                    reason = "تفکیک سطرهای اضافی و انتقال توضیحات به یادداشت"
                )
            }
        }

        val singleLine = lines.firstOrNull() ?: trimmedWord

        // ۲. بررسی وجود هم‌زمان فارسی و لاتین در فیلد کلمه (اسپانیایی)
        val wordHasLatin = VocabularyParser.hasLatin(singleLine)
        val wordHasPersian = VocabularyParser.hasPersian(singleLine)

        if (wordHasLatin && wordHasPersian) {
            // آیا یک عبارت ترکیبی معتبر با جداکننده واضح است یا اختلاط نامطمئن؟
            val pair = VocabularyParser.splitPair(singleLine)
            if (pair != null && pair.first.isNotBlank() && pair.second.isNotBlank()) {
                val left = pair.first.trim()
                val right = pair.second.trim()
                if (VocabularyParser.hasLatin(left) && !VocabularyParser.hasPersian(left) && VocabularyParser.hasPersian(right)) {
                    // جداکننده استاندارد واضح مثل "casa : خانه"
                    val combinedTrans = (translations + TextUtilities.splitTranslations(right)).distinct().joinToString("، ")
                    return FieldValidationResult(
                        action = ReviewAction.SUGGEST_CORRECTION,
                        confidence = 0.95,
                        originalWord = singleLine,
                        originalTranslation = translationCombined,
                        suggestedWord = left,
                        suggestedTranslation = combinedTrans,
                        suggestedNote = existingNote,
                        reason = "جداسازی ترجمه چسبیده با جداکننده صریح ($right)"
                    )
                }
            }

            // پرانتز حاوی ترجمه فارسی مثل "libro (کتاب)"
            val parenMatch = REGEX_PAREN_BRACKET.find(singleLine)
            if (parenMatch != null) {
                val main = parenMatch.groupValues[1].trim()
                val inside = parenMatch.groupValues[2].trim()
                if (main.isNotBlank() && inside.isNotBlank() && !VocabularyParser.hasPersian(main) && VocabularyParser.hasPersian(inside)) {
                    val combinedTrans = (translations + TextUtilities.splitTranslations(inside)).distinct().joinToString("، ")
                    return FieldValidationResult(
                        action = ReviewAction.MOVE_TO_NOTE,
                        confidence = 0.98,
                        originalWord = singleLine,
                        originalTranslation = translationCombined,
                        suggestedWord = main,
                        suggestedTranslation = combinedTrans,
                        suggestedNote = existingNote,
                        reason = "انتقال ترجمه داخل پرانتز به فیلد ترجمه ($inside)"
                    )
                }
            }

            // اختلاط بدون جداکننده مشخص (مانند Dispositivo V-16 چراغ هشدار V-16)
            // طبق قاعده: "هر دو فیلد یا فیلد کلمه ترکیبی از فارسی و اسپانیایی بدون جداکننده صریح -> بازبینی دستی"
            val (separatedWord, separatedPersian) = separateLatinAndPersian(singleLine)
            val suggestedWord = separatedWord.ifBlank { singleLine }
            val suggestedTrans = if (separatedPersian.isNotBlank()) {
                (translations + TextUtilities.splitTranslations(separatedPersian)).distinct().joinToString("، ")
            } else {
                translationCombined
            }

            return FieldValidationResult(
                action = ReviewAction.MANUAL_REVIEW,
                confidence = 0.70,
                originalWord = singleLine,
                originalTranslation = translationCombined,
                suggestedWord = suggestedWord,
                suggestedTranslation = suggestedTrans,
                suggestedNote = existingNote,
                reason = "حضور همزمان زبان اسپانیایی و فارسی در فیلد کلمه بدون جداکننده صریح؛ نیازمند بازبینی دستی"
            )
        }

        // ۳. بررسی فیلد کلمه‌ای که فقط فارسی است
        if (!wordHasLatin && wordHasPersian) {
            return FieldValidationResult(
                action = ReviewAction.MANUAL_REVIEW,
                confidence = 0.40,
                originalWord = singleLine,
                originalTranslation = translationCombined,
                suggestedWord = translationCombined.ifBlank { singleLine },
                suggestedTranslation = singleLine,
                suggestedNote = existingNote,
                reason = "فیلد کلمه به زبان فارسی درج شده و احتمال جابه‌جایی فیلدها وجود دارد"
            )
        }

        // ۴. بررسی پرانتزهای نقش گرامری کوتاه (فقط برچسب‌های صریح دستوری مانند correr (v.) یا apple [n.])
        // عبارات معتبر واژگانی مانند "El chancla (Las chanclas)" یا "Press de banca (Bench press)" یا "Dominadas (Pull-ups)"
        // بخشی از خود اصطلاح هستند و هرگز نباید به یادداشت منتقل شوند!
        val parenMatch = REGEX_PAREN_BRACKET.find(singleLine)
        if (parenMatch != null) {
            val main = parenMatch.groupValues[1].trim()
            val inside = parenMatch.groupValues[2].trim()
            if (main.isNotBlank() && inside.isNotBlank() && isStrictGrammarPosTag(inside)) {
                val combinedNote = listOfNotNull(existingNote, "نقش/توضیح: $inside").joinToString("\n")
                return FieldValidationResult(
                    action = ReviewAction.MOVE_TO_NOTE,
                    confidence = 0.98,
                    originalWord = singleLine,
                    originalTranslation = translationCombined,
                    suggestedWord = main,
                    suggestedTranslation = translationCombined,
                    suggestedNote = combinedNote,
                    reason = "انتقال نقش یا توضیحات داخل پرانتز به یادداشت ($inside)"
                )
            }
        }

        // ۵. بررسی نشانه‌گذاری اسپانیایی و علائم آسیب‌دیده (مانند ¿Cuánto es la carrera;¿)
        val (normWord, punctMods) = TextUtilities.normalizeSpanishPunctuation(singleLine)
        if (normWord != singleLine) {
            return FieldValidationResult(
                action = ReviewAction.SUGGEST_CORRECTION,
                confidence = 0.98,
                originalWord = singleLine,
                originalTranslation = translationCombined,
                suggestedWord = normWord,
                suggestedTranslation = translationCombined,
                suggestedNote = existingNote,
                reason = "اصلاح نشانه‌گذاری اسپانیایی: ${punctMods.joinToString("، ")}"
            )
        }

        // ۶. بررسی شماره‌گذاری‌ها یا گلوله‌های نگارشی آغازین (مانند 1. hablar یا • comer)
        val strippedNum = VocabularyAuditService.stripNumberingAndBullets(singleLine)
        if (strippedNum != singleLine && strippedNum.isNotBlank()) {
            return FieldValidationResult(
                action = ReviewAction.SUGGEST_CORRECTION,
                confidence = 0.99,
                originalWord = singleLine,
                originalTranslation = translationCombined,
                suggestedWord = strippedNum,
                suggestedTranslation = translationCombined,
                suggestedNote = existingNote,
                reason = "حذف شماره‌گذاری یا علامت گلوله آغازین"
            )
        }

        // ۷. بررسی نقل‌قول‌های احاطه‌کننده (مانند "casa")
        val strippedQuotes = VocabularyAuditService.stripSurroundingQuotes(singleLine)
        if (strippedQuotes != singleLine && strippedQuotes.isNotBlank()) {
            return FieldValidationResult(
                action = ReviewAction.SUGGEST_CORRECTION,
                confidence = 0.99,
                originalWord = singleLine,
                originalTranslation = translationCombined,
                suggestedWord = strippedQuotes,
                suggestedTranslation = translationCombined,
                suggestedNote = existingNote,
                reason = "حذف گیومه‌های احاطه‌کننده واژه"
            )
        }

        // ۸. بررسی فیلد ترجمه
        if (translationCombined.isBlank()) {
            return FieldValidationResult(
                action = ReviewAction.MANUAL_REVIEW,
                confidence = 0.40,
                originalWord = singleLine,
                originalTranslation = translationCombined,
                suggestedWord = singleLine,
                suggestedTranslation = "",
                suggestedNote = existingNote,
                reason = "ترجمه برای این واژه ثبت نشده است؛ نیازمند بازبینی دستی"
            )
        }

        // بررسی حضور اسپانیایی در فیلد ترجمه:
        // "وجود هم‌زمان فارسی و اسپانیایی به‌تنهایی دلیل خرابی نیست. مثلاً ممکن است ترجمه شامل یک نام اسپانیایی، کد فنی یا مثال معتبر باشد."
        // اگر متن ترجمه عمدتاً فارسی است و واژه لاتین داخل پرانتز یا یک کد فنی/تایمر کوتاه (مثل HIIT، TRX، BMI، V-16، RPE) است، کاملاً سالم است.
        val translationWithoutParens = translationCombined.replace(Regex("""[\(\[（«][^\)\]）»]*[\)\]）»]"""), "").trim()
        if (VocabularyParser.hasLatin(translationWithoutParens)) {
            val latinWords = translationWithoutParens.split(Regex("""\s+"""))
                .map { it.trim('،', ',', '.', ';', '؛', ':', '!', '؟', '?', '"', '\'', '«', '»', '(', ')') }
                .filter { VocabularyParser.hasLatin(it) }
            val allAreShortCodes = latinWords.all { wordToken ->
                wordToken.length <= 8 || REGEX_TECHNICAL_CODE.matches(wordToken)
            }
            if (!allAreShortCodes) {
                // آیا جداکننده صریح در ترجمه است مثل "اتومبیل - coche"؟
                val transPair = VocabularyParser.splitPair(translationCombined)
                if (transPair != null) {
                    return FieldValidationResult(
                        action = ReviewAction.SUGGEST_CORRECTION,
                        confidence = 0.85,
                        originalWord = singleLine,
                        originalTranslation = translationCombined,
                        suggestedWord = singleLine,
                        suggestedTranslation = transPair.first.trim(),
                        suggestedNote = listOfNotNull(existingNote, "عبارت لاتین در ترجمه: ${transPair.second.trim()}").joinToString("\n"),
                        reason = "وجود عبارت لاتین با جداکننده در فیلد ترجمه؛ پیشنهاد تفکیک به یادداشت"
                    )
                } else {
                    return FieldValidationResult(
                        action = ReviewAction.MANUAL_REVIEW,
                        confidence = 0.75,
                        originalWord = singleLine,
                        originalTranslation = translationCombined,
                        suggestedWord = singleLine,
                        suggestedTranslation = translationCombined,
                        suggestedNote = existingNote,
                        reason = "فیلد ترجمه شامل کلمات لاتین است؛ نیازمند بازبینی دستی"
                    )
                }
            }
        }

        // واژه و ترجمه سالم و معتبر است
        return FieldValidationResult(
            action = ReviewAction.KEEP,
            confidence = 1.0,
            originalWord = singleLine,
            originalTranslation = translationCombined,
            suggestedWord = singleLine,
            suggestedTranslation = translationCombined,
            suggestedNote = existingNote,
            reason = "کلمه اسپانیایی و ترجمه فارسی معتبر و سالم است"
        )
    }

    private val STRICT_POS_TAGS = setOf(
        "n", "n.", "v", "v.", "vt", "vt.", "vi", "vi.", "adj", "adj.", "adv", "adv.",
        "prep", "prep.", "conj", "conj.", "pron", "pron.", "interj", "interj.",
        "m", "m.", "f", "f.", "pl", "pl.", "sing", "sing.", "inf", "inf.",
        "noun", "verb", "adjective", "adverb", "sustantivo", "verbo", "adjetivo", "adverbio"
    )

    fun isStrictGrammarPosTag(inside: String): Boolean {
        val normalized = inside.trim().lowercase()
        return STRICT_POS_TAGS.contains(normalized)
    }

    private fun separateLatinAndPersian(text: String): Pair<String, String> {
        var firstPersian = -1
        var lastPersian = -1
        for (i in text.indices) {
            val code = text[i].code
            if ((code in 0x0600..0x06FF) || (code in 0x0750..0x077F) || (code in 0x08A0..0x08FF)) {
                if (firstPersian == -1) firstPersian = i
                lastPersian = i
            }
        }
        if (firstPersian != -1 && lastPersian != -1) {
            var start = firstPersian
            var end = lastPersian + 1
            while (start > 0 && (text[start - 1] == ' ' || text[start - 1] == '-' || text[start - 1] == '(')) start--
            while (end < text.length && (text[end] == ' ' || text[end] == '-' || text[end] == ')')) end++
            val persianPart = text.substring(start, end).trim()
            val latinPart = (text.substring(0, start) + " " + text.substring(end)).trim().replace(Regex("""\s+"""), " ")
            return Pair(latinPart, persianPart)
        }
        return Pair(text, "")
    }
}
