package com.example.exchangenotifier.data.datastore

/** Snapshot of all user-configurable settings read from DataStore. */
data class AppPreferences(
    val upperThreshold: Double?,        // null = not configured
    val lowerThreshold: Double?,        // null = not configured
    val upperAlertEnabled: Boolean,
    val lowerAlertEnabled: Boolean,
    val pollIntervalMinutes: Int,       // minimum 15 (WorkManager constraint)
    val lastKnownRate: Double?,         // persisted across worker runs for cross detection
    val wasAboveUpper: Boolean,         // anti-spam: was rate above upper threshold last run?
    val wasBelowLower: Boolean,         // anti-spam: was rate below lower threshold last run?
    val historyRetentionDays: Int       // how many days of Room snapshots to keep
)
