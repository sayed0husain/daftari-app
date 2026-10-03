package com.daftari.app.backup

import com.daftari.app.data.entity.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupSubject(
    val id: Long,
    val name: String,
    val colorHex: String,
    val weeklyHours: Int,
    val notebookComplete: Boolean,
    val copybookComplete: Boolean,
    val copybookMissingPagesInput: String,
    val copybookRequiredPagesInput: String,
    val subjectBehaviorScore: Int
)

@Serializable
data class BackupGrade(
    val id: Long, val subjectId: Long, val name: String,
    val scoreObtained: Double, val scoreTotal: Double, val date: Long
)

@Serializable
data class BackupExam(
    val id: Long, val subjectId: Long, val name: String, val date: Long,
    val notes: String, val reminderEnabled: Boolean, val reminderOffsetMinutes: Long
)

@Serializable
data class BackupHomework(
    val id: Long, val subjectId: Long, val description: String, val dueDate: Long,
    val completed: Boolean, val reminderEnabled: Boolean, val reminderOffsetMinutes: Long
)

@Serializable
data class BackupNotebookCheck(
    val id: Long, val subjectId: Long, val description: String, val date: Long,
    val reminderEnabled: Boolean, val reminderOffsetMinutes: Long
)

@Serializable
data class BackupNote(
    val id: Long, val text: String, val date: Long
)

@Serializable
data class BackupBehaviorGrade(
    val id: Long, val periodName: String, val score: Int, val scoreTotal: Int, val date: Long
)

@Serializable
data class BackupScheduleSlot(
    val id: Long, val subjectId: Long, val dayOfWeek: Int, val startTime: String, val endTime: String
)

@Serializable
data class BackupData(
    val version: Int = 1,
    val subjects: List<BackupSubject> = emptyList(),
    val grades: List<BackupGrade> = emptyList(),
    val exams: List<BackupExam> = emptyList(),
    val homework: List<BackupHomework> = emptyList(),
    val notebookChecks: List<BackupNotebookCheck> = emptyList(),
    val behaviorGrades: List<BackupBehaviorGrade> = emptyList(),
    val scheduleSlots: List<BackupScheduleSlot> = emptyList(),
    val notes: List<BackupNote> = emptyList()
)

fun Subject.toBackup() = BackupSubject(
    id, name, colorHex, weeklyHours, notebookComplete, copybookComplete,
    copybookMissingPagesInput, copybookRequiredPagesInput, subjectBehaviorScore
)
fun BackupSubject.toEntity() = Subject(
    id, name, colorHex, weeklyHours, notebookComplete, copybookComplete,
    copybookMissingPagesInput, copybookRequiredPagesInput, subjectBehaviorScore
)

fun Grade.toBackup() = BackupGrade(id, subjectId, name, scoreObtained, scoreTotal, date)
fun BackupGrade.toEntity() = Grade(id, subjectId, name, scoreObtained, scoreTotal, date)

fun Exam.toBackup() = BackupExam(id, subjectId, name, date, notes, reminderEnabled, reminderOffsetMinutes)
fun BackupExam.toEntity() = Exam(id, subjectId, name, date, notes, reminderEnabled, reminderOffsetMinutes)

fun Homework.toBackup() = BackupHomework(id, subjectId, description, dueDate, completed, reminderEnabled, reminderOffsetMinutes)
fun BackupHomework.toEntity() = Homework(id, subjectId, description, dueDate, completed, reminderEnabled, reminderOffsetMinutes)

fun NotebookCheck.toBackup() = BackupNotebookCheck(id, subjectId, description, date, reminderEnabled, reminderOffsetMinutes)
fun BackupNotebookCheck.toEntity() = NotebookCheck(id, subjectId, description, date, reminderEnabled, reminderOffsetMinutes)

fun Note.toBackup() = BackupNote(id, text, date)
fun BackupNote.toEntity() = Note(id, text, date)

fun BehaviorGrade.toBackup() = BackupBehaviorGrade(id, periodName, score, scoreTotal, date)
fun BackupBehaviorGrade.toEntity() = BehaviorGrade(id, periodName, score, scoreTotal, date)

fun ScheduleSlot.toBackup() = BackupScheduleSlot(id, subjectId, dayOfWeek, startTime, endTime)
fun BackupScheduleSlot.toEntity() = ScheduleSlot(id, subjectId, dayOfWeek, startTime, endTime)
