package com.tunifyx.app.ui.adapter

import android.view.*
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.tunifyx.app.R
import com.tunifyx.app.data.model.Track
import com.tunifyx.app.databinding.ItemQuickPickBinding

class QuickPickAdapter(
    private val onPlay: (Track, List<Track>, Int) -> Unit
) : ListAdapter<Track, QuickPickAdapter.VH>(DIFF) {

    inner class VH(val b: ItemQuickPickBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemQuickPickBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val track = getItem(pos)
        h.b.tvTitle.text = track.title
        Glide.with(h.b.root)
            .load(track.thumbnail)
            .placeholder(R.color.bg_card)
            .centerCrop()
            .into(h.b.imgThumbnail)
        h.b.root.setOnClickListener { onPlay(track, currentList, pos) }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Track>() {
            override fun areItemsTheSame(a: Track, b: Track) = a.videoId == b.videoId
            override fun areContentsTheSame(a: Track, b: Track) = a == b
        }
    }
}
