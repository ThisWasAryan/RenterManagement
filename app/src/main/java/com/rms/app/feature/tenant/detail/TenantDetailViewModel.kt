package com.rms.app.feature.tenant.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.*
import com.rms.app.core.model.enums.PaymentMode
import com.rms.app.core.model.relations.TenantWithRoom
import com.rms.app.core.util.DateUtils
import com.rms.app.feature.payment.RecordPaymentData
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
    val error: String? = null,
    // Payment sheet
    val showPaymentSheet: Boolean = false,
    // Electricity payment dialog
    val showElectricityPayDialog: Boolean = false,
    val selectedReadingId: Long = 0,
    val electricityPayMode: PaymentMode = PaymentMode.CASH,
    // Delete tenant
    val showDeleteDialog: Boolean = false,
    val isDeleted: Boolean = false,
    // Electricity Detail
    val showElectricityDetailDialog: Boolean = false,
    val selectedElectricityReading: ElectricityReading? = null
)

@HiltViewModel
class TenantDetailViewModel @Inject constructor(
    private val tenantRepository: TenantRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tenantId: Long = savedStateHandle.get<String>("tenantId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(TenantDetailUiState())
    val uiState: StateFlow<TenantDetailUiState> = _uiState.asStateFlow()

    // Payment sheet data
    private val _paymentData = MutableStateFlow(RecordPaymentData())
    val paymentData: StateFlow<RecordPaymentData> = _paymentData.asStateFlow()

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

    fun showDeleteDialog() { _uiState.update { it.copy(showDeleteDialog = true) } }
    fun dismissDeleteDialog() { _uiState.update { it.copy(showDeleteDialog = false) } }

    fun deleteTenant() {
        viewModelScope.launch {
            val tenant = _uiState.value.tenantWithRoom?.tenant ?: return@launch
            try {
                tenantRepository.deleteTenant(tenant)
                _uiState.update { it.copy(showDeleteDialog = false, isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(showDeleteDialog = false, error = e.message) }
            }
        }
    }

    // --- Payment Recording ---

    fun openPaymentSheet() {
        viewModelScope.launch {
            val tenant = tenantRepository.getTenantById(tenantId)
            val room = tenant?.roomId?.let { tenantRepository.getRoomById(it) }
            val effectiveRent = when {
                (tenant?.monthlyRent ?: 0.0) > 0 -> tenant?.monthlyRent ?: 0.0
                (room?.monthlyRent ?: 0.0) > 0 -> room?.monthlyRent ?: 0.0
                else -> 0.0
            }


            val tenantPayments = _uiState.value.payments.filter { it.type == "RENT" }

            // Calculate unpaid months
            val moveInDate = tenant?.moveInDate ?: System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = moveInDate
            val startMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val startYear = calendar.get(java.util.Calendar.YEAR)
            
            val currentMonth = DateUtils.getCurrentMonth()
            val currentYear = DateUtils.getCurrentYear()
            
            val allMonths = mutableListOf<Pair<Int, Int>>()
            var tempMonth = startMonth
            var tempYear = startYear
            while (tempYear < currentYear || (tempYear == currentYear && tempMonth <= currentMonth)) {
                allMonths.add(tempMonth to tempYear)
                tempMonth++
                if (tempMonth > 12) {
                    tempMonth = 1
                    tempYear++
                }
            }
            
            // Allow next month as well
            val nextMonth = if (currentMonth == 12) 1 else currentMonth + 1
            val nextYear = if (currentMonth == 12) currentYear + 1 else currentYear
            allMonths.add(nextMonth to nextYear)
            
            val paidMonths = tenantPayments.map { it.forMonth to it.forYear }.toSet()
            val unpaidMonths = allMonths.filter { it !in paidMonths }.sortedWith(compareBy({ it.second }, { it.first }))
            
            val defaultMonth = unpaidMonths.firstOrNull() ?: (currentMonth to currentYear)

            _paymentData.value = RecordPaymentData(
                tenantId = tenantId,
                tenantName = tenant?.name ?: "",
                roomNumber = room?.roomNumber ?: "",
                suggestedAmount = effectiveRent,
                amount = if (effectiveRent > 0) effectiveRent.toInt().toString() else "",
                forMonth = defaultMonth.first,
                forYear = defaultMonth.second,
                unpaidMonths = unpaidMonths
            )
            _uiState.update { it.copy(showPaymentSheet = true) }
        }
    }

    fun dismissPaymentSheet() {
        _uiState.update { it.copy(showPaymentSheet = false) }
    }

    fun onPaymentAmountChange(amount: String) {
        _paymentData.update { it.copy(amount = amount, error = null) }
    }

    fun onPaymentModeChange(mode: PaymentMode) {
        _paymentData.update { it.copy(paymentMode = mode) }
    }

    fun onPaymentMonthChange(month: Int) {
        _paymentData.update { it.copy(forMonth = month) }
    }

    fun onPaymentYearChange(year: Int) {
        _paymentData.update { it.copy(forYear = year) }
    }

    fun onPaymentNotesChange(notes: String) {
        _paymentData.update { it.copy(notes = notes) }
    }

    fun savePayment() {
        val data = _paymentData.value
        val amount = data.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _paymentData.update { it.copy(error = "Enter a valid amount") }
            return
        }

        viewModelScope.launch {
            _paymentData.update { it.copy(isSaving = true, error = null) }
            try {
                val payment = Payment(
                    tenantId = tenantId,
                    amount = amount,
                    type = "RENT",
                    mode = data.paymentMode.name,
                    paymentDate = System.currentTimeMillis(),
                    forMonth = data.forMonth,
                    forYear = data.forYear,
                    notes = data.notes.ifBlank { null }
                )
                tenantRepository.insertPayment(payment)
                _paymentData.update { it.copy(isSaving = false) }
                _uiState.update { it.copy(showPaymentSheet = false) }
            } catch (e: Exception) {
                _paymentData.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    // --- Electricity Payment ---

    fun showElectricityPayDialog(readingId: Long) {
        _uiState.update { it.copy(showElectricityPayDialog = true, selectedReadingId = readingId) }
    }

    fun dismissElectricityPayDialog() {
        _uiState.update { it.copy(showElectricityPayDialog = false, selectedReadingId = 0) }
    }

    fun onElectricityPayModeChange(mode: PaymentMode) {
        _uiState.update { it.copy(electricityPayMode = mode) }
    }

    fun markElectricityPaid() {
        val readingId = _uiState.value.selectedReadingId
        val mode = _uiState.value.electricityPayMode
        if (readingId <= 0) return

        viewModelScope.launch {
            try {
                tenantRepository.markElectricityPaid(readingId, System.currentTimeMillis(), mode.name)
                _uiState.update { it.copy(showElectricityPayDialog = false, selectedReadingId = 0) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun showElectricityDetail(reading: ElectricityReading) {
        _uiState.update { it.copy(showElectricityDetailDialog = true, selectedElectricityReading = reading) }
    }
    
    fun dismissElectricityDetail() {
        _uiState.update { it.copy(showElectricityDetailDialog = false, selectedElectricityReading = null) }
    }

    fun sendWhatsAppReminder(context: android.content.Context, templateType: String) {
        viewModelScope.launch {
            val twr = _uiState.value.tenantWithRoom ?: return@launch
            val tenant = twr.tenant
            val room = twr.room
            
            // Assume we can compute the due amounts by taking all readings and payments
            // Since we need pending, we might just calculate it
            val lastReading = _uiState.value.electricityReadings.maxByOrNull { it.readingDate }
            val currentMonthPayment = _uiState.value.payments.find { 
                it.forMonth == DateUtils.getCurrentMonth() && it.forYear == DateUtils.getCurrentYear() 
            }
            
            val effectiveRent = when {
                tenant.monthlyRent > 0 -> tenant.monthlyRent
                room?.monthlyRent ?: 0.0 > 0.0 -> room?.monthlyRent ?: 0.0
                else -> 0.0
            }
            
            val paidAmount = currentMonthPayment?.amount ?: 0.0
            val pendingRent = if (effectiveRent > 0) (effectiveRent - paidAmount).coerceAtLeast(0.0) else 0.0
            val pendingElectricity = if (lastReading != null && !lastReading.isPaid) lastReading.totalAmount else 0.0
            
            val phone = tenant.whatsappNumber ?: tenant.phone
            
            if (templateType == "CUSTOM_MESSAGE") {
                com.rms.app.core.util.WhatsAppHelper.sendCustomMessage(context, phone, "")
                return@launch
            }

            val template = tenantRepository.getTemplate(templateType)
            val templateText = template?.messageTemplate ?: when (templateType) {
                "RENT_REMINDER" -> com.rms.app.core.util.WhatsAppHelper.getDefaultRentReminderTemplate()
                "ELECTRICITY_REMINDER" -> com.rms.app.core.util.WhatsAppHelper.getDefaultElectricityReminderTemplate()
                "COMBINED_REMINDER" -> com.rms.app.core.util.WhatsAppHelper.getDefaultCombinedReminderTemplate()
                "PAYMENT_CONFIRMATION" -> com.rms.app.core.util.WhatsAppHelper.getDefaultPaymentConfirmationTemplate()
                else -> com.rms.app.core.util.WhatsAppHelper.getDefaultRentReminderTemplate()
            }
            
            val amountStr = when (templateType) {
                "ELECTRICITY_REMINDER" -> com.rms.app.core.util.CurrencyUtils.formatAmountCompact(pendingElectricity)
                "COMBINED_REMINDER" -> com.rms.app.core.util.CurrencyUtils.formatAmountCompact(pendingRent + pendingElectricity)
                "PAYMENT_CONFIRMATION" -> com.rms.app.core.util.CurrencyUtils.formatAmountCompact(paidAmount)
                else -> com.rms.app.core.util.CurrencyUtils.formatAmountCompact(pendingRent)
            }

            val args = mapOf(
                "tenantName" to tenant.name,
                "amount" to amountStr,
                "month" to DateUtils.formatMonthYear(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear())
            )
            
            com.rms.app.core.util.WhatsAppHelper.formatAndSendMessage(context, phone, templateText, args)
        }
    }

    fun deleteDocument(document: Document) {
        viewModelScope.launch {
            try {
                tenantRepository.deleteDocument(document)
                // Reload documents is handled by the Flow in loadTenantDetails
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
