package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.ParseWarning
import com.manidigit.yadin.domain.model.ParseWarningType
import com.manidigit.yadin.domain.model.VariantType
import com.manidigit.yadin.domain.model.VocabularyBreakdownEntry
import com.manidigit.yadin.domain.model.VocabularyRelationEntry
import com.manidigit.yadin.domain.model.VocabularyVariantEntry
import com.manidigit.yadin.domain.text.TextUtilities
import java.text.Normalizer
import java.util.Locale

// Precompiled Regexes at file level for O(n) performance (Audit B-11)
private val REGEX_SEPARATOR = Regex("^[-—_=*•]{3,}$")
private val REGEX_NUMBERED = Regex("^\\s*[0-9۰-۹٠-٩]+\\s*(?:[.)-]|:|[-—])\\s*")
private val REGEX_DASH_SPLIT = Regex("\\s+[-—]\\s+")
private val REGEX_STRIP_DECORATIONS = Regex("^[-—*•#»«➜→\\s]+")
private val REGEX_PARENTHETICAL_NOTE = Regex("^(.*?)\\s*[(（]([^)）]{4,})[)）]\\s*$")
private val REGEX_EXAMPLE_MARKER = Regex("^(example|examples|note|notes|usage|ejemplo|ejemplos|nota|uso)\\s+[^:]{1,60}:", RegexOption.IGNORE_CASE)

private enum class LineType {
    COMMENT,
    SEPARATOR,
    NUMBER,
    BREAKDOWN,
    DERIVATIVE,
    VARIANT,
    RELATION,
    GRAMMAR_NOTE,
    NOTE,
    ENTRY_HEADER,
    TRANSLATION,
    UNKNOWN
}

private enum class ScriptType {
    LATIN,
    PERSIAN,
    MIXED,
    UNKNOWN
}

object VocabularyParser {

