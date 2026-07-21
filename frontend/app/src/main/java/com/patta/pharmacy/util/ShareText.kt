package com.patta.pharmacy.util

import android.content.Context
import android.content.Intent

/** Opens the Android share sheet with plain text (WhatsApp shows up there). */
object ShareText {
    fun send(context: Context, text: String, title: String = "Bhejo") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
