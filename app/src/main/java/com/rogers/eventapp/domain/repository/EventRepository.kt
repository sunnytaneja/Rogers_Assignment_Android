package com.rogers.eventapp.domain.repository

import com.rogers.eventapp.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEvents(forceRefresh: Boolean = false): Flow<Result<List<Event>>>

    suspend fun getEventById(id: String): Event?

    fun getBookmarkedEvents(): Flow<List<Event>>

    suspend fun isBookmarked(eventId: String): Boolean

    suspend fun toggleBookmark(event: Event)

    suspend fun cacheEvents(events: List<Event>)

    fun getCachedEvents(): Flow<List<Event>>

}