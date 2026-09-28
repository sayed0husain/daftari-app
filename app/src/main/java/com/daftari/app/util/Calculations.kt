package com.daftari.app.util

import com.daftari.app.data.entity.Attendance
import com.daftari.app.data.entity.AttendanceStatus
import com.daftari.app.data.entity.Grade

object Calculations {

    /**
     * Subject average: sum(obtained) / sum(total) * 100.
     * Behavior score is intentionally NOT included — it is shown as a separate figure.
     */
    fun subjectAverage(grades: List<Grade>): Double? {
        if (grades.isEmpty()) return null
        val totalObtained = grades.sumOf { it.scoreObtained }
        val totalFull = grades.sumOf { it.scoreTotal }
        if (totalFull <= 0.0) return null
        return (totalObtained / totalFull) * 100.0
    }

    /**
     * Attendance rate: (present + late) / total records * 100.
     * Defaults to 100% when there are no records yet.
     */
    fun attendanceRate(records: List<Attendance>): Double {
        if (records.isEmpty()) return 100.0
        val countedPresent = records.count {
            it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE
        }
        return (countedPresent.toDouble() / records.size) * 100.0
    }
}
