package com.rogers.eventapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rogers.eventapp.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): EventEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("UPDATE events SET isBookmarked = NOT isBookmarked WHERE id = :eventId")
    suspend fun toggleBookmark(eventId: String)

    @Query("SELECT * FROM events WHERE isBookmarked = 1")
    fun getBookmarkedEvents(): Flow<List<EventEntity>>

    @Query("SELECT id FROM events WHERE isBookmarked = 1")
    suspend fun getAllBookmarkIds(): List<String>

    @Query("SELECT isBookmarked FROM events WHERE id = :eventId")
    suspend fun isBookmarked(eventId: String): Boolean

}