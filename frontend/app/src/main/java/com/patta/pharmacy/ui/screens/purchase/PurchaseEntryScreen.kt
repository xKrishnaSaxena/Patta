package com.patta.pharmacy.ui.screens.purchase

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.local.entity.MedicineEntity
import com.patta.pharmacy.data.repo.PurchaseLine
import com.patta.pharmacy.data.repo.computeLineCost
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.components.PattaField
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.ui.theme.MoneyIn
import com.patta.pharmacy.util.Money
import com.patta.pharmacy.voice.VoicePurchaseParser
import com.patta.pharmacy.voice.rememberVoiceController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseEntryScreen(
    onBack: () -> Unit,
    viewModel: PurchaseEntryViewModel = hiltViewModel(),
) {
    val supplier by viewModel.supplier.collectAsStateWithLifecycle()
    val invoiceNo by viewModel.invoiceNo.collectAsStateWithLifecycle()
    val lines by viewModel.lines.collectAsStateWithLifecycle()
    val total by viewModel.invoiceTotalPaise.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Purchase Entry", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Invoice Total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        MoneyText(total, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    PattaPrimaryButton("Save Purchase", onClick = { viewModel.save(onBack) }, modifier = Modifier.weight(1f).padding(start = 16.dp))
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(supplier?.supplier?.name ?: "Supplier", style = MaterialTheme.typography.headlineMedium)
                        PattaField(invoiceNo, viewModel::onInvoiceNo, "Invoice No", Modifier.fillMaxWidth())
                    }
                }
            }

            itemsIndexed(lines) { index, line ->
                AddedLineCard(line) { viewModel.removeLine(index) }
            }

            item {
                AddItemForm(
                    suggestions = suggestions,
                    onNameChanged = viewModel::onNameChanged,
                    onPick = viewModel::onPickMedicine,
                    onAdd = viewModel::addLine,
                )
            }
        }
    }
}

@Composable
private fun AddedLineCard(line: PurchaseLine, onRemove: () -> Unit) {
    val cost = computeLineCost(line)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(line.medicineName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "Batch ${line.batchNo} · Exp ${line.expiryYm % 100}/${line.expiryYm / 100 % 100} · ${line.qtyPacks.trim()}+${line.freeQtyPacks.trim()} free",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) { Icon(Icons.Filled.Delete, "Hatao", tint = MaterialTheme.colorScheme.error) }
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Landed: ${Money.format(cost.landedCostPaise)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text("Margin ${"%.1f".format(cost.marginPercent)}%", style = MaterialTheme.typography.bodyMedium, color = MoneyIn, fontWeight = FontWeight.SemiBold)
                Text("Total ${Money.format(cost.lineTotalPaise)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemForm(
    suggestions: List<MedicineEntity>,
    onNameChanged: (String) -> Unit,
    onPick: (String) -> Unit,
    onAdd: (String, String, String, String, String, String, String, String, Int, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var salt by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var free by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var scheme by remember { mutableStateOf("") }
    var gst by remember { mutableStateOf(12) }
    var mrp by remember { mutableStateOf("") }
    var picked by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val voice = rememberVoiceController()
    val onVoice: (List<String>) -> Unit = { alts ->
        val p = VoicePurchaseParser.parse(alts.firstOrNull().orEmpty())
        p.batch?.let { batch = it }
        p.expiry?.let { expiry = it }
        p.qty?.let { qty = it }
        p.free?.let { free = it }
        p.rate?.let { rate = it }
        p.mrp?.let { mrp = it }
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) voice.startListening(onResult = onVoice, onError = {})
    }
    val startVoice: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) voice.startListening(onResult = onVoice, onError = {})
        else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Item add karo", style = MaterialTheme.typography.headlineMedium)
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { startVoice() },
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Text("Bolke bharo — \"batch A123 expiry 08/27 qty 50 rate 18\"", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            PattaField(name, {
                name = it; picked = false; onNameChanged(it)
            }, "Medicine name", Modifier.fillMaxWidth())

            // Existing-medicine suggestions — tap to auto-fill (no re-typing, no duplicate).
            if (!picked && suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    suggestions.take(5).forEach { med ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().clickable {
                                name = med.name
                                salt = med.salt
                                gst = med.gstPercent
                                if (med.defaultMrpPaise > 0) mrp = (med.defaultMrpPaise / 100.0).toString()
                                picked = true
                                onPick(med.id)
                            },
                        ) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(med.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                if (med.salt.isNotBlank()) {
                                    Text(med.salt, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            PattaField(salt, { salt = it }, "Salt (optional)", Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PattaField(batch, { batch = it }, "Batch", Modifier.weight(1f))
                PattaField(expiry, { expiry = it }, "Exp MM/YY", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PattaField(qty, { qty = it }, "Qty", Modifier.weight(1f), numeric = true)
                PattaField(free, { free = it }, "Free", Modifier.weight(1f), numeric = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PattaField(rate, { rate = it }, "Rate ₹", Modifier.weight(1f), numeric = true)
                PattaField(scheme, { scheme = it }, "Scheme %", Modifier.weight(1f), numeric = true)
            }
            PattaField(mrp, { mrp = it }, "MRP ₹", Modifier.fillMaxWidth(), numeric = true)
            Text("GST %", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 5, 12, 18).forEach { g ->
                    FilterChip(selected = gst == g, onClick = { gst = g }, label = { Text(g.toString()) })
                }
            }
            PattaPrimaryButton("Add Item", onClick = {
                onAdd(name, salt, batch, expiry, qty, free, rate, scheme, gst, mrp)
                name = ""; salt = ""; batch = ""; expiry = ""; qty = ""; free = ""; rate = ""; scheme = ""; mrp = ""; picked = false
            }, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun Double.trim(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
