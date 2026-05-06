package com.tunifyx.app.ui.adapter

import android.graphics.Color
import android.view.*
import androidx.recyclerview.widget.*
import com.tunifyx.app.databinding.ItemMoodBinding

data class Mood(val label: String, val emoji: String, val query: String, val color: String)

val MOODS = listOf(
    Mood("Galau",    "😢", "lagu galau indonesia",       "#6366f1"),
    Mood("Semangat", "🔥", "lagu semangat motivasi",      "#f97316"),
    Mood("Santai",   "🌊", "lagu santai lofi",            "#0ea5e9"),
    Mood("Anime",    "⛩️",  "anime ost opening",           "#ec4899"),
    Mood("K-Pop",    "💫", "kpop hits 2024",              "#a855f7"),
    Mood("Hip-Hop",  "🎤", "hip hop rap 2024",            "#eab308"),
    Mood("Study",    "📚", "lofi study music",            "#10b981"),
    Mood("Party",    "🎉", "party dance hits",            "#ef4444"),
)

class MoodAdapter(
    private val onClick: (Mood) -> Unit
) : RecyclerView.Adapter<MoodAdapter.VH>() {

    inner class VH(val b: ItemMoodBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = MOODS.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val mood = MOODS[pos]
        h.b.tvMoodLabel.text  = mood.label
        h.b.tvMoodEmoji.text  = mood.emoji
        h.b.root.setCardBackgroundColor(Color.parseColor(mood.color))
        h.b.root.setOnClickListener { onClick(mood) }
    }
}
