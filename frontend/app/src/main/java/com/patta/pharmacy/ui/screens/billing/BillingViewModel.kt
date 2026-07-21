package com.patta.pharmacy.ui.screens.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patta.pharmacy.data.local.dao.SellableRow
import com.patta.pharmacy.data.local.entity.CustomerEntity
import com.patta.pharmacy.data.repo.BillingRepository
import com.patta.pharmacy.data.repo.CartLine
import com.patta.pharmacy.data.repo.BillTotals
import com.patta.pharmacy.data.repo.CompletedBill
import com.patta.pharmacy.data.repo.CustomerRepository
import com.patta.pharmacy.data.repo.MissedSaleRepository
import com.patta.pharmacy.data.repo.ScheduleH1Repository
import com.patta.pharmacy.data.repo.StoreRepository
import com.patta.pharmacy.data.repo.computeTotals
import com.patta.pharmacy.util.Money
import com.patta.pharmacy.util.newId
import com.patta.pharmacy.voice.TextSimilarity
import com.patta.pharmacy.voice.VoiceBillParser
import com.patta.pharmacy.voice.VoskEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

/** A spoken item whose best match was uncertain — user picks from [candidates]. */
data class PendingMatch(
    val id: String,
    val query: String,
    val qty: Int,
    val perTablet: Boolean,
    val candidates: List<SellableRow>,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class BillingViewModel @Inject constructor(
    private val repository: BillingRepository,
    private val customerRepository: CustomerRepository,
    private val missedSaleRepository: MissedSaleRepository,
    private val scheduleH1Repository: ScheduleH1Repository,
    storeRepository: StoreRepository,
    private val voskEngine: VoskEngine,
) : ViewModel() {

    /** Shop details for the bill header + PDF. */
    val store: StateFlow<com.patta.pharmacy.data.local.entity.StoreEntity?> =
        storeRepository.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<SellableRow>> = _query
        .debounce(200)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()

    val totals: StateFlow<BillTotals> = _cart
        .map { computeTotals(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), computeTotals(emptyList()))

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Text for the TTS engine to read aloud (voice confirmations). Cleared after speaking.
    private val _speak = MutableStateFlow<String?>(null)
    val speak: StateFlow<String?> = _speak.asStateFlow()

    // Uncertain voice matches awaiting a tap-to-confirm.
    private val _pending = MutableStateFlow<List<PendingMatch>>(emptyList())
    val pending: StateFlow<List<PendingMatch>> = _pending.asStateFlow()

    // The just-saved bill, for the "share PDF" dialog. Cleared on dismiss.
    private val _lastBill = MutableStateFlow<CompletedBill?>(null)
    val lastBill: StateFlow<CompletedBill?> = _lastBill.asStateFlow()

    // Customer picker for udhaar bills.
    private val _customerQuery = MutableStateFlow("")
    val customerResults: StateFlow<List<CustomerEntity>> = _customerQuery
        .debounce(200)
        .flatMapLatest { q -> if (q.isBlank()) flowOf(emptyList()) else customerRepository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onCustomerQuery(text: String) { _customerQuery.value = text }

    fun onQuery(text: String) { _query.value = text }

    fun addToCart(row: SellableRow) {
        addRow(row, qty = 1, perTablet = false)
        _query.value = ""   // collapse search back to the cart after adding
    }

    private fun addRow(row: SellableRow, qty: Int, perTablet: Boolean) {
        val loose = perTablet && row.medicine.allowLooseSale
        val idx = _cart.value.indexOfFirst { it.batchId == row.batchId && it.perTablet == loose }
        _cart.value = if (idx >= 0) {
            _cart.value.mapIndexed { i, line ->
                if (i == idx) line.copy(qty = (line.qty + qty).coerceAtMost(line.maxQty.coerceAtLeast(1))) else line
            }
        } else {
            val base = CartLine(
                medicineId = row.medicine.id,
                name = row.medicine.name,
                salt = row.medicine.salt,
                batchId = row.batchId,
                batchNo = row.batchNo,
                expiryYm = row.expiryYm,
                packType = row.medicine.packType,
                gstPercent = row.medicine.gstPercent,
                mrpPaise = row.sellMrpPaise,
                unitsPerPack = row.medicine.unitsPerPack,
                allowLooseSale = row.medicine.allowLooseSale,
                isScheduleH1 = row.medicine.isScheduleH1,
                perTablet = loose,
                qty = 1,
                availablePacks = row.batchQty,
            )
            _cart.value + base.copy(qty = qty.coerceIn(1, base.maxQty.coerceAtLeast(1)))
        }
    }

    /** Handle spoken bill line(s): pick the best of the recognizer's alternatives,
     *  parse → fuzzy-match medicines → add → speak back. */
    fun onVoiceResult(alternatives: List<String>) {
        viewModelScope.launch {
            val stock = repository.allSellable()
            fun scoreOf(query: String) = stock.maxOfOrNull {
                maxOf(TextSimilarity.score(query, it.medicine.name), TextSimilarity.score(query, it.medicine.salt))
            } ?: 0.0
            // Choose the alternative whose parsed items best match real stock.
            val best = alternatives
                .map { it to VoiceBillParser.parse(it) }
                .maxByOrNull { (_, items) -> if (items.isEmpty()) -1.0 else items.map { scoreOf(it.nameQuery) }.average() }
            val parsed = best?.second ?: emptyList()
            if (parsed.isEmpty()) {
                val m = "Samajh nahi aaya: \"${alternatives.firstOrNull().orEmpty()}\""
                _message.value = m; _speak.value = m
                return@launch
            }
            val added = mutableListOf<String>()
            val missed = mutableListOf<String>()
            val pendingList = mutableListOf<PendingMatch>()
            parsed.forEach { p ->
                val ranked = stock
                    .map { it to maxOf(TextSimilarity.score(p.nameQuery, it.medicine.name), TextSimilarity.score(p.nameQuery, it.medicine.salt)) }
                    .sortedByDescending { it.second }
                val top = ranked.firstOrNull()
                when {
                    top == null || top.second < 0.5 -> missed += p.nameQuery
                    top.second >= 0.8 -> {
                        addRow(top.first, p.qty, p.perTablet)
                        added += "${top.first.medicine.name} ${p.qty} ${if (p.perTablet) "tablet" else "strip"}"
                    }
                    else -> pendingList += PendingMatch(newId(), p.nameQuery, p.qty, p.perTablet, ranked.take(3).map { it.first })
                }
            }
            _query.value = ""
            _pending.value = pendingList
            val summary = buildString {
                if (added.isNotEmpty()) append(added.joinToString(", ") + " add kiya. ")
                if (missed.isNotEmpty()) append(missed.joinToString(", ") + " nahi mili. ")
                if (pendingList.isNotEmpty()) append("Kuch confirm karo.")
            }.trim()
            _message.value = summary
            _speak.value = summary
        }
    }

    fun confirmPending(id: String, row: SellableRow) {
        val pm = _pending.value.firstOrNull { it.id == id } ?: return
        addRow(row, pm.qty, pm.perTablet)
        _pending.value = _pending.value.filter { it.id != id }
    }

    fun skipPending(id: String) { _pending.value = _pending.value.filter { it.id != id } }

    fun clearSpeak() { _speak.value = null }

    fun onVoiceError(text: String) { _message.value = text }

    /** Scanned a barcode — add that medicine straight to the cart. */
    fun addByBarcode(code: String) {
        viewModelScope.launch {
            val row = repository.byBarcode(code)
            if (row == null) {
                _message.value = "Is barcode ki dawai nahi mili — pehle medicine mein barcode save karo"
            } else {
                addRow(row, 1, false)
                _message.value = "${row.medicine.name} add kiya"
            }
        }
    }

    /** Customer asked for something we don't stock — log it for the next order. */
    fun logMissedSale(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            missedSaleRepository.log(name)
            _query.value = ""
            _message.value = "\"$name\" missed sale mein note ho gaya"
        }
    }

    /** Offline (Vosk) voice for billing — falls back with a message if model isn't ready. */
    fun startOfflineVoice(onError: (String) -> Unit) {
        viewModelScope.launch {
            if (!voskEngine.ensureLoaded()) {
                onError("Offline model pehle download karo (More → Voice & Language)")
                return@launch
            }
            voskEngine.startListening(onResult = { onVoiceResult(it) }, onError = onError)
        }
    }

    fun changeQty(index: Int, newQty: Int) {
        _cart.value = _cart.value.mapIndexed { i, line ->
            if (i != index) line
            else {
                val max = line.maxQty.coerceAtLeast(1)
                if (newQty > max) { _message.value = "Sirf $max ${line.unitLabel} stock hai"; line.copy(qty = max) }
                else line.copy(qty = newQty.coerceAtLeast(1))
            }
        }
    }

    /** Toggle a line between whole strip and loose tablet selling. */
    fun changeUnit(index: Int, perTablet: Boolean) {
        _cart.value = _cart.value.mapIndexed { i, line ->
            if (i != index) line else line.copy(perTablet = perTablet, qty = 1)
        }
    }

    fun removeLine(index: Int) {
        _cart.value = _cart.value.filterIndexed { i, _ -> i != index }
    }

    /** Cash / UPI — no customer needed. */
    fun checkout(paymentMode: String) = checkoutInternal(paymentMode, null)

    /** Udhaar to an existing customer. */
    fun checkoutUdhaar(customerId: String) {
        _customerQuery.value = ""
        checkoutInternal("udhaar", customerId)
    }

    /** Udhaar to a brand-new customer created on the spot. */
    fun addCustomerAndCheckoutUdhaar(name: String, phone: String) {
        if (name.isBlank()) { _message.value = "Customer ka naam daalo"; return }
        viewModelScope.launch {
            val id = customerRepository.addCustomer(name, phone)
            _customerQuery.value = ""
            checkoutInternal("udhaar", id)
        }
    }

    private fun checkoutInternal(paymentMode: String, customerId: String?) {
        val lines = _cart.value
        if (lines.isEmpty()) { _message.value = "Pehle dawai add karo"; return }
        viewModelScope.launch {
            runCatching { repository.createBill(lines, paymentMode, customerId) }
                .onSuccess { res ->
                    _lastBill.value = CompletedBill(
                        billNo = res.billNo,
                        dateTimeMillis = System.currentTimeMillis(),
                        paymentMode = paymentMode,
                        lines = lines,
                        totals = res.totals,
                    )
                    _cart.value = emptyList()
                }
                .onFailure { _message.value = "Bill save nahi hua: ${it.message}" }
        }
    }

    fun clearLastBill() { _lastBill.value = null }

    /** Records the Schedule-H1 lines of a saved bill into the legal register. */
    fun saveH1Record(bill: CompletedBill, patient: String, doctor: String) {
        viewModelScope.launch {
            scheduleH1Repository.recordForBill(bill.billNo, bill.lines, patient, doctor)
            _message.value = "H1 register mein note ho gaya"
        }
    }

    fun clearMessage() { _message.value = null }
}
