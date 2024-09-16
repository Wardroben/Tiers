package ru.smalljinn.tiers.data.images.repository.network

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.util.Result

interface NetworkImageRepository {
    suspend fun compressAndSaveImage(bitmap: Bitmap): Flow<Result<Uri>>
}