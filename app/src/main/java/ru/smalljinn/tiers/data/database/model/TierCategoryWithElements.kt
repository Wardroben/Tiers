package ru.smalljinn.tiers.data.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class TierCategoryWithElements(
    @Embedded val category: TierCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val elements: List<TierElement>
)