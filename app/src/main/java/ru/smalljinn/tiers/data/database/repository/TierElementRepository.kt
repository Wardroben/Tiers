package ru.smalljinn.tiers.data.database.repository

import ru.smalljinn.tiers.data.database.model.TierElement

interface TierElementRepository {
    suspend fun insertTierElement(tierElement: TierElement)
    suspend fun deleteTierElement(tierElement: TierElement)
}