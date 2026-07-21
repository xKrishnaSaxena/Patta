package com.patta.pharmacy.voice

import android.os.Build

/**
 * Normalizes speech-to-text output to lowercase Latin so downstream matching
 * (Latin medicine names + Latin keywords) works even if the recognizer returns
 * Devanagari. Uses Android's built-in ICU transliterator (free, offline) on
 * API 29+, and is a harmless lowercase pass-through below that.
 */
object SpeechNormalizer {

    private val transliterator by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                android.icu.text.Transliterator.getInstance("Devanagari-Latin; Latin-ASCII")
            else null
        } catch (t: Throwable) {
            null
        }
    }

    fun normalize(raw: String): String {
        val hasDevanagari = raw.any { it in 'ऀ'..'ॿ' }
        val latin = if (hasDevanagari) transliterator?.transliterate(raw) ?: raw else raw
        return latin.lowercase().replace("।", " ").trim()
    }
}
