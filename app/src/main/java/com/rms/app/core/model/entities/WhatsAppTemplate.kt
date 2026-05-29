package com.rms.app.core.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "whatsapp_templates")
data class WhatsAppTemplate(
    @PrimaryKey
    val templateType: String,
    val messageTemplate: String,
    val updatedAt: Long = System.currentTimeMillis()
)
