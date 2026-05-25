package com.rms.app.feature.tenant.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.Room
import com.rms.app.core.model.entities.Tenant
import com.rms.app.feature.tenant.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTenantUiState(
    val name: String = "",
    val phone: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    val selectedRoomId: Long? = null,
    val advanceDeposit: String = "",
    val notes: String = "",
    val aadhaarNumber: String = "",
    val panNumber: String = "",
    val availableRooms: List<Room> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddTenantViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tenantId: Long = savedStateHandle.get<String>("tenantId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(AddTenantUiState())
    val uiState: StateFlow<AddTenantUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
        if (tenantId > 0) loadTenant()
    }

    private fun loadRooms() {
        viewModelScope.launch {
            tenantRepository.getAllRooms().collect { rooms ->
                _uiState.update { it.copy(availableRooms = rooms) }
            }
        }
    }

    private fun loadTenant() {
        viewModelScope.launch {
            tenantRepository.getTenantById(tenantId)?.let { tenant ->
                _uiState.update {
                    it.copy(
                        name = tenant.name,
                        phone = tenant.phone,
                        whatsappNumber = tenant.whatsappNumber ?: "",
                        email = tenant.email ?: "",
                        selectedRoomId = tenant.roomId,
                        advanceDeposit = tenant.advanceDeposit.toString(),
                        notes = tenant.notes ?: "",
                        aadhaarNumber = tenant.aadhaarNumber ?: "",
                        panNumber = tenant.panNumber ?: "",
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) } }
    fun onPhoneChange(phone: String) { _uiState.update { it.copy(phone = phone) } }
    fun onWhatsAppChange(number: String) { _uiState.update { it.copy(whatsappNumber = number) } }
    fun onEmailChange(email: String) { _uiState.update { it.copy(email = email) } }
    fun onRoomSelected(roomId: Long?) { _uiState.update { it.copy(selectedRoomId = roomId) } }
    fun onDepositChange(deposit: String) { _uiState.update { it.copy(advanceDeposit = deposit) } }
    fun onNotesChange(notes: String) { _uiState.update { it.copy(notes = notes) } }
    fun onAadhaarChange(aadhaar: String) { _uiState.update { it.copy(aadhaarNumber = aadhaar) } }
    fun onPanChange(pan: String) { _uiState.update { it.copy(panNumber = pan) } }

    fun saveTenant() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val tenant = Tenant(
                    id = if (state.isEditing) tenantId else 0,
                    roomId = state.selectedRoomId,
                    name = state.name.trim(),
                    phone = state.phone.trim(),
                    whatsappNumber = state.whatsappNumber.trim().ifBlank { null },
                    email = state.email.trim().ifBlank { null },
                    aadhaarNumber = state.aadhaarNumber.trim().ifBlank { null },
                    panNumber = state.panNumber.trim().ifBlank { null },
                    advanceDeposit = state.advanceDeposit.toDoubleOrNull() ?: 0.0,
                    notes = state.notes.trim().ifBlank { null },
                    moveInDate = System.currentTimeMillis(),
                    isActive = true
                )

                if (state.isEditing) {
                    tenantRepository.updateTenant(tenant)
                } else {
                    tenantRepository.insertTenant(tenant)
                }

                // Mark room as occupied
                state.selectedRoomId?.let {
                    tenantRepository.updateRoomStatus(it, "occupied")
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
