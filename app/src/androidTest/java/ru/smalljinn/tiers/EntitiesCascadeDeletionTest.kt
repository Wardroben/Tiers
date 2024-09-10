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
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepositoryImpl
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepositoryImpl
import ru.smalljinn.tiers.features.tier_lists.CreateNewTierListUseCase

const val BASE_CATEGORIES_COUNT = 5

@RunWith(AndroidJUnit4::class)
class EntitiesCascadeDeletionTest {
    private lateinit var createNewTierListUseCase: CreateNewTierListUseCase
    private lateinit var elementRepository: TierElementRepository
    private lateinit var categoryRepository: TierCategoryRepository
    private lateinit var tierListRepository: TierListRepository
    private lateinit var db: TierDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TierDatabase::class.java).build()
        tierListRepository = TierListRepositoryImpl(db.tierListDao())
        categoryRepository = TierCategoryRepositoryImpl(db.categoryDao())
        elementRepository = TierElementRepositoryImpl(db.elementDao())
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
            val tierListId = createNewTierListUseCase("Aboba")
            repeat(3) {
                elementRepository.insertTierElement(
                    TierUtils.createTierElement(tierListId = tierListId)
                )
            }

            var tierLists = tierListRepository.getAllListsWithCategoriesStream().first()
            var categories = categoryRepository.getCategoriesWithElementsStream().first()

            assertThat(categories.size, equalTo(BASE_CATEGORIES_COUNT))

            var firstCategory = categories.first()

            assertThat(firstCategory.elements.size, equalTo(0))

            repeat(2) {
                elementRepository.insertTierElement(
                    TierUtils.createTierElement(
                        tierListId = tierListId,
                        tierCategoryId = firstCategory.category.id
                    )
                )
            }

            var unassertedElements =
                elementRepository.getNotAttachedElementsOfListStream(tierListId).first()
            categories = categoryRepository.getCategoriesWithElementsStream().first()
            firstCategory = categories.first()

            assertThat(firstCategory.elements.size, equalTo(2))
            assertThat(unassertedElements.size, equalTo(3))
            assertThat(tierLists.size, equalTo(1))

            tierListRepository.deleteTierList(tierLists.first().list)
            tierLists = tierListRepository.getAllListsWithCategoriesStream().first()
            categories = categoryRepository.getCategoriesWithElementsStream().first()
            unassertedElements =
                elementRepository.getNotAttachedElementsOfListStream(tierListId).first()

            assertThat(tierLists.size, equalTo(0))
            assertThat(categories.size, equalTo(0))
            assertThat(unassertedElements.size, equalTo(0))
        }
}