package com.patta.pharmacy.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import com.patta.pharmacy.data.local.entity.BillEntity
import com.patta.pharmacy.data.local.entity.BillItemEntity
import kotlinx.coroutines.flow.Flow

/** Sales grouped by GST rate for a period — powers the GSTR-1 summary. */
data class GstBucket(
    val gstPercent: Int,
    val grossPaise: Long,
)

/** A bill line plus its medicine name — used by the Sale Return screen. */
data class BillItemRow(
    @Embedded val item: BillItemEntity,
    val medicineName: String,
)

/** Today's sale, split by payment mode — powers the Home hero card. */
data class DaySummary(
    val cashPaise: Long,
    val upiPaise: Long,
    val udhaarPaise: Long,
    val billsCount: Int,
) {
    val totalPaise: Long get() = cashPaise + upiPaise + udhaarPaise
}

@Dao
interface BillDao {

    @Insert
    suspend fun insert(bill: BillEntity)

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun countAll(): Int

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getById(id: String): BillEntity?

    @Query("SELECT * FROM bills WHERE isDeleted = 0 ORDER BY dateTime DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<BillEntity>>

    /** Total sale value since [startOfDayMillis] — used by Home / Day Close. */
    @Query("SELECT IFNULL(SUM(totalPaise), 0) FROM bills WHERE dateTime >= :startOfDayMillis AND isDeleted = 0")
    fun observeTotalSince(startOfDayMillis: Long): Flow<Long>

    /** GST collected since [startOfDayMillis]. */
    @Query("SELECT IFNULL(SUM(gstPaise), 0) FROM bills WHERE dateTime >= :startOfDayMillis AND isDeleted = 0")
    fun observeGstSince(startOfDayMillis: Long): Flow<Long>

    @Query(
        """
        SELECT
            IFNULL(SUM(CASE WHEN paymentMode = 'cash'   THEN totalPaise ELSE 0 END), 0) AS cashPaise,
            IFNULL(SUM(CASE WHEN paymentMode = 'upi'    THEN totalPaise ELSE 0 END), 0) AS upiPaise,
            IFNULL(SUM(CASE WHEN paymentMode = 'udhaar' THEN totalPaise ELSE 0 END), 0) AS udhaarPaise,
            COUNT(*) AS billsCount
        FROM bills
        WHERE dateTime >= :startOfDayMillis AND isDeleted = 0
        """
    )
    fun observeDaySummary(startOfDayMillis: Long): Flow<DaySummary>
}

@Dao
interface BillItemDao {

    @Insert
    suspend fun insertAll(items: List<BillItemEntity>)

    @Query("SELECT * FROM bill_items WHERE billId = :billId")
    suspend fun forBill(billId: String): List<BillItemEntity>

    @Query("SELECT * FROM bill_items WHERE id = :id")
    suspend fun getById(id: String): BillItemEntity?

    @Query(
        """
        SELECT bi.*, m.name AS medicineName
        FROM bill_items bi JOIN medicines m ON m.id = bi.medicineId
        WHERE bi.billId = :billId
        """
    )
    fun observeItems(billId: String): Flow<List<BillItemRow>>

    @Query("UPDATE bill_items SET returnedQty = returnedQty + :delta WHERE id = :id")
    suspend fun addReturned(id: String, delta: Double)

    /** Gross sales per GST rate between two timestamps (MRP is GST-inclusive). */
    @Query(
        """
        SELECT bi.gstPercent AS gstPercent, IFNULL(SUM(bi.lineTotalPaise), 0) AS grossPaise
        FROM bill_items bi
        JOIN bills bl ON bl.id = bi.billId
        WHERE bl.dateTime BETWEEN :from AND :to AND bl.isDeleted = 0
        GROUP BY bi.gstPercent
        ORDER BY bi.gstPercent
        """
    )
    suspend fun gstBreakdown(from: Long, to: Long): List<GstBucket>

    /** Estimated gross profit since [start]: (sell rate − batch landed cost) × qty. */
    @Query(
        """
        SELECT IFNULL(SUM((bi.ratePaise - IFNULL(b.landedCostPaise, 0)) * bi.qty), 0)
        FROM bill_items bi
        JOIN bills bl ON bl.id = bi.billId
        LEFT JOIN batches b ON b.id = bi.batchId
        WHERE bl.dateTime >= :start AND bl.isDeleted = 0
        """
    )
    fun observeProfitSince(start: Long): Flow<Double>
}
