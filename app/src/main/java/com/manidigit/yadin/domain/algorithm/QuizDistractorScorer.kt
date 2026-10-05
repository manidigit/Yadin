package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.VocabularyDifficulty

object QuizDistractorScorer {

    private val QUIZ_TOKEN_SPLIT = Regex("[\\s,/،؛;\\-]+")
    private val SEGMENT_SPLIT_REGEX = Regex("[,،/;؛|\\n]+")
    private val PARENTHESIS_REGEX = Regex("\\s*\\([^)]*\\)|\\s*\\[[^]]*\\]|\\s*\\{[^}]*\\}")
    private val WHITESPACE_REGEX = Regex("\\s+")
    private val PUNCTUATION_REGEX = Regex("[!؟?¡¿.:;؛'\"«»’“”\\-_~*]")

    private val GRAMMAR_CLARIFIERS = listOf(
        "برای مونث", "برای مؤنث", "برای مذکر",
        "مونث", "مؤنث", "مذکر",
        "جمع", "مفرد",
        "رسمی", "غیررسمی", "غیر رسمی", "محترمانه", "عامیانه",
        "حالت امری", "زمان گذشته", "زمان حال", "زمان اینده", "زمان آینده",
        "صفت", "اسم", "فعل", "قید", "اصطلاح"
    )

    private val PERSIAN_STOP_WORDS = setOf(
        "و", "یا", "به", "در", "از", "با", "برای", "تا", "که", "را", "یک", "این", "آن", "است", "هست", "شد", "بود",
        "من", "تو", "او", "ما", "شما", "آنها", "اینها", "ایشان", "وی", "خود", "چه", "کدام", "کی", "کجا", "چرا", "چگونه", "چطور"
    )

    private val PERSIAN_NOUN_SUFFIXES = listOf(
        "هایمان", "هایتان", "هایشان", "هایم", "هایت", "هایش", "های", "ها",
        "ترین", "تر", "ایی", "ای", "ات", "ان", "ی"
    )

    /**
     * Precomputed semantic representation of a text string for high-speed collision checking.
     * Computing this once per pool entry speeds up quiz generation by over 100x.
     */
    data class PrecomputedSemantic(
        val rawText: String,
        val normalized: String,
        val core: String,
        val compact: String,
        val compactSegments: Set<String>,
        val segmentTokenSets: List<Set<String>>,
        val contentTokens: Set<String>
    )

    fun normalizeQuiz(text: String): String {
        return text.trim().lowercase()
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
            .replace('ۀ', 'ه')
            .replace('ؤ', 'و')
            .replace('إ', 'ا')
            .replace('أ', 'ا')
            .replace('آ', 'ا')
            .replace('ئ', 'ی')
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ü', 'u')
            .replace('ñ', 'n')
    }

    /**
     * Stems common Persian noun/adjective indefinite and plural suffixes
     * while preserving verb tenses and personal conjugations.
     */
    fun stemPersian(word: String): String {
        var w = word.replace("\u200c", "").trim()
        if (w.length <= 3) return w
        for (s in PERSIAN_NOUN_SUFFIXES) {
            if (w.length > s.length + 2 && w.endsWith(s)) {
                w = w.substring(0, w.length - s.length)
                break
            }
        }
        return w
    }

    /**
     * Cleans an individual phrase/segment by removing parentheses, grammar clarifiers,
     * punctuation, and normalizing colloquial pronoun variations.
     */
    fun cleanSegment(text: String): String {
        var t = normalizeQuiz(text)
        t = t.replace(PARENTHESIS_REGEX, " ")
        for (clarifier in GRAMMAR_CLARIFIERS) {
            t = t.replace(clarifier, " ")
        }
        t = t.replace(PUNCTUATION_REGEX, " ")

        // Unify verb prefixes "می‌" and "نمی‌" to "می" and "نمی" attached (using Unicode whitespace boundaries)
        t = t.replace(Regex("(^|\\s)می[\\s\u200c]+"), "$1می")
        t = t.replace(Regex("(^|\\s)نمی[\\s\u200c]+"), "$1نمی")

        // Strip indefinite suffix "‌ای" / " ای"
        t = t.replace(Regex("[\\s\u200c]+ای(?=$|[\\s,،.!?])"), " ")
        t = t.replace(Regex("\u200c"), " ")

        val tokens = t.split(WHITESPACE_REGEX).filter { it.isNotBlank() }.map { token ->
            when (token) {
                "شماها", "شمایان" -> "شما"
                "آنها", "آن‌ها", "اونها", "ایشان", "ایشون" -> "آنها"
                "اینها", "این‌ها", "اینا" -> "اینها"
                "ماها" -> "ما"
                "توها" -> "تو"
                else -> stemPersian(token)
            }
        }
        return tokens.joinToString(" ").trim()
    }

