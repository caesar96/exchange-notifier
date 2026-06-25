package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.config.ApiConfig
import com.example.exchangenotifier.data.remote.api.CurrencyApi
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.provider.RateProvider
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyApiProvider @Inject constructor(
    private val api: CurrencyApi,
) : RateProvider {

    override val id = "currency_api"
    override val displayName = "Currency API"
    // Each date is a separate CDN endpoint; a full year would require 365 requests.
    override val supportsTimeSeries = false

    private val base = ApiConfig.BASE_CURRENCY.lowercase()
    private val quote = ApiConfig.QUOTE_CURRENCY.lowercase()

    override suspend fun fetchLatestRate(): Result<Double> = runCatching {
        val dto = api.getLatest(base = base)
        dto.usd[quote] ?: error("${ApiConfig.QUOTE_CURRENCY} missing")
    }

    override suspend fun fetchSeries(from: LocalDate, to: LocalDate): Result<List<RatePoint>> =
        Result.failure(UnsupportedOperationException("$displayName does not support bulk time series"))
}
