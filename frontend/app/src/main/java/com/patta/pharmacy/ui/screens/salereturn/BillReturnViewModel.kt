package com.patta.pharmacy.ui.screens.salereturn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.BillItemRow
import com.patta.pharmacy.data.repo.SaleReturnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillReturnViewModel @Inject constructor(
    private val repository: SaleReturnRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val billId: String = checkNotNull(savedStateHandle["billId"])

    val items: StateFlow<List<BillItemRow>> =
        repository.billItems(billId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun returnItem(billItemId: String) {
        viewModelScope.launch { repository.returnItem(billItemId) }
    }
}
