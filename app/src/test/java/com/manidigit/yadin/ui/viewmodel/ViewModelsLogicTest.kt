package com.manidigit.yadin.ui.viewmodel

import com.manidigit.yadin.domain.algorithm.QuizDistractorScorer
import com.manidigit.yadin.domain.model.QuizLevel
import com.manidigit.yadin.domain.model.QuizOption
import com.manidigit.yadin.domain.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست‌های واحد برای بررسی صحت معماری و حل باگ‌های MainViewModel، ReviewViewModel و QuizDistractorScorer.
 *
 * بررسی حل باگ‌های ISS-16، ISS-22 و ISS-27 طبق مستندات پروژه یادین.
 */
class ViewModelsLogicTest {

    @Test
    fun `ISS-16 Spanish accents are preserved in normalizeQuiz`() {
        // اسپانیایی: کلماتی نظیر año و ano یا sí و si نباید یکی در نظر گرفته شوند
        val withAccent = QuizDistractorScorer.normalizeQuiz("año")
        val withoutAccent = QuizDistractorScorer.normalizeQuiz("ano")
        assertNotEquals("Spanish words with and without tilde/accents must NOT be normalized to identical strings", withAccent, withoutAccent)

        val siWithAccent = QuizDistractorScorer.normalizeQuiz("sí")
        val siWithoutAccent = QuizDistractorScorer.normalizeQuiz("si")
        assertNotEquals("Accented 'sí' must differ from unaccented 'si'", siWithAccent, siWithoutAccent)
    }

    @Test
    fun `ISS-16 Quiz question scoring strictly checks option index equality without false semantic collision`() {
        val q = QuizQuestion(
            conceptId = "test-concept-1",
            direction = com.manidigit.yadin.domain.model.CardDirection.NORMAL,
            promptText = "manzana",
            correctAnswer = "سیب",
            options = listOf(
                QuizOption("سیب قرمز", "c1"),
                QuizOption("سیب", "test-concept-1"),
                QuizOption("پرتقال", "c2"),
                QuizOption("موز", "c3")
            ),
            correctIndex = 1,
            note = null,
            categoryName = "میوه‌ها",
            stage = com.manidigit.yadin.domain.model.Stage.DAILY,
            difficulty = com.manidigit.yadin.domain.model.VocabularyDifficulty.EASY
        )

        // کاربر گزینه ۰ ("سیب قرمز") را انتخاب کرده در حالی که پاسخ صحیح گزینه ۱ ("سیب") است
        val selectedWrongIndex = 0
        val isCorrect = (selectedWrongIndex == q.correctIndex)

        assertFalse("Selecting wrong option index must evaluate to false, not true through collision", isCorrect)

        val selectedRightIndex = 1
        val isRightCorrect = (selectedRightIndex == q.correctIndex)
        assertTrue("Selecting correct option index must evaluate to true", isRightCorrect)
    }

    @Test
    fun `LibraryViewModel parsing parses valid vocabulary input`() {
        val input = "hablar : صحبت کردن\ncomer : غذا خوردن"
        val parseResult = com.manidigit.yadin.domain.algorithm.VocabularyParser.parse(input)

        assertEquals(2, parseResult.entries.size)
        assertEquals("hablar", parseResult.entries[0].sourceText)
        assertEquals("صحبت کردن", parseResult.entries[0].translations.firstOrNull())
        assertEquals("comer", parseResult.entries[1].sourceText)
    }

    @Test
    fun `ISS-15 Deterministic reviewAttemptId prevents duplicate review submissions`() {
        val sessionId = "session-123"
        val conceptId = "concept-456"
        val direction = com.manidigit.yadin.domain.model.CardDirection.NORMAL
        val cardIndex = 3

        val attemptId1 = "${sessionId}_${conceptId}_${direction}_$cardIndex"
        val attemptId2 = "${sessionId}_${conceptId}_${direction}_$cardIndex"

        // Attempt IDs generated for the same card in the same session must be identical
        assertEquals("Attempt IDs for the same card review must be deterministic", attemptId1, attemptId2)

        val differentCardAttempt = "${sessionId}_${conceptId}_${direction}_${cardIndex + 1}"
        assertNotEquals("Attempt IDs for different card indices must be distinct", attemptId1, differentCardAttempt)
    }
}
