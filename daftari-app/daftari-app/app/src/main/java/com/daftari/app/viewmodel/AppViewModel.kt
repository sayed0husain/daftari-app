package com.daftari.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daftari.app.DaftariApplication
import com.daftari.app.backup.BackupManager
import com.daftari.app.data.entity.*
import com.daftari.app.notification.DailySummaryScheduler
import com.daftari.app.notification.ReminderScheduler
import com.daftari.app.repository.AppRepository
import com.daftari.app.settings.SettingsDataStore
import com.daftari.app.util.Calculations
import com.daftari.app.util.PageParser
import androidx.work.ExistingWorkPolicy
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Which parts of the data a "start new term" archive action should clear. */
data class NewTermOptions(
    val clearGrades: Boolean = true,
    val clearExams: Boolean = true,
    val clearHomework: Boolean = true,
    val clearNotebookChecks: Boolean = true,
    val clearGeneralBehavior: Boolean = true,
    val resetSubjectBehaviorScores: Boolean = true,
    val resetRequirements: Boolean = true
)

class AppViewModel(
    application: Application,
    val repository: AppRepository,
    val settings: SettingsDataStore
) : AndroidViewModel(application) {

    private val backupManager = BackupManager(application, repository)

    val subjects: StateFlow<List<Subject>> = repository.getSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<Exam>> = repository.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHomework: StateFlow<List<Homework>> = repository.getAllHomework()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotebookChecks: StateFlow<List<NotebookCheck>> = repository.getAllNotebookChecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGrades: StateFlow<List<Grade>> = repository.getAllGrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val behaviorGrades: StateFlow<List<BehaviorGrade>> = repository.getBehaviorGrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleSlots: StateFlow<List<ScheduleSlot>> = repository.getScheduleSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = repository.getNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- Subjects ----------
    fun addSubject(name: String, colorHex: String, weeklyHours: Int) = viewModelScope.launch {
        repository.addSubject(Subject(name = name, colorHex = colorHex, weeklyHours = weeklyHours))
    }

    /** Updates a subject's editable identity fields (name/color/weekly hours) only. */
    fun editSubjectInfo(subject: Subject, name: String, colorHex: String, weeklyHours: Int) =
        viewModelScope.launch {
            repository.updateSubject(subject.copy(name = name, colorHex = colorHex, weeklyHours = weeklyHours))
        }

    fun updateSubject(subject: Subject) = viewModelScope.launch { repository.updateSubject(subject) }
    fun deleteSubject(subject: Subject) = viewModelScope.launch { repository.deleteSubject(subject) }

    fun subjectAverage(grades: List<Grade>): Double? = Calculations.subjectAverage(grades)
    fun missingPagesCount(input: String): Int = PageParser.countMissingPages(input)

    // ---------- Grades ----------
    fun addGrade(subjectId: Long, name: String, obtained: Double, total: Double) = viewModelScope.launch {
        repository.addGrade(Grade(subjectId = subjectId, name = name, scoreObtained = obtained, scoreTotal = total))
    }
    fun deleteGrade(grade: Grade) = viewModelScope.launch { repository.deleteGrade(grade) }

    // ---------- Exams ----------
    /** Returns true if this would be a third (or later) exam for the subject, requiring user confirmation. */
    suspend fun wouldExceedExamLimit(subjectId: Long): Boolean =
        repository.countExamsForSubject(subjectId) >= 2

    fun addExam(
        subjectId: Long, name: String, date: Long, notes: String,
        reminderEnabled: Boolean, reminderOffsetMinutes: Long
    ) = viewModelScope.launch {
        val id = repository.addExam(
            Exam(
                subjectId = subjectId, name = name, date = date, notes = notes,
                reminderEnabled = reminderEnabled, reminderOffsetMinutes = reminderOffsetMinutes
            )
        )
        if (reminderEnabled) scheduleExamReminder(id, name, date, reminderOffsetMinutes)
    }

    fun updateExam(exam: Exam) = viewModelScope.launch {
        repository.updateExam(exam)
        if (exam.reminderEnabled) {
            scheduleExamReminder(exam.id, exam.name, exam.date, exam.reminderOffsetMinutes)
        } else {
            ReminderScheduler.cancel(getApplication(), "exam_${exam.id}")
        }
    }

    fun deleteExam(exam: Exam) = viewModelScope.launch {
        repository.deleteExam(exam)
        ReminderScheduler.cancel(getApplication(), "exam_${exam.id}")
    }

    private fun scheduleExamReminder(id: Long, name: String, date: Long, offsetMinutes: Long) {
        val app = getApplication<Application>()
        val title = app.getString(com.daftari.app.R.string.notif_exam_title, name)
        ReminderScheduler.schedule(
            app, "exam_$id", title, date - offsetMinutes * 60_000L, id.toInt()
        )
    }

    // ---------- Homework ----------
    fun addHomework(
        subjectId: Long, description: String, dueDate: Long,
        reminderEnabled: Boolean, reminderOffsetMinutes: Long
    ) = viewModelScope.launch {
        val id = repository.addHomework(
            Homework(
                subjectId = subjectId, description = description, dueDate = dueDate,
                reminderEnabled = reminderEnabled, reminderOffsetMinutes = reminderOffsetMinutes
            )
        )
        if (reminderEnabled) {
            val app = getApplication<Application>()
            val title = app.getString(com.daftari.app.R.string.notif_homework_title, description)
            ReminderScheduler.schedule(app, "hw_$id", title, dueDate - reminderOffsetMinutes * 60_000L, id.toInt())
        }
    }

    fun toggleHomeworkDone(homework: Homework) = viewModelScope.launch {
        repository.updateHomework(homework.copy(completed = !homework.completed))
    }

    fun deleteHomework(homework: Homework) = viewModelScope.launch {
        repository.deleteHomework(homework)
        ReminderScheduler.cancel(getApplication(), "hw_${homework.id}")
    }

    // ---------- Notebook checks ----------
    fun addNotebookCheck(
        subjectId: Long, description: String, date: Long,
        reminderEnabled: Boolean, reminderOffsetMinutes: Long
    ) = viewModelScope.launch {
        val id = repository.addNotebookCheck(
            NotebookCheck(
                subjectId = subjectId, description = description, date = date,
                reminderEnabled = reminderEnabled, reminderOffsetMinutes = reminderOffsetMinutes
            )
        )
        if (reminderEnabled) {
            val app = getApplication<Application>()
            val title = app.getString(com.daftari.app.R.string.notif_check_title, description)
            ReminderScheduler.schedule(app, "check_$id", title, date - reminderOffsetMinutes * 60_000L, id.toInt())
        }
    }

    fun deleteNotebookCheck(check: NotebookCheck) = viewModelScope.launch {
        repository.deleteNotebookCheck(check)
        ReminderScheduler.cancel(getApplication(), "check_${check.id}")
    }

    // ---------- Requirements (notebook/copybook) ----------
    fun updateRequirements(
        subject: Subject,
        notebookComplete: Boolean,
        copybookComplete: Boolean,
        copybookMissingPagesInput: String,
        copybookRequiredPagesInput: String,
        subjectBehaviorScore: Int
    ) = viewModelScope.launch {
        repository.updateSubject(
            subject.copy(
                notebookComplete = notebookComplete,
                copybookComplete = copybookComplete,
                copybookMissingPagesInput = copybookMissingPagesInput,
                copybookRequiredPagesInput = copybookRequiredPagesInput,
                subjectBehaviorScore = subjectBehaviorScore
            )
        )
    }

    // ---------- General behavior ----------
    fun addBehaviorGrade(periodName: String, score: Int) = viewModelScope.launch {
        repository.addBehaviorGrade(BehaviorGrade(periodName = periodName, score = score))
    }
    fun deleteBehaviorGrade(grade: BehaviorGrade) = viewModelScope.launch { repository.deleteBehaviorGrade(grade) }

    // ---------- Schedule ----------
    fun hasConflict(dayOfWeek: Int, startTime: String, endTime: String, existing: List<ScheduleSlot>): Boolean {
        return existing.any {
            it.dayOfWeek == dayOfWeek && timesOverlap(startTime, endTime, it.startTime, it.endTime)
        }
    }

    private fun timesOverlap(aStart: String, aEnd: String, bStart: String, bEnd: String): Boolean {
        return aStart < bEnd && bStart < aEnd
    }

    fun addScheduleSlot(subjectId: Long, dayOfWeek: Int, startTime: String, endTime: String) =
        viewModelScope.launch {
            repository.addScheduleSlot(
                ScheduleSlot(subjectId = subjectId, dayOfWeek = dayOfWeek, startTime = startTime, endTime = endTime)
            )
        }

    fun deleteScheduleSlot(slot: ScheduleSlot) = viewModelScope.launch { repository.deleteScheduleSlot(slot) }

    // ---------- Notes ----------
    fun addNote(text: String) = viewModelScope.launch {
        repository.addNote(Note(text = text))
    }
    fun deleteNote(note: Note) = viewModelScope.launch { repository.deleteNote(note) }

    // ---------- New term / archive ----------
    fun startNewTerm(options: NewTermOptions) = viewModelScope.launch {
        if (options.clearGrades) repository.deleteAllGrades()
        if (options.clearExams) {
            // Cancel any pending exam reminders before wiping the rows.
            repository.getAllExams().first().forEach {
                ReminderScheduler.cancel(getApplication(), "exam_${it.id}")
            }
            repository.deleteAllExams()
        }
        if (options.clearHomework) {
            repository.getAllHomework().first().forEach {
                ReminderScheduler.cancel(getApplication(), "hw_${it.id}")
            }
            repository.deleteAllHomework()
        }
        if (options.clearNotebookChecks) {
            repository.getAllNotebookChecks().first().forEach {
                ReminderScheduler.cancel(getApplication(), "check_${it.id}")
            }
            repository.deleteAllNotebookChecks()
        }
        if (options.clearGeneralBehavior) repository.deleteAllBehaviorGrades()

        if (options.resetSubjectBehaviorScores || options.resetRequirements) {
            repository.getSubjects().first().forEach { subject ->
                var updated = subject
                if (options.resetSubjectBehaviorScores) {
                    updated = updated.copy(subjectBehaviorScore = 40)
                }
                if (options.resetRequirements) {
                    updated = updated.copy(
                        notebookComplete = true,
                        copybookComplete = true,
                        copybookMissingPagesInput = "",
                        copybookRequiredPagesInput = ""
                    )
                }
                if (updated != subject) repository.updateSubject(updated)
            }
        }
    }

    // ---------- Settings / backup ----------
    fun setThemeMode(mode: Int) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setLanguage(lang: String) = viewModelScope.launch { settings.setLanguage(lang) }
    fun setAppLock(enabled: Boolean, pin: String) = viewModelScope.launch { settings.setAppLock(enabled, pin) }
    fun setOnboardingDone(done: Boolean) = viewModelScope.launch { settings.setOnboardingDone(done) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { settings.setNotificationsEnabled(enabled) }

    fun setDailySummaryEnabled(enabled: Boolean) = viewModelScope.launch {
        settings.setDailySummaryEnabled(enabled)
        val app = getApplication<Application>()
        if (enabled) {
            DailySummaryScheduler.scheduleNext(app, ExistingWorkPolicy.REPLACE)
        } else {
            DailySummaryScheduler.cancel(app)
        }
    }

    suspend fun exportBackup(uri: android.net.Uri) = backupManager.exportToUri(uri)
    suspend fun importBackup(uri: android.net.Uri) = backupManager.importFromUri(uri)

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val app = application as DaftariApplication
                    return AppViewModel(app, app.repository, app.settingsDataStore) as T
                }
            }
    }
}
