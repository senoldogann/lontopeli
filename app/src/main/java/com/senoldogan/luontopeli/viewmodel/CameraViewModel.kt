package com.senoldogan.luontopeli.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import com.senoldogan.luontopeli.data.repository.NatureSpotRepository
import com.senoldogan.luontopeli.ml.ClassificationResult
import com.senoldogan.luontopeli.ml.PlantClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: NatureSpotRepository,
    private val classifier: PlantClassifier
) : ViewModel() {

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Tunnistustulos
    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()

    // Nykyinen sijainti (asetetaan MapViewModelista tai pidetään tässä)
    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

    fun takePhotoAndClassify(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true
        viewModelScope.launch {
            // 1. Ota kuva
            val imagePath = takePhotoSuspend(context, imageCapture)
            if (imagePath == null) {
                _isLoading.value = false
                return@launch
            }

            _capturedImagePath.value = imagePath

            // 2. Tunnista kasvi kuvasta
            try {
                val uri = Uri.fromFile(File(imagePath))
                val result = classifier.classify(uri, context)
                _classificationResult.value = result
            } catch (e: Exception) {
                _classificationResult.value = ClassificationResult.Error(e.message ?: "Tuntematon virhe")
            }

            _isLoading.value = false
        }
    }

    private suspend fun takePhotoSuspend(context: Context, imageCapture: ImageCapture): String? =
        suspendCancellableCoroutine { continuation ->
            // Luo tiedostonimi aikaleimasta
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputDir = File(context.filesDir, "nature_photos").also { it.mkdirs() }
            val outputFile = File(outputDir, "IMG_${timestamp}.jpg")

            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(outputFile.absolutePath)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resume(null)
                    }
                }
            )
        }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
        _classificationResult.value = null
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        viewModelScope.launch {
            val result = _classificationResult.value

            val spot = NatureSpot(
                name = when (result) {
                    is ClassificationResult.Success -> result.label
                    else -> "Luontolöytö"
                },
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence
            )
            repository.insertSpot(spot)
            clearCapturedImage()
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}
