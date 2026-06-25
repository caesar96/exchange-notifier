package com.example.exchangenotifier.data.repository

import com.example.exchangenotifier.data.local.dao.RateSnapshotDao
import com.example.exchangenotifier.data.local.entity.RateSnapshotEntity
import com.example.exchangenotifier.data.provider.CompositeRateProvider
import com.example.exchangenotifier.domain.model.CurrencyPair
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.model.RateSnapshot
import com.example.exchangenotifier.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrankfurterRepository @Inject constructor(
    private val composite: CompositeRateProvider,
    private val dao: RateSnapshotDao,
) : ExchangeRateRepository {

    override suspend fun getLatestRate(pair: CurrencyPair): Result<RateSnapshot> = runCatching {
        val rate = composite.fetchLatestRate(pair.base.code, pair.quote.code).getOrThrow()
        val now = Instant.now()
        dao.insert(RateSnapshotEntity(rate = rate, timestampMillis = now.toEpochMilli(), pairKey = pair.key))
        RateSnapshot(rate = rate, timestamp = now)
    }

    override suspend fun getRateSeries(pair: CurrencyPair, from: LocalDate, to: LocalDate): Result<List<RatePoint>> =
        composite.fetchSeries(pair.base.code, pair.quote.code, from, to)

    override fun observeLocalSnapshots(pair: CurrencyPair, from: Instant): Flow<List<RateSnapshot>> =
        dao.observeSnapshotsAfter(pair.key, from.toEpochMilli()).map { entities ->
            entities.map { e ->
                RateSnapshot(rate = e.rate, timestamp = Instant.ofEpochMilli(e.timestampMillis))
            }
        }

    override suspend fun clearLocalHistory() = dao.deleteAll()
}
