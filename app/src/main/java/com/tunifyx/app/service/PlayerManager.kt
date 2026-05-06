package com.tunifyx.app.service

import com.tunifyx.app.data.api.NetworkModule
import com.tunifyx.app.data.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.media3.common.MediaItem

/**
 * PlayerManager — single source of truth untuk state player
 * Diakses oleh semua Fragment via ViewModel
 */
object PlayerManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ── State flows ───────────────────────────────────────────
    private val _currentTrack  = MutableStateFlow<Track?>(null)
    private val _queue         = MutableStateFlow<List<Track>>(emptyList())
    private val _queueIndex    = MutableStateFlow(-1)
    private val _isPlaying     = MutableStateFlow(false)
    private val _isLoading     = MutableStateFlow(false)
    private val _upNext        = MutableStateFlow<List<Track>>(emptyList())
    private val _error         = MutableStateFlow<String?>(null)

    val currentTrack  = _currentTrack.asStateFlow()
    val queue         = _queue.asStateFlow()
    val queueIndex    = _queueIndex.asStateFlow()
    val isPlaying     = _isPlaying.asStateFlow()
    val isLoading     = _isLoading.asStateFlow()
    val upNext        = _upNext.asStateFlow()
    val error         = _error.asStateFlow()

    // Callback untuk update progress (dari service listener)
    var onProgressUpdate: ((Long, Long) -> Unit)? = null
    var onTrackEndedCallback: (() -> Unit)? = null

    // ── Play ──────────────────────────────────────────────────
    fun playContext(tracks: List<Track>, index: Int = 0) {
        _queue.value      = tracks
        _queueIndex.value = index
        _upNext.value     = emptyList()
        loadAndPlay(tracks[index])
    }

    fun addToQueue(track: Track) {
        _upNext.value = _upNext.value + track
    }

    fun addManyToQueue(tracks: List<Track>) {
        _upNext.value = _upNext.value + tracks
    }

    fun next() {
        val upNext = _upNext.value
        if (upNext.isNotEmpty()) {
            // Play from manual queue first
            val next = upNext.first()
            _upNext.value = upNext.drop(1)
            loadAndPlay(next)
            return
        }
        val queue = _queue.value
        val idx   = _queueIndex.value + 1
        if (idx < queue.size) {
            _queueIndex.value = idx
            loadAndPlay(queue[idx])
        }
    }

    fun prev() {
        val player = MusicService.player ?: return
        if (player.currentPosition > 3000) {
            player.seekTo(0)
            return
        }
        val idx = (_queueIndex.value - 1).coerceAtLeast(0)
        _queueIndex.value = idx
        val queue = _queue.value
        if (queue.isNotEmpty()) loadAndPlay(queue[idx])
    }

    fun togglePlayPause() {
        val player = MusicService.player ?: return
        if (player.isPlaying) player.pause() else player.play()
        _isPlaying.value = player.isPlaying
    }

    fun seekTo(ms: Long) {
        MusicService.player?.seekTo(ms)
    }

    fun setVolume(v: Float) {
        MusicService.player?.volume = v
    }

    fun onTrackEnded() {
        next()
    }

    // ── Internal: resolve stream + load ExoPlayer ─────────────
    private fun loadAndPlay(track: Track) {
        _isLoading.value    = true
        _error.value        = null
        _currentTrack.value = track
        _isPlaying.value    = true
        MusicService.currentTrack = track

        scope.launch {
            val url = resolveStream(track.videoId)
            if (url == null) {
                _error.value   = "Gagal memuat audio, skip..."
                _isLoading.value = false
                delay(1500)
                next()
                return@launch
            }

            withContext(Dispatchers.Main) {
                val player = MusicService.player
                if (player == null) { _isLoading.value = false; return@withContext }
                player.setMediaItem(MediaItem.fromUri(url))
                player.prepare()
                player.play()
                _isPlaying.value  = true
                _isLoading.value  = false
            }
        }
    }

    // ── Stream resolver: Piped → Invidious ────────────────────
    private suspend fun resolveStream(videoId: String): String? =
        withContext(Dispatchers.IO) {
            // Try Piped instances
            for (base in NetworkModule.PIPED_INSTANCES) {
                try {
                    val streams = NetworkModule.pipedApi(base).getStreams(videoId)
                    val best = streams.audioStreams
                        .filter { it.mimeType.contains("audio") }
                        .maxByOrNull { it.bitrate }
                    if (best?.url?.isNotEmpty() == true) return@withContext best.url
                } catch (_: Exception) {}
            }
            // Try Invidious instances
            for (base in NetworkModule.INVIDIOUS_INSTANCES) {
                try {
                    val video = NetworkModule.invidiousApi(base).getVideo(videoId)
                    val best = video.adaptiveFormats
                        .filter { it.type.startsWith("audio/") }
                        .maxByOrNull { it.bitrate.toLongOrNull() ?: 0L }
                    if (best?.url?.isNotEmpty() == true) return@withContext best.url
                } catch (_: Exception) {}
            }
            null
        }

    // ── Sync isPlaying from ExoPlayer ─────────────────────────
    fun syncPlayState() {
        _isPlaying.value = MusicService.player?.isPlaying == true
    }
}

    fun clearUpNextAndPrepend(track: Track, current: List<Track>) {
        _upNext.value = listOf(track) + current
    }
