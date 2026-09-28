package com.daftari.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val weeklyHours: Int = 0,
    val notebookComplete: Boolean = true,
    val copybookComplete: Boolean = true,
    val copybookMissingPagesInput: String = "",
    val copybookRequiredPagesInput: String = "",
    val subjectBehaviorScore: Int = 40
)
