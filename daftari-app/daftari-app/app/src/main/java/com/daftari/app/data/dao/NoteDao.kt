package com.daftari.app.data.dao

import androidx.room.*
import com.daftari.app.data.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAll(): Flow<List<Note>>

    @Insert
    suspend fun insert(note: Note): Long

    @Delete
    suspend fun delete(note: Note)
}
