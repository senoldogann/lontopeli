package com.senoldogan.luontopeli.data.repository

import com.senoldogan.luontopeli.data.local.dao.NatureSpotDao
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import com.senoldogan.luontopeli.data.remote.firebase.AuthManager
import com.senoldogan.luontopeli.data.remote.firebase.FirestoreManager
import com.senoldogan.luontopeli.data.remote.firebase.StorageManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NatureSpotRepository – koordinoi Room-tietokannan (offline) ja Firebasen (online) välillä.
 * Syllabuksen viikko 6 mukainen "Offline-first" toteutus.
 */
@Singleton
class NatureSpotRepository @Inject constructor(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager
) {
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()
    val spotsWithLocation: Flow<List<NatureSpot>> = dao.getSpotsWithLocation()

    // Tallenna löytö: ensin Room, sitten Firebase
    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(userId = authManager.currentUserId ?: "local_user")

        // 1. Tallenna paikallisesti HETI (toimii offline-tilassakin)
        dao.insert(spotWithUser.copy(synced = false))

        // 2. Yritä synkronoida Firebaseen
        syncSpotToFirebase(spotWithUser)
    }

    // Synkronoi yksittäinen kohde Firebaseen
    private suspend fun syncSpotToFirebase(spot: NatureSpot) {
        try {
            // 2a. Lataa kuva Storageen (jos paikallinen kuva olemassa)
            val firebaseImageUrl = spot.imageLocalPath?.let { localPath ->
                storageManager.uploadImage(localPath, spot.id).getOrNull()
            }

            // 2b. Tallenna metadata Firestoreen
            val spotWithUrl = spot.copy(imageFirebaseUrl = firebaseImageUrl)
            firestoreManager.saveSpot(spotWithUrl).getOrThrow()

            // 2c. Merkitse Room:ssa synkronoiduksi
            dao.markSynced(spot.id, firebaseImageUrl ?: "")
        } catch (e: Exception) {
            // Synkronointi epäonnistui – yritetään uudelleen myöhemmin
            // synced = false pysyy Room:ssa
        }
    }

    // Synkronoi kaikki odottavat kohteet (kutsutaan yhteyden palautuessa)
    suspend fun syncPendingSpots() {
        val unsyncedSpots = dao.getUnsyncedSpots()
        unsyncedSpots.forEach { spot ->
            syncSpotToFirebase(spot)
        }
    }

    suspend fun deleteSpot(spot: NatureSpot) {
        dao.delete(spot)
    }
}
