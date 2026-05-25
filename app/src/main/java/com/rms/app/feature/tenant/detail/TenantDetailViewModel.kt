package com.rms.app.feature.tenant.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.*
import com.rms.app.core.model.relations.TenantWithRoom
import com.rms.app.feature.tenant.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantDetailUiState(
    val isLoading: Boolean = true,
    val tenantWithRoom: TenantWithRoom? = null,
    val payments: List<Payment> = emptyList(),
    val electricityReadings: List<ElectricityReading> = emptyList(),
    val documents: List<Document> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val maintenanceLogs: List<MaintenanceLog> = emptyList(),
    val selectedTab: Int = 0,
    val error: String? = null
)

@HiltViewModel
class TenantDetailViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tenantId: Long = savedStateHandle.get<String>("tenantId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(TenantDetailUiState())
    val uiState: StateFlow<TenantDetailUiState> = _uiState.asStateFlow()

    init {
        loadTenantDetails()
    }

    private fun loadTenantDetails() {
        viewModelScope.launch {
            tenantRepository.getTenantWithRoom(tenantId).collect { twr ->
                _uiState.update {
                    it.copy(tenantWithRoom = twr, isLoading = false)
                }
            }
        }
        viewModelScope.launch {
            tenantRepository.getPaymentsByTenant(tenantId).collect { payments ->
                _uiState.update { it.copy(payments = payments) }
            }
        }
        viewModelScope.launch {
            tenantRepository.getReadingsByTenant(tenantId).collect { readings ->
                _uiState.update { it.copy(electricityReadings = readings) }
            }
        }
        viewModelScope.launch {
            tenantRepository.getDocumentsByTenant(tenantId).collect { docs ->
                _uiState.update { it.copy(documents = docs) }
            }
        }
        viewModelScope.launch {
            tenantRepository.getExpensesByTenant(tenantId).collect { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
            }
        }
        viewModelScope.launch {
            tenantRepository.getMaintenanceLogsByTenant(tenantId).collect { logs ->
                _uiState.update { it.copy(maintenanceLogs = logs) }
            }
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun deactivateTenant() {
        viewModelScope.launch {
            tenantRepository.deactivateTenant(tenantId)
        }
    }
}
