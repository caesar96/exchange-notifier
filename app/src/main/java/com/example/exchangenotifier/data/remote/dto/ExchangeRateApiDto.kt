package com.example.exchangenotifier.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateApiDto(
    @SerialName("result")    val result: String,
    @SerialName("base_code") val baseCode: String,
    @SerialName("rates")     val rates: Map<String, Double>,
)
