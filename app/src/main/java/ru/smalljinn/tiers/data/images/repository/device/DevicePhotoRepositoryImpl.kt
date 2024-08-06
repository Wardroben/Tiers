package ru.smalljinn.tiers.data.images.repository.device

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow

class DevicePhotoRepositoryImpl(
    private val photoProcessor: PhotoProcessor
) : DevicePhotoRepository {
    override val imageProcessingStream: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override suspend fun insertPhotos(uris: List<Uri>): List<Uri> {
        imageProcessingStream.emit(true)
        //val uris = urls.map { url -> Uri.parse(url) }
        val compressedImages = photoProcessor.compressAndSaveImages(uris)
        imageProcessingStream.emit(false)
        return compressedImages
    }
}