package com.rogers.eventapp.domain.usecase

import android.location.Location
import javax.inject.Inject

class DistanceUseCase @Inject constructor() {

    operator fun invoke(
        userLat: Double?,
        userLng: Double?,
        eventLat: Double,
        eventLng: Double
    ): Double? {
        if (userLat == null || userLng == null) return null
        val result = FloatArray(1)
        Location.distanceBetween(userLat, userLng, eventLat, eventLng, result)
        return (result[0] / 1000.0)   // convert metres → km
    }
}