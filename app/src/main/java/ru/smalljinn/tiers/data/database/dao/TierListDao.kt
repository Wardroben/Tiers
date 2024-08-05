package ru.smalljinn.tiers.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.data.database.model.TierListWithCategoriesAndElements

@Dao
interface TierListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierList(tierList: TierList): Long

    @Delete
    suspend fun deleteTierList(tierList: TierList)

    @Query("SELECT * FROM tier_lists WHERE id = :id")
    suspend fun getTierListById(id: Long): TierList

    @Query("SELECT * FROM tier_lists")
    fun getTierListsStream(): Flow<List<TierList>>

    @Transaction
    @Query("SELECT * FROM tier_lists WHERE id = :id")
    fun getTierListWithCategoriesAndElementsStream(id: Long): Flow<TierListWithCategoriesAndElements>

    @Transaction
    @Query("SELECT * FROM tier_lists")
    fun getAllListsWithCategoriesStream(): Flow<List<TierListWithCategories>>
}