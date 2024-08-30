package ru.smalljinn.tiers.data.images.repository.network

import android.graphics.Bitmap
import android.net.Uri
import coil.network.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import ru.smalljinn.tiers.data.images.model.Image
import ru.smalljinn.tiers.data.images.repository.device.PhotoProcessor
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.presentation.ui.screens.settings.API_REGEX
import ru.smalljinn.tiers.presentation.ui.screens.settings.CX_REGEX
import ru.smalljinn.tiers.util.Result
import java.io.IOException

@JvmInline
value class GoogleApiKey(val key: String) {
    fun isCorrect() = key.isNotBlank()
            && key.length == 39
            && key.contains(API_REGEX.toRegex(RegexOption.IGNORE_CASE))
}

@JvmInline
value class CxKey(val cx: String) {
    fun isCorrect() = cx.isNotBlank()
            && cx.length == 17
            && cx.contains(CX_REGEX.toRegex(RegexOption.IGNORE_CASE))
}

class NetworkImageRepositoryImpl(
    private val preferencesRepository: PreferencesRepository,
    private val googleSearchApi: GoogleSearchApi,
    private val photoProcessor: PhotoProcessor
) : NetworkImageRepository {
    override suspend fun getNetworkImagesList(query: String): Flow<Result<List<Image>>> {
        val settings = preferencesRepository.getSettingsStream().first()

        return flow {
            emit(Result.Loading(true))

            val imagesFromQuery = try {
                with(settings) {
                    if (GoogleApiKey(googleApiKey).isCorrect() && CxKey(cx).isCorrect())
                        googleSearchApi.getImages(
                            query = query,
                            cx = settings.cx,
                            apiKey = settings.googleApiKey
                        ).items.map { item -> item.image }
                    else googleSearchApi.getImages(query).items.map { item -> item.image }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Result.Error(message = "IO Error"))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Result.Error(message = "Internet Error"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.Error(message = "Unexpected Error"))
                return@flow
            } finally {
                emit(Result.Loading(false))
            }

            emit(Result.Success(imagesFromQuery))
        }
        //working.emit(true)
        //working.emit(false)
        //return imagesFromQuery
    }

    override suspend fun compressAndSaveImage(bitmap: Bitmap): Flow<Result<Uri>> {
        return flow {
            emit(Result.Loading(true))
            val compressedImage = photoProcessor.compressAndSaveImageFromInternet(bitmap)
            emit(Result.Loading(false))
            emit(Result.Success(compressedImage))
        }
        //working.emit(true)
        //working.emit(false)
        //return compressedImage
    }
}