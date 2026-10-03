package com.daftari.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_slots",
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
data class ScheduleSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val dayOfWeek: Int, // 0 = Sunday ... 6 = Saturday
    val startTime: String, // "HH:mm"
    val endTime: String
)
