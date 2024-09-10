package ru.smalljinn.tiers.data.images.repository.device

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor
import javax.inject.Inject

class DeviceImageRepositoryImpl @Inject constructor(
    private val photoProcessor: PhotoProcessor
) : DeviceImageRepository {
    override val imageProcessingStream: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override suspend fun insertPhotos(uris: List<Uri>): List<Uri> {
        imageProcessingStream.emit(true)
        val compressedImages = photoProcessor.compressAndSaveImages(uris)
        imageProcessingStream.emit(false)
        return compressedImages
    }

    override suspend fun deletePhotos(uris: List<Uri>): Boolean {
        imageProcessingStream.emit(true)
        val isPhotosDeleted = photoProcessor.deleteImagesFromDevice(uris)
        imageProcessingStream.emit(false)
        return isPhotosDeleted
    }
}