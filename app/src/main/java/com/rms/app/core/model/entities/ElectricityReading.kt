package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "electricity_readings",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tenantId")]
)
data class ElectricityReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tenantId: Long,
    val previousReading: Double,
    val currentReading: Double,
    val unitsConsumed: Double,
    val ratePerUnit: Double = 8.0,
    val totalAmount: Double,
    val meterPhotoUri: String? = null,
    val readingDate: Long = System.currentTimeMillis(),
    val forMonth: Int,
    val forYear: Int,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
