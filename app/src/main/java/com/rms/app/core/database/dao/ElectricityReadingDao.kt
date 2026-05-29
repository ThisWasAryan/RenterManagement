package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.ElectricityReading
import kotlinx.coroutines.flow.Flow

@Dao
interface ElectricityReadingDao {
    @Query("SELECT * FROM electricity_readings WHERE tenantId = :tenantId ORDER BY readingDate DESC")
    fun getReadingsByTenant(tenantId: Long): Flow<List<ElectricityReading>>

    @Query("SELECT * FROM electricity_readings WHERE id = :id")
    suspend fun getReadingById(id: Long): ElectricityReading?

    @Query("SELECT * FROM electricity_readings WHERE tenantId = :tenantId ORDER BY readingDate DESC LIMIT 1")
    suspend fun getLastReading(tenantId: Long): ElectricityReading?

    @Query("SELECT * FROM electricity_readings WHERE tenantId = :tenantId ORDER BY readingDate DESC LIMIT 1")
    fun getLastReadingFlow(tenantId: Long): Flow<ElectricityReading?>

    @Query("SELECT * FROM electricity_readings WHERE tenantId = :tenantId AND isPaid = 0")
    fun getUnpaidReadings(tenantId: Long): Flow<List<ElectricityReading>>

    @Query("SELECT SUM(totalAmount) FROM electricity_readings WHERE tenantId = :tenantId AND isPaid = 0")
    fun getUnpaidElectricityTotal(tenantId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ElectricityReading): Long

    @Update
    suspend fun updateReading(reading: ElectricityReading)

    @Delete
    suspend fun deleteReading(reading: ElectricityReading)

    @Query("UPDATE electricity_readings SET isPaid = 1 WHERE id = :readingId")
    suspend fun markAsPaid(readingId: Long)

    @Query("UPDATE electricity_readings SET isPaid = 1, paidDate = :paidDate, paymentMode = :mode WHERE id = :readingId")
    suspend fun markAsPaidWithDetails(readingId: Long, paidDate: Long, mode: String)
}
