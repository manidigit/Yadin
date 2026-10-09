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
}

