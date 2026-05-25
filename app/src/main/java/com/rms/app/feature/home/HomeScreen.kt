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
import com.rms.app.core.ui.components.*
import com.rms.app.core.ui.theme.Error
import com.rms.app.core.ui.theme.Success
import com.rms.app.core.ui.theme.Warning
import com.rms.app.core.util.CurrencyUtils
import com.rms.app.core.util.WhatsAppHelper
import com.rms.app.feature.payment.RecordPaymentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTenantDetail: (Long) -> Unit,
    onNavigateToAddTenant: () -> Unit,
    onNavigateToAddReading: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showPaymentSheet by viewModel.showPaymentSheet.collectAsStateWithLifecycle()
    val paymentData by viewModel.paymentData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showWhatsAppDialogFor by remember { mutableStateOf<Long?>(null) }

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
                        // Removed filter button for now
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                RMSSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )

                // Summary strip
                if (uiState.activeTenantCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryChip(
                            label = "Collected",
                            value = CurrencyUtils.formatAmountCompact(uiState.totalCollectedThisMonth),
                            color = Success,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryChip(
                            label = "Pending",
                            value = CurrencyUtils.formatAmountCompact(uiState.totalPendingRent),
                            color = if (uiState.totalPendingRent > 0) Warning else Success,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.overdueCount > 0) {
                            SummaryChip(
                                label = "Overdue",
                                value = "${uiState.overdueCount}",
                                color = Error,
                                modifier = Modifier.weight(0.7f)
                            )
                        }
                    }
                }
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
                                pendingElectricity = cardData.pendingElectricity,
                                onRecordRent = {
                                    viewModel.openPaymentSheet(cardData.tenantWithRoom.tenant.id)
                                },
                                onAddMeter = {
                                    onNavigateToAddReading(cardData.tenantWithRoom.tenant.id)
                                },
                                onWhatsAppReminder = {
                                    showWhatsAppDialogFor = cardData.tenantWithRoom.tenant.id
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

    // Record Payment Bottom Sheet — REAL implementation
    if (showPaymentSheet) {
        RecordPaymentSheet(
            data = paymentData,
            onAmountChange = viewModel::onPaymentAmountChange,
            onModeChange = viewModel::onPaymentModeChange,
            onMonthChange = viewModel::onPaymentMonthChange,
            onYearChange = viewModel::onPaymentYearChange,
            onNotesChange = viewModel::onPaymentNotesChange,
            onSave = viewModel::savePayment,
            onDismiss = viewModel::dismissPaymentSheet
        )
    }

    if (showWhatsAppDialogFor != null) {
        val tenantId = showWhatsAppDialogFor!!
        AlertDialog(
            onDismissRequest = { showWhatsAppDialogFor = null },
            title = { Text("Send WhatsApp Reminder") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Rent Reminder") },
                        leadingContent = { Icon(Icons.Filled.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            viewModel.sendWhatsAppReminder(context, tenantId, "RENT_REMINDER")
                            showWhatsAppDialogFor = null
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Electricity Reminder") },
                        leadingContent = { Icon(Icons.Filled.ElectricBolt, contentDescription = null, tint = Warning) },
                        modifier = Modifier.clickable {
                            viewModel.sendWhatsAppReminder(context, tenantId, "ELECTRICITY_REMINDER")
                            showWhatsAppDialogFor = null
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Combined Reminder") },
                        leadingContent = { Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                        modifier = Modifier.clickable {
                            viewModel.sendWhatsAppReminder(context, tenantId, "COMBINED_REMINDER")
                            showWhatsAppDialogFor = null
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialogFor = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}
