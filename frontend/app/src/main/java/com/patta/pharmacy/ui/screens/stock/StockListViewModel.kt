package com.patta.pharmacy.ui.screens.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.StockRow
import com.patta.pharmacy.data.repo.MedicineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

enum class StockFilter(val label: String) {
    ALL("Sab"),
    LOW("Kam stock"),
    NEAR_EXPIRY("Near expiry"),
    FRIDGE("Fridge"),
}

data class StockUiState(
    val query: String = "",
    val filter: StockFilter = StockFilter.ALL,
    val rows: List<StockRow> = emptyList(),
)

/** Months from now to a yyyyMM expiry (negative = already expired). */
fun monthsUntilExpiry(expiryYm: Int?): Int? {
    if (expiryYm == null) return null
    val exp = YearMonth.of(expiryYm / 100, expiryYm % 100)
    return ChronoUnit.MONTHS.between(YearMonth.now(), exp).toInt()
}

@HiltViewModel
class StockListViewModel @Inject constructor(
    repository: MedicineRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(StockFilter.ALL)

    val uiState: StateFlow<StockUiState> =
        combine(repository.stockList(), query, filter) { rows, q, f ->
            val filtered = rows
                .filter { row ->
                    q.isBlank() ||
                        row.medicine.name.contains(q, ignoreCase = true) ||
                        row.medicine.salt.contains(q, ignoreCase = true)
                }
                .filter { row ->
                    when (f) {
                        StockFilter.ALL -> true
                        StockFilter.LOW -> row.totalQty <= row.medicine.reorderLevel
                        StockFilter.NEAR_EXPIRY -> {
                            val m = monthsUntilExpiry(row.nearestExpiryYm)
                            m != null && m <= 6
                        }
                        StockFilter.FRIDGE -> row.medicine.isFridgeItem
                    }
                }
            StockUiState(query = q, filter = f, rows = filtered)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StockUiState())

    fun onQuery(text: String) { query.value = text }
    fun onFilter(f: StockFilter) { filter.value = f }
}
