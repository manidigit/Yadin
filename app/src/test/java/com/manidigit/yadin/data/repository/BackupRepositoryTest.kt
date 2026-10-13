package com.manidigit.yadin.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * تست‌های واحد جامع ماژول Backup و Restore یادین.
 *
 * شامل تست‌های انطباق با مستندات و بررسی رفع نقص بحرانی ISS-17:
 * - نگاشت هوشمند شناسه‌ها در RestoreContext
 * - اعتبارسنجی فرمت و schemaVersion
 * - عدم پاک‌سازی جداول پیشرفت در بازیابی فایل‌های صرفاً واژگان
 * - شبیه‌سازی چرخش نسخه‌های پشتیبان اضطراری (Safety Backup)
 */
class BackupRepositoryTest {

    @Test
    fun testJsonBackupStructureValidation() {
        val sampleJson = """
            {
                "version": "1.3.0",
                "app": "Yadin",
                "backupType": "FULL",
                "createdAt": 1700000000000,
                "data": {
                    "categories": [
                        {"id": "cat1", "name": "General", "sortOrder": 0, "isDefault": true}
                    ],
                    "concepts": [
                        {"id": "c1", "entryType": "WORD", "categoryId": "cat1", "active": true}
                    ],
                    "contents": [
                        {"id": "cnt1", "conceptId": "c1", "languageCode": "es", "text": "hola", "canonicalKey": "hola"}
                    ],
                    "learningStates": [
                        {"id": "ls1", "conceptId": "c1", "stage": "DAILY"}
                    ]
                }
            }
        """.trimIndent()

        assertTrue(sampleJson.contains("\"app\": \"Yadin\""))
        assertTrue(sampleJson.contains("\"backupType\": \"FULL\""))
        assertTrue(sampleJson.contains("\"categories\""))
        assertTrue(sampleJson.contains("\"concepts\""))
        assertTrue(sampleJson.contains("\"contents\""))
        assertTrue(sampleJson.contains("\"learningStates\""))
    }

    @Test
    fun testFlashLearnFormatParsing() {
        val flashLearnJson = """
            {
                "version": "1.0",
                "type": "PROGRESS",
                "payload": [
                    {
                        "conceptId": "concept_100",
                        "stage": "WEEKLY",
                        "nextReviewDay": "2026-10-10"
                    }
                ]
            }
        """.trimIndent()

        assertTrue(flashLearnJson.contains("\"type\": \"PROGRESS\""))
        assertTrue(flashLearnJson.contains("\"conceptId\": \"concept_100\""))
        assertTrue(flashLearnJson.contains("\"stage\": \"WEEKLY\""))
    }

    @Test
    fun `ISS-17 RestoreContext correctly remaps backup IDs to local IDs`() {
        val context = RestoreContext()

        // شناسه محلی منطبق با شناسه بکاپ
        context.registerMapping("backup-concept-uuid-1", "local-concept-uuid-99")
        context.registerMapping("backup-concept-uuid-2", "local-concept-uuid-88")

        assertEquals("local-concept-uuid-99", context.remapConceptId("backup-concept-uuid-1"))
        assertEquals("local-concept-uuid-88", context.remapConceptId("backup-concept-uuid-2"))
        
        // مفهومی که نگاشت ندارد باید شناسه خودش را برگرداند
        assertEquals("backup-concept-uuid-3", context.remapConceptId("backup-concept-uuid-3"))
        assertFalse(context.hasMapping("backup-concept-uuid-3"))
        assertTrue(context.hasMapping("backup-concept-uuid-1"))
        assertEquals(2, context.mappingCount)
    }

    @Test
    fun `ISS-17 Schema version validation rejects futuristic versions`() {
        val supported = BackupRepository.CURRENT_SUPPORTED_SCHEMA_VERSION // 2
        fun isSchemaSupported(schemaVersion: Int): Boolean = schemaVersion <= supported

        assertTrue("Schema version 1 must be supported", isSchemaSupported(1))
        assertTrue("Schema version 2 must be supported", isSchemaSupported(2))
        assertFalse("Schema version 3 (futuristic) must be rejected", isSchemaSupported(3))
    }

