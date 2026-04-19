package com.rogers.eventapp.presentation.navigation

sealed class Route(val route: String) {
    object EventList : Route("event_list")
    object Bookmarks : Route("bookmarks")
    object EventDetail : Route("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
}