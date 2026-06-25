package com.example.exchangenotifier.domain.model

import java.time.LocalDate

data class RatePoint(
    val date: LocalDate,
    val rate: Double
)
