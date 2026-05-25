package com.rms.app.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rms.app.core.model.entities.Payment
import com.rms.app.core.model.entities.ElectricityReading
import com.rms.app.core.model.entities.Tenant
import com.rms.app.core.model.entities.Room
import com.rms.app.core.ui.theme.*
import com.rms.app.core.util.CurrencyUtils
import com.rms.app.core.util.DateUtils

/**
 * Home screen tenant card matching the dark-themed mockup design.
 * Shows tenant info, rent status, quick actions, and WhatsApp reminder.
 */
@Composable
fun TenantCard(
    tenant: Tenant,
    room: Room?,
    lastPayment: Payment?,
    lastReading: ElectricityReading?,
    pendingBalance: Double,
    onRecordRent: () -> Unit,
    onAddMeter: () -> Unit,
    onWhatsAppReminder: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val isPaid = lastPayment != null &&
        lastPayment.forMonth == DateUtils.getCurrentMonth() &&
        lastPayment.forYear == DateUtils.getCurrentYear()
    val isDue = !isPaid
    val monthlyRent = room?.monthlyRent ?: 0.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewDetails() }
                .padding(16.dp)
        ) {
            // Header: Photo + Name + Room + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tenant photo
                if (tenant.photoUri != null) {
                    AsyncImage(
                        model = tenant.photoUri,
                        contentDescription = tenant.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tenant.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tenant.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = room?.roomNumber?.let { "Room $it" } ?: "No room",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rent info grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(
                    label = "Rent Amount",
                    value = CurrencyUtils.formatAmountCompact(monthlyRent)
                )
                InfoColumn(
                    label = "Paid Till",
                    value = if (lastPayment != null) DateUtils.formatMonthYear(lastPayment.forMonth, lastPayment.forYear) else "—"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoColumn(
                    label = "Last Payment",
                    value = if (lastPayment != null) DateUtils.formatDate(lastPayment.paymentDate) else "—"
                )
                InfoColumn(
                    label = "Meter Reading",
                    value = if (lastReading != null) "${lastReading.currentReading.toInt()} units" else "—"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status chips row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    text = if (isPaid) "Paid" else "Due",
                    color = if (isPaid) Success else Error
                )
                if (pendingBalance > 0) {
                    StatusChip(
                        text = "${CurrencyUtils.formatAmountCompact(pendingBalance)} pending",
                        color = Error,
                        icon = Icons.Filled.ErrorOutline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    text = "Record Rent",
                    icon = Icons.Outlined.Payment,
                    onClick = onRecordRent,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = "Add Meter",
                    icon = Icons.Outlined.ElectricMeter,
                    onClick = onAddMeter,
                    modifier = Modifier.weight(1f)
                )
            }

            // WhatsApp reminder (only when due)
            AnimatedVisibility(
                visible = isDue,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Button(
                    onClick = onWhatsAppReminder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp Reminder", fontWeight = FontWeight.SemiBold)
                }
            }

            // Expanded detail actions
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(spring(stiffness = Spring.StiffnessLow)),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            text = "Add Expense",
                            icon = Icons.Outlined.Receipt,
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            text = "View Details",
                            icon = Icons.Outlined.Person,
                            onClick = onViewDetails,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
