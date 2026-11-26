plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register("packageRemoteSeed") {
    group = "yadin"
    description = "Compresses app/src/main/assets/seed/vocabulary_seed.json into seed/vocabulary_seed.json.gz and updates seed/seed_manifest.json."

    doLast {
        val sourceFile = file("app/src/main/assets/seed/vocabulary_seed.json")
        require(sourceFile.exists()) { "Source seed file not found: ${sourceFile.absolutePath}" }

        val seedDir = file("seed")
        if (!seedDir.exists()) seedDir.mkdirs()

        val gzFile = File(seedDir, "vocabulary_seed.json.gz")
        val manifestFile = File(seedDir, "seed_manifest.json")

        // Deterministic GZIP compression
        sourceFile.inputStream().buffered().use { input ->
            java.util.zip.GZIPOutputStream(gzFile.outputStream().buffered()).use { gzipOut ->
                input.copyTo(gzipOut, bufferSize = 32 * 1024)
            }
        }

        val sha256 = java.security.MessageDigest.getInstance("SHA-256").let { md ->
            gzFile.inputStream().buffered().use { fis ->
                val buf = ByteArray(32 * 1024)
                while (true) {
                    val read = fis.read(buf)
                    if (read <= 0) break
                    md.update(buf, 0, read)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        }

        val seedRevision = (project.findProperty("seedRevision") as? String)?.toIntOrNull() ?: 120
        val seedVersion = (project.findProperty("seedVersion") as? String)?.takeIf { it.isNotBlank() } ?: "13342_v2.3.1"
        val conceptCount = (project.findProperty("conceptCount") as? String)?.toIntOrNull() ?: 13342
        val updatedAt = (project.findProperty("updatedAt") as? String)?.takeIf { it.isNotBlank() }
            ?: java.time.LocalDate.now().toString()
        val downloadUrl = (project.findProperty("downloadUrl") as? String)?.takeIf { it.isNotBlank() }
            ?: "https://raw.githubusercontent.com/manidigit/Yadin/main/seed/vocabulary_seed.json.gz"
        val changelogFa = (project.findProperty("changelogFa") as? String)?.takeIf { it.isNotBlank() }
            ?: "نسخه پایه بانک واژگان آنلاین با ۱۳٬۳۴۲ واژه، ۲۸٬۸۰۸ معنی فارسی و طبقه‌بندی متوازن سطح سختی."
        val changelogEn = (project.findProperty("changelogEn") as? String)?.takeIf { it.isNotBlank() }
            ?: "Baseline online vocabulary database with 13,342 concepts, 28,808 Persian meanings, and balanced difficulty classification."

        fun esc(s: String): String = s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")

        val manifestJson = buildString {
            appendLine("{")
            appendLine("  \"schemaVersion\": 1,")
            appendLine("  \"seedRevision\": $seedRevision,")
            appendLine("  \"seedVersion\": \"${esc(seedVersion)}\",")
            appendLine("  \"conceptCount\": $conceptCount,")
            appendLine("  \"updatedAt\": \"${esc(updatedAt)}\",")
            appendLine("  \"sizeBytes\": ${gzFile.length()},")
            appendLine("  \"sha256\": \"$sha256\",")
            appendLine("  \"compressed\": true,")
            appendLine("  \"downloadUrl\": \"${esc(downloadUrl)}\",")
            appendLine("  \"changelogFa\": \"${esc(changelogFa)}\",")
            appendLine("  \"changelogEn\": \"${esc(changelogEn)}\"")
            appendLine("}")
        }
        manifestFile.writeText(manifestJson, Charsets.UTF_8)
        logger.lifecycle("Generated ${gzFile.path} (${gzFile.length()} bytes) and ${manifestFile.path} (sha256=$sha256)")
    }
}
