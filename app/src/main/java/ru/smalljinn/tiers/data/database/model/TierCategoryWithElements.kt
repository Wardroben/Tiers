package ru.smalljinn.tiers.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation

@Immutable
data class TierCategoryWithElements(
    @Embedded val category: TierCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val elements: List<TierElement>
) {
    fun getSortedByPositionElements() = elements.sortedBy { it.position }
}