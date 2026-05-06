package com.tunifyx.app.ui.player

import android.os.Bundle
import android.view.*
import androidx.fragment.app.*
import androidx.lifecycle.lifecycleScope
import com.tunifyx.app.R
import com.tunifyx.app.databinding.FragmentLyricsBinding
import com.tunifyx.app.data.model.Track
import com.tunifyx.app.util.LyricsHelper
import kotlinx.coroutines.*

class LyricsFragment : Fragment(R.layout.fragment_lyrics) {

    private var _b: FragmentLyricsBinding? = null
    private val b get() = _b!!
    private var currentJob: Job? = null

    companion object {
        fun newInstance(track: Track) = LyricsFragment().apply {
            arguments = Bundle().apply {
                putParcelable("track", track)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentLyricsBinding.bind(view)

        val track = arguments?.getParcelable<Track>("track")

        b.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Toggle manual search form
        b.btnSearch.setOnClickListener {
            val visible = b.layoutSearchForm.visibility == View.VISIBLE
            b.layoutSearchForm.visibility = if (visible) View.GONE else View.VISIBLE
            if (!visible && track != null) {
                b.etLyricsTitle.setText(LyricsHelper.cleanTitle(track.title))
                b.etLyricsArtist.setText(LyricsHelper.cleanArtist(track.artist))
            }
        }

        b.btnSearchLyrics.setOnClickListener {
            val title  = b.etLyricsTitle.text?.toString()?.trim() ?: return@setOnClickListener
            val artist = b.etLyricsArtist.text?.toString()?.trim() ?: ""
            if (title.isEmpty()) return@setOnClickListener
            b.layoutSearchForm.visibility = View.GONE
            fetchManual(title, artist)
        }

        b.btnTryOther.setOnClickListener {
            b.layoutSearchForm.visibility = View.VISIBLE
            b.layoutNotFound.visibility   = View.GONE
            if (track != null) {
                b.etLyricsTitle.setText(LyricsHelper.cleanTitle(track.title))
                b.etLyricsArtist.setText(LyricsHelper.cleanArtist(track.artist))
            }
        }

        if (track != null) {
            b.tvTitle.text  = track.title
            b.tvArtist.text = track.artist
            fetchLyrics(track)
        }
    }

    private fun fetchLyrics(track: Track) {
        currentJob?.cancel()
        showLoading()
        currentJob = viewLifecycleOwner.lifecycleScope.launch {
            val lyrics = LyricsHelper.fetchLyrics(track)
            showResult(lyrics)
        }
    }

    private fun fetchManual(title: String, artist: String) {
        currentJob?.cancel()
        showLoading()
        currentJob = viewLifecycleOwner.lifecycleScope.launch {
            val fakeTrack = com.tunifyx.app.data.model.Track(
                videoId   = "",
                title     = title,
                artist    = artist,
                thumbnail = ""
            )
            val lyrics = LyricsHelper.fetchLyrics(fakeTrack)
            showResult(lyrics)
        }
    }

    private fun showLoading() {
        b.progressBar.visibility  = View.VISIBLE
        b.scrollView.visibility   = View.GONE
        b.layoutNotFound.visibility = View.GONE
    }

    private fun showResult(lyrics: String?) {
        b.progressBar.visibility = View.GONE
        if (!lyrics.isNullOrEmpty()) {
            b.tvLyrics.text         = lyrics
            b.scrollView.visibility = View.VISIBLE
            b.layoutNotFound.visibility = View.GONE
        } else {
            b.scrollView.visibility     = View.GONE
            b.layoutNotFound.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        currentJob?.cancel()
        super.onDestroyView()
        _b = null
    }
}
