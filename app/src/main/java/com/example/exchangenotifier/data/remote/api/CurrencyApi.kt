package com.example.exchangenotifier.data.remote.api

import kotlinx.serialization.json.JsonObject
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {
    @GET("v1/currencies/{base}.json")
    suspend fun getLatest(@Path("base") base: String): JsonObject
}
