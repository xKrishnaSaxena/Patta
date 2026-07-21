package com.patta.pharmacy.ui.screens.khata

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.CustomerRow
import com.patta.pharmacy.data.local.entity.CustomerLedgerEntry
import com.patta.pharmacy.data.repo.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerLedgerViewModel @Inject constructor(
    private val repository: CustomerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val customerId: String = checkNotNull(savedStateHandle["customerId"])

    val row: StateFlow<CustomerRow?> =
        repository.customerRow(customerId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val entries: StateFlow<List<CustomerLedgerEntry>> =
        repository.ledger(customerId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun recordPayment(amountPaise: Long, mode: String) {
        if (amountPaise <= 0) return
        viewModelScope.launch { repository.recordPayment(customerId, amountPaise, mode) }
    }
}
