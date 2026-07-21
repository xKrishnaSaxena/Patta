package com.patta.pharmacy.voice

import kotlin.math.max

/**
 * Fuzzy string matching so speech-to-text slips ("dulo" → "dolo") still map to
 * the right medicine.
 */
object TextSimilarity {

    fun levenshtein(a: String, b: String): Int {
        val s = a.lowercase()
        val t = b.lowercase()
        val dp = IntArray(t.length + 1) { it }
        for (i in 1..s.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..t.length) {
                val tmp = dp[j]
                dp[j] = if (s[i - 1] == t[j - 1]) prev
                else 1 + minOf(prev, dp[j], dp[j - 1])
                prev = tmp
            }
        }
        return dp[t.length]
    }

    /** 0.0 (different) … 1.0 (identical). */
    fun ratio(a: String, b: String): Double {
        val m = max(a.length, b.length)
        if (m == 0) return 1.0
        return 1.0 - levenshtein(a, b).toDouble() / m
    }

    /**
     * Score [query] against a medicine [name]: best of whole-string ratio, per-word
     * ratio (with a prefix boost), and a phonetic match (Soundex-style) so
     * sound-alikes like "zerodol"/"xerodol" still connect.
     */
    fun score(query: String, name: String): Double {
        val q = query.lowercase().trim()
        val n = name.lowercase().trim()
        if (q.isBlank() || n.isBlank()) return 0.0
        if (n.contains(q) || q.contains(n)) return 0.95
        val whole = ratio(q, n)
        val qFirst = q.split(" ").first()
        val qKey = phoneticKey(qFirst)
        val wordBest = n.split(Regex("\\s+")).maxOfOrNull { word ->
            val r = ratio(qFirst, word)
            val boosted = if (word.startsWith(q.take(3))) r + 0.15 else r
            // Phonetic hit lands in the "confirm" band (0.5–0.8) so the user vets it.
            val phonetic = if (qKey.length >= 2 && qKey == phoneticKey(word)) 0.7 else 0.0
            max(boosted, phonetic)
        } ?: 0.0
        return max(whole, wordBest).coerceAtMost(1.0)
    }

    /** Soundex-style consonant key (vowels dropped) — matches sound-alike spellings. */
    private fun phoneticKey(s: String): String {
        val letters = s.lowercase().filter { it in 'a'..'z' }
        val sb = StringBuilder()
        var prev = '?'
        for (c in letters) {
            val code = when (c) {
                'b', 'f', 'p', 'v', 'w' -> '1'
                'c', 'g', 'j', 'k', 'q', 's', 'x', 'z' -> '2'
                'd', 't' -> '3'
                'l' -> '4'
                'm', 'n' -> '5'
                'r' -> '6'
                else -> '0' // vowels, h, y
            }
            if (code != '0' && code != prev) sb.append(code)
            prev = code
        }
        return sb.toString()
    }
}
