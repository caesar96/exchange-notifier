package com.example.exchangenotifier.data.remote.api

import com.example.exchangenotifier.data.remote.dto.CurrencyApiDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {
    /**
     * Base URL: https://currency-api.pages.dev/
     * Full URL: https://currency-api.pages.dev/v1/currencies/{base}.json
     * @param base lowercase currency code, e.g. "usd"
     */
    @GET("v1/currencies/{base}.json")
    suspend fun getLatest(@Path("base") base: String): CurrencyApiDto
}
