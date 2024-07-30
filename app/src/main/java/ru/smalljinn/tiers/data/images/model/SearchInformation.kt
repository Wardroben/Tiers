package ru.smalljinn.tiers.data.images.model

data class SearchInformation(
    val formattedSearchTime: String,
    val formattedTotalResults: String,
    val searchTime: Double,
    val totalResults: String
)