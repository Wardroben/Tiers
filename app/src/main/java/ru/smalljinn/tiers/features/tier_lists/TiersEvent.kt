package ru.smalljinn.tiers.features.tier_lists

import ru.smalljinn.tiers.data.database.model.TierList

sealed class TiersEvent {
    data class Delete(val tierList: TierList) : TiersEvent()
    data object CreateNew : TiersEvent()
    data class Search(val query: String) : TiersEvent()
    data object ClearSearch : TiersEvent()
    data class ShareList(val listId: Long) : TiersEvent()
}