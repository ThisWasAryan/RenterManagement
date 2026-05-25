package com.rms.app.feature.whatsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.database.dao.WhatsAppTemplateDao
import com.rms.app.core.model.entities.WhatsAppTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WhatsAppTemplateUiState(
    val templates: List<WhatsAppTemplate> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WhatsAppTemplateViewModel @Inject constructor(
    private val templateDao: WhatsAppTemplateDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WhatsAppTemplateUiState())
    val uiState: StateFlow<WhatsAppTemplateUiState> = _uiState.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            templateDao.getAllTemplates().collect { templates ->
                _uiState.update {
                    it.copy(templates = templates, isLoading = false)
                }
            }
        }
    }

    fun updateTemplate(templateType: String, newMessage: String) {
        viewModelScope.launch {
            val template = templateDao.getTemplate(templateType)
            if (template != null) {
                templateDao.updateTemplate(template.copy(messageTemplate = newMessage, updatedAt = System.currentTimeMillis()))
            } else {
                templateDao.insertTemplate(WhatsAppTemplate(templateType = templateType, messageTemplate = newMessage))
            }
        }
    }
}
