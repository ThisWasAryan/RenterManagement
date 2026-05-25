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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadTenants() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
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
                        val currentMonthPayment = homeRepository.getPaymentForMonth(
                            twr.tenant.id, currentMonth, currentYear
                        )

                        // Determine effective rent: tenant's monthlyRent > room's monthlyRent > 0
                        val effectiveRent = when {
                            twr.tenant.monthlyRent > 0 -> twr.tenant.monthlyRent
                            twr.room?.monthlyRent ?: 0.0 > 0.0 -> twr.room?.monthlyRent ?: 0.0
                            else -> 0.0
                        }

                        val isPaid = currentMonthPayment != null
                        val paidAmount = currentMonthPayment?.amount ?: 0.0
                        val pending = if (effectiveRent > 0) (effectiveRent - paidAmount).coerceAtLeast(0.0) else 0.0

                        TenantCardData(
                            tenantWithRoom = twr,
                            lastPayment = lastPayment,
                            lastReading = lastReading,
                            pendingBalance = pending,
                            isPaidThisMonth = isPaid && paidAmount >= effectiveRent
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

            _paymentData.value = RecordPaymentData(
                tenantId = tenantId,
                tenantName = tenant?.name ?: "",
                roomNumber = room?.roomNumber ?: "",
                suggestedAmount = effectiveRent,
                amount = if (effectiveRent > 0) effectiveRent.toInt().toString() else "",
                forMonth = DateUtils.getCurrentMonth(),
                forYear = DateUtils.getCurrentYear()
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

    fun sendWhatsAppReminder(context: android.content.Context, tenantId: Long) {
        viewModelScope.launch {
            val tenant = homeRepository.getTenantById(tenantId) ?: return@launch
            val room = tenant.roomId?.let { homeRepository.getRoomById(it) }
            val effectiveRent = when {
                tenant.monthlyRent > 0 -> tenant.monthlyRent
                room?.monthlyRent ?: 0.0 > 0.0 -> room?.monthlyRent ?: 0.0
                else -> 0.0
            }
            
            val phone = tenant.whatsappNumber ?: tenant.phone
            val template = homeRepository.getTemplate("RENT_REMINDER")
            val templateText = template?.messageTemplate ?: com.rms.app.core.util.WhatsAppHelper.getDefaultRentReminderTemplate()
            
            val args = mapOf(
                "tenantName" to tenant.name,
                "amount" to com.rms.app.core.util.CurrencyUtils.formatAmountCompact(effectiveRent),
                "month" to "this month"
            )
            
            com.rms.app.core.util.WhatsAppHelper.formatAndSendMessage(context, phone, templateText, args)
        }
    }
}
