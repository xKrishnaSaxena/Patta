package com.patta.pharmacy.data.repo

import com.patta.pharmacy.data.local.dao.BatchDao
import com.patta.pharmacy.data.local.dao.MedicineDao
import com.patta.pharmacy.data.local.dao.StockMovementDao
import com.patta.pharmacy.data.local.dao.StockRow
import com.patta.pharmacy.data.local.entity.BatchEntity
import com.patta.pharmacy.data.local.entity.MedicineEntity
import com.patta.pharmacy.data.local.entity.StockMovementEntity
import com.patta.pharmacy.util.DEFAULT_STORE_ID
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepository @Inject constructor(
    private val medicineDao: MedicineDao,
    private val batchDao: BatchDao,
    private val stockMovementDao: StockMovementDao,
) {
    fun stockList(): Flow<List<StockRow>> = medicineDao.observeStockList()

    fun batchesFor(medicineId: String): Flow<List<BatchEntity>> =
        batchDao.observeByMedicine(medicineId)

    suspend fun getMedicine(id: String): MedicineEntity? = medicineDao.getById(id)

    /**
     * Saves a medicine (insert or update). If [openingQty] > 0, also creates an
     * opening batch and records a stock movement so the item shows real stock.
     */
    suspend fun saveMedicine(
        existingId: String?,
        name: String,
        salt: String,
        company: String,
        packType: String,
        unitsPerPack: Int,
        allowLooseSale: Boolean,
        hsnCode: String,
        gstPercent: Int,
        mrpPaise: Long,
        purchaseRatePaise: Long,
        rackLocation: String,
        reorderLevel: Int,
        isScheduleH1: Boolean,
        isFridgeItem: Boolean,
        barcode: String?,
        openingQty: Double = 0.0,
        openingBatchNo: String = "",
        openingExpiryYm: Int? = null,
    ): String {
        val now = System.currentTimeMillis()
        val id = existingId ?: newId()
        val existing = existingId?.let { medicineDao.getById(it) }

        val medicine = MedicineEntity(
            id = id,
            storeId = DEFAULT_STORE_ID,
            name = name.trim(),
            salt = salt.trim(),
            company = company.trim(),
            packType = packType,
            unitsPerPack = unitsPerPack,
            allowLooseSale = allowLooseSale,
            hsnCode = hsnCode.trim(),
            gstPercent = gstPercent,
            defaultMrpPaise = mrpPaise,
            purchaseRatePaise = purchaseRatePaise,
            rackLocation = rackLocation.trim(),
            reorderLevel = reorderLevel,
            isScheduleH1 = isScheduleH1,
            isFridgeItem = isFridgeItem,
            barcode = barcode?.trim()?.ifBlank { null },
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
        )
        if (existing == null) medicineDao.insert(medicine) else medicineDao.update(medicine)

        if (openingQty > 0.0 && openingExpiryYm != null) {
            val batchId = newId()
            batchDao.insert(
                BatchEntity(
                    id = batchId,
                    medicineId = id,
                    batchNo = openingBatchNo.trim().ifBlank { "OPENING" },
                    expiryYm = openingExpiryYm,
                    qtyPacks = openingQty,
                    mrpPaise = mrpPaise,
                    purchaseRatePaise = purchaseRatePaise,
                    landedCostPaise = purchaseRatePaise,
                    receivedDate = now,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            stockMovementDao.insert(
                StockMovementEntity(
                    id = newId(),
                    medicineId = id,
                    batchId = batchId,
                    changeQty = openingQty,
                    reason = "opening",
                    dateTime = now,
                )
            )
        }
        return id
    }
}
