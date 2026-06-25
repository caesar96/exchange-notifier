package com.example.exchangenotifier.data.datastore

import com.example.exchangenotifier.data.provider.CompositeRateProvider
import com.example.exchangenotifier.domain.model.CurrencyPair

data class AppPreferences(
    val upperThreshold: Double?,
    val lowerThreshold: Double?,
    val upperAlertEnabled: Boolean,
    val lowerAlertEnabled: Boolean,
    val pollIntervalMinutes: Int,
    val lastKnownRate: Double?,
    val wasAboveUpper: Boolean,
    val wasBelowLower: Boolean,
    val historyRetentionDays: Int,
    val preferredProvider: String,
    val selectedPairKey: String,
)
