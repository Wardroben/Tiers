package ru.smalljinn.tiers

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.IOException
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.smalljinn.tiers.data.database.TierDatabase
import ru.smalljinn.tiers.data.database.dao.TierDao
import ru.smalljinn.tiers.data.database.model.TierList

@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private lateinit var tierDao: TierDao
    private lateinit var db: TierDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TierDatabase::class.java).build()
        tierDao = db.tierDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeTierListAndRead() = runBlocking {
        val tierList: TierList = TierUtils.createTierList()
        val insertedListId = tierDao.insertTierList(tierList)

        //val categories = TierUtils.createBaseTierCategories(insertedListId)

        val elements = TierUtils.createTierElementsForList(12, insertedListId)
        val insertedElementsIds = tierDao.insertTierElements(elements)

        val lists = tierDao.getTierListsStream().first()
        assert(lists.size == 1)
        val addedList = tierDao.getTierListWithCategoriesStream(insertedListId).first()
        assertThat(addedList.tierList, equalTo(tierList.copy(id = insertedListId)))
        assert(addedList.categories.isEmpty())
        val unassertedElements = tierDao.getUnassertedElementsStream(insertedListId).first()
        assert(elements.containsAll(unassertedElements))

    }

    @Test
    @Throws(Exception::class)
    fun nestedRelationshipsTest() = runBlocking {
        val tierList = TierUtils.createTierList()
        val insertedTierListId = tierDao.insertTierList(tierList)

        val tierElement = TierUtils.createTierElement(tierListId = insertedTierListId)
        val insertedElementId = tierDao.insertTierElement(tierElement)

        val unassertedElements = tierDao.getUnassertedElementsStream(insertedTierListId).first()
        assert(unassertedElements.size == 1)

        val tierCategory = TierUtils.createTierCategory(tierListId = insertedTierListId)
        val insertedTierCategoryId = tierDao.insertTierCategory(tierCategory)

        tierDao.insertTierElement(
            tierElement.copy(
                id = insertedElementId,
                categoryId = insertedTierCategoryId
            )
        )

        val unassertedElementsAfterAssertingElement =
            tierDao.getUnassertedElementsStream(insertedTierListId).first()
        assert(unassertedElementsAfterAssertingElement.isEmpty())

        val tierListWithCategories =
            tierDao.getTierListWithCategoriesStream(insertedTierListId).first()
        assert(tierListWithCategories.categories.size == 1)
        assert(tierListWithCategories.categories.first().elements.size == 1)
    }
}