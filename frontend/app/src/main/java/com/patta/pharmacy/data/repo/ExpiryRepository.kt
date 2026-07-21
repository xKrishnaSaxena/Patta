package com.patta.pharmacy.data.repo

import androidx.room.withTransaction
import com.patta.pharmacy.data.local.PattaDatabase
import com.patta.pharmacy.data.local.dao.BatchDao
import com.patta.pharmacy.data.local.dao.ExpiryRow
import com.patta.pharmacy.data.local.dao.StockMovementDao
import com.patta.pharmacy.data.local.dao.SupplierDao
import com.patta.pharmacy.data.local.dao.SupplierLedgerDao
import com.patta.pharmacy.data.local.entity.StockMovementEntity
import com.patta.pharmacy.data.local.entity.SupplierLedgerEntry
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpiryRepository @Inject constructor(
    private val db: PattaDatabase,
    private val batchDao: BatchDao,
    private val stockMovementDao: StockMovementDao,
    private val supplierDao: SupplierDao,
    private val supplierLedgerDao: SupplierLedgerDao,
) {
    fun expiring(maxYm: Int): Flow<List<ExpiryRow>> = batchDao.observeExpiring(maxYm)

    /**
     * Return a whole batch to its supplier: zero the stock, log the movement, and
     * post a credit note to the supplier ledger (reducing what the shop owes).
     */
    suspend fun returnBatch(batchId: String, medicineName: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            val batch = batchDao.getById(batchId) ?: return@withTransaction
            if (batch.qtyPacks <= 0) return@withTransaction
            val value = Math.round(batch.qtyPacks * batch.landedCostPaise)

            batchDao.adjustQty(batchId, -batch.qtyPacks, now)
            stockMovementDao.insert(
                StockMovementEntity(
                    id = newId(), medicineId = batch.medicineId, batchId = batchId,
                    changeQty = -batch.qtyPacks, reason = "return", refId = batchId, dateTime = now,
                )
            )
            val supplierId = batch.supplierId
            if (supplierId != null && value > 0) {
                val prev = supplierLedgerDao.latestBalance(supplierId)
                    ?: (supplierDao.getById(supplierId)?.openingBalancePaise ?: 0L)
                supplierLedgerDao.insert(
                    SupplierLedgerEntry(
                        id = newId(), supplierId = supplierId, date = now,
                        type = "creditNote", refId = batchId, amountPaise = -value,
                        runningBalancePaise = prev - value,
                        note = "Expiry return · $medicineName",
                    )
                )
            }
        }
    }
}
