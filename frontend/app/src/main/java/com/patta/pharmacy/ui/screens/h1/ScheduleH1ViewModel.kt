package com.patta.pharmacy.ui.screens.h1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.entity.ScheduleH1Entry
import com.patta.pharmacy.data.repo.ScheduleH1Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleH1ViewModel @Inject constructor(
    private val repository: ScheduleH1Repository,
) : ViewModel() {

    val entries: StateFlow<List<ScheduleH1Entry>> =
        repository.recent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addManual(medicine: String, qty: String, patient: String, doctor: String) {
        if (medicine.isBlank() || patient.isBlank()) return
        viewModelScope.launch {
            repository.addManual(medicine, qty.toDoubleOrNull() ?: 1.0, patient, doctor)
        }
    }
}
