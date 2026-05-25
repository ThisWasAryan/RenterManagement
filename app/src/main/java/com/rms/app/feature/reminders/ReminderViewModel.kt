package com.rms.app.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemindersUiState(
    val isLoading: Boolean = true,
    val pendingReminders: List<Reminder> = emptyList(),
    val allReminders: List<Reminder> = emptyList(),
    val showAddDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            reminderRepository.getPendingReminders().collect { pending ->
                _uiState.update { it.copy(isLoading = false, pendingReminders = pending) }
            }
        }
        viewModelScope.launch {
            reminderRepository.getAllReminders().collect { all ->
                _uiState.update { it.copy(allReminders = all) }
            }
        }
    }

    fun markAsCompleted(reminderId: Long) {
        viewModelScope.launch { reminderRepository.markAsCompleted(reminderId) }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch { reminderRepository.insertReminder(reminder) }
    }

    fun toggleAddDialog() {
        _uiState.update { it.copy(showAddDialog = !it.showAddDialog) }
    }
}
