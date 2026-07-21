package com.patta.pharmacy.ui.screens.expiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.ExpiryRow
import com.patta.pharmacy.data.repo.ExpiryRepository
import com.patta.pharmacy.ui.screens.stock.monthsUntilExpiry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

enum class ExpiryTab(val label: String, val months: Int) {
    D30("30 din", 1), D60("60 din", 2), D90("90 din", 3)
}

fun ExpiryRow.valuePaise(): Long = Math.round(batch.qtyPacks * batch.landedCostPaise)

data class ExpiryUiState(
    val tab: ExpiryTab = ExpiryTab.D30,
    val counts: Map<ExpiryTab, Int> = emptyMap(),
    val rows: List<ExpiryRow> = emptyList(),
    val valueAtRiskPaise: Long = 0,
)

@HiltViewModel
class ExpiryViewModel @Inject constructor(
    private val repository: ExpiryRepository,
) : ViewModel() {

    private val maxYm: Int = YearMonth.now().plusMonths(3).let { it.year * 100 + it.monthValue }
    private val tab = MutableStateFlow(ExpiryTab.D30)

    val uiState: StateFlow<ExpiryUiState> =
        combine(repository.expiring(maxYm), tab) { all, current ->
            fun within(months: Int) = all.filter {
                val m = monthsUntilExpiry(it.batch.expiryYm)
                m != null && m <= months
            }
            val counts = ExpiryTab.entries.associateWith { within(it.months).size }
            val rows = within(current.months)
            ExpiryUiState(
                tab = current,
                counts = counts,
                rows = rows,
                valueAtRiskPaise = rows.sumOf { it.valuePaise() },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpiryUiState())

    fun onTab(t: ExpiryTab) { tab.value = t }

    fun returnBatch(row: ExpiryRow) {
        viewModelScope.launch { repository.returnBatch(row.batch.id, row.medicineName) }
    }
}
