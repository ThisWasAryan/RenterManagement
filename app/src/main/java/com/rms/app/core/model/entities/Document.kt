package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
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
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tenantId: Long,
    val documentType: String = "OTHER",
    val name: String,
    val fileUri: String,
    val mimeType: String? = null,
    val fileSize: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
