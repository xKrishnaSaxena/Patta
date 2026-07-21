package com.patta.pharmacy.ui.screens.khata

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import com.patta.pharmacy.ui.theme.MoneyIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onOpenCustomer: (String) -> Unit,
    viewModel: CustomersViewModel = hiltViewModel(),
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val totalLena = customers.sumOf { it.outstandingPaise.coerceAtLeast(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Khata", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(titleContentColor = MaterialTheme.colorScheme.primary),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, "Naya customer") }
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
                        Text("Lena hai", style = MaterialTheme.typography.bodyLarge)
                        MoneyText(totalLena, style = MaterialTheme.typography.displayMedium, color = MoneyIn)
                        Text("${customers.size} customers", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(customers) { row ->
                Card(Modifier.fillMaxWidth().clickable { onOpenCustomer(row.customer.id) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(row.customer.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            if (row.customer.phone.isNotBlank()) {
                                Text(row.customer.phone, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        MoneyText(
                            row.outstandingPaise,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (row.outstandingPaise > 0) MaterialTheme.colorScheme.error else MoneyIn,
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Naya Customer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PattaField(name, { name = it }, "Naam", Modifier.fillMaxWidth())
                    PattaField(phone, { phone = it }, "Phone", Modifier.fillMaxWidth(), numeric = true)
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.addCustomer(name, phone); showAdd = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
        )
    }
}
