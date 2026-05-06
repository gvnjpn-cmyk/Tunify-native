package com.tunifyx.app.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.tunifyx.app.R
import com.tunifyx.app.data.model.Track
import com.tunifyx.app.ui.MainActivity
import kotlinx.coroutines.*

/**
 * MusicService — ExoPlayer foreground service
 *
 * Cara kerja:
 *   1. App request play track → call MusicService via PlayerManager
 *   2. Service resolve stream URL (Piped → Invidious → fallback)
 *   3. ExoPlayer load & play audio
 *   4. Foreground notification muncul → Android tidak kill proses
 *   5. Audio jalan di background dengan notif play/pause/next
 */
class MusicService : MediaSessionService() {

    companion object {
        const val CHANNEL_ID  = "tunifyx_music"
        const val NOTIF_ID    = 42
        // Static reference agar Activity bisa akses player
        var player: ExoPlayer? = null
        var currentTrack: Track? = null
    }

    private lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        player = ExoPlayer.Builder(this).build().also { p ->
            p.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updateNotification()
                }
                override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                    updateNotification()
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        // Auto next handled by PlayerManager
                        PlayerManager.onTrackEnded()
                    }
                }
            })
        }

        mediaSession = MediaSession.Builder(this, player!!).build()
    }

    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification())
        return START_STICKY
    }

    fun updateNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val track  = currentTrack
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(track?.title  ?: "TunifyX")
            .setContentText(track?.artist  ?: "")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pi)
            .setOngoing(player?.isPlaying == true)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "TunifyX Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description   = "Background music playback"
                setShowBadge(false)
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }

    override fun onDestroy() {
        mediaSession.release()
        player?.release()
        player = null
        super.onDestroy()
    }
}
