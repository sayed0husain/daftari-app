package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.NotebookCheck
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookCheckDao {
    @Query("SELECT * FROM notebook_checks WHERE subjectId = :subjectId ORDER BY date")
    fun getForSubject(subjectId: Long): Flow<List<NotebookCheck>>

    @Query("SELECT * FROM notebook_checks ORDER BY date")
    fun getAll(): Flow<List<NotebookCheck>>

    @Query("DELETE FROM notebook_checks")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(check: NotebookCheck): Long

    @Update
    suspend fun update(check: NotebookCheck)

    @Delete
    suspend fun delete(check: NotebookCheck)
}
