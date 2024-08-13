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
        entityColumn = "tierListId"
    )
    val categories: List<TierCategoryWithElements>
) {
    fun getSortedCategoriesByPosition() = categories.sortedBy { it.category.position }
}