    fun parse(rawText: String?): ParseResult {
        if (rawText.isNullOrBlank()) {
            return ParseResult(emptyList(), emptyList())
        }

        // Phase 1: Normalization
        val normalized = Normalizer.normalize(rawText, Normalizer.Form.NFC)
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("\u200B", "")
            .replace("\t", " ")

        val lines = normalized.lines()
        val entries = mutableListOf<ParsedEntry>()
        val warnings = mutableListOf<ParseWarning>()

        var currentSource: String? = null
        var currentTranslations = mutableListOf<String>()
        val currentNotes = mutableListOf<String>()
        val currentBreakdowns = mutableListOf<VocabularyBreakdownEntry>()
        val currentVariants = mutableListOf<VocabularyVariantEntry>()
        val currentRelations = mutableListOf<VocabularyRelationEntry>()
        val currentEvidence = mutableSetOf<String>()
        val currentRawLines = mutableListOf<String>()
        var currentLineNumber = 1
        var currentConfidence = 1.0

        var pendingOrphanTranslation: Pair<String, Int>? = null

        fun flushEntry() {
            val src = currentSource?.let { TextUtilities.cleanText(it) }
            if (!src.isNullOrBlank()) {
                val transList = if (currentTranslations.isNotEmpty()) {
                    currentTranslations.flatMap { TextUtilities.splitTranslations(it) }.distinct()
                } else {
                    emptyList()
                }

                if (transList.isEmpty()) {
                    warnings.add(
                        ParseWarning(
                            type = ParseWarningType.ORPHAN_SOURCE,
                            lineNumber = currentLineNumber,
                            rawText = src,
                            message = "واژه مبدأ بدون ترجمه مشخص است"
                        )
                    )
                    currentConfidence = currentConfidence.coerceAtMost(0.4)
                }

                val entryType = classifyEntry(src)
                val noteCombined = if (currentNotes.isNotEmpty()) currentNotes.joinToString("\n") else null

                entries.add(
                    ParsedEntry(
                        sourceText = src,
                        translations = transList,
                        note = noteCombined,
                        grammarNotes = null,
                        categoryNames = emptyList(),
                        entryType = entryType,
                        confidence = currentConfidence.coerceIn(0.0, 1.0),
                        lineNumber = currentLineNumber,
                        rawLines = currentRawLines.toList(),
                        evidence = currentEvidence.toList(),
                        variants = currentVariants.toList(),
                        breakdowns = currentBreakdowns.toList(),
                        relations = currentRelations.toList()
                    )
                )
            }

            // Reset
            currentSource = null
            currentTranslations.clear()
            currentNotes.clear()
            currentBreakdowns.clear()
            currentVariants.clear()
            currentRelations.clear()
            currentEvidence.clear()
            currentRawLines.clear()
            currentConfidence = 1.0
        }

        for ((idx, rawLine) in lines.withIndex()) {
            val lineNum = idx + 1
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue

            val classification = classifyLine(trimmed)

            when (classification) {
                LineType.COMMENT -> {
                    // Ignore completely (Rule 10)
                }
                LineType.SEPARATOR -> {
                    flushEntry()
                }
                LineType.NUMBER -> {
                    // Ignored standalone numbers
                }
                LineType.BREAKDOWN -> {
                    currentRawLines.add(trimmed)
                    val content = stripMarker(trimmed)
                    val pair = splitPair(content)
                    if (pair != null) {
                        currentBreakdowns.add(
                            VocabularyBreakdownEntry(
                                sourcePart = pair.first,
                                translationPart = pair.second,
                                orderIndex = currentBreakdowns.size
                            )
                        )
                    } else {
                        currentNotes.add("تجزیه: $content")
                    }
                }
                LineType.NOTE, LineType.GRAMMAR_NOTE -> {
                    currentRawLines.add(trimmed)
                    val content = stripMarker(trimmed)
                    currentNotes.add(content)
                }
                LineType.VARIANT -> {
                    currentRawLines.add(trimmed)
                    val content = stripMarker(trimmed)
                    currentVariants.add(
                        VocabularyVariantEntry(
                            sourceText = content,
                            translationText = null,
                            variantType = VariantType.ALTERNATIVE
                        )
                    )
                }
                LineType.RELATION, LineType.DERIVATIVE -> {
                    currentRawLines.add(trimmed)
                    val content = stripMarker(trimmed)
                    currentNotes.add(content)
                }
                LineType.ENTRY_HEADER -> {
                    currentRawLines.add(trimmed)
                    val stripped = stripNumbering(trimmed)
                    val pair = splitPair(stripped)

                    if (pair != null) {
                        // Inline translation e.g. "1. manzana -> سیب"
                        flushEntry()
                        currentLineNumber = lineNum
                        currentSource = pair.first
                        val extracted = extractParentheticalNote(pair.second)
                        currentTranslations.add(extracted.first)
                        extracted.second?.let { currentNotes.add(it) }
                        currentConfidence = 1.0
                        currentEvidence.add("inlineTranslationMarker")
                        if (isNumbered(trimmed)) currentEvidence.add("numbered")
                    } else {
                        // Multi-line source or standalone source header
                        if (currentSource == null) {
                            currentLineNumber = lineNum
                            currentSource = stripped
                            currentConfidence = 0.8
                            if (isNumbered(trimmed)) {
                                currentConfidence = 0.9
                                currentEvidence.add("numbered")
                            }
                            if (pendingOrphanTranslation != null) {
                                currentTranslations.add(pendingOrphanTranslation.first)
                                currentEvidence.add("orphanPersianPaired")
                                currentConfidence += 0.1
                                pendingOrphanTranslation = null
                            }
                        } else if (currentTranslations.isEmpty()) {
                            // Multiline source continuation
                            currentSource = "$currentSource $stripped"
                            currentEvidence.add("multilineSource")
                        } else {
                            flushEntry()
                            currentLineNumber = lineNum
                            currentSource = stripped
                            currentConfidence = 0.8
                            if (isNumbered(trimmed)) {
                                currentConfidence = 0.9
                                currentEvidence.add("numbered")
                            }
                        }
                    }
                }
                LineType.TRANSLATION -> {
                    currentRawLines.add(trimmed)
                    val extracted = extractParentheticalNote(trimmed)
                    if (currentSource != null) {
                        currentTranslations.add(extracted.first)
                        extracted.second?.let { currentNotes.add(it) }
                        currentEvidence.add("adjacentPersianTranslation")
                        currentConfidence = (currentConfidence + 0.15).coerceAtMost(1.0)
                    } else {
                        // Persian translation before Spanish word
                        pendingOrphanTranslation = Pair(extracted.first, lineNum)
                        extracted.second?.let { currentNotes.add(it) }
                    }
                }
                LineType.UNKNOWN -> {
                    currentRawLines.add(trimmed)
                    if (currentSource != null) {
                        currentNotes.add(trimmed)
                    } else {
                        warnings.add(
                            ParseWarning(
                                type = ParseWarningType.UNKNOWN_FORMAT,
                                lineNumber = lineNum,
                                rawText = trimmed,
                                message = "ساختار خط نامشخص است"
                            )
                        )
                    }
                }
            }
        }

        flushEntry()

        if (pendingOrphanTranslation != null) {
            warnings.add(
                ParseWarning(
                    type = ParseWarningType.ORPHAN_TRANSLATION,
                    lineNumber = pendingOrphanTranslation.second,
                    rawText = pendingOrphanTranslation.first,
                    message = "ترجمه فارسی بدون واژه مبدأ باقی ماند"
                )
            )
        }

        return ParseResult(entries, warnings)
    }

