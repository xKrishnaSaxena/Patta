package com.patta.pharmacy.ui.screens.gst

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.util.Money
import com.patta.pharmacy.util.ShareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstSummaryScreen(
    onBack: () -> Unit,
    viewModel: GstSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GST Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        bottomBar = {
            PattaPrimaryButton(
                "Share (GSTR-1 ke liye)",
                onClick = { ShareText.send(context, viewModel.shareText(), "GST summary bhejo") },
                enabled = state.rows.isNotEmpty(),
                modifier = Modifier.padding(16.dp),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Is mahine", 1 to "Pichhle mahine").forEach { (back, label) ->
                        FilterChip(
                            selected = state.monthsBack == back,
                            onClick = { viewModel.load(back) },
                            label = { Text(label) },
                        )
                    }
                }
            }
            item {
                Text(state.monthLabel, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 4.dp))
            }

            if (state.rows.isEmpty()) {
                item {
                    Text(
                        "Is mahine koi sale nahi.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            } else {
                items(state.rows) { row ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("GST ${row.ratePercent}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            LabelValue("Taxable value", Money.format(row.taxablePaise))
                            LabelValue("GST", Money.format(row.gstPaise))
                            LabelValue("Total (MRP)", Money.format(row.grossPaise))
                        }
                    }
                }
                item {
                    Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Total", style = MaterialTheme.typography.headlineMedium)
                            LabelValue("Taxable value", Money.format(state.totalTaxable))
                            LabelValue("GST collected", Money.format(state.totalGst))
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total sale", style = MaterialTheme.typography.bodyLarge)
                                MoneyText(state.totalGross, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
