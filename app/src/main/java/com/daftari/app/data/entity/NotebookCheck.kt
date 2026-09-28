package com.daftari.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notebook_checks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class NotebookCheck(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val description: String,
    val date: Long,
    val reminderEnabled: Boolean = false,
    val reminderOffsetMinutes: Long = 1440
)
