package com.rms.app.feature.home

import com.rms.app.core.database.dao.*
import com.rms.app.core.model.entities.*
import com.rms.app.core.model.relations.TenantWithRoom
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao,
    private val electricityReadingDao: ElectricityReadingDao,
    private val roomDao: RoomDao
) {
    fun getActiveTenantsWithRooms(): Flow<List<TenantWithRoom>> =
        tenantDao.getActiveTenantsWithRooms()

    fun searchTenantsWithRooms(query: String): Flow<List<TenantWithRoom>> =
        tenantDao.searchTenantsWithRooms(query)

    fun getActiveTenantCount(): Flow<Int> =
        tenantDao.getActiveTenantCount()

    suspend fun getLastPayment(tenantId: Long): Payment? =
        paymentDao.getLastPayment(tenantId)

    fun getLastPaymentFlow(tenantId: Long): Flow<Payment?> =
        paymentDao.getLastPaymentFlow(tenantId)

    suspend fun getLastReading(tenantId: Long): ElectricityReading? =
        electricityReadingDao.getLastReading(tenantId)

    fun getLastReadingFlow(tenantId: Long): Flow<ElectricityReading?> =
        electricityReadingDao.getLastReadingFlow(tenantId)

    fun getUnpaidElectricityTotal(tenantId: Long): Flow<Double?> =
        electricityReadingDao.getUnpaidElectricityTotal(tenantId)
}
