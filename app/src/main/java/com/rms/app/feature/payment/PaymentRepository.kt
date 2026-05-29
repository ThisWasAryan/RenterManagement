package com.rms.app.feature.payment

import com.rms.app.core.database.dao.PaymentDao
import com.rms.app.core.database.dao.TenantDao
import com.rms.app.core.model.entities.Payment
import com.rms.app.core.model.entities.Tenant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao,
    private val tenantDao: TenantDao
) {
    fun getAllPayments(): Flow<List<Payment>> = paymentDao.getAllPayments()

    fun getPaymentsByTenant(tenantId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsByTenant(tenantId)

    suspend fun insertPayment(payment: Payment): Long =
        paymentDao.insertPayment(payment)

    suspend fun deletePayment(payment: Payment) =
        paymentDao.deletePayment(payment)

    fun getTotalCollectedBetweenDates(startDate: Long, endDate: Long): Flow<Double?> =
        paymentDao.getTotalCollectedBetweenDates(startDate, endDate)

    fun getActiveTenants(): Flow<List<Tenant>> = tenantDao.getActiveTenants()

    suspend fun getTenantById(tenantId: Long): Tenant? = tenantDao.getTenantById(tenantId)
}
