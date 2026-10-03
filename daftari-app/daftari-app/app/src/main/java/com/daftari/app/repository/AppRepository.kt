package com.daftari.app.repository

import com.daftari.app.data.dao.*
import com.daftari.app.data.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Sole gateway between the ViewModel layer and Room.
 */
class AppRepository(
    private val subjectDao: SubjectDao,
    private val gradeDao: GradeDao,
    private val examDao: ExamDao,
    private val homeworkDao: HomeworkDao,
    private val notebookCheckDao: NotebookCheckDao,
    private val behaviorGradeDao: BehaviorGradeDao,
    private val scheduleSlotDao: ScheduleSlotDao,
    private val noteDao: NoteDao
) {
    // Subjects
    fun getSubjects(): Flow<List<Subject>> = subjectDao.getAll()
    fun getSubject(id: Long): Flow<Subject?> = subjectDao.getById(id)
    suspend fun addSubject(subject: Subject): Long = subjectDao.insert(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.update(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.delete(subject)

    // Grades
    fun getGrades(subjectId: Long): Flow<List<Grade>> = gradeDao.getForSubject(subjectId)
    fun getAllGrades(): Flow<List<Grade>> = gradeDao.getAll()
    suspend fun addGrade(grade: Grade): Long = gradeDao.insert(grade)
    suspend fun deleteGrade(grade: Grade) = gradeDao.delete(grade)
    suspend fun deleteAllGrades() = gradeDao.deleteAll()

    // Exams
    fun getExams(subjectId: Long): Flow<List<Exam>> = examDao.getForSubject(subjectId)
    fun getAllExams(): Flow<List<Exam>> = examDao.getAll()
    suspend fun countExamsForSubject(subjectId: Long): Int = examDao.countForSubject(subjectId)
    suspend fun addExam(exam: Exam): Long = examDao.insert(exam)
    suspend fun updateExam(exam: Exam) = examDao.update(exam)
    suspend fun deleteExam(exam: Exam) = examDao.delete(exam)
    suspend fun deleteAllExams() = examDao.deleteAll()

    // Homework
    fun getHomework(subjectId: Long): Flow<List<Homework>> = homeworkDao.getForSubject(subjectId)
    fun getAllHomework(): Flow<List<Homework>> = homeworkDao.getAll()
    suspend fun addHomework(homework: Homework): Long = homeworkDao.insert(homework)
    suspend fun updateHomework(homework: Homework) = homeworkDao.update(homework)
    suspend fun deleteHomework(homework: Homework) = homeworkDao.delete(homework)
    suspend fun deleteAllHomework() = homeworkDao.deleteAll()

    // Notebook checks
    fun getNotebookChecks(subjectId: Long): Flow<List<NotebookCheck>> =
        notebookCheckDao.getForSubject(subjectId)
    fun getAllNotebookChecks(): Flow<List<NotebookCheck>> = notebookCheckDao.getAll()
    suspend fun addNotebookCheck(check: NotebookCheck): Long = notebookCheckDao.insert(check)
    suspend fun deleteNotebookCheck(check: NotebookCheck) = notebookCheckDao.delete(check)
    suspend fun deleteAllNotebookChecks() = notebookCheckDao.deleteAll()

    // Behavior (general)
    fun getBehaviorGrades(): Flow<List<BehaviorGrade>> = behaviorGradeDao.getAll()
    suspend fun addBehaviorGrade(grade: BehaviorGrade): Long = behaviorGradeDao.insert(grade)
    suspend fun deleteBehaviorGrade(grade: BehaviorGrade) = behaviorGradeDao.delete(grade)
    suspend fun deleteAllBehaviorGrades() = behaviorGradeDao.deleteAll()

    // Schedule
    fun getScheduleSlots(): Flow<List<ScheduleSlot>> = scheduleSlotDao.getAll()
    suspend fun addScheduleSlot(slot: ScheduleSlot): Long = scheduleSlotDao.insert(slot)
    suspend fun deleteScheduleSlot(slot: ScheduleSlot) = scheduleSlotDao.delete(slot)

    // Notes
    fun getNotes(): Flow<List<Note>> = noteDao.getAll()
    suspend fun addNote(note: Note): Long = noteDao.insert(note)
    suspend fun deleteNote(note: Note) = noteDao.delete(note)
}
