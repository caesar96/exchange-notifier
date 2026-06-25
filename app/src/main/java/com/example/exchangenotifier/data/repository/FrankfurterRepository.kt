package com.example.exchangenotifier.data.repository

import com.example.exchangenotifier.config.ApiConfig
import com.example.exchangenotifier.data.local.dao.RateSnapshotDao
import com.example.exchangenotifier.data.local.entity.RateSnapshotEntity
import com.example.exchangenotifier.data.remote.api.FrankfurterApi
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.model.RateSnapshot
import com.example.exchangenotifier.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrankfurterRepository @Inject constructor(
    private val api: FrankfurterApi,
    private val dao: RateSnapshotDao
) : ExchangeRateRepository {

    override suspend fun getLatestUsdMxn(): Result<RateSnapshot> = runCatching {
        val dto = api.getLatest(base = ApiConfig.BASE_CURRENCY, symbols = ApiConfig.QUOTE_CURRENCY)
        val rate = dto.rates[ApiConfig.QUOTE_CURRENCY]
            ?: error("${ApiConfig.QUOTE_CURRENCY} missing from API response")
        val now = Instant.now()
        dao.insert(RateSnapshotEntity(rate = rate, timestampMillis = now.toEpochMilli()))
        RateSnapshot(rate = rate, timestamp = now)
    }

    override suspend fun getUsdMxnSeries(
        from: LocalDate,
        to: LocalDate
    ): Result<List<RatePoint>> = runCatching {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val dto = api.getTimeSeries(
            from    = from.format(fmt),
            to      = to.format(fmt),
            base    = ApiConfig.BASE_CURRENCY,
            symbols = ApiConfig.QUOTE_CURRENCY
        )
        dto.rates.entries.map { (dateStr, currencies) ->
            val rate = currencies[ApiConfig.QUOTE_CURRENCY]
                ?: error("${ApiConfig.QUOTE_CURRENCY} missing for date $dateStr")
            RatePoint(date = LocalDate.parse(dateStr, fmt), rate = rate)
        }.sortedBy { it.date }
    }

    override fun observeLocalSnapshots(from: Instant): Flow<List<RateSnapshot>> =
        dao.observeSnapshotsAfter(from.toEpochMilli()).map { entities ->
            entities.map { e ->
                RateSnapshot(rate = e.rate, timestamp = Instant.ofEpochMilli(e.timestampMillis))
            }
        }

    override suspend fun clearLocalHistory() = dao.deleteAll()
}
