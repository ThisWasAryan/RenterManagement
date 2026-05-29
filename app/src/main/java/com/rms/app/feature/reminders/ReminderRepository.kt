package com.rms.app.feature.reminders

import com.rms.app.core.database.dao.ReminderDao
import com.rms.app.core.database.dao.TenantDao
import com.rms.app.core.model.entities.Reminder
import com.rms.app.core.model.entities.Tenant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val tenantDao: TenantDao
) {
    fun getPendingReminders(): Flow<List<Reminder>> = reminderDao.getPendingReminders()
    fun getAllReminders(): Flow<List<Reminder>> = reminderDao.getAllReminders()
    suspend fun insertReminder(reminder: Reminder): Long = reminderDao.insertReminder(reminder)
    suspend fun markAsCompleted(reminderId: Long) = reminderDao.markAsCompleted(reminderId)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.deleteReminder(reminder)
    suspend fun getOverdueReminders(): List<Reminder> = reminderDao.getOverdueReminders()
    fun getActiveTenants(): Flow<List<Tenant>> = tenantDao.getActiveTenants()
}
