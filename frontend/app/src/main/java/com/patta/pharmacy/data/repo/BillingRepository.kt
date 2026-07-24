package com.patta.pharmacy.data.repo

import androidx.room.withTransaction
import com.patta.pharmacy.data.local.PattaDatabase
import com.patta.pharmacy.data.local.dao.BatchDao
import com.patta.pharmacy.data.local.dao.BillDao
import com.patta.pharmacy.data.local.dao.BillItemDao
import com.patta.pharmacy.data.local.dao.CustomerDao
import com.patta.pharmacy.data.local.dao.CustomerLedgerDao
import com.patta.pharmacy.data.local.dao.MedicineDao
import com.patta.pharmacy.data.local.dao.SellableRow
import com.patta.pharmacy.data.local.dao.StockMovementDao
import com.patta.pharmacy.data.local.entity.BillEntity
import com.patta.pharmacy.data.local.entity.BillItemEntity
import com.patta.pharmacy.data.local.entity.CustomerLedgerEntry
import com.patta.pharmacy.data.local.entity.StockMovementEntity
import com.patta.pharmacy.util.DEFAULT_STORE_ID
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class BillResult(val billId: String, val billNo: String, val totals: BillTotals)

@Singleton
class BillingRepository @Inject constructor(
    private val db: PattaDatabase,
    private val medicineDao: MedicineDao,
    private val batchDao: BatchDao,
    private val stockMovementDao: StockMovementDao,
    private val billDao: BillDao,
    private val billItemDao: BillItemDao,
    private val customerDao: CustomerDao,
    private val customerLedgerDao: CustomerLedgerDao,
) {
    fun search(query: String): Flow<List<SellableRow>> = medicineDao.searchSellable(query.trim())

    suspend fun allSellable(): List<SellableRow> = medicineDao.allSellableOnce()

    suspend fun byBarcode(barcode: String): SellableRow? = medicineDao.sellableByBarcode(barcode.trim())

    /**
     * Persists a sale atomically: creates the bill + items, decrements each batch,
     * and records a stock movement per line. Everything commits or nothing does.
     */
    suspend fun createBill(
        lines: List<CartLine>,
        paymentMode: String,           // cash / upi / udhaar
        customerId: String? = null,
    ): BillResult {
        require(lines.isNotEmpty()) { "Bill khaali hai" }
        val totals = computeTotals(lines)
        val now = System.currentTimeMillis()
        val billId = newId()
        var billNo = ""

        db.withTransaction {
            billNo = "INV-${billDao.countAll() + 1}"
            billDao.insert(
                BillEntity(
                    id = billId,
                    storeId = DEFAULT_STORE_ID,
                    billNo = billNo,
                    dateTime = now,
                    customerId = customerId,
                    subtotalPaise = totals.subtotalPaise,
                    gstPaise = totals.gstPaise,
                    discountPaise = 0,
                    totalPaise = totals.totalPaise,
                    paymentMode = paymentMode,
                    amountPaidPaise = if (paymentMode == "udhaar") 0 else totals.totalPaise,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            billItemDao.insertAll(
                lines.map { l ->
                    BillItemEntity(
                        id = newId(),
                        billId = billId,
                        medicineId = l.medicineId,
                        batchId = l.batchId,
                        qty = l.packsSold,           // stored in PACKS (fractional for loose sale)
                        unit = l.unitLabel,
                        ratePaise = l.ratePaise,
                        gstPercent = l.gstPercent,
                        lineTotalPaise = l.lineTotalPaise,
                        dosage = l.dosage,
                    )
                }
            )
            lines.forEach { l ->
                batchDao.adjustQty(l.batchId, -l.packsSold, now)
                stockMovementDao.insert(
                    StockMovementEntity(
                        id = newId(),
                        medicineId = l.medicineId,
                        batchId = l.batchId,
                        changeQty = -l.packsSold,
                        reason = "sale",
                        refId = billId,
                        dateTime = now,
                    )
                )
            }
            if (paymentMode == "udhaar" && customerId != null) {
                val prev = customerLedgerDao.latestBalance(customerId)
                    ?: (customerDao.getById(customerId)?.openingBalancePaise ?: 0L)
                customerLedgerDao.insert(
                    CustomerLedgerEntry(
                        id = newId(), customerId = customerId, date = now,
                        type = "bill", refBillId = billId, amountPaise = totals.totalPaise,
                        runningBalancePaise = prev + totals.totalPaise,
                        note = billNo,
                    )
                )
            }
        }
        return BillResult(billId, billNo, totals)
    }
}
