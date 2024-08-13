package ru.smalljinn.tiers.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierElement

@Dao
interface ElementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElement(element: TierElement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierElements(elements: List<TierElement>): List<Long>

    @Delete
    suspend fun deleteElement(element: TierElement)

    @Delete
    suspend fun deleteElements(elements: List<TierElement>)

    @Query("SELECT * FROM tier_elements WHERE id = :id")
    suspend fun getElementById(id: Long): TierElement

    @Query("SELECT * FROM tier_elements WHERE tierListId = :listId AND categoryId is NULL")
    fun getUnassertedElementsStream(listId: Long): Flow<List<TierElement>>

    @Query("SELECT * FROM tier_elements WHERE tierListId = :listId")
    suspend fun getTierListElements(listId: Long): List<TierElement>
}