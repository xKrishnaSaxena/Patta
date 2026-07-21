package com.patta.pharmacy.ui.screens.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.patta.pharmacy.data.local.dao.StockRow
import com.patta.pharmacy.ui.components.SearchBarWithMic
import com.patta.pharmacy.ui.theme.ExpiryAmber
import com.patta.pharmacy.ui.theme.MoneyIn
import com.patta.pharmacy.ui.theme.MoneyOut

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    onAddMedicine: () -> Unit,
    onEditMedicine: (String) -> Unit,
    viewModel: StockListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Stock", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, contentDescription = "Nayi dawai") }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            SearchBarWithMic(
                value = state.query,
                onValueChange = viewModel::onQuery,
                placeholder = "Search Medicine...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StockFilter.entries.forEach { f ->
                    FilterChip(
                        selected = state.filter == f,
                        onClick = { viewModel.onFilter(f) },
                        label = { Text(f.label) },
                    )
                }
            }

            if (state.rows.isEmpty()) {
                EmptyStock()
            } else {
                val grouped = state.rows.groupBy { it.medicine.name.firstOrNull()?.uppercaseChar() ?: '#' }
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    grouped.toSortedMap().forEach { (letter, rows) ->
                        item {
                            Text(
                                letter.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(rows) { row -> StockCard(row, onClick = { onEditMedicine(row.medicine.id) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockCard(row: StockRow, onClick: () -> Unit) {
    val reorder = row.medicine.reorderLevel
    val qty = row.totalQty
    val months = monthsUntilExpiry(row.nearestExpiryYm)

    val barColor = when {
        qty <= reorder -> MoneyOut
        months != null && months <= 6 -> ExpiryAmber
        else -> MoneyIn
    }
    val fraction = (qty / (maxOf(reorder * 3, 10)).toDouble()).coerceIn(0.0, 1.0).toFloat()

    Card(Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(row.medicine.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    if (row.medicine.salt.isNotBlank()) {
                        Text(
                            row.medicine.salt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (months != null && months <= 6) {
                        Box(
                            Modifier
                                .padding(top = 6.dp)
                                .background(ExpiryAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (months < 0) "Expired" else "Exp ${months}m",
                                style = MaterialTheme.typography.labelLarge,
                                color = ExpiryAmber,
                            )
                        }
                    }
                }
                val unit = row.medicine.packType.lowercase()
                Text(
                    "${qty.trimQty()} $unit",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (qty <= reorder) MoneyOut else MaterialTheme.colorScheme.onSurface,
                )
            }
            Box(
                Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .background(barColor, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun EmptyStock() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Abhi koi dawai nahi", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Neeche '+' dabao aur pehli dawai jodo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** 14.0 -> "14", 0.4 -> "0.4" */
private fun Double.trimQty(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()