    private fun classifyLine(line: String): LineType {
        if (line.startsWith("#") || line.startsWith("//")) return LineType.COMMENT
        if (isSeparator(line)) return LineType.SEPARATOR
        if (line.all { it.isDigit() }) return LineType.NUMBER

        val lower = line.lowercase(Locale.ROOT)
        // Fix for audit A-3: Markers must end with colon or be exact full line
        if (matchesMarker(lower, NOTE_MARKERS)) return LineType.NOTE
        if (REGEX_EXAMPLE_MARKER.containsMatchIn(lower)) return LineType.NOTE
        if (matchesMarker(lower, GRAMMAR_MARKERS)) return LineType.GRAMMAR_NOTE
        if (matchesMarker(lower, BREAKDOWN_MARKERS)) return LineType.BREAKDOWN
        if (matchesMarker(lower, DERIVATIVE_MARKERS)) return LineType.DERIVATIVE
        if (matchesMarker(lower, VARIANT_MARKERS)) return LineType.VARIANT
        if (matchesMarker(lower, RELATION_MARKERS)) return LineType.RELATION

        val stripped = stripNumbering(line)
        val pair = splitPair(stripped)
        if (pair != null && hasLatin(pair.first) && hasPersian(pair.second)) {
            return LineType.ENTRY_HEADER
        }

        val script = detectScript(stripped)
        if (script == ScriptType.PERSIAN) return LineType.TRANSLATION
        if (script == ScriptType.LATIN || hasLatin(stripped)) return LineType.ENTRY_HEADER

        return LineType.UNKNOWN
    }

    private val NOTE_MARKERS = listOf("نکته:", "توضیحات:", "احتمال اشتباه:", "توجه:", "مثال:", "note:", "notes:", "usage:", "ejemplo:", "ejemplos:", "nota:", "uso:")
    private val GRAMMAR_MARKERS = listOf("نکته گرامری:", "نکته گرامری", "grammar:", "grammar note:", "grammatical note:", "nota gramatical:")
    private val BREAKDOWN_MARKERS = listOf("تجزیه:", "تجزیه", "breakdown:", "breakdown")
    private val DERIVATIVE_MARKERS = listOf("مشتق شده از:", "مشتق شده از", "derived from:", "derived from")
    private val VARIANT_MARKERS = listOf("حالت دیگر:", "حالت دیگر", "variant:", "variants:")
    private val RELATION_MARKERS = listOf("مرتبط:", "مرتبط", "related:", "related", "synonym:", "antonym:")

