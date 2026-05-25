package com.rms.app.feature.tenant.detail

import androidx.compose.foundation.clickable
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
    var showWhatsAppDialog by remember { mutableStateOf(false) }
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
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, "More Options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                onClick = {
                                    showMenu = false
                                    onEditTenant(twr.tenant.id)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    viewModel.showDeleteDialog()
                                }
                            )
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

        LaunchedEffect(uiState.isDeleted) {
            if (uiState.isDeleted) {
                onNavigateBack()
            }
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
                            showWhatsAppDialog = true
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
                    onMarkPaid = { readingId -> viewModel.showElectricityPayDialog(readingId) },
                    onReadingClick = { reading -> viewModel.showElectricityDetail(reading) }
                )
                3 -> DocumentsTab(
                    documents = uiState.documents,
                    onDelete = { doc -> viewModel.deleteDocument(doc) }
                )
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

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Tenant") },
            text = { Text("Are you sure you want to delete this tenant? All associated payments, electricity readings, and documents will also be deleted. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteTenant() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showElectricityDetailDialog && uiState.selectedElectricityReading != null) {
        ElectricityDetailDialog(
            reading = uiState.selectedElectricityReading!!,
            onDismiss = viewModel::dismissElectricityDetail,
            onMarkPaid = { 
                viewModel.dismissElectricityDetail()
                viewModel.showElectricityPayDialog(it.id)
            }
        )
    }

    if (showWhatsAppDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppDialog = false },
            title = { Text("Send WhatsApp Reminder") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Rent Reminder") },
                        leadingContent = { Icon(Icons.Filled.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            viewModel.sendWhatsAppReminder(context, "RENT_REMINDER")
                            showWhatsAppDialog = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Electricity Reminder") },
                        leadingContent = { Icon(Icons.Filled.ElectricBolt, contentDescription = null, tint = Warning) },
                        modifier = Modifier.clickable {
                            viewModel.sendWhatsAppReminder(context, "ELECTRICITY_REMINDER")
                            showWhatsAppDialog = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Combined Reminder") },
                        leadingContent = { Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                        modifier = Modifier.clickable {
                            viewModel.sendWhatsAppReminder(context, "COMBINED_REMINDER")
                            showWhatsAppDialog = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showWhatsAppDialog = false }) {
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
    onMarkPaid: (Long) -> Unit,
    onReadingClick: (com.rms.app.core.model.entities.ElectricityReading) -> Unit
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
                    modifier = Modifier.fillMaxWidth().clickable { onReadingClick(reading) },
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
private fun DocumentsTab(
    documents: List<com.rms.app.core.model.entities.Document>,
    onDelete: (com.rms.app.core.model.entities.Document) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf<com.rms.app.core.model.entities.Document?>(null) }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Document") },
            text = { Text("Are you sure you want to delete this document? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(showDeleteConfirm!!)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancel") }
            }
        )
    }

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
                        val mimeType = try {
                            if (doc.fileUri.isNotBlank()) context.contentResolver.getType(android.net.Uri.parse(doc.fileUri)) else null
                        } catch (e: Exception) { null }

                        if (mimeType != null && mimeType.startsWith("image/")) {
                            coil.compose.AsyncImage(
                                model = doc.fileUri,
                                contentDescription = doc.name,
                                modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(doc.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                            Text(doc.documentType, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(DateUtils.formatFullDate(doc.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val context = LocalContext.current
                        IconButton(onClick = {
                            if (doc.fileUri.isNotBlank()) {
                                try {
                                    val uri = android.net.Uri.parse(doc.fileUri)
                                    val finalMimeType = context.contentResolver.getType(uri) ?: "*/*"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, finalMimeType)
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Open with"))
                                } catch (e: Exception) {
                                    // ignore
                                }
                            }
                        }) { Icon(Icons.Filled.Visibility, "View", tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = { showDeleteConfirm = doc }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
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

@Composable
private fun ElectricityDetailDialog(
    reading: com.rms.app.core.model.entities.ElectricityReading,
    onDismiss: () -> Unit,
    onMarkPaid: (com.rms.app.core.model.entities.ElectricityReading) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Electricity Reading", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DetailRow("Billing Month", DateUtils.formatMonthYear(reading.forMonth, reading.forYear))
                DetailRow("Reading Date", DateUtils.formatFullDate(reading.readingDate))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DetailRow("Previous Reading", "${reading.previousReading.toInt()}")
                DetailRow("Current Reading", "${reading.currentReading.toInt()}")
                DetailRow("Units Consumed", "${reading.unitsConsumed.toInt()}")
                DetailRow("Amount Due", CurrencyUtils.formatAmountCompact(reading.totalAmount))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DetailRow("Status", if (reading.isPaid) "Paid" else "Unpaid")
                if (reading.isPaid) {
                    DetailRow("Payment Mode", reading.paymentMode ?: "—")
                    DetailRow("Paid On", reading.paidDate?.let { DateUtils.formatFullDate(it) } ?: "—")
                }

                if (reading.meterPhotoUri != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("Meter Photo", style = MaterialTheme.typography.labelMedium)
                    AsyncImage(
                        model = reading.meterPhotoUri,
                        contentDescription = "Meter Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        },
        confirmButton = {
            if (!reading.isPaid) {
                Button(onClick = { onMarkPaid(reading) }) {
                    Text("Mark Paid")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
