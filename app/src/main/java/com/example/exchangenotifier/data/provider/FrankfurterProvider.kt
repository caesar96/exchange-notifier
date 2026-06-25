package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.data.remote.api.FrankfurterApi
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.provider.RateProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrankfurterProvider @Inject constructor(
    private val api: FrankfurterApi,
) : RateProvider {

    override val id = "frankfurter"
    override val displayName = "Frankfurter"
    override val supportsTimeSeries = true

    override suspend fun fetchLatestRate(base: String, quote: String): Result<Double> = runCatching {
        val dto = api.getLatest(base, quote)
        dto.rates[quote] ?: error("$quote missing in response")
    }

    override suspend fun fetchSeries(base: String, quote: String, from: LocalDate, to: LocalDate): Result<List<RatePoint>> = runCatching {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val dto = api.getTimeSeries(
            from    = from.format(fmt),
            to      = to.format(fmt),
            base    = base,
            symbols = quote,
        )
        dto.rates.entries.map { (dateStr, currencies) ->
            val rate = currencies[quote] ?: error("$quote missing for $dateStr")
            RatePoint(date = LocalDate.parse(dateStr, fmt), rate = rate)
        }.sortedBy { it.date }
    }
}
