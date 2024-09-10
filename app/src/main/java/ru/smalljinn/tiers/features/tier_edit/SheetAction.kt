package ru.smalljinn.tiers.features.tier_edit

import ru.smalljinn.tiers.data.database.model.TierCategory

sealed class SheetAction {
    data object Init : SheetAction()
    data object Hide : SheetAction()
    data class EditCategory(val category: TierCategory) : SheetAction()
    data class CreateCategory(val newCategory: TierCategory) : SheetAction()
    data object SearchImages : SheetAction()
}