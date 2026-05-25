package com.rms.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.rms.app.core.database.dao.PropertyDao
import com.rms.app.core.database.dao.RoomDao
import com.rms.app.core.model.entities.Property
import com.rms.app.core.model.entities.Room
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val defaultRent: String = "10000",
    val electricityRate: String = "8",
    val properties: List<Property> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val showAddPropertyDialog: Boolean = false,
    val showAddRoomDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val propertyDao: PropertyDao,
    private val roomDao: RoomDao
) : ViewModel() {

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val DEFAULT_RENT_KEY = stringPreferencesKey("default_rent")
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
                        isDarkMode = prefs[DARK_MODE_KEY] ?: true,
                        defaultRent = prefs[DEFAULT_RENT_KEY] ?: "10000",
                        electricityRate = prefs[ELECTRICITY_RATE_KEY] ?: "8"
                    )
                }
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

    fun toggleDarkMode() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[DARK_MODE_KEY] ?: true
                prefs[DARK_MODE_KEY] = !current
            }
        }
    }

    fun updateDefaultRent(rent: String) {
        _uiState.update { it.copy(defaultRent = rent) }
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[DEFAULT_RENT_KEY] = rent }
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
}
