package ru.smalljinn.tiers.presentation.ui.screens.edit

import android.net.Uri
import ru.smalljinn.tiers.data.database.model.TierCategory

sealed class EditEvent {
    data class RemoveCategory(val tierCategory: TierCategory) : EditEvent()
    data object CreateNewCategory : EditEvent()
    data class EditCategory(val tierCategory: TierCategory) : EditEvent()
    data class ChangeTierName(val name: String) : EditEvent()
    data class RemoveElement(val elementId: Long) : EditEvent()
    data class UnattachElementFromCategory(val elementId: Long) : EditEvent()
    data class AttachElementToCategory(val categoryId: Long, val elementId: Long) :
        EditEvent()

    data class SelectCategory(val tierCategory: TierCategory) : EditEvent()
    data object HideSheet : EditEvent()
    data class AddImages(val images: List<Uri>) : EditEvent()
    data class ReorderElements(val firstId: Long, val secondId: Long) : EditEvent()
}
