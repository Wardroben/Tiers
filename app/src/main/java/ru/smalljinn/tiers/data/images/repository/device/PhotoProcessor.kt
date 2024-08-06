package ru.smalljinn.tiers.data.images.repository.device

import android.net.Uri

interface PhotoProcessor {
    suspend fun compressAndSaveImages(imageUris: List<Uri>): List<Uri>
}