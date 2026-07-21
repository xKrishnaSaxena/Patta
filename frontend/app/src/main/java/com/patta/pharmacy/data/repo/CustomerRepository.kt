package com.patta.pharmacy.data.repo

import androidx.room.withTransaction
import com.patta.pharmacy.data.local.PattaDatabase
import com.patta.pharmacy.data.local.dao.CustomerDao
import com.patta.pharmacy.data.local.dao.CustomerLedgerDao
import com.patta.pharmacy.data.local.dao.CustomerRow
import com.patta.pharmacy.data.local.entity.CustomerEntity
import com.patta.pharmacy.data.local.entity.CustomerLedgerEntry
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val db: PattaDatabase,
    private val customerDao: CustomerDao,
    private val ledgerDao: CustomerLedgerDao,
) {
    fun customers(): Flow<List<CustomerRow>> = customerDao.observeWithBalance()

    fun customerRow(id: String): Flow<CustomerRow?> = customerDao.observeRow(id)

    fun search(query: String): Flow<List<CustomerEntity>> = customerDao.searchByName(query.trim())

    fun ledger(customerId: String): Flow<List<CustomerLedgerEntry>> =
        ledgerDao.observeForCustomer(customerId)

    suspend fun addCustomer(name: String, phone: String, address: String = ""): String {
        val now = System.currentTimeMillis()
        val id = newId()
        customerDao.insert(
            CustomerEntity(
                id = id, name = name.trim(), phone = phone.trim(), address = address.trim(),
                createdAt = now, updatedAt = now,
            )
        )
        return id
    }

    /** Customer paid the shop — reduces what they owe. */
    suspend fun recordPayment(customerId: String, amountPaise: Long, mode: String) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            val prev = ledgerDao.latestBalance(customerId)
                ?: (customerDao.getById(customerId)?.openingBalancePaise ?: 0L)
            ledgerDao.insert(
                CustomerLedgerEntry(
                    id = newId(), customerId = customerId, date = now,
                    type = "payment", amountPaise = -amountPaise,
                    paymentMode = mode, runningBalancePaise = prev - amountPaise,
                    note = mode,
                )
            )
        }
    }
}
