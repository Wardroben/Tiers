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

    @Query("SELECT * FROM tier_elements WHERE id = :listId AND categoryId is NULL")
    fun getUnassertedElementsStream(listId: Long): Flow<List<TierElement>>
}