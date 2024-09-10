package ru.smalljinn.tiers.features.tier_lists

import androidx.compose.runtime.Immutable
import ru.smalljinn.tiers.data.database.model.TierListWithCategories

@Immutable
sealed class TiersState {
    data object Loading : TiersState()
    data object Empty : TiersState()
    data class Success(
        val tiersList: List<TierListWithCategories>,
        val searchEnabled: Boolean = false
    ) : TiersState()
}