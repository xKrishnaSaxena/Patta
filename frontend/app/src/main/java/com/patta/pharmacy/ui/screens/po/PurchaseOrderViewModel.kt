package com.patta.pharmacy.ui.screens.po

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.SupplierRow
import com.patta.pharmacy.data.repo.POSuggestion
import com.patta.pharmacy.data.repo.PurchaseOrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseOrderViewModel @Inject constructor(
    private val repository: PurchaseOrderRepository,
) : ViewModel() {

    private val _lines = MutableStateFlow<List<POSuggestion>>(emptyList())
    val lines: StateFlow<List<POSuggestion>> = _lines.asStateFlow()

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    private val _supplier = MutableStateFlow<SupplierRow?>(null)
    val supplier: StateFlow<SupplierRow?> = _supplier.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val suppliers: StateFlow<List<SupplierRow>> =
        repository.suppliers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val s = repository.suggestions()
            _lines.value = s
            _selected.value = s.map { it.id }.toSet()   // everything pre-checked
        }
    }

    fun toggle(id: String) {
        _selected.value = if (id in _selected.value) _selected.value - id else _selected.value + id
    }

    fun setQty(id: String, qty: Int) {
        _lines.value = _lines.value.map { if (it.id == id) it.copy(qty = qty.coerceAtLeast(1)) else it }
    }

    fun selectSupplier(row: SupplierRow?) { _supplier.value = row }

    fun selectedLines(): List<POSuggestion> = _lines.value.filter { it.id in _selected.value }

    fun estimatedTotalPaise(): Long = selectedLines().sumOf { it.qty * it.estRatePaise }

    fun orderText(): String =
        repository.buildOrderText(_supplier.value?.supplier?.name.orEmpty(), selectedLines())

    /** Persists the PO (and marks the missed-sale rows handled). */
    fun save(onDone: () -> Unit) {
        val lines = selectedLines()
        if (lines.isEmpty()) { _message.value = "Pehle item select karo"; return }
        viewModelScope.launch {
            runCatching {
                repository.createPurchaseOrder(
                    supplierId = _supplier.value?.supplier?.id,
                    supplierName = _supplier.value?.supplier?.name.orEmpty(),
                    lines = lines,
                )
            }.onSuccess { _message.value = "Purchase order save ho gaya"; onDone() }
                .onFailure { _message.value = "Save fail: ${it.message}" }
        }
    }

    fun clearMessage() { _message.value = null }
}
