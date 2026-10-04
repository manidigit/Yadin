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
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

object FileReaders {

    fun readText(inputStream: InputStream): ParseResult {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return VocabularyParser.parse(text)
    }

    fun readCsv(inputStream: InputStream): ParseResult {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines()
        if (lines.isEmpty()) return ParseResult(emptyList(), emptyList())

        // Delimiter auto-detection on first line
        val firstLine = lines.first()
        val delimiter = detectDelimiter(firstLine)

        val entries = mutableListOf<ParsedEntry>()
        val warnings = mutableListOf<ParseWarning>()

        var sourceCol = 0
        var transCol = 1
        var noteCol = 2
        var categoryCol = 3
        var typeCol = 4
        var startIndex = 0

        // Check header
        val headerTokens = parseCsvLine(firstLine, delimiter)
        if (isHeaderRow(headerTokens)) {
            startIndex = 1
            for ((idx, colName) in headerTokens.withIndex()) {
                val clean = colName.trim().lowercase()
                when {
                    clean in listOf("source", "word", "spanish", "واژه", "کلمه", "اسپانیایی") -> sourceCol = idx
                    clean in listOf("translation", "meaning", "translations", "ترجمه", "معنی", "فارسی") -> transCol = idx
                    clean in listOf("note", "notes", "یادداشت", "نکته") -> noteCol = idx
                    clean in listOf("category", "categories", "دسته", "دسته‌بندی") -> categoryCol = idx
                    clean in listOf("type", "entrytype", "نوع") -> typeCol = idx
                }
            }
        }

        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue

            val tokens = parseCsvLine(line, delimiter)
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
                        rawText = line,
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
                        rawLines = listOf(line)
                    )
                )
            }
        }

        return ParseResult(entries, warnings)
    }

    private fun detectDelimiter(line: String): Char {
        val commaCount = line.count { it == ',' }
        val semicolonCount = line.count { it == ';' }
        val tabCount = line.count { it == '\t' }
        return when {
            tabCount > commaCount && tabCount > semicolonCount -> '\t'
            semicolonCount > commaCount -> ';'
            else -> ','
        }
    }

    private fun isHeaderRow(tokens: List<String>): Boolean {
        val keywords = setOf("source", "word", "spanish", "translation", "meaning", "ترجمه", "واژه", "کلمه", "معنی")
        return tokens.any { it.trim().lowercase() in keywords }
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                result.add(current.toString().trim())
                current = StringBuilder()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
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

            if (source.isNotBlank() && translations.isNotEmpty()) {
                entries.add(
                    ParsedEntry(
                        sourceText = source,
                        translations = translations.filter { it.isNotBlank() },
                        note = note,
                        categoryNames = categories.filter { it.isNotBlank() },
                        entryType = entryType,
                        confidence = 1.0,
                        lineNumber = i + 1,
                        rawLines = listOf(item.toString())
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
            var sheetBytes: ByteArray? = null

            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml") {
                    parseSharedStrings(zip, sharedStrings)
                } else if (entry.name == "xl/worksheets/sheet1.xml") {
                    sheetBytes = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }

            if (sheetBytes != null) {
                parseSheet(sheetBytes.inputStream(), sharedStrings, sheetRows)
            }
        } catch (e: Exception) {
            return ParseResult(
                emptyList(),
                listOf(ParseWarning(ParseWarningType.UNKNOWN_FORMAT, 1, "", "خطا در خواندن فایل اکسل: ${e.message}"))
            )
        }

        // Convert parsed sheet rows to CSV-style parsing
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
        var cellValue = StringBuilder()
        var insideV = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t")
                            cellValue = StringBuilder()
                        }
                        "v" -> insideV = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideV) cellValue.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> insideV = false
                        "c" -> {
                            val textVal = cellValue.toString().trim()
                            val resolved = if (cellType == "s") {
                                val idx = textVal.toIntOrNull() ?: -1
                                if (idx in sharedStrings.indices) sharedStrings[idx] else textVal
                            } else {
                                textVal
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
}
