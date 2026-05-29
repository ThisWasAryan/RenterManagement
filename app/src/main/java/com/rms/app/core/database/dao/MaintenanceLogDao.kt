package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.MaintenanceLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceLogDao {
    @Query("SELECT * FROM maintenance_logs WHERE tenantId = :tenantId ORDER BY reportedDate DESC")
    fun getLogsByTenant(tenantId: Long): Flow<List<MaintenanceLog>>

    @Query("SELECT * FROM maintenance_logs ORDER BY reportedDate DESC")
    fun getAllLogs(): Flow<List<MaintenanceLog>>

    @Query("SELECT * FROM maintenance_logs WHERE id = :id")
    suspend fun getLogById(id: Long): MaintenanceLog?

    @Query("SELECT * FROM maintenance_logs WHERE status != 'RESOLVED' ORDER BY reportedDate ASC")
    fun getOpenLogs(): Flow<List<MaintenanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MaintenanceLog): Long

    @Update
    suspend fun updateLog(log: MaintenanceLog)

    @Delete
    suspend fun deleteLog(log: MaintenanceLog)
}
