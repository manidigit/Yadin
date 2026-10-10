package com.manidigit.yadin.domain.algorithm

import android.util.Xml
import com.manidigit.yadin.domain.model.EntryType
import com.manidigit.yadin.domain.model.ParsedEntry
import com.manidigit.yadin.domain.model.ParseResult
import com.manidigit.yadin.domain.model.ParseWarning
import com.manidigit.yadin.domain.model.ParseWarningType
import com.manidigit.yadin.domain.text.TextUtilities
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.zip.ZipInputStream

object FileReaders {

    fun readText(inputStream: InputStream): ParseResult {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return VocabularyParser.parse(text)
    }

    fun readCsv(inputStream: InputStream): ParseResult {
        val raw = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val text = raw.removePrefix("\uFEFF").trim()
        if (text.isBlank()) return ParseResult(emptyList(), emptyList())

        // Delimiter auto-detection ignoring quotes
        val delimiter = detectDelimiter(text)
        val allRecords = parseCsvRecords(text, delimiter)
        if (allRecords.isEmpty()) return ParseResult(emptyList(), emptyList())

        // Skip banner / master export title lines that are not table rows
        val records = allRecords.filter { tokens ->
            if (tokens.isEmpty() || tokens.all { it.isBlank() }) return@filter false
            val first = tokens[0].trim()
            !(first.startsWith("===") || first.startsWith("---") ||
              first.contains("خروجی جامع") || first.contains("تاریخ استخراج"))
        }
        if (records.isEmpty()) return ParseResult(emptyList(), emptyList())

        val entries = mutableListOf<ParsedEntry>()
        val warnings = mutableListOf<ParseWarning>()

        var sourceCol = 0
        var transCol = 1
        var noteCol = 2
        var categoryCol = 3
        var typeCol = 4
        var startIndex = 0

        val headerIndex = records.indexOfFirst { isHeaderRow(it) }
        if (headerIndex >= 0) {
            startIndex = headerIndex + 1
            val headerRow = records[headerIndex]
            var explicitSourceFound = false
            var explicitTransFound = false

            for ((idx, colName) in headerRow.withIndex()) {
                val clean = colName.trim().lowercase()
                when {
                    clean.contains("ردیف") || clean.contains("row") || clean == "id" -> {
                        // Row index column, explicitly ignore for words/translations
                    }
                    clean.contains("واژه") || clean.contains("کلمه") || clean.contains("اسپانیایی") ||
                    clean.contains("spanish") || clean.contains("word") || clean.contains("source") -> {
                        sourceCol = idx
                        explicitSourceFound = true
                    }
                    clean.contains("ترجمه") || clean.contains("معنی") || clean.contains("فارسی") ||
                    clean.contains("translation") || clean.contains("meaning") -> {
                        transCol = idx
                        explicitTransFound = true
                    }
                    clean.contains("یادداشت") || clean.contains("توضیح") || clean.contains("نکته") || clean.contains("note") -> {
                        noteCol = idx
                    }
                    clean.contains("دسته‌بندی") || clean.contains("دسته") || clean.contains("category") -> {
                        categoryCol = idx
                    }
                    clean.contains("نوع") || clean.contains("type") -> {
                        typeCol = idx
                    }
                }
            }

            // Fallback if header had "ردیف" at column 0 and source/translation weren't explicitly named
            if (!explicitSourceFound && headerRow.size > 1 && (headerRow[0].contains("ردیف") || headerRow[0].equals("id", ignoreCase = true) || headerRow[0].contains("row") || headerRow[0].contains("شناسه"))) {
                sourceCol = 1
                if (!explicitTransFound && headerRow.size > 2) {
                    transCol = 2
                }
            }
        } else {
            // If no explicit header row found, but first record column 0 is a row number (e.g., 1, 2, 3...), offset column indices
            val firstRecord = records.firstOrNull()
            if (firstRecord != null && firstRecord.size >= 2) {
                val firstColVal = firstRecord[0].trim()
                if (firstColVal.toIntOrNull() != null) {
                    sourceCol = 1
                    transCol = 2
                    if (firstRecord.size >= 3) noteCol = 3
                    if (firstRecord.size >= 4) categoryCol = 4
                    if (firstRecord.size >= 5) typeCol = 5
                }
            }
        }

        for (i in startIndex until records.size) {
            val tokens = records[i]
            if (tokens.all { it.isBlank() }) continue
            // Skip section headers in multi-section master files
            if (tokens.size == 1 && (tokens[0].startsWith("===") || tokens[0].contains("بخش"))) continue

            val source = tokens.getOrNull(sourceCol)?.let { TextUtilities.cleanText(it) } ?: ""
            val transText = tokens.getOrNull(transCol) ?: ""
            val translations = TextUtilities.splitTranslations(transText)
            val note = tokens.getOrNull(noteCol)?.let { TextUtilities.cleanText(it) }?.ifBlank { null }
            val categories = tokens.getOrNull(categoryCol)?.split("|")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
            val rawType = tokens.getOrNull(typeCol)?.trim()?.uppercase()
            val entryType = try {
                if (!rawType.isNullOrBlank()) EntryType.valueOf(rawType) else VocabularyParser.classifyEntry(source)
            } catch (e: Exception) {
                VocabularyParser.classifyEntry(source)
            }

            if (source.isBlank() || translations.isEmpty()) {
                warnings.add(
                    ParseWarning(
                        type = ParseWarningType.UNKNOWN_FORMAT,
                        lineNumber = i + 1,
                        rawText = tokens.joinToString(","),
                        message = "ردیف فاقد مبدأ یا ترجمه معتبر است"
                    )
                )
            } else {
                entries.add(
                    ParsedEntry(
                        sourceText = source,
                        translations = translations,
                        note = note,
                        categoryNames = categories,
                        entryType = entryType,
                        confidence = 1.0,
                        lineNumber = i + 1,
                        rawLines = listOf(tokens.joinToString(","))
                    )
                )
            }
        }

        return ParseResult(entries, warnings)
    }

