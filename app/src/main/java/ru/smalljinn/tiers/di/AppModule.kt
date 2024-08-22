package ru.smalljinn.tiers.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.smalljinn.tiers.data.database.DATABASE_NAME
import ru.smalljinn.tiers.data.database.TierDatabase
import ru.smalljinn.tiers.data.database.dao.CategoryDao
import ru.smalljinn.tiers.data.database.dao.ElementDao
import ru.smalljinn.tiers.data.database.dao.TierListDao
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepositoryImpl
import ru.smalljinn.tiers.data.images.repository.device.PhotoProcessor
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepository
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepositoryImpl
import ru.smalljinn.tiers.data.images.source.BASE_URL
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi
import ru.smalljinn.tiers.data.images.source.JSON_FORMAT
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideTierDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, TierDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideTierListDao(db: TierDatabase): TierListDao = db.tierListDao()

    @Provides
    fun provideTierCategoryDao(db: TierDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTierElementDao(db: TierDatabase): ElementDao = db.elementDao()

    @Provides
    fun provideTierCategoryRepository(categoryDao: CategoryDao): TierCategoryRepository =
        TierCategoryRepositoryImpl(categoryDao)

    @Provides
    fun provideTierElementRepository(elementDao: ElementDao): TierElementRepository =
        TierElementRepositoryImpl(elementDao)

    @Provides
    fun provideTierListRepository(tierListDao: TierListDao): TierListRepository =
        TierListRepositoryImpl(tierListDao)

    @Provides
    @Singleton
    fun provideRetrofit() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(Json.asConverterFactory(JSON_FORMAT.toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideGoogleSearchService(retrofit: Retrofit) =
        retrofit.create(GoogleSearchApi::class.java)

    @Provides
    fun provideSearchRepository(
        googleSearchService: GoogleSearchApi,
        photoProcessor: PhotoProcessor
    ): NetworkImageRepository =
        NetworkImageRepositoryImpl(googleSearchService, photoProcessor)
}