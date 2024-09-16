package ru.smalljinn.tiers.data.images.repository.network

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor
import ru.smalljinn.tiers.util.Result
import javax.inject.Inject

class NetworkImageRepositoryImpl @Inject constructor(
    private val photoProcessor: PhotoProcessor,
) : NetworkImageRepository {
    override suspend fun compressAndSaveImage(bitmap: Bitmap): Flow<Result<Uri>> {
        return flow {
            emit(Result.Loading(true))
            val compressedImage = photoProcessor.compressAndSaveImageFromInternet(bitmap)
            emit(Result.Loading(false))
            emit(Result.Success(compressedImage))
        }
    }
}