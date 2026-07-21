package com.patta.pharmacy.ui.screens.missedsale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.entity.MissedSaleEntity
import com.patta.pharmacy.data.repo.MissedSaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissedSalesViewModel @Inject constructor(
    private val repository: MissedSaleRepository,
) : ViewModel() {

    private val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000

    val items: StateFlow<List<MissedSaleEntity>> =
        repository.all().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weekCount: StateFlow<Int> =
        repository.countSince(weekAgo).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun log(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.log(name) }
    }

    fun setResolved(id: String, resolved: Boolean) {
        viewModelScope.launch { repository.setResolved(id, resolved) }
    }
}
