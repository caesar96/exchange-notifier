package com.example.exchangenotifier.data.provider

import com.example.exchangenotifier.data.datastore.PreferencesRepository
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.provider.RateProvider
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeRateProvider @Inject constructor(
    private val frankfurter: FrankfurterProvider,
    private val exchangeRateApi: ExchangeRateApiProvider,
    private val currencyApi: CurrencyApiProvider,
    private val yahooFinance: YahooFinanceProvider,
    private val prefsRepository: PreferencesRepository,
) {
    /** Ordered list of all registered providers. */
    val all: List<RateProvider> = listOf(frankfurter, exchangeRateApi, currencyApi, yahooFinance)

    /** Providers that expose historical series data. */
    private val seriesCapable: List<RateProvider> = all.filter { it.supportsTimeSeries }

    suspend fun fetchLatestRate(base: String, quote: String): Result<Double> {
        val orderedProviders = orderedByPreference(all)
        var lastError: Throwable = RuntimeException("All providers failed")
        for (provider in orderedProviders) {
            val result = provider.fetchLatestRate(base, quote)
            if (result.isSuccess) return result
            lastError = result.exceptionOrNull() ?: lastError
        }
        return Result.failure(lastError)
    }

    suspend fun fetchSeries(base: String, quote: String, from: LocalDate, to: LocalDate): Result<List<RatePoint>> {
        val orderedProviders = orderedByPreference(seriesCapable)
        var lastError: Throwable = RuntimeException("No providers support time series")
        for (provider in orderedProviders) {
            val result = provider.fetchSeries(base, quote, from, to)
            if (result.isSuccess) return result
            lastError = result.exceptionOrNull() ?: lastError
        }
        return Result.failure(lastError)
    }

    private suspend fun orderedByPreference(candidates: List<RateProvider>): List<RateProvider> {
        val preferredId = prefsRepository.appPreferences.first().preferredProvider
        if (preferredId == PROVIDER_AUTO) return candidates
        val preferred = candidates.find { it.id == preferredId } ?: return candidates
        return listOf(preferred) + (candidates - preferred)
    }

    companion object {
        const val PROVIDER_AUTO           = "auto"
        const val PROVIDER_FRANKFURTER    = "frankfurter"
        const val PROVIDER_EXCHANGE_RATE  = "exchangerate_api"
        const val PROVIDER_CURRENCY_API   = "currency_api"
        const val PROVIDER_YAHOO_FINANCE  = "yahoo_finance"
    }
}
