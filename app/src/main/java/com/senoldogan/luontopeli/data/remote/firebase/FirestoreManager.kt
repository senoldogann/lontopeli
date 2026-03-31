package com.senoldogan.luontopeli.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreManager @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()
    private val spotsCollection = db.collection("nature_spots")

    suspend fun saveSpot(spot: NatureSpot): Result<Unit> {
        return try {
            val data = mapOf(
                "id" to spot.id,
                "name" to spot.name,
                "latitude" to spot.latitude,
                "longitude" to spot.longitude,
                "plantLabel" to spot.plantLabel,
                "confidence" to spot.confidence,
                "imageFirebaseUrl" to spot.imageFirebaseUrl,
                "userId" to spot.userId,
                "timestamp" to spot.timestamp
            )
            spotsCollection.document(spot.id).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserSpots(userId: String): Flow<List<NatureSpot>> = callbackFlow {
        val listener = spotsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val spots = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        NatureSpot(
                            id = doc.getString("id") ?: return@mapNotNull null,
                            name = doc.getString("name") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            plantLabel = doc.getString("plantLabel"),
                            confidence = doc.getDouble("confidence")?.toFloat(),
                            imageFirebaseUrl = doc.getString("imageFirebaseUrl"),
                            userId = doc.getString("userId") ?: "unknown",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            synced = true
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(spots)
            }
        awaitClose { listener.remove() }
    }
}
