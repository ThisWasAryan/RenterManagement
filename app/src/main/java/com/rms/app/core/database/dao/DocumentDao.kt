package com.rms.app.core.database.dao

import androidx.room.*
import com.rms.app.core.model.entities.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE tenantId = :tenantId ORDER BY createdAt DESC")
    fun getDocumentsByTenant(tenantId: Long): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE documentType = :type ORDER BY createdAt DESC")
    fun getDocumentsByType(type: String): Flow<List<Document>>

    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Transaction
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocumentsWithContext(): Flow<List<com.rms.app.core.model.relations.DocumentWithContext>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): Document?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)
}
