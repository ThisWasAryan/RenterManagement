package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dueDate ASC")
    fun getPendingReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders ORDER BY dueDate DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE tenantId = :tenantId ORDER BY dueDate DESC")
    fun getRemindersByTenant(tenantId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND dueDate <= :timestamp")
    suspend fun getOverdueReminders(timestamp: Long = System.currentTimeMillis()): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :reminderId")
    suspend fun markAsCompleted(reminderId: Long)
}
