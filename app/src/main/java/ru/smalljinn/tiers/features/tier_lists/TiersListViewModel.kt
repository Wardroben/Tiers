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
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.util.EventHandler
import javax.inject.Inject

@HiltViewModel
class TiersListViewModel @Inject constructor(
    tierListRepository: TierListRepository,
    private val createNewTierListUseCase: CreateNewTierListUseCase,
    private val deleteTierListUseCase: DeleteTierListUseCase,
    private val exportShareListUseCase: ExportShareListUseCase
) : ViewModel(), EventHandler<TiersEvent> {

    var searchQuery by mutableStateOf("")
        private set

    private val tiersFlow = tierListRepository.getAllListsWithCategoriesStream()

    val uiState = combine(tiersFlow, snapshotFlow { searchQuery }) { tiers, search ->
        if (tiers.isEmpty()) TiersState.Empty
        else {
            val foundLists = searchLists(tiers, search)
            TiersState.Success(
                tiersList = foundLists,
                searchEnabled = shouldEnableSearch(listsCount = tiers.size, searchQuery = search)
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        TiersState.Loading
    )

    private val eventChannel = Channel<ActionEvent>()
    val eventsFlow = eventChannel.receiveAsFlow()

    override fun obtainEvent(event: TiersEvent) {
        when (event) {
            is TiersEvent.Delete -> viewModelScope.launch {
                deleteTierListUseCase(event.tierList)
            }

            is TiersEvent.CreateNew -> viewModelScope.launch {
                createNewTierListUseCase(event.name)
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