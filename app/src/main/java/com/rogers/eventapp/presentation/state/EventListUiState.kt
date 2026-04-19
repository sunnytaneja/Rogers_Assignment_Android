package com.rogers.eventapp.presentation.state

import android.location.Location
import com.rogers.eventapp.domain.model.Event

data class EventListUiState(
    val events: List<Event> = emptyList(),
    val filteredEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val userLocation: Location? = null,
    val hasLocationPermission: Boolean = false,
    val locationError: String? = null,
    val forceRefresh: Boolean = false
)