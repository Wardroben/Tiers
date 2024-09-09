package ru.smalljinn.tiers.data.share.models

import kotlinx.serialization.Serializable

@Serializable
data class ShareCategory(
    val name: String,
    val colorArgb: Int,
    val position: Int,
    val elements: List<ShareElement> = emptyList()
)
