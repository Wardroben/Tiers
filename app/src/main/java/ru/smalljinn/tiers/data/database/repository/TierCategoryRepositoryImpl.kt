package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.smalljinn.tiers.data.database.dao.CategoryDao
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.model.getWithSortedElements
import javax.inject.Inject


class TierCategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : TierCategoryRepository {
    override suspend fun insertCategory(category: TierCategory): Long {
        return categoryDao.insertCategory(category)
    }

    override suspend fun insertCategories(categories: List<TierCategory>): List<Long> {
        return categoryDao.insertCategories(categories)
    }

    override suspend fun deleteCategory(category: TierCategory) {
        return categoryDao.deleteCategory(category)
    }

    override fun getCategoriesWithElementsStream(): Flow<List<TierCategoryWithElements>> {
        return categoryDao.getCategoriesWithElementsStream()
    }

    override fun getCategoriesWithElementsOfListStream(listId: Long): Flow<List<TierCategoryWithElements>> {
        return categoryDao.getCategoriesWithElementsOfListStream(listId)
            .map { categoryWithElementsList ->
                categoryWithElementsList.map { it.getWithSortedElements() }
            }
    }

    override suspend fun unpinElementsFromCategory(categoryId: Long) =
        categoryDao.unpinElementsOfCategory(categoryId)
}