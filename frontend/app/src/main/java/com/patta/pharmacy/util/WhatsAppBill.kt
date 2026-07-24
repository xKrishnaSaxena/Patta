package com.patta.pharmacy.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.patta.pharmacy.data.repo.CompletedBill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sends a bill to a customer over WhatsApp using a free wa.me deep link — no API,
 * no cost. Opens the chat for [phone] with the full bill (items + dosage + total)
 * pre-typed; the shopkeeper just taps send.
 */
object WhatsAppBill {

    /** Builds the plain-text bill (WhatsApp has no tables, so keep it line-based). */
    fun buildText(bill: CompletedBill, shopName: String): String = buildString {
        val shop = shopName.ifBlank { "Medical Store" }
        append("*$shop*\n")
        val dateStr = SimpleDateFormat("dd/MM/yy hh:mm a", Locale("en", "IN")).format(Date(bill.dateTimeMillis))
        append("Bill ${bill.billNo} · $dateStr\n")
        append("----------------------\n")
        bill.lines.forEach { l ->
            append("${l.name} — ${l.qty} ${l.unitLabel} — ${Money.format(l.lineTotalPaise)}\n")
            if (l.dosage.isNotBlank()) append("   _${l.dosage}_\n")
        }
        append("----------------------\n")
        append("GST (included): ${Money.format(bill.totals.gstPaise)}\n")
        append("*TOTAL: ${Money.format(bill.totals.totalPaise)}*  (${bill.paymentMode.uppercase()})\n\n")
        append("Dhanyavaad! Get well soon 🙏")
    }

    /**
     * Opens WhatsApp at [rawPhone]'s chat with [text] prefilled. [rawPhone] can be a
     * 10-digit Indian number or already include a country code.
     */
    fun send(context: Context, rawPhone: String, text: String) {
        val digits = rawPhone.filter { it.isDigit() }
        val withCc = when {
            digits.length == 10 -> "91$digits"
            digits.startsWith("0") && digits.length == 11 -> "91" + digits.drop(1)
            else -> digits
        }
        val url = "https://wa.me/$withCc?text=" + Uri.encode(text)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (t: Throwable) {
            Toast.makeText(context, "WhatsApp nahi khula", Toast.LENGTH_SHORT).show()
        }
    }
}
