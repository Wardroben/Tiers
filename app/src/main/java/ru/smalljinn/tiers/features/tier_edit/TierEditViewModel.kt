package ru.smalljinn.tiers.features.tier_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.repository.TierCategoryRepository
import ru.smalljinn.tiers.data.database.repository.TierElementRepository
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.data.images.repository.device.DeviceImageRepository
import ru.smalljinn.tiers.data.images.repository.network.NetworkImageRepository
import ru.smalljinn.tiers.data.preferences.model.UserSettings
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.domain.usecase.DeleteElementsUseCase
import ru.smalljinn.tiers.features.tier_edit.usecase.InsertElementsUseCase
import ru.smalljinn.tiers.features.tier_edit.usecase.PinElementUseCase
import ru.smalljinn.tiers.features.tier_edit.usecase.RemoveCategoryUseCase
import ru.smalljinn.tiers.features.tier_edit.usecase.UnpinElementsUseCase
import ru.smalljinn.tiers.navigation.EDIT_TIER_NAV_ARGUMENT
import ru.smalljinn.tiers.util.EventHandler
import ru.smalljinn.tiers.util.Result
import ru.smalljinn.tiers.util.network.observer.ConnectivityObserver
import javax.inject.Inject

@HiltViewModel
class TierEditViewModel @Inject constructor(
    private val deviceImageRepository: DeviceImageRepository,
    private val categoryRepository: TierCategoryRepository,
    private val elementRepository: TierElementRepository,
    private val listRepository: TierListRepository,
    private val networkImageRepository: NetworkImageRepository,
    preferencesRepository: PreferencesRepository,
    private val deleteElementsUseCase: DeleteElementsUseCase,
    private val insertElementsUseCase: InsertElementsUseCase,
    private val unpinElementsUseCase: UnpinElementsUseCase,
    private val pinElementUseCase: PinElementUseCase,
    private val removeCategoryUseCase: RemoveCategoryUseCase,
    savedStateHandle: SavedStateHandle,
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

    private val photoProcessing = deviceImageRepository.imageProcessingStream

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
                viewModelScope.launch { removeCategoryUseCase(event.tierCategory) }
            }

            is EditEvent.ChangeTierName -> {
                if (event.name.isBlank()) return
                viewModelScope.launch {
                    listRepository.insertTierList(TierList(id = currentListId, event.name))
                }
            }

            is EditEvent.EditCategory -> {
                viewModelScope.launch {
                    categoryRepository.insertCategory(event.tierCategory)
                }
                sheetAction.update { SheetAction.Hide }
            }


            is EditEvent.RemoveElement -> viewModelScope.launch {
                val elementToDelete = elementRepository.getElementById(event.elementId)
                deleteElementsUseCase(elementToDelete)
            }

            is EditEvent.UnpinElementFromCategory -> {
                viewModelScope.launch {
                    unpinElementsUseCase(event.elementId)
                }
            }

            is EditEvent.AttachElementToCategory -> {
                viewModelScope.launch {
                    pinElementUseCase(categoryId = event.categoryId, elementId = event.elementId)
                }
            }

            is EditEvent.AddImages -> viewModelScope.launch {
                val addedImageUris = deviceImageRepository.insertPhotos(event.images)
                insertElementsUseCase(currentListId, addedImageUris)
            }

            is EditEvent.SelectCategory -> {
                sheetAction.update { SheetAction.EditCategory(event.tierCategory) }
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
            }

            is EditEvent.SaveInternetImage -> viewModelScope.launch {
                val resultStream = networkImageRepository.compressAndSaveImage(event.bitmap)
                resultStream.collect { result ->
                    when (result) {
                        is Result.Error -> searchStateStream.update { it.copy(errorMessage = result.message) }
                        is Result.Loading -> searchStateStream.update {
                            it.copy(
                                isLoading = result.isLoading,
                                errorMessage = null
                            )
                        }

                        is Result.Success -> {
                            //TODO result.data!!
                            insertElementsUseCase(currentListId, result.data!!)
                            val imageToRemove =
                                searchStateStream.value.images.elementAt(event.index)
                            searchStateStream.update { state ->
                                state.copy(
                                    images = state.images.minus(imageToRemove),
                                    errorMessage = null
                                )
                            }
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
                            is Result.Loading -> searchStateStream.update { it.copy(isLoading = result.isLoading) }
                            is Result.Success -> searchStateStream.update { searchState ->
                                searchState.copy(images = result.data?.map { it.thumbnailLink }
                                    ?: emptyList(), errorMessage = null)
                            }
                        }
                    }
                }
            }

            is EditEvent.OpenSearchSheet -> {
                sheetAction.update { SheetAction.SearchImages }
            }
        }
    }
}