package com.example.exchangenotifier.di

import com.example.exchangenotifier.data.repository.FrankfurterRepository
import com.example.exchangenotifier.domain.repository.ExchangeRateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // To swap providers: implement a new ExchangeRateRepository and change the binding here.
    @Binds @Singleton
    abstract fun bindExchangeRateRepository(impl: FrankfurterRepository): ExchangeRateRepository
}
