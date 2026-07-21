package com.patta.pharmacy.ui.screens.h1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.ui.components.PattaField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleH1Screen(
    onBack: () -> Unit,
    viewModel: ScheduleH1ViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val fmt = SimpleDateFormat("dd MMM yy", Locale("en", "IN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Schedule H1 Register", fontWeight = FontWeight.Bold)
                        Text("3 saal rakhna zaroori hai", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Filled.Add, "Entry jodo") }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (entries.isEmpty()) {
                item {
                    Text(
                        "Abhi koi H1 entry nahi. Jab Schedule-H1 dawai bikegi, bill ke baad app khud poochega.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(entries) { e ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(e.medicineName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Patient: ${e.patientName}${if (e.doctorName.isNotBlank()) " · Dr. ${e.doctorName}" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "${fmt.format(Date(e.date))} · Qty ${trim(e.qty)}${if (e.billNo.isNotBlank()) " · ${e.billNo}" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        var medicine by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        var patient by remember { mutableStateOf("") }
        var doctor by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("H1 entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PattaField(medicine, { medicine = it }, "Dawai", Modifier.fillMaxWidth())
                    PattaField(qty, { qty = it }, "Qty", Modifier.fillMaxWidth(), numeric = true)
                    PattaField(patient, { patient = it }, "Patient ka naam", Modifier.fillMaxWidth())
                    PattaField(doctor, { doctor = it }, "Doctor ka naam", Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addManual(medicine, qty, patient, doctor); showAdd = false }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
        )
    }
}

private fun trim(v: Double): String = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()
