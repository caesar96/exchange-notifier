package com.example.exchangenotifier.data.datastore

import com.example.exchangenotifier.data.provider.CompositeRateProvider

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
    /** ID of the preferred data provider; "auto" = try all in order. */
    val preferredProvider: String,
)
