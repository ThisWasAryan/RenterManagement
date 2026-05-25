package com.rms.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.ElectricityReading
import com.rms.app.core.model.entities.Payment
import com.rms.app.core.model.enums.PaymentMode
import com.rms.app.core.model.relations.TenantWithRoom
import com.rms.app.core.util.DateUtils
import com.rms.app.feature.payment.RecordPaymentData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantCardData(
    val tenantWithRoom: TenantWithRoom,
    val lastPayment: Payment? = null,
    val lastReading: ElectricityReading? = null,
    val pendingBalance: Double = 0.0,
    val pendingElectricity: Double = 0.0,
    val isPaidThisMonth: Boolean = false
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val tenants: List<TenantCardData> = emptyList(),
    val activeTenantCount: Int = 0,
    val searchQuery: String = "",
    val totalCollectedThisMonth: Double = 0.0,
    val totalPendingRent: Double = 0.0,
    val overdueCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _refreshTrigger = MutableStateFlow(0)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Payment sheet state
    private val _paymentData = MutableStateFlow(RecordPaymentData())
    val paymentData: StateFlow<RecordPaymentData> = _paymentData.asStateFlow()

    private val _showPaymentSheet = MutableStateFlow(false)
    val showPaymentSheet: StateFlow<Boolean> = _showPaymentSheet.asStateFlow()

    init {
        loadTenants()
        observeTenantCount()
        observeMonthlyCollection()
    }

    fun refresh() {
        _refreshTrigger.value += 1
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadTenants() {
        viewModelScope.launch {
            combine(_searchQuery.debounce(300), _refreshTrigger) { query, _ -> query }
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        homeRepository.getActiveTenantsWithRooms()
                    } else {
                        homeRepository.searchTenantsWithRooms(query)
                    }
                }
                .collect { tenantsWithRooms ->
                    val currentMonth = DateUtils.getCurrentMonth()
                    val currentYear = DateUtils.getCurrentYear()

                    val cardDataList = tenantsWithRooms.map { twr ->
                        val lastPayment = homeRepository.getLastPayment(twr.tenant.id)
                        val lastReading = homeRepository.getLastReading(twr.tenant.id)

                        // Determine effective rent: tenant's monthlyRent > room's monthlyRent > 0
                        val effectiveRent = when {
                            twr.tenant.monthlyRent > 0 -> twr.tenant.monthlyRent
                            twr.room?.monthlyRent ?: 0.0 > 0.0 -> twr.room?.monthlyRent ?: 0.0
                            else -> 0.0
                        }

                        // Calculate pending rent based on move-in date billing cycles
                        val moveInDate = twr.tenant.moveInDate ?: System.currentTimeMillis()
                        val monthsElapsed = DateUtils.calculateElapsedMonths(moveInDate)
                        
                        val rentPayments = homeRepository.getRentPaymentsByTenantList(twr.tenant.id)
                        val totalPaid = rentPayments.sumOf { it.amount }
                        val totalExpected = monthsElapsed * effectiveRent
                        
                        val pending = if (effectiveRent > 0) (totalExpected - totalPaid).coerceAtLeast(0.0) else 0.0
                        val pendingElectricity = if (lastReading != null && !lastReading.isPaid) lastReading.totalAmount else 0.0

                        TenantCardData(
                            tenantWithRoom = twr,
                            lastPayment = lastPayment,
                            lastReading = lastReading,
                            pendingBalance = pending,
                            pendingElectricity = pendingElectricity,
                            isPaidThisMonth = pending <= 0.0
                        )
                    }

                    val totalPending = cardDataList.sumOf { it.pendingBalance }
                    val overdueCount = cardDataList.count { !it.isPaidThisMonth && it.tenantWithRoom.tenant.monthlyRent > 0 }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tenants = cardDataList,
                            totalPendingRent = totalPending,
                            overdueCount = overdueCount,
                            error = null
                        )
                    }
                }
        }
    }

    private fun observeTenantCount() {
        viewModelScope.launch {
            homeRepository.getActiveTenantCount().collect { count ->
                _uiState.update { it.copy(activeTenantCount = count) }
            }
        }
    }

    private fun observeMonthlyCollection() {
        viewModelScope.launch {
            homeRepository.getTotalCollectedForMonth(
                DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()
            ).collect { total ->
                _uiState.update { it.copy(totalCollectedThisMonth = total ?: 0.0) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Payment sheet actions
    fun openPaymentSheet(tenantId: Long) {
        viewModelScope.launch {
            val tenant = homeRepository.getTenantById(tenantId)
            val room = tenant?.roomId?.let { homeRepository.getRoomById(it) }
            val effectiveRent = when {
                (tenant?.monthlyRent ?: 0.0) > 0 -> tenant?.monthlyRent ?: 0.0
                (room?.monthlyRent ?: 0.0) > 0 -> room?.monthlyRent ?: 0.0
                else -> 0.0
            }

            val payments = homeRepository.getRentPaymentsByTenantList(tenantId)
            
            // Calculate unpaid months
            val moveInDate = tenant?.moveInDate ?: System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = moveInDate
            val startMonth = calendar.get(java.util.Calendar.MONTH) + 1
            val startYear = calendar.get(java.util.Calendar.YEAR)
            
            val monthsElapsed = DateUtils.calculateElapsedMonths(moveInDate)
            
            val allMonths = mutableListOf<Pair<Int, Int>>()
            var tempMonth = startMonth
            var tempYear = startYear
            
            // Add cycles up to elapsed months (plus one for optional advance payment)
            for (i in 0..monthsElapsed) {
                allMonths.add(tempMonth to tempYear)
                tempMonth++
                if (tempMonth > 12) {
                    tempMonth = 1
                    tempYear++
                }
            }
            
            val paidMonths = payments.map { it.forMonth to it.forYear }.toSet()
            val unpaidMonths = allMonths.filter { it !in paidMonths }.sortedWith(compareBy({ it.second }, { it.first }))
            
            val defaultMonth = unpaidMonths.firstOrNull() ?: (DateUtils.getCurrentMonth() to DateUtils.getCurrentYear())

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
            _showPaymentSheet.value = true
        }
    }

    fun dismissPaymentSheet() {
        _showPaymentSheet.value = false
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
                    tenantId = data.tenantId,
                    amount = amount,
                    type = "RENT",
                    mode = data.paymentMode.name,
                    paymentDate = System.currentTimeMillis(),
                    forMonth = data.forMonth,
                    forYear = data.forYear,
                    notes = data.notes.ifBlank { null }
                )
                homeRepository.insertPayment(payment)
                _paymentData.update { it.copy(isSaving = false) }
                _showPaymentSheet.value = false

                // Refresh tenant list
                loadTenants()
            } catch (e: Exception) {
                _paymentData.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun sendWhatsAppReminder(context: android.content.Context, tenantId: Long, templateType: String) {
        viewModelScope.launch {
            val tenant = homeRepository.getTenantById(tenantId) ?: return@launch
            val room = tenant.roomId?.let { homeRepository.getRoomById(it) }
            val lastReading = homeRepository.getLastReading(tenantId)

            val effectiveRent = when {
                tenant.monthlyRent > 0 -> tenant.monthlyRent
                room?.monthlyRent ?: 0.0 > 0.0 -> room?.monthlyRent ?: 0.0
                else -> 0.0
            }
            val moveInDate = tenant.moveInDate ?: System.currentTimeMillis()
            val monthsElapsed = DateUtils.calculateElapsedMonths(moveInDate)
            val rentPayments = homeRepository.getRentPaymentsByTenantList(tenantId)
            val totalPaidRent = rentPayments.sumOf { it.amount }
            val totalExpected = monthsElapsed * effectiveRent
            
            val pendingRent = if (effectiveRent > 0) (totalExpected - totalPaidRent).coerceAtLeast(0.0) else 0.0
            val pendingElectricity = if (lastReading != null && !lastReading.isPaid) lastReading.totalAmount else 0.0
            
            val phone = tenant.whatsappNumber ?: tenant.phone
            
            if (templateType == "CUSTOM_MESSAGE") {
                com.rms.app.core.util.WhatsAppHelper.sendCustomMessage(context, phone, "")
                return@launch
            }

            val template = homeRepository.getTemplate(templateType)
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
                "PAYMENT_CONFIRMATION" -> com.rms.app.core.util.CurrencyUtils.formatAmountCompact(totalPaidRent) // Or recent payment amount
                else -> com.rms.app.core.util.CurrencyUtils.formatAmountCompact(pendingRent)
            }

            val args = mapOf(
                "tenantName" to tenant.name,
                "amount" to amountStr,
                "rentAmount" to com.rms.app.core.util.CurrencyUtils.formatAmountCompact(pendingRent),
                "electricityAmount" to com.rms.app.core.util.CurrencyUtils.formatAmountCompact(pendingElectricity),
                "totalAmount" to com.rms.app.core.util.CurrencyUtils.formatAmountCompact(pendingRent + pendingElectricity),
                "month" to DateUtils.formatMonthYear(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()),
                "units" to (lastReading?.unitsConsumed?.toString() ?: "0"),
                "previousReading" to (lastReading?.previousReading?.toString() ?: "0"),
                "currentReading" to (lastReading?.currentReading?.toString() ?: "0"),
                "roomNumber" to (room?.roomNumber ?: "")
            )
            
            com.rms.app.core.util.WhatsAppHelper.formatAndSendMessage(context, phone, templateText, args)
        }
    }
}
