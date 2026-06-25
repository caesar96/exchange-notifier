package com.example.exchangenotifier.data.remote.api

import com.example.exchangenotifier.data.remote.dto.LatestRateDto
import com.example.exchangenotifier.data.remote.dto.TimeSeriesDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FrankfurterApi {

    // GET /v1/latest?base=USD&symbols=MXN
    @GET("latest")
    suspend fun getLatest(
        @Query("base")    base: String,
        @Query("symbols") symbols: String
    ): LatestRateDto

    // GET /v1/2026-01-01..2026-06-24?base=USD&symbols=MXN
    // The "{from}..{to}" template keeps the ".." literal; date chars are URL-safe and not encoded.
    @GET("{from}..{to}")
    suspend fun getTimeSeries(
        @Path("from")     from: String,
        @Path("to")       to: String,
        @Query("base")    base: String,
        @Query("symbols") symbols: String
    ): TimeSeriesDto
}
