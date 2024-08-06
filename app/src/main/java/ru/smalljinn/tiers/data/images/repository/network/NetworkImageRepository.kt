package ru.smalljinn.tiers.data.images.repository.network

import ru.smalljinn.tiers.data.images.model.Image

/**
 * @property getNetworkImagesList photos from google photo by query
 */
interface NetworkImageRepository {
    suspend fun getNetworkImagesList(query: String): List<Image>

}