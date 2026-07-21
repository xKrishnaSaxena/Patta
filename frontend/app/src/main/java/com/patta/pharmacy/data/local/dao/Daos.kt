package com.patta.pharmacy.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patta.pharmacy.data.local.entity.BatchEntity
import com.patta.pharmacy.data.local.entity.MedicineEntity
import com.patta.pharmacy.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

/** A medicine row plus its aggregated stock — used by the Stock List screen. */
data class StockRow(
    @Embedded val medicine: MedicineEntity,
    val totalQty: Double,
    val nearestExpiryYm: Int?,
)

/** An in-stock batch nearing (or past) expiry, with medicine + supplier names. */
data class ExpiryRow(
    @Embedded val batch: BatchEntity,
    val medicineName: String,
    val supplierName: String?,
)

/** A medicine with its FEFO (earliest-expiry, in-stock) batch — used by Billing search. */
data class SellableRow(
    @Embedded val medicine: MedicineEntity,
    val batchId: String,
    val batchNo: String,
    val expiryYm: Int,
    val sellMrpPaise: Long,
    val batchQty: Double,
)

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: MedicineEntity)

    @Update
    suspend fun update(medicine: MedicineEntity)

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: String): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE isDeleted = 0 AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE isDeleted = 0 ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<MedicineEntity>>

    /** Name/salt search across the whole master (with or without stock) — for restock picker. */
    @Query(
        """
        SELECT * FROM medicines
        WHERE isDeleted = 0 AND (name LIKE '%' || :q || '%' OR salt LIKE '%' || :q || '%')
        ORDER BY name COLLATE NOCASE LIMIT 15
        """
    )
    fun searchByName(q: String): Flow<List<MedicineEntity>>

    /** Medicines with total in-stock qty (sum across live batches) and nearest expiry. */
    @Query(
        """
        SELECT m.*,
               IFNULL(SUM(b.qtyPacks), 0) AS totalQty,
               MIN(b.expiryYm) AS nearestExpiryYm
        FROM medicines m
        LEFT JOIN batches b ON b.medicineId = m.id AND b.isDeleted = 0 AND b.qtyPacks > 0
        WHERE m.isDeleted = 0
        GROUP BY m.id
        ORDER BY m.name COLLATE NOCASE
        """
    )
    fun observeStockList(): Flow<List<StockRow>>

    /** Search medicines that have stock, each paired with its FEFO batch. */
    @Query(
        """
        SELECT m.*,
               b.id AS batchId, b.batchNo AS batchNo, b.expiryYm AS expiryYm,
               b.mrpPaise AS sellMrpPaise, b.qtyPacks AS batchQty
        FROM medicines m
        JOIN batches b ON b.id = (
            SELECT b2.id FROM batches b2
            WHERE b2.medicineId = m.id AND b2.isDeleted = 0 AND b2.qtyPacks > 0
            ORDER BY b2.expiryYm ASC LIMIT 1
        )
        WHERE m.isDeleted = 0
          AND (m.name LIKE '%' || :q || '%' OR m.salt LIKE '%' || :q || '%')
        ORDER BY m.name COLLATE NOCASE
        LIMIT 25
        """
    )
    fun searchSellable(q: String): Flow<List<SellableRow>>

    /** One-shot list of every in-stock medicine + its FEFO batch — for voice matching. */
    @Query(
        """
        SELECT m.*,
               b.id AS batchId, b.batchNo AS batchNo, b.expiryYm AS expiryYm,
               b.mrpPaise AS sellMrpPaise, b.qtyPacks AS batchQty
        FROM medicines m
        JOIN batches b ON b.id = (
            SELECT b2.id FROM batches b2
            WHERE b2.medicineId = m.id AND b2.isDeleted = 0 AND b2.qtyPacks > 0
            ORDER BY b2.expiryYm ASC LIMIT 1
        )
        WHERE m.isDeleted = 0
        ORDER BY m.name COLLATE NOCASE
        """
    )
    suspend fun allSellableOnce(): List<SellableRow>

    /** Look up an in-stock medicine by scanned barcode, with its FEFO batch. */
    @Query(
        """
        SELECT m.*,
               b.id AS batchId, b.batchNo AS batchNo, b.expiryYm AS expiryYm,
               b.mrpPaise AS sellMrpPaise, b.qtyPacks AS batchQty
        FROM medicines m
        JOIN batches b ON b.id = (
            SELECT b2.id FROM batches b2
            WHERE b2.medicineId = m.id AND b2.isDeleted = 0 AND b2.qtyPacks > 0
            ORDER BY b2.expiryYm ASC LIMIT 1
        )
        WHERE m.isDeleted = 0 AND m.barcode = :barcode
        LIMIT 1
        """
    )
    suspend fun sellableByBarcode(barcode: String): SellableRow?
}

@Dao
interface BatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: BatchEntity)

    /** Live batches for a medicine, earliest expiry first (FEFO). */
    @Query(
        """
        SELECT * FROM batches
        WHERE medicineId = :medicineId AND isDeleted = 0 AND qtyPacks > 0
        ORDER BY expiryYm ASC
        """
    )
    fun observeByMedicine(medicineId: String): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getById(id: String): BatchEntity?

    @Query("UPDATE batches SET qtyPacks = qtyPacks + :delta, updatedAt = :now WHERE id = :id")
    suspend fun adjustQty(id: String, delta: Double, now: Long)

    /** In-stock batches expiring on/before [maxYm] (yyyyMM), earliest first. */
    @Query(
        """
        SELECT b.*, m.name AS medicineName, s.name AS supplierName
        FROM batches b
        JOIN medicines m ON m.id = b.medicineId
        LEFT JOIN suppliers s ON s.id = b.supplierId
        WHERE b.isDeleted = 0 AND b.qtyPacks > 0 AND b.expiryYm <= :maxYm
        ORDER BY b.expiryYm ASC
        """
    )
    fun observeExpiring(maxYm: Int): Flow<List<ExpiryRow>>
}

@Dao
interface StockMovementDao {

    @Insert
    suspend fun insert(movement: StockMovementEntity)

    @Query("SELECT * FROM stock_movements WHERE medicineId = :medicineId ORDER BY dateTime DESC LIMIT :limit")
    fun observeRecent(medicineId: String, limit: Int = 30): Flow<List<StockMovementEntity>>
}
