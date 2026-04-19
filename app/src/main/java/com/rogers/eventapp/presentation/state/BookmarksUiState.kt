package com.rogers.eventapp.presentation.state

import com.rogers.eventapp.domain.model.Event

data class BookmarksUiState(
    val bookmarks: List<Event> = emptyList(),
    val isLoading: Boolean = true
)