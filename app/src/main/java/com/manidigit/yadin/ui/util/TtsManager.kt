package com.manidigit.yadin.ui.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.manidigit.yadin.R
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

    @Volatile
    private var currentSpeechRate: Float = 1.0f

    private var pendingSpeech: PendingRequest? = null

    private data class PendingRequest(
        val text: String,
        val locale: Locale,
        val speechRate: Float,
        val onError: ((String) -> Unit)?
    )

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 2.0f)
        currentSpeechRate = clamped
        synchronized(this) {
            try {
                if (isInitialized) {
                    tts?.setSpeechRate(clamped)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun speak(
        context: Context,
        text: String,
        languageCode: String? = null,
        speechRate: Float = currentSpeechRate,
        onError: ((String) -> Unit)? = null
    ) {
        if (text.isBlank()) return
        val appContext = context.applicationContext
        val effectiveRate = speechRate.coerceIn(0.5f, 2.0f)
        currentSpeechRate = effectiveRate

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
                speakInternal(appContext, text, locale, effectiveRate, onError)
                return
            }

            if (isInitializing) {
                // If engine is currently initializing, queue latest request instead of destroying and recreating
                pendingSpeech = PendingRequest(text, locale, effectiveRate, onError)
                return
            }

            // Engine not initialized and not initializing -> Start initialization once
            isInitializing = true
            pendingSpeech = PendingRequest(text, locale, effectiveRate, onError)

            tts = TextToSpeech(appContext) { status ->
                synchronized(this@TtsManager) {
                    isInitializing = false
                    if (status == TextToSpeech.SUCCESS) {
                        isInitialized = true
                        val request = pendingSpeech
                        pendingSpeech = null
                        if (request != null) {
                            speakInternal(appContext, request.text, request.locale, request.speechRate, request.onError)
                        }
                    } else {
                        isInitialized = false
                        val request = pendingSpeech
                        pendingSpeech = null
                        val errMsg = appContext.getString(R.string.tts_error_not_ready)
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
        speechRate: Float,
        onError: ((String) -> Unit)?
    ) {
        val engine = tts ?: return
        try {
            engine.setSpeechRate(speechRate.coerceIn(0.5f, 2.0f))
            val result = engine.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val fallbackResult = engine.setLanguage(Locale.US)
                val langName = locale.displayLanguage
                val errMsg = appContext.getString(R.string.tts_error_lang_missing, langName)
                Log.w(TAG, "Language $locale not supported. Fallback result: $fallbackResult")

                if (fallbackResult != TextToSpeech.LANG_MISSING_DATA && fallbackResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yadin_tts_utterance")
                }
                onError?.invoke(errMsg) ?: showToast(appContext, errMsg)
            } else {
                val speakResult = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "yadin_tts_utterance")
                if (speakResult == TextToSpeech.ERROR) {
                    val errMsg = appContext.getString(R.string.tts_error_playback)
                    Log.e(TAG, errMsg)
                    onError?.invoke(errMsg) ?: showToast(appContext, errMsg)
                }
            }
        } catch (e: Exception) {
            val errMsg = appContext.getString(R.string.tts_error_engine, e.message ?: "")
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
