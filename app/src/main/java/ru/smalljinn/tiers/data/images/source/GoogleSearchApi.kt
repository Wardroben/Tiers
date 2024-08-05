package ru.smalljinn.tiers.data.images.source

import retrofit2.http.GET
import retrofit2.http.Query
import ru.smalljinn.tiers.BuildConfig
import ru.smalljinn.tiers.data.images.model.Response

const val JSON_FORMAT = "application/json"
const val BASE_URL = "https://www.googleapis.com"
private const val IMAGE_SEARCH_TYPE = "image"

interface GoogleSearchApi {
    @GET("/customsearch/v1")
    suspend fun getImages(
        @Query("q") query: String,
        @Query("key") apiKey: String = BuildConfig.googleSearchApiKey,
        @Query("cx") cx: String = BuildConfig.cx,
        @Query("searchType") searchType: String = IMAGE_SEARCH_TYPE
    ): Response
}



