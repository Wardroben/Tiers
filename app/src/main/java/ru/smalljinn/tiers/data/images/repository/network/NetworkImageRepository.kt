package ru.smalljinn.tiers.data.images.repository.network

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.images.model.Image

/**
 * @property getNetworkImagesList photos from google photo by query
 */
interface NetworkImageRepository {
    val working: Flow<Boolean>
    suspend fun getNetworkImagesList(query: String): List<Image>
    suspend fun compressAndSaveImage(bitmap: Bitmap): Uri
}