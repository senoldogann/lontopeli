package com.senoldogan.luontopeli.data.repository

import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor() {

    // Stub for Firebase Auth
    fun isUserLoggedIn(): Boolean = true
    fun getUserId(): String = "local_user_placeholder"

    // Stub for Firebase Storage & Firestore Sync
    suspend fun syncNatureSpot(spot: NatureSpot): String? {
        // Simulate network delay
        delay(1000)
        // In a real app, we would upload the image to Firebase Storage 
        // and then save the metadata to Firestore.
        return "https://firebasestorage.googleapis.com/v0/b/luntopeli.appspot.com/o/spots%2F${spot.id}"
    }
}
