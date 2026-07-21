package com.patta.pharmacy.ui.screens.expiry

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.local.dao.ExpiryRow
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryScreen(
    onBack: () -> Unit,
    viewModel: ExpiryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expiry Management", fontWeight = FontWeight.Bold) },
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExpiryTab.entries.forEach { t ->
                        FilterChip(
                            selected = state.tab == t,
                            onClick = { viewModel.onTab(t) },
                            label = { Text("${t.label} (${state.counts[t] ?: 0})") },
                        )
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Risk pe stock", style = MaterialTheme.typography.bodyLarge)
                        MoneyText(state.valueAtRiskPaise, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (state.rows.isEmpty()) {
                item {
                    Text(
                        "Is window mein koi dawai expire nahi ho rahi — badhiya!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            } else {
                // Group by supplier — returns go back supplier-wise.
                val grouped = state.rows.groupBy { it.supplierName ?: "Supplier nahi" }
                grouped.forEach { (supplier, rows) ->
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(supplier, style = MaterialTheme.typography.headlineMedium)
                            MoneyText(rows.sumOf { it.valuePaise() }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(rows) { row -> ExpiryCard(row, onReturn = { viewModel.returnBatch(row) }) }
                }
            }
        }
    }
}

@Composable
private fun ExpiryCard(row: ExpiryRow, onReturn: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.medicineName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Batch ${row.batch.batchNo} · Exp ${row.batch.expiryYm % 100}/${row.batch.expiryYm / 100 % 100} · ${row.batch.qtyPacks.toInt()} pack",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Text("Value ${Money.format(row.valuePaise())}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = onReturn) { Text("Return") }
        }
    }
}
