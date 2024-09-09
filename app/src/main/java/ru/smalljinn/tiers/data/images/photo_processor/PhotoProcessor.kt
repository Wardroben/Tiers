package ru.smalljinn.tiers.data.images.photo_processor

import android.graphics.Bitmap
import android.net.Uri

interface PhotoProcessor {
    suspend fun compressAndSaveImages(imageUris: List<Uri>): List<Uri>
    suspend fun deleteImagesFromDevice(imageUris: List<Uri>): Boolean
    suspend fun compressAndSaveImageFromInternet(bitmap: Bitmap): Uri
    fun readImageBytes(uri: Uri): ByteArray
    fun importImage(bytes: ByteArray): Uri
}