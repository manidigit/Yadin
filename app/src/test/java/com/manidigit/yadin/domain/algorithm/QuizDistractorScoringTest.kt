package com.manidigit.yadin.domain.algorithm

import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.VocabularyDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizDistractorScoringTest {

    @Test
    fun `identical strings have maximum lexical similarity`() {
        val sim = QuizDistractorScorer.quizLexicalSimilarity("manzana", "manzana")
        assertEquals(1.0, sim, 0.001)
    }

    @Test
    fun `similar words have higher lexical similarity than unrelated words`() {
        val simClose = QuizDistractorScorer.quizLexicalSimilarity("casa", "caza")
        val simFar = QuizDistractorScorer.quizLexicalSimilarity("casa", "elefante")
        assertTrue("Similar words ($simClose) should score higher than far words ($simFar)", simClose > simFar)
    }

    @Test
    fun `calculateConfusability is higher when category matches and difficulty is identical`() {
        val highConfusability = QuizDistractorScorer.calculateConfusability(
            correctAnswer = "سیب",
            candidateText = "گلابی",
            correctCategory = "خوراکی",
            candidateCategory = "خوراکی",
            correctDifficulty = VocabularyDifficulty.MEDIUM,
            candidateDifficulty = VocabularyDifficulty.MEDIUM
        )

        val lowConfusability = QuizDistractorScorer.calculateConfusability(
            correctAnswer = "سیب",
            candidateText = "هواپیما",
            correctCategory = "خوراکی",
            candidateCategory = "حمل‌ونقل",
            correctDifficulty = VocabularyDifficulty.MEDIUM,
            candidateDifficulty = VocabularyDifficulty.VERY_HARD
        )

        assertTrue(
            "Matching category and close difficulty ($highConfusability) should have higher confusability than unrelated ($lowConfusability)",
            highConfusability > lowConfusability
        )
    }

    @Test
    fun `EASY quiz level selects lowest confusability distractors`() {
        val candidates = listOf(
            QuizDistractorScorer.ScoredItem("خیلی گمراه‌کننده", 0.95),
            QuizDistractorScorer.ScoredItem("گمراه‌کننده متوسط", 0.50),
            QuizDistractorScorer.ScoredItem("کاندید ساده ۱", 0.10),
            QuizDistractorScorer.ScoredItem("کاندید ساده ۲", 0.15)
        )

        val selected = QuizDistractorScorer.selectDistractorTexts(QuizLevel.EASY, candidates, 2)
        assertEquals(listOf("کاندید ساده ۱", "کاندید ساده ۲"), selected)
    }

    @Test
    fun `HARD quiz level selects highest confusability distractors`() {
        val candidates = listOf(
            QuizDistractorScorer.ScoredItem("کاندید ساده ۱", 0.10),
            QuizDistractorScorer.ScoredItem("متوسط", 0.50),
            QuizDistractorScorer.ScoredItem("خیلی گمراه‌کننده ۱", 0.95),
            QuizDistractorScorer.ScoredItem("خیلی گمراه‌کننده ۲", 0.88)
        )

        val selected = QuizDistractorScorer.selectDistractorTexts(QuizLevel.HARD, candidates, 2)
        assertEquals(listOf("خیلی گمراه‌کننده ۱", "خیلی گمراه‌کننده ۲"), selected)
    }

    @Test
    fun `MEDIUM quiz level selects balanced confusability near half`() {
        val candidates = listOf(
            QuizDistractorScorer.ScoredItem("خیلی کم", 0.05),
            QuizDistractorScorer.ScoredItem("خیلی زیاد", 0.99),
            QuizDistractorScorer.ScoredItem("متعادل ۱", 0.48),
            QuizDistractorScorer.ScoredItem("متعادل ۲", 0.53)
        )

        val selected = QuizDistractorScorer.selectDistractorTexts(QuizLevel.MEDIUM, candidates, 2)
        assertTrue(selected.contains("متعادل ۱"))
        assertTrue(selected.contains("متعادل ۲"))
    }
}
