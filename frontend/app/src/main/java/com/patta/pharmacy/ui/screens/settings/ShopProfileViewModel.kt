package com.patta.pharmacy.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.repo.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopForm(
    val name: String = "",
    val drugLicenseNo: String = "",
    val gstin: String = "",
    val address: String = "",
    val phone: String = "",
)

@HiltViewModel
class ShopProfileViewModel @Inject constructor(
    private val repository: StoreRepository,
    private val guide: com.patta.pharmacy.ui.guide.GuideController,
) : ViewModel() {

    private val _initial = MutableStateFlow<ShopForm?>(null)
    val initial: StateFlow<ShopForm?> = _initial.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            val s = repository.get()
            _initial.value = ShopForm(
                name = s?.name.orEmpty(),
                drugLicenseNo = s?.drugLicenseNo.orEmpty(),
                gstin = s?.gstin.orEmpty(),
                address = s?.address.orEmpty(),
                phone = s?.phone.orEmpty(),
            )
        }
    }

    fun save(form: ShopForm, onDone: () -> Unit) {
        if (form.name.isBlank()) { _message.value = "Dukaan ka naam daalo"; return }
        viewModelScope.launch {
            repository.save(form.name, form.drugLicenseNo, form.gstin, form.address, form.phone)
            _message.value = "Shop profile save ho gaya"
            guide.complete(com.patta.pharmacy.ui.guide.GuideStep.SHOP_PROFILE)
            onDone()
        }
    }

    fun clearMessage() { _message.value = null }
}
