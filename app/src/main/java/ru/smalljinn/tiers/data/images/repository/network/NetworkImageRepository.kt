package ru.smalljinn.tiers.data.images.repository.network

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.images.model.Image
import ru.smalljinn.tiers.util.Result

/**
 * @property getNetworkImagesList photos from google photo by query
 */
interface NetworkImageRepository {
    suspend fun getNetworkImagesList(query: String): Flow<Result<List<Image>>>
    suspend fun compressAndSaveImage(bitmap: Bitmap): Flow<Result<Uri>>
}