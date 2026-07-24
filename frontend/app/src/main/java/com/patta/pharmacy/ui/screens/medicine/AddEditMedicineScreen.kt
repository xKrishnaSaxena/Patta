package com.patta.pharmacy.ui.screens.medicine

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.ui.components.BarcodeScanner
import com.patta.pharmacy.ui.components.DosagePicker
import com.patta.pharmacy.ui.components.PattaField
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.ui.components.QtyStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicineScreen(
    onDone: () -> Unit,
    viewModel: AddEditMedicineViewModel = hiltViewModel(),
) {
    val initial by viewModel.initialForm.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf(MedicineForm()) }
    var seeded by remember { mutableStateOf(false) }
    LaunchedEffect(initial) {
        if (!seeded && initial != null) { form = initial!!; seeded = true }
    }

    val context = LocalContext.current
    var showScanner by remember { mutableStateOf(false) }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showScanner = true
    }
    val startScan: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) showScanner = true else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    if (showScanner) {
        BarcodeScanner(
            onResult = { code -> form = form.copy(barcode = code); showScanner = false },
            onDismiss = { showScanner = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEdit) "Dawai Edit" else "Nayi Dawai", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Peeche")
                    }
                },
            )
        },
        bottomBar = {
            PattaPrimaryButton(
                text = "Save Medicine",
                onClick = { viewModel.save(form, onDone) },
                enabled = form.isValid,
                modifier = Modifier.padding(16.dp),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FormSection("Identity") {
                    PattaField(form.name, { form = form.copy(name = it) }, "Medicine name", Modifier.fillMaxWidth())
                    PattaField(form.salt, { form = form.copy(salt = it) }, "Salt / Composition", Modifier.fillMaxWidth())
                    PattaField(form.company, { form = form.copy(company = it) }, "Company", Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        PattaField(form.barcode, { form = form.copy(barcode = it) }, "Barcode", Modifier.weight(1f))
                        OutlinedButton(onClick = startScan) { Text("Scan") }
                    }
                }
            }
            item {
                FormSection("Dosage — kaise leni hai") {
                    DosagePicker(form.defaultDosage, { form = form.copy(defaultDosage = it) })
                }
            }
            item {
                FormSection("Pack Details") {
                    ChipRow(
                        options = listOf("Strip", "Bottle", "Box", "Tube"),
                        selected = form.packType,
                        onSelect = { form = form.copy(packType = it) },
                    )
                    LabeledStepper("Units per pack", form.unitsPerPack, min = 1) { form = form.copy(unitsPerPack = it) }
                    ToggleRow("Loose sale allowed?", form.allowLooseSale) { form = form.copy(allowLooseSale = it) }
                }
            }
            item {
                FormSection("Pricing & Tax") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PattaField(form.mrp, { form = form.copy(mrp = it) }, "MRP (₹)", Modifier.weight(1f), numeric = true)
                        PattaField(form.purchaseRate, { form = form.copy(purchaseRate = it) }, "Rate (₹)", Modifier.weight(1f), numeric = true)
                    }
                    Text("GST %", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChipRow(
                        options = listOf(0, 5, 12, 18).map { it.toString() },
                        selected = form.gstPercent.toString(),
                        onSelect = { form = form.copy(gstPercent = it.toInt()) },
                    )
                    PattaField(form.hsnCode, { form = form.copy(hsnCode = it) }, "HSN Code", Modifier.fillMaxWidth())
                }
            }
            item {
                FormSection("Shelf & Stock") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PattaField(form.rackLocation, { form = form.copy(rackLocation = it) }, "Rack", Modifier.weight(1f))
                    }
                    LabeledStepper("Reorder level", form.reorderLevel, min = 0) { form = form.copy(reorderLevel = it) }
                }
            }
            if (!viewModel.isEdit) {
                item {
                    FormSection("Opening Stock (optional)") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PattaField(form.openingQty, { form = form.copy(openingQty = it) }, "Qty (packs)", Modifier.weight(1f), numeric = true)
                            PattaField(form.openingBatchNo, { form = form.copy(openingBatchNo = it) }, "Batch no", Modifier.weight(1f))
                        }
                        PattaField(form.openingExpiry, { form = form.copy(openingExpiry = it) }, "Expiry (MM/YY)", Modifier.fillMaxWidth())
                    }
                }
            }
            item {
                FormSection("Flags") {
                    ToggleRow("Schedule H1 (prescription needed)", form.isScheduleH1) { form = form.copy(isScheduleH1 = it) }
                    ToggleRow("Fridge item (2–8°C)", form.isFridgeItem) { form = form.copy(isFridgeItem = it) }
                }
            }
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { opt ->
            FilterChip(selected = selected == opt, onClick = { onSelect(opt) }, label = { Text(opt) })
        }
    }
}

@Composable
private fun LabeledStepper(label: String, value: Int, min: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        QtyStepper(value = value, onChange = onChange, min = min)
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
