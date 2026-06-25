package com.example.exchangenotifier.data.remote.api

import com.example.exchangenotifier.data.remote.dto.YahooFinanceDto
import retrofit2.http.GET
import retrofit2.http.Query

interface YahooFinanceApi {
    /** GET /v7/finance/quote?symbols=USDMXN=X */
    @GET("v7/finance/quote")
    suspend fun getQuote(@Query("symbols") symbols: String): YahooFinanceDto
}
