package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
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
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tenantId: Long,
    val amount: Double,
    val type: String = "RENT",
    val mode: String = "CASH",
    val paymentDate: Long = System.currentTimeMillis(),
    val forMonth: Int,
    val forYear: Int,
    val notes: String? = null,
    val receiptUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
