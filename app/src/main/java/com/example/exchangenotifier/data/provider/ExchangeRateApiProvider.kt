package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.data.remote.api.ExchangeRateApi
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.provider.RateProvider
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateApiProvider @Inject constructor(
    private val api: ExchangeRateApi,
) : RateProvider {

    override val id = "exchangerate_api"
    override val displayName = "ExchangeRate-API"
    override val supportsTimeSeries = false

    override suspend fun fetchLatestRate(base: String, quote: String): Result<Double> = runCatching {
        val dto = api.getLatest(base)
        dto.rates[quote] ?: error("$quote missing in response")
    }

    override suspend fun fetchSeries(base: String, quote: String, from: LocalDate, to: LocalDate): Result<List<RatePoint>> =
        Result.failure(UnsupportedOperationException("$displayName does not support time series on the free tier"))
}
