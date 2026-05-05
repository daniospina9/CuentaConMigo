package com.example.cuentaconmigo.core.di

import com.example.cuentaconmigo.BuildConfig
import com.example.cuentaconmigo.core.network.OpenRouterTransactionParser
import com.example.cuentaconmigo.domain.repository.TransactionParserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindTransactionParserRepository(
        impl: OpenRouterTransactionParser
    ): TransactionParserRepository

    companion object {

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        @Provides
        @Named("openrouter_api_key")
        fun provideOpenRouterApiKey(): String = BuildConfig.OPENROUTER_API_KEY
    }
}