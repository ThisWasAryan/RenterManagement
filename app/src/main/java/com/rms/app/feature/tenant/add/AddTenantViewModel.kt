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
    // Personal
    val name: String = "",
    val phone: String = "",
    val whatsappNumber: String = "",
    val email: String = "",
    // Room & Rent
    val selectedRoomId: Long? = null,
    val roomNumber: String = "",
    val monthlyRent: String = "",
    val rentDueDay: String = "1",
    val advanceDeposit: String = "",
    val electricityRate: String = "8",
    // Move-in
    val moveInDate: Long = System.currentTimeMillis(),
    // ID
    val aadhaarNumber: String = "",
    val panNumber: String = "",
    // Other
    val notes: String = "",
    val availableRooms: List<Room> = emptyList(),
    val useExistingRoom: Boolean = false,
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
                val room = tenant.roomId?.let { tenantRepository.getRoomById(it) }
                _uiState.update {
                    it.copy(
                        name = tenant.name,
                        phone = tenant.phone,
                        whatsappNumber = tenant.whatsappNumber ?: "",
                        email = tenant.email ?: "",
                        selectedRoomId = tenant.roomId,
                        roomNumber = room?.roomNumber ?: "",
                        monthlyRent = if (tenant.monthlyRent > 0) tenant.monthlyRent.toInt().toString() else "",
                        rentDueDay = tenant.rentDueDay.toString(),
                        advanceDeposit = if (tenant.advanceDeposit > 0) tenant.advanceDeposit.toInt().toString() else "",
                        electricityRate = tenant.electricityRate.toString(),
                        moveInDate = tenant.moveInDate ?: System.currentTimeMillis(),
                        notes = tenant.notes ?: "",
                        aadhaarNumber = tenant.aadhaarNumber ?: "",
                        panNumber = tenant.panNumber ?: "",
                        isEditing = true,
                        useExistingRoom = tenant.roomId != null
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name, error = null) } }
    fun onPhoneChange(phone: String) { _uiState.update { it.copy(phone = phone) } }
    fun onWhatsAppChange(number: String) { _uiState.update { it.copy(whatsappNumber = number) } }
    fun onEmailChange(email: String) { _uiState.update { it.copy(email = email) } }
    fun onRoomSelected(roomId: Long?) {
        _uiState.update {
            val room = it.availableRooms.find { r -> r.id == roomId }
            it.copy(
                selectedRoomId = roomId,
                roomNumber = room?.roomNumber ?: "",
                monthlyRent = if (room != null && room.monthlyRent > 0) room.monthlyRent.toInt().toString() else it.monthlyRent,
                useExistingRoom = roomId != null
            )
        }
    }
    fun onRoomNumberChange(number: String) { _uiState.update { it.copy(roomNumber = number, selectedRoomId = null, useExistingRoom = false) } }
    fun onMonthlyRentChange(rent: String) { _uiState.update { it.copy(monthlyRent = rent) } }
    fun onRentDueDayChange(day: String) { _uiState.update { it.copy(rentDueDay = day) } }
    fun onDepositChange(deposit: String) { _uiState.update { it.copy(advanceDeposit = deposit) } }
    fun onElectricityRateChange(rate: String) { _uiState.update { it.copy(electricityRate = rate) } }
    fun onNotesChange(notes: String) { _uiState.update { it.copy(notes = notes) } }
    fun onAadhaarChange(aadhaar: String) { _uiState.update { it.copy(aadhaarNumber = aadhaar) } }
    fun onPanChange(pan: String) { _uiState.update { it.copy(panNumber = pan) } }
    fun toggleUseExistingRoom() { _uiState.update { it.copy(useExistingRoom = !it.useExistingRoom, selectedRoomId = null) } }

    fun saveTenant() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                var roomId = state.selectedRoomId

                // If no existing room selected but room number provided, create a new room
                if (roomId == null && state.roomNumber.isNotBlank()) {
                    val newRoom = Room(
                        propertyId = 1L, // Default property
                        roomNumber = state.roomNumber.trim(),
                        floor = "",
                        monthlyRent = state.monthlyRent.toDoubleOrNull() ?: 0.0,
                        securityDeposit = state.advanceDeposit.toDoubleOrNull() ?: 0.0,
                        status = "occupied"
                    )
                    roomId = tenantRepository.insertRoom(newRoom)
                }

                val tenant = Tenant(
                    id = if (state.isEditing) tenantId else 0,
                    roomId = roomId,
                    name = state.name.trim(),
                    phone = state.phone.trim(),
                    whatsappNumber = state.whatsappNumber.trim().ifBlank { null },
                    email = state.email.trim().ifBlank { null },
                    aadhaarNumber = state.aadhaarNumber.trim().ifBlank { null },
                    panNumber = state.panNumber.trim().ifBlank { null },
                    advanceDeposit = state.advanceDeposit.toDoubleOrNull() ?: 0.0,
                    monthlyRent = state.monthlyRent.toDoubleOrNull() ?: 0.0,
                    rentDueDay = state.rentDueDay.toIntOrNull()?.coerceIn(1, 28) ?: 1,
                    electricityRate = state.electricityRate.toDoubleOrNull() ?: 8.0,
                    notes = state.notes.trim().ifBlank { null },
                    moveInDate = state.moveInDate,
                    isActive = true
                )

                if (state.isEditing) {
                    tenantRepository.updateTenant(tenant)
                } else {
                    tenantRepository.insertTenant(tenant)
                }

                // Mark room as occupied
                roomId?.let {
                    tenantRepository.updateRoomStatus(it, "occupied")
                }

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
