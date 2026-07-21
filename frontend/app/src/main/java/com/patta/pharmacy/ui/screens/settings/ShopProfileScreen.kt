package com.patta.pharmacy.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.patta.pharmacy.ui.components.PattaPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopProfileScreen(
    onBack: () -> Unit,
    viewModel: ShopProfileViewModel = hiltViewModel(),
) {
    val initial by viewModel.initial.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf(ShopForm()) }
    var seeded by remember { mutableStateOf(false) }
    LaunchedEffect(initial) { if (!seeded && initial != null) { form = initial!!; seeded = true } }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            PattaPrimaryButton("Save", onClick = { viewModel.save(form, onBack) }, modifier = Modifier.padding(16.dp))
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Ye details bill (PDF) aur app ke header pe dikhengi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PattaField(form.name, { form = form.copy(name = it) }, "Dukaan ka naam", Modifier.fillMaxWidth())
                        PattaField(form.drugLicenseNo, { form = form.copy(drugLicenseNo = it) }, "Drug License No", Modifier.fillMaxWidth())
                        PattaField(form.gstin, { form = form.copy(gstin = it) }, "GSTIN", Modifier.fillMaxWidth())
                        PattaField(form.phone, { form = form.copy(phone = it) }, "Phone", Modifier.fillMaxWidth(), numeric = true)
                        PattaField(form.address, { form = form.copy(address = it) }, "Address", Modifier.fillMaxWidth(), singleLine = false)
                    }
                }
            }
        }
    }
}
