package com.patta.pharmacy.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Thin wrapper over Android's on-device speech recognition + TTS, tuned for
 * Hinglish (hi-IN). Speech recognition needs a real device with Google's speech
 * service; TTS needs an installed TTS engine.
 */
class VoiceController(context: Context) {

    private val appContext = context.applicationContext
    private val recognizer: SpeechRecognizer? =
        if (SpeechRecognizer.isRecognitionAvailable(appContext))
            SpeechRecognizer.createSpeechRecognizer(appContext)
        else null

    private var tts: TextToSpeech = TextToSpeech(appContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    val isAvailable: Boolean get() = recognizer != null

    fun startListening(
        onResult: (List<String>) -> Unit,   // recognizer's alternatives, normalized to Latin
        onError: (String) -> Unit,
        onListening: (Boolean) -> Unit = {},
    ) {
        val r = recognizer ?: run { onError("Voice available nahi hai is phone pe"); return }
        val lang = VoicePrefs.language(appContext)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // User-selected language (default en-IN = Latin output). Devanagari that
            // slips through is transliterated to Latin by SpeechNormalizer.
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { onListening(true) }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { onListening(false) }
            override fun onError(error: Int) { onListening(false); onError("Sun nahi paya, dobara bolo") }
            override fun onResults(results: Bundle?) {
                onListening(false)
                val alts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.map { SpeechNormalizer.normalize(it) }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    .orEmpty()
                if (alts.isEmpty()) onError("Kuch samajh nahi aaya") else onResult(alts)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        r.startListening(intent)
    }

    /** Finalize the current utterance (call on button release for hold-to-talk). */
    fun stopListening() {
        try { recognizer?.stopListening() } catch (_: Throwable) {}
    }

    fun speak(text: String) {
        if (text.isNotBlank()) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "patta-tts")
    }

    fun destroy() {
        recognizer?.destroy()
        tts.stop()
        tts.shutdown()
    }
}

@Composable
fun rememberVoiceController(): VoiceController {
    val context = LocalContext.current
    val controller = remember { VoiceController(context) }
    DisposableEffect(Unit) { onDispose { controller.destroy() } }
    return controller
}
