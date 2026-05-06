package com.tunifyx.app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

// ── Piped API (open-source YouTube frontend) ─────────────────
// Returns audio-only stream URLs, no API key needed
interface PipedApi {
    @GET("streams/{videoId}")
    suspend fun getStreams(@Path("videoId") videoId: String): PipedStreams
}

data class PipedStreams(
    val audioStreams: List<AudioStream> = emptyList(),
    val title:        String            = "",
    val uploader:     String            = ""
)

data class AudioStream(
    val url:      String = "",
    val bitrate:  Int    = 0,
    val mimeType: String = "",
    val quality:  String = ""
)

// ── Invidious API (fallback) ──────────────────────────────────
interface InvidiousApi {
    @GET("api/v1/videos/{videoId}")
    suspend fun getVideo(
        @Path("videoId") videoId: String,
        @retrofit2.http.Query("fields") fields: String = "adaptiveFormats,title,author"
    ): InvidiousVideo
}

data class InvidiousVideo(
    val title:           String               = "",
    val author:          String               = "",
    val adaptiveFormats: List<AdaptiveFormat> = emptyList()
)

data class AdaptiveFormat(
    val url:     String = "",
    val type:    String = "",
    val bitrate: String = "0"
)
