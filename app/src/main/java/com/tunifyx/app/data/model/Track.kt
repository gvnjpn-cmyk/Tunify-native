package com.tunifyx.app.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    val videoId:     String,
    val title:       String,
    val artist:      String,
    val thumbnail:   String,
    val duration:    String  = "--:--",
    val durationSec: Int     = 0,
    val streamUrl:   String? = null,   // cached Piped/Invidious URL
    val addedAt:     Long    = System.currentTimeMillis()
) : Parcelable

@Parcelize
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id:        Long   = 0,
    val name:      String,
    val cover:     String = "",
    val createdAt: Long   = System.currentTimeMillis()
) : Parcelable

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "videoId"])
data class PlaylistTrack(
    val playlistId: Long,
    val videoId:    String,
    val position:   Int = 0
)

data class PlaylistWithTracks(
    val playlist: Playlist,
    val tracks:   List<Track>
)
