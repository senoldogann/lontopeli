package com.senoldogan.luontopeli.data.repository

import com.senoldogan.luontopeli.data.local.dao.WalkSessionDao
import com.senoldogan.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalkRepository @Inject constructor(
    private val walkSessionDao: WalkSessionDao
) {
    fun getAllWalkSessions(): Flow<List<WalkSession>> = walkSessionDao.getAllSessions()

    suspend fun getActiveSession(): WalkSession? = walkSessionDao.getActiveSession()

    suspend fun insertSession(session: WalkSession) = walkSessionDao.insertSession(session)

    suspend fun updateSession(session: WalkSession) {
        walkSessionDao.updateSession(session)
    }
}
