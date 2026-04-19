package com.rogers.eventapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rogers.eventapp.data.local.dao.EventDao
import com.rogers.eventapp.data.local.entity.EventEntity
import com.rogers.eventapp.utils.AppConfig


@Database(
    entities = [EventEntity::class],
    version = AppConfig.Database.VERSION,
    exportSchema = false
)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}