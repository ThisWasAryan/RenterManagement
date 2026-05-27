package com.rms.app.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.Payment
import com.rms.app.core.model.entities.Tenant
import com.rms.app.core.model.enums.PaymentMode
import com.rms.app.core.model.enums.PaymentType
import com.rms.app.core.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val isLoading: Boolean = true,
    val payments: List<Payment> = emptyList(),
    val totalCollected: Double = 0.0,
    val error: String? = null
)

data class RecordPaymentState(
    val tenantId: Long = 0,
    val tenantName: String = "",
    val amount: String = "",
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val paymentType: PaymentType = PaymentType.RENT,
    val forMonth: Int = DateUtils.getCurrentMonth(),
    val forYear: Int = DateUtils.getCurrentYear(),
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _recordState = MutableStateFlow(RecordPaymentState())
    val recordState: StateFlow<RecordPaymentState> = _recordState.asStateFlow()

    init {
        loadPayments()
    }

    private fun loadPayments() {
        viewModelScope.launch {
            paymentRepository.getAllPayments().collect { payments ->
                _uiState.update { it.copy(isLoading = false, payments = payments) }
            }
        }
        viewModelScope.launch {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis

            calendar.add(java.util.Calendar.MONTH, 1)
            calendar.add(java.util.Calendar.MILLISECOND, -1)
            val endOfMonth = calendar.timeInMillis
            
            paymentRepository.getTotalCollectedBetweenDates(startOfMonth, endOfMonth).collect { total ->
                _uiState.update { it.copy(totalCollected = total ?: 0.0) }
            }
        }
    }

    fun initRecordPayment(tenantId: Long) {
        viewModelScope.launch {
            val tenant = paymentRepository.getTenantById(tenantId)
            _recordState.update {
                it.copy(
                    tenantId = tenantId,
                    tenantName = tenant?.name ?: "",
                    isSaved = false
                )
            }
        }
    }

    fun onAmountChange(amount: String) { _recordState.update { it.copy(amount = amount) } }
    fun onModeChange(mode: PaymentMode) { _recordState.update { it.copy(paymentMode = mode) } }
    fun onTypeChange(type: PaymentType) { _recordState.update { it.copy(paymentType = type) } }
    fun onMonthChange(month: Int) { _recordState.update { it.copy(forMonth = month) } }
    fun onYearChange(year: Int) { _recordState.update { it.copy(forYear = year) } }
    fun onNotesChange(notes: String) { _recordState.update { it.copy(notes = notes) } }

    fun savePayment() {
        val state = _recordState.value
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _recordState.update { it.copy(error = "Enter a valid amount") }
            return
        }

        viewModelScope.launch {
            _recordState.update { it.copy(isSaving = true, error = null) }
            try {
                val payment = Payment(
                    tenantId = state.tenantId,
                    amount = amount,
                    type = state.paymentType.name,
                    mode = state.paymentMode.name,
                    paymentDate = System.currentTimeMillis(),
                    forMonth = state.forMonth,
                    forYear = state.forYear,
                    notes = state.notes.ifBlank { null }
                )
                paymentRepository.insertPayment(payment)
                _recordState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _recordState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
