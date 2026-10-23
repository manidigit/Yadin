package com.manidigit.yadin.domain.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextUtilitiesTest {

    @Test
    fun cleanText_normalizesArabicYehAndKafToPersian() {
        val raw = "كتاب ياسمين" // Contains Arabic Kaf (ك: \u0643) and Arabic Yeh (ي: \u064A)
        val cleaned = TextUtilities.cleanText(raw)
        assertEquals("کتاب یاسمین", cleaned) // Should become Persian Kaf (ک: \u06A9) and Yeh (ی: \u06CC)
    }

    @Test
    fun cleanText_trimsSurroundingZwnjAndCollapsesMultipleZwnj() {
        val raw = "\u200Cکتاب\u200C\u200Cها\u200C"
        val cleaned = TextUtilities.cleanText(raw)
        assertEquals("کتاب\u200Cها", cleaned)
    }

    @Test
    fun cleanText_removesZeroWidthSpace() {
        val raw = "سلام\u200Bجهان"
        val cleaned = TextUtilities.cleanText(raw)
        assertEquals("سلامجهان", cleaned)
    }

    @Test
    fun canonicalKey_preservesSpanishAccentsAndNormalizesPersian() {
        assertEquals("adiós", TextUtilities.canonicalKey("Adiós"))
        assertEquals("niño", TextUtilities.canonicalKey("Niño"))
        assertEquals("کتاب", TextUtilities.canonicalKey("كتاب")) // Arabic kaf normalized
    }

    @Test
    fun searchKey_stripsLatinDiacriticsAndSpanishAccents() {
        assertEquals("adios", TextUtilities.searchKey("Adiós"))
        assertEquals("nino", TextUtilities.searchKey("Niño"))
        assertEquals("esta", TextUtilities.searchKey("está"))
        assertEquals("arbol", TextUtilities.searchKey("árbol"))
    }

    @Test
    fun searchKey_normalizesPersianAlefVariantsAndRemovesZwnjAndHarakat() {
        assertEquals("اب", TextUtilities.searchKey("آب")) // Alef with madda
        assertEquals("امروز", TextUtilities.searchKey("اِمروز")) // Kasrah diacritic
        assertEquals("کتابها", TextUtilities.searchKey("کتاب\u200Cها")) // ZWNJ stripped for search
        assertEquals("کتاب", TextUtilities.searchKey("كِتاب")) // Arabic kaf + kasrah
    }

    @Test
    fun splitTranslations_doesNotSplitInsideParentheses() {
        val raw = "بیمار (مذکر، مؤنث)، مراجعه\u200Cکننده (پزشکی، روان\u200Cشناسی)"
        val split = TextUtilities.splitTranslations(raw)
        assertEquals(listOf("بیمار", "مراجعه\u200Cکننده (پزشکی، روان\u200Cشناسی)"), split)
    }

    @Test
    fun deduplicatePersianTranslations_removesBrokenParensGenderAndNormalizedDuplicates() {
        val brokenAndGendered = listOf("بیمار", "بیمار (مذکر", "مؤنث)")
        assertEquals(listOf("بیمار"), TextUtilities.deduplicatePersianTranslations(brokenAndGendered))

        val genderPair = listOf("معلم مرد", "معلم زن")
        assertEquals(listOf("معلم"), TextUtilities.deduplicatePersianTranslations(genderPair))

        val combinedAndSplit = listOf("سرعت، شتاب، تندی", "سرعت", "شتاب", "تندی")
        assertEquals(listOf("سرعت", "شتاب", "تندی"), TextUtilities.deduplicatePersianTranslations(combinedAndSplit))

        val zwnjAndEzafe = listOf("جلوه ی ویژه", "جلوه\u200Cی ویژه", "چراغ قوه", "چراغ\u200Cقوه")
        assertEquals(listOf("جلوه\u200Cی ویژه", "چراغ\u200Cقوه"), TextUtilities.deduplicatePersianTranslations(zwnjAndEzafe))

        val redundantParen = listOf("حق تقدم", "حق تقدم (در رانندگی)")
        assertEquals(listOf("حق تقدم"), TextUtilities.deduplicatePersianTranslations(redundantParen))
    }

    @Test
    fun spanishDedupeKey_ignoresArticlesAndPunctuationWithoutStrippingFromOriginalWord() {
        val withArticle = TextUtilities.analyzeSpanishHeadword("El trampolín")
        val withoutArticle = TextUtilities.analyzeSpanishHeadword("Trampolín")

        assertEquals("El trampolín", withArticle.originalText)
        assertEquals("El", withArticle.articlePrefix)
        assertEquals(TextUtilities.SpanishArticleGender.MASCULINE, withArticle.gender)
        assertEquals("trampolín", withArticle.dedupeKey)
        assertEquals("trampolín", withoutArticle.dedupeKey)
        assertEquals(withArticle.dedupeKey, withoutArticle.dedupeKey)

        // Fixed expressions with un/una or relative pronouns should not strip their first word
        assertEquals("una vez", TextUtilities.spanishDedupeKey("Una vez"))
        assertEquals("un poco", TextUtilities.spanishDedupeKey("Un poco"))
        assertEquals("el que", TextUtilities.spanishDedupeKey("El que"))
    }

    @Test
    fun selectPreferredSpanishHeadword_retainsArticleFormForGenderLearning() {
        assertEquals(
            "El trampolín",
            TextUtilities.selectPreferredSpanishHeadword(listOf("Trampolín", "El trampolín"))
        )
        assertEquals(
            "La biblioteca",
            TextUtilities.selectPreferredSpanishHeadword(listOf("biblioteca", "La biblioteca"))
        )
        // Combines article from one entry with proper accent from the other entry
        assertEquals(
            "El trampolín",
            TextUtilities.selectPreferredSpanishHeadword(listOf("El trampolin", "Trampolín"))
        )
    }

    @Test
    fun analyzeSpanishHeadword_detectsSlashDualFormsAndRetainsThemOnMerge() {
        val dualAdj = TextUtilities.analyzeSpanishHeadword("Claro / clara")
        assertTrue(dualAdj.isSlashDualForm)
        assertEquals(listOf("claro", "clara"), dualAdj.slashVariantKeys)

        val dualNoun = TextUtilities.analyzeSpanishHeadword("el sobrino / la sobrina")
        assertTrue(dualNoun.isSlashDualForm)
        assertEquals(listOf("sobrino", "sobrina"), dualNoun.slashVariantKeys)

        assertEquals(
            "Claro / clara",
            TextUtilities.selectPreferredSpanishHeadword(listOf("claro", "Claro / clara"))
        )
    }

    @Test
    fun normalizePersianTranslation_fixesHalfSpacesTrailingPeriodsAndBilingualPunctuation() {
        assertEquals(
            "می\u200Cشود",
            TextUtilities.normalizePersianTranslation("میشود").first
        )
        assertEquals(
            "کتاب\u200Cها",
            TextUtilities.normalizePersianTranslation("کتاب ها").first
        )
        assertEquals(
            "قبوله",
            TextUtilities.normalizePersianTranslation("قبوله.", "Vale.").first
        )
        assertEquals(
            "حالت چطوره؟",
            TextUtilities.normalizePersianTranslation("حالت چطوره", "¿Cómo estás?").first
        )
        assertEquals(
            "آفرین!",
            TextUtilities.normalizePersianTranslation("آفرین.", "¡Muy bien!").first
        )
    }

    @Test
    fun areSpanishNearSpellingVariants_detectsCloseInflectionsAndRejectsShortOrDifferentWords() {
        assertTrue(TextUtilities.areSpanishNearSpellingVariants("claro", "claros"))
        assertTrue(TextUtilities.areSpanishNearSpellingVariants("tranquilo", "tranquila"))
        assertTrue(TextUtilities.areSpanishNearSpellingVariants("tienes", "tienen"))
        assertFalse(TextUtilities.areSpanishNearSpellingVariants("casa", "cama"))
        assertFalse(TextUtilities.areSpanishNearSpellingVariants("sol", "sal")) // Too short (< 4 chars)
    }
}


