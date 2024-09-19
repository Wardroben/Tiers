package ru.smalljinn.tiers.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.smalljinn.tiers.data.network.observer.ConnectivityObserver
import ru.smalljinn.tiers.data.network.observer.NetworkConnectivityObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver =
        NetworkConnectivityObserver(context)

}