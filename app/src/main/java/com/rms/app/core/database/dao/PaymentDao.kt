package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY paymentDate DESC")
    fun getPaymentsByTenant(tenantId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): Payment?

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId AND forMonth = :month AND forYear = :year AND type = :type")
    suspend fun getPaymentForMonth(tenantId: Long, month: Int, year: Int, type: String = "RENT"): Payment?

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY paymentDate DESC LIMIT 1")
    suspend fun getLastPayment(tenantId: Long): Payment?

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY paymentDate DESC LIMIT 1")
    fun getLastPaymentFlow(tenantId: Long): Flow<Payment?>

    @Query("SELECT SUM(amount) FROM payments WHERE tenantId = :tenantId")
    fun getTotalPaidByTenant(tenantId: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE forMonth = :month AND forYear = :year")
    fun getTotalCollectedForMonth(month: Int, year: Int): Flow<Double?>

    @Query("SELECT * FROM payments WHERE paymentDate >= :startDate AND paymentDate <= :endDate ORDER BY paymentDate DESC")
    fun getPaymentsBetweenDates(startDate: Long, endDate: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment)
}
