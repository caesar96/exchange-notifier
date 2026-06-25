package com.example.exchangenotifier.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyApiDto(
    val date: String,
    /** Rate map keyed by lowercase target currency. Always requested with USD as base. */
    val usd: Map<String, Double>,
)
