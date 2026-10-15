package com.manidigit.yadin.domain.algorithm

import androidx.room.withTransaction
import com.manidigit.yadin.data.local.dao.ConceptDao
import com.manidigit.yadin.data.local.dao.LearningDao
import com.manidigit.yadin.data.local.dao.ReviewSessionDao
import com.manidigit.yadin.data.local.database.YadinDatabase
import com.manidigit.yadin.data.local.entity.ConceptCategoryEntity
import com.manidigit.yadin.data.local.entity.ConceptEntity
import com.manidigit.yadin.data.local.entity.ContentEntity
import com.manidigit.yadin.data.local.entity.LearningStateEntity
import com.manidigit.yadin.domain.model.AuditActionDetail
import com.manidigit.yadin.domain.model.AuditActionType
import com.manidigit.yadin.domain.model.CardDirection
import com.manidigit.yadin.domain.model.LibraryAuditReport
import com.manidigit.yadin.domain.model.Stage
import com.manidigit.yadin.domain.text.TextUtilities
import java.util.UUID

/**
 * سرویس بررسی، پالایش، پاکسازی و ادغام هوشمند واژگان کتابخانه (Library Audit & Smart Merge Service)
 *
 * قابلیت‌ها:
 * ۱. شناسایی و ادغام (Merge) کلیه واژگان تکراری در کتابخانه با حفظ بالاترین مرحله جعبه لایتنر کاربر.
 * ۲. استخراج پرانتزها، نقش‌های گرامری و توضیحات چسبیده به متن واژه و انتقال منظم آن‌ها به بخش یادداشت‌ها یا ترجمه.
 * ۳. تفکیک جداکننده‌ها (نظیر :، -، =) در صورت ثبت ناخواسته جفت واژه/ترجمه در فیلد واژه.
 * ۴. حذف شماره‌گذاری‌های آغازین، نشانه‌ها و گیومه‌های زائد.
 * ۵. استانداردسازی پایگاه‌داده و اتصال مجدد سوابق مرور کارت‌های ادغام‌شده به کارت مرجع.
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

    companion object {
        private val REGEX_PAREN_BRACKET = Regex("""^(.*?)\s*[\(\[（]([^\)\]）]+)[\)\]）]\s*$""")
        private val REGEX_NUM_PREFIX = Regex("""^\s*[0-9۰-۹٠-٩]+\s*(?:[.)-]|:|[-—])\s*""")
        private val REGEX_BULLET_PREFIX = Regex("""^[-—*•#»«➜→\s]+""")

        /**
         * پالایش واژه و جداسازی کاراکترهای نامناسب، پرانتزها، جداکننده‌ها و علائم زائد
         */
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
                            modifications.add("استخراج ترجمه فارسی از سطر بعدی ($line)")
                        } else {
                            extractedNotes.add("متن اضافی سطر بعدی: $line")
                            modifications.add("انتقال سطر اضافی به یادداشت ($line)")
                        }
                    }
                }
            }

            // ۱. استانداردسازی فاصله و حذف نویسه‌های نامرئی
            val normalized = TextUtilities.cleanText(text)
            if (normalized != text) {
                text = normalized
                modifications.add("حذف کاراکترهای نامرئی و استانداردسازی فاصله")
            }

            // ۲. حذف شماره‌گذاری آغازین یا گلوله‌های نگارشی (مانند 1. یا - یا •)
            val strippedNumbering = stripNumberingAndBullets(text)
            if (strippedNumbering != text && strippedNumbering.isNotBlank()) {
                modifications.add("حذف شماره‌گذاری یا علامت آغازین")
                text = strippedNumbering
            }

            // ۳. حذف گیومه و نقل‌قول‌های احاطه‌کننده (مانند "word" یا «word»)
            val strippedQuotes = stripSurroundingQuotes(text)
            if (strippedQuotes != text && strippedQuotes.isNotBlank()) {
                modifications.add("حذف گیومه یا نشانه‌های نقل‌قول")
                text = strippedQuotes
            }

            // ۳.۵. اصلاح اولیه نشانه‌گذاری و علائم خراب انتهای متن (مانند ;¿ یا ;¡ قبل از بررسی جداکننده‌ها)
            val (prePunctCleaned, prePunctMods) = TextUtilities.normalizeSpanishPunctuation(text)
            if (prePunctCleaned != text) {
                text = prePunctCleaned
                modifications.addAll(prePunctMods)
            }

            // ۴. بررسی وجود جداکننده دوطرفه (مانند "casa : خانه" یا "run - دویدن" یا "Dispositivo V-16 / چراغ هشدار V-16")
            val pair = VocabularyParser.splitPair(text)
            if (pair != null) {
                val left = pair.first.trim()
                val right = pair.second.trim()
                if (left.isNotBlank() && right.isNotBlank()) {
                    text = left
                    if (VocabularyParser.hasPersian(right)) {
                        extractedTranslations.addAll(TextUtilities.splitTranslations(right))
                        modifications.add("جداسازی ترجمه چسبیده به واژه ($right)")
                    } else {
                        extractedNotes.add("توضیح جداشده: $right")
                        modifications.add("انتقال متن دوم به یادداشت ($right)")
                    }
                }
            }

            // ۵. استخراج پرانتزها و کروشه‌ها (مانند "apple (n.)" یا "correr (دویدن)" یا "good [adj]")
            var keepExtracting = true
            var loopCount = 0
            while (keepExtracting && loopCount < 5) {
                loopCount++
                val parenMatch = REGEX_PAREN_BRACKET.find(text)
                if (parenMatch != null) {
                    val main = parenMatch.groupValues[1].trim()
                    val inside = parenMatch.groupValues[2].trim()
                    if (main.isNotBlank() && inside.isNotBlank()) {
                        text = main
                        if (VocabularyParser.hasPersian(inside)) {
                            extractedTranslations.addAll(TextUtilities.splitTranslations(inside))
                            modifications.add("استخراج ترجمه فارسی داخل پرانتز ($inside)")
                        } else {
                            extractedNotes.add("نقش/توضیح: $inside")
                            modifications.add("انتقال توضیحات داخل پرانتز به یادداشت ($inside)")
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
                        modifications.add("تفکیک متن فارسی متداخل ($persianPart)")
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
    }

    private data class ConceptWorkingData(
        val concept: ConceptEntity,
        val sourceContent: ContentEntity?,
        val targetContents: List<ContentEntity>,
        val learningState: LearningStateEntity?,
        val categoryIds: List<String>,
        val cleanedResult: CleanedWordResult
    )

    /**
     * اجرای کامل اسکن، پالایش، ادغام واژگان تکراری و رفرش پایگاه داده
     */
    suspend fun auditAndMergeLibrary(
        direction: CardDirection = CardDirection.NORMAL,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): LibraryAuditReport {
        val actions = mutableListOf<AuditActionDetail>()
        var mergedCount = 0
        var cleanedCount = 0

        val (sourceLang, targetLang) = direction.resolveSourceAndTarget()

        return database.withTransaction {
            val allConcepts = conceptDao.getAllConcepts().filter { it.active }
            val totalScanned = allConcepts.size
            if (totalScanned == 0) {
                return@withTransaction LibraryAuditReport(
                    totalScanned = 0,
                    mergedCount = 0,
                    cleanedCount = 0,
                    totalRemaining = 0,
                    actions = emptyList()
                )
            }

            // گام ۱: واکشی داده‌های فعال و بررسی اولیه کاراکترها
            val workingList = mutableListOf<ConceptWorkingData>()
            allConcepts.forEachIndexed { idx, concept ->
                onProgress(idx + 1, totalScanned)
                val contents = conceptDao.getContentsForConcept(concept.id)
                val source = contents.firstOrNull { it.languageCode == sourceLang }
                    ?: contents.firstOrNull { it.languageCode != "fa" }
                    ?: contents.firstOrNull()
                val targets = contents.filter { it.id != source?.id }
                val learning = learningDao.getLearningState(concept.id, direction)
                val cats = conceptDao.getCategoriesForConcept(concept.id).map { it.id }

                val rawSourceText = source?.text ?: ""
                val cleaned = cleanAndExtractWord(rawSourceText)

                workingList.add(
                    ConceptWorkingData(
                        concept = concept,
                        sourceContent = source,
                        targetContents = targets,
                        learningState = learning,
                        categoryIds = (cats + listOfNotNull(concept.categoryId)).distinct(),
                        cleanedResult = cleaned
                    )
                )
            }

            // گام ۲: گروه‌بندی بر اساس کلید استاندارد (Canonical Key)
            val groupedByCanonical = workingList.groupBy {
                TextUtilities.toCanonicalKey(it.cleanedResult.cleanSourceText)
            }

            groupedByCanonical.forEach { (canonicalKey, items) ->
                if (canonicalKey.isBlank()) return@forEach

                if (items.size > 1) {
                    // واژگان تکراری شناسایی شدند! ادغام با حفظ پیشرفت لایتنر:
                    val primary = items.maxWithOrNull(
                        compareBy<ConceptWorkingData> { item ->
                            when (item.learningState?.stage) {
                                Stage.LEARNED -> 4
                                Stage.MONTHLY -> 3
                                Stage.WEEKLY -> 2
                                Stage.DAILY -> 1
                                null -> 0
                            }
                        }.thenBy {
                            it.learningState?.updatedAt ?: it.concept.updatedAt
                        }
                    ) ?: items.first()

                    val duplicates = items.filter { it.concept.id != primary.concept.id }
                    val duplicateIds = duplicates.map { it.concept.id }

                    // ترکیب ترجمه‌ها بدون تکرار
                    val existingTrans = (primary.targetContents.map { it.text } + primary.cleanedResult.extractedTranslations).toMutableList()
                    duplicates.forEach { dup ->
                        existingTrans.addAll(dup.targetContents.map { it.text })
                        existingTrans.addAll(dup.cleanedResult.extractedTranslations)
                    }
                    val mergedTranslations = existingTrans
                        .map { TextUtilities.cleanText(it) }
                        .filter { it.isNotBlank() }
                        .distinct()

                    // ترکیب یادداشت‌ها
                    val allNotes = mutableListOf<String>()
                    primary.sourceContent?.note?.let { if (it.isNotBlank()) allNotes.add(it) }
                    allNotes.addAll(primary.cleanedResult.extractedNotes)
                    duplicates.forEach { dup ->
                        dup.sourceContent?.note?.let { if (it.isNotBlank()) allNotes.add(it) }
                        allNotes.addAll(dup.cleanedResult.extractedNotes)
                    }
                    val mergedNote = allNotes.distinct().joinToString("\n").ifBlank { null }

                    // تجمیع دسته‌بندی‌ها
                    val allCatIds = (primary.categoryIds + duplicates.flatMap { it.categoryIds }).distinct()

                    // انتخاب بالاترین مرحله لایتنر
                    val bestStage = items.mapNotNull { it.learningState?.stage }.maxWithOrNull(
                        compareBy { stage ->
                            when (stage) {
                                Stage.LEARNED -> 4
                                Stage.MONTHLY -> 3
                                Stage.WEEKLY -> 2
                                Stage.DAILY -> 1
                            }
                        }
                    ) ?: (primary.learningState?.stage ?: Stage.DAILY)

                    val now = System.currentTimeMillis()

                    // به‌روزرسانی مفهوم مرجع
                    conceptDao.updateConcept(
                        primary.concept.copy(
                            categoryId = allCatIds.firstOrNull() ?: primary.concept.categoryId,
                            updatedAt = now
                        )
                    )

                    // بازنویسی محتوای مفهوم مرجع
                    conceptDao.deleteContentsForConcept(primary.concept.id)
                    val newSourceContent = ContentEntity(
                        id = primary.sourceContent?.id ?: UUID.randomUUID().toString(),
                        conceptId = primary.concept.id,
                        languageCode = sourceLang,
                        text = primary.cleanedResult.cleanSourceText,
                        canonicalKey = canonicalKey,
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
                    conceptDao.insertContents(listOf(newSourceContent) + newTargetContents)

                    // اتصال دسته‌های تجمیع‌شده
                    conceptDao.deleteConceptCategories(primary.concept.id)
                    allCatIds.forEach { catId ->
                        conceptDao.insertConceptCategory(ConceptCategoryEntity(primary.concept.id, catId))
                    }

                    // به‌روزرسانی مرحله یادگیری به بالاترین سطح
                    if (primary.learningState != null) {
                        learningDao.updateLearningState(
                            primary.learningState.copy(
                                stage = bestStage,
                                updatedAt = now
                            )
                        )
                    }

                    // انتقال سوابق مرور و آیتم‌های نشست به مفهوم مرجع
                    reviewSessionDao.repointReviewHistory(duplicateIds, primary.concept.id)
                    reviewSessionDao.repointReviewSessionItems(duplicateIds, primary.concept.id)

                    // پاکسازی رکوردهای تکراری مازاد
                    conceptDao.deleteContentsForConcepts(duplicateIds)
                    learningDao.deleteLearningStatesForConcepts(duplicateIds)
                    learningDao.deleteDifficultyStatesForConcepts(duplicateIds)
                    conceptDao.deleteConceptCategoriesForConcepts(duplicateIds)
                    conceptDao.deleteConceptsPermanently(duplicateIds)

                    mergedCount += duplicates.size
                    actions.add(
                        AuditActionDetail(
                            originalText = primary.sourceContent?.text ?: primary.cleanedResult.cleanSourceText,
                            cleanedText = primary.cleanedResult.cleanSourceText,
                            actionType = AuditActionType.MERGED,
                            description = "ادغام ${duplicates.size + 1} کارت تکراری واژه '${primary.cleanedResult.cleanSourceText}' (مرحله: $bestStage، تعداد ترجمه‌ها: ${mergedTranslations.size})"
                        )
                    )
                } else {
                    // واژه تکراری نیست، اما ممکن است نیاز به پاکسازی کاراکتر یا یادداشت داشته باشد
                    val single = items.first()
                    val targetMods = mutableListOf<String>()
                    val cleanedTargets = single.targetContents.map { target ->
                        val (norm, mods) = TextUtilities.normalizeSpanishPunctuation(target.text)
                        if (mods.isNotEmpty()) targetMods.addAll(mods)
                        TextUtilities.cleanText(norm)
                    }.filter { it.isNotBlank() }.distinct()

                    val targetWasModified = targetMods.isNotEmpty() || cleanedTargets != single.targetContents.map { it.text }

                    if ((single.cleanedResult.wasModified || targetWasModified) && single.sourceContent != null) {
                        val now = System.currentTimeMillis()
                        val existingTrans = (cleanedTargets + single.cleanedResult.extractedTranslations).distinct()
                        val allNotes = mutableListOf<String>()
                        single.sourceContent.note?.let { if (it.isNotBlank()) allNotes.add(it) }
                        allNotes.addAll(single.cleanedResult.extractedNotes)
                        val mergedNote = allNotes.distinct().joinToString("\n").ifBlank { null }

                        conceptDao.deleteContentsForConcept(single.concept.id)
                        val updatedSource = ContentEntity(
                            id = single.sourceContent.id,
                            conceptId = single.concept.id,
                            languageCode = sourceLang,
                            text = single.cleanedResult.cleanSourceText,
                            canonicalKey = canonicalKey,
                            note = mergedNote,
                            translationIndex = 0
                        )
                        val updatedTargets = existingTrans.mapIndexed { idx, transText ->
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
                        conceptDao.insertContents(listOf(updatedSource) + updatedTargets)
                        conceptDao.updateConcept(single.concept.copy(updatedAt = now))

                        cleanedCount++
                        val allModifications = (single.cleanedResult.modifications + targetMods).distinct()
                        actions.add(
                            AuditActionDetail(
                                originalText = single.sourceContent.text,
                                cleanedText = single.cleanedResult.cleanSourceText,
                                actionType = if (single.cleanedResult.extractedNotes.isNotEmpty()) AuditActionType.NOTE_EXTRACTED else AuditActionType.CLEANED,
                                description = "پالایش واژه: ${allModifications.joinToString("، ")}"
                            )
                        )
                    }
                }
            }

            val totalRemaining = totalScanned - mergedCount
            LibraryAuditReport(
                totalScanned = totalScanned,
                mergedCount = mergedCount,
                cleanedCount = cleanedCount,
                totalRemaining = totalRemaining,
                actions = actions
            )
        }
    }
}