    private fun detectDelimiter(text: String): Char {
        var commaCount = 0
        var semicolonCount = 0
        var tabCount = 0
        var inQuotes = false
        for (i in text.indices) {
            val c = text[i]
            if (c == '"') {
                inQuotes = !inQuotes
            } else if (!inQuotes) {
                when (c) {
                    ',' -> commaCount++
                    ';' -> semicolonCount++
                    '\t' -> tabCount++
                    '\n', '\r' -> break
                }
            }
        }
        return when {
            tabCount > commaCount && tabCount > semicolonCount -> '\t'
            semicolonCount > commaCount -> ';'
            else -> ','
        }
    }

    private fun isHeaderRow(tokens: List<String>): Boolean {
        val keywords = setOf("source", "word", "spanish", "translation", "meaning", "ترجمه", "واژه", "کلمه", "معنی", "اسپانیایی", "فارسی", "ردیف", "row", "id", "شناسه")
        return tokens.any { token ->
            val clean = token.trim().lowercase()
            keywords.any { clean.contains(it) }
        }
    }

    private fun parseCsvRecords(text: String, delimiter: Char): List<List<String>> {
        val records = mutableListOf<List<String>>()
        var currentRecord = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < text.length) {
            val c = text[i]
            if (c == '"') {
                if (inQuotes && i + 1 < text.length && text[i + 1] == '"') {
                    currentField.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                currentRecord.add(currentField.toString().trim())
                currentField.clear()
            } else if ((c == '\n' || c == '\r') && !inQuotes) {
                if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') {
                    i++
                }
                currentRecord.add(currentField.toString().trim())
                currentField.clear()
                if (currentRecord.any { it.isNotBlank() }) {
                    records.add(currentRecord)
                }
                currentRecord = mutableListOf()
            } else {
                currentField.append(c)
            }
            i++
        }
        if (currentField.isNotEmpty() || currentRecord.isNotEmpty()) {
            currentRecord.add(currentField.toString().trim())
            if (currentRecord.any { it.isNotBlank() }) {
                records.add(currentRecord)
            }
        }
        return records
    }

    fun readJson(inputStream: InputStream): ParseResult {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val entries = mutableListOf<ParsedEntry>()
        val warnings = mutableListOf<ParseWarning>()

        try {
            val trimmed = text.trim()
            if (trimmed.startsWith("{")) {
                val jsonObject = JSONObject(trimmed)
                if (jsonObject.has("backupType") || jsonObject.has("format")) {
                    warnings.add(
                        ParseWarning(
                            type = ParseWarningType.CONFLICT,
                            lineNumber = 1,
                            rawText = "",
                            message = "این فایل پشتیبان است. لطفاً از بخش بازیابی استفاده کنید"
                        )
                    )
                    return ParseResult(emptyList(), warnings)
                }

                val array = if (jsonObject.has("entries")) jsonObject.getJSONArray("entries") else JSONArray()
                parseJsonArray(array, entries, warnings)
            } else if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                parseJsonArray(array, entries, warnings)
            } else {
                warnings.add(
                    ParseWarning(
                        type = ParseWarningType.UNKNOWN_FORMAT,
                        lineNumber = 1,
                        rawText = text.take(100),
                        message = "فرمت فایل نامعتبر است: داده با { یا [ آغاز نمی‌شود"
                    )
                )
            }
        } catch (e: Exception) {
            warnings.add(
                ParseWarning(
                    type = ParseWarningType.UNKNOWN_FORMAT,
                    lineNumber = 1,
                    rawText = text.take(100),
                    message = "خطا در تجزیه JSON: ${e.message}"
                )
            )
        }

        return ParseResult(entries, warnings)
    }

    private fun parseJsonArray(array: JSONArray, entries: MutableList<ParsedEntry>, warnings: MutableList<ParseWarning>) {
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val source = TextUtilities.cleanText(item.optString("source", item.optString("word", "")))
            val translations = mutableListOf<String>()

            val transArr = item.optJSONArray("translations")
            if (transArr != null) {
                for (j in 0 until transArr.length()) {
                    translations.add(TextUtilities.cleanText(transArr.optString(j)))
                }
            } else {
                val singleTrans = item.optString("translation", item.optString("meaning", ""))
                translations.addAll(TextUtilities.splitTranslations(singleTrans))
            }

            val note = item.optString("note", "").ifBlank { null }
            val categories = mutableListOf<String>()
            val catArr = item.optJSONArray("categories")
            if (catArr != null) {
                for (j in 0 until catArr.length()) {
                    categories.add(catArr.optString(j).trim())
                }
            }

            val rawType = item.optString("entryType", item.optString("type", ""))
            val entryType = try {
                if (rawType.isNotBlank()) EntryType.valueOf(rawType.uppercase()) else VocabularyParser.classifyEntry(source)
            } catch (e: Exception) {
                VocabularyParser.classifyEntry(source)
            }

            val cleanTrans = translations.filter { it.isNotBlank() }
            if (source.isNotBlank() && cleanTrans.isNotEmpty()) {
                entries.add(
                    ParsedEntry(
                        sourceText = source,
                        translations = cleanTrans,
                        note = note,
                        categoryNames = categories.filter { it.isNotBlank() },
                        entryType = entryType,
                        confidence = 1.0,
                        lineNumber = i + 1,
                        rawLines = listOf(item.toString())
                    )
                )
            } else {
                warnings.add(
                    ParseWarning(
                        type = ParseWarningType.UNKNOWN_FORMAT,
                        lineNumber = i + 1,
                        rawText = item.toString().take(100),
                        message = "آیتم به دلیل نبود واژه مبدا یا ترجمه نادیده گرفته شد"
                    )
                )
            }
        }
    }

    fun readXlsx(inputStream: InputStream): ParseResult {
        val sharedStrings = mutableListOf<String>()
        val sheetRows = mutableListOf<List<String>>()

        try {
            val zip = ZipInputStream(inputStream)
            var entry = zip.nextEntry
            val sheetMap = mutableMapOf<String, ByteArray>()

            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml") {
                    parseSharedStrings(zip, sharedStrings)
                } else if (entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml")) {
                    sheetMap[entry.name] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }

            val targetSheetBytes = sheetMap["xl/worksheets/sheet1.xml"]
                ?: sheetMap.entries.sortedBy { it.key }.firstOrNull()?.value

            if (targetSheetBytes != null) {
                parseSheet(targetSheetBytes.inputStream(), sharedStrings, sheetRows)
            }
        } catch (e: Exception) {
            return ParseResult(
                emptyList(),
                listOf(ParseWarning(ParseWarningType.UNKNOWN_FORMAT, 1, "", "خطا در خواندن فایل اکسل: ${e.message}"))
            )
        }

        if (sheetRows.isEmpty()) return ParseResult(emptyList(), emptyList())
        val csvSimulated = sheetRows.joinToString("\n") { row ->
            row.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" }
        }
        return readCsv(csvSimulated.byteInputStream(Charsets.UTF_8))
    }

    private fun parseSharedStrings(stream: InputStream, list: MutableList<String>) {
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")
        var eventType = parser.eventType
        var currentString = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") insideT = true
                    if (parser.name == "si") currentString = StringBuilder()
                }
                XmlPullParser.TEXT -> {
                    if (insideT) currentString.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") insideT = false
                    if (parser.name == "si") list.add(currentString.toString())
                }
            }
            eventType = parser.next()
        }
    }

    private fun parseSheet(stream: InputStream, sharedStrings: List<String>, rows: MutableList<List<String>>) {
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")
        var eventType = parser.eventType

        var currentRow = mutableListOf<String>()
        var cellType: String? = null
        var cellRef: String? = null
        var cellValue = StringBuilder()
        var insideV = false
        var insideInlineT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t")
                            cellRef = parser.getAttributeValue(null, "r")
                            cellValue = StringBuilder()
                        }
                        "v" -> insideV = true
                        "t" -> if (cellType == "inlineStr") insideInlineT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideV || insideInlineT) cellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> insideV = false
                        "t" -> insideInlineT = false
                        "c" -> {
                            val textVal = cellValue.toString().trim()
                            val resolved = if (cellType == "s") {
                                val idx = textVal.toIntOrNull() ?: -1
                                if (idx in sharedStrings.indices) sharedStrings[idx] else textVal
                            } else {
                                textVal
                            }

                            // Pad columns if cellRef specifies a column letter (e.g. B2 -> col index 1)
                            val colIdx = cellRef?.takeWhile { it.isLetter() }?.let { colLettersToIndex(it) }
                            if (colIdx != null && colIdx > currentRow.size) {
                                while (currentRow.size < colIdx) {
                                    currentRow.add("")
                                }
                            }
                            currentRow.add(resolved)
                        }
                        "row" -> rows.add(currentRow.toList())
                    }
                }
            }
            eventType = parser.next()
        }
    }

    private fun colLettersToIndex(letters: String): Int {
        var result = 0
        for (ch in letters.uppercase()) {
            result = result * 26 + (ch - 'A' + 1)
        }
        return (result - 1).coerceAtLeast(0)
    }
}
