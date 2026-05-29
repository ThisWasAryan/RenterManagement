package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("propertyId")]
)
data class Room(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long,
    val roomNumber: String,
    val floor: String = "",
    val monthlyRent: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val status: String = "available"
)
