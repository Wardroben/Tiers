package ru.smalljinn.tiers.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TierCategory): Long

    @Delete
    suspend fun deleteCategory(category: TierCategory)

    @Transaction
    @Query("SELECT * FROM tier_categories")
    fun getCategoriesWithElementsStream(): Flow<List<TierCategoryWithElements>>
}