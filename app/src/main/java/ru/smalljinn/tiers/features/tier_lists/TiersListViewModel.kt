package ru.smalljinn.tiers.features.tier_lists

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.util.EventHandler
import ru.smalljinn.tiers.util.Result
import javax.inject.Inject

@HiltViewModel
class TiersListViewModel @Inject constructor(
    private val createNewTierListUseCase: CreateNewTierListUseCase,
    private val deleteTierListUseCase: DeleteTierListUseCase,
    private val exportShareListUseCase: ExportShareListUseCase,
    getTiersWithCategoriesUseCase: GetTiersWithCategoriesUseCase
) : ViewModel(), EventHandler<TiersEvent> {

    var searchQuery by mutableStateOf("")
        private set

    private val tiersFlow =
        getTiersWithCategoriesUseCase()

    val uiState = combine(tiersFlow, snapshotFlow { searchQuery }) { result, search ->
        when (result) {
            is Result.Error -> TiersState.Error(result.message)
            is Result.Loading -> TiersState.Loading
            is Result.Success -> {
                with(result) {
                    if (data.isNullOrEmpty()) TiersState.Empty
                    else {
                        val foundLists = searchLists(data, search)
                        TiersState.Success(
                            tiersList = foundLists,
                            searchEnabled = shouldEnableSearch(
                                listsCount = data.size,
                                searchQuery = search
                            )
                        )
                    }
                }
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily, //to prevent loading when user returns from other screens
        TiersState.Loading
    )

    private val eventChannel = Channel<ActionEvent>()
    val eventsFlow = eventChannel.receiveAsFlow()

    override fun obtainEvent(event: TiersEvent) {
        when (event) {
            is TiersEvent.Delete -> viewModelScope.launch { deleteTierListUseCase(event.tierList) }

            TiersEvent.CreateNew -> viewModelScope.launch {
                if (uiState.value is TiersState.Success &&
                    (uiState.value as TiersState.Success).searchEnabled
                ) createNewTierListUseCase(name = searchQuery)
                else createNewTierListUseCase()
            }

            is TiersEvent.Search -> searchQuery = event.query

            TiersEvent.ClearSearch -> searchQuery = ""

            is TiersEvent.ShareList -> viewModelScope.launch {
                val intent = exportShareListUseCase.invoke(event.listId)
                eventChannel.send(ActionEvent.StartIntent(intent))
            }
        }
    }

    private fun searchLists(
        lists: List<TierListWithCategories>,
        query: String
    ): List<TierListWithCategories> {
        val queryLower = query.lowercase()
        return if (query.isBlank()) lists
        else lists.filter { tier -> tier.list.name.lowercase().contains(queryLower) }
    }

    private fun shouldEnableSearch(listsCount: Int, searchQuery: String): Boolean =
        listsCount > 1 || searchQuery.isNotBlank()

}