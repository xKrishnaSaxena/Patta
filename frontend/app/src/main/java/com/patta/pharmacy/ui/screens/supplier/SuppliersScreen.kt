package com.patta.pharmacy.ui.screens.supplier

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.components.PattaField
import com.patta.pharmacy.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    onBack: () -> Unit,
    onOpenSupplier: (String) -> Unit,
    viewModel: SuppliersViewModel = hiltViewModel(),
) {
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val totalDena = suppliers.sumOf { it.outstandingPaise.coerceAtLeast(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suppliers & Payments", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, "Naya supplier") }
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
                        Text("Total dena hai", style = MaterialTheme.typography.bodyLarge)
                        MoneyText(totalDena, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.error)
                        Text("${suppliers.size} suppliers", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(suppliers) { row ->
                Card(Modifier.fillMaxWidth().clickable { onOpenSupplier(row.supplier.id) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(row.supplier.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                if (row.supplier.creditPeriodDays > 0) "${row.supplier.creditPeriodDays} din credit" else "Cash supplier",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        MoneyText(
                            row.outstandingPaise,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (row.outstandingPaise > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddSupplierDialog(
            onDismiss = { showAdd = false },
            onSave = { name, phone, gstin, credit ->
                viewModel.addSupplier(name, phone, gstin, credit)
                showAdd = false
            },
        )
    }
}

@Composable
private fun AddSupplierDialog(onDismiss: () -> Unit, onSave: (String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var credit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Naya Supplier") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PattaField(name, { name = it }, "Naam", Modifier.fillMaxWidth())
                PattaField(phone, { phone = it }, "Phone", Modifier.fillMaxWidth(), numeric = true)
                PattaField(gstin, { gstin = it }, "GSTIN (optional)", Modifier.fillMaxWidth())
                PattaField(credit, { credit = it }, "Credit period (din)", Modifier.fillMaxWidth(), numeric = true)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, phone, gstin, credit.toIntOrNull() ?: 0) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
