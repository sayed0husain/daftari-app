package com.daftari.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE
}

@Entity(
    tableName = "attendance",
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
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val date: Long = System.currentTimeMillis(),
    val status: AttendanceStatus
)
