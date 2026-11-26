package com.manidigit.yadin.data.repository

import android.content.Context
import com.manidigit.yadin.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.GZIPInputStream

/**
 * مشخصات نسخه آنلاین بانک واژگان (فایل seed_manifest.json در مخزن گیت‌هاب).
 */
data class RemoteSeedManifest(
    val schemaVersion: Int,
    val seedRevision: Int,
    val seedVersion: String,
    val conceptCount: Int,
    val updatedAt: String,
    val sizeBytes: Long,
    val sha256: String,
    val compressed: Boolean,
    val downloadUrl: String,
    val changelogFa: String,
    val changelogEn: String
)

/**
 * وضعیت ماشین حالت (State Machine) فرآیند بررسی و به‌روزرسانی آنلاین بانک واژگان.
 */
sealed class SeedUpdateState {
    object Idle : SeedUpdateState()
    object Checking : SeedUpdateState()
    data class UpToDate(val currentVersion: String, val currentRevision: Int) : SeedUpdateState()
    data class Available(val manifest: RemoteSeedManifest) : SeedUpdateState()
    data class Downloading(val progress: Float, val message: String) : SeedUpdateState()
    data class Applying(val progress: Float, val message: String) : SeedUpdateState()
    data class Success(val newVersion: String, val newRevision: Int, val conceptCount: Int) : SeedUpdateState()
    data class Error(val message: String) : SeedUpdateState()
}

/**
 * پارسر مستقل و خالص Kotlin برای فایل `seed_manifest.json` و ابزارهای اعتبارسنجی امنیتی
 * (بدون وابستگی به کلاس‌های Android تا در تست‌های واحد JVM مستقیماً اجرا و تأیید شود).
 */
object SeedManifestParser {

    fun isRemoteNewer(remoteRevision: Int, localRevision: Int): Boolean =
        remoteRevision > localRevision

    fun isValidSha256Hex(value: String): Boolean =
        value.length == 64 && value.all {
            it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F'
        }

    fun isAllowedHttpsUrl(url: String): Boolean =
        url.trim().startsWith("https://", ignoreCase = true) && url.trim().length > 10

