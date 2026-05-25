package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
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
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tenantId: Long? = null,
    val category: String = "OTHER",
    val description: String = "",
    val amount: Double = 0.0,
    val expenseDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
