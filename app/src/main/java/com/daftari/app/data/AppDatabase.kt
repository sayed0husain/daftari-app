package com.daftari.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.daftari.app.data.dao.*
import com.daftari.app.data.entity.*

@Database(
    entities = [
        Subject::class,
        Grade::class,
        Exam::class,
        Homework::class,
        NotebookCheck::class,
        Attendance::class,
        BehaviorGrade::class,
        ScheduleSlot::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao
    abstract fun examDao(): ExamDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun notebookCheckDao(): NotebookCheckDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun behaviorGradeDao(): BehaviorGradeDao
    abstract fun scheduleSlotDao(): ScheduleSlotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daftari.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
