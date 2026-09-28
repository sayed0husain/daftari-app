package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.Attendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getForSubject(subjectId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance")
    fun getAll(): Flow<List<Attendance>>

    @Insert
    suspend fun insert(attendance: Attendance): Long

    @Delete
    suspend fun delete(attendance: Attendance)
}
