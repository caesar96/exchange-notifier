package com.example.exchangenotifier.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// {"base":"USD","start_date":"...","end_date":"...","rates":{"2026-05-26":{"MXN":17.41},...}}
@Serializable
data class TimeSeriesDto(
    val base: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date")   val endDate: String,
    // outer key = ISO date, inner key = currency code
    val rates: Map<String, Map<String, Double>>
)
