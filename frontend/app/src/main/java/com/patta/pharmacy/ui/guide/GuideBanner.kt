package com.patta.pharmacy.ui.guide

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Persistent bottom banner during the interactive guide. Tapping it re-navigates
 * to the current step's screen (in case the user wandered off); "Chhodo" skips.
 */
@Composable
fun GuideBanner(step: GuideStep, onGo: () -> Unit, onSkip: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.primary, shadowElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().clickable { onGo() }.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${step.title}  (${step.index}/${GuideStep.TOTAL})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Text(
                    step.instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            TextButton(onClick = onSkip) {
                Text("Chhodo", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
