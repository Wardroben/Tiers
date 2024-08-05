package ru.smalljinn.tiers

import android.app.Application
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
import ru.smalljinn.tiers.data.images.repository.ImageRepository
import ru.smalljinn.tiers.data.images.repository.ImageRepositoryImpl
import ru.smalljinn.tiers.data.images.source.BASE_URL
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi
import ru.smalljinn.tiers.domain.usecase.CreateNewTierListUseCase

class TierApp : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        val db = TierDatabase.getInstance(applicationContext)
        val tierListDao = db.tierListDao()
        val tierCategoryDao = db.categoryDao()
        val tierElementDao = db.elementDao()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(BASE_URL)
            .build()
        val googleSearchService = retrofit.create(GoogleSearchApi::class.java)
        appContainer = AppContainerImpl(
            tierListDao = tierListDao,
            tierCategoryDao = tierCategoryDao,
            tierElementDao = tierElementDao,
            googleSearchApi = googleSearchService
        )
    }
}

interface AppContainer {
    val tierElementRepository: TierElementRepository
    val tierCategoryRepository: TierCategoryRepository
    val tierListRepository: TierListRepository
    val createNewTierListUseCase: CreateNewTierListUseCase
    val imageRepository: ImageRepository
}

private class AppContainerImpl(
    private val tierListDao: TierListDao,
    private val tierCategoryDao: CategoryDao,
    private val tierElementDao: ElementDao,
    private val googleSearchApi: GoogleSearchApi
) : AppContainer {
    override val tierElementRepository: TierElementRepository
        get() = TierElementRepositoryImpl(tierElementDao)
    override val tierCategoryRepository: TierCategoryRepository
        get() = TierCategoryRepositoryImpl(tierCategoryDao)
    override val tierListRepository: TierListRepository
        get() = TierListRepositoryImpl(tierListDao)
    override val imageRepository: ImageRepository
        get() = ImageRepositoryImpl(googleSearchApi)
    override val createNewTierListUseCase: CreateNewTierListUseCase
        get() = CreateNewTierListUseCase(tierListRepository, tierCategoryRepository)
}