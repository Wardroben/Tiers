package ru.smalljinn.tiers

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.smalljinn.tiers.data.database.TierDatabase
import ru.smalljinn.tiers.data.database.dao.CategoryDao
import ru.smalljinn.tiers.data.database.dao.ElementDao
import ru.smalljinn.tiers.data.database.dao.TierListDao
import ru.smalljinn.tiers.data.database.model.TierList

@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private lateinit var tierListDao: TierListDao
    private lateinit var tierCategoryDao: CategoryDao
    private lateinit var tierElementDao: ElementDao
    private lateinit var db: TierDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TierDatabase::class.java).build()
        tierListDao = db.tierListDao()
        tierCategoryDao = db.categoryDao()
        tierElementDao = db.elementDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeTierListAndRead() = runTest {
        val tierList: TierList = TierUtils.createTierList()
        val insertedListId = tierListDao.insertTierList(tierList)

        //val categories = TierUtils.createBaseTierCategories(insertedListId)

        val elements = TierUtils.createTierElementsForList(12, insertedListId)
        val insertedElementsIds = tierElementDao.insertTierElements(elements)

        val lists = tierListDao.getTierListsStream().first()
        assert(lists.size == 1)
        val addedList =
            tierListDao.getTierListWithCategoriesAndElementsStream(insertedListId).first()
        assertThat(addedList.tierList, equalTo(tierList.copy(id = insertedListId)))
        assert(addedList.categories.isEmpty())
        val unassertedElements = tierElementDao.getUnassertedElementsStream(insertedListId).first()
        assert(elements.containsAll(unassertedElements))

    }

    @Test
    @Throws(Exception::class)
    fun nestedRelationshipsTest() = runTest {
        val tierList = TierUtils.createTierList()
        val insertedTierListId = tierListDao.insertTierList(tierList)

        val tierElement = TierUtils.createTierElement(tierListId = insertedTierListId)
        val insertedElementId = tierElementDao.insertElement(tierElement)

        val unassertedElements =
            tierElementDao.getUnassertedElementsStream(insertedTierListId).first()
        assert(unassertedElements.size == 1)

        val tierCategory = TierUtils.createTierCategory(tierListId = insertedTierListId)
        val insertedTierCategoryId = tierCategoryDao.insertCategory(tierCategory)

        tierElementDao.insertElement(
            tierElement.copy(
                id = insertedElementId,
                categoryId = insertedTierCategoryId
            )
        )

        val unassertedElementsAfterAssertingElement =
            tierElementDao.getUnassertedElementsStream(insertedTierListId).first()
        assert(unassertedElementsAfterAssertingElement.isEmpty())

        val tierListWithCategories =
            tierListDao.getTierListWithCategoriesAndElementsStream(insertedTierListId).first()
        assert(tierListWithCategories.categories.size == 1)
        assert(tierListWithCategories.categories.first().elements.size == 1)
    }
}