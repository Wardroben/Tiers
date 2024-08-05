package ru.smalljinn.tiers.viewmodel

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.data.database.model.TierListWithCategoriesAndElements
import ru.smalljinn.tiers.data.database.repository.TierListRepository

class MockTiersListRepository : TierListRepository {
    private val tiers = mutableStateListOf<TierList>()
    private val flow = MutableSharedFlow<List<TierList>>()

    override fun getAllTierListsStream(): Flow<List<TierList>> {
        return flow
    }

    override fun getTierListWithCategoriesAndElementsStream(listId: Long): Flow<TierListWithCategoriesAndElements> {
        TODO("Not yet implemented")
    }

    override fun getAllListsWithCategoriesStream(): Flow<List<TierListWithCategories>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTierListById(id: Long): TierList {
        return tiers.find { it.id == id } ?: throw Exception("No list finded")
    }

    override suspend fun deleteTierList(tierList: TierList) {
        tiers.remove(tierList)
    }

    override suspend fun insertTierList(tierList: TierList): Long {
        flow.emit(listOf(tierList))
        /*val lastElementId = tiers.size.toLong()
        tiers.add(tierList.copy(id = lastElementId))*/
        return 0
    }

    override suspend fun changeTierListName(tierList: TierList, newName: String) {
        val index = tiers.indexOf(tierList)
        tiers[index] = tierList.copy(name = newName)
    }
}