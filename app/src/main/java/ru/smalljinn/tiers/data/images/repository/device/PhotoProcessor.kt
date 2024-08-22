package ru.smalljinn.tiers.data.images.repository.device

import android.graphics.Bitmap
import android.net.Uri

interface PhotoProcessor {
    suspend fun compressAndSaveImages(imageUris: List<Uri>): List<Uri>
    suspend fun deleteImagesFromDevice(imageUris: List<Uri>): Boolean
    suspend fun compressAndSaveImageFromInternet(bitmap: Bitmap): Uri
}