package com.daftari.app.util

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
}
