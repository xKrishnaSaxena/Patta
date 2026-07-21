package com.patta.pharmacy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patta.pharmacy.data.local.entity.MissedSaleEntity
import com.patta.pharmacy.data.local.entity.PurchaseOrderEntity
import com.patta.pharmacy.data.local.entity.PurchaseOrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissedSaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MissedSaleEntity)

    @Query("SELECT * FROM missed_sales WHERE resolved = 0 AND medicineName = :name COLLATE NOCASE LIMIT 1")
    suspend fun findUnresolvedByName(name: String): MissedSaleEntity?

    @Query("SELECT * FROM missed_sales ORDER BY resolved ASC, lastAskedAt DESC")
    fun observeAll(): Flow<List<MissedSaleEntity>>

    @Query("SELECT * FROM missed_sales WHERE resolved = 0 ORDER BY timesAsked DESC, lastAskedAt DESC")
    suspend fun unresolvedOnce(): List<MissedSaleEntity>

    @Query("UPDATE missed_sales SET timesAsked = timesAsked + 1, lastAskedAt = :now, updatedAt = :now WHERE id = :id")
    suspend fun bumpAsked(id: String, now: Long)

    @Query("UPDATE missed_sales SET resolved = :resolved, updatedAt = :now WHERE id = :id")
    suspend fun setResolved(id: String, resolved: Boolean, now: Long)

    /** How many times customers asked for out-of-stock items in the last week. */
    @Query("SELECT IFNULL(SUM(timesAsked), 0) FROM missed_sales WHERE lastAskedAt >= :since")
    fun observeCountSince(since: Long): Flow<Int>
}

@Dao
interface PurchaseOrderDao {

    @Insert
    suspend fun insert(po: PurchaseOrderEntity)

    @Query("SELECT * FROM purchase_orders WHERE isDeleted = 0 ORDER BY dateTime DESC LIMIT :limit")
    fun observeRecent(limit: Int = 30): Flow<List<PurchaseOrderEntity>>
}

@Dao
interface PurchaseOrderItemDao {

    @Insert
    suspend fun insertAll(items: List<PurchaseOrderItemEntity>)

    @Query("SELECT * FROM purchase_order_items WHERE poId = :poId")
    suspend fun forPo(poId: String): List<PurchaseOrderItemEntity>
}
