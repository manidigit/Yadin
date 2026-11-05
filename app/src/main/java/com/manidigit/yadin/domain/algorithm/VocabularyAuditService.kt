package com.manidigit.yadin.domain.algorithm

import androidx.room.withTransaction
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.database.YadinDatabase
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.DifficultyStateEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.data.local.entity.SettingEntity
import com.manidigit.yadin.domain.model.AuditActionDetail
import com.manidigit.yadin.domain.model.AuditActionType
import com.manidigit.yadin.domain.model.AuditReviewItem
import com.manidigit.yadin.domain.model.AuditUserDecision
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.LibraryAuditReport
import com.manidigit.yadin.domain.model.ReviewAction
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.text.TextUtilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * سرویس بررسی، پالایش، پاکسازی و ادغام هوشمند واژگان کتابخانه (Library Audit & Smart Merge Service)
 *
 * قابلیت‌ها:
 * ۱. اسکن اولیه ایمن و گزارش‌گیری بدون تغییر داده (Pre-Scan / Audit Report Mode).
 * ۲. تشخیص، تفکیک و تشکیل صف بازبینی دستی برای موارد مشکوک، چندخطی یا با اطمینان زیر ۸۰٪.
 * ۳. امکان ویرایش دستی و انتقال مطمئن توضیحات به یادداشت قبل از اعمال در پایگاه داده.
 * ۴. شناسایی و ادغام (Merge) کلیه واژگان تکراری در کتابخانه (از جمله تشخیص هوشمند حروف تعریف El/La بدون حذف آن‌ها از واژه) با حفظ فرم دارای حرف تعریف و بالاترین مرحله جعبه لایتنر کاربر.
 * ۵. استخراج پرانتزها، نقش‌های گرامری و توضیحات چسبیده به متن واژه و انتقال منظم آن‌ها به بخش یادداشت‌ها.
 * ۶. اصلاح نشانه‌گذاری‌های آسیب‌دیده اسپانیایی (مانند ¿...;¿).
 */
