package com.tunifyx.app.ui

import android.app.Application
import androidx.lifecycle.*
import com.tunifyx.app.data.api.MusicRepository
import com.tunifyx.app.data.model.Track
import com.tunifyx.app.service.PlayerManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val repo = MusicRepository(app)

    // ── Player state (from PlayerManager flows) ───────────────
    val currentTrack = PlayerManager.currentTrack
    val isPlaying    = PlayerManager.isPlaying
    val isLoading    = PlayerManager.isLoading
    val queue        = PlayerManager.queue
    val upNext       = PlayerManager.upNext
    val error        = PlayerManager.error

    // ── Progress (updated by service timer) ───────────────────
    private val _position  = MutableStateFlow(0L)
    private val _duration  = MutableStateFlow(0L)
    val position = _position.asStateFlow()
    val duration = _duration.asStateFlow()

    // ── Search ────────────────────────────────────────────────
    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    private val _searchLoading = MutableStateFlow(false)
    private val _searchError   = MutableStateFlow<String?>(null)
    val searchResults = _searchResults.asStateFlow()
    val searchLoading = _searchLoading.asStateFlow()
    val searchError   = _searchError.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        if (query.isBlank()) { _searchResults.value = emptyList(); return }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350) // debounce
            _searchLoading.value = true
            _searchError.value   = null
            try {
                _searchResults.value = repo.search(query)
            } catch (e: Exception) {
                _searchError.value = "Pencarian gagal. Cek koneksi internet."
            } finally {
                _searchLoading.value = false
            }
        }
    }

    // ── Trending / Home ───────────────────────────────────────
    private val _trending = MutableStateFlow<List<Track>>(emptyList())
    private val _history  = MutableStateFlow<List<Track>>(emptyList())
    val trending = _trending.asStateFlow()
    val history  = _history.asStateFlow()

    fun loadHome() {
        viewModelScope.launch {
            try { _trending.value = repo.getTrending() } catch (_: Exception) {}
            try { _history.value  = repo.getHistory()  } catch (_: Exception) {}
        }
    }

    // ── Player actions ────────────────────────────────────────
    fun play(tracks: List<Track>, index: Int = 0) {
        viewModelScope.launch { repo.addToHistory(tracks[index]) }
        PlayerManager.playContext(tracks, index)
    }

    fun addToQueue(track: Track)       = PlayerManager.addToQueue(track)
    fun addManyToQueue(tracks: List<Track>) = PlayerManager.addManyToQueue(tracks)
    fun togglePlayPause()              = PlayerManager.togglePlayPause()
    fun next()                         = PlayerManager.next()
    fun prev()                         = PlayerManager.prev()
    fun seekTo(ms: Long)               = PlayerManager.seekTo(ms)

    // ── Progress polling ──────────────────────────────────────
    init {
        PlayerManager.onProgressUpdate = { pos, dur ->
            _position.value = pos
            _duration.value = dur
        }
        viewModelScope.launch {
            while (true) {
                delay(500)
                val player = com.tunifyx.app.service.MusicService.player ?: continue
                _position.value = player.currentPosition
                _duration.value = player.duration.coerceAtLeast(0)
                PlayerManager.syncPlayState()
            }
        }
    }
}
