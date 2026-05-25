package com.rms.app.feature.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rms.app.core.model.enums.PaymentMode
import com.rms.app.core.util.CurrencyUtils
import com.rms.app.core.util.DateUtils

data class RecordPaymentData(
    val tenantId: Long = 0,
    val tenantName: String = "",
    val roomNumber: String = "",
    val suggestedAmount: Double = 0.0,
    val amount: String = "",
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val forMonth: Int = DateUtils.getCurrentMonth(),
    val forYear: Int = DateUtils.getCurrentYear(),
    val unpaidMonths: List<Pair<Int, Int>> = emptyList(),
    val notes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentSheet(
    data: RecordPaymentData,
    onAmountChange: (String) -> Unit,
    onModeChange: (PaymentMode) -> Unit,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Record Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Tenant info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            data.tenantName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (data.roomNumber.isNotBlank()) {
                            Text(
                                "Room ${data.roomNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Rent: ${CurrencyUtils.formatAmountCompact(data.suggestedAmount)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Amount
            OutlinedTextField(
                value = data.amount,
                onValueChange = onAmountChange,
                label = { Text("Amount Paid *") },
                prefix = { Text("₹") },
                leadingIcon = { Icon(Icons.Filled.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
                isError = data.error != null && data.amount.isBlank()
            )

            // Payment month selector
            Text(
                "Payment For",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentMonth = DateUtils.getCurrentMonth()
                val currentYear = DateUtils.getCurrentYear()
                
                val months = if (data.unpaidMonths.isNotEmpty()) {
                    data.unpaidMonths.take(5) // Show up to 5 unpaid months
                } else {
                    listOf(
                        (if (currentMonth - 2 < 1) currentMonth + 10 else currentMonth - 2) to
                                (if (currentMonth - 2 < 1) currentYear - 1 else currentYear),
                        (if (currentMonth - 1 < 1) 12 else currentMonth - 1) to
                                (if (currentMonth - 1 < 1) currentYear - 1 else currentYear),
                        currentMonth to currentYear
                    )
                }

                months.forEach { (month, year) ->
                    val selected = data.forMonth == month && data.forYear == year
                    FilterChip(
                        selected = selected,
                        onClick = { onMonthChange(month); onYearChange(year) },
                        label = {
                            Text(
                                DateUtils.formatMonthYear(month, year),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            // Payment method
            Text(
                "Payment Method",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PaymentMode.entries.forEach { mode ->
                    val selected = data.paymentMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { onModeChange(mode) },
                        label = {
                            Text(
                                mode.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = data.notes,
                onValueChange = onNotesChange,
                label = { Text("Notes (optional)") },
                leadingIcon = { Icon(Icons.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = MaterialTheme.shapes.medium
            )

            // Error
            data.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !data.isSaving,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (data.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save Payment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
