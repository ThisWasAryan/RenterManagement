package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.Property
import com.rms.app.core.model.relations.PropertyWithRooms
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY createdAt DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyById(id: Long): Property?

    @Transaction
    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyWithRooms(id: Long): Flow<PropertyWithRooms?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property): Long

    @Update
    suspend fun updateProperty(property: Property)

    @Delete
    suspend fun deleteProperty(property: Property)

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun getPropertyCount(): Int
}