class VocabularyAuditService(
    private val database: YadinDatabase,
    private val conceptDao: ConceptDao,
    private val learningDao: LearningDao,
    private val reviewSessionDao: ReviewSessionDao
) {

    data class CleanedWordResult(
        val cleanSourceText: String,
        val extractedTranslations: List<String>,
        val extractedNotes: List<String>,
        val wasModified: Boolean,
        val modifications: List<String>
    )

    fun cleanAndExtractWord(originalSource: String): CleanedWordResult =
        Companion.cleanAndExtractWord(originalSource)

    private suspend fun loadDismissedSignatures(): Set<String> {
        return try {
            val raw = database.settingsDao().getSetting(AUDIT_DISMISSED_SETTING_KEY)
            if (raw.isNullOrBlank()) emptySet()
            else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    private suspend fun saveDismissedSignatures(signatures: Set<String>) {
        try {
            database.settingsDao().setSetting(
                SettingEntity(
                    key = AUDIT_DISMISSED_SETTING_KEY,
                    value = signatures.joinToString(",")
                )
            )
        } catch (_: Exception) {
        }
    }

    private fun conceptSignature(conceptId: String, rawSourceText: String): String {
        return "$conceptId:${TextUtilities.cleanText(rawSourceText).hashCode()}"
    }

    private fun isConceptDismissed(conceptId: String, rawSourceText: String, dismissedSet: Set<String>): Boolean {
        return conceptId in dismissedSet || conceptSignature(conceptId, rawSourceText) in dismissedSet
    }

    companion object {
        const val AUDIT_DISMISSED_SETTING_KEY = "audit_dismissed_concept_ids"
        val REGEX_PAREN_BRACKET = Regex("""^(.*?)\s*[\(\[（]([^\)\]）]+)[\)\]）]\s*$""")
        val REGEX_NUM_PREFIX = Regex("""^\s*[0-9۰-۹٠-٩]+\s*(?:[.)-]|:|[-—])\s*""")
        val REGEX_BULLET_PREFIX = Regex("""^[-—*•#»«➜→\s]+""")

        /**
         * پالایش واژه و جداسازی کاراکترهای نامناسب، پرانتزها، جداکننده‌ها و علائم زائد
         */
        private val isEn: Boolean
            get() = java.util.Locale.getDefault().language.startsWith("en", ignoreCase = true)

        private fun tr(fa: String, en: String): String = if (isEn) en else fa

        fun cleanAndExtractWord(originalSource: String): CleanedWordResult {
            var text = originalSource.trim()
            val modifications = mutableListOf<String>()
            val extractedTranslations = mutableListOf<String>()
            val extractedNotes = mutableListOf<String>()

            // ۰. بررسی و تفکیک متون چندخطی چسبیده
            if (text.contains("\n") || text.contains("\r")) {
                val lines = text.replace("\r\n", "\n").replace("\r", "\n").split("\n").map { it.trim() }.filter { it.isNotBlank() }
                if (lines.size >= 2) {
                    text = lines[0]
                    for (i in 1 until lines.size) {
                        val line = lines[i]
                        if (VocabularyParser.hasPersian(line)) {
                            extractedTranslations.addAll(TextUtilities.splitTranslations(line))
                            modifications.add(tr("استخراج ترجمه فارسی از سطر بعدی ($line)", "Extracted Persian translation from next line ($line)"))
                        } else {
                            extractedNotes.add(tr("متن اضافی سطر بعدی: $line", "Extra text from next line: $line"))
                            modifications.add(tr("انتقال سطر اضافی به یادداشت ($line)", "Moved extra line to note ($line)"))
                        }
                    }
                }
            }

            // ۱. استانداردسازی فاصله و حذف نویسه‌های نامرئی
            val normalized = TextUtilities.cleanText(text)
            if (normalized != text) {
                text = normalized
                modifications.add(tr("حذف کاراکترهای نامرئی و استانداردسازی فاصله", "Removed invisible characters and normalized whitespace"))
            }

            // ۲. حذف شماره‌گذاری آغازین یا گلوله‌های نگارشی (مانند 1. یا - یا •)
            val strippedNumbering = stripNumberingAndBullets(text)
            if (strippedNumbering != text && strippedNumbering.isNotBlank()) {
                modifications.add(tr("حذف شماره‌گذاری یا علامت آغازین", "Removed leading numbering or bullet"))
                text = strippedNumbering
            }

            // ۳. حذف گیومه و نقل‌قول‌های احاطه‌کننده (مانند "word" یا «word»)
            val strippedQuotes = stripSurroundingQuotes(text)
            if (strippedQuotes != text && strippedQuotes.isNotBlank()) {
                modifications.add(tr("حذف گیومه یا نشانه‌های نقل‌قول", "Removed surrounding quotes"))
                text = strippedQuotes
            }

            // ۳.۵. اصلاح اولیه نشانه‌گذاری و علائم خراب انتهای متن (مانند ;¿ یا ;¡ قبل از بررسی جداکننده‌ها)
            val (prePunctCleaned, prePunctMods) = TextUtilities.normalizeSpanishPunctuation(text)
            if (prePunctCleaned != text) {
                text = prePunctCleaned
                modifications.addAll(prePunctMods)
            }

            // ۴. بررسی وجود جداکننده دوطرفه (مانند "casa : خانه" یا "run - دویدن")
            val pair = VocabularyParser.splitPair(text)
            if (pair != null) {
                val left = pair.first.trim()
                val right = pair.second.trim()
                if (left.isNotBlank() && right.isNotBlank()) {
                    text = left
                    if (VocabularyParser.hasPersian(right)) {
                        extractedTranslations.addAll(TextUtilities.splitTranslations(right))
                        modifications.add(tr("جداسازی ترجمه چسبیده به واژه ($right)", "Separated attached translation ($right)"))
                    } else {
                        extractedNotes.add(tr("توضیح جداشده: $right", "Separated note: $right"))
                        modifications.add(tr("انتقال متن دوم به یادداشت ($right)", "Moved secondary text to note ($right)"))
                    }
                }
            }

            // ۵. استخراج پرانتزها و کروشه‌ها (مانند "apple (n.)" یا "correr (دویدن)")
            // عبارات معتبر واژگانی مانند "El chancla (Las chanclas)" یا "Press de banca (Bench press)" حفظ می‌شوند.
            var keepExtracting = true
            var loopCount = 0
            while (keepExtracting && loopCount < 5) {
                loopCount++
                val parenMatch = REGEX_PAREN_BRACKET.find(text)
                if (parenMatch != null) {
                    val main = parenMatch.groupValues[1].trim()
                    val inside = parenMatch.groupValues[2].trim()
                    if (main.isNotBlank() && inside.isNotBlank()) {
                        if (VocabularyParser.hasPersian(inside)) {
                            text = main
                            extractedTranslations.addAll(TextUtilities.splitTranslations(inside))
                            modifications.add(tr("استخراج ترجمه فارسی داخل پرانتز ($inside)", "Extracted parenthetical Persian translation ($inside)"))
                        } else if (FieldValidationEngine.isStrictGrammarPosTag(inside)) {
                            text = main
                            extractedNotes.add(tr("نقش/توضیح: $inside", "POS/Note: $inside"))
                            modifications.add(tr("انتقال توضیحات داخل پرانتز به یادداشت ($inside)", "Moved parenthetical note ($inside)"))
                        } else {
                            keepExtracting = false
                        }
                    } else {
                        keepExtracting = false
                    }
                } else {
                    keepExtracting = false
                }
            }

            // ۶. تفکیک متون فارسی و اسپانیایی متداخل در فیلد واژه (Script Separation هوشمند)
            if (VocabularyParser.hasLatin(text) && VocabularyParser.hasPersian(text)) {
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
                    while (start > 0 && (text[start - 1] == ' ' || text[start - 1] == '-' || text[start - 1] == '(' || text[start - 1].isDigit())) {
                        start--
                    }
                    while (end < text.length && (text[end] == ' ' || text[end] == '-' || text[end] == ')' || text[end].isDigit() || text[end] == 'V' || text[end] == 'v')) {
                        end++
                    }
                    val persianPart = text.substring(start, end).trim()
                    val remainingText = (text.substring(0, start) + " " + text.substring(end)).trim().replace(Regex("""\s+"""), " ")
                    if (persianPart.isNotBlank() && remainingText.isNotBlank()) {
                        text = remainingText
                        extractedTranslations.addAll(TextUtilities.splitTranslations(persianPart))
                        modifications.add(tr("تفکیک متن فارسی متداخل ($persianPart)", "Separated mixed Persian text ($persianPart)"))
                    }
                }
            }

            // ۷. بررسی و اصلاح نشانه‌گذاری اسپانیایی و علائم آسیب‌دیده (مانند ¿...;¿ یا نشانه‌های زائد پایانی)
            val (punctCleaned, punctMods) = TextUtilities.normalizeSpanishPunctuation(text)
            if (punctCleaned != text) {
                text = punctCleaned
                modifications.addAll(punctMods)
            }

            text = TextUtilities.cleanText(text)
            val wasModified = text != originalSource.trim() || extractedTranslations.isNotEmpty() || extractedNotes.isNotEmpty()

            return CleanedWordResult(
                cleanSourceText = if (text.isNotBlank()) text else originalSource.trim(),
                extractedTranslations = extractedTranslations.distinct(),
                extractedNotes = extractedNotes.distinct(),
                wasModified = wasModified,
                modifications = modifications
            )
        }

        fun stripNumberingAndBullets(s: String): String {
            val withoutNum = REGEX_NUM_PREFIX.replaceFirst(s, "").trim()
            return REGEX_BULLET_PREFIX.replaceFirst(withoutNum, "").trim()
        }

        fun stripSurroundingQuotes(s: String): String {
            var res = s.trim()
            if ((res.startsWith("\"") && res.endsWith("\"")) ||
                (res.startsWith("'") && res.endsWith("'")) ||
                (res.startsWith("«") && res.endsWith("»")) ||
                (res.startsWith("“") && res.endsWith("”"))
            ) {
                if (res.length >= 2) {
                    res = res.substring(1, res.length - 1).trim()
                }
            }
            return res
        }

        internal fun stageRank(stage: Stage?): Int = when (stage) {
            Stage.LEARNED -> 4
            Stage.MONTHLY -> 3
            Stage.WEEKLY -> 2
            Stage.DAILY -> 1
            null -> 0
        }

        internal fun isPluralInGroup(candidate: ConceptWorkingData, group: List<ConceptWorkingData>): Boolean {
            return group.any { other ->
                other.concept.id != candidate.concept.id &&
                    TextUtilities.isSpanishPhrasePluralOf(
                        pluralAnalysis = candidate.spanishAnalysis,
                        singularAnalysis = other.spanishAnalysis,
                        pluralPersianTranslations = candidate.allPersianTranslations()
                    )
            }
        }

        internal fun selectPrimaryConcept(items: List<ConceptWorkingData>): ConceptWorkingData {
            return items.maxWithOrNull(
                compareBy<ConceptWorkingData> { if (!isPluralInGroup(it, items)) 1 else 0 }
                    .thenBy { stageRank(it.learningState?.stage) }
                    .thenBy { it.learningState?.sm2Repetitions ?: 0 }
                    .thenBy { if (it.spanishAnalysis.articlePrefix != null && it.spanishAnalysis.isDefiniteArticle) 1 else 0 }
                    .thenByDescending { it.concept.createdAt }
                    .thenBy { it.learningState?.updatedAt ?: it.concept.updatedAt }
            ) ?: items.first()
        }

        internal fun hasGroupTranslationOverlap(items: List<ConceptWorkingData>): Boolean {
            if (items.size <= 1) return false
            for (i in items.indices) {
                for (j in i + 1 until items.size) {
                    if (TextUtilities.hasPersianTranslationOverlap(
                            items[i].allPersianTranslations(),
                            items[j].allPersianTranslations()
                        )
                    ) {
                        return true
                    }
                }
            }
            return false
        }

        private val SUBJECT_PRONOUN_PREFIXES = setOf(
            "yo", "tu", "el", "ella", "usted",
            "nosotros", "nosotras", "vosotros", "vosotras",
            "ellos", "ellas", "ustedes"
        )

        private val SPANISH_DIACRITICAL_TILDE_KEYS = setOf(
            "que", "como", "cuando", "donde", "adonde",
            "cual", "cuales", "quien", "quienes",
            "cuanto", "cuanta", "cuantos", "cuantas",
            "si", "mas", "aun", "de", "se", "tu", "mi", "el", "te", "solo"
        )

        private fun isSubjectPronounVerbDrill(item: ConceptWorkingData): Boolean {
            val words = item.spanishAnalysis.accentInsensitiveKey.split(Regex("\\s+")).filter { it.isNotBlank() }
            return words.size >= 2 && words.first() in SUBJECT_PRONOUN_PREFIXES
        }

        internal fun hasToneConflict(items: List<ConceptWorkingData>): Boolean {
            val hasEllipsisItem = items.any { it.spanishAnalysis.hasEllipsis }
            val hasExclItem = items.any { it.spanishAnalysis.hasExclamation }
            val hasNonExclItem = items.any { !it.spanishAnalysis.hasExclamation }
            val hasQuestItem = items.any { it.spanishAnalysis.hasQuestion }
            val hasNonQuestItem = items.any { !it.spanishAnalysis.hasQuestion }
            if (hasExclItem && hasNonExclItem) return true
            if (hasQuestItem && hasNonQuestItem) return true
            if (hasEllipsisItem && (hasExclItem || hasQuestItem)) return true
            val hasNonEllipsisItem = items.any { !it.spanishAnalysis.hasEllipsis }
            if (hasEllipsisItem && hasNonEllipsisItem && !hasGroupTranslationOverlap(items)) return true
            return false
        }

        /**
         * تفکیک هوشمند واژگان به سه گروه:
         * ۱. تکراری‌های قطعی قابل ادغام خودکار (مثلاً El trampolín و Trampolín، فرم‌های اسلش‌دار هم‌معنی مثل Claro / clara و claro، یا تفاوت نقطه پایانی)
         * ۲. تکراری‌های مشکوک نیازمند بررسی دستی (مثلاً تضاد جنسیت El در برابر La با هم‌پوشانی معنایی، یا ترجمه فارسی کاملاً یکسان با املای نزدیک اسپانیایی)
         * ۳. واژگان غیرتکراری
         */
        internal fun partitionDuplicateGroups(workingList: List<ConceptWorkingData>): DuplicatePartitionResult {
            val definiteMergeGroups = mutableListOf<List<ConceptWorkingData>>()
            val suspiciousGroups = mutableListOf<List<ConceptWorkingData>>()
            val mergedConceptIds = mutableSetOf<String>()
            val suspiciousConceptIds = mutableSetOf<String>()

            // ۱. گروه‌بندی بر اساس کلید اسپانیایی بدون حرف تعریف (با حفظ حساسیت به آکسان)
            val byDedupeKey = workingList
                .filter { it.spanishAnalysis.dedupeKey.isNotBlank() }
                .groupBy { it.spanishAnalysis.dedupeKey }

            // ۲. بررسی گروه‌هایی که فقط در آکسان/تیلده تفاوت دارند (accentInsensitiveKey یکسان ولی dedupeKey متفاوت)
            val byAccentInsensitive = byDedupeKey.entries
                .filter { it.key.length >= 3 }
                .groupBy { (_, list) -> list.first().spanishAnalysis.accentInsensitiveKey }

            val processedDedupeKeys = mutableSetOf<String>()

            for ((accentKey, bucketEntries) in byAccentInsensitive) {
                if (accentKey.isBlank() || bucketEntries.size <= 1 || accentKey in SPANISH_DIACRITICAL_TILDE_KEYS) continue
                val combinedItems = bucketEntries.flatMap { it.value }
                val hasMasc = combinedItems.any { it.spanishAnalysis.gender == TextUtilities.SpanishArticleGender.MASCULINE }
                val hasFem = combinedItems.any { it.spanishAnalysis.gender == TextUtilities.SpanishArticleGender.FEMININE }
                val hasGenderConflict = hasMasc && hasFem && combinedItems.none { it.spanishAnalysis.gender == TextUtilities.SpanishArticleGender.BOTH }
                val toneConflict = hasToneConflict(combinedItems)
                val hasOverlap = hasGroupTranslationOverlap(combinedItems)

                val isMultiWordPhrase = accentKey.split(Regex("\\s+")).filter { it.isNotBlank() }.size >= 3
                if (!hasGenderConflict && !toneConflict && (hasOverlap || isMultiWordPhrase)) {
                    definiteMergeGroups.add(combinedItems)
                    combinedItems.forEach { mergedConceptIds.add(it.concept.id) }
                    bucketEntries.forEach { processedDedupeKeys.add(it.key) }
                } else if (!toneConflict && hasOverlap) {
                    suspiciousGroups.add(combinedItems)
                    combinedItems.forEach { suspiciousConceptIds.add(it.concept.id) }
                }
            }

            // ۳. بررسی گروه‌های هم‌کلید (dedupeKey یکسان، مانند El trampolín و Trampolín یا Vale. و Vale...)
            for ((dedupeKey, items) in byDedupeKey) {
                if (dedupeKey in processedDedupeKeys || items.size <= 1) continue

                val hasMasc = items.any { it.spanishAnalysis.gender == TextUtilities.SpanishArticleGender.MASCULINE }
                val hasFem = items.any { it.spanishAnalysis.gender == TextUtilities.SpanishArticleGender.FEMININE }
                val hasBothTag = items.any { it.spanishAnalysis.gender == TextUtilities.SpanishArticleGender.BOTH }
                val hasGenderConflict = hasMasc && hasFem && !hasBothTag
                val toneConflict = hasToneConflict(items)
                val hasOverlap = hasGroupTranslationOverlap(items)

                val hasOnlyIndefiniteVsBare = items.none { it.spanishAnalysis.isDefiniteArticle } &&
                    items.any { it.spanishAnalysis.articlePrefix != null && !it.spanishAnalysis.isDefiniteArticle } &&
                    items.any { it.spanishAnalysis.articlePrefix == null }

                val isDefiniteDuplicate = when {
                    toneConflict -> false
                    hasGenderConflict -> hasOverlap
                    hasOnlyIndefiniteVsBare -> hasOverlap
                    else -> true
                }

                if (isDefiniteDuplicate) {
                    definiteMergeGroups.add(items)
                    items.forEach { mergedConceptIds.add(it.concept.id) }
                } else if (!toneConflict && hasOverlap) {
                    suspiciousGroups.add(items)
                    items.forEach { suspiciousConceptIds.add(it.concept.id) }
                }
            }

            // ۴. بررسی فرم‌های اسلش‌دار (مانند Claro / clara یا el sobrino / la sobrina) در برابر فرم‌های تکی هم‌ریشه
            val slashItems = workingList.filter {
                it.spanishAnalysis.isSlashDualForm &&
                    it.spanishAnalysis.slashVariantKeys.size >= 2 &&
                    it.concept.id !in mergedConceptIds
            }
            for (slashItem in slashItems) {
                if (slashItem.concept.id in mergedConceptIds) continue
                val vKeys = slashItem.spanishAnalysis.slashVariantKeys
                val isProtectedHomographSlash = vKeys.size == 2 && TextUtilities.isFalseSpanishGenderPair(vKeys[0], vKeys[1])
                if (isProtectedHomographSlash) continue

                val matchedVariants = vKeys
                    .flatMap { variantKey -> byDedupeKey[variantKey].orEmpty() }
                    .filter { candidate ->
                        candidate.concept.id != slashItem.concept.id &&
                            candidate.concept.id !in mergedConceptIds &&
                            !hasToneConflict(listOf(slashItem, candidate))
                    }
                    .distinctBy { it.concept.id }

                if (matchedVariants.isNotEmpty()) {
                    val combined = listOf(slashItem) + matchedVariants
                    if (!hasToneConflict(combined) && hasGroupTranslationOverlap(combined)) {
                        definiteMergeGroups.add(combined)
                        combined.forEach { mergedConceptIds.add(it.concept.id) }
                    }
                }
            }

            // ۴.۵. ادغام خودکار واژگان و عبارات مفرد و جمع اسپانیایی (مانند Testigo و Testigos یا La luz de emergencia و Luces de emergencia)
            val byAccentKeyAll = mutableMapOf<String, MutableList<ConceptWorkingData>>()
            for (wItem in workingList) {
                val mainKey = wItem.spanishAnalysis.accentInsensitiveKey
                if (mainKey.isNotBlank()) {
                    byAccentKeyAll.getOrPut(mainKey) { mutableListOf() }.add(wItem)
                }
                for (sv in wItem.spanishAnalysis.slashVariantKeys) {
                    val svAccentKey = TextUtilities.analyzeSpanishHeadword(sv).accentInsensitiveKey
                    if (svAccentKey.isNotBlank()) {
                        val bucket = byAccentKeyAll.getOrPut(svAccentKey) { mutableListOf() }
                        if (bucket.none { it.concept.id == wItem.concept.id }) {
                            bucket.add(wItem)
                        }
                    }
                }
            }

            val singularIdToPlurals = mutableMapOf<String, MutableList<ConceptWorkingData>>()
            val candidatePluralItems = workingList.filter { it.concept.id !in mergedConceptIds }
            for (pluralItem in candidatePluralItems) {
                if (pluralItem.concept.id in mergedConceptIds) continue
                val candidateSingKeys = TextUtilities.spanishSingularCandidateKeys(pluralItem.spanishAnalysis)
                if (candidateSingKeys.isEmpty()) continue

                val matchedSingulars = candidateSingKeys
                    .flatMap { singKey -> byAccentKeyAll[singKey].orEmpty() }
                    .filter { singItem ->
                        singItem.concept.id != pluralItem.concept.id &&
                            TextUtilities.isSpanishPhrasePluralOf(
                                pluralAnalysis = pluralItem.spanishAnalysis,
                                singularAnalysis = singItem.spanishAnalysis,
                                pluralPersianTranslations = pluralItem.allPersianTranslations()
                            ) &&
                            !hasToneConflict(listOf(singItem, pluralItem))
                    }
                    .distinctBy { it.concept.id }

                val bestSingular = matchedSingulars.firstOrNull()
                if (bestSingular != null) {
                    val existingGroupIdx = definiteMergeGroups.indexOfFirst { grp ->
                        grp.any { it.concept.id == bestSingular.concept.id }
                    }
                    if (existingGroupIdx >= 0) {
                        val existingGroup = definiteMergeGroups[existingGroupIdx]
                        if (existingGroup.none { it.concept.id == pluralItem.concept.id }) {
                            definiteMergeGroups[existingGroupIdx] = existingGroup + pluralItem
                            mergedConceptIds.add(pluralItem.concept.id)
                        }
                    } else if (bestSingular.concept.id !in mergedConceptIds) {
                        singularIdToPlurals.getOrPut(bestSingular.concept.id) { mutableListOf() }.add(pluralItem)
                    }
                }
            }

            val workingByIdMap = workingList.associateBy { it.concept.id }
            for ((singId, plurals) in singularIdToPlurals) {
                val singItem = workingByIdMap[singId] ?: continue
                if (singItem.concept.id in mergedConceptIds) continue
                val validPlurals = plurals.filter { it.concept.id !in mergedConceptIds }.distinctBy { it.concept.id }
                if (validPlurals.isNotEmpty()) {
                    val combined = listOf(singItem) + validPlurals
                    definiteMergeGroups.add(combined)
                    combined.forEach { mergedConceptIds.add(it.concept.id) }
                }
            }

            // ۴.۶. ادغام خودکار جفت‌واژگان مذکر و مؤنث اسپانیایی (مانند Rojo و Roja یا El director و la directora) با فرمت اسلش (/)
            val candidateGenderItems = workingList.filter { it.concept.id !in mergedConceptIds }
            for (item in candidateGenderItems) {
                if (item.concept.id in mergedConceptIds) continue
                val partnerKeys = TextUtilities.spanishGenderPartnerCandidateKeys(item.spanishAnalysis)
                if (partnerKeys.isEmpty()) continue

                val matchedPartners = partnerKeys
                    .flatMap { pk -> byAccentKeyAll[pk].orEmpty() }
                    .filter { partner ->
                        partner.concept.id != item.concept.id &&
                            partner.concept.id !in mergedConceptIds &&
                            TextUtilities.isSpanishMasculineFemininePair(
                                firstAnalysis = item.spanishAnalysis,
                                secondAnalysis = partner.spanishAnalysis,
                                firstTranslations = item.allPersianTranslations(),
                                secondTranslations = partner.allPersianTranslations()
                            ) &&
                            !hasToneConflict(listOf(item, partner))
                    }
                    .distinctBy { it.concept.id }

                if (matchedPartners.isNotEmpty()) {
                    val combined = (listOf(item) + matchedPartners).distinctBy { it.concept.id }
                    definiteMergeGroups.add(combined)
                    combined.forEach { mergedConceptIds.add(it.concept.id) }
                }
            }

            // ۵. یافتن کارت‌های مشکوک با ترجمه فارسی کاملاً یکسان و املای نزدیک اسپانیایی (مانند claro/claros یا tranquilo/tranquila)
            val unmergedItems = workingList.filter { it.concept.id !in mergedConceptIds }
            val byPersianKey = mutableMapOf<String, MutableList<ConceptWorkingData>>()
            for (item in unmergedItems) {
                if (isSubjectPronounVerbDrill(item) || item.spanishAnalysis.isSlashDualForm) continue
                val faKeys = item.allPersianTranslations()
                    .map { TextUtilities.persianDedupeKey(it) }
                    .filter { it.length >= 2 }
                    .distinct()
                for (fk in faKeys) {
                    byPersianKey.getOrPut(fk) { mutableListOf() }.add(item)
                }
            }

            val pairedNearKeys = mutableSetOf<String>()
            for ((_, bucket) in byPersianKey) {
                if (bucket.size !in 2..10) continue
                for (i in bucket.indices) {
                    for (j in i + 1 until bucket.size) {
                        val a = bucket[i]
                        val b = bucket[j]
                        if (a.concept.id == b.concept.id) continue
                        val pairId = if (a.concept.id < b.concept.id) "${a.concept.id}_${b.concept.id}" else "${b.concept.id}_${a.concept.id}"
                        if (pairId in pairedNearKeys) continue
                        val keyA = a.spanishAnalysis.accentInsensitiveKey
                        val keyB = b.spanishAnalysis.accentInsensitiveKey
                        if (hasToneConflict(listOf(a, b)) || TextUtilities.isFalseSpanishGenderPair(keyA, keyB)) continue
                        val aIsBareInfinitive = a.spanishAnalysis.articlePrefix == null && (keyA.endsWith("ar") || keyA.endsWith("er") || keyA.endsWith("ir"))
                        val bIsBareInfinitive = b.spanishAnalysis.articlePrefix == null && (keyB.endsWith("ar") || keyB.endsWith("er") || keyB.endsWith("ir"))
                        if ((aIsBareInfinitive && b.spanishAnalysis.articlePrefix != null) ||
                            (bIsBareInfinitive && a.spanishAnalysis.articlePrefix != null)
                        ) {
                            continue
                        }
                        if (TextUtilities.areSpanishNearSpellingVariants(keyA, keyB)) {
                            pairedNearKeys.add(pairId)
                            if (a.concept.id !in suspiciousConceptIds || b.concept.id !in suspiciousConceptIds) {
                                suspiciousGroups.add(listOf(a, b))
                                suspiciousConceptIds.add(a.concept.id)
                                suspiciousConceptIds.add(b.concept.id)
                            }
                        }
                    }
                }
            }

            val singleItems = workingList.filter { it.concept.id !in mergedConceptIds }
            return DuplicatePartitionResult(
                definiteMergeGroups = definiteMergeGroups,
                suspiciousGroups = suspiciousGroups,
                singleItems = singleItems
            )
        }
    }

    internal data class ConceptWorkingData(
        val concept: ConceptEntity,
        val sourceContent: ContentEntity?,
        val targetContents: List<ContentEntity>,
        val learningState: LearningStateEntity?,
        val allLearningStates: List<LearningStateEntity>,
        val allDifficultyStates: List<DifficultyStateEntity>,
        val categoryIds: List<String>,
        val cleanedResult: CleanedWordResult,
        val spanishAnalysis: TextUtilities.SpanishHeadwordAnalysis
    ) {
        fun allPersianTranslations(): List<String> =
            (targetContents.map { it.text } + cleanedResult.extractedTranslations).filter { it.isNotBlank() }
    }

    internal data class DuplicatePartitionResult(
        val definiteMergeGroups: List<List<ConceptWorkingData>>,
        val suspiciousGroups: List<List<ConceptWorkingData>>,
        val singleItems: List<ConceptWorkingData>
    )

    /**
     * گام ۱: اسکن، تحلیل و گزارش‌گیری امن از پایگاه‌داده کتابخانه بدون اعمال هرگونه تغییر
     * در این مرحله، داده‌های مشکوک یا نیازمند بازبینی دستی شناسایی شده و در صف بازبینی قرار می‌گیرند.
     */
    suspend fun scanLibraryForAudit(
        direction: CardDirection = CardDirection.NORMAL,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): LibraryAuditReport = withContext(Dispatchers.Default) {
        val (sourceLang, _) = direction.resolveSourceAndTarget()
        val allConcepts = withContext(Dispatchers.IO) { conceptDao.getAllConcepts().filter { it.active } }
        val totalScanned = allConcepts.size

        if (totalScanned == 0) {
            return@withContext LibraryAuditReport(
                totalScanned = 0,
                mergedCount = 0,
                cleanedCount = 0,
                totalRemaining = 0,
                actions = emptyList(),
                needsReviewCount = 0,
                reviewItems = emptyList()
            )
        }

        // ۱. پیش‌فراخوانی دسته‌جمعی داده‌ها خارج از ترنزاکشن
        val contentsByConcept = withContext(Dispatchers.IO) { conceptDao.getAllContents().groupBy { it.conceptId } }
        val allLearningByConcept = withContext(Dispatchers.IO) { learningDao.getAllLearningStates().groupBy { it.conceptId } }
        val allDifficultyByConcept = withContext(Dispatchers.IO) { learningDao.getAllDifficultyStates().groupBy { it.conceptId } }
        val conceptCategoriesByConcept = withContext(Dispatchers.IO) { conceptDao.getAllConceptCategories().groupBy { it.conceptId } }
        val dismissedSet = withContext(Dispatchers.IO) { loadDismissedSignatures() }

        val reviewItems = mutableListOf<AuditReviewItem>()
        val actions = mutableListOf<AuditActionDetail>()
        var duplicateCount = 0
        var autoCleanCount = 0

        val workingList = ArrayList<ConceptWorkingData>(totalScanned)

        // ۲. پردازش بر روی Dispatchers.Default با کنترل نرخ انتشار گزارش پیشرفت (حد اکثر ۱۰ بار در ثانیه)
        var lastReportTime = 0L
        allConcepts.forEachIndexed { idx, concept ->
            val now = System.currentTimeMillis()
            if (now - lastReportTime >= 100 || idx == totalScanned - 1) {
                lastReportTime = now
                onProgress(idx + 1, totalScanned)
            }
            val contents = contentsByConcept[concept.id] ?: emptyList()
            val source = contents.firstOrNull { it.languageCode == sourceLang }
                ?: contents.firstOrNull { it.languageCode != "fa" }
                ?: contents.firstOrNull()
            val targets = contents.filter { it.id != source?.id }
            val conceptLearningStates = allLearningByConcept[concept.id] ?: emptyList()
            val learning = conceptLearningStates.firstOrNull { it.direction == direction }
            val conceptDifficultyStates = allDifficultyByConcept[concept.id] ?: emptyList()
            val catIdsFromJoin = conceptCategoriesByConcept[concept.id]?.map { it.categoryId } ?: emptyList()
            val cats = (catIdsFromJoin + listOfNotNull(concept.categoryId)).distinct()

            val rawSourceText = source?.text ?: ""
            val rawTransTexts = targets.map { it.text }
            val existingNote = source?.note

            // ارزیابی با موتور اعتبارسنجی فیلدها
            val validation = FieldValidationEngine.validateRecord(rawSourceText, rawTransTexts, existingNote)

            val cleaned = cleanAndExtractWord(rawSourceText)
            val normalizedTargetsPreview = rawTransTexts.map { tr ->
                TextUtilities.normalizePersianTranslation(tr, cleaned.cleanSourceText).first
            }
            val persianOrthographyChanged = normalizedTargetsPreview != rawTransTexts
            if (validation.action == ReviewAction.MANUAL_REVIEW || validation.confidence < 0.98) {
                if (!isConceptDismissed(concept.id, rawSourceText, dismissedSet)) {
                    reviewItems.add(
                        AuditReviewItem(
                            conceptId = concept.id,
                            originalWord = rawSourceText,
                            originalTranslation = rawTransTexts.joinToString("، "),
                            suggestedWord = validation.suggestedWord ?: rawSourceText,
                            suggestedTranslation = validation.suggestedTranslation ?: normalizedTargetsPreview.joinToString("، "),
                            suggestedNote = validation.suggestedNote,
                            reason = validation.reason,
                            confidence = validation.confidence
                        )
                    )
                }
            } else if (validation.action == ReviewAction.MOVE_TO_NOTE || validation.action == ReviewAction.SUGGEST_CORRECTION || persianOrthographyChanged) {
                autoCleanCount++
            }

            val spanishAnalysis = TextUtilities.analyzeSpanishHeadword(cleaned.cleanSourceText)
            workingList.add(
                ConceptWorkingData(
                    concept = concept,
                    sourceContent = source,
                    targetContents = targets,
                    learningState = learning,
                    allLearningStates = conceptLearningStates,
                    allDifficultyStates = conceptDifficultyStates,
                    categoryIds = cats,
                    cleanedResult = cleaned,
                    spanishAnalysis = spanishAnalysis
                )
            )
        }

        // ۳. بررسی موارد تکراری قطعی (ادغام خودکار) و موارد مشکوک (ارسال به لیست بازبینی)
        val partition = partitionDuplicateGroups(workingList)

        partition.definiteMergeGroups.forEach { items ->
            duplicateCount += (items.size - 1)
            val preferredWord = TextUtilities.selectPreferredSpanishHeadword(items.map { it.cleanedResult.cleanSourceText })
            val originalForms = items.map { it.sourceContent?.text ?: it.cleanedResult.cleanSourceText }.distinct().joinToString(" + ")
            val hasPluralItem = items.any { isPluralInGroup(it, items) }
            actions.add(
                AuditActionDetail(
                    originalText = originalForms,
                    cleanedText = preferredWord,
                    actionType = AuditActionType.MERGED,
                    description = if (hasPluralItem) {
                        tr(
                            "ادغام فرم مفرد و جمع به «$preferredWord» و انتقال فرم جمع به یادداشت",
                            "Merge singular and plural into «$preferredWord» and move plural form to note"
                        )
                    } else {
                        tr(
                            "امکان ادغام ${items.size} واژه هم‌ریشه با حفظ فرم «$preferredWord» و بالاترین مرحله مرور",
                            "Can merge ${items.size} duplicate words keeping «$preferredWord» and highest review stage"
                        )
                    }
                )
            )
        }

        val existingReviewIds = reviewItems.map { it.conceptId }.toMutableSet()
        partition.suspiciousGroups.forEach { items ->
            val primary = selectPrimaryConcept(items)
            val primaryWord = primary.cleanedResult.cleanSourceText
            val primaryTransPreview = primary.allPersianTranslations().take(2).joinToString("، ")
            items.filter { it.concept.id != primary.concept.id }.forEach { secondary ->
                val secWord = secondary.sourceContent?.text ?: secondary.cleanedResult.cleanSourceText
                if (secondary.concept.id !in existingReviewIds && !isConceptDismissed(secondary.concept.id, secWord, dismissedSet)) {
                    existingReviewIds.add(secondary.concept.id)
                    val secTrans = secondary.targetContents.joinToString("، ") { it.text }
                    reviewItems.add(
                        AuditReviewItem(
                            conceptId = secondary.concept.id,
                            originalWord = secWord,
                            originalTranslation = secTrans,
                            suggestedWord = secondary.cleanedResult.cleanSourceText,
                            suggestedTranslation = secTrans,
                            suggestedNote = secondary.sourceContent?.note,
                            reason = tr(
                                "تشابه مشکوک با «$primaryWord» ($primaryTransPreview) — می‌توانید با آن ادغام کنید، متن را به یادداشت بفرستید یا بدون تغییر رد کنید",
                                "Suspicious similarity with «$primaryWord» ($primaryTransPreview) — you can merge, move to note, or keep original"
                            ),
                            confidence = 0.65,
                            mergeTargetConceptId = primary.concept.id,
                            mergeTargetWord = primaryWord,
                            mergeTargetTranslation = primaryTransPreview
                        )
                    )
                }
            }
        }

        val totalRemaining = totalScanned - duplicateCount

        LibraryAuditReport(
            totalScanned = totalScanned,
            mergedCount = duplicateCount,
            cleanedCount = autoCleanCount,
            totalRemaining = totalRemaining,
            actions = actions,
            needsReviewCount = reviewItems.size,
            reviewItems = reviewItems
        )
    }

    /**
     * گام ۲: اعمال اصلاحات، ادغام تکراری‌ها و ثبت رکوردهای بازبینی‌شده در پایگاه‌داده
     */
    suspend fun applyAuditFixes(
        direction: CardDirection = CardDirection.NORMAL,
        approvedReviewItems: List<AuditReviewItem> = emptyList(),
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): LibraryAuditReport = withContext(Dispatchers.Default) {
        val actions = mutableListOf<AuditActionDetail>()
        var mergedCount = 0
        var cleanedCount = 0
        val (sourceLang, targetLang) = direction.resolveSourceAndTarget()

        val dismissedSet = withContext(Dispatchers.IO) { loadDismissedSignatures().toMutableSet() }
        val explicitMergeTargets = mutableMapOf<String, String>() // secondaryConceptId -> primaryConceptId

        for (item in approvedReviewItems) {
            val isExplicitMerge = item.userDecision == AuditUserDecision.MERGE_WITH_TARGET ||
                (!item.mergeTargetConceptId.isNullOrBlank() &&
                    !item.mergeTargetWord.isNullOrBlank() &&
                    TextUtilities.cleanText(item.suggestedWord).equals(TextUtilities.cleanText(item.mergeTargetWord), ignoreCase = true))

            if (isExplicitMerge && !item.mergeTargetConceptId.isNullOrBlank()) {
                explicitMergeTargets[item.conceptId] = item.mergeTargetConceptId
            }

            // ثبت در لیست بررسی‌شده‌ها تا در اسکن‌های بعدی مجدداً به عنوان مشکوک نمایش داده نشود
            dismissedSet.add(item.conceptId)
            dismissedSet.add(conceptSignature(item.conceptId, item.originalWord))
            dismissedSet.add(conceptSignature(item.conceptId, item.suggestedWord))
        }

        if (approvedReviewItems.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                saveDismissedSignatures(dismissedSet)
            }
        }

        // ۱. اعمال ویرایش‌های تأییدشده در صف بازبینی دستی توسط کاربر
        val modifiedReviewItems = approvedReviewItems.filter { item ->
            item.userDecision != AuditUserDecision.KEEP_ORIGINAL_DISMISS && (
                item.userDecision == AuditUserDecision.EDITED ||
                    item.userDecision == AuditUserDecision.MOVE_TO_NOTE ||
                    item.userDecision == AuditUserDecision.MERGE_WITH_TARGET ||
                    TextUtilities.cleanText(item.suggestedWord) != TextUtilities.cleanText(item.originalWord) ||
                    TextUtilities.cleanText(item.suggestedTranslation) != TextUtilities.cleanText(item.originalTranslation) ||
                    !item.suggestedNote.isNullOrBlank()
                )
        }
        val reviewMap = modifiedReviewItems.associateBy { it.conceptId }
        if (modifiedReviewItems.isNotEmpty()) {
            val approvedConceptIds = modifiedReviewItems.map { it.conceptId }
            val approvedContentsToInsert = mutableListOf<ContentEntity>()
            for (item in modifiedReviewItems) {
                val targetList = TextUtilities.splitTranslations(item.suggestedTranslation)
                    .ifEmpty { listOf(item.suggestedTranslation).filter { it.isNotBlank() } }
                val newSourceContent = ContentEntity(
                    id = UUID.randomUUID().toString(),
                    conceptId = item.conceptId,
                    languageCode = sourceLang,
                    text = item.suggestedWord,
                    canonicalKey = TextUtilities.toCanonicalKey(item.suggestedWord),
                    note = item.suggestedNote?.ifBlank { null },
                    translationIndex = 0
                )
                val newTargets = targetList.mapIndexed { idx, tr ->
                    ContentEntity(
                        id = UUID.randomUUID().toString(),
                        conceptId = item.conceptId,
                        languageCode = targetLang,
                        text = tr,
                        canonicalKey = TextUtilities.toCanonicalKey(tr),
                        note = null,
                        translationIndex = idx
                    )
                }
                approvedContentsToInsert.add(newSourceContent)
                approvedContentsToInsert.addAll(newTargets)
                if (item.conceptId !in explicitMergeTargets) {
                    cleanedCount++
                    actions.add(
                        AuditActionDetail(
                            originalText = item.originalWord,
                            cleanedText = item.suggestedWord,
                            actionType = if (!item.suggestedNote.isNullOrBlank() || item.userDecision == AuditUserDecision.MOVE_TO_NOTE) {
                                AuditActionType.NOTE_EXTRACTED
                            } else {
                                AuditActionType.CLEANED
                            },
                            description = tr("اصلاح دستی کاربر: ${item.reason}", "Manual user edit: ${item.reason}")
                        )
                    )
                }
            }
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    conceptDao.deleteContentsForConcepts(approvedConceptIds)
                    if (approvedContentsToInsert.isNotEmpty()) {
                        approvedContentsToInsert.chunked(1000).forEach { conceptDao.insertContents(it) }
                    }
                }
            }
        }

        // ۲. پیش‌فراخوانی دسته‌جمعی داده‌ها خارج از ترنزاکشن
        val allConcepts = withContext(Dispatchers.IO) { conceptDao.getAllConcepts().filter { it.active } }
        val totalScanned = allConcepts.size

        if (totalScanned == 0) {
            return@withContext LibraryAuditReport(
                totalScanned = 0,
                mergedCount = 0,
                cleanedCount = 0,
                totalRemaining = 0,
                actions = emptyList()
            )
        }

        val contentsByConcept = withContext(Dispatchers.IO) { conceptDao.getAllContents().groupBy { it.conceptId } }
        val allLearningByConcept = withContext(Dispatchers.IO) { learningDao.getAllLearningStates().groupBy { it.conceptId } }
        val allDifficultyByConcept = withContext(Dispatchers.IO) { learningDao.getAllDifficultyStates().groupBy { it.conceptId } }
        val conceptCategoriesByConcept = withContext(Dispatchers.IO) { conceptDao.getAllConceptCategories().groupBy { it.conceptId } }

        val workingList = ArrayList<ConceptWorkingData>(totalScanned)
        var lastReportTime = 0L

        allConcepts.forEachIndexed { idx, concept ->
            val now = System.currentTimeMillis()
            if (now - lastReportTime >= 100 || idx == totalScanned - 1) {
                lastReportTime = now
                onProgress(idx + 1, totalScanned)
            }
            val contents = contentsByConcept[concept.id] ?: emptyList()
            val source = contents.firstOrNull { it.languageCode == sourceLang }
                ?: contents.firstOrNull { it.languageCode != "fa" }
                ?: contents.firstOrNull()
            val targets = contents.filter { it.id != source?.id }
            val conceptLearningStates = allLearningByConcept[concept.id] ?: emptyList()
            val learning = conceptLearningStates.firstOrNull { it.direction == direction }
            val conceptDifficultyStates = allDifficultyByConcept[concept.id] ?: emptyList()
            val catIdsFromJoin = conceptCategoriesByConcept[concept.id]?.map { it.categoryId } ?: emptyList()
            val cats = (catIdsFromJoin + listOfNotNull(concept.categoryId)).distinct()

            val rawSourceText = source?.text ?: ""
            val cleaned = if (reviewMap.containsKey(concept.id)) {
                CleanedWordResult(
                    cleanSourceText = rawSourceText,
                    extractedTranslations = emptyList(),
                    extractedNotes = emptyList(),
                    wasModified = false,
                    modifications = emptyList()
                )
            } else {
                cleanAndExtractWord(rawSourceText)
            }
            val spanishAnalysis = TextUtilities.analyzeSpanishHeadword(cleaned.cleanSourceText)

            workingList.add(
                ConceptWorkingData(
                    concept = concept,
                    sourceContent = source,
                    targetContents = targets,
                    learningState = learning,
                    allLearningStates = conceptLearningStates,
                    allDifficultyStates = conceptDifficultyStates,
                    categoryIds = cats,
                    cleanedResult = cleaned,
                    spanishAnalysis = spanishAnalysis
                )
            )
        }

        // ۳. گروه‌بندی و ادغام هوشمند واژگان تکراری در حافظه
        val partition = partitionDuplicateGroups(workingList)
        val workingById = workingList.associateBy { it.concept.id }
        val alreadyInDefinite = partition.definiteMergeGroups.flatten().map { it.concept.id }.toMutableSet()
        val extraMergeGroups = mutableListOf<List<ConceptWorkingData>>()

        for ((secId, primId) in explicitMergeTargets) {
            if (secId !in alreadyInDefinite && primId !in alreadyInDefinite) {
                val secData = workingById[secId]
                val primData = workingById[primId]
                if (secData != null && primData != null) {
                    extraMergeGroups.add(listOf(primData, secData))
                    alreadyInDefinite.add(secId)
                    alreadyInDefinite.add(primId)
                }
            }
        }

        val allMergeGroups = partition.definiteMergeGroups + extraMergeGroups

        val conceptsToUpdate = mutableListOf<ConceptEntity>()
        val conceptsToDeletePerm = mutableListOf<String>()
        val contentsToDeleteForConcepts = mutableListOf<String>()
        val contentsToInsertAll = mutableListOf<ContentEntity>()
        val conceptCategoriesToDeleteForConcepts = mutableListOf<String>()
        val conceptCategoriesToInsertAll = mutableListOf<ConceptCategoryEntity>()
        val learningStatesToUpdate = mutableListOf<LearningStateEntity>()
        val difficultyStatesToUpdate = mutableListOf<DifficultyStateEntity>()
        val duplicateIdsToRepoint = mutableListOf<Pair<List<String>, String>>()

        allMergeGroups.forEach { items ->
            if (items.size <= 1) return@forEach

            val primary = selectPrimaryConcept(items)
            val duplicates = items.filter { it.concept.id != primary.concept.id }
            val duplicateIds = duplicates.map { it.concept.id }

            val pluralItems = items.filter { isPluralInGroup(it, items) }
            val nonPluralItems = items.filterNot { isPluralInGroup(it, items) }.ifEmpty { items }

            // انتخاب عنوان اسپانیایی برتر (با اولویت فرم مفرد و حفظ حرف تعریف El/La برای یادگیری جنسیت اسم)
            val preferredSourceText = TextUtilities.selectPreferredSpanishHeadword(
                items.map { it.cleanedResult.cleanSourceText }
            ).ifBlank { primary.cleanedResult.cleanSourceText }
            val preferredCanonicalKey = TextUtilities.toCanonicalKey(preferredSourceText)

            // ترکیب ترجمه‌های فرم مفرد و اعمال نرمال‌سازی نگارش فارسی (ترجمه‌های فرم جمع به یادداشت منتقل می‌شوند)
            val existingTrans = mutableListOf<String>()
            nonPluralItems.forEach { item ->
                existingTrans.addAll(item.targetContents.map { it.text })
                existingTrans.addAll(item.cleanedResult.extractedTranslations)
            }
            if (existingTrans.isEmpty()) {
                pluralItems.forEach { dup ->
                    existingTrans.addAll(dup.targetContents.map { it.text })
                    existingTrans.addAll(dup.cleanedResult.extractedTranslations)
                }
            }
            val normalizedTrans = existingTrans
                .map { TextUtilities.normalizePersianTranslation(it, preferredSourceText).first }
                .filter { it.isNotBlank() }
            val mergedTranslations = TextUtilities.deduplicatePersianTranslations(normalizedTrans)
                .ifEmpty {
                    normalizedTrans.distinct()
                }

            // ترکیب یادداشت‌ها (به همراه انتقال فرم‌های جمع و توضیحات پرانتزی به یادداشت)
            val allNotes = mutableListOf<String>()
            primary.sourceContent?.note?.let { if (it.isNotBlank()) allNotes.add(it) }
            allNotes.addAll(primary.cleanedResult.extractedNotes)
            duplicates.forEach { dup ->
                dup.sourceContent?.note?.let { if (it.isNotBlank()) allNotes.add(it) }
                allNotes.addAll(dup.cleanedResult.extractedNotes)
            }
            pluralItems.forEach { pluralItem ->
                val pluralSource = pluralItem.cleanedResult.cleanSourceText
                    .ifBlank { pluralItem.sourceContent?.text.orEmpty() }
                if (pluralSource.isNotBlank()) {
                    val pluralTrans = pluralItem.allPersianTranslations()
                        .map { TextUtilities.normalizePersianTranslation(it, pluralSource).first }
                        .filter { it.isNotBlank() }
                    allNotes.add(TextUtilities.formatPluralFormNote(pluralSource, pluralTrans))
                }
            }
            items.filter { it !in pluralItems }.mapNotNull { it.spanishAnalysis.trailingParenNote }.forEach { parenNote ->
                if (parenNote.isNotBlank() && !preferredSourceText.contains(parenNote, ignoreCase = true)) {
                    allNotes.add(tr("توضیح واژه: $parenNote", "Word note: $parenNote"))
                }
            }
            var mergedNote: String? = null
            for (noteChunk in allNotes) {
                mergedNote = TextUtilities.mergeNotesWithPluralSupport(mergedNote, noteChunk)
            }

            val allCatIds = (primary.categoryIds + duplicates.flatMap { it.categoryIds }).distinct()

            val bestStage = items.mapNotNull { it.learningState?.stage }.maxByOrNull { stageRank(it) }
                ?: (primary.learningState?.stage ?: Stage.DAILY)

            val now = System.currentTimeMillis()

            conceptsToUpdate.add(
                primary.concept.copy(
                    categoryId = allCatIds.firstOrNull() ?: primary.concept.categoryId,
                    updatedAt = now
                )
            )

            contentsToDeleteForConcepts.add(primary.concept.id)
            val newSourceContent = ContentEntity(
                id = primary.sourceContent?.id ?: UUID.randomUUID().toString(),
                conceptId = primary.concept.id,
                languageCode = sourceLang,
                text = preferredSourceText,
                canonicalKey = preferredCanonicalKey,
                note = mergedNote,
                translationIndex = 0
            )
            val newTargetContents = mergedTranslations.mapIndexed { idx, transText ->
                ContentEntity(
                    id = UUID.randomUUID().toString(),
                    conceptId = primary.concept.id,
                    languageCode = targetLang,
                    text = transText,
                    canonicalKey = TextUtilities.toCanonicalKey(transText),
                    note = null,
                    translationIndex = idx
                )
            }
            contentsToInsertAll.add(newSourceContent)
            contentsToInsertAll.addAll(newTargetContents)

            conceptCategoriesToDeleteForConcepts.add(primary.concept.id)
            allCatIds.forEach { catId ->
                conceptCategoriesToInsertAll.add(ConceptCategoryEntity(primary.concept.id, catId))
            }

            // حفظ بالاترین وضعیت یادگیری و بالاترین سطح دشواری در هر دو جهت مرور (NORMAL و REVERSE)
            for (dir in CardDirection.entries) {
                val statesForDir = items.flatMap { it.allLearningStates }.filter { it.direction == dir }
                val bestLearningForDir = statesForDir.maxWithOrNull(
                    compareBy<LearningStateEntity> { stageRank(it.stage) }
                        .thenBy { it.sm2Repetitions }
                        .thenBy { it.fsrsReps }
                        .thenBy { it.updatedAt }
                )
                if (bestLearningForDir != null) {
                    val primaryExistingForDir = primary.allLearningStates.firstOrNull { it.direction == dir }
                    learningStatesToUpdate.add(
                        bestLearningForDir.copy(
                            id = primaryExistingForDir?.id ?: UUID.randomUUID().toString(),
                            conceptId = primary.concept.id,
                            updatedAt = now
                        )
                    )
                }

                val diffsForDir = items.flatMap { it.allDifficultyStates }.filter { it.direction == dir }
                val highestDiffForDir = diffsForDir.maxWithOrNull(
                    compareBy<DifficultyStateEntity> { it.current.ordinal }
                        .thenBy { if (it.conceptId == primary.concept.id) 1 else 0 }
                )
                if (highestDiffForDir != null) {
                    val primaryExistingDiffForDir = primary.allDifficultyStates.firstOrNull { it.direction == dir }
                    val anyReachedVeryHard = diffsForDir.any {
                        it.hasReachedVeryHard || it.current == com.manidigit.yadin.domain.model.VocabularyDifficulty.VERY_HARD
                    }
                    difficultyStatesToUpdate.add(
                        highestDiffForDir.copy(
                            id = primaryExistingDiffForDir?.id ?: UUID.randomUUID().toString(),
                            conceptId = primary.concept.id,
                            hasReachedVeryHard = anyReachedVeryHard
                        )
                    )
                }
            }

            duplicateIdsToRepoint.add(Pair(duplicateIds, primary.concept.id))

            contentsToDeleteForConcepts.addAll(duplicateIds)
            conceptCategoriesToDeleteForConcepts.addAll(duplicateIds)
            conceptsToDeletePerm.addAll(duplicateIds)

            mergedCount += duplicates.size
            val originalForms = items.map { it.sourceContent?.text ?: it.cleanedResult.cleanSourceText }.distinct().joinToString(" + ")
            actions.add(
                AuditActionDetail(
                    originalText = originalForms,
                    cleanedText = preferredSourceText,
                    actionType = AuditActionType.MERGED,
                    description = if (pluralItems.isNotEmpty()) {
                        tr(
                            "ادغام فرم مفرد و جمع به «$preferredSourceText» و انتقال فرم جمع به یادداشت (مرحله: $bestStage)",
                            "Merged singular and plural into «$preferredSourceText» and moved plural to note (stage: $bestStage)"
                        )
                    } else {
                        tr(
                            "ادغام ${duplicates.size + 1} واژه تکراری به «$preferredSourceText» (مرحله: $bestStage، تعداد ترجمه‌ها: ${mergedTranslations.size})",
                            "Merged ${duplicates.size + 1} duplicate words into «$preferredSourceText» (stage: $bestStage, translations: ${mergedTranslations.size})"
                        )
                    }
                )
            )
        }

        // بررسی اصلاحات خودکار برای واژگان غیرتکراری
        partition.singleItems.forEach { single ->
            if (!reviewMap.containsKey(single.concept.id) && single.concept.id !in alreadyInDefinite) {
                val updatedSourceText = single.cleanedResult.cleanSourceText
                val canonicalKey = TextUtilities.toCanonicalKey(updatedSourceText)
                val sourceTextChanged = updatedSourceText != single.sourceContent?.text

                val targetMods = mutableListOf<String>()
                val cleanedTargets = single.targetContents.map { target ->
                    val (norm, mods) = TextUtilities.normalizePersianTranslation(target.text, updatedSourceText)
                    if (mods.isNotEmpty()) targetMods.addAll(mods)
                    TextUtilities.cleanText(norm)
                }.filter { it.isNotBlank() }.distinct()

                val allNotes = mutableListOf<String>()
                single.sourceContent?.note?.let { if (it.isNotBlank()) allNotes.add(it) }
                allNotes.addAll(single.cleanedResult.extractedNotes)
                val mergedNote = allNotes.distinct().joinToString("\n").ifBlank { null }
                val noteChanged = mergedNote != single.sourceContent?.note

                val existingTargetTexts = single.targetContents.map { it.text }
                val extractedNormalized = single.cleanedResult.extractedTranslations.map {
                    TextUtilities.normalizePersianTranslation(it, updatedSourceText).first
                }.filter { it.isNotBlank() }
                val rawCombinedTargets = (cleanedTargets + extractedNormalized).distinct()
                val newCombinedTargets = TextUtilities.deduplicatePersianTranslations(rawCombinedTargets)
                    .ifEmpty { rawCombinedTargets }
                val targetsChanged = newCombinedTargets != existingTargetTexts
                if (targetsChanged && newCombinedTargets.size < existingTargetTexts.size) {
                    targetMods.add(tr("حذف ترجمه‌های فارسی تکراری", "Removed duplicate Persian translations"))
                }

                // فقط در صورت وجود تغییر واقعی دیتابیس آپدیت می‌شود
                if ((sourceTextChanged || targetsChanged || noteChanged) && single.sourceContent != null) {
                    val now = System.currentTimeMillis()

                    contentsToDeleteForConcepts.add(single.concept.id)
                    val updatedSource = ContentEntity(
                        id = single.sourceContent.id,
                        conceptId = single.concept.id,
                        languageCode = sourceLang,
                        text = updatedSourceText,
                        canonicalKey = canonicalKey,
                        note = mergedNote,
                        translationIndex = 0
                    )
                    val updatedTargets = newCombinedTargets.mapIndexed { idx, transText ->
                        ContentEntity(
                            id = UUID.randomUUID().toString(),
                            conceptId = single.concept.id,
                            languageCode = targetLang,
                            text = transText,
                            canonicalKey = TextUtilities.toCanonicalKey(transText),
                            note = null,
                            translationIndex = idx
                        )
                    }
                    contentsToInsertAll.add(updatedSource)
                    contentsToInsertAll.addAll(updatedTargets)

                    conceptsToUpdate.add(single.concept.copy(updatedAt = now))
                    cleanedCount++
                    val allModifications = (single.cleanedResult.modifications + targetMods).distinct()
                    actions.add(
                        AuditActionDetail(
                            originalText = single.sourceContent.text,
                            cleanedText = updatedSourceText,
                            actionType = if (single.cleanedResult.extractedNotes.isNotEmpty()) AuditActionType.NOTE_EXTRACTED else AuditActionType.CLEANED,
                            description = tr("پالایش واژه: ${allModifications.joinToString("، ")}", "Cleaned word: ${allModifications.joinToString(", ")}")
                        )
                    )
                }
            }
        }

        // ۴. اجرای دسته‌جمعی و سریع تغییرات دیتابیس در ترنزاکشن متمرکز
        withContext(Dispatchers.IO) {
            database.withTransaction {
                for ((dupIds, primaryId) in duplicateIdsToRepoint) {
                    reviewSessionDao.repointReviewHistory(dupIds, primaryId)
                    reviewSessionDao.repointReviewSessionItems(dupIds, primaryId)
                }

                if (conceptsToUpdate.isNotEmpty()) {
                    conceptsToUpdate.chunked(500).forEach { conceptDao.updateConcepts(it) }
                }
                if (contentsToDeleteForConcepts.isNotEmpty()) {
                    contentsToDeleteForConcepts.distinct().chunked(500).forEach { conceptDao.deleteContentsForConcepts(it) }
                }
                if (contentsToInsertAll.isNotEmpty()) {
                    contentsToInsertAll.chunked(1000).forEach { conceptDao.insertContents(it) }
                }
                if (conceptCategoriesToDeleteForConcepts.isNotEmpty()) {
                    conceptCategoriesToDeleteForConcepts.distinct().chunked(500).forEach { conceptDao.deleteConceptCategoriesForConcepts(it) }
                }
                if (conceptCategoriesToInsertAll.isNotEmpty()) {
                    conceptCategoriesToInsertAll.chunked(1000).forEach { conceptDao.insertConceptCategories(it) }
                }
                if (learningStatesToUpdate.isNotEmpty()) {
                    learningStatesToUpdate.chunked(500).forEach { learningDao.insertLearningStates(it) }
                }
                if (difficultyStatesToUpdate.isNotEmpty()) {
                    difficultyStatesToUpdate.chunked(500).forEach { learningDao.insertDifficultyStates(it) }
                }
                if (conceptsToDeletePerm.isNotEmpty()) {
                    val toDelete = conceptsToDeletePerm.distinct()
                    toDelete.chunked(500).forEach { chunk ->
                        learningDao.deleteLearningStatesForConcepts(chunk)
                        learningDao.deleteDifficultyStatesForConcepts(chunk)
                        conceptDao.deleteConceptCategoriesForConcepts(chunk)
                        conceptDao.deleteConceptsPermanently(chunk)
                    }
                }
            }
        }

        val totalRemaining = totalScanned - mergedCount
        LibraryAuditReport(
            totalScanned = totalScanned,
            mergedCount = mergedCount,
            cleanedCount = cleanedCount,
            totalRemaining = totalRemaining,
            actions = actions,
            needsReviewCount = 0,
            reviewItems = emptyList()
        )
    }

    /**
     * روش کمکی برای اجرای یک‌مرحله‌ای (Backward Compatibility)
     */
    suspend fun auditAndMergeLibrary(
        direction: CardDirection = CardDirection.NORMAL,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): LibraryAuditReport {
        return applyAuditFixes(direction, emptyList(), onProgress)
    }
}
