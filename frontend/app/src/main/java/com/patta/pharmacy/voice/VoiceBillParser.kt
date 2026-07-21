package com.patta.pharmacy.voice

/** One item extracted from a spoken bill utterance. */
data class ParsedItem(
    val nameQuery: String,
    val qty: Int,
    val perTablet: Boolean,
)

/**
 * Turns a Hinglish utterance like "dolo 650 do strip aur pan d ek tablet" into
 * [ParsedItem]s. Numbers come from [HinglishNumbers] (digits + English + Hindi +
 * mishears). Pure and side-effect-free so it can be unit-tested without a device.
 */
object VoiceBillParser {

    private val tabletWords = setOf("tablet", "tablets", "tab", "tabs", "goli", "goliyan", "cap", "capsule", "capsules", "gee")
    private val stripWords = setOf("strip", "strips", "patti", "patta", "pati", "pattiyan", "leaf")
    private val fillerWords = setOf("dena", "de", "chahiye", "aur", "and", "ka", "ki", "please", "ek", "wala")

    fun parse(text: String): List<ParsedItem> {
        val normalized = text.lowercase().replace(",", " aur ")
        return normalized.split(" aur ", " and ", " plus ")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { parseChunk(it) }
    }

    private fun parseChunk(chunk: String): ParsedItem? {
        val tokens = chunk.split(Regex("\\s+")).filter { it.isNotBlank() }
        var qty = 1
        var qtyFound = false
        var perTablet = false
        val nameTokens = mutableListOf<String>()

        for (raw in tokens) {
            val t = raw.trim('.', ',', '?')
            val num = HinglishNumbers.parse(t)
            when {
                // A small number (1–30) is a quantity; a big one (650, 500) is part of the name.
                num != null && !qtyFound && num in 1..30 -> { qty = num; qtyFound = true }
                tabletWords.contains(t) -> perTablet = true
                stripWords.contains(t) -> perTablet = false
                fillerWords.contains(t) && nameTokens.isNotEmpty() -> { /* drop trailing filler */ }
                else -> nameTokens += t
            }
        }
        val name = nameTokens.joinToString(" ").trim()
        if (name.isBlank()) return null
        return ParsedItem(nameQuery = name, qty = qty, perTablet = perTablet)
    }
}
