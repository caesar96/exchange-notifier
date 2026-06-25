package com.example.exchangenotifier.worker

import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    companion object {
        const val WORK_NAME = "rate_check_periodic"
    }

    fun schedule(intervalMinutes: Int) {
        val safeInterval = intervalMinutes.toLong().coerceAtLeast(15L)
        val request = PeriodicWorkRequestBuilder<RateCheckWorker>(safeInterval, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        // UPDATE replaces the existing work while keeping the next scheduled run close to original
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun cancel() = workManager.cancelUniqueWork(WORK_NAME)

    /** Fire a one-shot run immediately (used by the "test notification" button in Settings). */
    fun runOnce() {
        val request = OneTimeWorkRequestBuilder<RateCheckWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        workManager.enqueue(request)
    }
}
