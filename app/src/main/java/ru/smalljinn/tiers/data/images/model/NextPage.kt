package ru.smalljinn.tiers.data.images.model

import kotlinx.serialization.Serializable

@Serializable
data class NextPage(
    val count: Int,
    val cx: String,
    val inputEncoding: String,
    val outputEncoding: String,
    val safe: String,
    val searchTerms: String,
    val searchType: String,
    val startIndex: Int,
    val title: String,
    val totalResults: String
)