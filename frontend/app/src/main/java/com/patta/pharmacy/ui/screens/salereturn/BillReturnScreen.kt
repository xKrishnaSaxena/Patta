package com.patta.pharmacy.ui.screens.salereturn

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
import com.patta.pharmacy.data.local.dao.BillItemRow
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.theme.MoneyIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillReturnScreen(
    onBack: () -> Unit,
    viewModel: BillReturnViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Return", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items) { row -> ReturnRow(row, onReturn = { viewModel.returnItem(row.item.id) }) }
        }
    }
}

@Composable
private fun ReturnRow(row: BillItemRow, onReturn: () -> Unit) {
    val remaining = row.item.qty - row.item.returnedQty
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.medicineName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Becha: ${trim(row.item.qty)} ${row.item.unit}" +
                        if (row.item.returnedQty > 0) " · Wapas: ${trim(row.item.returnedQty)}" else "",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            MoneyText(row.item.lineTotalPaise, style = MaterialTheme.typography.bodyLarge)
            if (remaining > 0.0) {
                OutlinedButton(onClick = onReturn, modifier = Modifier.padding(start = 8.dp)) { Text("Return") }
            } else {
                Text("Returned", style = MaterialTheme.typography.labelLarge, color = MoneyIn, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

private fun trim(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
