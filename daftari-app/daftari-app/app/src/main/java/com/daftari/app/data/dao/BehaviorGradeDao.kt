package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.BehaviorGrade
import kotlinx.coroutines.flow.Flow

@Dao
interface BehaviorGradeDao {
    @Query("SELECT * FROM behavior_grades ORDER BY date DESC")
    fun getAll(): Flow<List<BehaviorGrade>>

    @Query("DELETE FROM behavior_grades")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(behaviorGrade: BehaviorGrade): Long

    @Delete
    suspend fun delete(behaviorGrade: BehaviorGrade)
}
