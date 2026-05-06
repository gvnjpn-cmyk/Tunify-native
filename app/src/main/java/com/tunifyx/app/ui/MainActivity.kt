package com.tunifyx.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.tunifyx.app.R
import com.tunifyx.app.databinding.ActivityMainBinding
import com.tunifyx.app.service.MusicService
import com.tunifyx.app.ui.home.HomeFragment
import com.tunifyx.app.ui.library.LibraryFragment
import com.tunifyx.app.ui.player.PlayerFragment
import com.tunifyx.app.ui.search.SearchFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dark system bars
        window.statusBarColor     = getColor(R.color.bg_primary)
        window.navigationBarColor = getColor(R.color.bg_primary)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
        }

        // Start MusicService
        val serviceIntent = Intent(this, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Load home fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, HomeFragment())
                .commit()
        }

        // Bottom navigation
        b.bottomNav.setOnItemSelectedListener { item ->
            val frag = when (item.itemId) {
                R.id.nav_home    -> HomeFragment()
                R.id.nav_search  -> SearchFragment()
                R.id.nav_library -> LibraryFragment()
                else             -> HomeFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.navHostFragment, frag)
                .commit()
            true
        }

        setupPlayerBar()
    }

    private fun setupPlayerBar() {
        // Show/hide player bar
        lifecycleScope.launch {
            vm.currentTrack.collectLatest { track ->
                if (track == null) {
                    b.playerBar.root.visibility = View.GONE
                } else {
                    b.playerBar.root.visibility = View.VISIBLE
                    b.playerBar.tvTitle.text  = track.title
                    b.playerBar.tvTitle.isSelected = true  // enable marquee scroll
                    b.playerBar.tvArtist.text = track.artist
                    Glide.with(this@MainActivity)
                        .load(track.thumbnail)
                        .placeholder(R.color.bg_card)
                        .centerCrop()
                        .into(b.playerBar.imgThumbnail)
                }
            }
        }

        // Progress line
        lifecycleScope.launch {
            vm.position.collectLatest { pos ->
                val dur = vm.duration.value
                if (dur > 0) {
                    val pct = (pos.toFloat() / dur * b.playerBar.root.width).toInt()
                    b.playerBar.progressLine.layoutParams.width = pct
                    b.playerBar.progressLine.requestLayout()
                }
            }
        }

        // Play/pause state
        lifecycleScope.launch {
            vm.isPlaying.collectLatest { playing ->
                b.playerBar.btnPlayPause.setImageResource(
                    if (playing) android.R.drawable.ic_media_pause
                    else android.R.drawable.ic_media_play
                )
            }
        }

        // Player bar controls
        b.playerBar.btnPlayPause.setOnClickListener { vm.togglePlayPause() }
        b.playerBar.btnNext.setOnClickListener     { vm.next() }
        b.playerBar.btnPrev.setOnClickListener     { vm.prev() }

        // Tap player bar → open fullscreen player
        b.playerBar.root.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, PlayerFragment())
                .addToBackStack("player")
                .commit()
        }
    }
}
