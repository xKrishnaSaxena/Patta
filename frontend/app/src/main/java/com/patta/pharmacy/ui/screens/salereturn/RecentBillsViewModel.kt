package com.patta.pharmacy.ui.screens.salereturn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.entity.BillEntity
import com.patta.pharmacy.data.repo.SaleReturnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecentBillsViewModel @Inject constructor(
    repository: SaleReturnRepository,
) : ViewModel() {
    val bills: StateFlow<List<BillEntity>> =
        repository.recentBills().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
