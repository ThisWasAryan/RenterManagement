package com.rms.app.feature.whatsapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rms.app.core.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppTemplateScreen(
    onNavigateBack: () -> Unit,
    viewModel: WhatsAppTemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Templates", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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

        var templateToEdit by remember { mutableStateOf<com.rms.app.core.model.entities.WhatsAppTemplate?>(null) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.templates) { template ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                template.templateType.replace("_", " "),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { templateToEdit = template }) {
                                Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            template.messageTemplate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        templateToEdit?.let { template ->
            EditTemplateDialog(
                template = template,
                onDismiss = { templateToEdit = null },
                onSave = { newMessage ->
                    viewModel.updateTemplate(template.templateType, newMessage)
                    templateToEdit = null
                }
            )
        }
    }
}

@Composable
private fun EditTemplateDialog(
    template: com.rms.app.core.model.entities.WhatsAppTemplate,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var message by remember { mutableStateOf(template.messageTemplate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Template: ${template.templateType.replace("_", " ")}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "You can use placeholders like {tenantName}, {amount}, {month}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Template") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(message) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
