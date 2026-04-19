package com.rogers.eventapp.presentation.state

import com.rogers.eventapp.domain.model.Event

data class EventDetailUiState(
    val event: Event? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val distanceText: String? = null,
    val isBookmarkLoading: Boolean = false
)