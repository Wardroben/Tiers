package ru.smalljinn.tiers.data.images.model

import kotlinx.serialization.Serializable

@Serializable
data class Url(
    val template: String,
    val type: String
)