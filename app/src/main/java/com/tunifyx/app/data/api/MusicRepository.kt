package com.tunifyx.app.data.api

import android.content.Context
import com.tunifyx.app.BuildConfig
import com.tunifyx.app.data.db.AppDatabase
import com.tunifyx.app.data.model.Track
import com.tunifyx.app.util.toTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(ctx: Context) {

    private val db    = AppDatabase.get(ctx)
    private val ytApi = NetworkModule.youtubeApi
    private val key   = BuildConfig.YT_API_KEY

    // ── YouTube Search ────────────────────────────────────────
    suspend fun search(query: String): List<Track> = withContext(Dispatchers.IO) {
        val searchRes = ytApi.search(query = smartQuery(query), key = key)
        val ids       = searchRes.items.mapNotNull { it.id.videoId.takeIf { v -> v.isNotEmpty() } }
        if (ids.isEmpty()) return@withContext emptyList()

        val detailRes = ytApi.getVideos(ids.joinToString(","), key = key)
        detailRes.items.map { it.toTrack() }
    }

    // ── Trending ID ───────────────────────────────────────────
    suspend fun getTrending(): List<Track> = withContext(Dispatchers.IO) {
        ytApi.getTrending(key = key).items.map { it.toTrack() }
    }

    // ── History ───────────────────────────────────────────────
    suspend fun getHistory(): List<Track>     = db.trackDao().getHistory()
    suspend fun addToHistory(track: Track)    = db.trackDao().upsert(track)

    // ── Playlist ──────────────────────────────────────────────
    suspend fun getPlaylists()                = db.playlistDao().getAll()
    suspend fun getPlaylistTracks(id: Long)   = db.playlistDao().getTracksForPlaylist(id)

    // ── Smart query (same logic as TunifyX web) ───────────────
    private fun smartQuery(raw: String): String {
        val q = raw.lowercase()
        return when {
            q.contains(Regex("lagu indo|pop indo|galau|dangdut|koplo")) -> "$raw official audio"
            q.contains(Regex("anime|vocaloid|jpop|j-pop|ost"))          -> "$raw official audio"
            q.contains(Regex("kpop|k-pop|bts|blackpink|twice"))         -> "$raw official mv"
            q.contains(Regex("lofi|lo-fi|chill|study"))                 -> "$raw music"
            else                                                         -> "$raw official audio"
        }
    }
}
