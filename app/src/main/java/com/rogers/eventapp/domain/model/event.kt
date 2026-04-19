package com.rogers.eventapp.domain.model

data class Event(
    val id: String,
    val title: String,
    val latitude: Double,
    val longitude: Double,
    val time: String,
    val imageUrl: String,
    val isBookmarked: Boolean = false,
    val distanceKm: Double? = null
)
