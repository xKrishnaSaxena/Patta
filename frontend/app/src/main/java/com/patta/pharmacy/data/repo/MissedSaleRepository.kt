package com.patta.pharmacy.data.repo

import com.patta.pharmacy.data.local.dao.MissedSaleDao
import com.patta.pharmacy.data.local.entity.MissedSaleEntity
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissedSaleRepository @Inject constructor(
    private val dao: MissedSaleDao,
) {
    fun all(): Flow<List<MissedSaleEntity>> = dao.observeAll()

    fun countSince(since: Long): Flow<Int> = dao.observeCountSince(since)

    /** Log that a customer asked for something we didn't have; repeats just bump the count. */
    suspend fun log(name: String, salt: String = "", medicineId: String? = null) {
        val clean = name.trim()
        if (clean.isBlank()) return
        val now = System.currentTimeMillis()
        val existing = dao.findUnresolvedByName(clean)
        if (existing != null) {
            dao.bumpAsked(existing.id, now)
        } else {
            dao.insert(
                MissedSaleEntity(
                    id = newId(), medicineId = medicineId, medicineName = clean, salt = salt.trim(),
                    timesAsked = 1, lastAskedAt = now, createdAt = now, updatedAt = now,
                )
            )
        }
    }

    suspend fun setResolved(id: String, resolved: Boolean) =
        dao.setResolved(id, resolved, System.currentTimeMillis())
}
