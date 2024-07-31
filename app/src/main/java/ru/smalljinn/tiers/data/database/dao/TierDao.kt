package ru.smalljinn.tiers.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.TierListWithCategories

@Dao
interface TierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierList(tierList: TierList): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierCategory(tierCategory: TierCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierElement(tierElement: TierElement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierElements(tierElements: List<TierElement>): List<Long>

    @Delete
    suspend fun deleteTierList(tierList: TierList)

    @Delete
    suspend fun deleteTierCategory(tierCategory: TierCategory)

    @Delete
    suspend fun deleteTierElement(tierElement: TierElement)

    @Query("SELECT * FROM tier_lists WHERE id = :id")
    suspend fun getTierListById(id: Long): TierList

    @Query("SELECT * FROM tier_lists")
    fun getTierListsStream(): Flow<List<TierList>>

    @Transaction
    @Query("SELECT * FROM tier_lists WHERE id = :id")
    fun getTierListWithCategoriesStream(id: Long): Flow<TierListWithCategories>

    @Query("SELECT * FROM tier_elements WHERE id = :listId AND categoryId is NULL")
    fun getUnassertedElementsStream(listId: Long): Flow<List<TierElement>>
}