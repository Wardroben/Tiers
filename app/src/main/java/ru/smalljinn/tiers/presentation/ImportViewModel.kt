package ru.smalljinn.tiers.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.data.share.models.ShareList
import ru.smalljinn.tiers.domain.usecase.ImportListUseCase
import ru.smalljinn.tiers.util.EventHandler
import javax.inject.Inject

sealed class ImportEvent {
    data class Import(val shareList: ShareList) : ImportEvent()
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val importListUseCase: ImportListUseCase
) : ViewModel(), EventHandler<ImportEvent> {
    override fun obtainEvent(event: ImportEvent) {
        when (event) {
            is ImportEvent.Import -> viewModelScope.launch {
                val imported = importListUseCase.invoke(event.shareList)
            }
        }
    }
}