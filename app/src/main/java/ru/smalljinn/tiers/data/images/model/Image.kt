package ru.smalljinn.tiers.data.images.model

data class Image(
    val byteSize: Int,
    val contextLink: String,
    val height: Int,
    val thumbnailHeight: Int,
    val thumbnailLink: String,
    val thumbnailWidth: Int,
    val width: Int
)