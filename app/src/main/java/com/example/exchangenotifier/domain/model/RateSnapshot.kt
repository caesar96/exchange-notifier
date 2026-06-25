package com.example.exchangenotifier.domain.model

import java.time.Instant

data class RateSnapshot(
    val rate: Double,
    val timestamp: Instant
)
