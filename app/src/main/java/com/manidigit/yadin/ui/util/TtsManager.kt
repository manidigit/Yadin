package com.manidigit.yadin.ui.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Thread-safe singleton for TextToSpeech to eliminate IPC service connection leaks,
 * reduce audio latency, and manage speech playback reliably.
 */
object TtsManager {

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var isInitialized = false

    fun speak(context: Context, text: String, languageCode: String? = null) {
        if (text.isBlank()) return
        val appContext = context.applicationContext
        
        // Auto-detect if languageCode not explicitly given: contains Arabic/Persian Unicode chars
        val hasPersianChars = text.any { ch -> ch in '\u0600'..'\u06FF' || ch in '\uFB50'..'\uFDFF' || ch in '\uFE70'..'\uFEFF' }
        val resolvedCode = languageCode?.lowercase(Locale.ROOT) ?: if (hasPersianChars) "fa" else "es"

        val locale = when (resolvedCode) {
            "fa", "fas", "per" -> Locale("fa", "IR")
            "es", "spa" -> Locale("es", "ES")
            "en", "eng" -> Locale.US
            else -> if (hasPersianChars) Locale("fa", "IR") else Locale("es", "ES")
        }

        val existing = tts
        if (existing != null && isInitialized) {
            try {
                existing.language = locale
                existing.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yadin_tts_utterance")
                return
            } catch (_: Exception) {
                // If instance became invalid, fall through to re-init
            }
        }

        synchronized(this) {
            tts?.shutdown()
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    tts?.language = locale
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yadin_tts_utterance")
                } else {
                    isInitialized = false
                }
            }
        }
    }

    fun shutdown() {
        synchronized(this) {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        }
    }
}
