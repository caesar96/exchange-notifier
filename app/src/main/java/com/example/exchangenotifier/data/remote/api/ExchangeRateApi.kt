package com.example.exchangenotifier.data.remote.api

import com.example.exchangenotifier.data.remote.dto.ExchangeRateApiDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    /** GET /v6/latest/{base} — returns all rates relative to base currency. */
    @GET("latest/{base}")
    suspend fun getLatest(@Path("base") base: String): ExchangeRateApiDto
}
