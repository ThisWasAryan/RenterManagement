package com.rms.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rms.app.core.model.entities.ElectricityReading
import com.rms.app.core.model.entities.Payment
import com.rms.app.core.model.relations.TenantWithRoom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TenantCardData(
    val tenantWithRoom: TenantWithRoom,
    val lastPayment: Payment? = null,
    val lastReading: ElectricityReading? = null,
    val pendingBalance: Double = 0.0
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val tenants: List<TenantCardData> = emptyList(),
    val activeTenantCount: Int = 0,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTenants()
        observeTenantCount()
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
                    val cardDataList = tenantsWithRooms.map { twr ->
                        val lastPayment = homeRepository.getLastPayment(twr.tenant.id)
                        val lastReading = homeRepository.getLastReading(twr.tenant.id)
                        TenantCardData(
                            tenantWithRoom = twr,
                            lastPayment = lastPayment,
                            lastReading = lastReading,
                            pendingBalance = 0.0 // Calculated from room rent minus payments
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tenants = cardDataList,
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }
}
