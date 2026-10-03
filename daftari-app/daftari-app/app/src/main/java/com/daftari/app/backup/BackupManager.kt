package com.daftari.app.backup

import android.content.Context
import android.net.Uri
import com.daftari.app.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BackupManager(
    private val context: Context,
    private val repository: AppRepository
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun buildBackupJson(): String {
        val data = BackupData(
            subjects = repository.getSubjects().first().map { it.toBackup() },
            grades = repository.getAllGrades().first().map { it.toBackup() },
            exams = repository.getAllExams().first().map { it.toBackup() },
            homework = repository.getAllHomework().first().map { it.toBackup() },
            notebookChecks = repository.getAllNotebookChecks().first().map { it.toBackup() },
            behaviorGrades = repository.getBehaviorGrades().first().map { it.toBackup() },
            scheduleSlots = repository.getScheduleSlots().first().map { it.toBackup() },
            notes = repository.getNotes().first().map { it.toBackup() }
        )
        return json.encodeToString(data)
    }

    suspend fun exportToUri(uri: Uri) {
        val text = buildBackupJson()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
        }
    }

    suspend fun importFromUri(uri: Uri) {
        val text = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: return
        val data = json.decodeFromString<BackupData>(text)

        data.subjects.forEach { repository.addSubject(it.toEntity()) }
        data.grades.forEach { repository.addGrade(it.toEntity()) }
        data.exams.forEach { repository.addExam(it.toEntity()) }
        data.homework.forEach { repository.addHomework(it.toEntity()) }
        data.notebookChecks.forEach { repository.addNotebookCheck(it.toEntity()) }
        data.behaviorGrades.forEach { repository.addBehaviorGrade(it.toEntity()) }
        data.scheduleSlots.forEach { repository.addScheduleSlot(it.toEntity()) }
        data.notes.forEach { repository.addNote(it.toEntity()) }
    }
}
