package com.example.exchangenotifier.di

import com.example.exchangenotifier.config.ApiConfig
import com.example.exchangenotifier.data.remote.api.CurrencyApi
import com.example.exchangenotifier.data.remote.api.ExchangeRateApi
import com.example.exchangenotifier.data.remote.api.FrankfurterApi
import com.example.exchangenotifier.data.remote.api.YahooFinanceApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json".toMediaType()

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        // Some providers (e.g. Yahoo Finance) require a browser-like User-Agent.
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 10)")
                    .build()
            )
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private fun retrofit(client: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory(jsonMediaType))
        .build()

    @Provides @Singleton
    fun provideFrankfurterApi(client: OkHttpClient): FrankfurterApi =
        retrofit(client, ApiConfig.FRANKFURTER_BASE_URL).create(FrankfurterApi::class.java)

    @Provides @Singleton
    fun provideExchangeRateApi(client: OkHttpClient): ExchangeRateApi =
        retrofit(client, ApiConfig.EXCHANGE_RATE_BASE_URL).create(ExchangeRateApi::class.java)

    @Provides @Singleton
    fun provideCurrencyApi(client: OkHttpClient): CurrencyApi =
        retrofit(client, ApiConfig.CURRENCY_API_BASE_URL).create(CurrencyApi::class.java)

    @Provides @Singleton
    fun provideYahooFinanceApi(client: OkHttpClient): YahooFinanceApi =
        retrofit(client, ApiConfig.YAHOO_FINANCE_BASE_URL).create(YahooFinanceApi::class.java)
}
