package com.patta.pharmacy.voice

/** Fields pulled from a spoken purchase line; nulls are left for the user to fill. */
data class ParsedPurchase(
    val batch: String? = null,
    val expiry: String? = null,   // MM/YY
    val qty: String? = null,
    val free: String? = null,
    val rate: String? = null,
    val mrp: String? = null,
)

/** Parses "batch A123 expiry 08/27 qty 50 free 5 rate 18 mrp 30" into fields. */
object VoicePurchaseParser {

    private val expiryRegex = Regex("\\d{1,2}[/-]\\d{2,4}")

    fun parse(text: String): ParsedPurchase {
        val t = text.lowercase().replace("-", "/")
        val tokens = t.split(Regex("\\s+")).filter { it.isNotBlank() }

        fun after(vararg keys: String): String? {
            for (i in tokens.indices) {
                if (tokens[i] in keys && i + 1 < tokens.size) return tokens[i + 1]
            }
            return null
        }
        fun number(vararg keys: String): String? {
            val v = after(*keys) ?: return null
            v.filter { it.isDigit() || it == '.' }.ifBlank { null }?.let { return it }
            return HinglishNumbers.parse(v)?.toString()   // "fifty" -> "50"
        }

        val expiry = expiryRegex.find(t)?.value ?: after("expiry", "exp")
        return ParsedPurchase(
            batch = after("batch")?.uppercase(),
            expiry = expiry,
            qty = number("qty", "quantity"),
            free = number("free"),
            rate = number("rate", "price"),
            mrp = number("mrp"),
        )
    }
}
