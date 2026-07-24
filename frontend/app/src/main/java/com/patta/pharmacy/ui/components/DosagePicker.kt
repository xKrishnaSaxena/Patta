package com.patta.pharmacy.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Common Indian pharmacy dosage patterns. "1-0-1" = morning-noon-night counts.
 * Tapping a chip builds a human-readable line the shopkeeper can still edit.
 */
private val timingChips = listOf(
    "1-0-0" to "🌅 Subah 1",
    "0-0-1" to "🌙 Raat 1",
    "1-0-1" to "🌅🌙 Subah-Raat",
    "1-1-1" to "🌅☀️🌙 Din mein 3 baar",
    "SOS" to "Zaroorat padne par",
    "Cream 2x" to "🧴 Din mein 2 baar lagayein",
)
private val foodChips = listOf("Khaana ke baad", "Khaana se pehle", "Doodh ke saath")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DosagePicker(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PattaField(value, onChange, "Dosage / kaise le (bill pe chhpega)", Modifier.fillMaxWidth(), singleLine = false)
        Text("Jaldi bharo:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            timingChips.forEach { (_, label) ->
                FilterChip(selected = false, onClick = { onChange(appendPart(value, label)) }, label = { Text(label) })
            }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            foodChips.forEach { f ->
                FilterChip(selected = false, onClick = { onChange(appendPart(value, f)) }, label = { Text(f) })
            }
        }
    }
}

private fun appendPart(current: String, part: String): String =
    if (current.isBlank()) part else "$current · $part"
