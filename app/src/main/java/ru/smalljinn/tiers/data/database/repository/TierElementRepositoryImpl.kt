package ru.smalljinn.tiers.data.database.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.dao.ElementDao
import ru.smalljinn.tiers.data.database.model.TierElement

private const val TAG = "ElementRepository"
class TierElementRepositoryImpl(private val elementDao: ElementDao) : TierElementRepository {
    override suspend fun getElementById(elementId: Long): TierElement {
        return elementDao.getElementById(elementId)
    }

    override suspend fun insertTierElements(tierElements: List<TierElement>): List<Long> {
        return elementDao.insertTierElements(tierElements)
    }

    override suspend fun insertTierElement(tierElement: TierElement): Long {
        Log.i(TAG, "inserted $tierElement")
        return elementDao.insertElement(tierElement)
    }

    override suspend fun deleteTierElement(tierElement: TierElement) {
        return elementDao.deleteElement(tierElement)
    }

    override suspend fun deleteTierElements(tierElements: List<TierElement>) {
        return elementDao.deleteElements(tierElements)
    }

    override fun getNotAttachedElementsOfListStream(tierListId: Long): Flow<List<TierElement>> {
        return elementDao.getUnassertedElementsStream(tierListId)
    }

    override suspend fun getListElements(listId: Long): List<TierElement> {
        return elementDao.getTierListElements(listId)
    }

    override suspend fun reorderElements(draggedElementId: Long, targetElementId: Long) {
        return elementDao.reorderElements(draggedElementId, targetElementId)
    }
}