package com.patta.pharmacy.data.repo

import androidx.room.withTransaction
import com.patta.pharmacy.data.local.PattaDatabase
import com.patta.pharmacy.data.local.dao.BatchDao
import com.patta.pharmacy.data.local.dao.BillDao
import com.patta.pharmacy.data.local.dao.BillItemDao
import com.patta.pharmacy.data.local.dao.BillItemRow
import com.patta.pharmacy.data.local.dao.CustomerDao
import com.patta.pharmacy.data.local.dao.CustomerLedgerDao
import com.patta.pharmacy.data.local.dao.StockMovementDao
import com.patta.pharmacy.data.local.entity.BillEntity
import com.patta.pharmacy.data.local.entity.CustomerLedgerEntry
import com.patta.pharmacy.data.local.entity.StockMovementEntity
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleReturnRepository @Inject constructor(
    private val db: PattaDatabase,
    private val billDao: BillDao,
    private val billItemDao: BillItemDao,
    private val batchDao: BatchDao,
    private val stockMovementDao: StockMovementDao,
    private val customerDao: CustomerDao,
    private val customerLedgerDao: CustomerLedgerDao,
) {
    fun recentBills(): Flow<List<BillEntity>> = billDao.observeRecent()

    fun billItems(billId: String): Flow<List<BillItemRow>> = billItemDao.observeItems(billId)

    /**
     * Return the still-unreturned quantity of a bill line: put the stock back on its
     * batch, log the movement, and — if it was an udhaar bill — credit the customer's
     * khata by the prorated line value.
     */
    suspend fun returnItem(billItemId: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            val item = billItemDao.getById(billItemId) ?: return@withTransaction
            val remaining = item.qty - item.returnedQty
            if (remaining <= 0.0) return@withTransaction

            item.batchId?.let { batchDao.adjustQty(it, remaining, now) }
            stockMovementDao.insert(
                StockMovementEntity(
                    id = newId(), medicineId = item.medicineId, batchId = item.batchId,
                    changeQty = remaining, reason = "saleReturn", refId = item.billId, dateTime = now,
                )
            )
            billItemDao.addReturned(billItemId, remaining)

            // Prorate the stored line total by the fraction being returned.
            val returnValue = if (item.qty > 0) Math.round(item.lineTotalPaise * (remaining / item.qty)) else 0L
            val bill = billDao.getById(item.billId)
            val customerId = bill?.customerId
            if (bill?.paymentMode == "udhaar" && customerId != null && returnValue > 0) {
                val prev = customerLedgerDao.latestBalance(customerId)
                    ?: (customerDao.getById(customerId)?.openingBalancePaise ?: 0L)
                customerLedgerDao.insert(
                    CustomerLedgerEntry(
                        id = newId(), customerId = customerId, date = now,
                        type = "return", refBillId = item.billId, amountPaise = -returnValue,
                        runningBalancePaise = prev - returnValue,
                        note = "Return ${bill.billNo}",
                    )
                )
            }
        }
    }
}
