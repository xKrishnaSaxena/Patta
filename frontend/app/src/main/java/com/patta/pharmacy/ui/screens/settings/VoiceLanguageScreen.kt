package com.patta.pharmacy.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import com.patta.pharmacy.voice.VoicePrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLanguageScreen(
    onBack: () -> Unit,
    viewModel: VoiceSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var language by remember { mutableStateOf(VoicePrefs.language(context)) }
    var engine by remember { mutableStateOf(VoicePrefs.engine(context)) }
    val modelReady by viewModel.modelReady.collectAsStateWithLifecycle()
    val downloading by viewModel.downloading.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice & Language", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionLabel("Bhaasha") }
            item {
                Text(
                    "Hinglish sabse accha chalta hai (medicine naam English mein hote hain).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            VoicePrefs.options.forEach { (label, tag) ->
                item {
                    SelectableCard(label, selected = language == tag) {
                        VoicePrefs.setLanguage(context, tag); language = tag
                    }
                }
            }

            item { SectionLabel("Voice Engine") }
            item {
                SelectableCard(
                    title = "Google (online)",
                    subtitle = "Net chahiye · sabse accha (default)",
                    selected = engine == "google",
                ) { VoicePrefs.setEngine(context, "google"); engine = "google" }
            }
            item {
                SelectableCard(
                    title = "Vosk (offline)  ·  BETA",
                    subtitle = if (modelReady) "Model ready · bina net ke chalega" else "Model download karna hoga (~40MB)",
                    selected = engine == "vosk",
                ) { VoicePrefs.setEngine(context, "vosk"); engine = "vosk" }
            }
            if (engine == "vosk" && !modelReady) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        if (downloading) {
                            Text("Download ho raha hai… $progress%", style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            )
                        } else {
                            PattaPrimaryButton("Offline model download karo (~40MB)", onClick = { viewModel.downloadModel() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SelectableCard(title: String, subtitle: String? = null, selected: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                subtitle?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (selected) Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
