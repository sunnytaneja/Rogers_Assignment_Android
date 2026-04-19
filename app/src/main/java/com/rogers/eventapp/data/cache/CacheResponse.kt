package com.rogers.eventapp.data.cache

import com.rogers.eventapp.data.remote.dto.EventDto
import com.rogers.eventapp.utils.AppConfig.Cache.EVENTS_TTL_MS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheResponse @Inject constructor() {
    private data class CacheEntry<T>(
        val data: T,
        val timestampMs: Long = System.currentTimeMillis()
    ) {
        fun isExpired(ttlMs: Long): Boolean {
            return System.currentTimeMillis() - timestampMs > ttlMs
        }
    }

    private var eventsCache: CacheEntry<List<EventDto>>? = null

    fun getEvents(): List<EventDto>? {
        val entry = eventsCache ?: return null
        return if (entry.isExpired(EVENTS_TTL_MS)) {
            eventsCache = null
            null
        } else {
            entry.data
        }
    }

    fun putEvents(events: List<EventDto>) {
        eventsCache = CacheEntry(events)
    }

//    fun invalidate() {
//        eventsCache = null
//    }

    fun isEventsCacheValid(): Boolean {
        return eventsCache?.isExpired(EVENTS_TTL_MS) == false
    }
}