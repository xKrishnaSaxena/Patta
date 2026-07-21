package com.patta.pharmacy.ui.screens.khata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.CustomerRow
import com.patta.pharmacy.data.repo.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val repository: CustomerRepository,
) : ViewModel() {

    val customers: StateFlow<List<CustomerRow>> =
        repository.customers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCustomer(name: String, phone: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.addCustomer(name, phone) }
    }
}
