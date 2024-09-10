package ru.smalljinn.tiers.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepositoryImpl
import ru.smalljinn.tiers.data.images.repository.device.DeviceImageRepository
import ru.smalljinn.tiers.data.images.repository.device.DeviceImageRepositoryImpl
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepository
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepositoryImpl
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepositoryImpl
import ru.smalljinn.tiers.data.share.repository.ShareRepository
import ru.smalljinn.tiers.data.share.repository.ShareRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideShareRepository(shareRepositoryImpl: ShareRepositoryImpl): ShareRepository

    @Binds
    abstract fun provideTierElementRepository(elementRepositoryImpl: TierElementRepositoryImpl): TierElementRepository

    @Binds
    abstract fun provideTierCategoryRepository(categoryRepositoryImpl: TierCategoryRepositoryImpl): TierCategoryRepository

    @Binds
    abstract fun provideTierListRepository(listRepositoryImpl: TierListRepositoryImpl): TierListRepository

    @Binds
    abstract fun providePreferencesRepository(preferencesRepositoryImpl: PreferencesRepositoryImpl): PreferencesRepository

    @Binds
    abstract fun provideDeviceImageRepository(deviceImageRepositoryImpl: DeviceImageRepositoryImpl): DeviceImageRepository

    @Binds
    abstract fun provideSearchRepository(networkImageRepositoryImpl: NetworkImageRepositoryImpl): NetworkImageRepository
}