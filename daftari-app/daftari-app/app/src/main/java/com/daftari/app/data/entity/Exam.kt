package com.daftari.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exams",
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
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val name: String,
    val date: Long,
    val notes: String = "",
    val reminderEnabled: Boolean = false,
    val reminderOffsetMinutes: Long = 1440 // default 1 day
)
