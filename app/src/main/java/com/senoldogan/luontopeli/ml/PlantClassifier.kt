package com.senoldogan.luontopeli.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class PlantClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ImageLabeler käyttää laitteen koneoppimismallia
    // confidenceThreshold: jätetään pois alle 50% varmuuden tunnisteet
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f)
            .build()
    )

    // Luontoon liittyvät avainsanat suodatukseen
    private val natureKeywords = setOf(
        "plant", "flower", "tree", "shrub", "leaf", "fern", "moss",
        "mushroom", "fungus", "grass", "herb", "bush", "berry",
        "pine", "birch", "spruce", "algae", "lichen", "bark",
        "nature", "forest", "woodland", "botanical", "flora"
    )

    // suspend-funktio: odottaa ML Kitin vastausta asynkronisesti
    suspend fun classify(imageUri: Uri, context: Context): ClassificationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Muunna kuva ML Kitin InputImage-muotoon
                val inputImage = InputImage.fromFilePath(context, imageUri)

                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        // Suodata luontoon liittyvät tunnisteet
                        val natureLabels = labels.filter { label ->
                            natureKeywords.any { keyword ->
                                label.text.contains(keyword, ignoreCase = true)
                            }
                        }

                        val result = if (natureLabels.isNotEmpty()) {
                            // Otetaan parhaiten vastaava tunnistetyyppi
                            val best = natureLabels.maxByOrNull { it.confidence }!!
                            ClassificationResult.Success(
                                label = best.text,
                                confidence = best.confidence,
                                allLabels = labels.take(5)  // Top 5 kaikista tunnistuksista
                            )
                        } else {
                            // Ei luontoon liittyviä tunnisteita
                            ClassificationResult.NotNature(
                                allLabels = labels.take(3)
                            )
                        }

                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    // Vapauta resurssit
    fun close() {
        labeler.close()
    }
}

// Tunnistustulos – sealed class selkeyttää eri tapaukset
sealed class ClassificationResult {
    data class Success(
        val label: String,          // Parhaiten vastaava tunnistetyyppi
        val confidence: Float,      // Varmuus 0.0 – 1.0
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    data class NotNature(
        val allLabels: List<ImageLabel>  // Mitä kuvassa tunnistettiin
    ) : ClassificationResult()

    data class Error(val message: String) : ClassificationResult()
}
