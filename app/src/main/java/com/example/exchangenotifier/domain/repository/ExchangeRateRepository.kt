package com.example.exchangenotifier.domain.repository

import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.model.RateSnapshot
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface ExchangeRateRepository {
    /** Fetches the current rate from the network and persists a snapshot to Room. */
    suspend fun getLatestUsdMxn(): Result<RateSnapshot>

    /** Fetches daily historical rates from the network (no local caching). */
    suspend fun getUsdMxnSeries(from: LocalDate, to: LocalDate): Result<List<RatePoint>>

    /** Observes locally stored snapshots (from Room) for the intraday chart view. */
    fun observeLocalSnapshots(from: Instant): Flow<List<RateSnapshot>>

    /** Deletes all locally cached snapshots. */
    suspend fun clearLocalHistory()
}
