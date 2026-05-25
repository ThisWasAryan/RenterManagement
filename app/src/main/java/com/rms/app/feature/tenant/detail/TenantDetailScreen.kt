package com.rms.app.feature.tenant.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.rms.app.core.model.enums.PaymentMode
import com.rms.app.core.ui.components.EmptyState
import com.rms.app.core.ui.components.LoadingState
import com.rms.app.core.ui.components.StatusChip
import com.rms.app.core.ui.theme.*
import com.rms.app.core.util.CurrencyUtils
import com.rms.app.core.util.DateUtils
import com.rms.app.core.util.WhatsAppHelper
import com.rms.app.feature.payment.RecordPaymentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailScreen(
    onNavigateBack: () -> Unit,
    onEditTenant: (Long) -> Unit,
    onAddReading: (Long) -> Unit,
    viewModel: TenantDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paymentData by viewModel.paymentData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val tabs = listOf("Overview", "Payments", "Electricity", "Documents")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    uiState.tenantWithRoom?.let { twr ->
                        IconButton(onClick = { onEditTenant(twr.tenant.id) }) {
                            Icon(Icons.Filled.Edit, "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        val twr = uiState.tenantWithRoom ?: return@Scaffold
        val tenant = twr.tenant
        val room = twr.room

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tenant.photoUri != null) {
                        AsyncImage(
                            model = tenant.photoUri,
                            contentDescription = tenant.name,
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    tenant.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            tenant.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        room?.let {
                            Text(
                                "Room ${it.roomNumber} • ${it.floor}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            tenant.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // WhatsApp button
                    val phone = tenant.whatsappNumber ?: tenant.phone
                    if (phone.isNotBlank()) {
                        IconButton(onClick = {
                            WhatsAppHelper.sendCustomMessage(context, phone, "Hi ${tenant.name}")
                        }) {
                            Icon(
                                Icons.Filled.Chat,
                                contentDescription = "WhatsApp",
                                tint = WhatsAppGreen
                            )
                        }
                    }
                }
            }

            // Quick actions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.openPaymentSheet() },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Filled.Payment, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Record Payment", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { onAddReading(tenant.id) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Filled.ElectricMeter, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Reading", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            // Tab content
            when (uiState.selectedTab) {
                0 -> OverviewTab(tenant, room)
                1 -> PaymentsTab(uiState.payments)
                2 -> ElectricityTab(
                    readings = uiState.electricityReadings,
                    onAddReading = { onAddReading(tenant.id) },
                    onMarkPaid = { readingId -> viewModel.showElectricityPayDialog(readingId) }
                )
                3 -> DocumentsTab(uiState.documents)
            }
        }
    }

    // Record Payment Bottom Sheet
    if (uiState.showPaymentSheet) {
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

    // Electricity Payment Dialog
    if (uiState.showElectricityPayDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissElectricityPayDialog() },
            title = { Text("Mark as Paid") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select payment method:", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentMode.entries.take(3).forEach { mode ->
                            FilterChip(
                                selected = uiState.electricityPayMode == mode,
                                onClick = { viewModel.onElectricityPayModeChange(mode) },
                                label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentMode.entries.drop(3).forEach { mode ->
                            FilterChip(
                                selected = uiState.electricityPayMode == mode,
                                onClick = { viewModel.onElectricityPayModeChange(mode) },
                                label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.markElectricityPaid() }) {
                    Text("Mark Paid")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissElectricityPayDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun OverviewTab(tenant: com.rms.app.core.model.entities.Tenant, room: com.rms.app.core.model.entities.Room?) {
    val effectiveRent = when {
        tenant.monthlyRent > 0 -> tenant.monthlyRent
        (room?.monthlyRent ?: 0.0) > 0 -> room?.monthlyRent ?: 0.0
        else -> 0.0
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DetailCard("Rent Details") {
                DetailRow("Monthly Rent", CurrencyUtils.formatAmountCompact(effectiveRent))
                DetailRow("Due Day", "${tenant.rentDueDay} of every month")
                DetailRow("Electricity Rate", "₹${tenant.electricityRate}/unit")
                DetailRow("Security Deposit", CurrencyUtils.formatAmountCompact(tenant.advanceDeposit))
            }
        }
        item {
            DetailCard("Room Details") {
                DetailRow("Room Number", room?.roomNumber ?: "—")
                DetailRow("Floor", room?.floor?.ifBlank { "—" } ?: "—")
                DetailRow("Status", room?.status ?: "—")
            }
        }
        item {
            DetailCard("Agreement") {
                DetailRow("Move-in Date", tenant.moveInDate?.let { DateUtils.formatFullDate(it) } ?: "—")
                DetailRow("Aadhaar", tenant.aadhaarNumber ?: "—")
                DetailRow("PAN", tenant.panNumber ?: "—")
            }
        }
        tenant.notes?.let { notes ->
            item {
                DetailCard("Notes") {
                    Text(notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PaymentsTab(payments: List<com.rms.app.core.model.entities.Payment>) {
    if (payments.isEmpty()) {
        EmptyState(icon = Icons.Outlined.Payments, title = "No payments yet", subtitle = "Record the first payment")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(payments) { payment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(DateUtils.formatMonthYear(payment.forMonth, payment.forYear), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(DateUtils.formatFullDate(payment.paymentDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(payment.mode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            payment.notes?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(CurrencyUtils.formatAmountCompact(payment.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
                            StatusChip(text = payment.type, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElectricityTab(
    readings: List<com.rms.app.core.model.entities.ElectricityReading>,
    onAddReading: () -> Unit,
    onMarkPaid: (Long) -> Unit
) {
    if (readings.isEmpty()) {
        EmptyState(icon = Icons.Outlined.ElectricMeter, title = "No readings yet", subtitle = "Add the first meter reading", actionText = "Add Reading", onAction = onAddReading)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(readings) { reading ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(DateUtils.formatMonthYear(reading.forMonth, reading.forYear), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            StatusChip(text = if (reading.isPaid) "Paid" else "Unpaid", color = if (reading.isPaid) Success else Error)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("${reading.previousReading.toInt()} → ${reading.currentReading.toInt()}", style = MaterialTheme.typography.bodyMedium)
                                Text("${reading.unitsConsumed.toInt()} units × ₹${reading.ratePerUnit.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (reading.isPaid && reading.paymentMode != null) {
                                    Text("Paid via ${reading.paymentMode}", style = MaterialTheme.typography.labelSmall, color = Success)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(CurrencyUtils.formatAmountCompact(reading.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (!reading.isPaid) {
                                    Spacer(Modifier.height(4.dp))
                                    FilledTonalButton(
                                        onClick = { onMarkPaid(reading.id) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, null, Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Mark Paid", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentsTab(documents: List<com.rms.app.core.model.entities.Document>) {
    if (documents.isEmpty()) {
        EmptyState(icon = Icons.Outlined.Description, title = "No documents", subtitle = "Upload agreements, IDs, and receipts")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(documents) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(doc.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                            Text(doc.documentType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatFullDate(doc.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { /* TODO: open document */ }) { Text("View") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
