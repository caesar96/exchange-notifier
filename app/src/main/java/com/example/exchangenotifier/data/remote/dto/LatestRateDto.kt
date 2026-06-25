package com.example.exchangenotifier.data.remote.dto

import kotlinx.serialization.Serializable

// {"base":"USD","date":"2026-06-24","rates":{"MXN":17.6197}}
@Serializable
data class LatestRateDto(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
