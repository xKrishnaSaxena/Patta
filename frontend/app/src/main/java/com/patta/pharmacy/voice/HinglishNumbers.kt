package com.patta.pharmacy.voice

/**
 * Maps a spoken number token to an Int — digits, English words, Hindi (romanized)
 * words, and the common ways en-IN speech-to-text mangles Hindi numbers
 * ("do" heard as "to"/"two"). Shared by billing + purchase voice parsing.
 */
object HinglishNumbers {

    private val words: Map<String, Int> = buildMap {
        // English 0–20 + tens + hundred
        put("zero", 0); put("one", 1); put("two", 2); put("three", 3); put("four", 4)
        put("five", 5); put("six", 6); put("seven", 7); put("eight", 8); put("nine", 9)
        put("ten", 10); put("eleven", 11); put("twelve", 12); put("thirteen", 13)
        put("fourteen", 14); put("fifteen", 15); put("sixteen", 16); put("seventeen", 17)
        put("eighteen", 18); put("nineteen", 19); put("twenty", 20)
        put("thirty", 30); put("forty", 40); put("fifty", 50); put("sixty", 60)
        put("seventy", 70); put("eighty", 80); put("ninety", 90); put("hundred", 100)
        // Hindi (romanized)
        put("ek", 1); put("do", 2); put("teen", 3); put("tin", 3); put("char", 4)
        put("chaar", 4); put("panch", 5); put("paanch", 5); put("chhe", 6); put("che", 6)
        put("chah", 6); put("saat", 7); put("sat", 7); put("aath", 8); put("ath", 8)
        put("nau", 9); put("das", 10); put("dus", 10); put("gyarah", 11); put("barah", 12)
        put("terah", 13); put("chaudah", 14); put("pandrah", 15); put("solah", 16)
        put("satrah", 17); put("atharah", 18); put("unnis", 19); put("bees", 20)
        put("tees", 30); put("chalis", 40); put("pachas", 50); put("sau", 100)
        // Common en-IN mishears of spoken Hindi numbers
        put("to", 2); put("too", 2); put("dou", 2); put("dho", 2)
        put("for", 4); put("fore", 4); put("tree", 3); put("ate", 8)
        put("van", 1); put("won", 1); put("nao", 9)
    }

    fun parse(token: String): Int? {
        val t = token.trim().lowercase().trim('.', ',', '?')
        t.toIntOrNull()?.let { return it }
        return words[t]
    }
}
