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
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepository
import ru.smalljinn.tiers.data.preferences.model.UserSettings
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.domain.usecase.DeleteElementsUseCase
import ru.smalljinn.tiers.presentation.navigation.EDIT_TIER_NAV_ARGUMENT
import ru.smalljinn.tiers.util.EventHandler
import ru.smalljinn.tiers.util.Result
import ru.smalljinn.tiers.util.network.observer.ConnectivityObserver

private const val TIER_LIST_UNTITLED_NAME = "Untitled"

sealed class SheetAction {
    data object Init : SheetAction()
    data object Hide : SheetAction()
    data class EditCategory(val category: TierCategory) : SheetAction()
    data class CreateCategory(val newCategory: TierCategory) : SheetAction()
    data object SearchImages : SheetAction()
}


sealed class SheetState {
    data object Hidden : SheetState()
    data class CategoryCreation(val newCategory: TierCategory) : SheetState()
    data class CategoryEditing(val category: TierCategory) : SheetState()
    data class SearchImages(
        val query: String,
        val images: List<String>,
        val loading: Boolean,
        val errorMessage: String? = null
    ) :
        SheetState()
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

@Immutable
data class ImageSearchState(
    val searchQuery: String = "",
    val images: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

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
    private val savedStateHandle: SavedStateHandle,
    private val networkImageRepository: NetworkImageRepository,
    private val preferencesRepository: PreferencesRepository,
    connectivityObserver: ConnectivityObserver
) : ViewModel(), EventHandler<EditEvent> {
    private val currentListId: Long = savedStateHandle.get<Long>(EDIT_TIER_NAV_ARGUMENT)
        ?: throw IllegalArgumentException("Bad navigation argument")

    private val notAttachedElements =
        elementRepository.getNotAttachedElementsOfListStream(currentListId)
    private val tierListNameStream = listRepository.getTierListNameStream(currentListId)
    private val categoriesWithElementsStream =
        categoryRepository.getCategoriesWithElementsOfListStream(currentListId)

    private val sheetAction = MutableStateFlow<SheetAction>(SheetAction.Init)
    private val searchStateStream = MutableStateFlow(ImageSearchState())

    val connectionStatusStream = connectivityObserver.observe()

    val settingsStream = preferencesRepository.getSettingsStream()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000L),
            initialValue = UserSettings()
        )

    private val sheetUiState = combine(sheetAction, searchStateStream) { action, state ->
        when (action) {
            is SheetAction.EditCategory -> SheetState.CategoryEditing(action.category)
            is SheetAction.CreateCategory -> SheetState.CategoryCreation(action.newCategory)
            is SheetAction.SearchImages -> SheetState.SearchImages(
                query = state.searchQuery,
                images = state.images,
                loading = state.isLoading,
                errorMessage = state.errorMessage
            )

            else -> SheetState.Hidden
        }
    }

    private val photoProcessing = devicePhotoRepository.imageProcessingStream

    val uiState: StateFlow<EditUiState> =
        combine(
            categoriesWithElementsStream,
            notAttachedElements,
            photoProcessing,
            sheetUiState,
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
            EditEvent.CreateNewCategory -> sheetAction.update {
                SheetAction.CreateCategory(
                    TierCategory.getCategoryToCreate(
                        tierId = currentListId,
                        position = uiState.value.lastCategoryIndex + 1
                    )
                )
            }

            is EditEvent.RemoveCategory -> {
                sheetAction.update { SheetAction.Hide }
                val tierCategoryWithElements =
                    uiState.value.categoriesWithElements.find { categoryWithElements ->
                        event.tierCategory == categoryWithElements.category
                    } ?: return
                viewModelScope.launch {
                    elementRepository.insertTierElements(tierCategoryWithElements.elements.unpinElements())
                    categoryRepository.deleteCategory(event.tierCategory)
                }
                sheetAction.update { SheetAction.Hide }
            }

            is EditEvent.ChangeTierName -> {
                if (event.name.isBlank()) return
                viewModelScope.launch {
                    listRepository.insertTierList(TierList(id = currentListId, event.name))
                }
            }

            is EditEvent.EditCategory -> {
                sheetAction.update { SheetAction.Hide }
                //sheetState.update { SheetState.Hidden }
                viewModelScope.launch {
                    categoryRepository.insertCategory(event.tierCategory)
                }
            }


            is EditEvent.RemoveElement -> viewModelScope.launch {
                val elementToDelete = elementRepository.getElementById(event.elementId)
                deleteElementsUseCase(elementToDelete)
                //elementRepository.deleteTierElement(event.tierElement)
            }

            is EditEvent.UnpinElementFromCategory -> {
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
                sheetAction.update { SheetAction.EditCategory(event.tierCategory) }
                //sheetState.update { SheetState.CategoryEditing(event.tierCategory) }
            }

            is EditEvent.HideSheet -> sheetAction.update { SheetAction.Hide }

            is EditEvent.ReorderElements -> {
                viewModelScope.launch {
                    elementRepository.reorderElements(
                        draggedElementId = event.firstId,
                        targetElementId = event.secondId
                    )
                }
            }

            is EditEvent.SearchEdited -> {
                searchStateStream.update { it.copy(searchQuery = event.query) }
                //searchQuery = event.query
            }

            is EditEvent.SaveInternetImage -> viewModelScope.launch {
                val resultStream = networkImageRepository.compressAndSaveImage(event.bitmap)
                resultStream.collect { result ->
                    when (result) {
                        is Result.Error -> searchStateStream.update { it.copy(errorMessage = result.message) }
                        is Result.Loading -> searchStateStream.update { it.copy(isLoading = result.isLoading) }//imagesLoading.update { result.isLoading }
                        is Result.Success -> {
                            searchStateStream.update { it.copy(errorMessage = null) }
                            elementRepository.insertTierElement(
                                TierElement(
                                    tierListId = currentListId,
                                    imageUrl = result.data.toString()
                                )
                            )
                            val imageToRemove =
                                searchStateStream.value.images.elementAt(event.index)
                            searchStateStream.update { state ->
                                state.copy(
                                    images = state.images.minus(
                                        imageToRemove
                                    )
                                )
                            }
                            //imagesFromSearch.update { it.minus(imageToRemove) }
                        }
                    }
                }
            }

            is EditEvent.SearchImages -> {
                if (event.query.isBlank() || event.query.length < 2) return
                viewModelScope.launch {
                    val imagesStream = networkImageRepository.getNetworkImagesList(event.query)
                    imagesStream.collect { result ->
                        when (result) {
                            is Result.Error -> searchStateStream.update { it.copy(errorMessage = result.message) }
                            is Result.Loading -> searchStateStream.update { it.copy(isLoading = result.isLoading) }//imagesLoading.update { result.isLoading }
                            is Result.Success -> searchStateStream.update { state ->
                                state.copy(images = result.data?.map { it.thumbnailLink }
                                    ?: emptyList(), errorMessage = null)
                            }
                            /*imagesFromSearch.update {
                            result.data?.map { it.thumbnailLink } ?: emptyList()
                        }*/
                        }
                    }
                }
            }

            is EditEvent.OpenSearchSheet -> {
                sheetAction.update { SheetAction.SearchImages }
                //sheetState.update { SheetState.SearchImages("", emptyList()) }
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
                val networkImageRepository = appContainer.networkImageRepository
                TierEditViewModel(
                    categoryRepository = categoryRepo,
                    listRepository = listRepo,
                    elementRepository = elementRepo,
                    devicePhotoRepository = appContainer.devicePhotoRepository,
                    deleteElementsUseCase = deleteElementsUseCase,
                    networkImageRepository = networkImageRepository,
                    preferencesRepository = appContainer.preferencesRepository,
                    connectivityObserver = appContainer.connectivityObserver,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}