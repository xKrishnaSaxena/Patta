package com.patta.pharmacy.voice

import android.content.Context

/** Persists the user's chosen speech-recognition language (BCP-47). */
object VoicePrefs {
    private const val PREFS = "patta_voice"
    private const val KEY_LANG = "stt_lang"
    private const val KEY_ENGINE = "stt_engine"   // "google" (online) | "vosk" (offline)

    fun engine(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ENGINE, "google") ?: "google"

    fun setEngine(context: Context, engine: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ENGINE, engine).apply()
    }

    /** Options shown in settings → stored BCP-47 tag. */
    val options = listOf(
        "Hinglish" to "en-IN",   // Latin output, best for English medicine names (default)
        "Hindi" to "hi-IN",      // Devanagari (transliterated to Latin downstream)
        "English" to "en-US",
    )

    fun language(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LANG, "en-IN") ?: "en-IN"

    fun setLanguage(context: Context, bcp47: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_LANG, bcp47).apply()
    }

    fun label(context: Context): String {
        val current = language(context)
        return options.firstOrNull { it.second == current }?.first ?: "Hinglish"
    }
}
