package com.example.exchangenotifier.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exchangenotifier.data.datastore.AppPreferences
import com.example.exchangenotifier.data.datastore.PreferencesRepository
import com.example.exchangenotifier.data.provider.CompositeRateProvider
import com.example.exchangenotifier.domain.provider.RateProvider
import com.example.exchangenotifier.domain.repository.ExchangeRateRepository
import com.example.exchangenotifier.notification.NotificationHelper
import com.example.exchangenotifier.worker.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: PreferencesRepository,
    private val repository: ExchangeRateRepository,
    private val workScheduler: WorkScheduler,
    private val notificationHelper: NotificationHelper,
    compositeProvider: CompositeRateProvider,
) : ViewModel() {

    val prefs: StateFlow<AppPreferences> = prefsRepository.appPreferences.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AppPreferences(
            upperThreshold = null, lowerThreshold = null,
            upperAlertEnabled = false, lowerAlertEnabled = false,
            pollIntervalMinutes = 15, lastKnownRate = null,
            wasAboveUpper = false, wasBelowLower = false,
            historyRetentionDays = 7,
            preferredProvider = CompositeRateProvider.PROVIDER_AUTO,
        )
    )

    /** All registered providers plus the "auto" sentinel. */
    val providers: List<RateProvider> = compositeProvider.all

    fun setUpperThreshold(text: String) = viewModelScope.launch {
        prefsRepository.setUpperThreshold(text.toDoubleOrNull())
    }

    fun setLowerThreshold(text: String) = viewModelScope.launch {
        prefsRepository.setLowerThreshold(text.toDoubleOrNull())
    }

    fun setUpperAlertEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setUpperAlertEnabled(enabled)
        reschedule()
    }

    fun setLowerAlertEnabled(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.setLowerAlertEnabled(enabled)
        reschedule()
    }

    fun setInterval(minutes: Int) = viewModelScope.launch {
        prefsRepository.setPollIntervalMinutes(minutes)
        reschedule()
    }

    fun setHistoryRetentionDays(days: Int) = viewModelScope.launch {
        prefsRepository.setHistoryRetentionDays(days)
    }

    fun setPreferredProvider(id: String) = viewModelScope.launch {
        prefsRepository.setPreferredProvider(id)
    }

    fun runCheck() = workScheduler.runOnce()

    fun testNotification() = notificationHelper.sendTestNotification()

    fun clearHistory() = viewModelScope.launch {
        repository.clearLocalHistory()
    }

    private suspend fun reschedule() {
        val current = prefsRepository.appPreferences.first()
        if (current.upperAlertEnabled || current.lowerAlertEnabled) {
            workScheduler.schedule(current.pollIntervalMinutes)
        } else {
            workScheduler.cancel()
        }
    }
}
