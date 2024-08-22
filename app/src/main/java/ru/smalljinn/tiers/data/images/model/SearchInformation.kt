package ru.smalljinn.tiers.data.images.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchInformation(
    val formattedSearchTime: String,
    val formattedTotalResults: String,
    val searchTime: Double,
    val totalResults: String
)