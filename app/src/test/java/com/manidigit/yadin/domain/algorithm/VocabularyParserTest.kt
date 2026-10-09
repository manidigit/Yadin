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

    @Test
    fun parseEmbeddedSlashInPhraseDoesNotBreakTranslation() {
        val raw = "contar con alguien - روی کسی/چیزی حساب کردن"
        val result = VocabularyParser.parse(raw)
        assertEquals(1, result.entries.size)
        val entry = result.entries.first()
        assertEquals("contar con alguien", entry.sourceText)
        assertEquals(listOf("روی کسی/چیزی حساب کردن"), entry.translations)
    }

    @Test
    fun parseSubsequentBreakdownItemsRemainAttachedToMainEntry() {
        val raw = """
            estoy seguro de que todo irá bien
            مطمئنم که همه‌چیز خوب پیش می‌رود
            estoy seguro de que: مطمئنم که
            todo: همه‌چیز
            irá bien: خوب پیش خواهد رفت
        """.trimIndent()
        val result = VocabularyParser.parse(raw)
        assertEquals(1, result.entries.size)
        val entry = result.entries.first()
        assertEquals("estoy seguro de que todo irá bien", entry.sourceText)
        assertEquals(3, entry.breakdowns.size)
        assertEquals("todo", entry.breakdowns[1].sourcePart)
        assertEquals("همه‌چیز", entry.breakdowns[1].translationPart)
    }
}
