package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.config.ApiConfig
import com.example.exchangenotifier.data.remote.api.YahooFinanceApi
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.provider.RateProvider
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YahooFinanceProvider @Inject constructor(
    private val api: YahooFinanceApi,
) : RateProvider {

    override val id = "yahoo_finance"
    override val displayName = "Yahoo Finance"
    override val supportsTimeSeries = false

    private val symbol = "${ApiConfig.BASE_CURRENCY}${ApiConfig.QUOTE_CURRENCY}=X"

    override suspend fun fetchLatestRate(): Result<Double> = runCatching {
        val quotes = api.getQuote(symbol).quoteResponse.result
        quotes?.firstOrNull()?.regularMarketPrice
            ?: error("No quote returned for $symbol")
    }

    override suspend fun fetchSeries(from: LocalDate, to: LocalDate): Result<List<RatePoint>> =
        Result.failure(UnsupportedOperationException("$displayName time series not implemented"))
}
