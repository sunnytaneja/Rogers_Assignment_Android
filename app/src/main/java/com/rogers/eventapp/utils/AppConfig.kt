package com.rogers.eventapp.utils

import com.rogers.eventapp.BuildConfig

object AppConfig {

    object Network {
        val baseUrl: String get() = BuildConfig.BASE_URL
    }

    object Database {
        const val NAME = BuildConfig.DB_NAME
        const val VERSION = 1
    }

    object Cache {
        const val EVENTS_TTL_MS = 15 * 60 * 1000L
        const val IMAGE_DISK_CACHE_BYTES = 100L * 1024 * 1024
        const val IMAGE_MEMORY_CACHE_PCT = 0.25
        const val IMAGE_CACHE_DIR = "image_cache"
    }
}