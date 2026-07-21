package com.patta.pharmacy.ui.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Full-screen camera barcode scanner. Fires [onResult] once with the first code
 * it reads, then the caller should dismiss it.
 */
@Composable
fun BarcodeScanner(onResult: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var handled by remember { mutableStateOf(false) }
    var provider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(Unit) {
        onDispose { provider?.unbindAll() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val future = ProcessCameraProvider.getInstance(ctx)
                future.addListener({
                    runCatching {
                        val cameraProvider = future.get()
                        provider = cameraProvider
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        val scanner = BarcodeScanning.getClient()
                        analysis.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { proxy ->
                            val mediaImage = proxy.image
                            if (mediaImage == null || handled) {
                                proxy.close()
                            } else {
                                val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
                                scanner.process(image)
                                    .addOnSuccessListener { codes ->
                                        val code = codes.firstOrNull()?.rawValue
                                        if (!code.isNullOrBlank() && !handled) {
                                            handled = true
                                            onResult(code)
                                        }
                                    }
                                    .addOnCompleteListener { proxy.close() }
                            }
                        }
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis,
                        )
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
        )

        Text(
            "Barcode camera ke saamne rakho",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
        ) { Icon(Icons.Filled.Close, contentDescription = "Band karo", tint = Color.White) }
    }
}
