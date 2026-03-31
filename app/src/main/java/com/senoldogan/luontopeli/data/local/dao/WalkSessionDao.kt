package com.senoldogan.luontopeli.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.senoldogan.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkSessionDao {
    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    @Query("SELECT * FROM walk_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): WalkSession?

    @Insert
    suspend fun insertSession(session: WalkSession)

    @Update
    suspend fun updateSession(session: WalkSession)
}
