package com.patta.pharmacy.ui.screens.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.repo.VoiceQueryRepository
import com.patta.pharmacy.voice.QueryIntent
import com.patta.pharmacy.voice.VoiceQueryParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceAssistantViewModel @Inject constructor(
    private val repository: VoiceQueryRepository,
) : ViewModel() {

    private val _lastQuery = MutableStateFlow("")
    val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()

    private val _answer = MutableStateFlow<String?>(null)
    val answer: StateFlow<String?> = _answer.asStateFlow()

    private val _speak = MutableStateFlow<String?>(null)
    val speak: StateFlow<String?> = _speak.asStateFlow()

    /** From the recognizer's alternatives, prefer one that classifies to a known intent. */
    fun onQuery(alternatives: List<String>) {
        val text = alternatives.firstOrNull { VoiceQueryParser.classify(it).intent != QueryIntent.UNKNOWN }
            ?: alternatives.firstOrNull() ?: return
        _lastQuery.value = text
        viewModelScope.launch {
            val a = repository.answer(VoiceQueryParser.classify(text))
            _answer.value = a
            _speak.value = a
        }
    }

    /** For tapping an example chip (already plain text). */
    fun onQueryText(text: String) = onQuery(listOf(text))

    fun onError(text: String) { _answer.value = text }
    fun clearSpeak() { _speak.value = null }
}
