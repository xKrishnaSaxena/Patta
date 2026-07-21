package com.patta.pharmacy.data.repo

import com.patta.pharmacy.data.local.dao.BillDao
import com.patta.pharmacy.data.local.dao.CustomerDao
import com.patta.pharmacy.data.local.dao.MedicineDao
import com.patta.pharmacy.data.local.dao.SupplierDao
import com.patta.pharmacy.util.Money
import com.patta.pharmacy.voice.QueryIntent
import com.patta.pharmacy.voice.TextSimilarity
import com.patta.pharmacy.voice.VoiceQuery
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceQueryRepository @Inject constructor(
    private val billDao: BillDao,
    private val medicineDao: MedicineDao,
    private val supplierDao: SupplierDao,
    private val customerDao: CustomerDao,
) {
    /** Runs a classified voice query and returns a spoken-style answer. */
    suspend fun answer(q: VoiceQuery): String = when (q.intent) {
        QueryIntent.COLLECTION -> {
            val d = billDao.observeDaySummary(startOfDay()).first()
            "Aaj ka collection ${Money.format(d.totalPaise)}. " +
                "Cash ${Money.formatWhole(d.cashPaise)}, UPI ${Money.formatWhole(d.upiPaise)}, " +
                "Udhaar ${Money.formatWhole(d.udhaarPaise)}."
        }
        QueryIntent.STOCK -> {
            val rows = medicineDao.observeStockList().first()
            val m = rows.maxByOrNull {
                maxOf(TextSimilarity.score(q.name, it.medicine.name), TextSimilarity.score(q.name, it.medicine.salt))
            }
            val score = m?.let { maxOf(TextSimilarity.score(q.name, it.medicine.name), TextSimilarity.score(q.name, it.medicine.salt)) } ?: 0.0
            if (m == null || score < 0.5) "\"${q.name}\" nahi mili."
            else "${m.medicine.name} ka stock ${trim(m.totalQty)} ${m.medicine.packType.lowercase()}."
        }
        QueryIntent.SUPPLIER_DUE -> {
            val rows = supplierDao.observeWithOutstanding().first()
            val s = rows.maxByOrNull { TextSimilarity.score(q.name, it.supplier.name) }
            val score = s?.let { TextSimilarity.score(q.name, it.supplier.name) } ?: 0.0
            if (s == null || score < 0.5) "\"${q.name}\" supplier nahi mila."
            else "${s.supplier.name} ko ${Money.format(s.outstandingPaise)} dena hai."
        }
        QueryIntent.CUSTOMER_DUE -> {
            val rows = customerDao.observeWithBalance().first()
            val c = rows.maxByOrNull { TextSimilarity.score(q.name, it.customer.name) }
            val score = c?.let { TextSimilarity.score(q.name, it.customer.name) } ?: 0.0
            if (c == null || score < 0.5) "\"${q.name}\" customer nahi mila."
            else "${c.customer.name} se ${Money.format(c.outstandingPaise)} lena hai."
        }
        QueryIntent.UNKNOWN ->
            "Samajh nahi aaya. Puchho: aaj ka collection, Dolo ka stock, ya Cipla ka payment."
    }

    private fun startOfDay(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun trim(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
}
