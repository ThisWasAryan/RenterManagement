package com.rms.app.feature.electricity

import com.rms.app.core.database.dao.ElectricityReadingDao
import com.rms.app.core.database.dao.TenantDao
import com.rms.app.core.model.entities.ElectricityReading
import com.rms.app.core.model.entities.Tenant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectricityRepository @Inject constructor(
    private val electricityReadingDao: ElectricityReadingDao,
    private val tenantDao: TenantDao
) {
    suspend fun getLastReading(tenantId: Long): ElectricityReading? =
        electricityReadingDao.getLastReading(tenantId)

    fun getReadingsByTenant(tenantId: Long): Flow<List<ElectricityReading>> =
        electricityReadingDao.getReadingsByTenant(tenantId)

    suspend fun insertReading(reading: ElectricityReading): Long =
        electricityReadingDao.insertReading(reading)

    suspend fun getTenantById(tenantId: Long): Tenant? =
        tenantDao.getTenantById(tenantId)
}
