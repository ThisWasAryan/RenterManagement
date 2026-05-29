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
                if (templates.isEmpty()) {
                    insertDefaultTemplates()
                } else {
                    _uiState.update {
                        it.copy(templates = templates, isLoading = false)
                    }
                }
            }
        }
    }

    private suspend fun insertDefaultTemplates() {
        val defaults = listOf(
            WhatsAppTemplate(templateType = "RENT_REMINDER", messageTemplate = "Hi {tenantName},\n\nJust a gentle reminder that your rent of {amount} for {month} is due. Please make the payment at your earliest convenience.\n\nThank you!"),
            WhatsAppTemplate(templateType = "OVERDUE_REMINDER", messageTemplate = "Hi {tenantName},\n\nThis is a reminder that your rent payment of {amount} for {month} is currently overdue. Please process the payment immediately to avoid any late fees.\n\nThank you!"),
            WhatsAppTemplate(templateType = "ELECTRICITY_REMINDER", messageTemplate = "Hi {tenantName},\n\nYour electricity bill for {month} is {amount} based on a reading of {reading} units. Please pay this along with your rent.\n\nThank you!"),
            WhatsAppTemplate(templateType = "PAYMENT_CONFIRMATION", messageTemplate = "Hi {tenantName},\n\nWe have received your payment of {amount} for {month}. Thank you for your prompt payment!\n\nRegards."),
            WhatsAppTemplate(templateType = "COMBINED_REMINDER", messageTemplate = "Hi {tenantName},\n\nYour total dues for {month} are {totalAmount}.\nRent: {rentAmount}\nElectricity: {electricityAmount}\n\nPlease make the payment.\n\nThank you! \uD83C\uDFE0⚡")
        )
        defaults.forEach { templateDao.insertTemplate(it) }
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
