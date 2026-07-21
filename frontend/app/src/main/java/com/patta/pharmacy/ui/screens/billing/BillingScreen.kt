package com.patta.pharmacy.ui.screens.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.local.dao.SellableRow
import com.patta.pharmacy.data.local.entity.CustomerEntity
import com.patta.pharmacy.ui.components.PattaField
import com.patta.pharmacy.data.repo.CartLine
import com.patta.pharmacy.ui.components.BarcodeScanner
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.components.QtyStepper
import com.patta.pharmacy.ui.components.SearchBarWithMic
import com.patta.pharmacy.ui.screens.stock.monthsUntilExpiry
import com.patta.pharmacy.ui.theme.ExpiryAmber
import com.patta.pharmacy.ui.theme.MoneyIn
import com.patta.pharmacy.util.BillPdf
import com.patta.pharmacy.util.Money
import com.patta.pharmacy.voice.VoicePrefs
import com.patta.pharmacy.voice.rememberVoiceController

private val UpiBlue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(viewModel: BillingViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val customerResults by viewModel.customerResults.collectAsStateWithLifecycle()
    val lastBill by viewModel.lastBill.collectAsStateWithLifecycle()
    val speak by viewModel.speak.collectAsStateWithLifecycle()
    val pending by viewModel.pending.collectAsStateWithLifecycle()
    var showUdhaar by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val voice = rememberVoiceController()
    val listen: () -> Unit = {
        if (VoicePrefs.engine(context) == "vosk") viewModel.startOfflineVoice(viewModel::onVoiceError)
        else voice.startListening(onResult = viewModel::onVoiceResult, onError = viewModel::onVoiceError)
    }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen() else viewModel.onVoiceError("Mic permission chahiye voice ke liye")
    }
    val startVoice: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) listen() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    var showScanner by remember { mutableStateOf(false) }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) showScanner = true else viewModel.onVoiceError("Camera permission chahiye scan ke liye")
    }
    val startScan: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) showScanner = true else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(speak) {
        speak?.let {
            voice.speak(it)
            viewModel.clearSpeak()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sharma Medical Store", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            SearchBarWithMic(
                value = query,
                onValueChange = viewModel::onQuery,
                placeholder = "Dawai ka naam bolo ya likho",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onMic = startVoice,
                onBarcode = startScan,
            )

            if (query.isNotBlank()) {
                SearchResults(
                    results = results,
                    query = query,
                    onPick = viewModel::addToCart,
                    onMissed = { viewModel.logMissedSale(query) },
                )
            } else {
                Box(Modifier.weight(1f)) {
                    if (cart.isEmpty()) EmptyCart()
                    else CartList(cart, viewModel::changeQty, viewModel::changeUnit, viewModel::removeLine)
                }
                SummaryBar(
                    subtotal = totals.subtotalPaise,
                    gst = totals.gstPaise,
                    total = totals.totalPaise,
                    enabled = cart.isNotEmpty(),
                    onPay = { mode -> if (mode == "udhaar") showUdhaar = true else viewModel.checkout(mode) },
                )
            }
        }
    }

    if (showScanner) {
        BarcodeScanner(
            onResult = { code -> showScanner = false; viewModel.addByBarcode(code) },
            onDismiss = { showScanner = false },
        )
    }

    if (pending.isNotEmpty()) {
        val item = pending.first()
        AlertDialog(
            onDismissRequest = { viewModel.skipPending(item.id) },
            title = { Text("Confirm karo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Aapne bola: \"${item.query}\" (${item.qty} ${if (item.perTablet) "tablet" else "strip"})")
                    Text("Kaunsi dawai?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    item.candidates.forEach { row ->
                        Text(
                            "${row.medicine.name}${if (row.medicine.salt.isNotBlank()) " · ${row.medicine.salt}" else ""}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.confirmPending(item.id, row) }.padding(vertical = 6.dp),
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { viewModel.skipPending(item.id) }) { Text("Skip") } },
        )
    }

    lastBill?.let { bill ->
        AlertDialog(
            onDismissRequest = { viewModel.clearLastBill() },
            title = { Text("Bill ${bill.billNo} bana ✅") },
            text = { Text("${Money.format(bill.totals.totalPaise)} · ${bill.paymentMode.uppercase()}") },
            confirmButton = {
                TextButton(onClick = { BillPdf.generateAndShare(context, bill); viewModel.clearLastBill() }) {
                    Text("Bhejo (PDF/WhatsApp)")
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.clearLastBill() }) { Text("Close") } },
        )
    }

    if (showUdhaar) {
        UdhaarCustomerDialog(
            results = customerResults,
            onQuery = viewModel::onCustomerQuery,
            onPick = { id -> viewModel.checkoutUdhaar(id); showUdhaar = false },
            onAddNew = { name, phone -> viewModel.addCustomerAndCheckoutUdhaar(name, phone); showUdhaar = false },
            onDismiss = { showUdhaar = false; viewModel.onCustomerQuery("") },
        )
    }
}

