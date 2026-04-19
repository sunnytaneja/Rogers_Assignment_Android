package com.rogers.eventapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EventsResponseDto(
    @SerializedName("events") val events: List<EventDto>
)

data class EventDto(
    @SerializedName("id")          val id: String,
    @SerializedName("title")       val title: String,
    @SerializedName("latitude")    val latitude: Double,
    @SerializedName("longitude")   val longitude: Double,
    @SerializedName("time")        val time: String,
    @SerializedName("imageUrl")    val imageUrl: String
)
