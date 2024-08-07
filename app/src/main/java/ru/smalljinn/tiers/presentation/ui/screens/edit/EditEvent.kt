package ru.smalljinn.tiers.presentation.ui.screens.edit

import android.net.Uri
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierElement

sealed class EditEvent {
    data class RemoveCategory(val tierCategory: TierCategory) : EditEvent()
    data class AddCategory(val tierCategory: TierCategory) : EditEvent()
    data class EditCategory(val tierCategory: TierCategory) : EditEvent()
    data class ChangeTierName(val name: String) : EditEvent()
    data class RemoveElement(val tierElement: TierElement) : EditEvent()
    data class UnattachElementFromCategory(val tierElement: TierElement) : EditEvent()
    data class AttachElementToCategory(val tierElement: TierElement, val categoryId: Long) :
        EditEvent()
    data class SelectCategory(val tierCategory: TierCategory?) : EditEvent()

    data class AddImages(val images: List<Uri>) : EditEvent()
}
