package ru.smalljinn.tiers.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class TierListWithCategoriesAndElements(
    @Embedded val tierList: TierList,
    @Relation(
        entity = TierCategory::class,
        parentColumn = "id",
        entityColumn = "tierListId"
    )
    val categories: List<TierCategoryWithElements>
)