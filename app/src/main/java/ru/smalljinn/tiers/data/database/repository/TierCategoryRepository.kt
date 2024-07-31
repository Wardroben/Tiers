package ru.smalljinn.tiers.data.database.repository

import ru.smalljinn.tiers.data.database.model.TierCategory

interface TierCategoryRepository {
    suspend fun insertCategory(tierCategory: TierCategory): Long
    suspend fun deleteCategory(tierCategory: TierCategory)
}