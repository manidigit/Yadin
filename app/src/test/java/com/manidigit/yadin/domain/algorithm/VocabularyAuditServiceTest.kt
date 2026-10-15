package com.manidigit.yadin.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyAuditServiceTest {

    @Test
    fun testStripNumberingAndBullets() {
        assertEquals("hablar", VocabularyAuditService.stripNumberingAndBullets("1. hablar"))
        assertEquals("comer", VocabularyAuditService.stripNumberingAndBullets("12) comer"))
        assertEquals("vivir", VocabularyAuditService.stripNumberingAndBullets("• vivir"))
        assertEquals("dormir", VocabularyAuditService.stripNumberingAndBullets("- dormir"))
        assertEquals("despertar", VocabularyAuditService.stripNumberingAndBullets("➜ despertar"))
    }

    @Test
    fun testStripSurroundingQuotes() {
        assertEquals("casa", VocabularyAuditService.stripSurroundingQuotes("\"casa\""))
        assertEquals("coche", VocabularyAuditService.stripSurroundingQuotes("«coche»"))
        assertEquals("perro", VocabularyAuditService.stripSurroundingQuotes("“perro”"))
        assertEquals("gato", VocabularyAuditService.stripSurroundingQuotes("'gato'"))
    }

    @Test
    fun testCleanWordWithParentheticalNote() {
        val result = VocabularyAuditService.cleanAndExtractWord("apple (n.)")
        assertEquals("apple", result.cleanSourceText)
        assertTrue(result.wasModified)
        assertTrue(result.extractedNotes.any { it.contains("n.") })
    }

    @Test
    fun testCleanWordWithPersianTranslationInParentheses() {
        val result = VocabularyAuditService.cleanAndExtractWord("libro (کتاب)")
        assertEquals("libro", result.cleanSourceText)
        assertTrue(result.wasModified)
        assertTrue(result.extractedTranslations.contains("کتاب"))
    }

    @Test
    fun testCleanWordWithPairDelimiter() {
        val result = VocabularyAuditService.cleanAndExtractWord("casa : خانه")
        assertEquals("casa", result.cleanSourceText)
        assertTrue(result.wasModified)
        assertTrue(result.extractedTranslations.contains("خانه"))
    }

    @Test
    fun testCleanWordWithNumberingAndQuotes() {
        val result = VocabularyAuditService.cleanAndExtractWord("1. \"gracias\"")
        assertEquals("gracias", result.cleanSourceText)
        assertTrue(result.wasModified)
    }

    @Test
    fun testCleanWordAlreadyClean() {
        val result = VocabularyAuditService.cleanAndExtractWord("estudiar")
        assertEquals("estudiar", result.cleanSourceText)
        assertFalse(result.wasModified)
        assertTrue(result.extractedTranslations.isEmpty())
        assertTrue(result.extractedNotes.isEmpty())
    }

    @Test
    fun testCleanSpanishCorruptedQuestionMarks() {
        // Exact user case: "¿Cuánto es la carrera;¿" -> "¿Cuánto es la carrera?"
        val result1 = VocabularyAuditService.cleanAndExtractWord("¿Cuánto es la carrera;¿")
        assertEquals("¿Cuánto es la carrera?", result1.cleanSourceText)
        assertTrue(result1.wasModified)

        // "¿Cuánto cuesta;¿" -> "¿Cuánto cuesta?"
        val result2 = VocabularyAuditService.cleanAndExtractWord("¿Cuánto cuesta;¿")
        assertEquals("¿Cuánto cuesta?", result2.cleanSourceText)
        assertTrue(result2.wasModified)

        // "¿Cuánto cuesta¿" -> "¿Cuánto cuesta?"
        val result3 = VocabularyAuditService.cleanAndExtractWord("¿Cuánto cuesta¿")
        assertEquals("¿Cuánto cuesta?", result3.cleanSourceText)
        assertTrue(result3.wasModified)

        // "Cuánto cuesta;¿" -> "¿Cuánto cuesta?"
        val result4 = VocabularyAuditService.cleanAndExtractWord("Cuánto cuesta;¿")
        assertEquals("¿Cuánto cuesta?", result4.cleanSourceText)
        assertTrue(result4.wasModified)

        // Valid Spanish question: untouched!
        val resultValid = VocabularyAuditService.cleanAndExtractWord("¿Cuánto cuesta?")
        assertEquals("¿Cuánto cuesta?", resultValid.cleanSourceText)
        assertFalse(resultValid.wasModified)

        // Trailing stray semicolon
        val resultTrailing = VocabularyAuditService.cleanAndExtractWord("palabra;")
        assertEquals("palabra", resultTrailing.cleanSourceText)
        assertTrue(resultTrailing.wasModified)
    }
}
