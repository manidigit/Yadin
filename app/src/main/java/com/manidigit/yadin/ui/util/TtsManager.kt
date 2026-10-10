package com.manidigit.yadin.ui.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

/**
 * Thread-safe singleton for TextToSpeech to eliminate IPC service connection leaks,
 * reduce audio latency, prevent repeated engine recreation, and report language missing issues.
 */
object TtsManager {

    private const val TAG = "TtsManager"

    @Volatile
    private var tts: TextToSpeech? = null

    @Volatile
    private var isInitialized = false

    @Volatile
    private var isInitializing = false

    private var pendingSpeech: PendingRequest? = null

    private data class PendingRequest(
        val text: String,
        val locale: Locale,
        val onError: ((String) -> Unit)?
    )

    fun speak(
        context: Context,
        text: String,
        languageCode: String? = null,
        onError: ((String) -> Unit)? = null
    ) {
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

        synchronized(this) {
            if (isInitialized && tts != null) {
                speakInternal(appContext, text, locale, onError)
                return
            }

            if (isInitializing) {
                // If engine is currently initializing, queue latest request instead of destroying and recreating
                pendingSpeech = PendingRequest(text, locale, onError)
                return
            }

            // Engine not initialized and not initializing -> Start initialization once
            isInitializing = true
            pendingSpeech = PendingRequest(text, locale, onError)

            tts = TextToSpeech(appContext) { status ->
                synchronized(this@TtsManager) {
                    isInitializing = false
                    if (status == TextToSpeech.SUCCESS) {
                        isInitialized = true
                        val request = pendingSpeech
                        pendingSpeech = null
                        if (request != null) {
                            speakInternal(appContext, request.text, request.locale, request.onError)
                        }
                    } else {
                        isInitialized = false
                        val request = pendingSpeech
                        pendingSpeech = null
                        val errMsg = "موتور تبدیل متن به گفتار (TTS) آماده نشد."
                        Log.e(TAG, errMsg)
                        request?.onError?.invoke(errMsg) ?: showToast(appContext, errMsg)
                    }
                }
            }
        }
    }

    private fun speakInternal(
        appContext: Context,
        text: String,
        locale: Locale,
        onError: ((String) -> Unit)?
    ) {
        val engine = tts ?: return
        try {
            val result = engine.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallbackResult = engine.setLanguage(Locale.US)
                val langName = locale.displayLanguage
                val errMsg = "داده‌های صوتی زبان $langName در دستگاه شما نصب نیست."
                Log.w(TAG, "Language $locale not supported. Fallback result: $fallbackResult")

                if (fallbackResult != TextToSpeech.LANG_MISSING_DATA && fallbackResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yadin_tts_utterance")
                }
                onError?.invoke(errMsg) ?: showToast(appContext, errMsg)
            } else {
                val speakResult = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yadin_tts_utterance")
                if (speakResult == TextToSpeech.ERROR) {
                    val errMsg = "خطا در پخش تلفظ صوتی."
                    Log.e(TAG, errMsg)
                    onError?.invoke(errMsg) ?: showToast(appContext, errMsg)
                }
            }
        } catch (e: Exception) {
            val errMsg = "خطا در موتور گفتار: ${e.message}"
            Log.e(TAG, "Error during TTS speak", e)
            onError?.invoke(errMsg) ?: showToast(appContext, errMsg)
        }
    }

    private fun showToast(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            // Ignore UI errors on background threads
        }
    }

    fun shutdown() {
        synchronized(this) {
            try {
                tts?.stop()
                tts?.shutdown()
            } catch (e: Exception) {
                Log.e(TAG, "Error shutting down TTS", e)
            } finally {
                tts = null
                isInitialized = false
                isInitializing = false
                pendingSpeech = null
            }
        }
    }
}
