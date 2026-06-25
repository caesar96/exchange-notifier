package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.config.ApiConfig
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

    override suspend fun fetchLatestRate(): Result<Double> = runCatching {
        val dto = api.getLatest(ApiConfig.BASE_CURRENCY, ApiConfig.QUOTE_CURRENCY)
        dto.rates[ApiConfig.QUOTE_CURRENCY] ?: error("${ApiConfig.QUOTE_CURRENCY} missing")
    }

    override suspend fun fetchSeries(from: LocalDate, to: LocalDate): Result<List<RatePoint>> = runCatching {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val dto = api.getTimeSeries(
            from    = from.format(fmt),
            to      = to.format(fmt),
            base    = ApiConfig.BASE_CURRENCY,
            symbols = ApiConfig.QUOTE_CURRENCY,
        )
        dto.rates.entries.map { (dateStr, currencies) ->
            val rate = currencies[ApiConfig.QUOTE_CURRENCY]
                ?: error("${ApiConfig.QUOTE_CURRENCY} missing for $dateStr")
            RatePoint(date = LocalDate.parse(dateStr, fmt), rate = rate)
        }.sortedBy { it.date }
    }
}
