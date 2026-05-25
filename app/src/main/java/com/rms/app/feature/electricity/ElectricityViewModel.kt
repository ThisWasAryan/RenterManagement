package com.rms.app.feature.electricity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.ElectricityReading
import com.rms.app.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddReadingUiState(
    val tenantId: Long = 0,
    val tenantName: String = "",
    val previousReading: String = "0",
    val currentReading: String = "",
    val ratePerUnit: String = "8",
    val forMonth: Int = DateUtils.getCurrentMonth(),
    val forYear: Int = DateUtils.getCurrentYear(),
    val unitsConsumed: Double = 0.0,
    val totalAmount: Double = 0.0,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ElectricityViewModel @Inject constructor(
    private val electricityRepository: ElectricityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tenantId: Long = savedStateHandle.get<String>("tenantId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(AddReadingUiState(tenantId = tenantId))
    val uiState: StateFlow<AddReadingUiState> = _uiState.asStateFlow()

    init {
        loadLastReading()
        loadTenantName()
    }

    private fun loadLastReading() {
        viewModelScope.launch {
            val lastReading = electricityRepository.getLastReading(tenantId)
            _uiState.update {
                it.copy(previousReading = lastReading?.currentReading?.toString() ?: "0")
            }
        }
    }

    private fun loadTenantName() {
        viewModelScope.launch {
            val tenant = electricityRepository.getTenantById(tenantId)
            _uiState.update { it.copy(tenantName = tenant?.name ?: "") }
        }
    }

    fun onCurrentReadingChange(reading: String) {
        _uiState.update { state ->
            val current = reading.toDoubleOrNull() ?: 0.0
            val previous = state.previousReading.toDoubleOrNull() ?: 0.0
            val units = (current - previous).coerceAtLeast(0.0)
            val rate = state.ratePerUnit.toDoubleOrNull() ?: 0.0
            state.copy(
                currentReading = reading,
                unitsConsumed = units,
                totalAmount = units * rate
            )
        }
    }

    fun onRateChange(rate: String) {
        _uiState.update { state ->
            val rateVal = rate.toDoubleOrNull() ?: 0.0
            state.copy(ratePerUnit = rate, totalAmount = state.unitsConsumed * rateVal)
        }
    }

    fun onPreviousReadingChange(reading: String) {
        _uiState.update { state ->
            val prev = reading.toDoubleOrNull() ?: 0.0
            val current = state.currentReading.toDoubleOrNull() ?: 0.0
            val units = (current - prev).coerceAtLeast(0.0)
            val rate = state.ratePerUnit.toDoubleOrNull() ?: 0.0
            state.copy(previousReading = reading, unitsConsumed = units, totalAmount = units * rate)
        }
    }

    fun saveReading() {
        val state = _uiState.value
        val current = state.currentReading.toDoubleOrNull()
        if (current == null || current <= 0) {
            _uiState.update { it.copy(error = "Enter a valid current reading") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val reading = ElectricityReading(
                    tenantId = tenantId,
                    previousReading = state.previousReading.toDoubleOrNull() ?: 0.0,
                    currentReading = current,
                    unitsConsumed = state.unitsConsumed,
                    ratePerUnit = state.ratePerUnit.toDoubleOrNull() ?: 8.0,
                    totalAmount = state.totalAmount,
                    forMonth = state.forMonth,
                    forYear = state.forYear
                )
                electricityRepository.insertReading(reading)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
