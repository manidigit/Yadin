package com.manidigit.yadin.domain.algorithm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VocabularyParserTest {

    @Test
    fun parseSimpleWordAndTranslation() {
        val raw = "hablar - صحبت کردن"
        val result = VocabularyParser.parse(raw)
        assertEquals(1, result.entries.size)
        val entry = result.entries.first()
        assertEquals("hablar", entry.sourceText)
        assertEquals(listOf("صحبت کردن"), entry.translations)
    }

    @Test
    fun parseMultipleTranslationsWithSlash() {
        val raw = "casa - خانه / منزل"
        val result = VocabularyParser.parse(raw)
        assertEquals(1, result.entries.size)
        val entry = result.entries.first()
        assertEquals("casa", entry.sourceText)
        assertEquals(listOf("خانه", "منزل"), entry.translations)
    }

    @Test
    fun parseWithGrammarNoteAndParenthetical() {
        val raw = """
            libro - کتاب (اسم مذکر)
            نکته: برای مطالعه استفاده می‌شود
        """.trimIndent()
        val result = VocabularyParser.parse(raw)
        assertEquals(1, result.entries.size)
        val entry = result.entries.first()
        assertEquals("libro", entry.sourceText)
        assertTrue(entry.translations.any { it.contains("کتاب") })
    }
}
