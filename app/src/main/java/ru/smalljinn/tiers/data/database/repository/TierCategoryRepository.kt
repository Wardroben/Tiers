package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements

interface TierCategoryRepository {
    suspend fun insertCategories(categories: List<TierCategory>): List<Long>
    suspend fun insertCategory(category: TierCategory): Long
    suspend fun deleteCategory(category: TierCategory)
    fun getCategoriesWithElementsStream(): Flow<List<TierCategoryWithElements>>
    fun getCategoriesWithElementsOfListStream(listId: Long): Flow<List<TierCategoryWithElements>>
}