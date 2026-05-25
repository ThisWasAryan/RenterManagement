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
    private val maintenanceLogDao: MaintenanceLogDao
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

    suspend fun insertPayment(payment: Payment): Long =
        paymentDao.insertPayment(payment)

    suspend fun markElectricityPaid(readingId: Long, paidDate: Long, mode: String) =
        electricityReadingDao.markAsPaidWithDetails(readingId, paidDate, mode)

    suspend fun insertDocument(document: Document): Long =
        documentDao.insertDocument(document)
}
