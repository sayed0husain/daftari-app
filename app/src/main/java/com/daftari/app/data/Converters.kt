package com.daftari.app.data

import androidx.room.TypeConverter
import com.daftari.app.data.entity.AttendanceStatus

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)
}
