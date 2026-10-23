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

    @Test
    fun parseCsvWithYadinExportHeadersAndRowNumbers() {
        val csv = """
            ردیف,واژه یا عبارت (اسپانیایی),ترجمه‌های فارسی,دسته‌بندی,توضیحات و یادداشت
            1,hola,سلام,عمومی,احوالپرسی
            2,gracias,"ممنون ، تشکر",عمومی,
        """.trimIndent()
        val stream = csv.byteInputStream(Charsets.UTF_8)
        val result = FileReaders.readCsv(stream)
        assertEquals(2, result.entries.size)
        assertEquals("hola", result.entries[0].sourceText)
        assertEquals(listOf("سلام"), result.entries[0].translations)
        assertEquals("gracias", result.entries[1].sourceText)
        assertTrue(result.entries[1].translations.contains("ممنون"))
    }

    @Test
    fun parseCsvWithBomAndMasterExportBanners() {
        val csv = "\uFEFF" + """
            =========================================
               خروجی جامع پایگاه داده و آمار یادین (MASTER EXPORT)   
               تاریخ استخراج: 2026-10-10 12:00:00   
            =========================================

            === بخش ۱: بانک واژگان و دسته‌بندی‌ها ===
            ردیف,واژه یا عبارت (اسپانیایی),ترجمه‌های فارسی,دسته‌بندی
            1,amigo,دوست,عمومی
        """.trimIndent()
        val stream = csv.byteInputStream(Charsets.UTF_8)
        val result = FileReaders.readCsv(stream)
        assertEquals(1, result.entries.size)
        assertEquals("amigo", result.entries[0].sourceText)
        assertEquals(listOf("دوست"), result.entries[0].translations)
    }

    @Test
    fun parseConsecutiveLinesWithoutTranslationDoNotMerge() {
        val raw = """
            casa
            perro
        """.trimIndent()
        val result = VocabularyParser.parse(raw)
        assertEquals(2, result.entries.size)
        assertEquals("casa", result.entries[0].sourceText)
        assertEquals("perro", result.entries[1].sourceText)
    }

    @Test
    fun parseCsvWithMadridDoesNotTreatMadridAsHeader() {
        val csv = "Madrid,مادرید"
        val result = FileReaders.readCsv(csv.byteInputStream(Charsets.UTF_8))
        assertEquals(1, result.entries.size)
        assertEquals("Madrid", result.entries[0].sourceText)
        assertEquals(listOf("مادرید"), result.entries[0].translations)
    }

    @Test
    fun parseCsvWithComplexStatsHeadersResolvesCorrectSourceAndTargetColumns() {
        val csv = """
            ردیف,واژه یا عبارت (اسپانیایی),ترجمه‌های فارسی,دسته‌بندی,مرحله عادی (اسپانیایی به فارسی),مرحله برعکس (فارسی به اسپانیایی),دشواری (عادی),توضیحات و یادداشت
            1,hola,سلام,عمومی,مرحله 1,مرحله 1,ساده,احوالپرسی
        """.trimIndent()
        val result = FileReaders.readCsv(csv.byteInputStream(Charsets.UTF_8))
        assertEquals(1, result.entries.size)
        val entry = result.entries.first()
        assertEquals("hola", entry.sourceText)
        assertEquals(listOf("سلام"), entry.translations)
        assertEquals("احوالپرسی", entry.note)
    }

    @Test
    fun parseCsvPreservesAccurateWarningLineNumbersWithBanner() {
        val csv = """
            === خروجی جامع ===
            ردیف,واژه یا عبارت (اسپانیایی),ترجمه‌های فارسی
            1,hola,سلام
            2,,بدون مبدأ
        """.trimIndent()
        val result = FileReaders.readCsv(csv.byteInputStream(Charsets.UTF_8))
        assertEquals(1, result.entries.size)
        assertEquals(1, result.warnings.size)
        // Row 4 in file has the invalid entry
        assertEquals(4, result.warnings.first().lineNumber)
    }

    @Test
    fun parseBulkGymVocabularyListPreservesSlashesParenthesesAndSecondTranslations() {
        val raw = """
            El gimnasio
            باشگاه ورزشی
            سالن بدنسازی
            El chancla (Las chanclas)
            دمپایی (دمپایی‌های لاانگشتی)
            La zona de peso libre
            بخش وزنه‌های آزاد (دمبل و هالتر)
            El monitor / La monitora de sala
            مربی سالن
            مربی شیفت باشگاه
            El entrenador personal (Personal trainer)
            مربی خصوصی
            El compañero / La compañera de entrenamiento (El gymbro)
            یار تمرینی
            حریف تمرینی
            La barra Z (Barra EZ)
            هالتر خمیده
            هالتر لاری
            El entrenamiento de alta intensidad (HIIT)
            تمرین تناوبی با شدت بالا (HIIT)
            ¡Una más!
            یکی دیگه! (تشویق برای تکرار آخر)
        """.trimIndent()

        val result = VocabularyParser.parse(raw)
        assertEquals(0, result.warnings.size)
        assertEquals(9, result.entries.size)

        // 1. El gimnasio -> باشگاه ورزشی, سالن بدنسازی (100% confidence, no warning)
        val e0 = result.entries[0]
        assertEquals("El gimnasio", e0.sourceText)
        assertEquals(listOf("باشگاه ورزشی", "سالن بدنسازی"), e0.translations)
        assertEquals(null, e0.note)
        assertEquals(1.0, e0.confidence, 0.001)

        // 2. El chancla (Las chanclas) -> دمپایی (دمپایی‌های لاانگشتی) (parentheses preserved in both word and translation!)
        val e1 = result.entries[1]
        assertEquals("El chancla (Las chanclas)", e1.sourceText)
        assertEquals(listOf("دمپایی (دمپایی‌های لاانگشتی)"), e1.translations)
        assertEquals(null, e1.note)
        assertEquals(1.0, e1.confidence, 0.001)

        // 3. La zona de peso libre -> بخش وزنه‌های آزاد (دمبل و هالتر)
        val e2 = result.entries[2]
        assertEquals("La zona de peso libre", e2.sourceText)
        assertEquals(listOf("بخش وزنه‌های آزاد (دمبل و هالتر)"), e2.translations)
        assertEquals(null, e2.note)

        // 4. El monitor / La monitora de sala -> slash NOT split into translation/note!
        val e3 = result.entries[3]
        assertEquals("El monitor / La monitora de sala", e3.sourceText)
        assertEquals(listOf("مربی سالن", "مربی شیفت باشگاه"), e3.translations)
        assertEquals(null, e3.note)
        assertEquals(1.0, e3.confidence, 0.001)

        // 5. El entrenador personal (Personal trainer) -> English in parens NOT moved to note!
        val e4 = result.entries[4]
        assertEquals("El entrenador personal (Personal trainer)", e4.sourceText)
        assertEquals(listOf("مربی خصوصی"), e4.translations)
        assertEquals(null, e4.note)

        // 6. El compañero / La compañera de entrenamiento (El gymbro) -> both slash and parens preserved!
        val e5 = result.entries[5]
        assertEquals("El compañero / La compañera de entrenamiento (El gymbro)", e5.sourceText)
        assertEquals(listOf("یار تمرینی", "حریف تمرینی"), e5.translations)
        assertEquals(null, e5.note)

        // 7. El entrenamiento de alta intensidad (HIIT) -> HIIT in Persian translation does not trigger Latin warning!
        val e7 = result.entries[7]
        assertEquals("El entrenamiento de alta intensidad (HIIT)", e7.sourceText)
        assertEquals(listOf("تمرین تناوبی با شدت بالا (HIIT)"), e7.translations)
        assertEquals(1.0, e7.confidence, 0.001)
    }

    @Test
    fun parseDrivingLicenseBatchWithLongLatinParentheticalsAndStrippedZwnj() {
        val raw = """
            Informatizado
            رایانهای
            کامپیوتری
            سیستمیشده
            Ficha técnica
            برگه مشخصات فنی خودرو (کارت معاینه فنی ITV)
            ITV
            مرکز معاینه فنی خودرو در اسپانیا (Inspección Técnica de Vehículos)
            Matriculación
            پلاکگذاری و شمارهگذاری خودرو
            Distintivo medioambiental
            برچسب زیستمحیطی خودرو
            Favorable
            نتیجه قبولی و مثبت (معاینه فنی تاییدشده)
            Fichero de Vehículos Asegurados
            سامانه و بانک اطلاعاتی خودروهای دارای بیمهنامه (FIVA)
            Atestado
            کروکی پلیس
            صورتجلسه رسمی تصادف
            Seguro a todo riesgo
            بیمه بدنه جامع (بیمه تمامخطر)
            Franquicia
            فرانشیز بیمه (سهم پرداخت خسارت بر عهده بیمهگذار)
            Calzada
            کفی سوارهرو (بخشی از جاده ویژه حرکت وسایل نقلیه)
        """.trimIndent()

        val result = VocabularyParser.parse(raw)
        assertEquals("Expected zero warnings for ITV and parenthetical translations", 0, result.warnings.size)
        assertEquals(11, result.entries.size)

        // Verify Informatizado ZWNJ restoration on dual-joining letters
        assertEquals(listOf("رایانه‌ای", "کامپیوتری", "سیستمی‌شده"), result.entries[0].translations)

        // Verify ITV entry is recognized with its Persian translation containing long Spanish parenthetical
        val itvEntry = result.entries[2]
        assertEquals("ITV", itvEntry.sourceText)
        assertEquals(
            listOf("مرکز معاینه فنی خودرو در اسپانیا (Inspección Técnica de Vehículos)"),
            itvEntry.translations
        )
        assertEquals(1.0, itvEntry.confidence, 0.001)

        // Verify ZWNJ restoration on copy-pasted Persian translations
        assertEquals(listOf("پلاک‌گذاری و شماره‌گذاری خودرو"), result.entries[3].translations)
        assertEquals(listOf("برچسب زیست‌محیطی خودرو"), result.entries[4].translations)
        assertEquals(listOf("نتیجه قبولی و مثبت (معاینه فنی تاییدشده)"), result.entries[5].translations)
        assertEquals(listOf("سامانه و بانک اطلاعاتی خودروهای دارای بیمه‌نامه (FIVA)"), result.entries[6].translations)
        assertEquals(listOf("کروکی پلیس", "صورت‌جلسه رسمی تصادف"), result.entries[7].translations)
        assertEquals(listOf("بیمه بدنه جامع (بیمه تمام‌خطر)"), result.entries[8].translations)
        assertEquals(listOf("فرانشیز بیمه (سهم پرداخت خسارت بر عهده بیمه‌گذار)"), result.entries[9].translations)
        assertEquals(listOf("کفی سواره‌رو (بخشی از جاده ویژه حرکت وسایل نقلیه)"), result.entries[10].translations)
    }
}


