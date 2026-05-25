package com.rms.app.feature.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rms.app.core.navigation.Screen
import com.rms.app.core.ui.components.*
import com.rms.app.core.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTenantDetail: (Long) -> Unit,
    onNavigateToAddTenant: () -> Unit,
    onNavigateToAddReading: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Record payment bottom sheet state
    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedTenantId by remember { mutableLongStateOf(0L) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Tenants",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${uiState.activeTenantCount} active rentals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { /* TODO: theme toggle */ }) {
                            Icon(
                                Icons.Filled.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { /* TODO: filter */ }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RMSSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTenant,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = "Add Tenant")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.tenants.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Outlined.PersonAdd,
                        title = "No tenants yet",
                        subtitle = "Add your first tenant to start managing rent",
                        actionText = "Add Tenant",
                        onAction = onNavigateToAddTenant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.tenants,
                            key = { it.tenantWithRoom.tenant.id }
                        ) { cardData ->
                            TenantCard(
                                tenant = cardData.tenantWithRoom.tenant,
                                room = cardData.tenantWithRoom.room,
                                lastPayment = cardData.lastPayment,
                                lastReading = cardData.lastReading,
                                pendingBalance = cardData.pendingBalance,
                                onRecordRent = {
                                    selectedTenantId = cardData.tenantWithRoom.tenant.id
                                    showPaymentSheet = true
                                },
                                onAddMeter = {
                                    onNavigateToAddReading(cardData.tenantWithRoom.tenant.id)
                                },
                                onWhatsAppReminder = {
                                    val tenant = cardData.tenantWithRoom.tenant
                                    val phone = tenant.whatsappNumber ?: tenant.phone
                                    val room = cardData.tenantWithRoom.room
                                    WhatsAppHelper.sendRentReminder(
                                        context = context,
                                        phoneNumber = phone,
                                        tenantName = tenant.name,
                                        amount = room?.monthlyRent ?: 0.0,
                                        month = "this month"
                                    )
                                },
                                onViewDetails = {
                                    onNavigateToTenantDetail(cardData.tenantWithRoom.tenant.id)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }

                        // Bottom spacing for FAB
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }

    // Record Payment Bottom Sheet
    if (showPaymentSheet) {
        RecordPaymentBottomSheet(
            tenantId = selectedTenantId,
            onDismiss = { showPaymentSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPaymentBottomSheet(
    tenantId: Long,
    onDismiss: () -> Unit
) {
    // This is a placeholder — the full implementation is in feature/payment
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Placeholder content — real implementation will be in RecordPaymentSheet.kt
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Record Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Payment recording UI — coming from PaymentViewModel",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
