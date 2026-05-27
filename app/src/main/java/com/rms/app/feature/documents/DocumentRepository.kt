package com.rms.app.feature.documents

import com.rms.app.core.database.dao.DocumentDao
import com.rms.app.core.database.dao.TenantDao
import com.rms.app.core.model.entities.Document
import com.rms.app.core.model.entities.Tenant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val tenantDao: TenantDao
) {
    fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()
    fun getAllDocumentsWithContext(): Flow<List<com.rms.app.core.model.relations.DocumentWithContext>> = documentDao.getAllDocumentsWithContext()
    fun getDocumentsByType(type: String): Flow<List<Document>> = documentDao.getDocumentsByType(type)
    fun getDocumentsByTenant(tenantId: Long): Flow<List<Document>> = documentDao.getDocumentsByTenant(tenantId)
    suspend fun insertDocument(document: Document): Long = documentDao.insertDocument(document)
    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)
    fun getActiveTenants(): Flow<List<Tenant>> = tenantDao.getActiveTenants()
}
