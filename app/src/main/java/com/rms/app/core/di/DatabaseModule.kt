package com.rms.app.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rms.app.core.database.RMSDatabase
import com.rms.app.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RMSDatabase {
        return Room.databaseBuilder(
            context,
            RMSDatabase::class.java,
            "rms_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed default property on first launch
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        db.execSQL(
                            "INSERT INTO properties (name, address, type, createdAt) VALUES ('My Property', '', 'residential', ${System.currentTimeMillis()})"
                        )
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun providePropertyDao(db: RMSDatabase): PropertyDao = db.propertyDao()
    @Provides fun provideRoomDao(db: RMSDatabase): RoomDao = db.roomDao()
    @Provides fun provideTenantDao(db: RMSDatabase): TenantDao = db.tenantDao()
    @Provides fun providePaymentDao(db: RMSDatabase): PaymentDao = db.paymentDao()
    @Provides fun provideElectricityReadingDao(db: RMSDatabase): ElectricityReadingDao = db.electricityReadingDao()
    @Provides fun provideDocumentDao(db: RMSDatabase): DocumentDao = db.documentDao()
    @Provides fun provideExpenseDao(db: RMSDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideReminderDao(db: RMSDatabase): ReminderDao = db.reminderDao()
    @Provides fun provideMaintenanceLogDao(db: RMSDatabase): MaintenanceLogDao = db.maintenanceLogDao()
}
