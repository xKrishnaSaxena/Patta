package com.patta.pharmacy.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.theme.MoneyIn
import com.patta.pharmacy.util.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var actualCash by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Day Close", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile("Aaj ki Sale", state.day.totalPaise, Modifier.weight(1f))
                    StatTile("Est. Profit", state.profitPaise, Modifier.weight(1f), MoneyIn)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    CountTile("Bills", state.day.billsCount.toString(), Modifier.weight(1f))
                    StatTile("Avg Bill", state.avgBillPaise, Modifier.weight(1f))
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Din ka Hisaab (Cash drawer)", style = MaterialTheme.typography.headlineMedium)
                        Text("Expected cash (system)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                        MoneyText(state.day.cashPaise, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = actualCash,
                            onValueChange = { actualCash = it },
                            label = { Text("Actual cash counted (₹)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                        val actual = Money.parseRupees(actualCash)
                        if (actualCash.isNotBlank()) {
                            val diff = actual - state.day.cashPaise
                            Text(
                                when {
                                    diff == 0L -> "✅ Match — sahi hai"
                                    diff > 0 -> "⚠️ ${Money.format(diff)} zyada hai"
                                    else -> "🔴 ${Money.format(-diff)} kam hai (shortfall)"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = if (diff == 0L) MoneyIn else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("GST collected (aaj)", style = MaterialTheme.typography.bodyLarge)
                            Text("GSTR-1 ke liye", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        MoneyText(state.gstPaise, style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, paise: Long, modifier: Modifier, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MoneyText(paise, style = MaterialTheme.typography.headlineMedium, color = color)
        }
    }
}

@Composable
private fun CountTile(label: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}
