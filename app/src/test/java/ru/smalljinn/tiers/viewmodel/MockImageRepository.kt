package ru.smalljinn.tiers.viewmodel

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.images.repository.device.DeviceImageRepository

class MockImageRepository : DeviceImageRepository {
    override val imageProcessingStream: Flow<Boolean>
        get() = TODO("Not yet implemented")

    override suspend fun insertPhotos(uris: List<Uri>): List<Uri> {
        TODO("Not yet implemented")
    }

    override suspend fun deletePhotos(uris: List<Uri>): Boolean {
        TODO("Not yet implemented")
    }
}