package ru.smalljinn.tiers.features.tier_edit.usecase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import ru.smalljinn.tiers.util.Result
import java.io.IOException

data class InternetImage(val link: String)

private const val TAG = "Scraper"
private const val CONNECTION_TIMEOUT = 5000

class GetInternetImagesUseCase {
    suspend operator fun invoke(query: String): Flow<Result<List<InternetImage>>> {
        return channelFlow {
            trySend(Result.Loading(isLoading = true))
            val formattedQuery = query.replace(" ", "+")
            try {
                withContext(Dispatchers.IO) {
                    val document =
                        Jsoup
                            .connect("https://www.google.com/search?q=$formattedQuery&num=10&tbm=isch")
                            .timeout(CONNECTION_TIMEOUT)
                            .userAgent("Mozilla")
                            .get()
                    val links = document.select("img").map { it.attr("src") }
                    val images = if (links.size > 1) links.takeLast(links.size - 1) else emptyList()
                    trySend(Result.Loading(false))
                    trySend(Result.Success(images.map { InternetImage(it) }))
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                e.printStackTrace()
                trySend(Result.Error("Error loading images"))
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
                e.printStackTrace()
                trySend(Result.Error("Connection error"))
            } finally {
                trySend(Result.Loading(false))
            }
        }
    }
}

