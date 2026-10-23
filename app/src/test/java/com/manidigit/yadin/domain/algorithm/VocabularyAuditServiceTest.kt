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

    @Test
    fun testCleanSpanishExclamationAndQuestionMismatches() {
        // 1. Leading regular '!' instead of inverted '¡'
        val r1 = VocabularyAuditService.cleanAndExtractWord("!No respira!")
        assertEquals("¡No respira!", r1.cleanSourceText)
        assertTrue(r1.wasModified)

        // 2. Missing opening '¡' when ending with '!'
        val r2 = VocabularyAuditService.cleanAndExtractWord("Hasta mañana!")
        assertEquals("¡Hasta mañana!", r2.cleanSourceText)
        assertTrue(r2.wasModified)

        // 3. Mismatched opening '¿' with closing '!'
        val r3 = VocabularyAuditService.cleanAndExtractWord("¿qué nervios!")
        assertEquals("¡qué nervios!", r3.cleanSourceText)
        assertTrue(r3.wasModified)

        // 4. Missing opening '¿' when ending with '?'
        val r4 = VocabularyAuditService.cleanAndExtractWord("De dónde eres?")
        assertEquals("¿De dónde eres?", r4.cleanSourceText)
        assertTrue(r4.wasModified)

        // 5. Duplicate opening '¿' before comma clause
        val r5 = VocabularyAuditService.cleanAndExtractWord("¿Por favor, ¿puedes recoger las mancuernas al terminar?")
        assertEquals("Por favor, ¿puedes recoger las mancuernas al terminar?", r5.cleanSourceText)
        assertTrue(r5.wasModified)

        // 6. Valid "¡Hola!" remains untouched
        val r6 = VocabularyAuditService.cleanAndExtractWord("¡Hola!")
        assertEquals("¡Hola!", r6.cleanSourceText)
        assertFalse(r6.wasModified)
    }

    @Test
    fun testFieldValidationRules() {
        // ۱. واژه سالم اسپانیایی و ترجمه معتبر فارسی -> KEEP, confidence 1.0
        val res1 = FieldValidationEngine.validateRecord("Presión de los neumáticos", listOf("فشار باد لاستیک‌ها"))
        assertEquals(com.manidigit.yadin.domain.model.ReviewAction.KEEP, res1.action)
        assertEquals(1.0, res1.confidence, 0.001)

        // ۲. واژه چندخطی شامل مفاهیم مستقل لاتین -> MANUAL_REVIEW, confidence 0.60
        val res2 = FieldValidationEngine.validateRecord("Dispositivo V-16 چراغ هشدار\nPresión de los neumáticos", listOf("چراغ هشدار"))
        assertEquals(com.manidigit.yadin.domain.model.ReviewAction.MANUAL_REVIEW, res2.action)
        assertEquals(0.60, res2.confidence, 0.001)

        // ۳. اختلاط اسپانیایی و فارسی بدون جداکننده مشخص -> MANUAL_REVIEW, confidence 0.70
        val res3 = FieldValidationEngine.validateRecord("Dispositivo V-16 چراغ هشدار V-16", listOf("چراغ هشدار"))
        assertEquals(com.manidigit.yadin.domain.model.ReviewAction.MANUAL_REVIEW, res3.action)
        assertEquals(0.70, res3.confidence, 0.001)

        // ۴. پرانتز توضیحی یا نقش کلمه -> MOVE_TO_NOTE, confidence 0.98
        val res4 = FieldValidationEngine.validateRecord("correr (v.)", listOf("دویدن"))
        assertEquals(com.manidigit.yadin.domain.model.ReviewAction.MOVE_TO_NOTE, res4.action)
        assertEquals(0.98, res4.confidence, 0.001)
        assertEquals("correr", res4.suggestedWord)
        assertTrue(res4.suggestedNote?.contains("v.") == true)

        // ۵. علامت مخدوش پرسشی اسپانیایی -> SUGGEST_CORRECTION, confidence 0.98
        val res5 = FieldValidationEngine.validateRecord("¿Cuánto es la carrera;¿", listOf("کرایه چقدر است؟"))
        assertEquals(com.manidigit.yadin.domain.model.ReviewAction.SUGGEST_CORRECTION, res5.action)
        assertEquals(0.98, res5.confidence, 0.001)
        assertEquals("¿Cuánto es la carrera?", res5.suggestedWord)

        // ۶. فیلد واژه فقط به فارسی -> MANUAL_REVIEW, confidence 0.40
        val res6 = FieldValidationEngine.validateRecord("کتاب", listOf("libro"))
        assertEquals(com.manidigit.yadin.domain.model.ReviewAction.MANUAL_REVIEW, res6.action)
        assertEquals(0.40, res6.confidence, 0.001)
    }

    @Test
    fun testSeedJsonSpanishPunctuationIsNormalized() {
        val candidateFiles = listOf(
            java.io.File("src/main/assets/seed/vocabulary_seed.json"),
            java.io.File("app/src/main/assets/seed/vocabulary_seed.json")
        ).filter { it.exists() && it.isFile }

        val esEntryRegex = Regex(""""languageCode":\s*"es",\s*"text":\s*"([^"\\]*)",\s*"canonicalKey":\s*"([^"\\]*)"""")

        for (file in candidateFiles) {
            val verifyRaw = file.readText(Charsets.UTF_8)
            val remainingUnnormalized = esEntryRegex.findAll(verifyRaw).count { m ->
                val txt = m.groupValues[1]
                com.manidigit.yadin.domain.text.TextUtilities.normalizeSpanishPunctuation(txt).first != txt
            }
            assertEquals("Expected 0 unnormalized Spanish punctuation entries in ${file.path}", 0, remainingUnnormalized)
        }
    }
}
