package com.example.exchangenotifier.domain.provider

import com.example.exchangenotifier.domain.model.RatePoint
import java.time.LocalDate

interface RateProvider {
    val id: String
    val displayName: String
    /** True when the provider exposes historical daily series (not just the latest quote). */
    val supportsTimeSeries: Boolean

    suspend fun fetchLatestRate(): Result<Double>
    suspend fun fetchSeries(from: LocalDate, to: LocalDate): Result<List<RatePoint>>
}
