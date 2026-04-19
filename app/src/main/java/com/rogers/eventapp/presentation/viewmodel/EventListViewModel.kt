package com.rogers.eventapp.presentation.viewmodel

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogers.eventapp.R
import com.rogers.eventapp.domain.model.Event
import com.rogers.eventapp.presentation.state.EventListUiState
import com.rogers.eventapp.utils.DistanceUtils
import com.rogers.eventapp.utils.LocationUtils
import com.rogers.eventapp.domain.usecase.DistanceUseCase
import com.rogers.eventapp.domain.usecase.EventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventsUseCase: EventsUseCase,
    private val distanceUseCase: DistanceUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val TAG = "EventListViewModel"

    private val _uiState = MutableStateFlow(EventListUiState(isLoading = false))
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(replay = 0)
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadEvents()
    }

    private fun loadEvents(forceRefresh: Boolean = false) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(dispatcher) {
            eventsUseCase.getEvents(forceRefresh = forceRefresh)
                .catch { e ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = e.message ?: context.getString(R.string.error_event_load_failed)
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { events ->
                            val enriched = enrichWithDistance(events)
                            _uiState.update { state ->
                                state.copy(
                                    events = enriched,
                                    filteredEvents = applyFilters(
                                        enriched,
                                        state.searchQuery
                                    ),
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { state ->
                                state.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    error = error.message
                                        ?: context.getString(R.string.something_went_wrong)
                                )
                            }
                        }
                    )
                }
        }
    }

    fun setForceRefresh(enabled: Boolean) {
        _uiState.update { it.copy(forceRefresh = enabled) }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        loadEvents(forceRefresh = uiState.value.forceRefresh)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredEvents = applyFilters(state.events, query)
            )
        }
    }

    fun onToggleBookmark(event: Event) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(dispatcher) {
            try {
                eventsUseCase.toggleBookmark(event)
                val message =
                    if (event.isBookmarked) context.getString(R.string.removed_from_bookmarks) else context.getString(
                        R.string.added_to_bookmarks
                    )
                _snackbarMessage.emit(message)

                // Update the local list optimistically
                _uiState.update { state ->
                    val updatedEvents = state.events.map {
                        if (it.id == event.id) it.copy(isBookmarked = !it.isBookmarked) else it
                    }
                    state.copy(
                        events = updatedEvents,
                        filteredEvents = applyFilters(
                            updatedEvents,
                            state.searchQuery,
                        )
                    )
                }
            } catch (e: Exception) {
                _snackbarMessage.emit(context.getString(R.string.snack_bookmark_failed))
            } finally {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onLocationPermissionGranted() {
        _uiState.update { it.copy(hasLocationPermission = true) }
        fetchLocation()
    }

    fun onLocationPermissionDenied() {
        _uiState.update { it.copy(hasLocationPermission = false) }
    }

    private fun fetchLocation() {
        viewModelScope.launch(dispatcher) {
            try {
                val location = LocationUtils.getCurrentLocation(context)
                if (location != null) {
                    _uiState.update { state ->
                        val enriched = enrichWithDistance(state.events, location)
                        state.copy(
                            userLocation = location,
                            events = enriched,
                            filteredEvents = applyFilters(
                                enriched,
                                state.searchQuery,
                            )
                        )
                    }
                } else if (LocationUtils.isLocationEnabled(context)) {
                    _uiState.update { it.copy(locationError = context.getString(R.string.error_location_off)) }
                } else {
                    _uiState.update { it.copy(locationError = context.getString(R.string.error_location_unavailable)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(locationError = e.message) }
            }
        }
    }

    private fun enrichWithDistance(
        events: List<Event>,
        location: Location? = _uiState.value.userLocation
    ): List<Event> {
        if (location == null) return events
        return events.map { event ->
            val distance = DistanceUtils.calculateDistanceKm(
                location.latitude, location.longitude,
                event.latitude, event.longitude
            )
            Log.i(TAG, "Enriched events distance: ${event.title} -> $distance")
            event.copy(distanceKm = distance)
        }.sortedBy { it.distanceKm }
    }

    private fun applyFilters(
        events: List<Event>,
        query: String,
    ): List<Event> {
        return events.filter { event ->
            val matchesQuery = query.isBlank() ||
                    event.title.contains(query, ignoreCase = true)

            matchesQuery
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearLocationError() {
        _uiState.update { it.copy(locationError = null) }
    }
}