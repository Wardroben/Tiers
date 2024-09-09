package ru.smalljinn.tiers.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor

class MockPhotoProcessor : PhotoProcessor {
    override suspend fun compressAndSaveImages(imageUris: List<Uri>): List<Uri> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteImagesFromDevice(imageUris: List<Uri>): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun compressAndSaveImageFromInternet(bitmap: Bitmap): Uri {
        TODO("Not yet implemented")
    }

    override fun readImageBytes(uri: Uri): ByteArray {
        TODO("Not yet implemented")
    }

    override fun importImage(bytes: ByteArray): Uri {
        TODO("Not yet implemented")
    }
}