package ru.smalljinn.tiers.features.tier_edit

import ru.smalljinn.tiers.data.database.model.TierCategory

sealed class SheetState {
    data object Hidden : SheetState()
    data class CategoryCreation(val newCategory: TierCategory) : SheetState()
    data class CategoryEditing(val category: TierCategory) : SheetState()
    data class SearchImages(
        val query: String,
        val images: List<String>,
        val loading: Boolean,
        val errorMessage: String? = null
    ) : SheetState()
}