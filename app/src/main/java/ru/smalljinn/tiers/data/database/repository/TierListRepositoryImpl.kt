package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.dao.TierListDao
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.data.database.model.TierListWithCategoriesAndElements

class TierListRepositoryImpl(private val tierListDao: TierListDao) : TierListRepository {
    override fun getAllTierListsStream(): Flow<List<TierList>> {
        return tierListDao.getTierListsStream()
    }

    override suspend fun getTierListById(id: Long): TierList {
        return tierListDao.getTierListById(id)
    }

    override suspend fun deleteTierList(tierList: TierList) {
        return tierListDao.deleteTierList(tierList)
    }

    override suspend fun deleteTierListById(listId: Long) {
        return tierListDao.deleteTierListById(listId)
    }

    override suspend fun insertTierList(tierList: TierList): Long {
        return tierListDao.insertTierList(tierList)
    }

    override suspend fun changeTierListName(tierList: TierList, newName: String) {
        if (tierList.name != newName) tierListDao.insertTierList(tierList.copy(name = newName))
    }

    override fun getTierListWithCategoriesAndElementsStream(listId: Long): Flow<TierListWithCategoriesAndElements> {
        return tierListDao.getTierListWithCategoriesAndElementsStream(listId)
    }

    override fun getAllListsWithCategoriesStream(): Flow<List<TierListWithCategories>> {
        return tierListDao.getAllListsWithCategoriesStream()
    }
}