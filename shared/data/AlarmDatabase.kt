package com.example.miram.shared.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.miram.shared.model.Alarm

@Database(entities = [Alarm::class], version = 1, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
