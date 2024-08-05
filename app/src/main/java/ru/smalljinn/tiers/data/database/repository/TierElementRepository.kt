package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierElement

interface TierElementRepository {
    suspend fun insertTierElement(tierElement: TierElement): Long
    suspend fun deleteTierElement(tierElement: TierElement)
    fun getNotAttachedElementsOfListStream(tierListId: Long): Flow<List<TierElement>>
}