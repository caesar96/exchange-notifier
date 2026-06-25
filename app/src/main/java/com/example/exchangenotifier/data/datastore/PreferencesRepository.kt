package com.example.exchangenotifier.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.exchangenotifier.data.provider.CompositeRateProvider
import com.example.exchangenotifier.domain.model.CurrencyPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>
) {
    private object Keys {
        val UPPER_THRESHOLD        = doublePreferencesKey("upper_threshold")
        val LOWER_THRESHOLD        = doublePreferencesKey("lower_threshold")
        val UPPER_ALERT_ENABLED    = booleanPreferencesKey("upper_alert_enabled")
        val LOWER_ALERT_ENABLED    = booleanPreferencesKey("lower_alert_enabled")
        val POLL_INTERVAL_MINUTES  = intPreferencesKey("poll_interval_minutes")
        val LAST_KNOWN_RATE        = doublePreferencesKey("last_known_rate")
        val WAS_ABOVE_UPPER        = booleanPreferencesKey("was_above_upper")
        val WAS_BELOW_LOWER        = booleanPreferencesKey("was_below_lower")
        val HISTORY_RETENTION_DAYS = intPreferencesKey("history_retention_days")
        val PREFERRED_PROVIDER     = stringPreferencesKey("preferred_provider")
        val SELECTED_PAIR          = stringPreferencesKey("selected_pair")
    }

    val appPreferences: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            upperThreshold       = prefs[Keys.UPPER_THRESHOLD],
            lowerThreshold       = prefs[Keys.LOWER_THRESHOLD],
            upperAlertEnabled    = prefs[Keys.UPPER_ALERT_ENABLED]    ?: false,
            lowerAlertEnabled    = prefs[Keys.LOWER_ALERT_ENABLED]    ?: false,
            pollIntervalMinutes  = prefs[Keys.POLL_INTERVAL_MINUTES]  ?: 15,
            lastKnownRate        = prefs[Keys.LAST_KNOWN_RATE],
            wasAboveUpper        = prefs[Keys.WAS_ABOVE_UPPER]        ?: false,
            wasBelowLower        = prefs[Keys.WAS_BELOW_LOWER]        ?: false,
            historyRetentionDays = prefs[Keys.HISTORY_RETENTION_DAYS] ?: 7,
            preferredProvider    = prefs[Keys.PREFERRED_PROVIDER]     ?: CompositeRateProvider.PROVIDER_AUTO,
            selectedPairKey     = prefs[Keys.SELECTED_PAIR]          ?: CurrencyPair.DEFAULT.key,
        )
    }

    suspend fun setUpperThreshold(value: Double?) = dataStore.edit { prefs ->
        if (value != null) prefs[Keys.UPPER_THRESHOLD] = value else prefs.remove(Keys.UPPER_THRESHOLD)
    }

    suspend fun setLowerThreshold(value: Double?) = dataStore.edit { prefs ->
        if (value != null) prefs[Keys.LOWER_THRESHOLD] = value else prefs.remove(Keys.LOWER_THRESHOLD)
    }

    suspend fun setUpperAlertEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.UPPER_ALERT_ENABLED] = enabled }

    suspend fun setLowerAlertEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.LOWER_ALERT_ENABLED] = enabled }

    suspend fun setPollIntervalMinutes(minutes: Int) =
        dataStore.edit { it[Keys.POLL_INTERVAL_MINUTES] = minutes }

    suspend fun setHistoryRetentionDays(days: Int) =
        dataStore.edit { it[Keys.HISTORY_RETENTION_DAYS] = days }

    suspend fun setPreferredProvider(id: String) =
        dataStore.edit { it[Keys.PREFERRED_PROVIDER] = id }

    suspend fun setSelectedPair(key: String) =
        dataStore.edit { it[Keys.SELECTED_PAIR] = key }

    suspend fun updateRateCrossState(rate: Double, aboveUpper: Boolean, belowLower: Boolean) =
        dataStore.edit { prefs ->
            prefs[Keys.LAST_KNOWN_RATE] = rate
            prefs[Keys.WAS_ABOVE_UPPER] = aboveUpper
            prefs[Keys.WAS_BELOW_LOWER] = belowLower
        }
}
