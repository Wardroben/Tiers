package ru.smalljinn.tiers.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierList

@Dao
interface TierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierList(tierList: TierList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierCategory(tierCategory: TierCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierElement(tierElement: TierElement)

    @Delete
    suspend fun deleteTierList(tierList: TierList)

    @Delete
    suspend fun deleteTierCategory(tierCategory: TierCategory)

    @Delete
    suspend fun deleteTierElement(tierElement: TierElement)

    @Query("SELECT * FROM tier_lists")
    fun getTierListsStream(): Flow<List<TierList>>

    @Query("SELECT * FROM tier_lists WHERE id = :id")
    suspend fun getTierListById(id: Long): TierList
}