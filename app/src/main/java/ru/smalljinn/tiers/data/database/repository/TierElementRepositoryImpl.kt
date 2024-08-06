package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.dao.ElementDao
import ru.smalljinn.tiers.data.database.model.TierElement

class TierElementRepositoryImpl(private val elementDao: ElementDao) : TierElementRepository {
    override suspend fun insertTierElements(tierElements: List<TierElement>): List<Long> {
        return elementDao.insertTierElements(tierElements)
    }

    override suspend fun insertTierElement(tierElement: TierElement): Long {
        return elementDao.insertElement(tierElement)
    }

    override suspend fun deleteTierElement(tierElement: TierElement) {
        return elementDao.deleteElement(tierElement)
    }

    override fun getNotAttachedElementsOfListStream(tierListId: Long): Flow<List<TierElement>> {
        return elementDao.getUnassertedElementsStream(tierListId)
    }
}