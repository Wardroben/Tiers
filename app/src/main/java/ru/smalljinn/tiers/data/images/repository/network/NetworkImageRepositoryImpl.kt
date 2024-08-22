package ru.smalljinn.tiers.data.images.repository.network

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import ru.smalljinn.tiers.data.images.model.Image
import ru.smalljinn.tiers.data.images.repository.device.PhotoProcessor
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi

class NetworkImageRepositoryImpl(
    private val googleSearchApi: GoogleSearchApi,
    private val photoProcessor: PhotoProcessor
) : NetworkImageRepository {
    override val working: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override suspend fun getNetworkImagesList(query: String): List<Image> {
        working.emit(true)
        val imagesFromQuery = googleSearchApi.getImages(query).items.map { item -> item.image }
        working.emit(false)
        return imagesFromQuery
    }

    override suspend fun compressAndSaveImage(bitmap: Bitmap): Uri {
        working.emit(true)
        val compressedImage = photoProcessor.compressAndSaveImageFromInternet(bitmap)
        working.emit(false)
        return compressedImage
    }
}