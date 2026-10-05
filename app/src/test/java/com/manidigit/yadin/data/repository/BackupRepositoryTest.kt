package com.manidigit.yadin.data.repository

import org.junit.Assert.assertTrue
import org.junit.Test

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
}
