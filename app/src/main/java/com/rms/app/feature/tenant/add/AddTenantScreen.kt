package com.rms.app.feature.tenant.add

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Personal Info
            SectionHeader("Personal Information")

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

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = MaterialTheme.shapes.medium
            )

            // Section: Room
            SectionHeader("Room Assignment")

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
                            text = {
                                Text("Room ${room.roomNumber} (${room.floor}) - ₹${room.monthlyRent.toInt()}/mo [${room.status}]")
                            },
                            onClick = {
                                viewModel.onRoomSelected(room.id)
                                roomDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Section: Financial
            SectionHeader("Financial Details")

            OutlinedTextField(
                value = uiState.advanceDeposit,
                onValueChange = viewModel::onDepositChange,
                label = { Text("Advance / Security Deposit") },
                leadingIcon = { Icon(Icons.Filled.AccountBalance, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("₹") },
                shape = MaterialTheme.shapes.medium
            )

            // Section: ID Documents
            SectionHeader("ID Documents")

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
                    .height(120.dp),
                maxLines = 4,
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
