package com.patta.pharmacy.ui.screens.medicine

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.entity.MedicineEntity
import com.patta.pharmacy.data.repo.MedicineRepository
import com.patta.pharmacy.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Plain holder for the Add/Edit-Medicine form. Strings kept raw; parsed on save. */
data class MedicineForm(
    val name: String = "",
    val salt: String = "",
    val company: String = "",
    val packType: String = "Strip",
    val unitsPerPack: Int = 1,
    val allowLooseSale: Boolean = false,
    val hsnCode: String = "",
    val gstPercent: Int = 12,
    val mrp: String = "",
    val purchaseRate: String = "",
    val rackLocation: String = "",
    val reorderLevel: Int = 0,
    val isScheduleH1: Boolean = false,
    val isFridgeItem: Boolean = false,
    val barcode: String = "",
    val defaultDosage: String = "",
    val openingQty: String = "",
    val openingBatchNo: String = "",
    val openingExpiry: String = "",   // MM/YY
) {
    val isValid: Boolean get() = name.isNotBlank()
}

@HiltViewModel
class AddEditMedicineViewModel @Inject constructor(
    private val repository: MedicineRepository,
    private val guide: com.patta.pharmacy.ui.guide.GuideController,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val editingId: String? = savedStateHandle["id"]
    val isEdit: Boolean get() = editingId != null

    // Null until loaded (edit) or immediately ready (add). The screen seeds its
    // local form state from this once it emits.
    private val _initialForm = MutableStateFlow<MedicineForm?>(if (editingId == null) MedicineForm() else null)
    val initialForm: StateFlow<MedicineForm?> = _initialForm.asStateFlow()

    init {
        if (editingId != null) {
            viewModelScope.launch {
                repository.getMedicine(editingId)?.let { _initialForm.value = it.toForm() }
            }
        }
    }

    fun save(form: MedicineForm, onDone: () -> Unit) {
        if (!form.isValid) return
        viewModelScope.launch {
            repository.saveMedicine(
                existingId = editingId,
                name = form.name,
                salt = form.salt,
                company = form.company,
                packType = form.packType,
                unitsPerPack = form.unitsPerPack,
                allowLooseSale = form.allowLooseSale,
                hsnCode = form.hsnCode,
                gstPercent = form.gstPercent,
                mrpPaise = Money.parseRupees(form.mrp),
                purchaseRatePaise = Money.parseRupees(form.purchaseRate),
                rackLocation = form.rackLocation,
                reorderLevel = form.reorderLevel,
                isScheduleH1 = form.isScheduleH1,
                isFridgeItem = form.isFridgeItem,
                barcode = form.barcode.ifBlank { null },
                defaultDosage = form.defaultDosage,
                // Opening stock only applies when creating a brand-new medicine.
                openingQty = if (editingId == null) form.openingQty.toDoubleOrNull() ?: 0.0 else 0.0,
                openingBatchNo = form.openingBatchNo,
                openingExpiryYm = if (editingId == null) parseExpiryYm(form.openingExpiry) else null,
            )
            guide.complete(com.patta.pharmacy.ui.guide.GuideStep.ADD_MEDICINE)
            onDone()
        }
    }

    private fun MedicineEntity.toForm() = MedicineForm(
        name = name,
        salt = salt,
        company = company,
        packType = packType,
        unitsPerPack = unitsPerPack,
        allowLooseSale = allowLooseSale,
        hsnCode = hsnCode,
        gstPercent = gstPercent,
        mrp = if (defaultMrpPaise > 0) (defaultMrpPaise / 100.0).toString() else "",
        purchaseRate = if (purchaseRatePaise > 0) (purchaseRatePaise / 100.0).toString() else "",
        rackLocation = rackLocation,
        reorderLevel = reorderLevel,
        isScheduleH1 = isScheduleH1,
        isFridgeItem = isFridgeItem,
        barcode = barcode.orEmpty(),
        defaultDosage = defaultDosage,
    )

    /** "08/27" -> 202708 ; blank/invalid -> null. */
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
