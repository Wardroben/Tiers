package ru.smalljinn.tiers.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.TierApp
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.domain.usecase.ImportListUseCase
import ru.smalljinn.tiers.util.EventHandler

sealed class ImportEvent {
    data class Import(val shareList: ShareList) : ImportEvent()
}

class ImportViewModel(
    private val importListUseCase: ImportListUseCase
) : ViewModel(), EventHandler<ImportEvent> {
    override fun obtainEvent(event: ImportEvent) {
        when (event) {
            is ImportEvent.Import -> viewModelScope.launch {
                importListUseCase.invoke(event.shareList)
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val appContainer = (this[APPLICATION_KEY] as TierApp).appContainer
                ImportViewModel(importListUseCase = appContainer.importListUseCase)
            }
        }
    }
}