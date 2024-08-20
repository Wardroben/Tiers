package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.data.database.model.TierListWithCategoriesAndElements

interface TierListRepository {
    fun getAllTierListsStream(): Flow<List<TierList>>
    fun getTierListWithCategoriesAndElementsStream(listId: Long): Flow<TierListWithCategoriesAndElements>
    fun getAllListsWithCategoriesStream(): Flow<List<TierListWithCategories>>
    fun getTierListNameStream(listId: Long): Flow<String>
    suspend fun getTierListById(id: Long): TierList
    suspend fun deleteTierList(tierList: TierList)
    suspend fun deleteTierListById(listId: Long)
    suspend fun insertTierList(tierList: TierList): Long
    suspend fun changeTierListName(tierList: TierList, newName: String)
}