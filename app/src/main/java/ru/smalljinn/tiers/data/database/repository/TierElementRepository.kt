package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierElement

interface TierElementRepository {
    suspend fun getElementById(elementId: Long): TierElement
    suspend fun insertTierElements(tierElements: List<TierElement>): List<Long>
    suspend fun insertTierElement(tierElement: TierElement): Long
    suspend fun deleteTierElement(tierElement: TierElement)
    suspend fun deleteTierElements(tierElements: List<TierElement>)
    fun getNotAttachedElementsOfListStream(tierListId: Long): Flow<List<TierElement>>
    suspend fun getListElements(listId: Long): List<TierElement>
    suspend fun reorderElements(draggedElementId: Long, targetElementId: Long)
}