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

    @Test
    fun `areSemanticallyColliding correctly flags parenthetical and pronoun plural duplicates`() {
        // Core case reported by user: "usted durmió"
        val answer = "شما خوابیدید"
        val duplicateParenthetical = "شما (جمع) خوابیدید"
        val duplicateColloquial = "شماها خوابیدید"
        val validDistractorDifferentTense = "شما می‌خوابیدید"
        val validDistractorDifferentPerson = "ما خوابیدیم"

        assertTrue(
            "Parenthetical clarification '(جمع)' must collide with base answer",
            QuizDistractorScorer.areSemanticallyColliding(duplicateParenthetical, answer)
        )

        assertTrue(
            "Colloquial plural 'شماها' must collide with 'شما'",
            QuizDistractorScorer.areSemanticallyColliding(duplicateColloquial, answer)
        )

        org.junit.Assert.assertFalse(
            "Different tense 'می‌خوابیدید' should NOT collide with 'خوابیدید'",
            QuizDistractorScorer.areSemanticallyColliding(validDistractorDifferentTense, answer)
        )

        // Second case reported by user: "ustedes enseñen"
        val answerEnsenen = "شما (جمع) درس بدهید"
        val duplicateEnsenen = "شما درس بدهید"
        val duplicateEnsenenColloquial = "شماها درس بدهید"
        val distractorDifferentTense = "شما (جمع) درس دادید"

        assertTrue(
            "'شما درس بدهید' must collide with 'شما (جمع) درس بدهید'",
            QuizDistractorScorer.areSemanticallyColliding(duplicateEnsenen, answerEnsenen)
        )

        assertTrue(
            "'شماها درس بدهید' must collide with 'شما (جمع) درس بدهید'",
            QuizDistractorScorer.areSemanticallyColliding(duplicateEnsenenColloquial, answerEnsenen)
        )

        org.junit.Assert.assertFalse(
            "Different tense 'درس دادید' should NOT collide with 'درس بدهید'",
            QuizDistractorScorer.areSemanticallyColliding(distractorDifferentTense, answerEnsenen)
        )
    }

    @Test
    fun `areSemanticallyColliding correctly flags all reported medium mode duplicate option cases`() {
        // Screenshot 1: "Media jornada"
        val answerMediaJornada = "پاره‌وقت، نیمه‌وقت"
        val duplicateSwappedOrder = "نیمه‌وقت، پاره‌وقت"
        assertTrue(
            "Permuted synonym order 'نیمه‌وقت، پاره‌وقت' must collide with 'پاره‌وقت، نیمه‌وقت'",
            QuizDistractorScorer.areSemanticallyColliding(duplicateSwappedOrder, answerMediaJornada)
        )

        // Screenshot 2: "¡Relájate!"
        val answerRelajate = "آرام باش!، ریلکس کن!"
        val duplicateFemaleVariant = "آرام باش (برای مؤنث)"
        assertTrue(
            "'آرام باش (برای مؤنث)' must collide with 'آرام باش!، ریلکس کن!'",
            QuizDistractorScorer.areSemanticallyColliding(duplicateFemaleVariant, answerRelajate)
        )

        // Screenshot 3: "Llevo unos días fatal"
        val answerLlevo = "چند روز است که حالم خیلی بد است، چند روزی است که حالم بسیار بد است"
        val duplicateSubset1 = "حالم خیلی بد است"
        val duplicateSubset2 = "حالم بسیار بد است، احساس افتضاحی دارم"
        assertTrue(
            "'حالم خیلی بد است' must collide with longer containing answer",
            QuizDistractorScorer.areSemanticallyColliding(duplicateSubset1, answerLlevo)
        )
        assertTrue(
            "'حالم بسیار بد است، احساس افتضاحی دارم' must collide with answer containing 'حالم بسیار بد است'",
            QuizDistractorScorer.areSemanticallyColliding(duplicateSubset2, answerLlevo)
        )

        // Screenshot 4: "¡Qué barbaridad!"
        val answerBarbaridad = "چه فاجعه‌ای!، چه عجیب!، عجب چیزی!"
        val duplicateBarbaridadVariant = "چه فاجعه و وحشتی!"
        assertTrue(
            "'چه فاجعه و وحشتی!' must collide with 'چه فاجعه‌ای!، چه عجیب!، عجب چیزی!'",
            QuizDistractorScorer.areSemanticallyColliding(duplicateBarbaridadVariant, answerBarbaridad)
        )
    }

    @Test
    fun benchmarkPrecomputedCollisions_100QuestionsPerformance() {
        val poolTexts = (1..500).map { "واژه تست شماره $it، ترجمه آزمایشی $it" }
        val startPrecompute = System.currentTimeMillis()
        val precomputedPool = poolTexts.map { QuizDistractorScorer.precomputeSemantic(it) }
        val precomputeTime = System.currentTimeMillis() - startPrecompute

        val questionTargets = (1..100).map { QuizDistractorScorer.precomputeSemantic("واژه تست شماره $it، ترجمه آزمایشی $it") }
        val startMatching = System.currentTimeMillis()
        var collisionsFound = 0
        for (q in questionTargets) {
            val nonColliding = precomputedPool.filter { !QuizDistractorScorer.arePrecomputedColliding(it, q) }
            collisionsFound += (500 - nonColliding.size)
        }
        val matchingTime = System.currentTimeMillis() - startMatching

        // 100 questions against 500 pool items (50,000 comparisons) should complete in well under 1000ms
        assertTrue("Precomputation time ($precomputeTime ms) must be fast", precomputeTime < 500)
        assertTrue("100 questions collision checks ($matchingTime ms) must be ultra fast", matchingTime < 500)
        assertTrue("Collisions should be found accurately", collisionsFound >= 100)
    }
}
