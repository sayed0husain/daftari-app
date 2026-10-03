package com.daftari.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.daftari.app.data.dao.*
import com.daftari.app.data.entity.*

@Database(
    entities = [
        Subject::class,
        Grade::class,
        Exam::class,
        Homework::class,
        NotebookCheck::class,
        BehaviorGrade::class,
        ScheduleSlot::class,
        Note::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao
    abstract fun examDao(): ExamDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun notebookCheckDao(): NotebookCheckDao
    abstract fun behaviorGradeDao(): BehaviorGradeDao
    abstract fun scheduleSlotDao(): ScheduleSlotDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daftari.db"
                )
                    // Attendance was removed and Note was added in v2; there is no user
                    // data worth preserving across this early schema change yet.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
