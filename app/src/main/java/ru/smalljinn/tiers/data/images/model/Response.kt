package ru.smalljinn.tiers.data.images.model

import kotlinx.serialization.Serializable

@Serializable
data class Response(
    //val context: Context,
    val items: List<Item>,
    //val kind: String,
    //val queries: Queries,
    //val searchInformation: SearchInformation,
    //val url: Url
)