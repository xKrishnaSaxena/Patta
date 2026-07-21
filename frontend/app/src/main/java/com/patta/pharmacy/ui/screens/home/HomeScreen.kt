package com.patta.pharmacy.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.data.local.dao.StockRow
import com.patta.pharmacy.voice.rememberVoiceController
import com.patta.pharmacy.ui.components.MoneyText
import com.patta.pharmacy.ui.theme.ExpiryAmber
import com.patta.pharmacy.ui.theme.MoneyIn
import com.patta.pharmacy.ui.theme.MoneyOut
import com.patta.pharmacy.util.Money

/**
 * @param onNavigate switch to a bottom-tab route ("billing" / "stock" / "khata" / "more")
 */
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val answer by viewModel.answer.collectAsStateWithLifecycle()
    val speak by viewModel.speak.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val voice = rememberVoiceController()
    var listening by remember { mutableStateOf(false) }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val startListen: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            listening = true
            voice.startListening(
                onResult = { alts -> listening = false; viewModel.onQuery(alts) },
                onError = { listening = false },
            )
        } else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(speak) { speak?.let { voice.speak(it); viewModel.clearSpeak() } }

    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("Sharma Medical Store", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("License: 20B/21B-4567", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { SaleHero(state) }

        if (state.nearExpiryCount > 0 || state.lowStock.isNotEmpty()) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.nearExpiryCount > 0) {
                        AlertCard(
                            "${state.nearExpiryCount} dawai 3 mahine mein expire",
                            ExpiryAmber,
                            Modifier.weight(1f),
                        ) { onNavigate("more") }
                    }
                    if (state.lowStock.isNotEmpty()) {
                        AlertCard(
                            "${state.lowStock.size} item kam stock",
                            MoneyOut,
                            Modifier.weight(1f),
                        ) { onNavigate("stock") }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickTile("Naya Bill", Modifier.weight(1f)) { onNavigate("billing") }
                QuickTile("Purchase Entry", Modifier.weight(1f)) { onNavigate("more") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickTile("Khata / Udhaar", Modifier.weight(1f)) { onNavigate("khata") }
                QuickTile("Stock Check", Modifier.weight(1f)) { onNavigate("stock") }
            }
        }

        if (state.lowStock.isNotEmpty()) {
            item {
                Text("Kam stock", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(state.lowStock.take(5)) { row ->
                LowStockRow(row)
            }
        }
    }

        PulsingMicButton(
            listening = listening,
            onClick = startListen,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        )
    }

    answer?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearAnswer() },
            title = { Text("Jawaab") },
            text = { Text(it, style = MaterialTheme.typography.headlineMedium) },
            confirmButton = { TextButton(onClick = { viewModel.clearAnswer() }) { Text("Theek hai") } },
        )
    }
}

@Composable
private fun PulsingMicButton(listening: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "mic")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.9f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "scale",
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "alpha",
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        // Expanding halo ring — brighter while actively listening.
        Box(
            Modifier
                .size(64.dp)
                .scale(scale)
                .alpha(if (listening) ringAlpha else ringAlpha * 0.5f)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(64.dp),
        ) { Icon(Icons.Filled.Mic, contentDescription = "Bolke poocho", modifier = Modifier.size(28.dp)) }
    }
}

@Composable
private fun SaleHero(state: HomeUiState) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Aaj ki Sale", style = MaterialTheme.typography.headlineMedium)
            MoneyText(
                state.day.totalPaise,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Cash ${Money.formatWhole(state.day.cashPaise)}", MoneyIn)
                Pill("UPI ${Money.formatWhole(state.day.upiPaise)}", MaterialTheme.colorScheme.primary)
                Pill("Udhaar ${Money.formatWhole(state.day.udhaarPaise)}", ExpiryAmber)
            }
            Text(
                "${state.day.billsCount} bills",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Box(
        Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AlertCard(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.SemiBold)
            Text("Dekho", style = MaterialTheme.typography.labelLarge, color = color, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun QuickTile(label: String, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.height(96.dp).clickable { onClick() }) {
        Box(Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(label, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun LowStockRow(row: StockRow) {
    Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.medicine.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (row.medicine.salt.isNotBlank()) {
                    Text(row.medicine.salt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                "${row.totalQty.toInt()} ${row.medicine.packType.lowercase()} left",
                style = MaterialTheme.typography.bodyLarge,
                color = MoneyOut,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
