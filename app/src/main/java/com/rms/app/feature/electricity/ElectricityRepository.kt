package com.rms.app.feature.electricity

import com.rms.app.core.database.dao.ElectricityReadingDao
import com.rms.app.core.database.dao.TenantDao
import com.rms.app.core.model.entities.ElectricityReading
import com.rms.app.core.model.entities.Tenant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

import com.rms.app.core.database.dao.PaymentDao

@Singleton
class ElectricityRepository @Inject constructor(
    private val electricityReadingDao: ElectricityReadingDao,
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao
) {
    suspend fun insertPayment(payment: com.rms.app.core.model.entities.Payment) =
        paymentDao.insertPayment(payment)

    suspend fun getLastReading(tenantId: Long): ElectricityReading? =
        electricityReadingDao.getLastReading(tenantId)

    fun getReadingsByTenant(tenantId: Long): Flow<List<ElectricityReading>> =
        electricityReadingDao.getReadingsByTenant(tenantId)

    suspend fun insertReading(reading: ElectricityReading): Long =
        electricityReadingDao.insertReading(reading)

    suspend fun getTenantById(tenantId: Long): Tenant? =
        tenantDao.getTenantById(tenantId)

    suspend fun markAsPaidWithDetails(readingId: Long, paidDate: Long, mode: String) =
        electricityReadingDao.markAsPaidWithDetails(readingId, paidDate, mode)

    fun getUnpaidReadings(tenantId: Long): Flow<List<ElectricityReading>> =
        electricityReadingDao.getUnpaidReadings(tenantId)

    fun getUnpaidTotal(tenantId: Long): Flow<Double?> =
        electricityReadingDao.getUnpaidElectricityTotal(tenantId)
}
