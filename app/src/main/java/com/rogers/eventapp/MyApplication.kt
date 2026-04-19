package com.rogers.eventapp

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import com.rogers.eventapp.service.EventRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("WORK", "workerFactory class = ${workerFactory::class.java.name}")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .setMinimumLoggingLevel(Log.INFO)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        Log.d("WORK", "Scheduling work, workerFactory initialized: ${::workerFactory.isInitialized}")
        EventRefreshWorker.schedulePeriodicRefresh(
            WorkManager.getInstance(this)
        )

//        To Text worker to be scheduled
//        EventRefreshWorker.scheduleOneTimeRefresh(
//            WorkManager.getInstance(this)
//        )
    }
}