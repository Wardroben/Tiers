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
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.unpin
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.images.repository.device.DevicePhotoRepository
import ru.smalljinn.tiers.domain.usecase.DeleteElementsUseCase
import ru.smalljinn.tiers.presentation.navigation.EDIT_TIER_NAV_ARGUMENT
import ru.smalljinn.tiers.util.EventHandler

private const val TIER_LIST_UNTITLED_NAME = "Untitled"

sealed class SheetState {
    data object Hidden : SheetState()
    data class CategoryCreation(val newCategory: TierCategory) : SheetState()
    data class CategoryEditing(val category: TierCategory) : SheetState()
}

@Immutable
data class EditUiState(
    val notAttachedElements: List<TierElement> = emptyList(),
    val categoriesWithElements: List<TierCategoryWithElements> = emptyList(),
    val tierListName: String = TIER_LIST_UNTITLED_NAME,
    val isPhotoProcessing: Boolean = false,
    val sheetState: SheetState = SheetState.Hidden
) {
    val lastCategoryIndex = categoriesWithElements.lastIndex
}

/**
 * Set categoryId to null for all elements to unpin them
 */
private fun List<TierElement>.unpinElements() =
    this.map { element -> element.copy(categoryId = null) }

class TierEditViewModel(
    private val devicePhotoRepository: DevicePhotoRepository,
    private val categoryRepository: TierCategoryRepository,
    private val elementRepository: TierElementRepository,
    private val listRepository: TierListRepository,
    private val deleteElementsUseCase: DeleteElementsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(), EventHandler<EditEvent> {
    private val currentListId: Long = savedStateHandle.get<Long>(EDIT_TIER_NAV_ARGUMENT)
        ?: throw IllegalArgumentException("Bad navigation argument")

    private val notAttachedElements =
        elementRepository.getNotAttachedElementsOfListStream(currentListId)
    private val tierListNameStream = listRepository.getTierListNameStream(currentListId)
    private val categoriesWithElementsStream =
        categoryRepository.getCategoriesWithElementsOfListStream(currentListId)

    private val sheetState = MutableStateFlow<SheetState>(SheetState.Hidden)

    private val photoProcessing = devicePhotoRepository.imageProcessingStream

    val uiState: StateFlow<EditUiState> =
        combine(
            categoriesWithElementsStream,
            notAttachedElements,
            photoProcessing,
            sheetState,
            tierListNameStream,
        ) { categoriesWithElements, elements, isPhotoProcessing, sheetState, listName ->
            EditUiState(
                notAttachedElements = elements,
                categoriesWithElements = categoriesWithElements,
                isPhotoProcessing = isPhotoProcessing,
                tierListName = listName,
                sheetState = sheetState,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            EditUiState()
        )

    override fun obtainEvent(event: EditEvent) {
        when (event) {
            EditEvent.CreateNewCategory -> sheetState.update {
                SheetState.CategoryEditing(
                    TierCategory.getCategoryToCreate(
                        tierId = currentListId,
                        position = uiState.value.lastCategoryIndex + 1
                    )
                )
            }

            is EditEvent.RemoveCategory -> {
                sheetState.update { SheetState.Hidden }
                val tierCategoryWithElements =
                    uiState.value.categoriesWithElements.find { categoryWithElements ->
                        event.tierCategory == categoryWithElements.category
                    } ?: return
                viewModelScope.launch {
                    elementRepository.insertTierElements(tierCategoryWithElements.elements.unpinElements())
                    categoryRepository.deleteCategory(event.tierCategory)
                }
                sheetState.update { SheetState.Hidden }
            }

            is EditEvent.ChangeTierName -> {
                if (event.name.isBlank()) return
                viewModelScope.launch {
                    listRepository.insertTierList(TierList(id = currentListId, event.name))
                }
            }

            is EditEvent.EditCategory -> {
                sheetState.update { SheetState.Hidden }
                viewModelScope.launch {
                    categoryRepository.insertCategory(event.tierCategory)
                }
            }


            is EditEvent.RemoveElement -> viewModelScope.launch {
                val elementToDelete = elementRepository.getElementById(event.elementId)
                deleteElementsUseCase(elementToDelete)
                //elementRepository.deleteTierElement(event.tierElement)
            }

            is EditEvent.UnattachElementFromCategory -> {
                val elementInUnpinnedList =
                    uiState.value.notAttachedElements.find { element ->
                        element.elementId == event.elementId
                    }
                if (elementInUnpinnedList != null) return
                viewModelScope.launch {
                    val elementIdToUnpin = elementRepository.getElementById(event.elementId)
                    elementRepository.insertTierElement(elementIdToUnpin.unpin())
                }
            }

            is EditEvent.AttachElementToCategory -> {
                val category =
                    uiState.value.categoriesWithElements.find { it.category.id == event.categoryId }
                        ?: return
                val position = category.elements.lastOrNull()?.position?.let { it + 1 } ?: 0
                viewModelScope.launch {
                    val element = elementRepository.getElementById(event.elementId)
                    if (element in category.elements) return@launch
                    with(event) {
                        elementRepository.insertTierElement(
                            element.copy(
                                categoryId = categoryId,
                                position = position
                            )
                        )
                    }
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
                sheetState.update { SheetState.CategoryEditing(event.tierCategory) }
            }

            is EditEvent.HideSheet -> sheetState.update { SheetState.Hidden }

            is EditEvent.ReorderElements -> {
                //TODO make use case
                viewModelScope.launch {
                    val firstElement = elementRepository.getElementById(event.firstId)
                    val secondElement = elementRepository.getElementById(event.secondId)

                    val swappedElements = listOf(
                        firstElement.copy(position = secondElement.position),
                        secondElement.copy(position = firstElement.position)
                    )

                    elementRepository.insertTierElements(swappedElements)
                }

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
                val deleteElementsUseCase = appContainer.deleteElementsUseCase
                TierEditViewModel(
                    categoryRepository = categoryRepo,
                    listRepository = listRepo,
                    elementRepository = elementRepo,
                    devicePhotoRepository = appContainer.devicePhotoRepository,
                    deleteElementsUseCase = deleteElementsUseCase,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}