package com.rms.app.feature.tenant

import com.rms.app.core.database.dao.*
import com.rms.app.core.model.entities.*
import com.rms.app.core.model.relations.TenantWithRoom
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantRepository @Inject constructor(
    private val tenantDao: TenantDao,
    private val roomDao: RoomDao,
    private val paymentDao: PaymentDao,
    private val electricityReadingDao: ElectricityReadingDao,
    private val documentDao: DocumentDao,
    private val expenseDao: ExpenseDao,
    private val maintenanceLogDao: MaintenanceLogDao,
    private val templateDao: WhatsAppTemplateDao
) {
    fun getTenantWithRoom(tenantId: Long): Flow<TenantWithRoom?> =
        tenantDao.getTenantWithRoom(tenantId)

    fun getTenantByIdFlow(tenantId: Long): Flow<Tenant?> =
        tenantDao.getTenantByIdFlow(tenantId)

    suspend fun getTenantById(tenantId: Long): Tenant? =
        tenantDao.getTenantById(tenantId)

    suspend fun insertTenant(tenant: Tenant): Long =
        tenantDao.insertTenant(tenant)

    suspend fun updateTenant(tenant: Tenant) =
        tenantDao.updateTenant(tenant)

    suspend fun deactivateTenant(tenantId: Long) =
        tenantDao.deactivateTenant(tenantId)

    suspend fun getActiveTenantsCountForRoom(roomId: Long): Int =
        tenantDao.getActiveTenantsCountForRoom(roomId)

    fun getAllRooms(): Flow<List<Room>> = roomDao.getAllRooms()

    fun getPaymentsByTenant(tenantId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsByTenant(tenantId)

    fun getReadingsByTenant(tenantId: Long): Flow<List<ElectricityReading>> =
        electricityReadingDao.getReadingsByTenant(tenantId)

    fun getDocumentsByTenant(tenantId: Long): Flow<List<Document>> =
        documentDao.getDocumentsByTenant(tenantId)

    fun getExpensesByTenant(tenantId: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByTenant(tenantId)

    fun getMaintenanceLogsByTenant(tenantId: Long): Flow<List<MaintenanceLog>> =
        maintenanceLogDao.getLogsByTenant(tenantId)

    suspend fun updateRoomStatus(roomId: Long, status: String) =
        roomDao.updateRoomStatus(roomId, status)

    suspend fun insertRoom(room: Room): Long =
        roomDao.insertRoom(room)

    suspend fun getRoomById(roomId: Long): Room? =
        roomDao.getRoomById(roomId)

    suspend fun updateRoom(room: Room) =
        roomDao.updateRoom(room)

    suspend fun insertPayment(payment: Payment): Long =
        paymentDao.insertPayment(payment)

    suspend fun markElectricityPaid(readingId: Long, paidDate: Long, mode: String) {
        electricityReadingDao.markAsPaidWithDetails(readingId, paidDate, mode)
        val reading = electricityReadingDao.getReadingById(readingId)
        if (reading != null && reading.totalAmount > 0) {
            val payment = Payment(
                tenantId = reading.tenantId,
                amount = reading.totalAmount,
                type = "ELECTRICITY",
                mode = mode,
                paymentDate = paidDate,
                forMonth = reading.forMonth,
                forYear = reading.forYear,
                notes = "Electricity Payment for ${reading.previousReading.toInt()} -> ${reading.currentReading.toInt()} units"
            )
            paymentDao.insertPayment(payment)
        }
    }

    suspend fun insertDocument(document: Document): Long =
        documentDao.insertDocument(document)
        
    suspend fun deleteDocument(document: Document) = 
        documentDao.deleteDocument(document)
        
    suspend fun getTemplate(type: String): WhatsAppTemplate? =
        templateDao.getTemplate(type)
    
    suspend fun deleteTenant(tenant: Tenant) {
        tenantDao.deleteTenant(tenant)
    }
}
