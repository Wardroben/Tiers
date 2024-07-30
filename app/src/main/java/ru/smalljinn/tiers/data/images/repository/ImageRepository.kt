package ru.smalljinn.tiers.data.images.repository

import ru.smalljinn.tiers.data.images.model.Image

interface ImageRepository {
    suspend fun getImagesList(query: String): List<Image>
}