package com.patta.pharmacy.ui.screens.gst

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.BillItemDao
import com.patta.pharmacy.util.Money
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class GstRow(
    val ratePercent: Int,
    val taxablePaise: Long,
    val gstPaise: Long,
    val grossPaise: Long,
)

data class GstUiState(
    val monthLabel: String = "",
    val monthsBack: Int = 0,
    val rows: List<GstRow> = emptyList(),
) {
    val totalTaxable: Long get() = rows.sumOf { it.taxablePaise }
    val totalGst: Long get() = rows.sumOf { it.gstPaise }
    val totalGross: Long get() = rows.sumOf { it.grossPaise }
}

@HiltViewModel
class GstSummaryViewModel @Inject constructor(
    private val billItemDao: BillItemDao,
) : ViewModel() {

    private val _state = MutableStateFlow(GstUiState())
    val state: StateFlow<GstUiState> = _state.asStateFlow()

    init { load(0) }

    fun load(monthsBack: Int) {
        viewModelScope.launch {
            val ym = YearMonth.now().minusMonths(monthsBack.toLong())
            val zone = ZoneId.systemDefault()
            val from = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val to = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            val rows = billItemDao.gstBreakdown(from, to).map { b ->
                // MRP is GST-inclusive, so back the tax out of the gross.
                val gst = Math.round(b.grossPaise * b.gstPercent.toDouble() / (100 + b.gstPercent))
                GstRow(
                    ratePercent = b.gstPercent,
                    taxablePaise = b.grossPaise - gst,
                    gstPaise = gst,
                    grossPaise = b.grossPaise,
                )
            }
            _state.value = GstUiState(
                monthLabel = "${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}",
                monthsBack = monthsBack,
                rows = rows,
            )
        }
    }

    fun shareText(): String {
        val s = _state.value
        return buildString {
            append("*GST Summary — ${s.monthLabel}*\n\n")
            s.rows.forEach {
                append("GST ${it.ratePercent}%  |  Taxable ${Money.format(it.taxablePaise)}  |  Tax ${Money.format(it.gstPaise)}\n")
            }
            append("\nTotal taxable: ${Money.format(s.totalTaxable)}")
            append("\nTotal GST: ${Money.format(s.totalGst)}")
            append("\nTotal sale: ${Money.format(s.totalGross)}")
        }
    }
}
