package ru.smalljinn.tiers.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation

@Immutable
data class TierListWithCategories(
    @Embedded val list: TierList,
    @Relation(
        parentColumn = "id",
        entityColumn = "tierListId"
    )
    val categories: List<TierCategory>
)
