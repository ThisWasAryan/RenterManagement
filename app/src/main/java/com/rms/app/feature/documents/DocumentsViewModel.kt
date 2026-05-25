package com.rms.app.feature.documents

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
    val documents: List<Document> = emptyList(),
    val tenants: List<Tenant> = emptyList(),
    val agreements: List<Document> = emptyList(),
    val idDocuments: List<Document> = emptyList(),
    val meterPhotos: List<Document> = emptyList(),
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
            documentRepository.getAllDocuments().collect { docs ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        documents = docs,
                        agreements = docs.filter { d -> d.documentType == DocumentType.AGREEMENT.name },
                        idDocuments = docs.filter { d ->
                            d.documentType in listOf(
                                DocumentType.AADHAAR.name,
                                DocumentType.PAN.name,
                                DocumentType.PASSPORT.name,
                                DocumentType.DRIVING_LICENSE.name
                            )
                        },
                        meterPhotos = docs.filter { d -> d.documentType == DocumentType.METER_PHOTO.name }
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
}
