package com.example.exchangenotifier.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YahooFinanceDto(
    @SerialName("quoteResponse") val quoteResponse: QuoteResponse,
) {
    @Serializable
    data class QuoteResponse(
        val result: List<Quote>?,
    ) {
        @Serializable
        data class Quote(
            @SerialName("regularMarketPrice") val regularMarketPrice: Double,
        )
    }
}
