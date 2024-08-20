package ru.smalljinn.tiers.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation

@Immutable
data class TierCategoryWithElements(
    @Embedded val category: TierCategory,
    @Relation(
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val elements: List<TierElement>
) {
    fun getSortedByPositionElements() = elements.sortedBy { it.position }
}

fun TierCategoryWithElements.getWithSortedElements() =
    TierCategoryWithElements(category, this.getSortedByPositionElements())