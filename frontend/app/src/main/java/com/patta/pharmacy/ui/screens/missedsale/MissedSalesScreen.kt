package com.patta.pharmacy.ui.screens.missedsale

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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.local.entity.MissedSaleEntity
import com.patta.pharmacy.ui.components.PattaField
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.ui.theme.ExpiryAmber
import com.patta.pharmacy.ui.theme.MoneyIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissedSalesScreen(
    onBack: () -> Unit,
    onCreatePo: () -> Unit,
    viewModel: MissedSalesViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val weekCount by viewModel.weekCount.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Missed Sales", fontWeight = FontWeight.Bold)
                        Text("Jo maanga, nahi tha", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
                actions = { TextButton(onClick = onCreatePo) { Text("PO banao") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ExpiryAmber.copy(alpha = 0.12f)),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            if (weekCount > 0) "Is hafte $weekCount baar stock nahi tha" else "Is hafte sab available tha 👍",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ExpiryAmber,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("Inko order karo — customer wapas na jaaye", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PattaField(newName, { newName = it }, "Kya maanga tha?", Modifier.fillMaxWidth())
                        PattaPrimaryButton("+ Note karo", onClick = { viewModel.log(newName); newName = "" }, enabled = newName.isNotBlank())
                    }
                }
            }

            item {
                Text("Recent", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(items) { entry -> MissedRow(entry) { viewModel.setResolved(entry.id, !entry.resolved) } }
        }
    }
}

@Composable
private fun MissedRow(entry: MissedSaleEntity, onToggle: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(entry.medicineName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (entry.salt.isNotBlank()) {
                    Text(entry.salt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${entry.timesAsked} baar maanga",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (entry.resolved) MoneyIn else ExpiryAmber,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextButton(onClick = onToggle) { Text(if (entry.resolved) "Undo" else "Ordered") }
        }
    }
}
