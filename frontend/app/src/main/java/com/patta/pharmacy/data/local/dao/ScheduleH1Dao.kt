package com.patta.pharmacy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.patta.pharmacy.data.local.entity.ScheduleH1Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleH1Dao {

    @Insert
    suspend fun insertAll(entries: List<ScheduleH1Entry>)

    @Query("SELECT * FROM h1_register ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<ScheduleH1Entry>>
}
