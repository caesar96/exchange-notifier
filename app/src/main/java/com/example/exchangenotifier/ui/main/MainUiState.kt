package com.example.exchangenotifier.ui.main

import androidx.annotation.StringRes
import com.example.exchangenotifier.R
import java.time.Instant

enum class ChartPeriod(@StringRes val labelRes: Int) {
    TODAY(R.string.period_today),
    WEEK(R.string.period_1w),
    MONTH(R.string.period_1m),
    YEAR(R.string.period_1y),
    MAX(R.string.period_max),
}

data class ChartPoint(val label: String, val value: Float)

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(
        val rate: Double,
        val change: Double?,
        val changePercent: Double?,
        val lastUpdated: Instant,
        val chartPoints: List<ChartPoint>,
        val selectedPeriod: ChartPeriod,
    ) : MainUiState
    data class Error(val message: String, val lastRate: Double?) : MainUiState
}
