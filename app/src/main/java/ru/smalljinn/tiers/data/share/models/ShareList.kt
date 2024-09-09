package ru.smalljinn.tiers.data.share.models

import kotlinx.serialization.Serializable

@Serializable
data class ShareList(
    val name: String,
    val categories: List<ShareCategory> = emptyList(),
    val unattachedElements: List<ShareElement> = emptyList()
)