@Composable
private fun UdhaarCustomerDialog(
    results: List<CustomerEntity>,
    onQuery: (String) -> Unit,
    onPick: (String) -> Unit,
    onAddNew: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Udhaar kiske naam?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PattaField(query, { query = it; onQuery(it) }, "Customer dhundo", Modifier.fillMaxWidth())
                results.take(4).forEach { c ->
                    Text(
                        "${c.name}${if (c.phone.isNotBlank()) " · ${c.phone}" else ""}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().clickable { onPick(c.id) }.padding(vertical = 6.dp),
                    )
                }
                Text("Ya naya customer:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                PattaField(newName, { newName = it }, "Naam", Modifier.fillMaxWidth())
                PattaField(newPhone, { newPhone = it }, "Phone", Modifier.fillMaxWidth(), numeric = true)
            }
        },
        confirmButton = { TextButton(onClick = { onAddNew(newName, newPhone) }, enabled = newName.isNotBlank()) { Text("Naya + Udhaar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun SearchResults(
    results: List<SellableRow>,
    query: String,
    onPick: (SellableRow) -> Unit,
    onMissed: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (results.isEmpty()) {
            item {
                Card(Modifier.fillMaxWidth().clickable { onMissed() }) {
                    Column(Modifier.padding(16.dp)) {
                        Text("\"$query\" nahi mila", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Tap karo — Missed Sale mein note ho jayega (agle order mein aa jayega)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
        items(results) { row ->
            Card(
                Modifier.fillMaxWidth().clickable { onPick(row) },
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(row.medicine.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        if (row.medicine.salt.isNotBlank()) {
                            Text(row.medicine.salt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            "Batch ${row.batchNo} · Exp ${formatExpiry(row.expiryYm)} · ${row.batchQty.trimQty()} ${row.medicine.packType.lowercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    MoneyText(row.sellMrpPaise, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}

@Composable
private fun CartList(
    cart: List<CartLine>,
    onQty: (Int, Int) -> Unit,
    onUnit: (Int, Boolean) -> Unit,
    onRemove: (Int) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(cart) { index, line ->
            val months = monthsUntilExpiry(line.expiryYm)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(line.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                if (months != null && months <= 6) {
                                    Box(
                                        Modifier.padding(start = 6.dp).size(8.dp)
                                            .background(ExpiryAmber, CircleShape)
                                    )
                                }
                            }
                            Text(
                                "Batch ${line.batchNo} · Exp ${formatExpiry(line.expiryYm)} · ${Money.format(line.ratePaise)}/${line.unitLabel}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        MoneyText(line.lineTotalPaise, style = MaterialTheme.typography.headlineMedium)
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hatao", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (line.allowLooseSale) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(selected = !line.perTablet, onClick = { onUnit(index, false) }, label = { Text(line.packType) })
                                FilterChip(selected = line.perTablet, onClick = { onUnit(index, true) }, label = { Text("Tablet") })
                            }
                        } else {
                            Box(Modifier)
                        }
                        QtyStepper(value = line.qty, onChange = { onQty(index, it) }, min = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBar(
    subtotal: Long,
    gst: Long,
    total: Long,
    enabled: Boolean,
    onPay: (String) -> Unit,
) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Subtotal ${Money.format(subtotal)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("GST ${Money.format(gst)} (included)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TOTAL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    MoneyText(total, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PayButton("CASH", MoneyIn, Modifier.weight(1f), enabled) { onPay("cash") }
                PayButton("UPI", UpiBlue, Modifier.weight(1f), enabled) { onPay("upi") }
                PayButton("UDHAAR", ExpiryAmber, Modifier.weight(1f), enabled) { onPay("udhaar") }
            }
        }
    }
}

@Composable
private fun PayButton(text: String, color: Color, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (enabled) color else color.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(64.dp).clickable(enabled = enabled) { onClick() },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyCart() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎤", style = MaterialTheme.typography.displayMedium)
        Text(
            "Mic dabao aur bolo:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
        listOf("\"Dolo do strip\"", "\"Pan D ek tablet\"", "\"Azithral 500 teen strip\"").forEach {
            Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private fun formatExpiry(ym: Int): String {
    val mm = ym % 100
    val yy = ym / 100 % 100
    return "%02d/%02d".format(mm, yy)
}

private fun Double.trimQty(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()
