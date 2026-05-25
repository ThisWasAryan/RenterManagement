package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.WhatsAppTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface WhatsAppTemplateDao {
    @Query("SELECT * FROM whatsapp_templates ORDER BY templateType ASC")
    fun getAllTemplates(): Flow<List<WhatsAppTemplate>>

    @Query("SELECT * FROM whatsapp_templates WHERE templateType = :type")
    suspend fun getTemplate(type: String): WhatsAppTemplate?

    @Query("SELECT * FROM whatsapp_templates WHERE templateType = :type")
    fun getTemplateFlow(type: String): Flow<WhatsAppTemplate?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WhatsAppTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<WhatsAppTemplate>)

    @Update
    suspend fun updateTemplate(template: WhatsAppTemplate)

    @Delete
    suspend fun deleteTemplate(template: WhatsAppTemplate)
}
