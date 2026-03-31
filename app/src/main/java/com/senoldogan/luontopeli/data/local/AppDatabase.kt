package com.senoldogan.luontopeli.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.senoldogan.luontopeli.data.local.dao.NatureSpotDao
import com.senoldogan.luontopeli.data.local.dao.WalkSessionDao
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import com.senoldogan.luontopeli.data.local.entity.WalkSession

@Database(entities = [NatureSpot::class, WalkSession::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun natureSpotDao(): NatureSpotDao
    abstract fun walkSessionDao(): WalkSessionDao

}
