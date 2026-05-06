package com.tunifyx.app.util

import com.tunifyx.app.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

object LyricsHelper {

    private val titleCleanRegex = Regex(
        """[\(\[][^\)\]]*?(official|lyric|audio|mv|video|hd|remaster|vevo|ost|feat|ft\.)[\s\S]*?[\)\]]""",
        RegexOption.IGNORE_CASE
    )

    fun cleanTitle(raw: String) = raw
        .replace(titleCleanRegex, "")
        .replace(Regex("""[\s]*[-–—×|][\s].+$"""), "")
        .replace(Regex("""[\s]*(feat\.?|ft\.?)[\s].+$""", RegexOption.IGNORE_CASE), "")
        .trim()

    fun cleanArtist(raw: String) = raw
        .replace(Regex("""VEVO$|Official$|Music$|Records?$|TV$|Channel$""", RegexOption.IGNORE_CASE), "")
        .replace(Regex("""[\s]*[-–][\s].+$"""), "")
        .trim()

    suspend fun fetchLyrics(track: Track): String? = withContext(Dispatchers.IO) {
        val title  = cleanTitle(track.title)
        val artist = cleanArtist(track.artist)

        // Try lrclib first
        var lyrics = tryLrclib(title, artist)
        if (lyrics == null) lyrics = tryLrclib(title, "")
        if (lyrics == null && artist.isNotEmpty())
            lyrics = tryLyricsOvh(title, artist)

        lyrics
    }

    private suspend fun tryLrclib(title: String, artist: String): String? {
        return try {
            val params = "track_name=${enc(title)}" +
                if (artist.isNotEmpty()) "&artist_name=${enc(artist)}" else ""
            val url  = "https://lrclib.net/api/search?$params"
            val resp = java.net.URL(url).openConnection().apply {
                setRequestProperty("User-Agent", "TunifyX/1.0")
                connectTimeout = 6000
                readTimeout    = 6000
            }.getInputStream().bufferedReader().readText()

            val arr = org.json.JSONArray(resp)
            if (arr.length() == 0) return null

            // Find first with plainLyrics
            for (i in 0 until arr.length()) {
                val obj    = arr.getJSONObject(i)
                val plain  = obj.optString("plainLyrics", "")
                val synced = obj.optString("syncedLyrics", "")
                if (plain.isNotEmpty()) return plain.trim()
                if (synced.isNotEmpty()) {
                    // Strip LRC timestamps
                    return synced
                        .replace(Regex("""\[\d{2}:\d{2}[.:]\d{2,3}\]\s*"""), "")
                        .trim()
                }
            }
            null
        } catch (_: Exception) { null }
    }

    private suspend fun tryLyricsOvh(title: String, artist: String): String? {
        return try {
            val url  = "https://api.lyrics.ovh/v1/${enc(artist)}/${enc(title)}"
            val resp = java.net.URL(url).openConnection().apply {
                connectTimeout = 6000
                readTimeout    = 6000
            }.getInputStream().bufferedReader().readText()
            val lyr = org.json.JSONObject(resp).optString("lyrics", "")
            lyr.takeIf { it.length > 30 }?.trim()
        } catch (_: Exception) { null }
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
}
