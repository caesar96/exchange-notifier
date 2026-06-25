package com.example.exchangenotifier.config

/**
 * Single place to switch data providers.
 *
 * To migrate to another provider (ExchangeRate-API, Banxico, etc.):
 *  1. Update BASE_URL here.
 *  2. Add a new API interface (or update FrankfurterApi) to match the new JSON contract.
 *  3. In RepositoryModule, bind the new implementation to ExchangeRateRepository.
 *  No other code needs to change.
 */
object ApiConfig {
    const val BASE_URL        = "https://api.frankfurter.dev/v1/"
    const val BASE_CURRENCY   = "USD"
    const val QUOTE_CURRENCY  = "MXN"
}