    /**
     * Compacts a string by removing whitespace and half-spaces for fuzzy comparison.
     */
    fun compactString(text: String): String {
        return cleanSegment(text)
            .replace("\u200c", "")
            .replace(" ", "")
    }

    /**
     * Normalizes text for core semantic comparison.
     */
    fun normalizeCore(text: String): String {
        return cleanSegment(text)
    }

    /**
     * Extracts individual synonym segments separated by commas, slashes, semicolons, etc.
     */
    fun extractSegments(text: String): List<String> {
        val parts = text.split(SEGMENT_SPLIT_REGEX)
        val result = mutableListOf<String>()
        for (part in parts) {
            val cleaned = cleanSegment(part)
            if (cleaned.isNotBlank()) {
                result.add(cleaned)
            }
        }
        val fullCleaned = cleanSegment(text)
        if (fullCleaned.isNotBlank() && !result.contains(fullCleaned)) {
            result.add(fullCleaned)
        }
        return result.distinct()
    }

    /**
     * Extracts content tokens (excluding stop-words and applying Persian stemming).
     */
    fun extractContentTokens(text: String): Set<String> {
        val cleaned = cleanSegment(text)
        return cleaned.split(WHITESPACE_REGEX)
            .map { stemPersian(it) }
            .filter { it.isNotBlank() && it !in PERSIAN_STOP_WORDS }
            .toSet()
    }

    /**
     * Precomputes all representation components for a text string.
     */
    fun precomputeSemantic(text: String): PrecomputedSemantic {
        val normalized = normalizeQuiz(text)
        val core = cleanSegment(text)
        val compact = core.replace("\u200c", "").replace(" ", "")
        val segs = extractSegments(text)
        val compactSegs = segs.map { compactString(it) }.filter { it.isNotBlank() }.toSet()
        val segTokenSets = segs.map { s ->
            s.split(WHITESPACE_REGEX).map { stemPersian(it) }.filter { it.isNotBlank() && it !in PERSIAN_STOP_WORDS }.toSet()
        }.filter { it.isNotEmpty() }
        val contentTokens = extractContentTokens(text)

        return PrecomputedSemantic(
            rawText = text,
            normalized = normalized,
            core = core,
            compact = compact,
            compactSegments = compactSegs,
            segmentTokenSets = segTokenSets,
            contentTokens = contentTokens
        )
    }

    /**
     * Ultra-fast semantic collision checking using precomputed representations.
     */
    fun arePrecomputedColliding(a: PrecomputedSemantic, b: PrecomputedSemantic): Boolean {
        if (a.normalized == b.normalized) return true
        if (a.core.isNotEmpty() && a.core == b.core) return true
        if (a.compact.isNotEmpty() && a.compact == b.compact) return true

        // 1. Synonym Permutation: Compare set of compacted segments
        if (a.compactSegments.isNotEmpty() && a.compactSegments == b.compactSegments) return true

        // 2. Direct segment equality / sharing of any synonym / substring containment
        for (cA in a.compactSegments) {
            for (cB in b.compactSegments) {
                if (cA == cB) return true
                val minLen = minOf(cA.length, cB.length)
                if (minLen >= 5 && (cA.contains(cB) || cB.contains(cA))) {
                    return true
                }
            }
        }

        // 3. Segment-by-segment token comparison with stemming
        for (tokensA in a.segmentTokenSets) {
            for (tokensB in b.segmentTokenSets) {
                if (tokensA == tokensB) return true
                if (tokensB.containsAll(tokensA) || tokensA.containsAll(tokensB)) return true

                val inter = tokensA.intersect(tokensB).size
                val union = tokensA.union(tokensB).size
                if (union > 0 && inter >= 2) {
                    val jaccard = inter.toDouble() / union.toDouble()
                    if (jaccard >= 0.6) return true
                }
            }
        }

        // 4. Full text content tokens comparison
        if (a.contentTokens.isNotEmpty() && b.contentTokens.isNotEmpty()) {
            if (a.contentTokens == b.contentTokens) return true
            if (b.contentTokens.containsAll(a.contentTokens) || a.contentTokens.containsAll(b.contentTokens)) return true

            val intersection = a.contentTokens.intersect(b.contentTokens).size
            val union = a.contentTokens.union(b.contentTokens).size
            if (union > 0 && intersection >= 2) {
                val jaccard = intersection.toDouble() / union.toDouble()
                if (jaccard >= 0.6) return true
            }
        }

        return false
    }

