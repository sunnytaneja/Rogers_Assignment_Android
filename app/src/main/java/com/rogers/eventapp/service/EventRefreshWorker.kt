package com.rogers.eventapp.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.rogers.eventapp.domain.usecase.EventsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class EventRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val eventsUseCase: EventsUseCase
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "EventRefreshWorker"
        const val WORK_NAME = "event_refresh_work"

        fun schedulePeriodicRefresh(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<EventRefreshWorker>(
                30, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES // flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_NAME)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicRequest
            )

            Log.d(TAG, "Periodic event refresh scheduled (every 5 min)")
        }

        fun scheduleOneTimeRefresh(workManager: WorkManager) {
            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeRequest = OneTimeWorkRequestBuilder<EventRefreshWorker>()
                .setConstraints(constraints)
                .addTag("${WORK_NAME}_once")
                .build()

            Log.d(TAG, "One time event refresh scheduled")
            workManager.enqueueUniqueWork(
                "${WORK_NAME}_once",
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting event background refresh...")

        return try {
            val result = eventsUseCase.refreshEvents().first()
            if (result.isSuccess) {
                Log.d(TAG, "Background refresh succeeded: ${result.getOrNull()?.size} events")
                Result.success()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.w(TAG, "Background refresh failed: $error")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Background refresh threw exception: ${e.message}", e)
            Result.retry()
        }
    }
}