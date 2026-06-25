package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.data.remote.api.CurrencyApi
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.provider.RateProvider
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    override suspend fun fetchLatestRate(base: String, quote: String): Result<Double> = runCatching {
        val baseKey = base.lowercase()
        val quoteKey = quote.lowercase()
        val response = api.getLatest(baseKey)
        val rates = response[baseKey]?.jsonObject ?: error("$base not found in response")
        rates[quoteKey]?.jsonPrimitive?.double ?: error("$quote not found in rates")
    }

    override suspend fun fetchSeries(base: String, quote: String, from: LocalDate, to: LocalDate): Result<List<RatePoint>> =
        Result.failure(UnsupportedOperationException("$displayName does not support bulk time series"))
}
