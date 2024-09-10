package ru.smalljinn.tiers.features.tier_edit

import androidx.compose.runtime.Immutable
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.model.TierElement

private const val TIER_LIST_UNTITLED_NAME = "Untitled"

@Immutable
data class EditUiState(
    val notAttachedElements: List<TierElement> = emptyList(),
    val categoriesWithElements: List<TierCategoryWithElements> = emptyList(),
    val tierListName: String = TIER_LIST_UNTITLED_NAME,
    val isPhotoProcessing: Boolean = false,
    val sheetState: SheetState = SheetState.Hidden,
    val lastCategoryIndex: Int = categoriesWithElements.lastIndex
)