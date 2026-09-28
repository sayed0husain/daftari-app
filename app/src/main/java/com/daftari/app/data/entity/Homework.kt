package com.daftari.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "homework",
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
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val description: String,
    val dueDate: Long,
    val completed: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderOffsetMinutes: Long = 1440
)
