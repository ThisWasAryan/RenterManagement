package com.rms.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Settings", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Text("Configure your app", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Appearance
        item { SettingsSectionTitle("Appearance") }
        item {
            SettingsToggleRow(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                subtitle = "Use dark theme",
                isChecked = uiState.isDarkMode,
                onToggle = { viewModel.toggleDarkMode() }
            )
        }

        // Defaults
        item { SettingsSectionTitle("Defaults") }
        item {
            SettingsInputRow(
                icon = Icons.Filled.CurrencyRupee,
                title = "Default Rent Amount",
                value = uiState.defaultRent,
                onValueChange = viewModel::updateDefaultRent,
                keyboardType = KeyboardType.Number
            )
        }
        item {
            SettingsInputRow(
                icon = Icons.Filled.ElectricBolt,
                title = "Electricity Rate (₹/unit)",
                value = uiState.electricityRate,
                onValueChange = viewModel::updateElectricityRate,
                keyboardType = KeyboardType.Decimal
            )
        }

        // Property Management
        item { SettingsSectionTitle("Properties") }
        items(uiState.properties) { property ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Home, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(property.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        if (property.address.isNotBlank()) {
                            Text(property.address, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        item {
            TextButton(
                onClick = { viewModel.toggleAddPropertyDialog() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Property")
            }
        }

        // Room Management
        item { SettingsSectionTitle("Rooms") }
        items(uiState.rooms) { room ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MeetingRoom, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Room ${room.roomNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Text("${room.floor} • ₹${room.monthlyRent.toInt()}/mo • ${room.status}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            TextButton(
                onClick = { viewModel.toggleAddRoomDialog() },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add Room")
            }
        }

        // About
        item { SettingsSectionTitle("About") }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RMS - Rent Management System", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Replace paper rent notebooks with a practical, fast, organized mobile application.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    // Add Property Dialog
    if (uiState.showAddPropertyDialog) {
        AddPropertyDialog(
            onDismiss = { viewModel.toggleAddPropertyDialog() },
            onAdd = { name, address -> viewModel.addProperty(name, address) }
        )
    }

    // Add Room Dialog
    if (uiState.showAddRoomDialog) {
        AddRoomDialog(
            properties = uiState.properties,
            onDismiss = { viewModel.toggleAddRoomDialog() },
            onAdd = { propertyId, roomNumber, floor, rent ->
                viewModel.addRoom(propertyId, roomNumber, floor, rent)
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = isChecked, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun SettingsInputRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.width(100.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AddPropertyDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Property") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Property Name") }, singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, address); onDismiss() }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddRoomDialog(
    properties: List<com.rms.app.core.model.entities.Property>,
    onDismiss: () -> Unit,
    onAdd: (Long, String, String, Double) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    val propertyId = properties.firstOrNull()?.id ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = roomNumber, onValueChange = { roomNumber = it }, label = { Text("Room Number") }, singleLine = true)
                OutlinedTextField(value = floor, onValueChange = { floor = it }, label = { Text("Floor") }, singleLine = true)
                OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Monthly Rent (₹)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(propertyId, roomNumber, floor, rent.toDoubleOrNull() ?: 0.0); onDismiss() },
                enabled = roomNumber.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
