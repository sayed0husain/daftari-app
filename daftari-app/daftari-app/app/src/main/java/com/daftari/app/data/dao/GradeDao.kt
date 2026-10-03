package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.Grade
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getForSubject(subjectId: Long): Flow<List<Grade>>

    @Query("SELECT * FROM grades")
    fun getAll(): Flow<List<Grade>>

    @Query("DELETE FROM grades")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(grade: Grade): Long

    @Update
    suspend fun update(grade: Grade)

    @Delete
    suspend fun delete(grade: Grade)
}
