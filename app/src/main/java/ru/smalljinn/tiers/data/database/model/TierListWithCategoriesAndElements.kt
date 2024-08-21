package ru.smalljinn.tiers.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation

@Immutable
data class TierListWithCategoriesAndElements(
    @Embedded val tierList: TierList,
    @Relation(
        entity = TierCategory::class,
        parentColumn = "id",
        entityColumn = "tier_list_id"
    )
    val categories: List<TierCategoryWithElements>
)