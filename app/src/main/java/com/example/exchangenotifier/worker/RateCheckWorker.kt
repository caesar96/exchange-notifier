package com.example.exchangenotifier.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.exchangenotifier.data.datastore.PreferencesRepository
import com.example.exchangenotifier.data.local.dao.RateSnapshotDao
import com.example.exchangenotifier.domain.model.CurrencyPair
import com.example.exchangenotifier.domain.repository.ExchangeRateRepository
import com.example.exchangenotifier.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant

@HiltWorker
class RateCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ExchangeRateRepository,
    private val prefsRepo: PreferencesRepository,
    private val dao: RateSnapshotDao,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = prefsRepo.appPreferences.first()
        val pair  = CurrencyPair.fromKey(prefs.selectedPairKey) ?: CurrencyPair.DEFAULT

        val result = repository.getLatestRate(pair)
        if (result.isFailure) return Result.retry()
        val rate = result.getOrThrow().rate

        val cutoffMillis = Instant.now()
            .minusSeconds(prefs.historyRetentionDays * 86_400L)
            .toEpochMilli()
        dao.deleteOlderThan(cutoffMillis)

        val nowAboveUpper = prefs.upperThreshold?.let { rate > it } ?: false
        val nowBelowLower = prefs.lowerThreshold?.let { rate < it } ?: false
        val pairLabel = "${pair.base.code}/${pair.quote.code}"

        if (prefs.upperAlertEnabled && prefs.upperThreshold != null) {
            if (nowAboveUpper && !prefs.wasAboveUpper) {
                notificationHelper.notifyAboveUpper(pairLabel, rate, prefs.upperThreshold)
            }
        }
        if (prefs.lowerAlertEnabled && prefs.lowerThreshold != null) {
            if (nowBelowLower && !prefs.wasBelowLower) {
                notificationHelper.notifyBelowLower(pairLabel, rate, prefs.lowerThreshold)
            }
        }

        prefsRepo.updateRateCrossState(rate, nowAboveUpper, nowBelowLower)
        return Result.success()
    }
}
