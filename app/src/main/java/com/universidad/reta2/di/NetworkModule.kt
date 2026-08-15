package com.universidad.reta2.di

import android.content.Context
import com.universidad.reta2.data.remote.NetworkChecker
import com.universidad.reta2.data.remote.Reta2ApiService
import com.universidad.reta2.data.remote.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideReta2ApiService(): Reta2ApiService {
        return RetrofitClient.instance.create(Reta2ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNetworkChecker(
        @ApplicationContext context: Context
    ): NetworkChecker {
        return NetworkChecker(context)
    }
}