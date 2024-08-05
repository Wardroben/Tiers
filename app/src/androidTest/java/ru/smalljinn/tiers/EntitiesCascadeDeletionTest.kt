package ru.smalljinn.tiers

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.smalljinn.tiers.data.database.TierDatabase
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepositoryImpl
import ru.smalljinn.tiers.domain.usecase.CreateNewTierListUseCase

const val BASE_CATEGORIES_COUNT = 5

@RunWith(AndroidJUnit4::class)
class EntitiesCascadeDeletionTest {
    private lateinit var createNewTierListUseCase: CreateNewTierListUseCase
    private lateinit var categoryRepository: TierCategoryRepository
    private lateinit var tierListRepository: TierListRepository
    private lateinit var db: TierDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TierDatabase::class.java).build()
        tierListRepository = TierListRepositoryImpl(db.tierListDao())
        categoryRepository = TierCategoryRepositoryImpl(db.categoryDao())
        createNewTierListUseCase = CreateNewTierListUseCase(tierListRepository, categoryRepository)
        //TODO Ye ye this is very bad. New use case you will see soon
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun createNewTierListUseCase_TierListDelete_AllCategoriesAndElementsDeletedWithList() =
        runTest {
            //create empty list with base categories
            createNewTierListUseCase()
            var tierLists = tierListRepository.getAllListsWithCategoriesStream().first()
            var categories = categoryRepository.getCategoriesWithElementsStream().first()

            assertThat(tierLists.size, equalTo(1))
            assertThat(categories.size, equalTo(BASE_CATEGORIES_COUNT))

            tierListRepository.deleteTierList(tierLists.first().list)
            tierLists = tierListRepository.getAllListsWithCategoriesStream().first()
            categories = categoryRepository.getCategoriesWithElementsStream().first()

            assertThat(tierLists.size, equalTo(0))
            assertThat(categories.size, equalTo(0))
        }
}