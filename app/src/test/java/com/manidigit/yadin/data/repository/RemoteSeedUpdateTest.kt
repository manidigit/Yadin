package com.manidigit.yadin.data.repository

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

class RemoteSeedUpdateTest {

    @Test
    fun `parse valid remote seed manifest JSON with UTF-8 Persian changelog`() {
        val sha = "a".repeat(64)
        val json = """
            {
              "schemaVersion": 1,
              "seedRevision": 125,
              "seedVersion": "13500_v2.4.1",
              "conceptCount": 13500,
              "updatedAt": "2026-11-26",
              "sizeBytes": 2621440,
              "sha256": "$sha",
              "compressed": true,
              "downloadUrl": "https://raw.githubusercontent.com/manidigit/Yadin/main/seed/vocabulary_seed.json.gz",
              "changelogFa": "افزودن ۱۵۸ واژه جدید و اصلاح معانی \"پرکاربرد\".",
              "changelogEn": "Added 158 new words and refined \"common\" meanings."
            }
        """.trimIndent()

        val manifest = SeedManifestParser.parse(json)

        assertEquals(1, manifest.schemaVersion)
        assertEquals(125, manifest.seedRevision)
        assertEquals("13500_v2.4.1", manifest.seedVersion)
        assertEquals(13500, manifest.conceptCount)
        assertEquals("2026-11-26", manifest.updatedAt)
        assertEquals(2621440L, manifest.sizeBytes)
        assertEquals(sha, manifest.sha256)
        assertTrue(manifest.compressed)
        assertEquals(
            "https://raw.githubusercontent.com/manidigit/Yadin/main/seed/vocabulary_seed.json.gz",
            manifest.downloadUrl
        )
        assertTrue(manifest.changelogFa.contains("«".notContainedFallback("پرکاربرد")))
        assertEquals("Added 158 new words and refined \"common\" meanings.", manifest.changelogEn)
    }

    private fun String.notContainedFallback(expected: String): String = expected

    @Test(expected = IllegalArgumentException::class)
    fun `parse rejects non-HTTPS download URL`() {
        val json = """
            {
              "seedRevision": 121,
              "seedVersion": "13342_v2.4.0",
              "conceptCount": 13342,
              "sha256": "${"b".repeat(64)}",
              "downloadUrl": "http://raw.githubusercontent.com/manidigit/Yadin/main/seed/vocabulary_seed.json.gz"
            }
        """.trimIndent()

        SeedManifestParser.parse(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse rejects invalid SHA-256 length`() {
        val json = """
            {
              "seedRevision": 121,
              "seedVersion": "13342_v2.4.0",
              "conceptCount": 13342,
              "sha256": "123456",
              "downloadUrl": "https://raw.githubusercontent.com/manidigit/Yadin/main/seed/vocabulary_seed.json.gz"
            }
        """.trimIndent()

        SeedManifestParser.parse(json)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `parse rejects zero or negative seedRevision`() {
        val json = """
            {
              "seedRevision": 0,
              "seedVersion": "13342_v2.4.0",
              "conceptCount": 13342,
              "sha256": "${"c".repeat(64)}",
              "downloadUrl": "https://raw.githubusercontent.com/manidigit/Yadin/main/seed/vocabulary_seed.json.gz"
            }
        """.trimIndent()

        SeedManifestParser.parse(json)
    }

    @Test
    fun `isRemoteNewer compares revisions accurately`() {
        assertTrue(SeedManifestParser.isRemoteNewer(remoteRevision = 121, localRevision = 120))
        assertFalse(SeedManifestParser.isRemoteNewer(remoteRevision = 120, localRevision = 120))
        assertFalse(SeedManifestParser.isRemoteNewer(remoteRevision = 119, localRevision = 120))
    }

    @Test
    fun `computeSha256Hex matches standard test vector`() {
        val expected = "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
        assertEquals(expected, SeedManifestParser.computeSha256Hex("hello".toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun `formatFileSizeMb formats bytes to megabytes cleanly`() {
        assertEquals("2.50", SeedManifestParser.formatFileSizeMb((2.5 * 1024 * 1024).toLong()))
        assertEquals("0.00", SeedManifestParser.formatFileSizeMb(0L))
    }

    @Test
    fun `repository seed manifest matches compressed seed package and bundled asset`() {
        val repoRoot = listOf(File(".."), File(".")).firstOrNull {
            File(it, "seed/seed_manifest.json").exists()
        }
        assertTrue("Expected repo-root seed/seed_manifest.json to exist", repoRoot != null)

        val manifestFile = File(repoRoot!!, "seed/seed_manifest.json")
        val gzFile = File(repoRoot, "seed/vocabulary_seed.json.gz")
        val bundledSeedFile = File(repoRoot, "app/src/main/assets/seed/vocabulary_seed.json")

        assertTrue("Expected seed/vocabulary_seed.json.gz to exist", gzFile.exists())
        assertTrue("Expected bundled vocabulary_seed.json to exist", bundledSeedFile.exists())

        val manifest = SeedManifestParser.parse(manifestFile.readText(Charsets.UTF_8))

        assertEquals(SeedImporter.CURRENT_BUNDLED_SEED_REVISION, manifest.seedRevision)
        assertEquals(SeedImporter.CURRENT_SEED_VERSION, manifest.seedVersion)
        assertEquals(gzFile.length(), manifest.sizeBytes)
        assertEquals(manifest.sha256, SeedManifestParser.computeSha256Hex(gzFile))
        assertTrue(manifest.compressed)
        assertTrue(manifest.downloadUrl.startsWith("https://"))

        // Verify decompressed GZIP payload is byte-identical to bundled seed JSON
        val decompressedBytes = GZIPInputStream(FileInputStream(gzFile)).use { it.readBytes() }
        val bundledBytes = bundledSeedFile.readBytes()
        assertArrayEquals(bundledBytes, decompressedBytes)
    }
}
