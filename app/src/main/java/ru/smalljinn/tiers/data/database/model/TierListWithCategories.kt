package ru.smalljinn.tiers.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class TierListWithCategories(
    @Embedded val list: TierList,
    @Relation(
        parentColumn = "id",
        entityColumn = "tierListId"
    )
    val categories: List<TierCategory>
)
