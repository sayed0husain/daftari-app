package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.Exam
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE subjectId = :subjectId ORDER BY date")
    fun getForSubject(subjectId: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams ORDER BY date")
    fun getAll(): Flow<List<Exam>>

    @Query("SELECT COUNT(*) FROM exams WHERE subjectId = :subjectId")
    suspend fun countForSubject(subjectId: Long): Int

    @Query("DELETE FROM exams")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(exam: Exam): Long

    @Update
    suspend fun update(exam: Exam)

    @Delete
    suspend fun delete(exam: Exam)
}
