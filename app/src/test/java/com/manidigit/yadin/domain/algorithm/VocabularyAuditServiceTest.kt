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
}
