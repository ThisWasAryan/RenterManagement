package com.rms.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.rms.app.core.database.dao.PropertyDao
import com.rms.app.core.database.dao.RoomDao
import com.rms.app.core.model.entities.Property
import com.rms.app.core.model.entities.Room
import com.rms.app.core.ui.theme.ThemeManager
import com.rms.app.core.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val electricityRate: String = "8",
    val properties: List<Property> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val showAddPropertyDialog: Boolean = false,
    val showAddRoomDialog: Boolean = false,
    
    // Edit/Delete Property
    val showEditPropertyDialog: Property? = null,
    
    // Edit/Delete Room
    val showEditRoomDialog: Room? = null,
    val showSyncRentDialog: Pair<Room, Double>? = null, // Used when editing room rent to ask if sync is desired

    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val themeManager: ThemeManager,
    private val propertyDao: PropertyDao,
    private val roomDao: RoomDao,
    private val tenantDao: com.rms.app.core.database.dao.TenantDao
) : ViewModel() {

    companion object {
        val ELECTRICITY_RATE_KEY = stringPreferencesKey("electricity_rate")
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadProperties()
        loadRooms()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _uiState.update {
                    it.copy(
                        electricityRate = prefs[ELECTRICITY_RATE_KEY] ?: "8"
                    )
                }
            }
        }
        viewModelScope.launch {
            themeManager.themeMode.collect { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
    }

    private fun loadProperties() {
        viewModelScope.launch {
            propertyDao.getAllProperties().collect { properties ->
                _uiState.update { it.copy(properties = properties) }
            }
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            roomDao.getAllRooms().collect { rooms ->
                _uiState.update { it.copy(rooms = rooms) }
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeManager.setThemeMode(mode)
        }
    }


    fun updateElectricityRate(rate: String) {
        _uiState.update { it.copy(electricityRate = rate) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[ELECTRICITY_RATE_KEY] = rate }
        }
    }

    fun addProperty(name: String, address: String) {
        viewModelScope.launch {
            propertyDao.insertProperty(Property(name = name, address = address))
        }
    }

    fun addRoom(propertyId: Long, roomNumber: String, floor: String, monthlyRent: Double) {
        viewModelScope.launch {
            roomDao.insertRoom(Room(propertyId = propertyId, roomNumber = roomNumber, floor = floor, monthlyRent = monthlyRent))
        }
    }

    fun toggleAddPropertyDialog() {
        _uiState.update { it.copy(showAddPropertyDialog = !it.showAddPropertyDialog) }
    }

    fun toggleAddRoomDialog() {
        _uiState.update { it.copy(showAddRoomDialog = !it.showAddRoomDialog) }
    }

    // Property Editing & Deletion
    fun openEditPropertyDialog(property: Property) {
        _uiState.update { it.copy(showEditPropertyDialog = property) }
    }
    
    fun closeEditPropertyDialog() {
        _uiState.update { it.copy(showEditPropertyDialog = null) }
    }

    fun updateProperty(property: Property, newName: String, newAddress: String) {
        viewModelScope.launch {
            propertyDao.updateProperty(property.copy(name = newName, address = newAddress))
        }
    }

    fun deleteProperty(property: Property) {
        viewModelScope.launch {
            val count = roomDao.getRoomsCountForProperty(property.id)
            if (count > 0) {
                _uiState.update { it.copy(error = "Cannot delete property. It contains $count rooms. Please delete or reassign rooms first.") }
            } else {
                propertyDao.deleteProperty(property)
            }
        }
    }

    // Room Editing & Deletion
    fun openEditRoomDialog(room: Room) {
        _uiState.update { it.copy(showEditRoomDialog = room) }
    }
    
    fun closeEditRoomDialog() {
        _uiState.update { it.copy(showEditRoomDialog = null) }
    }

    fun updateRoom(room: Room, newRoomNumber: String, newFloor: String, newRent: Double) {
        viewModelScope.launch {
            val updatedRoom = room.copy(roomNumber = newRoomNumber, floor = newFloor, monthlyRent = newRent)
            roomDao.updateRoom(updatedRoom)
            
            if (room.monthlyRent != newRent) {
                // Ask user if they want to sync rent
                _uiState.update { it.copy(showSyncRentDialog = Pair(updatedRoom, newRent)) }
            }
        }
    }

    fun deleteRoom(room: Room) {
        viewModelScope.launch {
            val count = tenantDao.getActiveTenantsCountForRoom(room.id)
            if (count > 0) {
                _uiState.update { it.copy(error = "Cannot delete room. It has $count active tenants assigned to it.") }
            } else {
                roomDao.deleteRoom(room)
            }
        }
    }
    
    fun closeSyncRentDialog(sync: Boolean) {
        val dialogState = _uiState.value.showSyncRentDialog
        _uiState.update { it.copy(showSyncRentDialog = null) }
        
        if (sync && dialogState != null) {
            viewModelScope.launch {
                tenantDao.updateRentForActiveTenantsInRoom(dialogState.first.id, dialogState.second)
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
