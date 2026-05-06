package com.tunifyx.app.util

import com.tunifyx.app.data.api.*
import com.tunifyx.app.data.model.Track

// ── Parse ISO 8601 duration PT3M45S → "3:45" ────────────────
fun parseDuration(iso: String): Pair<String, Int> {
    val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
    val match = regex.find(iso) ?: return "--:--" to 0
    val h = match.groupValues[1].toIntOrNull() ?: 0
    val m = match.groupValues[2].toIntOrNull() ?: 0
    val s = match.groupValues[3].toIntOrNull() ?: 0
    val total = h * 3600 + m * 60 + s
    val str = if (h > 0) "%d:%02d:%02d".format(h, m, s)
              else "%d:%02d".format(m, s)
    return str to total
}

// ── Convert YouTube search result to Track ────────────────────
fun SearchItem.toTrack(duration: String = "--:--", durationSec: Int = 0) = Track(
    videoId     = id.videoId,
    title       = snippet.title,
    artist      = snippet.channelTitle,
    thumbnail   = snippet.thumbnails.medium?.url
               ?: snippet.thumbnails.high?.url
               ?: "https://i.ytimg.com/vi/${id.videoId}/mqdefault.jpg",
    duration    = duration,
    durationSec = durationSec
)

fun VideoItem.toTrack(): Track {
    val (dur, sec) = parseDuration(contentDetails?.duration ?: "")
    return Track(
        videoId     = id,
        title       = snippet.title,
        artist      = snippet.channelTitle,
        thumbnail   = snippet.thumbnails.medium?.url
                   ?: snippet.thumbnails.high?.url
                   ?: "https://i.ytimg.com/vi/$id/mqdefault.jpg",
        duration    = dur,
        durationSec = sec
    )
}

// ── Format seconds to mm:ss ───────────────────────────────────
fun Int.toTimeString(): String {
    val m = this / 60
    val s = this % 60
    return "%d:%02d".format(m, s)
}

fun Long.toTimeString(): String = (this / 1000).toInt().toTimeString()
