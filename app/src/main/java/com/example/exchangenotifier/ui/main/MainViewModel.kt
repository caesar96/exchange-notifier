package com.example.exchangenotifier.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchangenotifier.domain.model.RateSnapshot
import com.example.exchangenotifier.domain.repository.ExchangeRateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ExchangeRateRepository,
) : ViewModel() {

    private val _rateResult = MutableStateFlow<Result<RateSnapshot>?>(null)
    private val _chartPoints = MutableStateFlow<List<ChartPoint>>(emptyList())
    private val _selectedPeriod = MutableStateFlow(ChartPeriod.TODAY)

    val uiState: StateFlow<MainUiState> = combine(
        _rateResult, _chartPoints, _selectedPeriod,
    ) { rateResult, chartPoints, period ->
        rateResult ?: return@combine MainUiState.Loading
        rateResult.fold(
            onSuccess = { snapshot ->
                val firstVal = if (chartPoints.size > 1) chartPoints.first().value.toDouble() else null
                val change = firstVal?.let { snapshot.rate - it }
                val pct = if (firstVal != null && firstVal != 0.0) (change!! / firstVal) * 100.0 else null
                MainUiState.Success(
                    rate = snapshot.rate,
                    change = change,
                    changePercent = pct,
                    lastUpdated = snapshot.timestamp,
                    chartPoints = chartPoints,
                    selectedPeriod = period,
                )
            },
            onFailure = { err ->
                MainUiState.Error(err.message ?: "Error de red", null)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)

    private var chartJob: Job? = null

    init {
        loadRate()
        chartJob = viewModelScope.launch { collectTodaySnapshots() }
    }

    fun refresh() {
        loadRate()
        if (_selectedPeriod.value != ChartPeriod.TODAY) {
            chartJob?.cancel()
            chartJob = viewModelScope.launch { fetchSeriesForPeriod(_selectedPeriod.value) }
        }
        // TODAY: loadRate() inserts into Room → observeLocalSnapshots Flow auto-emits
    }

    fun onPeriodSelected(period: ChartPeriod) {
        if (_selectedPeriod.value == period) return
        _selectedPeriod.value = period
        chartJob?.cancel()
        chartJob = viewModelScope.launch {
            when (period) {
                ChartPeriod.TODAY -> collectTodaySnapshots()
                else -> fetchSeriesForPeriod(period)
            }
        }
    }

    private fun loadRate() {
        viewModelScope.launch {
            _rateResult.value = repository.getLatestUsdMxn()
        }
    }

    private suspend fun collectTodaySnapshots() {
        val from = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
        repository.observeLocalSnapshots(from).collect { snapshots ->
            _chartPoints.value = snapshots.map {
                ChartPoint(
                    label = DateTimeFormatter.ofPattern("HH:mm")
                        .format(it.timestamp.atZone(ZoneId.systemDefault())),
                    value = it.rate.toFloat(),
                )
            }
        }
    }

    private suspend fun fetchSeriesForPeriod(period: ChartPeriod) {
        val today = LocalDate.now()
        val from = when (period) {
            ChartPeriod.WEEK -> today.minusWeeks(1)
            ChartPeriod.MONTH -> today.minusMonths(1)
            ChartPeriod.YEAR -> today.minusYears(1)
            ChartPeriod.MAX -> today.minusYears(5)
            ChartPeriod.TODAY -> return
        }
        val fmt = when (period) {
            ChartPeriod.YEAR, ChartPeriod.MAX -> DateTimeFormatter.ofPattern("MM/yy")
            else -> DateTimeFormatter.ofPattern("dd/MM")
        }
        repository.getUsdMxnSeries(from, today).onSuccess { ratePoints ->
            _chartPoints.value = ratePoints.map {
                ChartPoint(label = fmt.format(it.date), value = it.rate.toFloat())
            }
        }
    }
}
