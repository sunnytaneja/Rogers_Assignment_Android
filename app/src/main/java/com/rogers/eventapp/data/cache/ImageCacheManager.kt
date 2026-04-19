package com.rogers.eventapp.data.cache

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.rogers.eventapp.utils.AppConfig.Cache.IMAGE_CACHE_DIR
import com.rogers.eventapp.utils.AppConfig.Cache.IMAGE_DISK_CACHE_BYTES
import com.rogers.eventapp.utils.AppConfig.Cache.IMAGE_MEMORY_CACHE_PCT
import okhttp3.OkHttpClient
import java.io.File

object ImageCacheManager {

    fun buildImageLoader(context: Context, okHttpClient: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, IMAGE_MEMORY_CACHE_PCT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(context.cacheDir, IMAGE_CACHE_DIR))
                    .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                    .build()
            }
            .components {
                add(
                    OkHttpNetworkFetcherFactory(okHttpClient)
                )
            }
            .crossfade(true)
            .build()
    }
}
