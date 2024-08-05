package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.dao.CategoryDao
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements

class TierCategoryRepositoryImpl(private val categoryDao: CategoryDao) : TierCategoryRepository {
    override suspend fun insertCategory(category: TierCategory): Long {
        return categoryDao.insertCategory(category)
    }

    override suspend fun deleteCategory(category: TierCategory) {
        return categoryDao.deleteCategory(category)
    }

    override fun getCategoriesWithElementsStream(): Flow<List<TierCategoryWithElements>> {
        return categoryDao.getCategoriesWithElementsStream()
    }
}