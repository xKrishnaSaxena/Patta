package com.patta.pharmacy.util

import java.text.NumberFormat
import java.util.Locale

/**
 * All money in Patta is stored as PAISE (Long) — never Float/Double — so ledger
 * math never drifts from rounding. Convert to rupees only for display.
 */
object Money {

    private val inFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    /** 152340L -> "₹1,523.40" (Indian grouping). */
    fun format(paise: Long): String = "₹" + inFormat.format(paise / 100.0)

    /** Whole-rupee display, no decimals: 152300L -> "₹1,523". */
    fun formatWhole(paise: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale("en", "IN"))
        nf.maximumFractionDigits = 0
        return "₹" + nf.format(paise / 100.0)
    }

    fun rupeesToPaise(rupees: Double): Long = Math.round(rupees * 100)

    fun parseRupees(text: String): Long =
        rupeesToPaise(text.trim().removePrefix("₹").replace(",", "").toDoubleOrNull() ?: 0.0)
}
