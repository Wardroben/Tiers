package ru.smalljinn.tiers.data.images.model

import kotlinx.serialization.Serializable

@Serializable
data class Queries(
    val nextPage: List<NextPage>,
    val request: List<Request>
)