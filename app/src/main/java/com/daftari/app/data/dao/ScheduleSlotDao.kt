package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.ScheduleSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleSlotDao {
    @Query("SELECT * FROM schedule_slots ORDER BY dayOfWeek, startTime")
    fun getAll(): Flow<List<ScheduleSlot>>

    @Insert
    suspend fun insert(slot: ScheduleSlot): Long

    @Delete
    suspend fun delete(slot: ScheduleSlot)
}
