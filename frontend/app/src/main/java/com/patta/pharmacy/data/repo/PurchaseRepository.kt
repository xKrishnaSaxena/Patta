package com.patta.pharmacy.data.repo

import androidx.room.withTransaction
import com.patta.pharmacy.data.local.PattaDatabase
import com.patta.pharmacy.data.local.dao.BatchDao
import com.patta.pharmacy.data.local.dao.MedicineDao
import com.patta.pharmacy.data.local.dao.PurchaseDao
import com.patta.pharmacy.data.local.dao.PurchaseItemDao
import com.patta.pharmacy.data.local.dao.StockMovementDao
import com.patta.pharmacy.data.local.dao.SupplierDao
import com.patta.pharmacy.data.local.dao.SupplierLedgerDao
import com.patta.pharmacy.data.local.dao.SupplierRow
import com.patta.pharmacy.data.local.entity.BatchEntity
import com.patta.pharmacy.data.local.entity.MedicineEntity
import com.patta.pharmacy.data.local.entity.PurchaseEntity
import com.patta.pharmacy.data.local.entity.PurchaseItemEntity
import com.patta.pharmacy.data.local.entity.StockMovementEntity
import com.patta.pharmacy.data.local.entity.SupplierEntity
import com.patta.pharmacy.data.local.entity.SupplierLedgerEntry
import com.patta.pharmacy.util.DEFAULT_STORE_ID
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val db: PattaDatabase,
    private val supplierDao: SupplierDao,
    private val ledgerDao: SupplierLedgerDao,
    private val purchaseDao: PurchaseDao,
    private val purchaseItemDao: PurchaseItemDao,
    private val medicineDao: MedicineDao,
    private val batchDao: BatchDao,
    private val stockMovementDao: StockMovementDao,
) {
    fun suppliers(): Flow<List<SupplierRow>> = supplierDao.observeWithOutstanding()

    fun searchMedicines(query: String): Flow<List<MedicineEntity>> =
        medicineDao.searchByName(query.trim())

    fun supplierRow(id: String): Flow<SupplierRow?> = supplierDao.observeRow(id)

    fun ledger(supplierId: String): Flow<List<SupplierLedgerEntry>> =
        ledgerDao.observeForSupplier(supplierId)

    suspend fun addSupplier(
        name: String,
        phone: String,
        gstin: String,
        creditPeriodDays: Int,
    ): String {
        val now = System.currentTimeMillis()
        val id = newId()
        supplierDao.insert(
            SupplierEntity(
                id = id, name = name.trim(), phone = phone.trim(), gstin = gstin.trim(),
                creditPeriodDays = creditPeriodDays, createdAt = now, updatedAt = now,
            )
        )
        return id
    }

    /** Record a payment to a supplier — reduces the outstanding balance. */
    suspend fun recordPayment(supplierId: String, amountPaise: Long, mode: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            val prev = currentBalance(supplierId)
            ledgerDao.insert(
                SupplierLedgerEntry(
                    id = newId(), supplierId = supplierId, date = now,
                    type = "payment", amountPaise = -amountPaise,
                    runningBalancePaise = prev - amountPaise,
                    note = mode,
                )
            )
        }
    }

    /**
     * Save a purchase invoice atomically: create/reuse each medicine, add a batch
     * (raising stock), record a stock movement, and post the invoice to the
     * supplier ledger.
     */
    suspend fun savePurchase(
        supplierId: String,
        invoiceNo: String,
        invoiceDate: Long,
        lines: List<PurchaseLine>,
    ): String {
        require(lines.isNotEmpty()) { "Koi item nahi hai" }
        val now = System.currentTimeMillis()
        val purchaseId = newId()
        db.withTransaction {
            val supplier = supplierDao.getById(supplierId)
            val dueDate = invoiceDate + supplier!!.creditPeriodDays * 24L * 60 * 60 * 1000

            var taxableTotal = 0L
            var gstTotal = 0L
            var grandTotal = 0L
            val items = mutableListOf<PurchaseItemEntity>()

            for (line in lines) {
                val medicineId = line.medicineId ?: findOrCreateMedicine(line, now)
                val cost = computeLineCost(line)
                val totalPacks = line.qtyPacks + line.freeQtyPacks
                val batchId = newId()

                batchDao.insert(
                    BatchEntity(
                        id = batchId, medicineId = medicineId, batchNo = line.batchNo.ifBlank { "NA" },
                        expiryYm = line.expiryYm, qtyPacks = totalPacks,
                        mrpPaise = line.mrpPaise, purchaseRatePaise = line.ratePaise,
                        landedCostPaise = cost.landedCostPaise, supplierId = supplierId,
                        receivedDate = now, createdAt = now, updatedAt = now,
                    )
                )
                stockMovementDao.insert(
                    StockMovementEntity(
                        id = newId(), medicineId = medicineId, batchId = batchId,
                        changeQty = totalPacks, reason = "purchase", refId = purchaseId, dateTime = now,
                    )
                )
                items += PurchaseItemEntity(
                    id = newId(), purchaseId = purchaseId, medicineId = medicineId, batchId = batchId,
                    batchNo = line.batchNo, expiryYm = line.expiryYm, qtyPacks = line.qtyPacks,
                    freeQtyPacks = line.freeQtyPacks, ratePaise = line.ratePaise,
                    schemeDiscPercent = line.schemeDiscPercent, gstPercent = line.gstPercent,
                    landedCostPaise = cost.landedCostPaise, marginPercent = cost.marginPercent,
                    lineTotalPaise = cost.lineTotalPaise,
                )
                taxableTotal += cost.taxablePaise
                gstTotal += cost.gstPaise
                grandTotal += cost.lineTotalPaise
            }

            purchaseDao.insert(
                PurchaseEntity(
                    id = purchaseId, supplierId = supplierId, invoiceNo = invoiceNo.trim(),
                    invoiceDate = invoiceDate, dueDate = dueDate,
                    subtotalPaise = taxableTotal, gstPaise = gstTotal, discountPaise = 0,
                    totalPaise = grandTotal, createdAt = now, updatedAt = now,
                )
            )
            purchaseItemDao.insertAll(items)

            val prev = currentBalance(supplierId)
            ledgerDao.insert(
                SupplierLedgerEntry(
                    id = newId(), supplierId = supplierId, date = now,
                    type = "purchase", refId = purchaseId, amountPaise = grandTotal,
                    runningBalancePaise = prev + grandTotal,
                    note = "Invoice ${invoiceNo.trim()}",
                )
            )
        }
        return purchaseId
    }

    private suspend fun currentBalance(supplierId: String): Long =
        ledgerDao.latestBalance(supplierId)
            ?: (supplierDao.getById(supplierId)?.openingBalancePaise ?: 0L)

    private suspend fun findOrCreateMedicine(line: PurchaseLine, now: Long): String {
        medicineDao.findByName(line.medicineName)?.let { return it.id }
        val id = newId()
        medicineDao.insert(
            MedicineEntity(
                id = id, name = line.medicineName.trim(), salt = line.salt.trim(),
                gstPercent = line.gstPercent, defaultMrpPaise = line.mrpPaise,
                purchaseRatePaise = line.ratePaise, createdAt = now, updatedAt = now,
            )
        )
        return id
    }
}
