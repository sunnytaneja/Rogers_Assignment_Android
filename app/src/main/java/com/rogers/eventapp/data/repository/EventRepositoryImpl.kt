package com.rogers.eventapp.data.repository

import android.util.Log
import com.rogers.eventapp.data.cache.CacheResponse
import com.rogers.eventapp.data.local.dao.EventDao
import com.rogers.eventapp.data.mapper.toDomain
import com.rogers.eventapp.data.mapper.toEntity
import com.rogers.eventapp.data.remote.api.EventApiService
import com.rogers.eventapp.data.remote.dto.EventsResponseDto
import com.rogers.eventapp.domain.model.Event
import com.rogers.eventapp.domain.repository.EventRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val eventApiService: EventApiService,
    private val responseCache: CacheResponse
) : EventRepository {
    private val TAG = "EventRepositoryImpl"
    override fun getEvents(forceRefresh: Boolean): Flow<Result<List<Event>>> = flow {

        // Memory Cache
        if (!forceRefresh && responseCache.isEventsCacheValid()) {
            Log.i(TAG, "Using cached events")
            val cachedDtos = responseCache.getEvents()
            if (cachedDtos != null) {
                val bookmarkedIds = eventDao.getAllBookmarkIds().toSet()
                val events = cachedDtos.map { dto ->
                    dto.toDomain(isBookmarked = dto.id in bookmarkedIds)
                }
                emit(Result.success(events))
                return@flow
            }
        }

        // DB cache
        if (!forceRefresh) {
            Log.i(TAG, "Using local events")

            val entities = eventDao.getAllEvents().first()

            if (entities.isNotEmpty()) {
                val events = entities.map { it.toDomain() }
                Log.i(TAG, "Send local events")
                emit(Result.success(events))
            }
        }

        // From API
        val apiResult = eventApiService.getEvents()
        delay(3000)
        if (apiResult.isSuccessful) {
            Log.i(TAG, "Using API events")
            val dtos = apiResult.body() ?: EventsResponseDto(emptyList())

            responseCache.putEvents(dtos.events)

            val entities = dtos.events.map { it.toEntity() }
            eventDao.insertEvents(entities)

            val bookmarkedIds = eventDao.getAllBookmarkIds().toSet()
            val events = dtos.events.map { dto ->
                dto.toDomain(isBookmarked = dto.id in bookmarkedIds)
            }
            emit(Result.success(events))
        } else {
            emit(Result.failure(Exception(apiResult.message() ?: "Unknown error")))
        }
    }

    override suspend fun getEventById(id: String): Event? {
        val cachedDtos = responseCache.getEvents()
        val cachedDto = cachedDtos?.find { it.id == id }
        if (cachedDto != null) {
            val isBookmarked = eventDao.isBookmarked(id)
            return cachedDto.toDomain(isBookmarked = isBookmarked)
        }

        val entity = eventDao.getEventById(id) ?: return null
        return entity.toDomain()
    }

    override fun getBookmarkedEvents(): Flow<List<Event>> {
        return eventDao.getBookmarkedEvents().map { bookmarks ->
            bookmarks.map { it.toDomain() }
        }
    }

    override suspend fun isBookmarked(eventId: String): Boolean {
        return eventDao.isBookmarked(eventId)
    }

    override suspend fun toggleBookmark(event: Event) {
        eventDao.toggleBookmark(event.id)
    }

    override suspend fun cacheEvents(events: List<Event>) {
        val entities = events.map { it.toEntity() }
        eventDao.insertEvents(entities)
    }

    override fun getCachedEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents().map { it.map { it.toDomain() } }
    }
}