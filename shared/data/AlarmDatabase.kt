package com.seungmin.miram.shared.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seungmin.miram.shared.model.Alarm

@Database(entities = [Alarm::class], version = 3, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
