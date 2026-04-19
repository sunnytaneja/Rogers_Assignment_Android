package com.rogers.eventapp.di

import android.content.Context
import androidx.room.Room
import com.rogers.eventapp.data.local.EventDatabase
import com.rogers.eventapp.data.local.dao.EventDao
import com.rogers.eventapp.utils.AppConfig
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): EventDatabase {
        return Room.databaseBuilder(
            context,
            EventDatabase::class.java,
            AppConfig.Database.NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideEventDao(
        database: EventDatabase
    ): EventDao {
        return database.eventDao()
    }
}