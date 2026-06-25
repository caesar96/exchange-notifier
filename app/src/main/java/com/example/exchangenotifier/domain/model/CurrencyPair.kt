package com.example.exchangenotifier.domain.model

data class CurrencyPair(val base: Currency, val quote: Currency) {

    /** Stable string key used for persistence, e.g. "USD_MXN". */
    val key: String get() = "${base.code}_${quote.code}"

    /** Display string with flags, e.g. "🇺🇸 USD / 🇲🇽 MXN". */
    val label: String get() = "${base.flag} ${base.code} / ${quote.flag} ${quote.code}"

    companion object {
        val DEFAULT = CurrencyPair(Currency.USD, Currency.MXN)

        fun fromKey(key: String): CurrencyPair? {
            val parts = key.split("_")
            if (parts.size != 2) return null
            val base  = Currency.entries.find { it.code == parts[0] } ?: return null
            val quote = Currency.entries.find { it.code == parts[1] } ?: return null
            return CurrencyPair(base, quote)
        }

        val COMMON = listOf(
            CurrencyPair(Currency.USD, Currency.MXN),
            CurrencyPair(Currency.USD, Currency.EUR),
            CurrencyPair(Currency.USD, Currency.GBP),
            CurrencyPair(Currency.USD, Currency.JPY),
            CurrencyPair(Currency.USD, Currency.CAD),
            CurrencyPair(Currency.USD, Currency.AUD),
            CurrencyPair(Currency.USD, Currency.CHF),
            CurrencyPair(Currency.USD, Currency.BRL),
            CurrencyPair(Currency.USD, Currency.COP),
            CurrencyPair(Currency.USD, Currency.ARS),
            CurrencyPair(Currency.USD, Currency.CNY),
            CurrencyPair(Currency.EUR, Currency.MXN),
            CurrencyPair(Currency.EUR, Currency.GBP),
            CurrencyPair(Currency.EUR, Currency.USD),
        )
    }
}
