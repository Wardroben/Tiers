package ru.smalljinn.tiers.data.images.repository.device

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface DevicePhotoRepository {
    val imageProcessingStream: Flow<Boolean>
    suspend fun insertPhotos(uris: List<Uri>): List<Uri>
}