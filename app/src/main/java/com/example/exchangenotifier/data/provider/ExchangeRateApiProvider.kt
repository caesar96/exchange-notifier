package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.config.ApiConfig
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

    override suspend fun fetchLatestRate(): Result<Double> = runCatching {
        val dto = api.getLatest(ApiConfig.BASE_CURRENCY)
        dto.rates[ApiConfig.QUOTE_CURRENCY] ?: error("${ApiConfig.QUOTE_CURRENCY} missing")
    }

    override suspend fun fetchSeries(from: LocalDate, to: LocalDate): Result<List<RatePoint>> =
        Result.failure(UnsupportedOperationException("$displayName does not support time series on the free tier"))
}
