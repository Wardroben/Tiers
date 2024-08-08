package ru.smalljinn.tiers.viewmodel

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository

class MockCategoryRepositoryImpl : TierCategoryRepository {
    override suspend fun insertCategories(categories: List<TierCategory>): List<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun insertCategory(category: TierCategory): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCategory(category: TierCategory) {
        TODO("Not yet implemented")
    }

    override fun getCategoriesWithElementsStream(): Flow<List<TierCategoryWithElements>> {
        TODO("Not yet implemented")
    }
}