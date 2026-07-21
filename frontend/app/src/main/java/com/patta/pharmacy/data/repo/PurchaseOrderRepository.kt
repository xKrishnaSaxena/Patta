package com.patta.pharmacy.data.repo

import androidx.room.withTransaction
import com.patta.pharmacy.data.local.PattaDatabase
import com.patta.pharmacy.data.local.dao.MedicineDao
import com.patta.pharmacy.data.local.dao.MissedSaleDao
import com.patta.pharmacy.data.local.dao.PurchaseOrderDao
import com.patta.pharmacy.data.local.dao.PurchaseOrderItemDao
import com.patta.pharmacy.data.local.dao.SupplierDao
import com.patta.pharmacy.data.local.dao.SupplierRow
import com.patta.pharmacy.data.local.entity.PurchaseOrderEntity
import com.patta.pharmacy.data.local.entity.PurchaseOrderItemEntity
import com.patta.pharmacy.util.Money
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil

/** One auto-suggested line for a purchase order. */
data class POSuggestion(
    val id: String,
    val medicineId: String?,
    val name: String,
    val salt: String,
    val reason: String,
    val qty: Int,
    val estRatePaise: Long,
    val missedSaleId: String? = null,
)

@Singleton
class PurchaseOrderRepository @Inject constructor(
    private val db: PattaDatabase,
    private val medicineDao: MedicineDao,
    private val missedSaleDao: MissedSaleDao,
    private val supplierDao: SupplierDao,
    private val poDao: PurchaseOrderDao,
    private val poItemDao: PurchaseOrderItemDao,
) {
    fun suppliers(): Flow<List<SupplierRow>> = supplierDao.observeWithOutstanding()

    /**
     * Builds the order suggestion list: items at/below reorder level, plus anything
     * customers asked for that we didn't have.
     */
    suspend fun suggestions(): List<POSuggestion> {
        val out = mutableListOf<POSuggestion>()

        medicineDao.observeStockList().first()
            .filter { it.medicine.reorderLevel > 0 && it.totalQty <= it.medicine.reorderLevel }
            .forEach { row ->
                val target = row.medicine.reorderLevel * 2
                val qty = ceil(target - row.totalQty).toInt().coerceAtLeast(1)
                out += POSuggestion(
                    id = newId(),
                    medicineId = row.medicine.id,
                    name = row.medicine.name,
                    salt = row.medicine.salt,
                    reason = "${trim(row.totalQty)} ${row.medicine.packType.lowercase()} left",
                    qty = qty,
                    estRatePaise = row.medicine.purchaseRatePaise,
                )
            }

        val alreadyListed = out.mapNotNull { it.medicineId }.toSet()
        missedSaleDao.unresolvedOnce()
            .filter { it.medicineId == null || it.medicineId !in alreadyListed }
            .forEach { ms ->
                out += POSuggestion(
                    id = newId(),
                    medicineId = ms.medicineId,
                    name = ms.medicineName,
                    salt = ms.salt,
                    reason = "${ms.timesAsked} baar maanga",
                    qty = (ms.timesAsked * 2).coerceIn(1, 50),
                    estRatePaise = 0,
                    missedSaleId = ms.id,
                )
            }
        return out
    }

    /** Saves the PO and marks any included missed-sale rows as resolved. */
    suspend fun createPurchaseOrder(
        supplierId: String?,
        supplierName: String,
        lines: List<POSuggestion>,
    ): String {
        require(lines.isNotEmpty()) { "Koi item select nahi kiya" }
        val now = System.currentTimeMillis()
        val poId = newId()
        val value = lines.sumOf { it.qty * it.estRatePaise }
        db.withTransaction {
            poDao.insert(
                PurchaseOrderEntity(
                    id = poId, supplierId = supplierId, supplierName = supplierName,
                    dateTime = now, status = "sent", itemCount = lines.size,
                    estimatedValuePaise = value, createdAt = now, updatedAt = now,
                )
            )
            poItemDao.insertAll(
                lines.map {
                    PurchaseOrderItemEntity(
                        id = newId(), poId = poId, medicineId = it.medicineId,
                        medicineName = it.name, orderQty = it.qty.toDouble(),
                        reason = it.reason, estRatePaise = it.estRatePaise,
                    )
                }
            )
            lines.mapNotNull { it.missedSaleId }.forEach { missedSaleDao.setResolved(it, true, now) }
        }
        return poId
    }

    /** Plain-text order, ready to paste into WhatsApp. */
    fun buildOrderText(supplierName: String, lines: List<POSuggestion>): String = buildString {
        append("*Purchase Order*")
        if (supplierName.isNotBlank()) append(" — $supplierName")
        append("\n\n")
        lines.forEachIndexed { i, l ->
            append("${i + 1}. ${l.name}")
            if (l.salt.isNotBlank()) append(" (${l.salt})")
            append(" — ${l.qty}\n")
        }
        val value = lines.sumOf { it.qty * it.estRatePaise }
        if (value > 0) append("\nApprox: ${Money.format(value)}")
        append("\n\n— Sharma Medical Store")
    }

    private fun trim(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
}
