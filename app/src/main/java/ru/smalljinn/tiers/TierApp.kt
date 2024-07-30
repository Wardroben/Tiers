package ru.smalljinn.tiers

import android.app.Application
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import ru.smalljinn.tiers.data.database.TierDatabase
import ru.smalljinn.tiers.data.database.dao.TierDao
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepositoryImpl
import ru.smalljinn.tiers.data.images.repository.ImageRepository
import ru.smalljinn.tiers.data.images.repository.ImageRepositoryImpl
import ru.smalljinn.tiers.data.images.source.BASE_URL
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi

class TierApp : Application() {
    companion object {
        lateinit var appContainer: AppContainer
    }

    override fun onCreate() {
        super.onCreate()
        val tierDao = TierDatabase.getInstance(applicationContext).tierDao()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(BASE_URL)
            .build()
        val googleSearchService = retrofit.create(GoogleSearchApi::class.java)
        appContainer = AppContainerImpl(tierDao, googleSearchService)
    }
}

interface AppContainer {
    val tierElementRepository: TierElementRepository
    val tierCategoryRepository: TierCategoryRepository
    val tierListRepository: TierListRepository
    val imageRepository: ImageRepository
}

private class AppContainerImpl(
    private val tierDao: TierDao,
    private val googleSearchApi: GoogleSearchApi
) : AppContainer {
    override val tierElementRepository: TierElementRepository
        get() = TierElementRepositoryImpl(tierDao)
    override val tierCategoryRepository: TierCategoryRepository
        get() = TierCategoryRepositoryImpl(tierDao)
    override val tierListRepository: TierListRepository
        get() = TierListRepositoryImpl(tierDao)
    override val imageRepository: ImageRepository
        get() = ImageRepositoryImpl(googleSearchApi)
}