package ru.smalljinn.tiers.viewmodel

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.repository.TierElementRepository

class MockElementRepository : TierElementRepository {
    override suspend fun getElementById(elementId: Long): TierElement {
        TODO("Not yet implemented")
    }

    override suspend fun insertTierElements(tierElements: List<TierElement>): List<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun insertTierElement(tierElement: TierElement): Long {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTierElement(tierElement: TierElement) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTierElements(tierElements: List<TierElement>) {
        TODO("Not yet implemented")
    }

    override fun getNotAttachedElementsOfListStream(tierListId: Long): Flow<List<TierElement>> {
        TODO("Not yet implemented")
    }

    override suspend fun getListElements(listId: Long): List<TierElement> {
        TODO("Not yet implemented")
    }

    override suspend fun reorderElements(draggedElementId: Long, targetElementId: Long) {
        TODO("Not yet implemented")
    }
}