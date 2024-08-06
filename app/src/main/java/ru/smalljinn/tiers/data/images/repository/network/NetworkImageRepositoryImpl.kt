package ru.smalljinn.tiers.data.images.repository.network

import ru.smalljinn.tiers.data.images.model.Image
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi

class NetworkImageRepositoryImpl(private val googleSearchApi: GoogleSearchApi) :
    NetworkImageRepository {
    override suspend fun getNetworkImagesList(query: String): List<Image> {
        val imagesFromQuery = googleSearchApi.getImages(query).items.map { item -> item.image }
        return imagesFromQuery
    }
}