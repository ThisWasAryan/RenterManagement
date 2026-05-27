package com.rms.app.feature.tenant.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rms.app.core.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTenantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditing) "Edit Tenant" else "Add Tenant",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section: Personal Info
            SectionHeader("Tenant Information")

            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Full Name *") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null && uiState.name.isBlank(),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = uiState.phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = MaterialTheme.shapes.medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.sameAsPhone,
                    onCheckedChange = { viewModel.toggleSameAsPhone() }
                )
                Text(
                    text = "Same as Phone Number",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { viewModel.toggleSameAsPhone() }
                )
            }

            OutlinedTextField(
                value = uiState.whatsappNumber,
                onValueChange = viewModel::onWhatsAppChange,
                label = { Text("WhatsApp Number") },
                leadingIcon = { Icon(Icons.Filled.Chat, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = MaterialTheme.shapes.medium
            )

            // Section: Room & Rent
            SectionHeader("Room & Rent")

            // Toggle between existing room and new room
            // Toggle between existing room and new room
            if (uiState.availableRooms.isNotEmpty() || uiState.availableProperties.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !uiState.useExistingRoom,
                        onClick = { if (uiState.useExistingRoom) viewModel.toggleUseExistingRoom() },
                        label = { Text("New Room") }
                    )
                    FilterChip(
                        selected = uiState.useExistingRoom,
                        onClick = { if (!uiState.useExistingRoom) viewModel.toggleUseExistingRoom() },
                        label = { Text("Existing Room") }
                    )
                }
            }

            if (uiState.useExistingRoom && uiState.availableRooms.isNotEmpty()) {
                // Existing room dropdown
                var roomDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = roomDropdownExpanded,
                    onExpandedChange = { roomDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.availableRooms.find { it.id == uiState.selectedRoomId }
                            ?.let { "Room ${it.roomNumber} - ₹${it.monthlyRent.toInt()}/mo" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Room") },
                        leadingIcon = { Icon(Icons.Filled.MeetingRoom, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = roomDropdownExpanded,
                        onDismissRequest = { roomDropdownExpanded = false }
                    ) {
                        uiState.availableRooms.forEach { room ->
                            DropdownMenuItem(
                                text = { Text("Room ${room.roomNumber} - ₹${room.monthlyRent.toInt()}/mo [${room.status}]") },
                                onClick = {
                                    viewModel.onRoomSelected(room.id)
                                    roomDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                // New room
                if (uiState.availableProperties.isNotEmpty()) {
                    var propDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = propDropdownExpanded,
                        onExpandedChange = { propDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.availableProperties.find { it.id == uiState.selectedPropertyId }?.name ?: uiState.newPropertyName,
                            onValueChange = viewModel::onNewPropertyNameChange,
                            label = { Text("Property Name") },
                            leadingIcon = { Icon(Icons.Filled.Home, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = propDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = MaterialTheme.shapes.medium
                        )
                        ExposedDropdownMenu(
                            expanded = propDropdownExpanded,
                            onDismissRequest = { propDropdownExpanded = false }
                        ) {
                            uiState.availableProperties.forEach { prop ->
                                DropdownMenuItem(
                                    text = { Text(prop.name) },
                                    onClick = {
                                        viewModel.onPropertySelected(prop.id)
                                        propDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = uiState.newPropertyName,
                        onValueChange = viewModel::onNewPropertyNameChange,
                        label = { Text("Property Name") },
                        leadingIcon = { Icon(Icons.Filled.Home, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }

                if (uiState.selectedPropertyId == null) {
                    OutlinedTextField(
                        value = uiState.newPropertyAddress,
                        onValueChange = viewModel::onNewPropertyAddressChange,
                        label = { Text("Property Address (Optional)") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = uiState.roomNumber,
                        onValueChange = viewModel::onRoomNumberChange,
                        label = { Text("Room Number") },
                        leadingIcon = { Icon(Icons.Filled.MeetingRoom, null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = uiState.roomFloor,
                        onValueChange = viewModel::onRoomFloorChange,
                        label = { Text("Floor (Opt)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            OutlinedTextField(
                value = uiState.monthlyRent,
                onValueChange = viewModel::onMonthlyRentChange,
                label = { Text("Monthly Rent *") },
                prefix = { Text("₹") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium
            )

            // Move-in Date picker
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.moveInDate
            )

            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = DateUtils.formatFullDate(uiState.moveInDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Move-in Date") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                viewModel.onMoveInDateSelected(it)
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = uiState.advanceDeposit,
                onValueChange = viewModel::onDepositChange,
                label = { Text("Security Deposit") },
                prefix = { Text("₹") },
                leadingIcon = { Icon(Icons.Filled.AccountBalance, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium
            )

            // Section: Electricity
            SectionHeader("Electricity")

            OutlinedTextField(
                value = uiState.electricityRate,
                onValueChange = viewModel::onElectricityRateChange,
                label = { Text("Rate per Unit (₹)") },
                leadingIcon = { Icon(Icons.Filled.ElectricBolt, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = MaterialTheme.shapes.medium
            )

            // Section: ID Documents
            SectionHeader("ID Documents (Optional)")

            OutlinedTextField(
                value = uiState.aadhaarNumber,
                onValueChange = viewModel::onAadhaarChange,
                label = { Text("Aadhaar Number") },
                leadingIcon = { Icon(Icons.Filled.Badge, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = uiState.panNumber,
                onValueChange = viewModel::onPanChange,
                label = { Text("PAN Number") },
                leadingIcon = { Icon(Icons.Filled.CreditCard, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // Section: Notes
            SectionHeader("Notes")

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Additional Notes") },
                leadingIcon = { Icon(Icons.Filled.Notes, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            // Error
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save button
            Button(
                onClick = viewModel::saveTenant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isSaving,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.isEditing) "Update Tenant" else "Save Tenant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.showRentSyncDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissRentSyncDialog() },
                title = { Text("Update Room Rent?") },
                text = { Text("This tenant's rent differs from the room's default rent.\nDo you also want to update the room's default rent?") },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmRentSync(updateRoom = true) }) {
                        Text("Update Room Rent")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.confirmRentSync(updateRoom = false) }) {
                        Text("Keep Tenant Override")
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}
