package com.rms.app.feature.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rms.app.core.ui.components.EmptyState
import com.rms.app.core.ui.components.StatusChip
import com.rms.app.core.ui.theme.*
import com.rms.app.core.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminders", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: add reminder dialog */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Add Reminder")
            }
        }
    ) { padding ->
        if (uiState.pendingReminders.isEmpty() && uiState.allReminders.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.NotificationsNone,
                title = "No reminders",
                subtitle = "Reminders for rent due dates and agreement expiry will appear here",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.pendingReminders.isNotEmpty()) {
                    item {
                        Text(
                            "Pending",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.pendingReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onComplete = { viewModel.markAsCompleted(reminder.id) }
                        )
                    }
                }

                val completedReminders = uiState.allReminders.filter { it.isCompleted }
                if (completedReminders.isNotEmpty()) {
                    item {
                        Text(
                            "Completed",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(completedReminders, key = { it.id }) { reminder ->
                        ReminderCard(reminder = reminder, onComplete = null)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: com.rms.app.core.model.entities.Reminder,
    onComplete: (() -> Unit)?
) {
    val isOverdue = !reminder.isCompleted && DateUtils.isOverdue(reminder.dueDate)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onComplete != null) {
                IconButton(onClick = onComplete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.NotificationsNone, null, tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                Icon(
                    Icons.Filled.CheckCircle, null,
                    tint = Success,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    reminder.message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Due: ${DateUtils.formatFullDate(reminder.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isOverdue) {
                StatusChip("Overdue", Error)
            } else if (!reminder.isCompleted) {
                StatusChip(reminder.type, MaterialTheme.colorScheme.primary)
            }
        }
    }
}
