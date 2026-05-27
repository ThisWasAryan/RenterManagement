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
import com.rms.app.core.model.entities.Property
import com.rms.app.core.database.dao.PropertyDao

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
    val availableProperties: List<Property> = emptyList(),
    val useExistingRoom: Boolean = false,
    val selectedPropertyId: Long? = null,
    val newPropertyName: String = "",
    val newPropertyAddress: String = "",
    val roomFloor: String = "",
    val roomNotes: String = "",
    val sameAsPhone: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    // Rent Sync Dialog
    val showRentSyncDialog: Boolean = false,
    val pendingTenantToSave: Tenant? = null,
    val relatedRoomIdForSync: Long? = null
)

@HiltViewModel
class AddTenantViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    private val propertyDao: PropertyDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tenantId: Long = savedStateHandle.get<String>("tenantId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(AddTenantUiState())
    val uiState: StateFlow<AddTenantUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
        loadProperties()
        if (tenantId > 0) loadTenant()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            propertyDao.getAllProperties().collect { props ->
                _uiState.update { it.copy(availableProperties = props) }
            }
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            tenantRepository.getAllRooms().collect { rooms ->
                // Filter to show only available rooms, plus the room currently assigned to this tenant if editing
                val currentRoomId = _uiState.value.selectedRoomId
                val assignableRooms = rooms.filter { it.status == "available" || it.id == currentRoomId }
                _uiState.update { it.copy(availableRooms = assignableRooms) }
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
    fun onPhoneChange(phone: String) { 
        _uiState.update { 
            it.copy(
                phone = phone,
                whatsappNumber = if (it.sameAsPhone) phone else it.whatsappNumber
            ) 
        } 
    }
    fun onWhatsAppChange(number: String) { _uiState.update { it.copy(whatsappNumber = number, sameAsPhone = false) } }
    fun toggleSameAsPhone() {
        _uiState.update {
            val newSameAsPhone = !it.sameAsPhone
            it.copy(
                sameAsPhone = newSameAsPhone,
                whatsappNumber = if (newSameAsPhone) it.phone else it.whatsappNumber
            )
        }
    }
    fun onEmailChange(email: String) { _uiState.update { it.copy(email = email) } }
    fun onPropertySelected(propertyId: Long?) { _uiState.update { it.copy(selectedPropertyId = propertyId) } }
    fun onNewPropertyNameChange(name: String) { _uiState.update { it.copy(newPropertyName = name, selectedPropertyId = null) } }
    fun onNewPropertyAddressChange(address: String) { _uiState.update { it.copy(newPropertyAddress = address, selectedPropertyId = null) } }
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
    fun onRoomFloorChange(floor: String) { _uiState.update { it.copy(roomFloor = floor, selectedRoomId = null, useExistingRoom = false) } }
    fun onRoomNotesChange(notes: String) { _uiState.update { it.copy(roomNotes = notes, selectedRoomId = null, useExistingRoom = false) } }
    fun onMonthlyRentChange(rent: String) { _uiState.update { it.copy(monthlyRent = rent) } }
    fun onDepositChange(deposit: String) { _uiState.update { it.copy(advanceDeposit = deposit) } }
    fun onElectricityRateChange(rate: String) { _uiState.update { it.copy(electricityRate = rate) } }
    fun onMoveInDateSelected(date: Long) { _uiState.update { it.copy(moveInDate = date) } }
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
                    var propId = state.selectedPropertyId
                    if (propId == null) {
                        if (state.newPropertyName.isNotBlank()) {
                            propId = propertyDao.insertProperty(Property(name = state.newPropertyName.trim(), address = state.newPropertyAddress.trim()))
                        } else {
                            // Default property if none provided
                            val existingProps = propertyDao.getAllProperties().first()
                            if (existingProps.isEmpty()) {
                                propId = propertyDao.insertProperty(Property(name = "Default Property", address = ""))
                            } else {
                                propId = existingProps.first().id
                            }
                        }
                    }

                    val newRoom = Room(
                        propertyId = propId,
                        roomNumber = state.roomNumber.trim(),
                        floor = state.roomFloor.trim(),
                        monthlyRent = state.monthlyRent.toDoubleOrNull() ?: 0.0,
                        securityDeposit = state.advanceDeposit.toDoubleOrNull() ?: 0.0,
                        status = "occupied"
                    )
                    roomId = tenantRepository.insertRoom(newRoom)
                }

                // Strict Room Occupancy Validation
                if (roomId != null) {
                    val activeCount = tenantRepository.getActiveTenantsCountForRoom(roomId)
                    // If editing, the count might include the current tenant (count == 1). 
                    // If moving into a new room, count must be 0.
                    val isEditingCurrentRoom = state.isEditing && roomId == tenantId
                    // Actually, if editing, we might be keeping the same room.
                    val originalTenant = if (state.isEditing) tenantRepository.getTenantById(tenantId) else null
                    val isSameRoom = originalTenant?.roomId == roomId

                    if (activeCount > 0 && !isSameRoom) {
                        _uiState.update { it.copy(isSaving = false, error = "Room is already occupied by another active tenant.") }
                        return@launch
                    }
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
                    electricityRate = state.electricityRate.toDoubleOrNull() ?: 8.0,
                    notes = state.notes.trim().ifBlank { null },
                    moveInDate = state.moveInDate,
                    isActive = true
                )

                // Rent Sync Check
                if (roomId != null) {
                    val room = tenantRepository.getRoomById(roomId)
                    if (room != null && tenant.monthlyRent != room.monthlyRent && room.monthlyRent > 0.0) {
                        _uiState.update { 
                            it.copy(
                                showRentSyncDialog = true,
                                pendingTenantToSave = tenant,
                                relatedRoomIdForSync = roomId,
                                isSaving = false
                            )
                        }
                        return@launch
                    }
                }

                proceedWithSave(tenant, roomId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun confirmRentSync(updateRoom: Boolean) {
        val state = _uiState.value
        val tenant = state.pendingTenantToSave ?: return
        val roomId = state.relatedRoomIdForSync

        viewModelScope.launch {
            _uiState.update { it.copy(showRentSyncDialog = false, isSaving = true) }
            try {
                if (updateRoom && roomId != null) {
                    val room = tenantRepository.getRoomById(roomId)
                    if (room != null) {
                        tenantRepository.updateRoom(room.copy(monthlyRent = tenant.monthlyRent))
                    }
                }
                proceedWithSave(tenant, roomId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun dismissRentSyncDialog() {
        _uiState.update { it.copy(showRentSyncDialog = false, pendingTenantToSave = null, relatedRoomIdForSync = null) }
    }

    private suspend fun proceedWithSave(tenant: Tenant, roomId: Long?) {
        if (_uiState.value.isEditing) {
            tenantRepository.updateTenant(tenant)
        } else {
            tenantRepository.insertTenant(tenant)
        }

        roomId?.let {
            tenantRepository.updateRoomStatus(it, "occupied")
        }

        _uiState.update { it.copy(isSaving = false, isSaved = true) }
    }
}
