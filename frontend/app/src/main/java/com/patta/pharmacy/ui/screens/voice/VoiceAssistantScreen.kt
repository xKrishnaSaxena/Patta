package com.patta.pharmacy.ui.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patta.pharmacy.voice.rememberVoiceController

private val examples = listOf(
    "Aaj ka collection?",
    "Dolo ka stock?",
    "Cipla ka payment?",
    "Ramesh ka udhaar?",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceAssistantScreen(
    onBack: () -> Unit,
    viewModel: VoiceAssistantViewModel = hiltViewModel(),
) {
    val lastQuery by viewModel.lastQuery.collectAsStateWithLifecycle()
    val answer by viewModel.answer.collectAsStateWithLifecycle()
    val speak by viewModel.speak.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val voice = rememberVoiceController()

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) voice.startListening(onResult = viewModel::onQuery, onError = viewModel::onError)
        else viewModel.onError("Mic permission chahiye")
    }
    val startVoice: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) voice.startListening(onResult = viewModel::onQuery, onError = viewModel::onError)
        else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    LaunchedEffect(speak) { speak?.let { voice.speak(it); viewModel.clearSpeak() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Peeche") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (lastQuery.isNotBlank()) {
                Text("\"$lastQuery\"", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            answer?.let {
                Card(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Text(it, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(20.dp))
                }
            }

            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                FloatingActionButton(
                    onClick = startVoice,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(96.dp),
                ) { Icon(Icons.Filled.Mic, contentDescription = "Bolo", modifier = Modifier.size(40.dp)) }
            }

            Text("Aap ye bol sakte ho:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                examples.forEach { ex ->
                    AssistChip(onClick = { viewModel.onQueryText(ex) }, label = { Text(ex) })
                }
            }
        }
    }
}
