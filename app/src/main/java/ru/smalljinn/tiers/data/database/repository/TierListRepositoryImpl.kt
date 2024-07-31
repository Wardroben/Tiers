package ru.smalljinn.tiers.data.database.repository

import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.dao.TierDao
import ru.smalljinn.tiers.data.database.model.TierList

class TierListRepositoryImpl(private val tierDao: TierDao) : TierListRepository {
    override fun getAllTierListsStream(): Flow<List<TierList>> {
        return tierDao.getTierListsStream()
    }

    override suspend fun getTierListById(id: Long): TierList {
        return tierDao.getTierListById(id)
    }

    override suspend fun deleteTierList(tierList: TierList) {
        return tierDao.deleteTierList(tierList)
    }

    override suspend fun insertTierList(tierList: TierList): Long {
        return tierDao.insertTierList(tierList)
    }
}