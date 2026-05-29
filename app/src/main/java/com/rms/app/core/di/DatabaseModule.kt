package com.rms.app.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rms.app.core.database.RMSDatabase
import com.rms.app.core.database.dao.*
import com.rms.app.core.model.entities.WhatsAppTemplate
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

    @Volatile
    private var INSTANCE: RMSDatabase? = null

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
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        // Seed default property
                        db.execSQL(
                            "INSERT INTO properties (name, address, type, createdAt) VALUES ('My Property', '', 'residential', ${System.currentTimeMillis()})"
                        )
                        // Seed default WhatsApp templates
                        val templates = listOf(
                            "rent_due" to "Hi {tenantName},\n\nThis is a friendly reminder that your rent of {amountDue} for {month} is due.\n\nPlease make the payment at your earliest convenience.\n\nThank you! 🏠",
                            "overdue" to "Hi {tenantName},\n\nYour rent payment is overdue. Pending balance: {amountDue}\n\nPlease clear the dues at the earliest.\n\nThank you! 🏠",
                            "electricity_due" to "Hi {tenantName},\n\nYour electricity bill of {amountDue} for {month} is pending.\n\nUnits consumed: {units}\nRate: {rate}/unit\n\nPlease pay at your convenience.\n\nThank you! ⚡",
                            "payment_confirmation" to "Hi {tenantName},\n\nYour rent payment of {amountDue} for {month} has been received.\n\nThank you! ✅"
                        )
                        templates.forEach { (type, msg) ->
                            db.execSQL(
                                "INSERT INTO whatsapp_templates (templateType, messageTemplate, updatedAt) VALUES ('$type', '${msg.replace("'", "''")}', ${System.currentTimeMillis()})"
                            )
                        }
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
    @Provides fun provideWhatsAppTemplateDao(db: RMSDatabase): WhatsAppTemplateDao = db.whatsAppTemplateDao()
}
