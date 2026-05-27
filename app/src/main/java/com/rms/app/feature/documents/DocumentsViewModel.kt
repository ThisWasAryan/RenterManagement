package com.rms.app.feature.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.Document
import com.rms.app.core.model.entities.Tenant
import com.rms.app.core.model.enums.DocumentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentsUiState(
    val isLoading: Boolean = true,
    val categorizedDocuments: Map<DocumentType, List<com.rms.app.core.model.relations.DocumentWithContext>> = emptyMap(),
    val tenants: List<Tenant> = emptyList(),
    val showUploadDialog: Boolean = false,
    val selectedTenantId: Long? = null,
    val selectedDocType: DocumentType = DocumentType.OTHER,
    val pendingUri: Uri? = null,
    val error: String? = null
)

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    init {
        loadDocuments()
        loadTenants()
    }

    private fun loadDocuments() {
        viewModelScope.launch {
            documentRepository.getAllDocumentsWithContext().collect { docs ->
                // Exclude METER_PHOTO entirely
                val filteredDocs = docs.filter { it.document.documentType != DocumentType.METER_PHOTO.name }
                
                // Group by DocumentType enum
                val categorized = filteredDocs.groupBy { docWithCtx ->
                    try {
                        DocumentType.valueOf(docWithCtx.document.documentType)
                    } catch (e: Exception) {
                        DocumentType.OTHER
                    }
                }
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        categorizedDocuments = categorized
                    )
                }
            }
        }
    }

    private fun loadTenants() {
        viewModelScope.launch {
            documentRepository.getActiveTenants().collect { tenants ->
                _uiState.update { it.copy(tenants = tenants) }
            }
        }
    }

    fun onFileSelected(uri: Uri) {
        _uiState.update { it.copy(pendingUri = uri, showUploadDialog = true) }
    }

    fun onTenantSelected(tenantId: Long) {
        _uiState.update { it.copy(selectedTenantId = tenantId) }
    }

    fun onDocTypeSelected(type: DocumentType) {
        _uiState.update { it.copy(selectedDocType = type) }
    }

    fun dismissUploadDialog() {
        _uiState.update { it.copy(showUploadDialog = false, pendingUri = null) }
    }

    fun uploadDocument(name: String) {
        val state = _uiState.value
        val uri = state.pendingUri ?: return
        val tenantId = state.selectedTenantId ?: return

        viewModelScope.launch {
            try {
                val doc = Document(
                    tenantId = tenantId,
                    documentType = state.selectedDocType.name,
                    name = name.ifBlank { state.selectedDocType.name },
                    fileUri = uri.toString(),
                    mimeType = null,
                    fileSize = 0,
                    createdAt = System.currentTimeMillis()
                )
                documentRepository.insertDocument(doc)
                _uiState.update { it.copy(showUploadDialog = false, pendingUri = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            try {
                documentRepository.deleteDocument(document)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
