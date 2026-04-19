package com.rogers.eventapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogers.eventapp.R
import com.rogers.eventapp.domain.model.Event
import com.rogers.eventapp.presentation.state.BookmarksUiState
import com.rogers.eventapp.utils.UiText
import com.rogers.eventapp.domain.usecase.EventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val eventsUseCase: EventsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<UiText>()
    val snackbarMessage: SharedFlow<UiText> = _snackbarMessage.asSharedFlow()

    init {
        observeBookmarks()
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            eventsUseCase.getBookmarks()
                .catch {
                    _uiState.update { it.copy(isLoading = false) }
                    _snackbarMessage.emit(UiText.StringResource(R.string.snack_bookmark_load_failed))
                }
                .collect { bookmarks ->
                    _uiState.update { it.copy(bookmarks = bookmarks, isLoading = false) }
                }
        }
    }

    fun onRemoveBookmark(event: Event) {
        viewModelScope.launch {
            try {
                eventsUseCase.toggleBookmark(event.copy(isBookmarked = true))
                _snackbarMessage.emit(UiText.StringResource(R.string.removed_from_bookmarks))
            } catch (e: Exception) {
                _snackbarMessage.emit(UiText.StringResource(R.string.snack_bookmark_failed))
            }
        }
    }
}