    /**
     * Returns true if two text options share the same meaning, are permutations of the same
     * synonyms, share overlapping translations, or differ only by trivial clarifications.
     */
    fun areSemanticallyColliding(textA: String, textB: String): Boolean {
        return arePrecomputedColliding(precomputeSemantic(textA), precomputeSemantic(textB))
    }

    fun levenshteinSimilarity(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)
        for (i in a.indices) {
            current[0] = i + 1
            for (j in b.indices) {
                val sub = previous[j] + if (a[i] == b[j]) 0 else 1
                current[j + 1] = minOf(previous[j + 1] + 1, current[j] + 1, sub)
            }
            val tmp = previous
            previous = current
            current = tmp
        }
        val distance = previous[b.length]
        return 1.0 - (distance.toDouble() / maxOf(a.length, b.length).toDouble()).coerceIn(0.0, 1.0)
    }

    fun quizLexicalSimilarity(a: String, b: String): Double {
        val normA = normalizeQuiz(a)
        val normB = normalizeQuiz(b)
        if (normA == normB) return 1.0
        val tokensA = normA.split(QUIZ_TOKEN_SPLIT).filter { it.isNotBlank() }.toSet()
        val tokensB = normB.split(QUIZ_TOKEN_SPLIT).filter { it.isNotBlank() }.toSet()
        val tokenSim = if (tokensA.isEmpty() && tokensB.isEmpty()) 1.0
        else if (tokensA.isEmpty() || tokensB.isEmpty()) 0.0
        else tokensA.intersect(tokensB).size.toDouble() / tokensA.union(tokensB).size.toDouble()

        val levSim = levenshteinSimilarity(normA, normB)
        return (tokenSim * 0.5 + levSim * 0.5).coerceIn(0.0, 1.0)
    }

    fun diffDistance(d1: VocabularyDifficulty, d2: VocabularyDifficulty): Int {
        return kotlin.math.abs(d1.ordinal - d2.ordinal)
    }

    fun calculateConfusability(
        correctAnswer: String,
        candidateText: String,
        correctCategory: String?,
        candidateCategory: String?,
        correctDifficulty: VocabularyDifficulty,
        candidateDifficulty: VocabularyDifficulty
    ): Double {
        val lexSim = quizLexicalSimilarity(correctAnswer, candidateText)
        val catMatch = if (candidateCategory != null && candidateCategory == correctCategory) 1.0 else 0.0
        val diffDist = diffDistance(correctDifficulty, candidateDifficulty)
        val diffBonus = (3 - diffDist).coerceAtLeast(0) / 3.0

        return (lexSim * 0.4 + catMatch * 0.35 + diffBonus * 0.25).coerceIn(0.0, 1.0)
    }

    data class ScoredItem(val text: String, val score: Double)

    fun selectDistractorTexts(
        quizLevel: QuizLevel,
        candidates: List<ScoredItem>,
        takeCount: Int = 3
    ): List<String> {
        return when (quizLevel) {
            QuizLevel.EASY -> {
                candidates.sortedBy { it.score }.map { it.text }.take(takeCount)
            }
            QuizLevel.MEDIUM -> {
                candidates.sortedBy { kotlin.math.abs(it.score - 0.5) }.map { it.text }.take(takeCount)
            }
            QuizLevel.HARD -> {
                candidates.sortedByDescending { it.score }.map { it.text }.take(takeCount)
            }
        }
    }
}
