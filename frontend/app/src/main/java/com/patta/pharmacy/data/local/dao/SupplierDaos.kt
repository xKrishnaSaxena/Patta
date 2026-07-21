package com.patta.pharmacy.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patta.pharmacy.data.local.entity.PurchaseEntity
import com.patta.pharmacy.data.local.entity.PurchaseItemEntity
import com.patta.pharmacy.data.local.entity.SupplierEntity
import com.patta.pharmacy.data.local.entity.SupplierLedgerEntry
import kotlinx.coroutines.flow.Flow

/** Supplier plus current outstanding (opening + all ledger movements). */
data class SupplierRow(
    @Embedded val supplier: SupplierEntity,
    val outstandingPaise: Long,
)

@Dao
interface SupplierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: SupplierEntity)

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: String): SupplierEntity?

    @Query(
        """
        SELECT s.*,
               (s.openingBalancePaise + IFNULL(SUM(l.amountPaise), 0)) AS outstandingPaise
        FROM suppliers s
        LEFT JOIN supplier_ledger l ON l.supplierId = s.id
        WHERE s.isDeleted = 0
        GROUP BY s.id
        ORDER BY s.name COLLATE NOCASE
        """
    )
    fun observeWithOutstanding(): Flow<List<SupplierRow>>

    @Query(
        """
        SELECT s.*,
               (s.openingBalancePaise + IFNULL(SUM(l.amountPaise), 0)) AS outstandingPaise
        FROM suppliers s
        LEFT JOIN supplier_ledger l ON l.supplierId = s.id
        WHERE s.id = :id
        GROUP BY s.id
        """
    )
    fun observeRow(id: String): Flow<SupplierRow?>

    /** Case-insensitive exact-name match, for find-or-create during purchase. */
    @Query("SELECT * FROM suppliers WHERE isDeleted = 0 AND name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): SupplierEntity?
}

@Dao
interface SupplierLedgerDao {

    @Insert
    suspend fun insert(entry: SupplierLedgerEntry)

    @Query("SELECT * FROM supplier_ledger WHERE supplierId = :supplierId ORDER BY date DESC")
    fun observeForSupplier(supplierId: String): Flow<List<SupplierLedgerEntry>>

    @Query("SELECT runningBalancePaise FROM supplier_ledger WHERE supplierId = :supplierId ORDER BY date DESC LIMIT 1")
    suspend fun latestBalance(supplierId: String): Long?
}

@Dao
interface PurchaseDao {

    @Insert
    suspend fun insert(purchase: PurchaseEntity)

    @Query("SELECT COUNT(*) FROM purchases")
    suspend fun countAll(): Int
}

@Dao
interface PurchaseItemDao {

    @Insert
    suspend fun insertAll(items: List<PurchaseItemEntity>)
}
