package com.senoldogan.luontopeli.data.local.dao

import androidx.room.*
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow

@Dao
interface NatureSpotDao {

    // Lisää uusi kohde (tai korvaa jos sama id)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(spot: NatureSpot)

    // Kaikki kohteet uusimmasta vanhimpaan (Flow päivittää UI:n automaattisesti)
    @Query("SELECT * FROM nature_spots ORDER BY timestamp DESC")
    fun getAllSpots(): Flow<List<NatureSpot>>

    // Hae sijainnin mukaan kartalle
    @Query("SELECT * FROM nature_spots WHERE latitude != 0.0")
    fun getSpotsWithLocation(): Flow<List<NatureSpot>>

    // Hae synkronoimattomat (Firebase-lähetystä varten)
    @Query("SELECT * FROM nature_spots WHERE synced = 0")
    suspend fun getUnsyncedSpots(): List<NatureSpot>

    // Merkitse synkronoiduksi
    @Query("UPDATE nature_spots SET synced = 1, imageFirebaseUrl = :url WHERE id = :id")
    suspend fun markSynced(id: String, url: String)

    @Delete
    suspend fun delete(spot: NatureSpot)
}
