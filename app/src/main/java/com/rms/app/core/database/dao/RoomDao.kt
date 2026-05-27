package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.Room
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE propertyId = :propertyId ORDER BY roomNumber ASC")
    fun getRoomsByProperty(propertyId: Long): Flow<List<Room>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getRoomById(id: Long): Room?

    @Query("SELECT * FROM rooms WHERE status = 'available' AND propertyId = :propertyId")
    fun getAvailableRooms(propertyId: Long): Flow<List<Room>>

    @Query("SELECT * FROM rooms ORDER BY roomNumber ASC")
    fun getAllRooms(): Flow<List<Room>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: Room): Long

    @Update
    suspend fun updateRoom(room: Room)

    @Delete
    suspend fun deleteRoom(room: Room)

    @Query("UPDATE rooms SET status = :status WHERE id = :roomId")
    suspend fun updateRoomStatus(roomId: Long, status: String)

    @Query("SELECT COUNT(*) FROM rooms WHERE propertyId = :propertyId")
    suspend fun getRoomsCountForProperty(propertyId: Long): Int
}
