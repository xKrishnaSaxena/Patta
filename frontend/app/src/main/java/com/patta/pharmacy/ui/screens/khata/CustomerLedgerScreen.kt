package com.patta.pharmacy.ui.screens.khata

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.local.entity.CustomerLedgerEntry
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.components.PattaField
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.util.Money
import com.patta.pharmacy.util.ShareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    onBack: () -> Unit,
    viewModel: CustomerLedgerViewModel = hiltViewModel(),
) {
    val row by viewModel.row.collectAsStateWithLifecycle()
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showPay by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(row?.customer?.name ?: "Customer", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Outstanding Balance", style = MaterialTheme.typography.bodyLarge)
                        MoneyText(
                            row?.outstandingPaise ?: 0,
                            style = MaterialTheme.typography.displayMedium,
                            color = if ((row?.outstandingPaise ?: 0) > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        )
                        row?.customer?.phone?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        PattaPrimaryButton("Payment mila", onClick = { showPay = true }, modifier = Modifier.padding(top = 12.dp))
                        row?.let { r ->
                            if (r.outstandingPaise > 0) {
                                OutlinedButton(
                                    onClick = {
                                        ShareText.send(
                                            context,
                                            "Namaste ${r.customer.name} ji,\n\nAapka ${Money.format(r.outstandingPaise)} udhaar baaki hai. " +
                                                "Jab sahulat ho, de dijiyega.\n\n— Sharma Medical Store",
                                            "Reminder bhejo",
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                ) { Text("Reminder bhejo (WhatsApp)") }
                            }
                        }
                    }
                }
            }
            item { Text("Statement", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp)) }
            items(entries) { e -> LedgerRow(e) }
        }
    }

    if (showPay) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPay = false },
            title = { Text("Payment mila") },
            text = { PattaField(amount, { amount = it }, "Amount (₹)", Modifier.fillMaxWidth(), numeric = true) },
            confirmButton = { TextButton(onClick = { viewModel.recordPayment(Money.parseRupees(amount), "cash"); showPay = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showPay = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun LedgerRow(e: CustomerLedgerEntry) {
    // Bill => customer owes more (+, red). Payment => owes less (−, green).
    val isBill = e.type == "bill"
    val color = if (isBill) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val label = if (isBill) "Bill ${e.note}" else "Payment"
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    (if (e.amountPaise >= 0) "+" else "−") + Money.format(kotlin.math.abs(e.amountPaise)),
                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color,
                )
            }
            Text("Balance: ${Money.format(e.runningBalancePaise)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
