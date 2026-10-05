package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.VocabularyDifficulty

object QuizDistractorScorer {

    private val QUIZ_TOKEN_SPLIT = Regex("[\\s,/،\\-]+")

    fun normalizeQuiz(text: String): String {
        return text.trim().lowercase()
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('á', 'a')
            .replace('é', 'e')
            .replace('í', 'i')
            .replace('ó', 'o')
            .replace('ú', 'u')
            .replace('ñ', 'n')
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
