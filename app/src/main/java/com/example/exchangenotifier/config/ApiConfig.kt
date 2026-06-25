package com.example.exchangenotifier.config

object ApiConfig {
    const val BASE_CURRENCY  = "USD"
    const val QUOTE_CURRENCY = "MXN"

    // ── Provider base URLs ────────────────────────────────────────────────────
    /** https://www.frankfurter.app — free, no key, supports time series */
    const val FRANKFURTER_BASE_URL   = "https://api.frankfurter.dev/v1/"

    /** https://www.exchangerate-api.com — free open endpoint, latest only */
    const val EXCHANGE_RATE_BASE_URL = "https://open.er-api.com/v6/"

    /**
     * https://github.com/fawazahmed0/exchange-api — Cloudflare Pages mirror.
     * Avoids `@` in the base URL so Retrofit accepts it (base URL must end with `/`).
     * Path: v1/currencies/{base}.json  (latest only — historical is per-date CDN, unused here)
     */
    const val CURRENCY_API_BASE_URL  = "https://currency-api.pages.dev/"

    /** https://finance.yahoo.com — unofficial but stable v7 quote endpoint, latest only */
    const val YAHOO_FINANCE_BASE_URL = "https://query2.finance.yahoo.com/"
}
