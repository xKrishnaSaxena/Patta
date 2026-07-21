package com.patta.pharmacy.ui.screens.supplier

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.local.entity.SupplierLedgerEntry
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.components.PattaField
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierLedgerScreen(
    onBack: () -> Unit,
    onNewPurchase: (String) -> Unit,
    viewModel: SupplierLedgerViewModel = hiltViewModel(),
) {
    val row by viewModel.row.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showPay by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(row?.supplier?.name ?: "Supplier", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Naya Purchase") },
                icon = { Icon(Icons.Filled.Add, null) },
                onClick = { onNewPurchase(viewModel.supplierId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Total Outstanding", style = MaterialTheme.typography.bodyLarge)
                        MoneyText(row?.outstandingPaise ?: 0, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.error)
                        row?.supplier?.let {
                            Text(
                                buildString {
                                    if (it.phone.isNotBlank()) append(it.phone + "  ·  ")
                                    append(if (it.creditPeriodDays > 0) "${it.creditPeriodDays} din credit" else "Cash")
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        PattaPrimaryButton("Payment karo", onClick = { showPay = true }, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            item { Text("Statement", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp)) }
            items(entries) { e -> LedgerRow(e) }
        }
    }

    if (showPay) {
        PaymentDialog(
            onDismiss = { showPay = false },
            onPay = { amount, mode ->
                viewModel.recordPayment(Money.parseRupees(amount), mode)
                showPay = false
            },
        )
    }
}

@Composable
private fun LedgerRow(e: SupplierLedgerEntry) {
    val color = when (e.type) {
        "payment" -> MaterialTheme.colorScheme.secondary       // green (money out to supplier = balance down)
        "creditNote" -> MaterialTheme.colorScheme.primary      // teal
        else -> MaterialTheme.colorScheme.error                // purchase = balance up
    }
    val label = when (e.type) {
        "payment" -> "Payment"
        "creditNote" -> "Credit Note"
        else -> "Purchase"
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("$label ${if (e.note.isNotBlank()) "· ${e.note}" else ""}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    (if (e.amountPaise >= 0) "+" else "−") + Money.format(kotlin.math.abs(e.amountPaise)),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
            Text("Balance: ${Money.format(e.runningBalancePaise)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PaymentDialog(onDismiss: () -> Unit, onPay: (String, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("cash") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Payment karo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PattaField(amount, { amount = it }, "Amount (₹)", Modifier.fillMaxWidth(), numeric = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("cash", "upi").forEach { m ->
                        TextButton(onClick = { mode = m }) {
                            Text(if (mode == m) "● ${m.uppercase()}" else m.uppercase())
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onPay(amount, mode) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
