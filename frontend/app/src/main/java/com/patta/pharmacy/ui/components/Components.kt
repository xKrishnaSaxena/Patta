package com.patta.pharmacy.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.patta.pharmacy.util.Money

/** Large, arm's-length-legible amount. Colour by kind: neutral / in / out. */
@Composable
fun MoneyText(
    paise: Long,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    whole: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = if (whole) Money.formatWhole(paise) else Money.format(paise),
        style = style,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier,
    )
}

/** −  n  +  stepper for quantities. Supports whole steps; min clamped at 0. */
@Composable
fun QtyStepper(
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        IconButton(onClick = { if (value > min) onChange(value - 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = "kam karo")
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        IconButton(onClick = { onChange(value + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = "badhao")
        }
    }
}

/** Full-width primary action. */
@Composable
fun PattaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

/**
 * Search field with a leading search icon and a trailing mic icon.
 * The mic is a placeholder until the Phase 4 voice layer wires STT to [onMic].
 */
@Composable
fun SearchBarWithMic(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onMic: () -> Unit = {},
    onBarcode: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMic) {
                    Icon(
                        Icons.Filled.Mic,
                        contentDescription = "bolke likho",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                onBarcode?.let {
                    IconButton(onClick = it) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = "barcode scan",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Compact number/text field for form rows. */
@Composable
fun PattaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    numeric: Boolean = false,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = if (numeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    )
}
