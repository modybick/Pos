package com.example.pos.database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    /**
     * Timestamp(Long)からDateに変換
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * DateからTimestamp(Long)に変換
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}