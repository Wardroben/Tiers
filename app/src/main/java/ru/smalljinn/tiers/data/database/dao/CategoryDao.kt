package ru.smalljinn.tiers.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements

@Dao
interface CategoryDao {
    @Upsert
    suspend fun insertCategory(category: TierCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<TierCategory>): List<Long>

    @Delete
    suspend fun deleteCategory(category: TierCategory)

    @Transaction
    @Query("SELECT * FROM tier_category ORDER BY position ASC")
    fun getCategoriesWithElementsStream(): Flow<List<TierCategoryWithElements>>

    @Transaction
    @Query("SELECT * FROM tier_category WHERE tier_list_id = :listId ORDER BY position ASC")
    fun getCategoriesWithElementsOfListStream(listId: Long): Flow<List<TierCategoryWithElements>>
}