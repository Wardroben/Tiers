package ru.smalljinn.tiers.data.images.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val displayLink: String,
    val fileFormat: String,
    val htmlSnippet: String,
    val htmlTitle: String,
    val image: Image,
    val kind: String,
    val link: String,
    val mime: String,
    val snippet: String,
    val title: String
)