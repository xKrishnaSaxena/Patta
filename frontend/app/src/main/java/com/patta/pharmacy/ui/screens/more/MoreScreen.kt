package com.patta.pharmacy.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class MoreItem(val title: String, val subtitle: String, val route: String? = null)

private val moreSections = listOf(
    "Paisa & Supplier" to listOf(
        MoreItem("Suppliers & Payments", "Kisko kitna dena hai", "suppliers"),
        MoreItem("Purchase Entry", "Supplier ka maal ghusao", "suppliers"),
        MoreItem("Purchase Orders", "Order banao aur bhejo", "purchase_order"),
    ),
    "Stock" to listOf(
        MoreItem("Expiry Management", "Near-expiry aur returns", "expiry"),
        MoreItem("Sale Return", "Bill ki wapsi", "sale_return"),
        MoreItem("Missed Sales", "Jo maanga, nahi tha", "missed_sales"),
        MoreItem("Add / Edit Medicine", "Nayi dawai jodo", "add_medicine"),
    ),
    "Hisaab" to listOf(
        MoreItem("Reports & Day Close", "Din ka hisaab, profit", "reports"),
        MoreItem("GST Summary", "GSTR-1 ready", "gst_summary"),
    ),
    "Voice" to listOf(
        MoreItem("Voice Assistant", "Bolke poocho — collection, stock, payment", "voice_assistant"),
    ),
    "Madad" to listOf(
        MoreItem("App Tour", "Saare features dobara dekho", "tour"),
    ),
    "Settings" to listOf(
        MoreItem("Shop Profile", "Naam, license, GSTIN", "shop_profile"),
        MoreItem("Schedule H1 Register", "Prescription wali dawai ka record", "h1_register"),
        MoreItem("Backup & Sync", "Data safe rakho"),
        MoreItem("Voice & Language", "Hinglish / Hindi / English", "voice_language"),
    ),
)

@Composable
fun MoreScreen(onNavigate: (String) -> Unit = {}) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        moreSections.forEach { (section, entries) ->
            item {
                Text(
                    section.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                )
            }
            items(entries) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable(enabled = entry.route != null) { entry.route?.let(onNavigate) },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            entry.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
