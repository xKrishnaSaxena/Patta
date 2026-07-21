package com.patta.pharmacy.ui.screens.po

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.repo.POSuggestion
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.ui.components.QtyStepper
import com.patta.pharmacy.util.ShareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOrderScreen(
    onBack: () -> Unit,
    viewModel: PurchaseOrderViewModel = hiltViewModel(),
) {
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val supplier by viewModel.supplier.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() } }

    val selectedCount = lines.count { it.id in selected }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase Order", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("$selectedCount item selected", style = MaterialTheme.typography.bodyLarge)
                        MoneyText(viewModel.estimatedTotalPaise(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { ShareText.send(context, viewModel.orderText(), "Order bhejo") },
                            enabled = selectedCount > 0,
                            modifier = Modifier.weight(1f),
                        ) { Text("WhatsApp bhejo") }
                        PattaPrimaryButton(
                            "Save",
                            onClick = { viewModel.save(onBack) },
                            enabled = selectedCount > 0,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text("Supplier chuno (optional)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suppliers.forEach { s ->
                        FilterChip(
                            selected = supplier?.supplier?.id == s.supplier.id,
                            onClick = { viewModel.selectSupplier(if (supplier?.supplier?.id == s.supplier.id) null else s) },
                            label = { Text(s.supplier.name) },
                        )
                    }
                }
            }

            if (lines.isEmpty()) {
                item {
                    Text(
                        "Abhi kuch suggest nahi — stock theek hai aur koi missed sale nahi.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            } else {
                item { Text("Suggest kiye gaye items", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp)) }
                items(lines) { line ->
                    PORow(
                        line = line,
                        checked = line.id in selected,
                        onToggle = { viewModel.toggle(line.id) },
                        onQty = { viewModel.setQty(line.id, it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PORow(line: POSuggestion, checked: Boolean, onToggle: () -> Unit, onQty: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f)) {
                Text(line.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (line.salt.isNotBlank()) {
                    Text(line.salt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(line.reason, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            }
            QtyStepper(value = line.qty, onChange = onQty, min = 1)
        }
    }
}
