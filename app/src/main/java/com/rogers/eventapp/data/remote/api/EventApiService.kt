package com.rogers.eventapp.data.remote.api

import com.rogers.eventapp.data.remote.dto.EventsResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface EventApiService {
    @GET("events")
    suspend fun getEvents(): Response<EventsResponseDto>
}