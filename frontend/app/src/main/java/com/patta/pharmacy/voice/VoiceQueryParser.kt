package com.patta.pharmacy.voice

enum class QueryIntent { COLLECTION, STOCK, SUPPLIER_DUE, CUSTOMER_DUE, UNKNOWN }

data class VoiceQuery(val intent: QueryIntent, val name: String)

/** Classifies a spoken question and pulls out the entity name (medicine/supplier/customer). */
object VoiceQueryParser {

    private val stopWords = setOf(
        "ka", "ki", "ke", "kitna", "kitni", "hai", "hain", "stock", "payment", "dena", "denge",
        "baaki", "baki", "udhaar", "khata", "lena", "collection", "sale", "supplier", "bill",
        "aaj", "batao", "bolo", "mera", "meri", "kya", "maal", "bacha", "kamai", "bikri",
        "becha", "ko", "se", "total", "kul", "kitne",
    )

    fun classify(text: String): VoiceQuery {
        val t = text.lowercase()
        val base = when {
            listOf("collection", "colletion", "connection", "kalekshan", "sale", "bikri", "becha", "kamai", "total").any { t.contains(it) } -> QueryIntent.COLLECTION
            listOf("udhaar", "udhar", "khata", "lena").any { t.contains(it) } -> QueryIntent.CUSTOMER_DUE
            listOf("payment", "peyment", "dena", "baaki", "baki", "supplier", "distributor").any { t.contains(it) } -> QueryIntent.SUPPLIER_DUE
            listOf("stock", "stok", "kitna", "kitni", "maal", "bacha").any { t.contains(it) } -> QueryIntent.STOCK
            else -> QueryIntent.UNKNOWN
        }
        val name = t.split(Regex("\\s+"))
            .map { it.trim('.', ',', '?') }
            .filter { it.isNotBlank() && it !in stopWords }
            .joinToString(" ")
            .trim()
        // If no keyword matched but a name was spoken, assume a stock check
        // ("Dolo?" / "Dolo hai kya?") — the most common bare query.
        val intent = if (base == QueryIntent.UNKNOWN && name.isNotBlank()) QueryIntent.STOCK else base
        return VoiceQuery(intent, name)
    }
}
