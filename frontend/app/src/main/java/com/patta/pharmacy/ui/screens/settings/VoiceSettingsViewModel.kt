package com.patta.pharmacy.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.voice.VoskEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceSettingsViewModel @Inject constructor(
    private val vosk: VoskEngine,
) : ViewModel() {

    private val _modelReady = MutableStateFlow(vosk.isModelReady())
    val modelReady: StateFlow<Boolean> = _modelReady.asStateFlow()

    private val _downloading = MutableStateFlow(false)
    val downloading: StateFlow<Boolean> = _downloading.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun downloadModel() {
        if (_downloading.value) return
        _downloading.value = true
        _progress.value = 0
        viewModelScope.launch {
            val result = vosk.downloadModel { _progress.value = it }
            _downloading.value = false
            if (result.isSuccess) {
                vosk.ensureLoaded()
                _modelReady.value = true
                _message.value = "Offline model ready! Ab bina net ke chalega."
            } else {
                _message.value = "Download fail: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearMessage() { _message.value = null }
}
