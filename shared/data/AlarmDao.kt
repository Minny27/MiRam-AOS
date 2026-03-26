package com.seungmin.miram.shared.data

import androidx.room.*
import com.seungmin.miram.shared.model.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: String): Alarm?

    @Query(
        """
        SELECT * FROM alarms
        WHERE hour = :hour
          AND minute = :minute
          AND repeatDays = :repeatDays
          AND (
            (specificDateMillis IS NULL AND :specificDateMillis IS NULL)
            OR specificDateMillis = :specificDateMillis
          )
          AND (:excludeId IS NULL OR id != :excludeId)
        """
    )
    suspend fun findDuplicateAlarms(
        hour: Int,
        minute: Int,
        repeatDays: String,
        specificDateMillis: Long?,
        excludeId: String? = null
    ): List<Alarm>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm)

    @Update
    suspend fun updateAlarm(alarm: Alarm)

    @Delete
    suspend fun deleteAlarm(alarm: Alarm)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: String)
}
