package ru.smalljinn.tiers.features.tier_edit

import androidx.compose.runtime.Immutable

@Immutable
data class ImageSearchState(
    val searchQuery: String = "",
    val images: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)