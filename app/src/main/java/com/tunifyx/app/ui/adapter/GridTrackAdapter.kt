package com.tunifyx.app.ui.adapter

import android.view.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.tunifyx.app.R
import com.tunifyx.app.data.model.Track
import com.tunifyx.app.databinding.ItemTrackGridBinding

class GridTrackAdapter(
    private val onPlay: (Track, List<Track>, Int) -> Unit
) : ListAdapter<Track, GridTrackAdapter.VH>(DIFF) {

    inner class VH(val b: ItemTrackGridBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTrackGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val track = getItem(pos)
        with(h.b) {
            tvTitle.text  = track.title
            tvArtist.text = track.artist
            Glide.with(root)
                .load(track.thumbnail)
                .placeholder(R.color.bg_card)
                .centerCrop()
                .into(imgThumbnail)
            root.setOnClickListener { onPlay(track, currentList, pos) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(a: Track, b: Track) = a.videoId == b.videoId
            override fun areContentsTheSame(a: Track, b: Track) = a == b
        }
    }
}
