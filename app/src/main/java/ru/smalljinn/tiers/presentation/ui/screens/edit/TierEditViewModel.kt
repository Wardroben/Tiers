package ru.smalljinn.tiers.presentation.ui.screens.edit

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.TierApp
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierListWithCategoriesAndElements
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.images.repository.device.DevicePhotoRepository
import ru.smalljinn.tiers.presentation.navigation.EDIT_TIER_NAV_ARGUMENT
import ru.smalljinn.tiers.util.EventHandler

private const val TIER_LIST_UNTITLED_NAME = "Untitled"

@Immutable
data class EditUiState(
    val notAttachedElements: List<TierElement> = emptyList(),
    val listWithCategoriesAndElements: TierListWithCategoriesAndElements? = null,
    val tierListName: String = TIER_LIST_UNTITLED_NAME,
    val isPhotoProcessing: Boolean = false,
    val selectedCategory: TierCategory? = null
)

//categories list
//elements list
class TierEditViewModel(
    private val devicePhotoRepository: DevicePhotoRepository,
    private val categoryRepository: TierCategoryRepository,
    private val elementRepository: TierElementRepository,
    private val listRepository: TierListRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), EventHandler<EditEvent> {
    private val currentListId: Long = savedStateHandle.get<Long>(EDIT_TIER_NAV_ARGUMENT)
        ?: throw IllegalArgumentException("Bad navigation argument")
    private val fullTierList =
        listRepository.getTierListWithCategoriesAndElementsStream(currentListId)
    private val notAttachedElements =
        elementRepository.getNotAttachedElementsOfListStream(currentListId)
    private val photoProcessing = devicePhotoRepository.imageProcessingStream
    private val selectedCategory = MutableStateFlow<TierCategory?>(null)

    val uiState: StateFlow<EditUiState> =
        combine(
            fullTierList,
            notAttachedElements,
            photoProcessing,
            selectedCategory
        ) { listWithCategories, elements, isPhotoProcessing, selectedCategory ->
            EditUiState(
                notAttachedElements = elements,
                listWithCategoriesAndElements = listWithCategories,
                isPhotoProcessing = isPhotoProcessing,
                tierListName = listWithCategories.tierList.name,
                selectedCategory = selectedCategory
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            EditUiState()
        )

    override fun obtainEvent(event: EditEvent) {
        when (event) {
            is EditEvent.AddCategory -> viewModelScope.launch {
                categoryRepository.insertCategory(event.tierCategory)
            }

            is EditEvent.RemoveCategory -> viewModelScope.launch {
                val tierCategoryWithElements =
                    uiState.value.listWithCategoriesAndElements?.categories?.find { categoryWithElements ->
                        event.tierCategory == categoryWithElements.category
                    } ?: return@launch

                elementRepository.insertTierElements(tierCategoryWithElements.elements.map {
                    it.copy(
                        categoryId = null
                    )
                })
                /*tierCategoryWithElements.elements.forEach { element ->
                    elementRepository.insertTierElement(element.copy(categoryId = null))
                }*/
                categoryRepository.deleteCategory(event.tierCategory)

                selectedCategory.update { null }
                //TODO("Find elements with this tierCategory id and set null value")
            }

            is EditEvent.ChangeTierName -> viewModelScope.launch {
                listRepository.insertTierList(
                    uiState.value.listWithCategoriesAndElements?.tierList?.copy(
                        name = event.name
                    ) ?: return@launch
                )
            }

            is EditEvent.EditCategory -> viewModelScope.launch {
                categoryRepository.insertCategory(event.tierCategory)
            }


            is EditEvent.RemoveElement -> viewModelScope.launch {
                elementRepository.deleteTierElement(event.tierElement)
            }

            is EditEvent.UnattachElementFromCategory -> viewModelScope.launch {
                elementRepository.insertTierElement(event.tierElement.copy(categoryId = null))
            }

            is EditEvent.AttachElementToCategory -> viewModelScope.launch {
                with(event) {
                    elementRepository.insertTierElement(tierElement.copy(categoryId = categoryId))
                }
            }

            is EditEvent.AddImages -> viewModelScope.launch {
                val addedImageUris = devicePhotoRepository.insertPhotos(event.images)
                val tierElementsToAdd = addedImageUris.map { uri ->
                    TierElement(
                        tierListId = currentListId,
                        imageUrl = uri.toString()
                    )
                }
                elementRepository.insertTierElements(tierElementsToAdd)
            }

            is EditEvent.SelectCategory -> {
                selectedCategory.update { event.tierCategory }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val appContainer = (this[APPLICATION_KEY] as TierApp).appContainer
                val categoryRepo = appContainer.tierCategoryRepository
                val elementRepo = appContainer.tierElementRepository
                val listRepo = appContainer.tierListRepository
                TierEditViewModel(
                    categoryRepository = categoryRepo,
                    listRepository = listRepo,
                    elementRepository = elementRepo,
                    devicePhotoRepository = appContainer.devicePhotoRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}