package com.tunifyx.app.data.db

import android.content.Context
import androidx.room.*
import com.tunifyx.app.data.model.*

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrack::class],
    version  = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao():    TrackDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                ctx.applicationContext,
                AppDatabase::class.java,
                "tunifyx.db"
            ).build().also { INSTANCE = it }
        }
    }
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY addedAt DESC LIMIT 50")
    suspend fun getHistory(): List<Track>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(track: Track)

    @Query("DELETE FROM tracks WHERE videoId = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM tracks WHERE videoId = :id LIMIT 1")
    suspend fun findById(id: String): Track?
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    suspend fun getAll(): List<Playlist>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(pt: PlaylistTrack)

    @Query("DELETE FROM playlist_tracks WHERE playlistId=:pid AND videoId=:vid")
    suspend fun removeTrackFromPlaylist(pid: Long, vid: String)

    @Query("""
        SELECT t.* FROM tracks t 
        INNER JOIN playlist_tracks pt ON t.videoId = pt.videoId 
        WHERE pt.playlistId = :playlistId 
        ORDER BY pt.position ASC
    """)
    suspend fun getTracksForPlaylist(playlistId: Long): List<Track>
}