    private fun matchesMarker(lowerLine: String, markers: List<String>): Boolean {
        for (marker in markers) {
            if (marker.endsWith(":")) {
                if (lowerLine.startsWith(marker)) return true
            } else {
                if (lowerLine == marker || lowerLine.startsWith("$marker ")) return true
            }
        }
        return false
    }

    private fun stripMarker(line: String): String {
        val colonIdx = line.indexOf(':')
        return if (colonIdx in 1..40) {
            line.substring(colonIdx + 1).trim()
        } else {
            line.trim()
        }
    }

    fun isSeparator(s: String): Boolean = REGEX_SEPARATOR.matches(s)

    fun isNumbered(s: String): Boolean = REGEX_NUMBERED.containsMatchIn(s)

    fun stripNumbering(s: String): String {
        val withoutNum = REGEX_NUMBERED.replaceFirst(s, "").trim()
        return REGEX_STRIP_DECORATIONS.replaceFirst(withoutNum, "").trim()
    }

    fun hasLatin(s: String): Boolean {
        return s.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.LATIN }
    }

    fun hasPersian(s: String): Boolean {
        return s.any { c ->
            val code = c.code
            (code in 0x0600..0x06FF) || (code in 0x0750..0x077F) || (code in 0x08A0..0x08FF)
        }
    }

    private fun detectScript(s: String): ScriptType {
        var latinCount = 0
        var persianCount = 0
        for (c in s) {
            val code = c.code
            if (Character.UnicodeScript.of(code) == Character.UnicodeScript.LATIN) latinCount++
            if ((code in 0x0600..0x06FF) || (code in 0x0750..0x077F) || (code in 0x08A0..0x08FF)) persianCount++
        }
        return when {
            latinCount > 0 && persianCount > 0 -> ScriptType.MIXED
            latinCount > 0 -> ScriptType.LATIN
            persianCount > 0 -> ScriptType.PERSIAN
            else -> ScriptType.UNKNOWN
        }
    }

    fun splitPair(s: String): Pair<String, String>? {
        // Priority 1: Arrow symbols
        for (arrow in listOf("→", "➜")) {
            val idx = s.indexOf(arrow)
            if (idx in 1 until s.length - 1) {
                return Pair(s.substring(0, idx).trim(), s.substring(idx + 1).trim())
            }
        }
        // Priority 2: Dash enclosed in spaces
        val dashMatch = REGEX_DASH_SPLIT.find(s)
        if (dashMatch != null && dashMatch.range.first > 0 && dashMatch.range.last < s.length - 1) {
            return Pair(s.substring(0, dashMatch.range.first).trim(), s.substring(dashMatch.range.last + 1).trim())
        }
        // Priority 3: Colon if left is Latin and right is Persian
        val colonIdx = s.indexOf(':')
        if (colonIdx in 1 until s.length - 1) {
            val left = s.substring(0, colonIdx).trim()
            val right = s.substring(colonIdx + 1).trim()
            if (hasLatin(left) && hasPersian(right)) {
                return Pair(left, right)
            }
        }
        return null
    }

    private fun extractParentheticalNote(translation: String): Pair<String, String?> {
        val match = REGEX_PARENTHETICAL_NOTE.find(translation) ?: return Pair(translation, null)
        val mainText = match.groupValues[1].trim()
        val noteContent = match.groupValues[2].trim()

        // Preserve short grammar qualifiers inside translation
        val isGrammarQualifier = noteContent.length <= 8 && (
            noteContent.contains("مذکر") || noteContent.contains("مؤنث") ||
            noteContent.contains("فعل") || noteContent.contains("صفت") ||
            noteContent.contains("جمع") || noteContent.contains("عام")
        )

        return if (isGrammarQualifier) {
            Pair(translation, null)
        } else {
            Pair(if (mainText.isNotBlank()) mainText else translation, noteContent)
        }
    }

    fun classifyEntry(source: String): EntryType {
        val words = source.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            words.size <= 1 -> EntryType.WORD
            words.size in 2..5 -> EntryType.PHRASE
            else -> EntryType.SENTENCE
        }
    }
}
