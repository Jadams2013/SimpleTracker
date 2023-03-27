package com.example.simpletracker

import androidx.room.TypeConverter
import java.util.*

class TagConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    //
    @TypeConverter
    fun fromMillis(it: Long?): Calendar? {
        var newCal = Calendar.getInstance()
        if (it != null) {
            newCal.timeInMillis = it
        } else {
            newCal.timeInMillis = 0
        }
        return newCal
    }

    @TypeConverter
    fun calendarToMillis(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }


}