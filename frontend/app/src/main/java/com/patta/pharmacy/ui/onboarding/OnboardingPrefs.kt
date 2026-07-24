package com.patta.pharmacy.ui.onboarding

import android.content.Context

/** Remembers whether the first-run walkthrough has been shown. */
object OnboardingPrefs {
    private const val PREFS = "patta_onboarding"
    private const val KEY_SEEN = "seen"

    fun seen(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_SEEN, false)

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_SEEN, true).apply()
    }
}
