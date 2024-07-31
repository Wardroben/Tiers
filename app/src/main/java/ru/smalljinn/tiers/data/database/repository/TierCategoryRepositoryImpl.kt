package ru.smalljinn.tiers.data.database.repository

import ru.smalljinn.tiers.data.database.dao.TierDao
import ru.smalljinn.tiers.data.database.model.TierCategory

class TierCategoryRepositoryImpl(private val tierDao: TierDao) : TierCategoryRepository {
    override suspend fun insertCategory(tierCategory: TierCategory): Long {
        return tierDao.insertTierCategory(tierCategory)
    }

    override suspend fun deleteCategory(tierCategory: TierCategory) {
        return tierDao.deleteTierCategory(tierCategory)
    }
}