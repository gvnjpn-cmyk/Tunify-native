package com.tunifyx.app.ui.player

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.tunifyx.app.R
import com.tunifyx.app.databinding.FragmentPlayerBinding
import com.tunifyx.app.ui.MainViewModel
import com.tunifyx.app.util.toTimeString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayerFragment : Fragment(R.layout.fragment_player) {

    private var _b: FragmentPlayerBinding? = null
    private val b get() = _b!!
    private val vm: MainViewModel by activityViewModels()
    private var isSeeking = false
    private var isShuffle = false
    private var repeatMode = 0 // 0=off 1=all 2=one

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentPlayerBinding.bind(view)

        // Close fullscreen
        b.btnClose.setOnClickListener { parentFragmentManager.popBackStack() }

        // Controls
        b.btnPlayPause.setOnClickListener { vm.togglePlayPause() }
        b.btnNext.setOnClickListener     { vm.next() }
        b.btnPrev.setOnClickListener     { vm.prev() }

        // Shuffle
        b.btnShuffle.setOnClickListener {
            isShuffle = !isShuffle
            b.btnShuffle.alpha = if (isShuffle) 1f else 0.4f
        }

        // Repeat
        b.btnRepeat.setOnClickListener {
            repeatMode = (repeatMode + 1) % 3
            b.btnRepeat.alpha = if (repeatMode > 0) 1f else 0.4f
        }

        // Add to playlist button → open lyrics
        b.btnAddPlaylist.setOnClickListener {
            val track = vm.currentTrack.value ?: return@setOnClickListener
            parentFragmentManager.beginTransaction()
                .add(android.R.id.content, LyricsFragment.newInstance(track))
                .addToBackStack("lyrics")
                .commit()
        }

        // SeekBar
        b.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, p: Int, user: Boolean) {
                if (user) b.tvPosition.text = (p.toLong() * 1000L).toTimeString()
            }
            override fun onStartTrackingTouch(sb: SeekBar) { isSeeking = true }
            override fun onStopTrackingTouch(sb: SeekBar) {
                isSeeking = false
                vm.seekTo(sb.progress.toLong() * 1000L)
            }
        })

        // Observe current track
        viewLifecycleOwner.lifecycleScope.launch {
            vm.currentTrack.collectLatest { track ->
                track ?: return@collectLatest
                b.tvTitle.text  = track.title
                b.tvTitle.isSelected = true  // enable marquee scroll
                b.tvArtist.text = track.artist
                Glide.with(this@PlayerFragment)
                    .load(track.thumbnail)
                    .placeholder(R.color.bg_card)
                    .centerCrop()
                    .into(b.imgAlbumArt)
            }
        }

        // Observe play state
        viewLifecycleOwner.lifecycleScope.launch {
            vm.isPlaying.collectLatest { playing ->
                b.btnPlayPause.setImageResource(
                    if (playing) android.R.drawable.ic_media_pause
                    else         android.R.drawable.ic_media_play
                )
                // Scale album art when playing
                val scale = if (playing) 1f else 0.85f
                b.imgAlbumArt.animate().scaleX(scale).scaleY(scale).setDuration(300).start()
            }
        }

        // Observe loading
        viewLifecycleOwner.lifecycleScope.launch {
            vm.isLoading.collectLatest { loading ->
                b.btnPlayPause.alpha = if (loading) 0.5f else 1f
            }
        }

        // Observe position/duration
        viewLifecycleOwner.lifecycleScope.launch {
            vm.position.collectLatest { pos ->
                if (!isSeeking) {
                    val dur = vm.duration.value
                    b.tvPosition.text = pos.toTimeString()
                    b.tvDuration.text = dur.toTimeString()
                    if (dur > 0) {
                        b.seekBar.max      = (dur / 1000L).toInt()
                        b.seekBar.progress = (pos / 1000L).toInt()
                    }
                }
            }
        }

        // Error
        viewLifecycleOwner.lifecycleScope.launch {
            vm.error.collectLatest { err ->
                if (err != null) {
                    android.widget.Toast.makeText(requireContext(), err, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
