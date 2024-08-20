package ru.smalljinn.tiers.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation

@Immutable
data class TierListWithCategories(
    @Embedded val list: TierList,
    @Relation(
        parentColumn = "id",
        entityColumn = "tier_list_id"
    )
    val categories: List<TierCategory>
)
