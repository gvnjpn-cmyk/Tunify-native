package com.tunifyx.app.ui

import android.content.Context
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.tunifyx.app.data.model.Track

fun showTrackMenu(ctx: Context, track: Track, anchor: View, vm: MainViewModel) {
    val menu = PopupMenu(ctx, anchor)
    menu.menu.add(0, 1, 0, "▶ Putar sekarang")
    menu.menu.add(0, 2, 0, "+ Tambah ke Antrian")
    menu.menu.add(0, 3, 0, "⏭ Putar Selanjutnya")
    menu.setOnMenuItemClickListener { item ->
        when (item.itemId) {
            1 -> vm.play(listOf(track), 0)
            2 -> vm.addToQueue(track)
            3 -> {
                // Insert at front of upNext
                val current = com.tunifyx.app.service.PlayerManager.upNext.value
                com.tunifyx.app.service.PlayerManager.clearUpNextAndPrepend(track, current)
            }
        }
        true
    }
    menu.show()
}
