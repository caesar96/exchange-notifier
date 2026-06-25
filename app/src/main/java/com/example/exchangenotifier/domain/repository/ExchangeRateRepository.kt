package com.example.exchangenotifier.domain.repository

import com.example.exchangenotifier.domain.model.CurrencyPair
import com.example.exchangenotifier.domain.model.RatePoint
import com.example.exchangenotifier.domain.model.RateSnapshot
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface ExchangeRateRepository {
    suspend fun getLatestRate(pair: CurrencyPair): Result<RateSnapshot>
    suspend fun getRateSeries(pair: CurrencyPair, from: LocalDate, to: LocalDate): Result<List<RatePoint>>
    fun observeLocalSnapshots(pair: CurrencyPair, from: Instant): Flow<List<RateSnapshot>>
    suspend fun clearLocalHistory()
}
