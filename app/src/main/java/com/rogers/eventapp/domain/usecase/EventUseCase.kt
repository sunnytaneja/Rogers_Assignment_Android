package com.rogers.eventapp.domain.usecase

import com.rogers.eventapp.domain.model.Event
import com.rogers.eventapp.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EventsUseCase @Inject constructor(
    private val repository: EventRepository
) {
     fun getEvents(
        forceRefresh: Boolean = false,
        searchQuery: String? = null
    ): Flow<Result<List<Event>>> {
        return repository.getEvents(forceRefresh).map { result ->
            result.map { events ->
                events
                    .filter { event ->
                        searchQuery.isNullOrBlank() ||
                                event.title.contains(searchQuery, ignoreCase = true)
                    }
                    .sortedBy { it.time }
            }
        }
    }

    suspend  fun getEventById(id: String): Event? {
        return repository.getEventById(id)
    }

    fun getBookmarks(): Flow<List<Event>> {
        return repository.getBookmarkedEvents()
    }

    suspend  fun toggleBookmark(event: Event) {
        repository.toggleBookmark(event)
    }

    fun refreshEvents(): Flow<Result<List<Event>>> {
        return repository.getEvents(forceRefresh = true)
    }
}