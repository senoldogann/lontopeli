package com.senoldogan.luontopeli.di

import android.content.Context
import androidx.room.Room
import com.senoldogan.luontopeli.data.local.AppDatabase
import com.senoldogan.luontopeli.data.local.dao.NatureSpotDao
import com.senoldogan.luontopeli.data.local.dao.WalkSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "luontopeli_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideNatureSpotDao(database: AppDatabase): NatureSpotDao {
        return database.natureSpotDao()
    }

    @Provides
    fun provideWalkSessionDao(database: AppDatabase): WalkSessionDao {
        return database.walkSessionDao()
    }
}
