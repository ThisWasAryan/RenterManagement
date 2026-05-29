package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.Tenant
import com.rms.app.core.model.relations.TenantWithRoom
import com.rms.app.core.model.relations.TenantWithPayments
import com.rms.app.core.model.relations.TenantWithElectricity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants ORDER BY name ASC")
    fun getAllTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getTenantById(id: Long): Tenant?

    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantByIdFlow(id: Long): Flow<Tenant?>

    @Transaction
    @Query("SELECT * FROM tenants WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveTenantsWithRooms(): Flow<List<TenantWithRoom>>

    @Transaction
    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantWithRoom(id: Long): Flow<TenantWithRoom?>

    @Transaction
    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantWithPayments(id: Long): Flow<TenantWithPayments?>

    @Transaction
    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantWithElectricity(id: Long): Flow<TenantWithElectricity?>

    @Query("SELECT * FROM tenants WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%')")
    fun searchTenants(query: String): Flow<List<Tenant>>

    @Transaction
    @Query("SELECT * FROM tenants WHERE isActive = 1 AND (name LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchTenantsWithRooms(query: String): Flow<List<TenantWithRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: Tenant): Long

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Query("SELECT COUNT(*) FROM tenants WHERE roomId = :roomId AND isActive = 1")
    suspend fun getActiveTenantsCountForRoom(roomId: Long): Int

    @Query("UPDATE tenants SET monthlyRent = :newRent WHERE roomId = :roomId AND isActive = 1")
    suspend fun updateRentForActiveTenantsInRoom(roomId: Long, newRent: Double)

    @Query("UPDATE tenants SET isActive = 0 WHERE id = :tenantId")
    suspend fun deactivateTenant(tenantId: Long)

    @Query("SELECT COUNT(*) FROM tenants WHERE isActive = 1")
    fun getActiveTenantCount(): Flow<Int>
}
