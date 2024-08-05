package ru.smalljinn.tiers.viewmodel

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.domain.usecase.CreateNewTierListUseCase
import ru.smalljinn.tiers.presentation.ui.screens.tierslist.TiersListViewModel
import ru.smalljinn.tiers.presentation.ui.screens.tierslist.TiersState
import java.io.IOException

class TiersListViewModelTests {
    /*@get:Rule
    val mainDispatcherRule = MainDispatcherRule()*/
    private lateinit var categoryRepository: TierCategoryRepository
    private lateinit var tierRepository: TierListRepository
    private lateinit var viewmodel: TiersListViewModel

    @Before
    fun createDb() {
        categoryRepository = MockCategoryRepositoryImpl()
        tierRepository = MockTiersListRepository()
        viewmodel = TiersListViewModel(
            tierRepository,
            createNewTierListUseCase = CreateNewTierListUseCase(tierRepository, categoryRepository)
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {

    }

    @Test
    @Throws(Exception::class)
    fun tierListRepository_newTierListInserted_FlowUpdated() = runTest {
        var listLists = tierRepository.getAllTierListsStream().first()
        assertEquals(listLists.size, 0)
        val tierList = TierUtils.createTierList(id = 25, "Jojoj")
        tierRepository.insertTierList(tierList)
        listLists = tierRepository.getAllTierListsStream().first()
        assertEquals(listLists.size, 1)
        assertThat(listLists.first(), equalTo(tierList))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun tiersListViewModel_newTierListCreated_SuccessStateSet() = runTest {
        /*var viewModelUiState = viewmodel.uiState.value
        assert(viewModelUiState is TiersState.Loading)
        val newListName = "Jojoj"
        viewmodel.obtainEvent(TiersEvent.CreateNew(newListName))
        viewModelUiState = viewmodel.uiState.value
        //Log.i("TEST", viewModelUiState.toString())
        assert(viewModelUiState is TiersState.Success)
        assert((viewModelUiState as TiersState.Success).tiersList.isNotEmpty())
        assertEquals(viewModelUiState.tiersList.first().name, newListName)*/
        val fakeRepo = MockTiersListRepository()
        val vm = TiersListViewModel(
            fakeRepo,
            createNewTierListUseCase = CreateNewTierListUseCase(tierRepository, categoryRepository)
        )
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }
        /*backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect {}
        }*/
        assertThat(vm.uiState.value, equalTo(TiersState.Loading))
        fakeRepo.insertTierList(TierList(name = "df"))

        val newTierList = TierList(id = 5, name = "Jojoj")
        val newTierListWithCategories =
            TierListWithCategories(newTierList, categories = emptyList())
        fakeRepo.insertTierList(newTierList)
        val successState = vm.uiState.value
        assertThat(successState, equalTo(TiersState.Success(listOf(newTierListWithCategories))))

        collectJob.cancel()
    }

    @Test
    @Throws(Exception::class)
    fun tiersListViewModel_loadTierListFromDb_SuccessStateSet() = runTest {
        val tierList = TierUtils.createTierList(id = 25, "Jojoj")
        tierRepository.insertTierList(tierList)
        val uiState = viewmodel.uiState.value as TiersState.Success
        assertThat(uiState.tiersList.first(), equalTo(tierList))
    }
}