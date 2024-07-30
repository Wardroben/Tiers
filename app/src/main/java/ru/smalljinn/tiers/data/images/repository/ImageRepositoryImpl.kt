package ru.smalljinn.tiers.data.images.repository

import ru.smalljinn.tiers.data.images.model.Image
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi

class ImageRepositoryImpl(private val googleSearchApi: GoogleSearchApi) : ImageRepository {
    override suspend fun getImagesList(query: String): List<Image> {
        val imagesFromQuery = googleSearchApi.getImages(query).items.map { item -> item.image }
        return imagesFromQuery
    }
}