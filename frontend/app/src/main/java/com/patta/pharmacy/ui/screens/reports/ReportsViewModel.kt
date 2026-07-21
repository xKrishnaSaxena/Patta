package com.patta.pharmacy.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.BillDao
import com.patta.pharmacy.data.local.dao.BillItemDao
import com.patta.pharmacy.data.local.dao.DaySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class ReportsUiState(
    val day: DaySummary = DaySummary(0, 0, 0, 0),
    val profitPaise: Long = 0,
    val gstPaise: Long = 0,
) {
    val avgBillPaise: Long get() = if (day.billsCount > 0) day.totalPaise / day.billsCount else 0
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    billDao: BillDao,
    billItemDao: BillItemDao,
) : ViewModel() {

    private val startOfDay: Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val uiState: StateFlow<ReportsUiState> = combine(
        billDao.observeDaySummary(startOfDay),
        billItemDao.observeProfitSince(startOfDay),
        billDao.observeGstSince(startOfDay),
    ) { day, profit, gst ->
        ReportsUiState(day = day, profitPaise = Math.round(profit), gstPaise = gst)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())
}
