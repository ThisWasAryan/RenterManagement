package com.rms.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rms.app.core.database.converters.DateConverters
import com.rms.app.core.database.dao.*
import com.rms.app.core.model.entities.*

@Database(
    entities = [
        Property::class,
        Room::class,
        Tenant::class,
        Payment::class,
        ElectricityReading::class,
        Document::class,
        Expense::class,
        Reminder::class,
        MaintenanceLog::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverters::class)
abstract class RMSDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun roomDao(): RoomDao
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao
    abstract fun electricityReadingDao(): ElectricityReadingDao
    abstract fun documentDao(): DocumentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun reminderDao(): ReminderDao
    abstract fun maintenanceLogDao(): MaintenanceLogDao
}
