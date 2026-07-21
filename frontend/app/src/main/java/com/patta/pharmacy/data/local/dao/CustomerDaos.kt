package com.patta.pharmacy.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.patta.pharmacy.data.local.entity.CustomerEntity
import com.patta.pharmacy.data.local.entity.CustomerLedgerEntry
import kotlinx.coroutines.flow.Flow

/** Customer plus current outstanding (opening + all ledger movements). +ve = customer owes shop. */
data class CustomerRow(
    @Embedded val customer: CustomerEntity,
    val outstandingPaise: Long,
)

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity)

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?

    @Query(
        """
        SELECT c.*,
               (c.openingBalancePaise + IFNULL(SUM(l.amountPaise), 0)) AS outstandingPaise
        FROM customers c
        LEFT JOIN customer_ledger l ON l.customerId = c.id
        WHERE c.isDeleted = 0
        GROUP BY c.id
        ORDER BY c.name COLLATE NOCASE
        """
    )
    fun observeWithBalance(): Flow<List<CustomerRow>>

    @Query(
        """
        SELECT c.*,
               (c.openingBalancePaise + IFNULL(SUM(l.amountPaise), 0)) AS outstandingPaise
        FROM customers c
        LEFT JOIN customer_ledger l ON l.customerId = c.id
        WHERE c.id = :id
        GROUP BY c.id
        """
    )
    fun observeRow(id: String): Flow<CustomerRow?>

    @Query(
        """
        SELECT * FROM customers
        WHERE isDeleted = 0 AND (name LIKE '%' || :q || '%' OR phone LIKE '%' || :q || '%')
        ORDER BY name COLLATE NOCASE LIMIT 15
        """
    )
    fun searchByName(q: String): Flow<List<CustomerEntity>>
}

@Dao
interface CustomerLedgerDao {

    @Insert
    suspend fun insert(entry: CustomerLedgerEntry)

    @Query("SELECT * FROM customer_ledger WHERE customerId = :customerId ORDER BY date DESC")
    fun observeForCustomer(customerId: String): Flow<List<CustomerLedgerEntry>>

    @Query("SELECT runningBalancePaise FROM customer_ledger WHERE customerId = :customerId ORDER BY date DESC LIMIT 1")
    suspend fun latestBalance(customerId: String): Long?
}
