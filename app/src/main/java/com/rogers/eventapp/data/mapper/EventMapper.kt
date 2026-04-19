
package com.rogers.eventapp.data.mapper

import com.rogers.eventapp.data.local.entity.EventEntity
import com.rogers.eventapp.data.remote.dto.EventDto
import com.rogers.eventapp.domain.model.Event

fun EventDto.toDomain(isBookmarked: Boolean): Event = Event(
    id = id,
    title = title,
    latitude = latitude,
    longitude = longitude,
    time = time,
    imageUrl = imageUrl,
    isBookmarked = isBookmarked
)

fun EventDto.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    latitude = latitude,
    longitude = longitude,
    time = time,
    imageUrl = imageUrl,
)

fun EventEntity.toDomain(): Event = Event(
        id = id,
        title = title,
        latitude = latitude,
        longitude = longitude,
        time = time,
        imageUrl = imageUrl,
        isBookmarked = isBookmarked
    )

fun Event.toEntity(): EventEntity = EventEntity(
    id = id,
    title = title,
    latitude = latitude,
    longitude = longitude,
    time = time,
    imageUrl = imageUrl,
    isBookmarked = isBookmarked
)