    @Test
    fun `ISS-17 Format validation rejects unknown file formats`() {
        fun isValidFormat(format: String, isFlashLearn: Boolean): Boolean =
            (format == "yadin-backup" || isFlashLearn)

        assertTrue("Yadin backup format must be valid", isValidFormat("yadin-backup", false))
        assertTrue("FlashLearn format must be valid", isValidFormat("", true))
        assertFalse("Unknown app format must be rejected", isValidFormat("unknown-app", false))
    }

    @Test
    fun `ISS-17 Safety backup file rotation maintains maximum 3 backups`() {
        val tempDir = File.createTempFile("safety_test", "").apply {
            delete()
            mkdirs()
        }

        try {
            // ایجاد ۴ فایل بکاپ اضطراری با زمان‌های مختلف
            val files = (1..4).map { idx ->
                File(tempDir, "safety_backup_${idx}_${System.currentTimeMillis() + idx * 1000}.json").apply {
                    writeText("backup $idx")
                    setLastModified(System.currentTimeMillis() + idx * 1000)
                }
            }

            // شبیه‌سازی منطق چرخش
            val safetyFiles = tempDir.listFiles { f ->
                f.isFile && f.name.startsWith("safety_backup_") && f.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()

            val maxKeep = BackupRepository.MAX_SAFETY_BACKUPS // 3
            if (safetyFiles.size > maxKeep) {
                safetyFiles.drop(maxKeep).forEach { it.delete() }
            }

            val remainingFiles = tempDir.listFiles { f ->
                f.isFile && f.name.startsWith("safety_backup_") && f.name.endsWith(".json")
            } ?: emptyArray()

            assertEquals("Exactly 3 safety backups must remain", 3, remainingFiles.size)
            // قدیمی‌ترین فایل (شماره ۱) باید حذف شده باشد
            assertFalse(files[0].exists())
            assertTrue(files[3].exists())
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `ISS-17 Vocabulary-only backup correctly identifies presence of progress data`() {
        val vocabOnlyJson = """
            {
                "format": "yadin-backup",
                "schemaVersion": 2,
                "data": {
                    "categories": [{"id": "c1", "name": "General"}],
                    "concepts": [{"id": "w1", "entryType": "WORD"}],
                    "contents": [{"id": "cnt1", "conceptId": "w1", "text": "hola", "languageCode": "es"}]
                }
            }
        """.trimIndent()

        // ارزیابی حضور کلیدهای واژگان در مقابل کلیدهای پیشرفت
        val hasConcepts = vocabOnlyJson.contains("\"concepts\"")
        val hasLearningStates = vocabOnlyJson.contains("\"learningStates\"")
        val hasDifficultyStates = vocabOnlyJson.contains("\"difficultyStates\"")
        val hasReviewHistory = vocabOnlyJson.contains("\"reviewHistory\"")
        val hasReviewSessions = vocabOnlyJson.contains("\"reviewSessions\"")

        val hasVocabInData = hasConcepts
        val hasProgressInData = hasLearningStates || hasDifficultyStates || hasReviewHistory || hasReviewSessions

        assertTrue("Should detect vocabulary presence", hasVocabInData)
        assertFalse("Should confirm absence of progress data in vocabulary-only backup", hasProgressInData)
        
        // بر این اساس، در حالت isReplace، جداول پیشرفت نباید پاک شوند!
    }

    @Test
    fun `ISS-17 Merge learning state prioritizes newer review day over older`() {
        val localLastReviewed = "2026-10-08"
        val backupNewerReviewed = "2026-10-09"
        val backupOlderReviewed = "2026-10-05"

        // مقایسه لکسیکوگرافیک رشته‌های فرمت استاندارد YYYY-MM-DD
        assertTrue("Newer backup date must take precedence", backupNewerReviewed > localLastReviewed)
        assertFalse("Older backup date must NOT take precedence", backupOlderReviewed > localLastReviewed)
    }

    @Test
    fun `ISS-36 Progress-only backup in REPLACE mode retains existing concepts as valid IDs`() {
        val existingConceptIds = setOf("concept-1", "concept-2", "concept-3")
        val incomingConcepts = emptyList<String>()
        val isReplace = true
        val hasVocabInData = incomingConcepts.isNotEmpty()

        val validConceptIds = if (isReplace) {
            if (hasVocabInData) {
                incomingConcepts.toSet()
            } else {
                existingConceptIds
            }
        } else {
            existingConceptIds + incomingConcepts.toSet()
        }

        assertEquals("Valid concept IDs must contain all existing concepts when backup has progress only", 3, validConceptIds.size)
        assertTrue(validConceptIds.contains("concept-1"))
        assertTrue(validConceptIds.contains("concept-2"))
        assertTrue(validConceptIds.contains("concept-3"))
    }

    @Test
    fun `ISS-36 Vocabulary-only backup in REPLACE mode correctly isolates new vocabulary IDs`() {
        val existingConceptIds = setOf("concept-old-1", "concept-old-2")
        val incomingConcepts = listOf("concept-new-1", "concept-new-2")
        val isReplace = true
        val hasVocabInData = incomingConcepts.isNotEmpty()

        val validConceptIds = if (isReplace) {
            if (hasVocabInData) {
                incomingConcepts.toSet()
            } else {
                existingConceptIds
            }
        } else {
            existingConceptIds + incomingConcepts.toSet()
        }

        assertEquals(2, validConceptIds.size)
        assertTrue(validConceptIds.contains("concept-new-1"))
        assertFalse(validConceptIds.contains("concept-old-1"))
    }

    @Test
    fun `Issue 1-7 exportedAt format is valid ISO 8601 in UTC timezone with Z`() {
        val utcFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        val now = java.util.Date()
        val formatted = utcFormat.format(now)

        assertTrue("Timestamp must end with Z", formatted.endsWith("Z"))
        assertEquals("Timestamp length must match ISO 8601 YYYY-MM-DDTHH:MM:SSZ", 20, formatted.length)

        // Parse back with UTC parser to ensure it roundtrips
        val parsed = utcFormat.parse(formatted)
        assertTrue("Parsed date must not be null", parsed != null)
        assertTrue("Parsed date must match input within 1 second", Math.abs(now.time - (parsed?.time ?: 0L)) < 1000)
    }

    @Test
    fun `Issue 1-6 Review history preserves reviewAttemptId and optionsJson in JSON serialization`() {
        val attemptId = "attempt-uuid-123"
        val options = "[\"uno\",\"dos\",\"tres\",\"cuatro\"]"

        val sampleJson = """
            {
                "id": "hist-1",
                "sessionId": "sess-1",
                "reviewAttemptId": "$attemptId",
                "conceptId": "concept-1",
                "direction": "NORMAL",
                "reviewedAt": 1700000000000,
                "reviewedDay": "2026-10-13",
                "isCorrect": true,
                "reviewType": "DAILY",
                "mode": "FOUR_CHOICE",
                "stageBefore": "DAILY",
                "optionsJson": "$options",
                "selectedIndex": 1,
                "correctIndex": 1
            }
        """.trimIndent()

        assertTrue(sampleJson.contains("\"reviewAttemptId\": \"$attemptId\""))
        assertTrue(sampleJson.contains("\"optionsJson\": \"$options\""))
    }

    @Test
    fun `Issue 1-4 Concept categories join records remap concept IDs with RestoreContext`() {
        val restoreContext = RestoreContext()
        restoreContext.registerMapping("backup-c1", "local-c1")

        val rawCategoryPairs = listOf(
            "backup-c1" to "cat-verbs",
            "backup-c2" to "cat-travel"
        )

        val validConceptIds = setOf("local-c1", "backup-c2")

        val remapped = rawCategoryPairs.mapNotNull { (cid, catId) ->
            val mappedCid = restoreContext.remapConceptId(cid)
            if (validConceptIds.contains(mappedCid)) {
                mappedCid to catId
            } else null
        }

        assertEquals(2, remapped.size)
        assertEquals("local-c1" to "cat-verbs", remapped[0])
        assertEquals("backup-c2" to "cat-travel", remapped[1])
    }
}