    fun computeSha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun computeSha256Hex(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(16 * 1024)
            while (true) {
                val read = fis.read(buf)
                if (read <= 0) break
                md.update(buf, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    fun formatFileSizeMb(bytes: Long): String {
        val mb = bytes.coerceAtLeast(0L).toDouble() / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.2f", mb)
    }

    /**
     * پارس و اعتبارسنجی دقیق JSON مانیفست بانک واژگان.
     */
    fun parse(jsonText: String): RemoteSeedManifest {
        val fields = parseFlatJsonObject(jsonText)

        val schemaVersion = (fields["schemaVersion"] as? Number)?.toInt() ?: 1
        val seedRevision = (fields["seedRevision"] as? Number)?.toInt()
            ?: throw IllegalArgumentException("Missing or invalid seedRevision")
        val seedVersion = (fields["seedVersion"] as? String)?.trim().orEmpty()
        val conceptCount = (fields["conceptCount"] as? Number)?.toInt() ?: 0
        val updatedAt = (fields["updatedAt"] as? String)?.trim().orEmpty()
        val sizeBytes = (fields["sizeBytes"] as? Number)?.toLong() ?: 0L
        val sha256 = (fields["sha256"] as? String)?.trim().orEmpty().lowercase(Locale.US)
        val compressed = (fields["compressed"] as? Boolean) ?: true
        val downloadUrl = (fields["downloadUrl"] as? String)?.trim().orEmpty()
        val changelogFa = (fields["changelogFa"] as? String)?.trim().orEmpty()
        val changelogEn = (fields["changelogEn"] as? String)?.trim().orEmpty()

        if (seedRevision <= 0) {
            throw IllegalArgumentException("seedRevision must be positive")
        }
        if (seedVersion.isBlank()) {
            throw IllegalArgumentException("seedVersion is blank")
        }
        if (conceptCount <= 0) {
            throw IllegalArgumentException("conceptCount must be positive")
        }
        if (!isValidSha256Hex(sha256)) {
            throw IllegalArgumentException("Invalid SHA-256 checksum in manifest")
        }
        if (!isAllowedHttpsUrl(downloadUrl)) {
            throw IllegalArgumentException("Download URL must use HTTPS")
        }

        return RemoteSeedManifest(
            schemaVersion = schemaVersion,
            seedRevision = seedRevision,
            seedVersion = seedVersion,
            conceptCount = conceptCount,
            updatedAt = updatedAt,
            sizeBytes = sizeBytes,
            sha256 = sha256,
            compressed = compressed,
            downloadUrl = downloadUrl,
            changelogFa = changelogFa,
            changelogEn = changelogEn
        )
    }

    /**
     * پارسر سبک و استاندارد JSON برای اشیای مسطح (پشتیبانی کامل از escape، یونیکد، اعداد و بولین).
     */
    internal fun parseFlatJsonObject(src: String): Map<String, Any?> {
        var i = 0
        val n = src.length

        fun skipWs() {
            while (i < n && (src[i] == ' ' || src[i] == '\n' || src[i] == '\r' || src[i] == '\t')) i++
        }

        fun parseString(): String {
            if (i >= n || src[i] != '"') throw IllegalArgumentException("Expected '\"' at $i")
            i++
            val sb = StringBuilder()
            while (i < n) {
                val c = src[i++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        if (i >= n) throw IllegalArgumentException("Unterminated escape")
                        when (val e = src[i++]) {
                            '"' -> sb.append('"')
                            '\\' -> sb.append('\\')
                            '/' -> sb.append('/')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> {
                                if (i + 4 > n) throw IllegalArgumentException("Invalid unicode escape")
                                val hex = src.substring(i, i + 4)
                                sb.append(hex.toInt(16).toChar())
                                i += 4
                            }
                            else -> sb.append(e)
                        }
                    }
                    else -> sb.append(c)
                }
            }
            throw IllegalArgumentException("Unterminated JSON string")
        }

        fun skipNested(openChar: Char, closeChar: Char) {
            var depth = 0
            while (i < n) {
                when (src[i]) {
                    '"' -> parseString()
                    openChar -> {
                        depth++
                        i++
                    }
                    closeChar -> {
                        depth--
                        i++
                        if (depth == 0) return
                    }
                    else -> i++
                }
            }
            throw IllegalArgumentException("Unterminated nested JSON structure")
        }

        fun parseValue(): Any? {
            skipWs()
            if (i >= n) throw IllegalArgumentException("Unexpected end of JSON")
            return when (val c = src[i]) {
                '"' -> parseString()
                '{' -> {
                    skipNested('{', '}')
                    null
                }
                '[' -> {
                    skipNested('[', ']')
                    null
                }
                't' -> {
                    if (src.startsWith("true", i)) {
                        i += 4
                        true
                    } else throw IllegalArgumentException("Invalid literal at $i")
                }
                'f' -> {
                    if (src.startsWith("false", i)) {
                        i += 5
                        false
                    } else throw IllegalArgumentException("Invalid literal at $i")
                }
                'n' -> {
                    if (src.startsWith("null", i)) {
                        i += 4
                        null
                    } else throw IllegalArgumentException("Invalid literal at $i")
                }
                '-', in '0'..'9' -> {
                    val start = i
                    if (src[i] == '-') i++
                    while (i < n && src[i] in '0'..'9') i++
                    var isFloat = false
                    if (i < n && src[i] == '.') {
                        isFloat = true
                        i++
                        while (i < n && src[i] in '0'..'9') i++
                    }
                    if (i < n && (src[i] == 'e' || src[i] == 'E')) {
                        isFloat = true
                        i++
                        if (i < n && (src[i] == '+' || src[i] == '-')) i++
                        while (i < n && src[i] in '0'..'9') i++
                    }
                    val token = src.substring(start, i)
                    if (isFloat) token.toDouble() else token.toLong()
                }
                else -> throw IllegalArgumentException("Unexpected character '$c' at $i")
            }
        }

        skipWs()
        if (i >= n || src[i] != '{') throw IllegalArgumentException("Manifest must be a JSON object")
        i++
        val result = linkedMapOf<String, Any?>()
        skipWs()
        if (i < n && src[i] == '}') {
            i++
            return result
        }
        while (i < n) {
            skipWs()
            val key = parseString()
            skipWs()
            if (i >= n || src[i] != ':') throw IllegalArgumentException("Expected ':' after key '$key'")
            i++
            val value = parseValue()
            result[key] = value
            skipWs()
            if (i < n && src[i] == ',') {
                i++
                continue
            }
            if (i < n && src[i] == '}') {
                i++
                return result
            }
            throw IllegalArgumentException("Expected ',' or '}' at $i")
        }
        throw IllegalArgumentException("Unterminated JSON object")
    }
}

/**
 * مخزن دریافت و اعمال به‌روزرسانی آنلاین بانک واژگان از گیت‌هاب (GitHub Raw Static Hosting).
 *
 * اصول طراحی:
 * - هسته یادگیری کاملاً آفلاین باقی می‌ماند و این مخزن صرفاً به درخواست صریح کاربر فراخوانی می‌شود.
 * - پیش از اعمال هر تغییری در دیتابیس، فایل دانلودشده با SHA-256 بررسی و یک پشتیبان ایمنی (Safety Backup) ایجاد می‌شود.
 * - همگام‌سازی واژگان از طریق [SeedImporter.importSeedFromStream] بدون کوچک‌ترین آسیبی به پیشرفت مطالعه و شمارنده‌های کاربر انجام می‌شود.
 */
class RemoteSeedUpdateRepository(
    private val context: Context,
    private val seedImporter: SeedImporter,
    private val backupRepository: BackupRepository,
    private val manifestUrl: String = DEFAULT_MANIFEST_URL
) {

    companion object {
        const val DEFAULT_MANIFEST_URL =
            "https://raw.githubusercontent.com/manidigit/Yadin/main/seed/seed_manifest.json"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 45_000
        private const val MAX_REDIRECTS = 5
        private const val MAX_MANIFEST_BYTES = 256 * 1024
        private const val MAX_SEED_FILE_BYTES = 50L * 1024L * 1024L
    }

    /**
     * بررسی وجود نسخه جدیدتر بانک واژگان در مخزن گیت‌هاب.
     * در صورت وجود نسخه جدیدتر، شیء [RemoteSeedManifest] و در غیر این صورت `null` برمی‌گرداند.
     */
    suspend fun checkForUpdate(localRevision: Int): Result<RemoteSeedManifest?> =
        withContext(Dispatchers.IO) {
            try {
                val manifestText = fetchTextHttps(manifestUrl)
                val manifest = SeedManifestParser.parse(manifestText)
                if (SeedManifestParser.isRemoteNewer(manifest.seedRevision, localRevision)) {
                    Result.success(manifest)
                } else {
                    Result.success(null)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * دانلود فایل فشرده بانک واژگان، اعتبارسنجی SHA-256، تهیه پشتیبان ایمنی خودکار
     * و همگام‌سازی بدون تخریب با دیتابیس محلی.
     */
    suspend fun downloadAndApplyUpdate(
        manifest: RemoteSeedManifest,
        onDownloading: (Float, String) -> Unit,
        onApplying: (Float, String) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        if (!SeedManifestParser.isAllowedHttpsUrl(manifest.downloadUrl)) {
            return@withContext Result.failure(IllegalArgumentException("Only HTTPS URLs are allowed"))
        }
        if (!SeedManifestParser.isValidSha256Hex(manifest.sha256)) {
            return@withContext Result.failure(IllegalArgumentException("Invalid SHA-256 in manifest"))
        }

        val tempFile = File(context.cacheDir, "yadin_seed_update_${manifest.seedRevision}.tmp")
        try {
            if (tempFile.exists()) tempFile.delete()

            onDownloading(0.02f, context.getString(R.string.seed_update_downloading))
            val actualSha256 = downloadFileWithSha256(
                urlString = manifest.downloadUrl,
                expectedSizeBytes = manifest.sizeBytes,
                targetFile = tempFile
            ) { fraction ->
                onDownloading(fraction, context.getString(R.string.seed_update_downloading))
            }

            if (!actualSha256.equals(manifest.sha256, ignoreCase = true)) {
                throw SecurityException(context.getString(R.string.seed_update_err_checksum))
            }

            onApplying(0.05f, context.getString(R.string.seed_update_creating_backup))
            try {
                backupRepository.createSafetyBackup()
            } catch (_: Exception) {
                // خطای غیربحرانی در ساخت بکاپ ایمنی نباید مانع همگام‌سازی سالم شود
            }

            onApplying(0.12f, context.getString(R.string.seed_update_applying))
            val isGzip = manifest.compressed || manifest.downloadUrl.endsWith(".gz", ignoreCase = true)
            val stream = if (isGzip) {
                GZIPInputStream(BufferedInputStream(FileInputStream(tempFile)))
            } else {
                BufferedInputStream(FileInputStream(tempFile))
            }

            stream.use { input ->
                seedImporter.importSeedFromStream(
                    inputStream = input,
                    targetSeedVersion = manifest.seedVersion,
                    targetSeedRevision = manifest.seedRevision
                ) { importProgress, msg ->
                    val scaled = (0.12f + importProgress * 0.88f).coerceIn(0.12f, 1.0f)
                    onApplying(scaled, msg)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    private fun openHttpsConnection(initialUrl: String): HttpURLConnection {
        var currentUrl = initialUrl
        var redirects = 0
        while (redirects <= MAX_REDIRECTS) {
            if (!SeedManifestParser.isAllowedHttpsUrl(currentUrl)) {
                throw SecurityException("Non-HTTPS URL blocked: $currentUrl")
            }
            val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                instanceFollowRedirects = false
                useCaches = false
                setRequestProperty("Cache-Control", "no-cache")
                setRequestProperty("User-Agent", "Yadin-Android")
            }
            val code = conn.responseCode
            if (code in 301..308 && code != 304) {
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if (location.isNullOrBlank()) {
                    throw IOException("HTTP $code redirect without Location header")
                }
                currentUrl = URL(URL(currentUrl), location).toString()
                redirects++
                continue
            }
            if (code !in 200..299) {
                conn.disconnect()
                throw IOException("HTTP $code")
            }
            return conn
        }
        throw IOException("Too many redirects")
    }

    private fun fetchTextHttps(urlString: String): String {
        val conn = openHttpsConnection(urlString)
        return try {
            BufferedInputStream(conn.inputStream).use { input ->
                val buffer = ByteArray(8 * 1024)
                val out = java.io.ByteArrayOutputStream()
                var total = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > MAX_MANIFEST_BYTES) {
                        throw IOException("Manifest exceeds maximum allowed size")
                    }
                    out.write(buffer, 0, read)
                }
                out.toString(Charsets.UTF_8.name())
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun downloadFileWithSha256(
        urlString: String,
        expectedSizeBytes: Long,
        targetFile: File,
        onProgress: (Float) -> Unit
    ): String {
        val conn = openHttpsConnection(urlString)
        return try {
            val headerLen = conn.contentLengthLong
            val totalExpected = when {
                headerLen > 0L -> headerLen
                expectedSizeBytes > 0L -> expectedSizeBytes
                else -> -1L
            }
            val md = MessageDigest.getInstance("SHA-256")
            BufferedInputStream(conn.inputStream).use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    var lastReportedBucket = -1
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        downloaded += read
                        if (downloaded > MAX_SEED_FILE_BYTES) {
                            throw IOException("Seed file exceeds maximum allowed size")
                        }
                        output.write(buffer, 0, read)
                        md.update(buffer, 0, read)
                        if (totalExpected > 0L) {
                            val fraction = (downloaded.toFloat() / totalExpected.toFloat()).coerceIn(0.02f, 0.99f)
                            val bucket = (fraction * 50).toInt()
                            if (bucket != lastReportedBucket) {
                                lastReportedBucket = bucket
                                onProgress(fraction)
                            }
                        }
                    }
                    output.flush()
                }
            }
            onProgress(1.0f)
            md.digest().joinToString("") { "%02x".format(it) }
        } finally {
            conn.disconnect()
        }
    }
}
