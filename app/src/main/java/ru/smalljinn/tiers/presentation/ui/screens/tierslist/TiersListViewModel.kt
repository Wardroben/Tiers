package ru.smalljinn.tiers.presentation.ui.screens.tierslist

import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.TierApp
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.data.database.repository.TierListRepository
import ru.smalljinn.tiers.domain.usecase.CreateNewTierListUseCase
import ru.smalljinn.tiers.domain.usecase.DeleteTierListUseCase
import ru.smalljinn.tiers.domain.usecase.ExportShareListUseCase
import ru.smalljinn.tiers.util.EventHandler

sealed class TiersState {
    data object Loading : TiersState()
    data object Empty : TiersState()

    @Immutable
    data class Success(val tiersList: List<TierListWithCategories>) : TiersState()
}
sealed class ListEvent {
    data class StartIntent(val intent: Intent) : ListEvent()
}

class TiersListViewModel(
    private val tierListRepository: TierListRepository,
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
            TiersState.Success(foundLists)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000L),
        TiersState.Loading
    )

    private val eventChannel = Channel<ListEvent>()
    val eventsFlow = eventChannel.receiveAsFlow()

    override fun obtainEvent(event: TiersEvent) {
        when (event) {
            is TiersEvent.ChangeName -> viewModelScope.launch {
                tierListRepository.changeTierListName(event.tierList, event.newName)
            }

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
                eventChannel.send(ListEvent.StartIntent(intent))
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = (this[APPLICATION_KEY] as TierApp).appContainer
                val repository: TierListRepository = app.tierListRepository
                TiersListViewModel(
                    repository,
                    app.createNewTierListUseCase,
                    app.deleteTierListUseCase,
                    app.exportShareListUseCase
                )
            }
        }
    }
}