package com.patta.pharmacy.ui.screens.purchase

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.SupplierRow
import com.patta.pharmacy.data.local.entity.MedicineEntity
import com.patta.pharmacy.data.repo.PurchaseLine
import com.patta.pharmacy.data.repo.PurchaseRepository
import com.patta.pharmacy.data.repo.computeLineCost
import com.patta.pharmacy.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PurchaseEntryViewModel @Inject constructor(
    private val repository: PurchaseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val supplierId: String = checkNotNull(savedStateHandle["supplierId"])

    val supplier: StateFlow<SupplierRow?> =
        repository.supplierRow(supplierId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _invoiceNo = MutableStateFlow("")
    val invoiceNo: StateFlow<String> = _invoiceNo.asStateFlow()

    // Restock picker: suggest existing medicines as the name is typed so details
    // auto-fill and no duplicate master is created.
    private val _nameQuery = MutableStateFlow("")
    val suggestions: StateFlow<List<MedicineEntity>> = _nameQuery
        .debounce(200)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else repository.searchMedicines(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var pickedMedicineId: String? = null

    fun onNameChanged(text: String) { _nameQuery.value = text; pickedMedicineId = null }
    fun onPickMedicine(id: String) { pickedMedicineId = id; _nameQuery.value = "" }

    private val _lines = MutableStateFlow<List<PurchaseLine>>(emptyList())
    val lines: StateFlow<List<PurchaseLine>> = _lines.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val invoiceTotalPaise: StateFlow<Long> = _lines
        .map { lines -> lines.sumOf { computeLineCost(it).lineTotalPaise } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun onInvoiceNo(text: String) { _invoiceNo.value = text }

    fun addLine(
        name: String, salt: String, batchNo: String, expiryMmYy: String,
        qty: String, free: String, rate: String, scheme: String, gst: Int, mrp: String,
    ) {
        if (name.isBlank()) { _message.value = "Dawai ka naam daalo"; return }
        val expiryYm = parseExpiryYm(expiryMmYy)
        if (expiryYm == null) { _message.value = "Expiry MM/YY sahi daalo"; return }
        _lines.value = _lines.value + PurchaseLine(
            medicineId = pickedMedicineId,
            medicineName = name.trim(),
            salt = salt.trim(),
            batchNo = batchNo.trim(),
            expiryYm = expiryYm,
            qtyPacks = qty.toDoubleOrNull() ?: 0.0,
            freeQtyPacks = free.toDoubleOrNull() ?: 0.0,
            ratePaise = Money.parseRupees(rate),
            schemeDiscPercent = scheme.toDoubleOrNull() ?: 0.0,
            gstPercent = gst,
            mrpPaise = Money.parseRupees(mrp),
        )
        pickedMedicineId = null
        _nameQuery.value = ""
    }

    fun removeLine(index: Int) {
        _lines.value = _lines.value.filterIndexed { i, _ -> i != index }
    }

    fun save(onDone: () -> Unit) {
        val lines = _lines.value
        if (lines.isEmpty()) { _message.value = "Pehle item add karo"; return }
        viewModelScope.launch {
            runCatching {
                repository.savePurchase(supplierId, _invoiceNo.value, System.currentTimeMillis(), lines)
            }.onSuccess {
                _message.value = "Purchase save ho gaya"
                onDone()
            }.onFailure { _message.value = "Save nahi hua: ${it.message}" }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun parseExpiryYm(mmYy: String): Int? {
        val parts = mmYy.trim().split("/", "-")
        if (parts.size != 2) return null
        val mm = parts[0].toIntOrNull() ?: return null
        val yy = parts[1].toIntOrNull() ?: return null
        if (mm !in 1..12) return null
        val year = if (yy < 100) 2000 + yy else yy
        return year * 100 + mm
    }
}
