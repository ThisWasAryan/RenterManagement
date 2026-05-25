package com.rms.app.feature.documents

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rms.app.core.model.enums.DocumentType
import com.rms.app.core.ui.components.EmptyState
import com.rms.app.core.util.DateUtils

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Documents",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Store and manage tenant documents",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Upload area
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.CloudUpload,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Upload Documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Agreements, IDs, meter photos, receipts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                filePickerLauncher.launch(
                                    arrayOf("image/*", "application/pdf", "application/msword")
                                )
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Filled.AttachFile, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Choose File")
                        }
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Filled.PhotoCamera, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Gallery")
                        }
                    }
                }
            }
        }

        // Rental Agreements Section
        item {
            SectionTitle(icon = Icons.Filled.Description, title = "Rental Agreements")
        }
        if (uiState.agreements.isEmpty()) {
            item { EmptyDocPlaceholder("No agreements uploaded") }
        } else {
            items(uiState.agreements) { doc ->
                DocumentRow(name = doc.name, subtitle = doc.documentType, date = DateUtils.formatFullDate(doc.createdAt), fileUri = doc.fileUri)
            }
        }

        // ID Documents Section
        item {
            SectionTitle(icon = Icons.Outlined.Badge, title = "ID Documents")
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val idTypes = listOf(
                    DocumentType.AADHAAR to "Aadhaar",
                    DocumentType.PAN to "PAN Card",
                    DocumentType.PASSPORT to "Passport",
                    DocumentType.DRIVING_LICENSE to "Driving License"
                )
                items(idTypes) { (type, label) ->
                    val hasDoc = uiState.idDocuments.any { it.documentType == type.name }
                    IdDocCard(label = label, isUploaded = hasDoc)
                }
            }
        }

        // Meter Photos Section
        item {
            SectionTitle(icon = Icons.Outlined.CameraAlt, title = "Meter Photos")
        }
        if (uiState.meterPhotos.isEmpty()) {
            item { EmptyDocPlaceholder("No meter photos yet") }
        } else {
            items(uiState.meterPhotos) { doc ->
                DocumentRow(name = doc.name, subtitle = "Meter Photo", date = DateUtils.formatFullDate(doc.createdAt), fileUri = doc.fileUri)
            }
        }

        // All Documents Section
        if (uiState.documents.isNotEmpty()) {
            item {
                SectionTitle(icon = Icons.Outlined.FolderOpen, title = "All Documents (${uiState.documents.size})")
            }
            items(uiState.documents) { doc ->
                DocumentRow(name = doc.name, subtitle = doc.documentType, date = DateUtils.formatFullDate(doc.createdAt), fileUri = doc.fileUri)
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }

    // Upload dialog — select tenant + document type
    if (uiState.showUploadDialog) {
        UploadDocDialog(
            tenants = uiState.tenants,
            selectedTenantId = uiState.selectedTenantId,
            selectedDocType = uiState.selectedDocType,
            onTenantSelected = viewModel::onTenantSelected,
            onDocTypeSelected = viewModel::onDocTypeSelected,
            onUpload = { name -> viewModel.uploadDocument(name) },
            onDismiss = viewModel::dismissUploadDialog
        )
    }
}

@Composable
private fun UploadDocDialog(
    tenants: List<com.rms.app.core.model.entities.Tenant>,
    selectedTenantId: Long?,
    selectedDocType: DocumentType,
    onTenantSelected: (Long) -> Unit,
    onDocTypeSelected: (DocumentType) -> Unit,
    onUpload: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var docName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Document") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tenant selector
                Text("Select Tenant", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tenants) { tenant ->
                        FilterChip(
                            selected = selectedTenantId == tenant.id,
                            onClick = { onTenantSelected(tenant.id) },
                            label = { Text(tenant.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Doc type selector
                Text("Document Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(DocumentType.entries) { type ->
                        FilterChip(
                            selected = selectedDocType == type,
                            onClick = { onDocTypeSelected(type) },
                            label = { Text(type.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Name
                OutlinedTextField(
                    value = docName,
                    onValueChange = { docName = it },
                    label = { Text("Document Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpload(docName) },
                enabled = selectedTenantId != null
            ) { Text("Upload") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DocumentRow(name: String, subtitle: String, date: String = "", fileUri: String = "") {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (date.isNotBlank()) {
                    Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (fileUri.isNotBlank()) {
                TextButton(onClick = {
                    try {
                        val uri = android.net.Uri.parse(fileUri)
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "*/*")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Open with"))
                    } catch (e: Exception) {
                        // ignore
                    }
                }) { Text("View") }
            }
        }
    }
}

@Composable
private fun IdDocCard(label: String, isUploaded: Boolean) {
    Card(
        modifier = Modifier.width(140.dp).height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                if (isUploaded) Icons.Filled.CheckCircle else Icons.Outlined.Description,
                null,
                tint = if (isUploaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text(
                if (isUploaded) "Uploaded" else "Not uploaded",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyDocPlaceholder(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
