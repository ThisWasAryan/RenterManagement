package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tenants",
    foreignKeys = [
        ForeignKey(
            entity = Room::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("roomId")]
)
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long? = null,
    val name: String,
    val phone: String = "",
    val whatsappNumber: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val aadhaarNumber: String? = null,
    val panNumber: String? = null,
    val moveInDate: Long? = null,
    val agreementStartDate: Long? = null,
    val agreementEndDate: Long? = null,
    val advanceDeposit: Double = 0.0,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
