package com.rogers.eventapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val imageUrl: String,
    val cachedAt: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false
)