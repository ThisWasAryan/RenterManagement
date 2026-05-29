package com.rms.app.feature.documents

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rms.app.core.model.enums.DocumentType
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

        // Categories
        val allCategories = listOf(
            DocumentType.AADHAAR,
            DocumentType.PAN,
            DocumentType.PASSPORT,
            DocumentType.DRIVING_LICENSE,
            DocumentType.AGREEMENT,
            DocumentType.RECEIPT,
            DocumentType.OTHER
        )

        allCategories.forEach { category ->
            val docsInCategory = uiState.categorizedDocuments[category] ?: emptyList()
            item {
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = docsInCategory.isNotEmpty()) { expanded = !expanded },
                    colors = CardDefaults.cardColors(
                        containerColor = if (docsInCategory.isEmpty()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (docsInCategory.isEmpty()) 0.dp else 2.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getIconForCategory(category),
                                contentDescription = null,
                                tint = if (docsInCategory.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (docsInCategory.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (docsInCategory.isEmpty()) "No uploads" else "${docsInCategory.size} uploaded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (docsInCategory.isNotEmpty()) {
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (expanded && docsInCategory.isNotEmpty()) {
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                docsInCategory.forEach { docWithCtx ->
                                    val doc = docWithCtx.document
                                    val tenantName = docWithCtx.tenant?.name ?: "Unknown Tenant"
                                    DocumentRow(
                                        name = doc.name,
                                        subtitle = tenantName,
                                        date = DateUtils.formatFullDate(doc.createdAt),
                                        fileUri = doc.fileUri,
                                        onDelete = { viewModel.deleteDocument(doc) }
                                    )
                                }
                            }
                        }
                    }
                }
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
private fun DocumentRow(name: String, subtitle: String, date: String = "", fileUri: String = "", onDelete: (() -> Unit)? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Document") },
            text = { Text("Are you sure you want to delete this document? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mimeType = try {
                if (fileUri.isNotBlank()) context.contentResolver.getType(android.net.Uri.parse(fileUri)) else null
            } catch (e: Exception) { null }

            if (mimeType?.startsWith("image/") == true) {
                coil.compose.AsyncImage(
                    model = fileUri,
                    contentDescription = name,
                    modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (date.isNotBlank()) {
                    Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (fileUri.isNotBlank()) {
                IconButton(onClick = {
                    try {
                        val uri = android.net.Uri.parse(fileUri)
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, mimeType ?: "*/*")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Open with"))
                    } catch (e: Exception) {
                        // ignore
                    }
                }) { Icon(Icons.Filled.Visibility, "View", tint = MaterialTheme.colorScheme.primary) }
            }
            if (onDelete != null) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
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

@Composable
private fun getIconForCategory(type: DocumentType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        DocumentType.AADHAAR, DocumentType.PAN, DocumentType.PASSPORT, DocumentType.DRIVING_LICENSE -> Icons.Outlined.Badge
        DocumentType.AGREEMENT -> Icons.Filled.Description
        DocumentType.RECEIPT -> Icons.Outlined.Receipt
        DocumentType.METER_PHOTO -> Icons.Outlined.CameraAlt
        DocumentType.OTHER -> Icons.Outlined.FolderOpen
    }
}
