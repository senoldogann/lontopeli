package com.senoldogan.luontopeli.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.senoldogan.luontopeli.viewmodel.CameraViewModel
import com.senoldogan.luontopeli.ml.ClassificationResult
import java.io.File
import androidx.concurrent.futures.await

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Kameran tila
    val capturedImagePath by viewModel.capturedImagePath.collectAsState()
    val classificationResult by viewModel.classificationResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // CameraX-komponentit
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    // Alusta kamera kun näkymä ladataan
    LaunchedEffect(Unit) {
        val cameraProvider = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context).await()
        val preview = androidx.camera.core.Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            // Log.e("CameraScreen", "Binding failed", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedImagePath == null) {
            // 1. Kameran etsinkuva-tila
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Kuvanotto-painike
            FloatingActionButton(
                onClick = { viewModel.takePhotoAndClassify(context, imageCapture) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Camera, contentDescription = "Ota kuva")
                }
            }
        } else {
            // 2. Kuva on otettu – Näytä tulos
            ResultPreview(
                imagePath = capturedImagePath!!,
                result = classificationResult,
                onSave = {
                    viewModel.saveCurrentSpot()
                    onNavigateBack()
                },
                onRetake = { viewModel.clearCapturedImage() }
            )
        }
    }
}

@Composable
fun ResultPreview(
    imagePath: String,
    result: ClassificationResult?,
    onSave: () -> Unit,
    onRetake: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Näytä otettu kuva
        Image(
            painter = rememberAsyncImagePainter(File(imagePath)),
            contentDescription = "Captured image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Tulostiedot (Lasi-efekti/tausta)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(24.dp)
        ) {
            Text(
                text = "Tunnistus:",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )

            when (result) {
                is ClassificationResult.Success -> {
                    Text(
                        text = result.label,
                        color = Color.Green,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Varmuus: ${(result.confidence * 100).toInt()}%",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                is ClassificationResult.NotNature -> {
                    Text("Ei tunnistettu kasvia", color = Color.Yellow)
                }
                is ClassificationResult.Error -> {
                    Text("Virhe: ${result.message}", color = Color.Red)
                }
                null -> {
                    Text("Analysoidaan...", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onRetake,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Icon(Icons.Default.Refresh, "Uudelleen")
                    Text(" Uudelleen")
                }
                Button(onClick = onSave) {
                    Icon(Icons.Default.Check, "Tallenna")
                    Text(" Tallenna")
                }
            }
        }
    }
}
