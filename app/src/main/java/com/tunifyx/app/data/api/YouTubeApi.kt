package com.tunifyx.app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// ── YouTube Data API v3 ──────────────────────────────────────
interface YouTubeApi {

    @GET("search")
    suspend fun search(
        @Query("q")          query:      String,
        @Query("part")       part:       String  = "snippet",
        @Query("type")       type:       String  = "video",
        @Query("maxResults") maxResults: Int     = 20,
        @Query("key")        key:        String,
    ): SearchResponse

    @GET("videos")
    suspend fun getVideos(
        @Query("id")   ids:  String,
        @Query("part") part: String = "contentDetails,snippet",
        @Query("key")  key:  String,
    ): VideoResponse

    @GET("videos")
    suspend fun getTrending(
        @Query("chart")           chart:      String = "mostPopular",
        @Query("videoCategoryId") categoryId: String = "10",
        @Query("regionCode")      region:     String = "ID",
        @Query("maxResults")      maxResults: Int    = 20,
        @Query("part")            part:       String = "snippet,contentDetails",
        @Query("key")             key:        String,
    ): VideoResponse
}

data class SearchResponse(val items: List<SearchItem> = emptyList())
data class SearchItem(
    val id:      SearchId,
    val snippet: Snippet
)
data class SearchId(@SerializedName("videoId") val videoId: String = "")

data class VideoResponse(val items: List<VideoItem> = emptyList())
data class VideoItem(
    val id:             String,
    val snippet:        Snippet,
    val contentDetails: ContentDetails? = null
)

data class Snippet(
    val title:        String       = "",
    val channelTitle: String       = "",
    val thumbnails:   Thumbnails   = Thumbnails()
)
data class Thumbnails(
    val medium: Thumbnail? = null,
    val high:   Thumbnail? = null
)
data class Thumbnail(val url: String = "")

data class ContentDetails(val duration: String = "")
