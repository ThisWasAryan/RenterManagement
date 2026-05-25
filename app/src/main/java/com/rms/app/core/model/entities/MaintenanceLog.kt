package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_logs",
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
data class MaintenanceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tenantId: Long? = null,
    val title: String,
    val description: String? = null,
    val status: String = "REPORTED",
    val reportedDate: Long = System.currentTimeMillis(),
    val resolvedDate: Long? = null,
    val cost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
