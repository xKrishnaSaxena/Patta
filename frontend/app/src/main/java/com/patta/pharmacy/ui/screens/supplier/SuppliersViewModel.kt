package com.patta.pharmacy.ui.screens.supplier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.SupplierRow
import com.patta.pharmacy.data.repo.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuppliersViewModel @Inject constructor(
    private val repository: PurchaseRepository,
) : ViewModel() {

    val suppliers: StateFlow<List<SupplierRow>> =
        repository.suppliers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addSupplier(name: String, phone: String, gstin: String, creditDays: Int) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addSupplier(name, phone, gstin, creditDays) }
    }
}
