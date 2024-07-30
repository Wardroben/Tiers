package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierList

interface TierListRepository {
    fun getAllTierListsStream(): Flow<List<TierList>>
    suspend fun getTierListById(id: Long): TierList
    suspend fun deleteTierList(tierList: TierList)
    suspend fun insertTierList(tierList: TierList)
}