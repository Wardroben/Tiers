package ru.smalljinn.tiers.presentation.ui.screens.edit

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierListWithCategoriesAndElements
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.util.EventHandler

const val TIER_LIST_NAVIGATION_KEY = "TIER_LIST_NAVIGATION_KEY"
const val TIER_LIST_UNTITLED_NAME = "Untitled"

@Immutable
data class EditUiState(
    val notAttachedElements: List<TierElement> = emptyList(),
    val listWithCategoriesAndElements: TierListWithCategoriesAndElements? = null,
    val tierListName: String = TIER_LIST_UNTITLED_NAME
)

//categories list
//elements list
class TierEditViewModel(
    private val tierCategoryRepository: TierCategoryRepository,
    private val tierElementRepository: TierElementRepository,
    private val tierListRepository: TierListRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), EventHandler<EditEvent> {
    private val currentListId: Long = savedStateHandle[TIER_LIST_NAVIGATION_KEY] ?: 0L
    private val tierWithCategories =
        tierListRepository.getTierListWithCategoriesAndElementsStream(currentListId)

    //private val categoriesWithElements = tierCategoryRepository.getCategoriesWithElementsStream()
    private val notAttachedElements =
        tierElementRepository.getNotAttachedElementsOfListStream(currentListId)

    val uiState: StateFlow<EditUiState> =
        combine(tierWithCategories, notAttachedElements) { listWithCategories, elements ->
            EditUiState(
                notAttachedElements = elements,
                listWithCategoriesAndElements = listWithCategories
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            EditUiState()
        )

    override fun obtainEvent(event: EditEvent) {
        when (event) {
            is EditEvent.AddCategory -> viewModelScope.launch {
                tierCategoryRepository.insertCategory(event.tierCategory)
            }

            is EditEvent.RemoveCategory -> viewModelScope.launch {
                val tierCategoryWithElements =
                    uiState.value.listWithCategoriesAndElements?.categories?.find { categoryWithElements ->
                        event.tierCategory == categoryWithElements.category
                    } ?: return@launch

                tierCategoryWithElements.elements.forEach { element ->
                    tierElementRepository.insertTierElement(element.copy(categoryId = null))
                }
                tierCategoryRepository.deleteCategory(event.tierCategory)
                //TODO("Find elements with this tierCategory id and set null value")
            }

            is EditEvent.ChangeTierName -> viewModelScope.launch {
                tierListRepository.insertTierList(
                    uiState.value.listWithCategoriesAndElements?.tierList?.copy(
                        name = event.name
                    ) ?: return@launch
                )
            }

            is EditEvent.EditCategory -> viewModelScope.launch {
                tierCategoryRepository.insertCategory(event.tierCategory)
            }


            is EditEvent.RemoveElement -> viewModelScope.launch {
                tierElementRepository.deleteTierElement(event.tierElement)
            }

            is EditEvent.UnattachElementFromCategory -> viewModelScope.launch {
                tierElementRepository.insertTierElement(event.tierElement.copy(categoryId = null))
            }

            is EditEvent.AttachElementToCategory -> viewModelScope.launch {
                with(event) {
                    tierElementRepository.insertTierElement(tierElement.copy(categoryId = categoryId))
                }
            }
        }
    }
}