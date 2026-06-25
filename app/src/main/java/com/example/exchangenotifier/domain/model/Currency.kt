package com.example.exchangenotifier.domain.model

enum class Currency(
    val code: String,
    val flag: String,
    val displayName: String,
) {
    USD("USD", "🇺🇸", "US Dollar"),
    EUR("EUR", "🇪🇺", "Euro"),
    GBP("GBP", "🇬🇧", "British Pound"),
    JPY("JPY", "🇯🇵", "Japanese Yen"),
    CAD("CAD", "🇨🇦", "Canadian Dollar"),
    AUD("AUD", "🇦🇺", "Australian Dollar"),
    CHF("CHF", "🇨🇭", "Swiss Franc"),
    MXN("MXN", "🇲🇽", "Mexican Peso"),
    BRL("BRL", "🇧🇷", "Brazilian Real"),
    COP("COP", "🇨🇴", "Colombian Peso"),
    ARS("ARS", "🇦🇷", "Argentine Peso"),
    CNY("CNY", "🇨🇳", "Chinese Yuan"),
    INR("INR", "🇮🇳", "Indian Rupee"),
    KRW("KRW", "🇰🇷", "South Korean Won"),
}
