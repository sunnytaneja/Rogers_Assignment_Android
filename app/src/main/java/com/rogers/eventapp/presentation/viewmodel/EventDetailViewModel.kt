package com.rogers.eventapp.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogers.eventapp.R
import com.rogers.eventapp.domain.model.Event
import com.rogers.eventapp.presentation.state.EventDetailUiState
import com.rogers.eventapp.utils.DistanceUtils
import com.rogers.eventapp.utils.LocationUtils
import com.rogers.eventapp.domain.usecase.EventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val eventsUseCase: EventsUseCase,
) : ViewModel() {

    private val TAG = "EventDetailViewModel"
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val event = eventsUseCase.getEventById(eventId)
                if (event != null) {
                    _uiState.update { it.copy(event = event, isLoading = false, error = null) }
                    fetchDistanceToEvent(event)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = context.getString(R.string.error_event_not_found)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: context.getString(R.string.error_event_load_failed)
                    )
                }
            }
        }
    }

    private fun fetchDistanceToEvent(event: Event) {
        viewModelScope.launch {
            try {
                val location = LocationUtils.getCurrentLocation(context) ?: return@launch
                val distanceKm = DistanceUtils.calculateDistanceKm(
                    location.latitude, location.longitude,
                    event.latitude, event.longitude
                )
                val distanceText = DistanceUtils.formatDistance(distanceKm)
                _uiState.update { it.copy(distanceText = distanceText) }
            } catch (e: Exception) {
                e.message?.let { Log.i(TAG, it) }
            }
        }
    }

    fun onToggleBookmark() {
        val event = _uiState.value.event ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBookmarkLoading = true) }
            try {
                eventsUseCase.toggleBookmark(event)
                val updatedEvent = event.copy(isBookmarked = !event.isBookmarked)
                val message =
                    if (updatedEvent.isBookmarked) context.getString(R.string.added_to_bookmarks) else context.getString(
                        R.string.removed_from_bookmarks
                    )
                _uiState.update { it.copy(event = updatedEvent, isBookmarkLoading = false) }
                _snackbarMessage.emit(message)
            } catch (e: Exception) {
                _uiState.update { it.copy(isBookmarkLoading = false) }
                _snackbarMessage.emit(context.getString(R.string.snack_bookmark_failed))
            }
        }
    }

    fun retry() = loadEvent()
}