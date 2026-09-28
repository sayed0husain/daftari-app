package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.Homework
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework WHERE subjectId = :subjectId ORDER BY dueDate")
    fun getForSubject(subjectId: Long): Flow<List<Homework>>

    @Query("SELECT * FROM homework ORDER BY dueDate")
    fun getAll(): Flow<List<Homework>>

    @Insert
    suspend fun insert(homework: Homework): Long

    @Update
    suspend fun update(homework: Homework)

    @Delete
    suspend fun delete(homework: Homework)
}
