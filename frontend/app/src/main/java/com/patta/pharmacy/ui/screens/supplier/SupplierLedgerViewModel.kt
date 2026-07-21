package com.patta.pharmacy.ui.screens.supplier

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.SupplierRow
import com.patta.pharmacy.data.local.entity.SupplierLedgerEntry
import com.patta.pharmacy.data.repo.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierLedgerViewModel @Inject constructor(
    private val repository: PurchaseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val supplierId: String = checkNotNull(savedStateHandle["supplierId"])

    val row: StateFlow<SupplierRow?> =
        repository.supplierRow(supplierId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val entries: StateFlow<List<SupplierLedgerEntry>> =
        repository.ledger(supplierId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun recordPayment(amountPaise: Long, mode: String) {
        if (amountPaise <= 0) return
        viewModelScope.launch { repository.recordPayment(supplierId, amountPaise, mode) }
    }
}
