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

    fun speak(context: Context, text: String, languageCode: String = "es") {
        if (text.isBlank()) return
        val appContext = context.applicationContext
        val locale = if (languageCode.equals("es", ignoreCase = true)) {
            Locale("es", "ES")
        } else {
            Locale.US
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
