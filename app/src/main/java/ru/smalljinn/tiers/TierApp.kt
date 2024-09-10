package ru.smalljinn.tiers

import android.app.Application
import android.content.Context
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
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessorImpl
import ru.smalljinn.tiers.data.images.repository.device.DeviceImageRepository
import ru.smalljinn.tiers.data.images.repository.device.DeviceImageRepositoryImpl
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepository
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepositoryImpl
import ru.smalljinn.tiers.data.images.source.BASE_URL
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi
import ru.smalljinn.tiers.data.images.source.JSON_FORMAT
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepositoryImpl
import ru.smalljinn.tiers.data.share.repository.ShareRepositoryImpl
import ru.smalljinn.tiers.domain.usecase.DeleteElementsUseCase
import ru.smalljinn.tiers.domain.usecase.ImportListUseCase
import ru.smalljinn.tiers.features.tier_edit.InsertElementsUseCase
import ru.smalljinn.tiers.features.tier_edit.PinElementUseCase
import ru.smalljinn.tiers.features.tier_edit.RemoveCategoryUseCase
import ru.smalljinn.tiers.features.tier_edit.UnpinElementsUseCase
import ru.smalljinn.tiers.features.tier_lists.CreateNewTierListUseCase
import ru.smalljinn.tiers.features.tier_lists.CreateShareListUseCase
import ru.smalljinn.tiers.features.tier_lists.DeleteTierListUseCase
import ru.smalljinn.tiers.features.tier_lists.ExportShareListUseCase
import ru.smalljinn.tiers.util.network.observer.ConnectivityObserver
import ru.smalljinn.tiers.util.network.observer.NetworkConnectivityObserver


class TierApp : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        val db = TierDatabase.getInstance(applicationContext)
        val tierListDao = db.tierListDao()
        val tierCategoryDao = db.categoryDao()
        val tierElementDao = db.elementDao()
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory(JSON_FORMAT.toMediaType()))
            .baseUrl(BASE_URL)
            .build()
        val googleSearchService = retrofit.create(GoogleSearchApi::class.java)
        val photoProcessor = PhotoProcessorImpl(applicationContext)
        appContainer = AppContainerImpl(
            tierListDao = tierListDao,
            tierCategoryDao = tierCategoryDao,
            tierElementDao = tierElementDao,
            googleSearchApi = googleSearchService,
            photoProcessor = photoProcessor,
            appContext = applicationContext
        )
    }
}

interface AppContainer {
    val deviceImageRepository: DeviceImageRepository
    val tierElementRepository: TierElementRepository
    val tierCategoryRepository: TierCategoryRepository
    val tierListRepository: TierListRepository
    val networkImageRepository: NetworkImageRepository
    val createNewTierListUseCase: CreateNewTierListUseCase
    val deleteElementsUseCase: DeleteElementsUseCase
    val deleteTierListUseCase: DeleteTierListUseCase
    val preferencesRepository: PreferencesRepository
    val connectivityObserver: ConnectivityObserver
    val exportShareListUseCase: ExportShareListUseCase
    val importListUseCase: ImportListUseCase
    val pinElementUseCase: PinElementUseCase
    val unpinElementsUseCase: UnpinElementsUseCase
    val removeCategoryUseCase: RemoveCategoryUseCase
    val insertElementsUseCase: InsertElementsUseCase
}

private class AppContainerImpl(
    private val photoProcessor: PhotoProcessor,
    private val tierListDao: TierListDao,
    private val tierCategoryDao: CategoryDao,
    private val tierElementDao: ElementDao,
    private val googleSearchApi: GoogleSearchApi,
    private val appContext: Context
) : AppContainer {
    override val insertElementsUseCase: InsertElementsUseCase
        get() = InsertElementsUseCase(tierElementRepository)
    override val removeCategoryUseCase: RemoveCategoryUseCase
        get() = RemoveCategoryUseCase(tierCategoryRepository)
    override val pinElementUseCase: PinElementUseCase
        get() = PinElementUseCase(tierElementRepository)
    override val unpinElementsUseCase: UnpinElementsUseCase
        get() = UnpinElementsUseCase(tierElementRepository)
    override val deviceImageRepository: DeviceImageRepository
        get() = DeviceImageRepositoryImpl(photoProcessor)
    override val tierElementRepository: TierElementRepository
        get() = TierElementRepositoryImpl(tierElementDao)
    override val tierCategoryRepository: TierCategoryRepository
        get() = TierCategoryRepositoryImpl(tierCategoryDao)
    override val tierListRepository: TierListRepository
        get() = TierListRepositoryImpl(tierListDao)
    override val networkImageRepository: NetworkImageRepository
        get() = NetworkImageRepositoryImpl(
            preferencesRepository,
            googleSearchApi,
            photoProcessor,
            appContext
        )
    override val createNewTierListUseCase: CreateNewTierListUseCase
        get() = CreateNewTierListUseCase(tierListRepository, tierCategoryRepository)
    override val deleteElementsUseCase: DeleteElementsUseCase
        get() = DeleteElementsUseCase(tierElementRepository, deviceImageRepository)
    override val deleteTierListUseCase: DeleteTierListUseCase
        get() = DeleteTierListUseCase(
            tierElementRepository,
            deleteElementsUseCase,
            tierListRepository
        )
    override val preferencesRepository: PreferencesRepository
        get() = PreferencesRepositoryImpl(appContext = appContext)
    override val connectivityObserver: ConnectivityObserver
        get() = NetworkConnectivityObserver(appContext = appContext)
    override val exportShareListUseCase: ExportShareListUseCase
        get() = ExportShareListUseCase(
            shareRepository = ShareRepositoryImpl(appContext),
            createShareListUseCase = CreateShareListUseCase(
                elementRepository = tierElementRepository,
                listRepository = tierListRepository,
                photoProcessor = photoProcessor
            )
        )
    override val importListUseCase: ImportListUseCase
        get() = ImportListUseCase(
            photoProcessor,
            tierListRepository,
            tierCategoryRepository,
            tierElementRepository
        )
}