package ru.smalljinn.tiers.data.images.repository.network

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import ru.smalljinn.tiers.R
import ru.smalljinn.tiers.data.images.model.Image
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor
import ru.smalljinn.tiers.data.images.source.GoogleSearchApi
import ru.smalljinn.tiers.data.preferences.repository.PreferencesRepository
import ru.smalljinn.tiers.presentation.ui.screens.settings.API_REGEX
import ru.smalljinn.tiers.presentation.ui.screens.settings.CX_REGEX
import ru.smalljinn.tiers.util.Result
import java.io.IOException

const val TAG = "NetworkImage"

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
    private val photoProcessor: PhotoProcessor,
    private val appContext: Context
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
                emit(
                    Result.Error(
                        message = appContext.getString(
                            R.string.io_error_sheet_message,
                            e.message
                        )
                    )
                )
                return@flow
            } catch (e: retrofit2.HttpException) {
                e.printStackTrace()
                emit(
                    Result.Error(
                        message = if (e.code() == 400) appContext.getString(R.string.incorrect_keys_sheet_message)
                        else appContext.getString(R.string.http_error_sheet_message)
                    )
                )
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Result.Error(message = appContext.getString(R.string.unexpected_error_sheet_message)))
                return@flow
            } finally {
                emit(Result.Loading(false))
            }

            emit(Result.Success(imagesFromQuery))
        }
    }

    override suspend fun compressAndSaveImage(bitmap: Bitmap): Flow<Result<Uri>> {
        return flow {
            emit(Result.Loading(true))
            val compressedImage = photoProcessor.compressAndSaveImageFromInternet(bitmap)
            emit(Result.Loading(false))
            emit(Result.Success(compressedImage))
        }
    }
}