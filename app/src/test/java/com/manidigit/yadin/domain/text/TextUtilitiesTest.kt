package com.manidigit.yadin.domain.text

import org.junit.Assert.assertEquals
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
}
