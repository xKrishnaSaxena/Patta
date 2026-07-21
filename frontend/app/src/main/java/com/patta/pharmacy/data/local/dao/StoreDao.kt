package com.patta.pharmacy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.patta.pharmacy.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(store: StoreEntity)

    @Query("SELECT * FROM stores WHERE id = :id")
    fun observe(id: String): Flow<StoreEntity?>

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun get(id: String): StoreEntity?
}
