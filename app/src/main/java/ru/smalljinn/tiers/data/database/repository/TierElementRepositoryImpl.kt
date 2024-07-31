package ru.smalljinn.tiers.data.database.repository

import ru.smalljinn.tiers.data.database.dao.TierDao
import ru.smalljinn.tiers.data.database.model.TierElement

class TierElementRepositoryImpl(private val tierDao: TierDao) : TierElementRepository {
    override suspend fun insertTierElement(tierElement: TierElement): Long {
        return tierDao.insertTierElement(tierElement)
    }

    override suspend fun deleteTierElement(tierElement: TierElement) {
        return tierDao.deleteTierElement(tierElement)
    }
}