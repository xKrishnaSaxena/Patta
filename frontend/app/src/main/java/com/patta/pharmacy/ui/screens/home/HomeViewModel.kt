package com.patta.pharmacy.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.BillDao
import com.patta.pharmacy.data.local.dao.DaySummary
import com.patta.pharmacy.data.local.dao.StockRow
import com.patta.pharmacy.data.repo.MedicineRepository
import com.patta.pharmacy.data.repo.VoiceQueryRepository
import com.patta.pharmacy.ui.screens.stock.monthsUntilExpiry
import com.patta.pharmacy.voice.QueryIntent
import com.patta.pharmacy.voice.VoiceQueryParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val day: DaySummary = DaySummary(0, 0, 0, 0),
    val lowStock: List<StockRow> = emptyList(),
    val nearExpiryCount: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    billDao: BillDao,
    medicineRepository: MedicineRepository,
    private val voiceQueryRepository: VoiceQueryRepository,
) : ViewModel() {

    private val startOfDay: Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val uiState: StateFlow<HomeUiState> =
        combine(billDao.observeDaySummary(startOfDay), medicineRepository.stockList()) { day, stock ->
            val low = stock.filter { it.medicine.reorderLevel > 0 && it.totalQty <= it.medicine.reorderLevel }
            val nearExpiry = stock.count {
                val m = monthsUntilExpiry(it.nearestExpiryYm)
                m != null && m in 0..3
            }
            HomeUiState(day = day, lowStock = low, nearExpiryCount = nearExpiry)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // Hold-to-talk voice query answer (shown in a dialog + spoken).
    private val _answer = MutableStateFlow<String?>(null)
    val answer: StateFlow<String?> = _answer.asStateFlow()
    private val _speak = MutableStateFlow<String?>(null)
    val speak: StateFlow<String?> = _speak.asStateFlow()

    fun onQuery(alternatives: List<String>) {
        val text = alternatives.firstOrNull { VoiceQueryParser.classify(it).intent != QueryIntent.UNKNOWN }
            ?: alternatives.firstOrNull() ?: return
        viewModelScope.launch {
            val a = voiceQueryRepository.answer(VoiceQueryParser.classify(text))
            _answer.value = a
            _speak.value = a
        }
    }

    fun clearAnswer() { _answer.value = null }
    fun clearSpeak() { _speak.value = null }
}
