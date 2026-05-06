package com.tunifyx.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tunifyx.app.R
import com.tunifyx.app.data.model.Track
import com.tunifyx.app.databinding.ItemTrackBinding

class TrackAdapter(
    private val onPlay:    (Track, List<Track>, Int) -> Unit,
    private val onMore:    (Track, View) -> Unit,
    private var activeId:  String = ""
) : ListAdapter<Track, TrackAdapter.VH>(DIFF) {

    fun setActiveId(id: String) {
        val old = activeId
        activeId = id
        // Refresh old + new active item only
        currentList.forEachIndexed { i, t ->
            if (t.videoId == old || t.videoId == id) notifyItemChanged(i)
        }
    }

    inner class VH(val b: ItemTrackBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val track   = getItem(pos)
        val isActive = track.videoId == activeId
        with(h.b) {
            tvTitle.text    = track.title
            tvArtist.text   = track.artist
            tvDuration.text = track.duration

            // Active track → green title + show equalizer
            tvTitle.setTextColor(
                root.context.getColor(
                    if (isActive) R.color.accent_green else R.color.text_primary
                )
            )
            layoutEqualizer.visibility = if (isActive) View.VISIBLE else View.GONE

            Glide.with(root)
                .load(track.thumbnail)
                .placeholder(R.color.bg_card)
                .centerCrop()
                .into(imgThumbnail)

            root.setOnClickListener {
                onPlay(track, currentList, pos)
            }
            btnMore.setOnClickListener { v -> onMore(track, v) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(a: Track, b: Track) = a.videoId == b.videoId
            override fun areContentsTheSame(a: Track, b: Track) = a == b
        }
    }
}
