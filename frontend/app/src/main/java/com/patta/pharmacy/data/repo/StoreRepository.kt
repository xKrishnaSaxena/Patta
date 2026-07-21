package com.patta.pharmacy.data.repo

import com.patta.pharmacy.data.local.dao.StoreDao
import com.patta.pharmacy.data.local.entity.StoreEntity
import com.patta.pharmacy.util.DEFAULT_STORE_ID
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val dao: StoreDao,
) {
    fun observe(): Flow<StoreEntity?> = dao.observe(DEFAULT_STORE_ID)

    suspend fun get(): StoreEntity? = dao.get(DEFAULT_STORE_ID)

    suspend fun save(
        name: String,
        drugLicenseNo: String,
        gstin: String,
        address: String,
        phone: String,
    ) {
        val now = System.currentTimeMillis()
        val existing = dao.get(DEFAULT_STORE_ID)
        dao.upsert(
            StoreEntity(
                id = DEFAULT_STORE_ID,
                name = name.trim(),
                drugLicenseNo = drugLicenseNo.trim(),
                gstin = gstin.trim(),
                address = address.trim(),
                phone = phone.trim(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
        )
    }
}
